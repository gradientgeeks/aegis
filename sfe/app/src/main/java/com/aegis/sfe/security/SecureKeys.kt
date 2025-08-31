package com.aegis.sfe.security

import android.util.Log

/**
 * Secure key storage using NDK.
 * This class provides access to sensitive keys stored in native C++ code,
 * making them harder to extract from the APK.
 */
object SecureKeys {
    
    private const val TAG = "SecureKeys"
    
    init {
        try {
            System.loadLibrary("aegis_keys")
            Log.d(TAG, "Native library loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library", e)
            throw RuntimeException("Could not load native library", e)
        }
    }
    
    /**
     * Retrieves the registration key from native code.
     * This key is used to authenticate the app with the Aegis Security API.
     */
    external fun getRegistrationKey(): String
    
    /**
     * Retrieves the client ID from native code.
     * This identifies the app to the Aegis Security API.
     */
    external fun getClientId(): String
    
    /**
     * Validates that the native library is properly loaded and keys are accessible.
     * Call this during app initialization to fail fast if there are issues.
     */
    fun validateKeys(): Boolean {
        return try {
            val key = getRegistrationKey()
            val clientId = getClientId()
            key.isNotEmpty() && clientId.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to validate keys", e)
            false
        }
    }
}