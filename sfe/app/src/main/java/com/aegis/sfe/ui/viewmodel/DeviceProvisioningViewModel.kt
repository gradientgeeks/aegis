package com.aegis.sfe.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.sfe.UCOBankApplication
import com.aegis.sfe.data.model.ProvisioningState
// import com.aegis.sfe.data.model.SecurityCheckResult // Commented out - focusing on HMAC validation and key exchange
import com.gradientgeeks.aegis.sfe_client.provisioning.ProvisioningResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceProvisioningViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "DeviceProvisioningVM"
    }
    
    private val _provisioningState = MutableStateFlow(ProvisioningState())
    val provisioningState: StateFlow<ProvisioningState> = _provisioningState.asStateFlow()
    
    // Security check is commented out - focusing on HMAC validation and key exchange
    // private val _securityCheckResult = MutableStateFlow<SecurityCheckResult?>(null)
    // val securityCheckResult: StateFlow<SecurityCheckResult?> = _securityCheckResult.asStateFlow()
    
    init {
        checkProvisioningStatus()
        // Security check is commented out - focusing on HMAC validation and key exchange
        // performSecurityCheck()
    }
    
    fun checkProvisioningStatus() {
        try {
            val isProvisioned = UCOBankApplication.aegisClient.isDeviceProvisioned()
            val deviceId = if (isProvisioned) {
                UCOBankApplication.aegisClient.getDeviceId()
            } else null
            
            _provisioningState.value = _provisioningState.value.copy(
                isProvisioned = isProvisioned,
                deviceId = deviceId,
                error = null
            )
            
            Log.d(TAG, "Provisioning status: $isProvisioned, deviceId: $deviceId")
            
            // If already provisioned, don't show provisioning screen
            if (isProvisioned && !deviceId.isNullOrEmpty()) {
                Log.d(TAG, "Device already provisioned with ID: $deviceId")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking provisioning status", e)
            _provisioningState.value = _provisioningState.value.copy(
                error = "Failed to check provisioning status: ${e.message}"
            )
        }
    }
    
    fun provisionDevice() {
        viewModelScope.launch {
            try {
                _provisioningState.value = _provisioningState.value.copy(
                    isLoading = true,
                    error = null
                )
                
                Log.d(TAG, "Starting device provisioning...")
                
                val result = UCOBankApplication.aegisClient.provisionDevice(
                    clientId = UCOBankApplication.CLIENT_ID,
                    registrationKey = UCOBankApplication.REGISTRATION_KEY
                )
                
                when (result) {
                    is ProvisioningResult.Success -> {
                        Log.d(TAG, "Device provisioned successfully")
                        _provisioningState.value = _provisioningState.value.copy(
                            isProvisioned = true,
                            deviceId = result.deviceId,
                            isLoading = false,
                            error = null
                        )
                    }
                    is ProvisioningResult.ApiError -> {
                        Log.e(TAG, "Device provisioning failed: ${result.message}")
                        _provisioningState.value = _provisioningState.value.copy(
                            isProvisioned = false,
                            isLoading = false,
                            error = "API Error: ${result.message}"
                        )
                    }
                    is ProvisioningResult.NetworkError -> {
                        Log.e(TAG, "Network error during provisioning: ${result.message}")
                        _provisioningState.value = _provisioningState.value.copy(
                            isProvisioned = false,
                            isLoading = false,
                            error = "Network error: ${result.message}"
                        )
                    }
                    is ProvisioningResult.StorageError -> {
                        Log.e(TAG, "Storage error during provisioning")
                        _provisioningState.value = _provisioningState.value.copy(
                            isProvisioned = false,
                            isLoading = false,
                            error = "Failed to store device credentials"
                        )
                    }
                    is ProvisioningResult.AlreadyProvisioned -> {
                        Log.d(TAG, "Device already provisioned")
                        _provisioningState.value = _provisioningState.value.copy(
                            isProvisioned = true,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Exception during device provisioning", e)
                _provisioningState.value = _provisioningState.value.copy(
                    isProvisioned = false,
                    isLoading = false,
                    error = "Provisioning failed: ${e.message}"
                )
            }
        }
    }
    
    // Security check is commented out - focusing on HMAC validation and key exchange
    /*
    fun performSecurityCheck() {
        try {
            val result = UCOBankApplication.aegisClient.performSecurityCheck()
            
            val warnings = mutableListOf<String>()
            if (result.rootDetected) warnings.add("Root access detected")
            if (result.emulatorDetected) warnings.add("Running on emulator")
            if (result.debugModeEnabled) warnings.add("Debug mode enabled")
            if (result.developerOptionsEnabled) warnings.add("Developer options enabled")
            if (result.adbEnabled) warnings.add("ADB debugging enabled")
            if (result.mockLocationEnabled) warnings.add("Mock location enabled")
            
            val securityResult = SecurityCheckResult(
                isSecure = result.isSecure,
                rootDetected = result.rootDetected,
                emulatorDetected = result.emulatorDetected,
                debugModeEnabled = result.debugModeEnabled,
                developerOptionsEnabled = result.developerOptionsEnabled,
                adbEnabled = result.adbEnabled,
                mockLocationEnabled = result.mockLocationEnabled,
                warnings = warnings
            )
            
            _securityCheckResult.value = securityResult
            
            Log.d(TAG, "Security check completed: $securityResult")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error performing security check", e)
            _securityCheckResult.value = SecurityCheckResult(
                isSecure = false,
                rootDetected = false,
                emulatorDetected = false,
                debugModeEnabled = false,
                developerOptionsEnabled = false,
                adbEnabled = false,
                mockLocationEnabled = false,
                warnings = listOf("Failed to perform security check: ${e.message}")
            )
        }
    }
    */
    
    fun clearProvisioningData() {
        try {
            val success = UCOBankApplication.aegisClient.clearProvisioningData()
            if (success) {
                _provisioningState.value = ProvisioningState()
                Log.d(TAG, "Provisioning data cleared successfully")
            } else {
                _provisioningState.value = _provisioningState.value.copy(
                    error = "Failed to clear provisioning data"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing provisioning data", e)
            _provisioningState.value = _provisioningState.value.copy(
                error = "Error clearing data: ${e.message}"
            )
        }
    }
    
    fun clearError() {
        _provisioningState.value = _provisioningState.value.copy(error = null)
    }
}