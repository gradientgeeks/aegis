package com.gradientgeeks.aegis.sfe_client.model

import com.google.gson.annotations.SerializedName

/**
 * Device fingerprint data for transmission to the Aegis Security API.
 * 
 * Contains stable hardware characteristics used for fraud detection
 * and preventing device reuse after factory resets.
 */
data class DeviceFingerprintData(
    
    /**
     * Version of the fingerprinting algorithm used.
     */
    @SerializedName("version")
    val version: String,
    
    /**
     * SHA-256 hash of the composite fingerprint for efficient comparison.
     */
    @SerializedName("compositeHash")
    val compositeHash: String,
    
    /**
     * Hardware characteristics.
     */
    @SerializedName("hardware")
    val hardware: HardwareFingerprintData,
    
    /**
     * Display characteristics.
     */
    @SerializedName("display")
    val display: DisplayFingerprintData,
    
    /**
     * Sensor information.
     */
    @SerializedName("sensors")
    val sensors: SensorFingerprintData,
    
    /**
     * Network characteristics.
     */
    @SerializedName("network")
    val network: NetworkFingerprintData,
    
    /**
     * Application fingerprint (optional, only when similarity threshold met).
     */
    @SerializedName("apps")
    val apps: AppFingerprintData? = null,
    
    /**
     * Timestamp when fingerprint was generated.
     */
    @SerializedName("timestamp")
    val timestamp: Long
)

/**
 * Hardware fingerprint data for API transmission.
 */
data class HardwareFingerprintData(
    @SerializedName("manufacturer")
    val manufacturer: String,
    
    @SerializedName("model")
    val model: String,
    
    @SerializedName("device")
    val device: String,
    
    @SerializedName("board")
    val board: String,
    
    @SerializedName("brand")
    val brand: String,
    
    @SerializedName("cpuArchitecture")
    val cpuArchitecture: String,
    
    @SerializedName("apiLevel")
    val apiLevel: Int,
    
    @SerializedName("buildFingerprint")
    val buildFingerprint: String,
    
    @SerializedName("hash")
    val hash: String
)

/**
 * Display fingerprint data for API transmission.
 */
data class DisplayFingerprintData(
    @SerializedName("widthPixels")
    val widthPixels: Int,
    
    @SerializedName("heightPixels")
    val heightPixels: Int,
    
    @SerializedName("densityDpi")
    val densityDpi: Int,
    
    @SerializedName("hash")
    val hash: String
)

/**
 * Sensor fingerprint data for API transmission.
 */
data class SensorFingerprintData(
    @SerializedName("sensorTypes")
    val sensorTypes: List<Int>,
    
    @SerializedName("sensorCount")
    val sensorCount: Int,
    
    @SerializedName("hash")
    val hash: String
)

/**
 * Network fingerprint data for API transmission.
 */
data class NetworkFingerprintData(
    @SerializedName("networkCountryIso")
    val networkCountryIso: String,
    
    @SerializedName("simCountryIso")
    val simCountryIso: String,
    
    @SerializedName("phoneType")
    val phoneType: Int,
    
    @SerializedName("hash")
    val hash: String
)

/**
 * App fingerprint data for API transmission.
 */
data class AppFingerprintData(
    @SerializedName("userApps")
    val userApps: List<AppInfoData>,
    
    @SerializedName("systemApps")
    val systemApps: List<AppInfoData>,
    
    @SerializedName("totalAppCount")
    val totalAppCount: Int,
    
    @SerializedName("userAppCount")
    val userAppCount: Int,
    
    @SerializedName("systemAppCount")
    val systemAppCount: Int,
    
    @SerializedName("hash")
    val hash: String
)

/**
 * App information data for API transmission.
 */
data class AppInfoData(
    @SerializedName("packageName")
    val packageName: String,
    
    @SerializedName("firstInstallTime")
    val firstInstallTime: Long,
    
    @SerializedName("lastUpdateTime")
    val lastUpdateTime: Long,
    
    @SerializedName("isSystemApp")
    val isSystemApp: Boolean
)