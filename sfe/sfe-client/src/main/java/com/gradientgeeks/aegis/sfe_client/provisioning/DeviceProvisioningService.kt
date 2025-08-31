package com.gradientgeeks.aegis.sfe_client.provisioning

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.gradientgeeks.aegis.sfe_client.api.AegisApiService
import com.gradientgeeks.aegis.sfe_client.crypto.CryptographyService
import com.gradientgeeks.aegis.sfe_client.model.DeviceRegistrationRequest
import com.gradientgeeks.aegis.sfe_client.model.DeviceRegistrationResponse
import com.gradientgeeks.aegis.sfe_client.security.IntegrityValidationService
import com.gradientgeeks.aegis.sfe_client.security.DeviceFingerprintingService
import com.gradientgeeks.aegis.sfe_client.security.toApiData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.UUID

/**
 * Service responsible for device provisioning and registration with the Aegis Security API.
 * 
 * Handles the secure handshake process where a device establishes its identity
 * and receives cryptographic credentials for subsequent API operations.
 * 
 * The provisioning process includes:
 * 1. Integrity verification (Google Play Integrity API)
 * 2. Registration key validation
 * 3. Device credential generation and storage
 * 4. Secure key storage in Android Keystore
 */
class DeviceProvisioningService(
    private val context: Context,
    private val apiService: AegisApiService,
    private val cryptographyService: CryptographyService,
    private val integrityService: IntegrityValidationService,
    private val fingerprintingService: DeviceFingerprintingService
) {
    
    companion object {
        private const val TAG = "DeviceProvisioningService"
        private const val PREFS_NAME = "aegis_device_prefs"  // Keep original name to preserve credentials
        private const val PREF_DEVICE_ID = "device_id"
        private const val PREF_IS_PROVISIONED = "is_provisioned"
        private const val PREF_CLIENT_ID = "client_id"
        private const val PREF_PROVISIONING_TIMESTAMP = "provisioning_timestamp"
        
        // Keystore aliases
        private const val SECRET_KEY_ALIAS = "aegis_device_secret_key"
        
        // Thread safety
        private val provisioningMutex = Mutex()
        @Volatile
        private var isProvisioningInProgress = false
    }
    
    private val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Checks if the device is already provisioned.
     * 
     * @return True if device has valid credentials, false otherwise
     */
    fun isDeviceProvisioned(): Boolean {
        val isProvisioned = sharedPrefs.getBoolean(PREF_IS_PROVISIONED, false)
        val deviceId = sharedPrefs.getString(PREF_DEVICE_ID, null)
        val hasSecretKey = cryptographyService.keyExists(SECRET_KEY_ALIAS)
        
        val result = isProvisioned && !deviceId.isNullOrEmpty() && hasSecretKey
        
        Log.d(TAG, "Device provisioning status: $result")
        Log.d(TAG, "  - Is provisioned flag: $isProvisioned")
        Log.d(TAG, "  - Has device ID: ${!deviceId.isNullOrEmpty()}")
        Log.d(TAG, "  - Has secret key: $hasSecretKey")
        
        return result
    }
    
    /**
     * Gets the current device ID if provisioned.
     * 
     * @return Device ID or null if not provisioned
     */
    fun getDeviceId(): String? {
        return sharedPrefs.getString(PREF_DEVICE_ID, null)
    }
    
    /**
     * Gets the client ID used during provisioning.
     * 
     * @return Client ID or null if not provisioned
     */
    fun getClientId(): String? {
        return sharedPrefs.getString(PREF_CLIENT_ID, null)
    }
    
    /**
     * Performs device provisioning with the Aegis Security API.
     * 
     * This is the main entry point for establishing device credentials.
     * Should only be called once per device installation.
     * 
     * @param clientId The client identifier for this application
     * @param registrationKey The shared registration key provided by administrators
     * @param forceReprovisioning If true, will clear existing credentials and reprovision
     * @return ProvisioningResult indicating success or failure with details
     */
    suspend fun provisionDevice(
        clientId: String, 
        registrationKey: String,
        forceReprovisioning: Boolean = false
    ): ProvisioningResult = withContext(Dispatchers.IO) {
        
        Log.i(TAG, "Starting device provisioning for client: $clientId (force=$forceReprovisioning)")
        
        // Thread safety - prevent multiple simultaneous provisioning attempts
        provisioningMutex.withLock {
            if (isProvisioningInProgress) {
                Log.w(TAG, "Provisioning already in progress, waiting...")
                // Wait a bit and check again
                delay(500)
                if (isDeviceProvisioned()) {
                    Log.i(TAG, "Device was provisioned by another thread")
                    return@withContext ProvisioningResult.AlreadyProvisioned
                }
            }
            isProvisioningInProgress = true
        }
        
        try {
            // Step 1: Check if already provisioned
            if (isDeviceProvisioned() && !forceReprovisioning) {
                Log.w(TAG, "Device is already provisioned")
                return@withContext ProvisioningResult.AlreadyProvisioned
            }
            
            // Step 1.5: If force reprovisioning, clear existing data
            if (forceReprovisioning) {
                Log.i(TAG, "Force reprovisioning requested, clearing existing credentials")
                clearProvisioningData()
                // Small delay to ensure cleanup is complete
                delay(100)
            }
            
            // Step 2: Generate device nonce for integrity validation
            val nonce = generateNonce()
            Log.d(TAG, "Generated nonce for integrity validation")
            
            // Step 3: Get integrity token (simulated for demo)
            val integrityToken = integrityService.getIntegrityToken(nonce)
            Log.d(TAG, "Obtained integrity token: ${if (integrityToken != null) "present" else "null"}")
            
            // Step 3.5: Generate device fingerprint for fraud detection
            Log.d(TAG, "Generating device fingerprint for fraud detection")
            val deviceFingerprint = fingerprintingService.generateDeviceFingerprint()
            val fingerprintData = deviceFingerprint.toApiData()
            Log.d(TAG, "Device fingerprint generated: ${fingerprintData.compositeHash}")
            
            // Step 4: Create registration request
            val registrationRequest = DeviceRegistrationRequest(
                clientId = clientId,
                registrationKey = registrationKey,
                integrityToken = integrityToken,
                deviceFingerprint = fingerprintData
            )
            
            // Step 5: Call registration API
            Log.d(TAG, "Sending registration request to API")
            val response = apiService.registerDevice(registrationRequest)
            
            // Step 6: Process response
            if (response.isSuccessful && response.body() != null) {
                val registrationResponse = response.body()!!
                Log.i(TAG, "Registration successful. Device ID: ${registrationResponse.deviceId}")
                
                // Step 7: Store credentials securely
                val stored = storeDeviceCredentials(
                    clientId = clientId,
                    deviceId = registrationResponse.deviceId,
                    secretKey = registrationResponse.secretKey
                )
                
                if (stored) {
                    Log.i(TAG, "Device provisioning completed successfully")
                    ProvisioningResult.Success(registrationResponse.deviceId)
                } else {
                    Log.e(TAG, "Failed to store device credentials")
                    ProvisioningResult.StorageError
                }
                
            } else {
                Log.e(TAG, "Registration failed. HTTP ${response.code()}: ${response.message()}")
                ProvisioningResult.ApiError(response.code(), response.message())
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Device provisioning failed with exception", e)
            ProvisioningResult.NetworkError(e.message ?: "Unknown network error")
        } finally {
            provisioningMutex.withLock {
                isProvisioningInProgress = false
            }
        }
    }
    
    /**
     * Stores device credentials securely after successful registration.
     * 
     * @param clientId The client identifier
     * @param deviceId The assigned device identifier
     * @param secretKey The Base64-encoded secret key
     * @return True if storage was successful, false otherwise
     */
    private fun storeDeviceCredentials(
        clientId: String,
        deviceId: String,
        secretKey: String
    ): Boolean {
        return try {
            Log.d(TAG, "Storing device credentials securely")
            
            // Step 1: Decode and store secret key in Android Keystore
            val secretKeyBytes = Base64.decode(secretKey, Base64.NO_WRAP)
            val keyStored = cryptographyService.storeExternalSecretKey(
                SECRET_KEY_ALIAS, 
                secretKeyBytes
            )
            
            if (!keyStored) {
                Log.e(TAG, "Failed to store secret key in Android Keystore")
                return false
            }
            
            // Step 2: Store device metadata in SharedPreferences
            sharedPrefs.edit()
                .putString(PREF_DEVICE_ID, deviceId)
                .putString(PREF_CLIENT_ID, clientId)
                .putBoolean(PREF_IS_PROVISIONED, true)
                .putLong(PREF_PROVISIONING_TIMESTAMP, System.currentTimeMillis())
                .apply()
            
            Log.d(TAG, "Device credentials stored successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store device credentials", e)
            false
        }
    }
    
    /**
     * Clears all device provisioning data.
     * 
     * This will force the device to go through provisioning again.
     * Should only be used for testing or when explicitly requested by user.
     * 
     * @return True if cleanup was successful, false otherwise
     */
    fun clearProvisioningData(): Boolean {
        return try {
            Log.i(TAG, "Clearing device provisioning data")
            
            // Clear secret key from keystore
            cryptographyService.deleteKey(SECRET_KEY_ALIAS)
            
            // Clear SharedPreferences
            sharedPrefs.edit().clear().apply()
            
            Log.i(TAG, "Device provisioning data cleared successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear device provisioning data", e)
            false
        }
    }
    
    /**
     * Gets the secret key for HMAC operations.
     * 
     * @return SecretKey instance or null if not available
     */
    fun getDeviceSecretKey() = cryptographyService.getSecretKey(SECRET_KEY_ALIAS)
    
    /**
     * Gets provisioning information for debugging/status purposes.
     * 
     * @return ProvisioningInfo with current status
     */
    fun getProvisioningInfo(): ProvisioningInfo {
        return ProvisioningInfo(
            isProvisioned = isDeviceProvisioned(),
            deviceId = getDeviceId(),
            clientId = getClientId(),
            provisioningTimestamp = sharedPrefs.getLong(PREF_PROVISIONING_TIMESTAMP, 0L),
            hasSecretKey = cryptographyService.keyExists(SECRET_KEY_ALIAS)
        )
    }
    
    /**
     * Generates a cryptographically secure nonce for integrity validation.
     * 
     * @return Base64-encoded nonce
     */
    private fun generateNonce(): String {
        val nonce = UUID.randomUUID().toString().replace("-", "")
        return Base64.encodeToString(nonce.toByteArray(), Base64.NO_WRAP)
    }
    
    /**
     * Handles authentication failure by checking if reprovisioning is needed.
     * 
     * This method should be called when requests fail with 401/403 errors
     * to determine if the device needs to be reprovisioned.
     * 
     * @return True if device should be reprovisioned, false otherwise
     */
    fun shouldReprovisionOnAuthFailure(): Boolean {
        if (!isDeviceProvisioned()) {
            Log.d(TAG, "Device not provisioned, should provision")
            return false // Not reprovisioning, just needs initial provisioning
        }
        
        val provisioningTimestamp = sharedPrefs.getLong(PREF_PROVISIONING_TIMESTAMP, 0L)
        val currentTime = System.currentTimeMillis()
        val hoursSinceProvisioning = (currentTime - provisioningTimestamp) / (1000 * 60 * 60)
        
        Log.w(TAG, "Auth failure detected. Device was provisioned $hoursSinceProvisioning hours ago")
        
        // If device was provisioned very recently (within 1 hour) and still failing,
        // it might indicate stale Keystore entries
        return hoursSinceProvisioning < 1
    }
}

/**
 * Result of device provisioning operation.
 */
sealed class ProvisioningResult {
    /**
     * Provisioning completed successfully.
     * @param deviceId The assigned device identifier
     */
    data class Success(val deviceId: String) : ProvisioningResult()
    
    /**
     * Device was already provisioned.
     */
    object AlreadyProvisioned : ProvisioningResult()
    
    /**
     * API returned an error response.
     * @param httpCode HTTP status code
     * @param message Error message
     */
    data class ApiError(val httpCode: Int, val message: String) : ProvisioningResult()
    
    /**
     * Network connectivity or communication error.
     * @param message Error details
     */
    data class NetworkError(val message: String) : ProvisioningResult()
    
    /**
     * Failed to store credentials securely.
     */
    object StorageError : ProvisioningResult()
}

/**
 * Information about current provisioning status.
 */
data class ProvisioningInfo(
    val isProvisioned: Boolean,
    val deviceId: String?,
    val clientId: String?,
    val provisioningTimestamp: Long,
    val hasSecretKey: Boolean
)