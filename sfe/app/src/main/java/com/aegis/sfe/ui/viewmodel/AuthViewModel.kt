package com.aegis.sfe.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.sfe.UCOBankApplication
import com.aegis.sfe.data.api.ApiClientFactory
import com.aegis.sfe.data.model.LoginState
import com.aegis.sfe.data.model.UserInfo
import com.aegis.sfe.data.repository.AuthRepository
import org.json.JSONObject
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "AuthViewModel"
    }
    
    private val authRepository = AuthRepository()
    
    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    // Device blocking and rebinding states
    private val _deviceBlockedState = MutableStateFlow<DeviceBlockedState?>(null)
    val deviceBlockedState: StateFlow<DeviceBlockedState?> = _deviceBlockedState.asStateFlow()
    
    private val _rebindingState = MutableStateFlow<DeviceRebindingState?>(null)
    val rebindingState: StateFlow<DeviceRebindingState?> = _rebindingState.asStateFlow()
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                _loginState.value = _loginState.value.copy(
                    isLoading = true,
                    error = null
                )
                
                // Log device provisioning status for debugging
                val deviceId = try {
                    UCOBankApplication.aegisClient.getDeviceId()
                } catch (e: Exception) {
                    null
                }
                
                Log.d(TAG, "Login attempt - deviceId: $deviceId")
                
                Log.d(TAG, "Attempting login for user: $username")
                
                // Login through repository (which handles HMAC signing)
                authRepository.login(username, password).collect { result ->
                    when (result) {
                        is com.aegis.sfe.data.model.ApiResult.Loading -> {
                            // Already set loading state
                        }
                        is com.aegis.sfe.data.model.ApiResult.Success -> {
                            Log.d(TAG, "Login successful for user: $username")
                            
                            // Store auth token
                            UCOBankApplication.authToken = result.data.accessToken
                            UCOBankApplication.currentUser = result.data.user
                            
                            // Set user context for policy enforcement
                            setupUserContextForPolicyEnforcement(result.data.user, username, deviceId)
                            
                            _loginState.value = _loginState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                error = null,
                                user = result.data.user
                            )
                        }
                        is com.aegis.sfe.data.model.ApiResult.Error -> {
                            Log.e(TAG, "Login failed: ${result.message}")
                            
                            // Check if this is a device blocking or rebinding scenario
                            val errorInfo = parseErrorResponse(result.message, null)
                            
                            when {
                                errorInfo.isDeviceBlocked -> {
                                    Log.w(TAG, "Device is blocked: ${errorInfo.reason}")
                                    _deviceBlockedState.value = DeviceBlockedState(
                                        reason = errorInfo.reason ?: "Device has been blocked for security reasons",
                                        isTemporary = errorInfo.isTemporary,
                                        contactSupport = !errorInfo.isTemporary
                                    )
                                }
                                errorInfo.requiresRebinding -> {
                                    Log.w(TAG, "Device rebinding required: ${errorInfo.reason}")
                                    _rebindingState.value = DeviceRebindingState(
                                        reason = errorInfo.reason ?: "Login from new device detected",
                                        username = username
                                    )
                                }
                                else -> {
                                    _loginState.value = _loginState.value.copy(
                                        isLoading = false,
                                        error = result.message
                                    )
                                }
                            }
                        }
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Login exception", e)
                _loginState.value = _loginState.value.copy(
                    isLoading = false,
                    error = "Login failed: ${e.message}"
                )
            }
        }
    }
    
    fun logout() {
        Log.i(TAG, "User logging out - clearing session data")
        
        // Clear authentication data
        UCOBankApplication.authToken = null
        UCOBankApplication.currentUser = null
        
        // Clear user metadata from SDK for policy enforcement
        UCOBankApplication.aegisClient.clearUserMetadata()
        
        // Reset login state
        _loginState.value = LoginState()
    }
    
    fun clearError() {
        _loginState.value = _loginState.value.copy(error = null)
    }
    
    /**
     * Handles device rebinding process - deprecated, use performDeviceRebinding instead.
     */
    @Deprecated("Use performDeviceRebinding with proper verification details")
    fun initiateDeviceRebinding(username: String, verificationMethod: String = "MANUAL_VERIFICATION") {
        // This method is kept for backward compatibility but should not be used
        // Navigate to DeviceRebindingScreen instead
        Log.w(TAG, "initiateDeviceRebinding is deprecated. Use DeviceRebindingScreen for proper verification.")
    }
    
    /**
     * Clears device blocked state.
     */
    fun clearDeviceBlockedState() {
        _deviceBlockedState.value = null
    }
    
    /**
     * Clears device rebinding state.
     */
    fun clearRebindingState() {
        _rebindingState.value = null
    }
    
    /**
     * Performs device rebinding with identity verification details.
     */
    fun performDeviceRebinding(
        username: String,
        aadhaarLast4: String,
        panNumber: String,
        securityAnswers: Map<String, String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val deviceId = UCOBankApplication.aegisClient.getDeviceId()
                if (deviceId == null) {
                    onError("Device not properly provisioned")
                    return@launch
                }
                
                Log.d(TAG, "Performing device rebinding with identity verification - User: $username, Device: $deviceId")
                
                // Call rebinding API with verification details
                val success = authRepository.rebindDevice(
                    username = username,
                    deviceId = deviceId,
                    aadhaarLast4 = aadhaarLast4,
                    panNumber = panNumber,
                    securityAnswers = securityAnswers
                )
                
                if (success) {
                    Log.i(TAG, "Device rebinding successful")
                    _rebindingState.value = null // Clear rebinding state
                    onSuccess()
                } else {
                    onError("Identity verification failed. Please check your details and try again.")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during device rebinding", e)
                onError("An error occurred during device verification: ${e.message}")
            }
        }
    }
    
    /**
     * Parses error response to determine if it's related to device blocking or rebinding.
     */
    private fun parseErrorResponse(message: String?, exception: Throwable?): ErrorInfo {
        var reason: String? = null
        var isDeviceBlocked = false
        var requiresRebinding = false
        var isTemporary = false
        
        try {
            // Try to parse JSON error response
            val errorMessage = message ?: ""
            
            // Check for device verification patterns
            when {
                errorMessage.contains("device verification", ignoreCase = true) ||
                errorMessage.contains("new device", ignoreCase = true) ||
                errorMessage.contains("DEVICE_VERIFICATION_REQUIRED", ignoreCase = true) -> {
                    requiresRebinding = true
                    reason = "Login from new device detected. Identity verification required."
                }
                
                errorMessage.contains("device has been blocked", ignoreCase = true) ||
                errorMessage.contains("device blocked", ignoreCase = true) -> {
                    isDeviceBlocked = true
                    isTemporary = errorMessage.contains("temporarily", ignoreCase = true)
                    reason = if (isTemporary) {
                        "Your device has been temporarily blocked for security reasons."
                    } else {
                        "Your device has been permanently blocked for security reasons."
                    }
                }
                
                errorMessage.contains("HMAC", ignoreCase = true) ||
                errorMessage.contains("signature", ignoreCase = true) -> {
                    isDeviceBlocked = true
                    isTemporary = true
                    reason = "Device security validation failed. Please try again or contact support."
                }
            }
            
            // Try to parse as JSON if it looks like JSON
            if (errorMessage.startsWith("{") && errorMessage.endsWith("}")) {
                try {
                    val jsonError = JSONObject(errorMessage)
                    if (jsonError.has("requiresRebinding") && jsonError.getBoolean("requiresRebinding")) {
                        requiresRebinding = true
                        reason = jsonError.optString("message", "Device verification required")
                    }
                    if (jsonError.has("errorCode")) {
                        when (jsonError.getString("errorCode")) {
                            "DEVICE_VERIFICATION_REQUIRED" -> {
                                requiresRebinding = true
                                reason = "Identity verification required for new device"
                            }
                            "DEVICE_BLOCKED" -> {
                                isDeviceBlocked = true
                                reason = jsonError.optString("message", "Device has been blocked")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Could not parse error as JSON: $errorMessage")
                }
            }
            
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing error response", e)
        }
        
        return ErrorInfo(reason, isDeviceBlocked, requiresRebinding, isTemporary)
    }
    
    /**
     * Sets up user context for policy enforcement after successful login.
     * This provides the SDK with user metadata needed for security policy validation.
     */
    private fun setupUserContextForPolicyEnforcement(user: UserInfo, username: String, deviceId: String?) {
        try {
            Log.d(TAG, "Setting up user context for policy enforcement")
            
            // Calculate account age (simplified estimation based on user ID)
            // In a real implementation, this would come from backend
            val accountAge = estimateAccountAge(user.id)
            
            // Determine account tier based on email domain or other heuristics
            val accountTier = determineAccountTier(user.email)
            
            // Determine KYC level (simplified - would come from backend)
            val kycLevel = "BASIC" // Default for demo
            
            // Check device binding status (would be provided by backend)
            val hasDeviceBinding = true // Assume device binding is enabled
            val deviceBindingCount = 1 // Simplified for demo
            
            // Set user session context in the SDK
            UCOBankApplication.aegisClient.setUserSessionContext(
                accountTier = accountTier,
                accountAge = accountAge,
                kycLevel = kycLevel,
                hasDeviceBinding = hasDeviceBinding,
                deviceBindingCount = deviceBindingCount
            )
            
            // Create anonymized user ID (in real implementation, this would come from backend)
            val effectiveDeviceId = deviceId ?: UCOBankApplication.aegisClient.getDeviceId()
            val anonymizedUserId = createAnonymizedUserId(username, effectiveDeviceId)
            UCOBankApplication.aegisClient.setAnonymizedUserId(anonymizedUserId)
            
            // Report any risk factors detected during login
            reportLoginRiskFactors(user, username)
            
            Log.i(TAG, "User context set for policy enforcement - Tier: $accountTier, Age: $accountAge months")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up user context for policy enforcement", e)
        }
    }
    
    /**
     * Estimates account age based on user ID (simplified implementation).
     * In production, this would be calculated from actual account creation date.
     */
    private fun estimateAccountAge(userId: Long): Int {
        // Simple heuristic: newer user IDs = newer accounts
        return when {
            userId < 1000 -> 24 // Older accounts (2+ years)
            userId < 5000 -> 12 // Established accounts (1 year)
            userId < 10000 -> 6 // Recent accounts (6 months)
            else -> 3 // New accounts (3 months)
        }
    }
    
    /**
     * Determines account tier based on email domain or other criteria.
     * In production, this would be determined by backend based on account type.
     */
    private fun determineAccountTier(email: String): String {
        return when {
            email.contains("@corporate") || email.contains("@company") -> "CORPORATE"
            email.contains("@premium") || email.contains("@vip") -> "PREMIUM"
            else -> "BASIC"
        }
    }
    
    /**
     * Creates an anonymized user ID for policy tracking.
     * In production, this would be generated by the backend service.
     */
    private fun createAnonymizedUserId(username: String, deviceId: String?): String {
        return try {
            val combinedData = "$username|${deviceId ?: "unknown"}|UCOBANK_ANDROID"
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(combinedData.toByteArray())
            android.util.Base64.encodeToString(hashBytes, android.util.Base64.NO_WRAP).take(16)
        } catch (e: Exception) {
            Log.w(TAG, "Error creating anonymized user ID", e)
            "anon_${System.currentTimeMillis()}"
        }
    }
    
    /**
     * Reports risk factors detected during login process.
     */
    private fun reportLoginRiskFactors(user: UserInfo, username: String) {
        // In production, these would be determined by various security checks
        val isLocationChanged = false // Would be determined by IP geolocation
        val isDeviceChanged = false // Would be determined by device fingerprinting
        val isDormantAccount = false // Would be determined by last login time
        val requiresDeviceRebinding = false // Would be determined by security policies
        
        UCOBankApplication.aegisClient.reportRiskFactors(
            isLocationChanged = isLocationChanged,
            isDeviceChanged = isDeviceChanged,
            isDormantAccount = isDormantAccount,
            requiresDeviceRebinding = requiresDeviceRebinding
        )
        
        Log.d(TAG, "Risk factors reported for user: $username")
    }
}

/**
 * Represents the state when a device is blocked.
 */
data class DeviceBlockedState(
    val reason: String,
    val isTemporary: Boolean,
    val contactSupport: Boolean
)

/**
 * Represents the state when device rebinding is required.
 */
data class DeviceRebindingState(
    val reason: String,
    val username: String,
    val isProcessing: Boolean = false,
    val error: String? = null
)

/**
 * Internal data class for parsing error information.
 */
private data class ErrorInfo(
    val reason: String?,
    val isDeviceBlocked: Boolean,
    val requiresRebinding: Boolean,
    val isTemporary: Boolean
)