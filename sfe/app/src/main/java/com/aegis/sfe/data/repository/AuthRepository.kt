package com.aegis.sfe.data.repository

import android.util.Log
import com.aegis.sfe.UCOBankApplication
import com.aegis.sfe.data.api.AuthApiService
import com.aegis.sfe.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthRepository {
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    private val gson = Gson()
    
    fun login(username: String, password: String): Flow<ApiResult<LoginResponse>> = flow {
        emit(ApiResult.Loading("Authenticating..."))
        
        try {
            // Create login request
            val loginRequest = mapOf(
                "username" to username,
                "password" to password
            )
            val requestJson = gson.toJson(loginRequest)
            
            Log.d(TAG, "Sending login request for user: $username")
            
            // Create signed request
            val signedHeaders = try {
                UCOBankApplication.aegisClient.signRequest(
                    method = "POST",
                    uri = "/api/v1/auth/login",
                    body = requestJson
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception while signing request", e)
                null
            }
            
            if (signedHeaders == null) {
                // Check provisioning status for better error message
                val isProvisioned = UCOBankApplication.aegisClient.isDeviceProvisioned()
                val deviceId = try { UCOBankApplication.aegisClient.getDeviceId() } catch (e: Exception) { null }
                
                Log.e(TAG, "Failed to sign request - isProvisioned: $isProvisioned, deviceId: $deviceId")
                
                emit(ApiResult.Error("Failed to sign request. Please close and restart the app."))
                return@flow
            }
            
            // Create OkHttp client with logging
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            
            // Build request with signed headers
            val request = Request.Builder()
                .url("${UCOBankApplication.BANK_API_BASE_URL}/auth/login")
                .post(requestJson.toRequestBody("application/json".toMediaType()))
                .header("Content-Type", "application/json")
                .header("X-Device-Id", signedHeaders.deviceId)
                .header("X-Signature", signedHeaders.signature)
                .header("X-Timestamp", signedHeaders.timestamp)
                .header("X-Nonce", signedHeaders.nonce)
                .build()
            
            // Execute request
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val loginResponse = gson.fromJson(responseBody, LoginResponse::class.java)
                    emit(ApiResult.Success(loginResponse))
                } else {
                    emit(ApiResult.Error("Empty response from server"))
                }
            } else {
                val errorBody = response.body?.string()
                val errorMessage = try {
                    val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                    errorResponse.message ?: "Login failed"
                } catch (e: Exception) {
                    "Login failed: ${response.code}"
                }
                emit(ApiResult.Error(errorMessage))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            emit(ApiResult.Error("Network error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Initiates device rebinding process.
     * 
     * @param username The username
     * @param deviceId The new device ID to bind
     * @param aadhaarLast4 Last 4 digits of Aadhaar
     * @param panNumber PAN number for verification
     * @param securityAnswers Security question answers
     * @param verificationMethod The verification method used
     * @return true if rebinding was successful, false otherwise
     */
    suspend fun rebindDevice(
        username: String, 
        deviceId: String, 
        aadhaarLast4: String,
        panNumber: String,
        securityAnswers: Map<String, String>,
        verificationMethod: String = "AADHAAR_PAN_SECURITY"
    ): Boolean = kotlinx.coroutines.withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Initiating device rebinding - User: $username, Device: $deviceId")
            
            // Create rebinding request body with actual verification data
            val rebindRequest = mapOf(
                "username" to username,
                "verificationMethod" to verificationMethod,
                "aadhaarLast4" to aadhaarLast4,
                "panNumber" to panNumber,
                "securityAnswers" to securityAnswers
            )
            val requestJson = gson.toJson(rebindRequest)
            
            // Create HTTP client with timeout
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
            
            // Build request URL
            val baseUrl = UCOBankApplication.BANK_API_BASE_URL
            val url = "$baseUrl/auth/rebind-device"
            
            Log.d(TAG, "Rebinding URL: $url")
            Log.d(TAG, "Request body: $requestJson")
            
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toRequestBody("application/json".toMediaType()))
                .header("Content-Type", "application/json")
                .header("X-Device-Id", deviceId)
                .build()
            
            // Execute request
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Device rebinding response: $responseBody")
                
                // Parse response to check if successful
                try {
                    val responseJson = gson.fromJson(responseBody, Map::class.java)
                    val success = responseJson["success"] as? Boolean
                    
                    // Store new auth token if provided
                    val token = responseJson["token"] as? String
                    if (token != null) {
                        UCOBankApplication.authToken = token
                    }
                    
                    success == true
                } catch (e: Exception) {
                    Log.w(TAG, "Could not parse rebinding response", e)
                    false
                }
            } else {
                val errorBody = response.body?.string()
                Log.e(TAG, "Device rebinding failed: ${response.code} - $errorBody")
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during device rebinding", e)
            false
        }
    }
}

data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: UserInfo,
    val issuedAt: Long
)

data class ErrorResponse(
    val timestamp: Long,
    val status: Int,
    val error: String?,
    val message: String?,
    val path: String?
)