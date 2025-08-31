package com.gradientgeeks.aegis.sfe_client.model

import com.google.gson.annotations.SerializedName

/**
 * Request model for device registration with the Aegis Security API.
 * 
 * Contains the necessary information for establishing a secure device identity
 * during the initial provisioning process, including device fingerprinting
 * for fraud detection.
 */
data class DeviceRegistrationRequest(
    
    /**
     * Client identifier that specifies which application/organization 
     * this device belongs to (e.g., "UCOBANK_PROD_ANDROID").
     */
    @SerializedName("clientId")
    val clientId: String,
    
    /**
     * Shared registration key provided by the organization's administrators.
     * This key is embedded in the application during the build process.
     */
    @SerializedName("registrationKey")
    val registrationKey: String,
    
    /**
     * Google Play Integrity API token for device and app verification.
     * This will be null in demo/development environments where the app
     * is not distributed through Google Play Store.
     */
    @SerializedName("integrityToken")
    val integrityToken: String?,
    
    /**
     * Device fingerprint containing stable hardware characteristics.
     * Used for fraud detection and preventing device reuse after factory reset.
     */
    @SerializedName("deviceFingerprint")
    val deviceFingerprint: DeviceFingerprintData
)