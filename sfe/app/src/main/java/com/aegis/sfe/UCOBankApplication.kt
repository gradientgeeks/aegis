package com.aegis.sfe

import android.app.Application
import com.aegis.sfe.security.SecureKeys
import com.gradientgeeks.aegis.sfe_client.AegisSfeClient

class UCOBankApplication : Application() {
    
    companion object {
        // Configuration constants - Azure endpoints
        const val AEGIS_API_BASE_URL = "http://192.168.20.13:8080/api"
        const val BANK_API_BASE_URL = "http://192.168.20.13:8081/api/v1"

        


        
        // Fraud reporting configuration
        const val AEGIS_FRAUD_ENDPOINT = "/admin/fraud-report"
        const val AEGIS_DEVICE_STATUS_ENDPOINT = "/admin/devices"
        
        // Secure key access - these will retrieve keys from native code
        val CLIENT_ID: String by lazy { SecureKeys.getClientId() }
        val REGISTRATION_KEY: String by lazy { SecureKeys.getRegistrationKey() }
        
        // Global SDK instance
        lateinit var aegisClient: AegisSfeClient
            private set
            
        // Auth state
        var authToken: String? = null
        var currentUser: com.aegis.sfe.data.model.UserInfo? = null
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Validate that native keys are accessible
        if (!SecureKeys.validateKeys()) {
            android.util.Log.e("UCOBankApp", "Failed to validate secure keys")
            throw RuntimeException("Secure key validation failed")
        }
        
        // Initialize Aegis SFE Client SDK
        try {
            aegisClient = AegisSfeClient.initialize(
                context = this,
                baseUrl = AEGIS_API_BASE_URL
            )
            
            // Log SDK initialization
            android.util.Log.d("UCOBankApp", "Aegis SDK initialized successfully")
            android.util.Log.d("UCOBankApp", "Client ID: ${CLIENT_ID.take(10)}...")
            
        } catch (e: Exception) {
            android.util.Log.e("UCOBankApp", "Failed to initialize Aegis SDK", e)
            throw RuntimeException("SDK initialization failed", e)
        }
    }
}