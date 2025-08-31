package com.gradientgeeks.aegis.sfe_client.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.gradientgeeks.aegis.sfe_client.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.*

/**
 * Device fingerprinting service that collects stable hardware characteristics
 * to create a persistent device identity that survives factory resets and app reinstalls.
 * 
 * This service focuses on hardware attributes that don't change and cannot be easily
 * modified by users, providing fraud detection capabilities while respecting privacy.
 */
class DeviceFingerprintingService(private val context: Context) {
    
    companion object {
        private const val TAG = "DeviceFingerprintingService"
        private const val FINGERPRINT_VERSION = "2.0"
        
        // Threshold for collecting app data - only when hardware similarity is very high (99%+)
        private const val HIGH_SIMILARITY_THRESHOLD = 0.99
    }
    
    /**
     * Generates a comprehensive device fingerprint based on stable hardware characteristics.
     * Optionally includes app package data for enhanced fraud detection when similarity threshold is met.
     * 
     * @param existingFingerprint Optional existing fingerprint to compare against for similarity
     * @return DeviceFingerprint containing all collected characteristics
     */
    suspend fun generateDeviceFingerprint(existingFingerprint: DeviceFingerprint? = null): DeviceFingerprint = withContext(Dispatchers.IO) {
        Log.d(TAG, "Generating device fingerprint v$FINGERPRINT_VERSION")
        
        val hardware = collectHardwareFingerprint()
        val display = collectDisplayFingerprint()
        val sensors = collectSensorFingerprint()
        val network = collectNetworkFingerprint()
        
        // Only collect sensitive app data if we have high hardware similarity with existing fingerprint
        val appData = if (shouldCollectAppData(hardware, existingFingerprint)) {
            collectAppFingerprint()
        } else {
            Log.d(TAG, "Hardware similarity below threshold or no existing fingerprint - skipping app data collection")
            null
        }
        
        val fingerprint = DeviceFingerprint(
            version = FINGERPRINT_VERSION,
            hardwareFingerprint = hardware,
            displayFingerprint = display,
            sensorFingerprint = sensors,
            networkFingerprint = network,
            appFingerprint = appData,
            timestamp = System.currentTimeMillis()
        )
        
        Log.d(TAG, "Device fingerprint generated: ${fingerprint.getCompositeHash()}")
        fingerprint
    }
    
    /**
     * Collects hardware-specific characteristics that are stable across resets.
     */
    private fun collectHardwareFingerprint(): HardwareFingerprint {
        val fingerprint = HardwareFingerprint(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            device = Build.DEVICE,
            product = Build.PRODUCT,
            board = Build.BOARD,
            brand = Build.BRAND,
            hardware = Build.HARDWARE,
            cpuArchitecture = Build.SUPPORTED_ABIS.joinToString(","),
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            buildFingerprint = Build.FINGERPRINT
        )
        
        Log.d(TAG, "Hardware fingerprint collected - Manufacturer: ${fingerprint.manufacturer}, " +
            "Model: ${fingerprint.model}, Device: ${fingerprint.device}, " +
            "Board: ${fingerprint.board}, Hash: ${fingerprint.getHash()}")
        
        return fingerprint
    }
    
    /**
     * Collects display characteristics.
     */
    private fun collectDisplayFingerprint(): DisplayFingerprint {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        
        return DisplayFingerprint(
            widthPixels = displayMetrics.widthPixels,
            heightPixels = displayMetrics.heightPixels,
            densityDpi = displayMetrics.densityDpi,
            xdpi = displayMetrics.xdpi,
            ydpi = displayMetrics.ydpi,
            scaledDensity = displayMetrics.scaledDensity
        )
    }
    
    /**
     * Collects available sensor information.
     */
    private fun collectSensorFingerprint(): SensorFingerprint {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        
        val sensorTypes = allSensors.map { it.type }.sorted()
        val sensorNames = allSensors.map { "${it.name}:${it.vendor}" }.sorted()
        
        return SensorFingerprint(
            availableSensorTypes = sensorTypes,
            sensorDetails = sensorNames,
            sensorCount = allSensors.size
        )
    }
    
    /**
     * Collects network-related characteristics.
     */
    @SuppressLint("MissingPermission")
    private fun collectNetworkFingerprint(): NetworkFingerprint {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            
            NetworkFingerprint(
                networkOperatorName = telephonyManager.networkOperatorName ?: "unknown",
                networkCountryIso = telephonyManager.networkCountryIso ?: "unknown",
                simCountryIso = telephonyManager.simCountryIso ?: "unknown",
                phoneType = telephonyManager.phoneType,
                networkType = telephonyManager.networkType
            )
        } catch (e: SecurityException) {
            Log.w(TAG, "Unable to collect network fingerprint due to missing permissions", e)
            NetworkFingerprint(
                networkOperatorName = "permission_denied",
                networkCountryIso = "permission_denied", 
                simCountryIso = "permission_denied",
                phoneType = -1,
                networkType = -1
            )
        }
    }
    
    /**
     * Determines if app data should be collected based on hardware similarity threshold.
     * Only collect sensitive app data when hardware characteristics are 99%+ similar to prevent
     * unnecessary privacy invasion.
     */
    private fun shouldCollectAppData(currentHardware: HardwareFingerprint, existingFingerprint: DeviceFingerprint?): Boolean {
        if (existingFingerprint == null) {
            // No existing fingerprint to compare against - collect baseline app data
            Log.d(TAG, "No existing fingerprint - collecting baseline app data for fraud detection")
            return true
        }
        
        val similarity = currentHardware.calculateSimilarity(existingFingerprint.hardwareFingerprint)
        Log.d(TAG, "Hardware similarity: $similarity (threshold: $HIGH_SIMILARITY_THRESHOLD)")
        
        return similarity >= HIGH_SIMILARITY_THRESHOLD
    }
    
    /**
     * Collects installed application data for enhanced fraud detection.
     * This includes both system and user apps with their package names and install times.
     * Used only when hardware similarity is very high to minimize privacy impact.
     */
    @SuppressLint("QueryPermissionsNeeded")
    private fun collectAppFingerprint(): AppFingerprint? {
        return try {
            Log.d(TAG, "Collecting app fingerprint data for fraud detection")
            
            val packageManager = context.packageManager
            
            // Get all installed applications
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            Log.d(TAG, "Found ${installedApps.size} total installed applications")
            
            val userApps = mutableListOf<AppInfo>()
            val systemApps = mutableListOf<AppInfo>()
            
            for (appInfo in installedApps) {
                try {
                    val packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
                    
                    val appEntry = AppInfo(
                        packageName = appInfo.packageName,
                        firstInstallTime = packageInfo.firstInstallTime,
                        lastUpdateTime = packageInfo.lastUpdateTime,
                        isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    )
                    
                    if (appEntry.isSystemApp) {
                        systemApps.add(appEntry)
                    } else {
                        userApps.add(appEntry)
                        // Log user apps for debugging
                        Log.d(TAG, "User app found: ${appInfo.packageName}")
                    }
                    
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.w(TAG, "Package not found: ${appInfo.packageName}")
                }
            }
            
            // Sort by package name for consistent hashing
            userApps.sortBy { it.packageName }
            systemApps.sortBy { it.packageName }
            
            val appFingerprint = AppFingerprint(
                userApps = userApps,
                systemApps = systemApps,
                totalAppCount = installedApps.size,
                userAppCount = userApps.size,
                systemAppCount = systemApps.size
            )
            
            Log.d(TAG, "App fingerprint collected - Total: ${appFingerprint.totalAppCount}, " +
                "User: ${appFingerprint.userAppCount}, System: ${appFingerprint.systemAppCount}")
            
            // Log first few user apps for verification
            if (userApps.isNotEmpty()) {
                Log.d(TAG, "Sample user apps: ${userApps.take(5).map { it.packageName }}")
            } else {
                Log.w(TAG, "WARNING: No user apps found! This may indicate permission issues.")
            }
            
            appFingerprint
            
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting app fingerprint", e)
            null
        }
    }
}

/**
 * Complete device fingerprint containing all collected characteristics.
 */
data class DeviceFingerprint(
    val version: String,
    val hardwareFingerprint: HardwareFingerprint,
    val displayFingerprint: DisplayFingerprint,
    val sensorFingerprint: SensorFingerprint,
    val networkFingerprint: NetworkFingerprint,
    val appFingerprint: AppFingerprint? = null,
    val timestamp: Long
) {
    /**
     * Generates a SHA-256 hash of the composite fingerprint for efficient comparison.
     */
    fun getCompositeHash(): String {
        val composite = buildString {
            append(hardwareFingerprint.getHash())
            append(displayFingerprint.getHash())
            append(sensorFingerprint.getHash())
            append(networkFingerprint.getHash())
            appFingerprint?.let { append(it.getHash()) }
        }
        
        return MessageDigest.getInstance("SHA-256")
            .digest(composite.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Calculates similarity score with another fingerprint (0.0 to 1.0).
     */
    fun calculateSimilarity(other: DeviceFingerprint): Double {
        val hardwareSimilarity = hardwareFingerprint.calculateSimilarity(other.hardwareFingerprint)
        val displaySimilarity = displayFingerprint.calculateSimilarity(other.displayFingerprint)
        val sensorSimilarity = sensorFingerprint.calculateSimilarity(other.sensorFingerprint)
        val networkSimilarity = networkFingerprint.calculateSimilarity(other.networkFingerprint)
        
        // Include app similarity if both fingerprints have app data
        val appSimilarity = if (appFingerprint != null && other.appFingerprint != null) {
            appFingerprint.calculateSimilarity(other.appFingerprint)
        } else null
        
        // Weighted average: hardware and display are most important, app data provides additional verification
        return if (appSimilarity != null) {
            // When app data is available, it gets significant weight for device reinstall detection
            (hardwareSimilarity * 0.35 + 
             displaySimilarity * 0.25 + 
             sensorSimilarity * 0.15 + 
             networkSimilarity * 0.05 + 
             appSimilarity * 0.20)
        } else {
            // Original weighting when no app data
            (hardwareSimilarity * 0.4 + 
             displaySimilarity * 0.3 + 
             sensorSimilarity * 0.2 + 
             networkSimilarity * 0.1)
        }
    }
}

/**
 * Hardware characteristics that are stable across factory resets.
 */
data class HardwareFingerprint(
    val manufacturer: String,
    val model: String,
    val device: String,
    val product: String,
    val board: String,
    val brand: String,
    val hardware: String,
    val cpuArchitecture: String,
    val androidVersion: String,
    val apiLevel: Int,
    val buildFingerprint: String
) {
    fun getHash(): String {
        // Normalize values to ensure consistency across app installs
        // IMPORTANT: buildFingerprint is the most unique identifier for individual devices
        val normalizedComposite = listOf(
            manufacturer.trim().lowercase(),
            model.trim().lowercase(),
            device.trim().lowercase(),
            product.trim().lowercase(),
            board.trim().lowercase(),
            brand.trim().lowercase(),
            hardware.trim().lowercase(),
            cpuArchitecture.trim().lowercase(),
            apiLevel.toString(),
            buildFingerprint.trim().lowercase()  // This ensures each physical device has unique hash
        ).joinToString(":")
        
        return MessageDigest.getInstance("SHA-256")
            .digest(normalizedComposite.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    
    fun calculateSimilarity(other: HardwareFingerprint): Double {
        var matches = 0
        var total = 0
        
        // Core hardware characteristics (most important)
        if (manufacturer == other.manufacturer) matches += 3
        total += 3
        
        if (model == other.model) matches += 3
        total += 3
        
        if (device == other.device) matches += 2
        total += 2
        
        if (board == other.board) matches += 2
        total += 2
        
        if (cpuArchitecture == other.cpuArchitecture) matches += 2
        total += 2
        
        // Less critical characteristics
        if (brand == other.brand) matches += 1
        total += 1
        
        if (hardware == other.hardware) matches += 1
        total += 1
        
        return matches.toDouble() / total.toDouble()
    }
}

/**
 * Display characteristics.
 */
data class DisplayFingerprint(
    val widthPixels: Int,
    val heightPixels: Int,
    val densityDpi: Int,
    val xdpi: Float,
    val ydpi: Float,
    val scaledDensity: Float
) {
    fun getHash(): String {
        val composite = "$widthPixels:$heightPixels:$densityDpi"
        return MessageDigest.getInstance("SHA-256")
            .digest(composite.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    
    fun calculateSimilarity(other: DisplayFingerprint): Double {
        return if (widthPixels == other.widthPixels && 
                  heightPixels == other.heightPixels && 
                  densityDpi == other.densityDpi) 1.0 else 0.0
    }
}

/**
 * Available sensor information.
 */
data class SensorFingerprint(
    val availableSensorTypes: List<Int>,
    val sensorDetails: List<String>,
    val sensorCount: Int
) {
    fun getHash(): String {
        val composite = "${availableSensorTypes.sorted().joinToString(",")}:$sensorCount"
        return MessageDigest.getInstance("SHA-256")
            .digest(composite.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    
    fun calculateSimilarity(other: SensorFingerprint): Double {
        val commonSensors = availableSensorTypes.intersect(other.availableSensorTypes.toSet()).size
        val totalUniqueSensors = (availableSensorTypes + other.availableSensorTypes).toSet().size
        
        return if (totalUniqueSensors > 0) {
            commonSensors.toDouble() / totalUniqueSensors.toDouble()
        } else 1.0
    }
}

/**
 * Network-related characteristics.
 */
data class NetworkFingerprint(
    val networkOperatorName: String,
    val networkCountryIso: String,
    val simCountryIso: String,
    val phoneType: Int,
    val networkType: Int
) {
    fun getHash(): String {
        val composite = "$networkOperatorName:$networkCountryIso:$simCountryIso:$phoneType"
        return MessageDigest.getInstance("SHA-256")
            .digest(composite.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    
    fun calculateSimilarity(other: NetworkFingerprint): Double {
        var matches = 0
        var total = 0
        
        if (networkCountryIso == other.networkCountryIso) matches += 2
        total += 2
        
        if (simCountryIso == other.simCountryIso) matches += 2
        total += 2
        
        if (networkOperatorName == other.networkOperatorName) matches += 1
        total += 1
        
        if (phoneType == other.phoneType) matches += 1
        total += 1
        
        return if (total > 0) matches.toDouble() / total.toDouble() else 1.0
    }
}

/**
 * Extension function to convert DeviceFingerprint to DeviceFingerprintData for API transmission.
 */
fun DeviceFingerprint.toApiData(): DeviceFingerprintData {
    return DeviceFingerprintData(
        version = this.version,
        compositeHash = this.getCompositeHash(),
        hardware = this.hardwareFingerprint.toApiData(),
        display = this.displayFingerprint.toApiData(),
        sensors = this.sensorFingerprint.toApiData(),
        network = this.networkFingerprint.toApiData(),
        apps = this.appFingerprint?.toApiData(),
        timestamp = this.timestamp
    )
}

/**
 * Extension function to convert HardwareFingerprint to HardwareFingerprintData.
 */
fun HardwareFingerprint.toApiData(): HardwareFingerprintData {
    return HardwareFingerprintData(
        manufacturer = this.manufacturer,
        model = this.model,
        device = this.device,
        board = this.board,
        brand = this.brand,
        cpuArchitecture = this.cpuArchitecture,
        apiLevel = this.apiLevel,
        buildFingerprint = this.buildFingerprint,
        hash = this.getHash()
    )
}

/**
 * Extension function to convert DisplayFingerprint to DisplayFingerprintData.
 */
fun DisplayFingerprint.toApiData(): DisplayFingerprintData {
    return DisplayFingerprintData(
        widthPixels = this.widthPixels,
        heightPixels = this.heightPixels,
        densityDpi = this.densityDpi,
        hash = this.getHash()
    )
}

/**
 * Extension function to convert SensorFingerprint to SensorFingerprintData.
 */
fun SensorFingerprint.toApiData(): SensorFingerprintData {
    return SensorFingerprintData(
        sensorTypes = this.availableSensorTypes,
        sensorCount = this.sensorCount,
        hash = this.getHash()
    )
}

/**
 * Extension function to convert NetworkFingerprint to NetworkFingerprintData.
 */
fun NetworkFingerprint.toApiData(): NetworkFingerprintData {
    return NetworkFingerprintData(
        networkCountryIso = this.networkCountryIso,
        simCountryIso = this.simCountryIso,
        phoneType = this.phoneType,
        hash = this.getHash()
    )
}

/**
 * App fingerprint containing installed application data for device reinstall detection.
 */
data class AppFingerprint(
    val userApps: List<AppInfo>,
    val systemApps: List<AppInfo>,
    val totalAppCount: Int,
    val userAppCount: Int,
    val systemAppCount: Int
) {
    /**
     * Generates a hash of the app fingerprint for efficient comparison.
     */
    fun getHash(): String {
        // Create a composite string from app counts and selected package names
        val composite = buildString {
            append("total:$totalAppCount")
            append(":user:$userAppCount")
            append(":system:$systemAppCount")
            
            // Include user app package names for better uniqueness
            append(":user_apps:")
            userApps.forEach { app ->
                append("${app.packageName},")
            }
            
            // Include a sample of system apps for additional uniqueness
            append(":system_apps:")
            systemApps.take(50).forEach { app ->
                append("${app.packageName},")
            }
        }
        
        return MessageDigest.getInstance("SHA-256")
            .digest(composite.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Calculates similarity with another app fingerprint.
     * Focuses on app count similarity and common package names.
     */
    fun calculateSimilarity(other: AppFingerprint): Double {
        // App count similarity (30% weight)
        val totalCountSimilarity = 1.0 - (kotlin.math.abs(totalAppCount - other.totalAppCount).toDouble() / 
                                         kotlin.math.max(totalAppCount, other.totalAppCount).toDouble())
        
        val userCountSimilarity = 1.0 - (kotlin.math.abs(userAppCount - other.userAppCount).toDouble() / 
                                        kotlin.math.max(userAppCount, other.userAppCount).toDouble())
        
        // Package name similarity (70% weight)
        val userPackageNames = userApps.map { it.packageName }.toSet()
        val otherUserPackageNames = other.userApps.map { it.packageName }.toSet()
        
        val commonUserApps = userPackageNames.intersect(otherUserPackageNames).size
        val totalUniqueUserApps = userPackageNames.union(otherUserPackageNames).size
        
        val userAppSimilarity = if (totalUniqueUserApps > 0) {
            commonUserApps.toDouble() / totalUniqueUserApps.toDouble()
        } else 1.0
        
        // System apps are less likely to change, but check a subset
        val systemPackageNames = systemApps.map { it.packageName }.toSet()
        val otherSystemPackageNames = other.systemApps.map { it.packageName }.toSet()
        
        val commonSystemApps = systemPackageNames.intersect(otherSystemPackageNames).size
        val totalUniqueSystemApps = systemPackageNames.union(otherSystemPackageNames).size
        
        val systemAppSimilarity = if (totalUniqueSystemApps > 0) {
            commonSystemApps.toDouble() / totalUniqueSystemApps.toDouble()
        } else 1.0
        
        // Weighted similarity: user apps matter more for device identification
        return (totalCountSimilarity * 0.15 + 
                userCountSimilarity * 0.15 + 
                userAppSimilarity * 0.50 + 
                systemAppSimilarity * 0.20)
    }
}

/**
 * Information about an installed application.
 */
data class AppInfo(
    val packageName: String,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val isSystemApp: Boolean
)

/**
 * Extension function to convert AppFingerprint to AppFingerprintData.
 */
fun AppFingerprint.toApiData(): AppFingerprintData {
    return AppFingerprintData(
        userApps = this.userApps.map { it.toApiData() },
        systemApps = this.systemApps.map { it.toApiData() },
        totalAppCount = this.totalAppCount,
        userAppCount = this.userAppCount,
        systemAppCount = this.systemAppCount,
        hash = this.getHash()
    )
}

/**
 * Extension function to convert AppInfo to AppInfoData.
 */
fun AppInfo.toApiData(): AppInfoData {
    return AppInfoData(
        packageName = this.packageName,
        firstInstallTime = this.firstInstallTime,
        lastUpdateTime = this.lastUpdateTime,
        isSystemApp = this.isSystemApp
    )
}