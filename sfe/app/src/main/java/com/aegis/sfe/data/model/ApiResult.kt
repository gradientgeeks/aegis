package com.aegis.sfe.data.model

sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val message: String, val code: Int? = null) : ApiResult<T>()
    data class Loading<T>(val message: String = "Loading...") : ApiResult<T>()
}

data class ErrorResponse(
    val error: String,
    val message: String,
    val path: String,
    val status: Int,
    val timestamp: String,
    val errors: Map<String, String>? = null
)

data class ProvisioningState(
    val isProvisioned: Boolean = false,
    val deviceId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Security check is commented out - focusing on HMAC validation and key exchange
/*
data class SecurityCheckResult(
    val isSecure: Boolean,
    val rootDetected: Boolean,
    val emulatorDetected: Boolean,
    val debugModeEnabled: Boolean,
    val developerOptionsEnabled: Boolean,
    val adbEnabled: Boolean,
    val mockLocationEnabled: Boolean,
    val warnings: List<String> = emptyList()
)
*/