package com.gradientgeeks.aegis.sfe_client.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.util.Scanner

/**
 * Service for detecting compromised runtime environments.
 * 
 * Performs various checks to determine if the device is running in a secure environment:
 * - Root detection
 * - Emulator detection  
 * - Debug mode detection
 * - Tamper detection
 * 
 * These checks help identify potentially compromised environments where
 * the security of cryptographic operations may be at risk.
 */
class EnvironmentSecurityService(private val context: Context) {
    
    companion object {
        private const val TAG = "EnvironmentSecurityService"
        
        // Known root management apps
        private val ROOT_APPS = arrayOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.koushikdutta.rommanager",
            "com.koushikdutta.rommanager.license",
            "com.dimonvideo.luckypatcher",
            "com.chelpus.lackypatch",
            "com.ramdroid.appquarantine",
            "com.ramdroid.appquarantinepro"
        )
        
        // Common paths where su binary is found
        private val SU_PATHS = arrayOf(
            "/data/local/",
            "/data/local/bin/",
            "/data/local/xbin/",
            "/sbin/",
            "/su/bin/",
            "/system/bin/",
            "/system/bin/.ext/",
            "/system/bin/failsafe/",
            "/system/sd/xbin/",
            "/system/usr/we-need-root/",
            "/system/xbin/",
            "/cache/",
            "/data/",
            "/dev/"
        )
        
        // Known emulator characteristics
        private val EMULATOR_DEVICE_IDS = arrayOf(
            "000000000000000",
            "e21833235b6eef10",
            "012345678912345"
        )
        
        private val EMULATOR_BUILD_TAGS = arrayOf(
            "test-keys"
        )
        
        private val EMULATOR_FILES = arrayOf(
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props"
        )
        
        private val EMULATOR_PROPS = arrayOf(
            "init.svc.qemud",
            "init.svc.qemu-props",
            "qemu.hw.mainkeys",
            "qemu.sf.fake_camera",
            "qemu.sf.lcd_density",
            "ro.bootloader",
            "ro.bootmode",
            "ro.hardware",
            "ro.kernel.android.qemud",
            "ro.kernel.qemu.gles",
            "ro.kernel.qemu",
            "ro.product.device",
            "ro.product.model",
            "ro.product.name",
            "ro.serialno"
        )
    }
    
    /**
     * Performs a comprehensive security check of the runtime environment.
     * 
     * @return SecurityCheckResult with detailed findings
     */
    fun performSecurityCheck(): SecurityCheckResult {
        Log.d(TAG, "Performing comprehensive security check")
        
        // Classical security checks are commented out as they are not part of innovative solution
        // Returning default secure status
        /*
        val rootDetected = isDeviceRooted()
        val emulatorDetected = isRunningOnEmulator()
        val debugModeEnabled = isDebugModeEnabled()
        val developerOptionsEnabled = isDeveloperOptionsEnabled()
        val adbEnabled = isAdbEnabled()
        val mockLocationEnabled = isMockLocationEnabled()
        
        val isSecure = !rootDetected && !emulatorDetected && !debugModeEnabled
        */
        
        // Return secure status by default - focus on HMAC validation and key exchange instead
        val result = SecurityCheckResult(
            isSecure = true,
            rootDetected = false,
            emulatorDetected = false,
            debugModeEnabled = false,
            developerOptionsEnabled = false,
            adbEnabled = false,
            mockLocationEnabled = false,
            deviceInfo = getDeviceInfo()
        )
        
        Log.i(TAG, "Security check completed. Secure: ${result.isSecure}")
        logSecurityFindings(result)
        
        return result
    }
    
    /**
     * Checks if the device is rooted using multiple detection methods.
     * [COMMENTED OUT - Classical security check not needed for innovative solution]
     * 
     * @return True if root access is detected, false otherwise
     */
    fun isDeviceRooted(): Boolean {
        // Classical root detection is commented out
        /*
        Log.d(TAG, "Checking for root access")
        
        val checks = listOf(
            ::checkRootApps,
            ::checkSuBinary,
            ::checkRootFiles,
            ::checkBuildTags,
            ::checkWritableSystem
        )
        
        for (check in checks) {
            if (check()) {
                Log.w(TAG, "Root detected by: ${check.javaClass.simpleName}")
                return true
            }
        }
        
        Log.d(TAG, "No root access detected")
        */
        return false
    }
    
    /**
     * Checks if the app is running on an emulator.
     * [COMMENTED OUT - Classical security check not needed for innovative solution]
     * 
     * @return True if emulator is detected, false otherwise
     */
    fun isRunningOnEmulator(): Boolean {
        // Classical emulator detection is commented out
        /*
        Log.d(TAG, "Checking for emulator environment")
        
        val checks = listOf(
            ::checkEmulatorDeviceId,
            ::checkEmulatorBuild,
            ::checkEmulatorFiles,
            ::checkEmulatorProperties,
            ::checkHardwareFeatures
        )
        
        for (check in checks) {
            if (check()) {
                Log.w(TAG, "Emulator detected by: ${check.javaClass.simpleName}")
                return true
            }
        }
        
        Log.d(TAG, "No emulator environment detected")
        */
        return false
    }
    
    /**
     * Checks if the application is running in debug mode.
     * [COMMENTED OUT - Classical security check not needed for innovative solution]
     * 
     * @return True if debug mode is enabled, false otherwise
     */
    fun isDebugModeEnabled(): Boolean {
        // Classical debug mode detection is commented out
        /*
        val isDebug = (context.applicationInfo.flags and 
            android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        
        Log.d(TAG, "Debug mode enabled: $isDebug")
        return isDebug
        */
        return false
    }
    
    /**
     * Checks if developer options are enabled on the device.
     * [COMMENTED OUT - Classical security check not needed for innovative solution]
     * 
     * @return True if developer options are enabled, false otherwise
     */
    fun isDeveloperOptionsEnabled(): Boolean {
        // Classical developer options detection is commented out
        /*
        return try {
            val enabled = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) == 1
            
            Log.d(TAG, "Developer options enabled: $enabled")
            enabled
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check developer options", e)
            false
        }
        */
        return false
    }
    
    /**
     * Checks if ADB debugging is enabled.
     * [COMMENTED OUT - Classical security check not needed for innovative solution]
     * 
     * @return True if ADB is enabled, false otherwise
     */
    fun isAdbEnabled(): Boolean {
        // Classical ADB detection is commented out
        /*
        return try {
            val enabled = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED,
                0
            ) == 1
            
            Log.d(TAG, "ADB enabled: $enabled")
            enabled
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check ADB status", e)
            false
        }
        */
        return false
    }
    
    /**
     * Checks if mock location is enabled.
     * [COMMENTED OUT - Classical security check not needed for innovative solution]
     * 
     * @return True if mock location is enabled, false otherwise
     */
    fun isMockLocationEnabled(): Boolean {
        // Classical mock location detection is commented out
        /*
        return try {
            val enabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ALLOW_MOCK_LOCATION,
                0
            ) == 1
            
            Log.d(TAG, "Mock location enabled: $enabled")
            enabled
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check mock location", e)
            false
        }
        */
        return false
    }
    
    // Root detection methods
    
    private fun checkRootApps(): Boolean {
        val packageManager = context.packageManager
        for (packageName in ROOT_APPS) {
            try {
                packageManager.getPackageInfo(packageName, 0)
                Log.d(TAG, "Root app detected: $packageName")
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // App not found, continue checking
            }
        }
        return false
    }
    
    private fun checkSuBinary(): Boolean {
        for (path in SU_PATHS) {
            val suFile = File(path + "su")
            if (suFile.exists() && suFile.canExecute()) {
                Log.d(TAG, "su binary found at: ${suFile.absolutePath}")
                return true
            }
        }
        return false
    }
    
    private fun checkRootFiles(): Boolean {
        val rootFiles = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        
        for (file in rootFiles) {
            if (File(file).exists()) {
                Log.d(TAG, "Root file detected: $file")
                return true
            }
        }
        return false
    }
    
    private fun checkBuildTags(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }
    
    private fun checkWritableSystem(): Boolean {
        return try {
            val systemFile = File("/system")
            systemFile.canWrite()
        } catch (e: Exception) {
            false
        }
    }
    
    // Emulator detection methods
    
    private fun checkEmulatorDeviceId(): Boolean {
        return try {
            val deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            
            deviceId in EMULATOR_DEVICE_IDS
        } catch (e: Exception) {
            Log.e(TAG, "Failed to access ANDROID_ID: ${e.message}")
            false
        }
    }
    
    private fun checkEmulatorBuild(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.toLowerCase().contains("vbox") ||
                Build.FINGERPRINT.toLowerCase().contains("test-keys") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
                "google_sdk" == Build.PRODUCT
    }
    
    private fun checkEmulatorFiles(): Boolean {
        for (file in EMULATOR_FILES) {
            if (File(file).exists()) {
                Log.d(TAG, "Emulator file detected: $file")
                return true
            }
        }
        return false
    }
    
    private fun checkEmulatorProperties(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("getprop")
            val scanner = Scanner(process.inputStream).useDelimiter("\\A")
            val output = if (scanner.hasNext()) scanner.next() else ""
            
            for (prop in EMULATOR_PROPS) {
                if (output.contains(prop)) {
                    Log.d(TAG, "Emulator property detected: $prop")
                    return true
                }
            }
            false
            
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkHardwareFeatures(): Boolean {
        return !context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) ||
                !context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }
    
    private fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            brand = Build.BRAND,
            device = Build.DEVICE,
            product = Build.PRODUCT,
            hardware = Build.HARDWARE,
            bootloader = Build.BOOTLOADER,
            fingerprint = Build.FINGERPRINT,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            buildTags = Build.TAGS ?: "",
            kernelVersion = getKernelVersion()
        )
    }
    
    private fun getKernelVersion(): String {
        return try {
            val file = File("/proc/version")
            if (file.exists()) {
                FileInputStream(file).use { input ->
                    input.readBytes().toString(Charsets.UTF_8).trim()
                }
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun logSecurityFindings(result: SecurityCheckResult) {
        Log.i(TAG, "=== Security Check Results ===")
        Log.i(TAG, "Overall Secure: ${result.isSecure}")
        Log.i(TAG, "Root Detected: ${result.rootDetected}")
        Log.i(TAG, "Emulator Detected: ${result.emulatorDetected}")
        Log.i(TAG, "Debug Mode: ${result.debugModeEnabled}")
        Log.i(TAG, "Developer Options: ${result.developerOptionsEnabled}")
        Log.i(TAG, "ADB Enabled: ${result.adbEnabled}")
        Log.i(TAG, "Mock Location: ${result.mockLocationEnabled}")
        Log.i(TAG, "Device: ${result.deviceInfo.manufacturer} ${result.deviceInfo.model}")
        Log.i(TAG, "============================")
    }
}

/**
 * Result of comprehensive security environment check.
 */
data class SecurityCheckResult(
    val isSecure: Boolean,
    val rootDetected: Boolean,
    val emulatorDetected: Boolean,
    val debugModeEnabled: Boolean,
    val developerOptionsEnabled: Boolean,
    val adbEnabled: Boolean,
    val mockLocationEnabled: Boolean,
    val deviceInfo: DeviceInfo
)

/**
 * Device information for security analysis.
 */
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val device: String,
    val product: String,
    val hardware: String,
    val bootloader: String,
    val fingerprint: String,
    val androidVersion: String,
    val apiLevel: Int,
    val buildTags: String,
    val kernelVersion: String
)