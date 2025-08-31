package com.gradientgeeks.aegis.sfe_client.session

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.SecretKey

/**
 * Manages session keys for encrypted communication with the backend.
 * Handles session lifecycle, key storage, and expiration.
 */
class SessionKeyManager(private val context: Context) {
    companion object {
        private const val TAG = "SessionKeyManager"
        private const val PREF_NAME = "aegis_session_prefs"
        private const val PREF_SESSION_ID = "current_session_id"
        private const val PREF_SESSION_EXPIRY = "session_expiry"
        private const val SESSION_DURATION_MS = 3600000L // 1 hour
    }
    
    private val keyExchangeService = KeyExchangeService()
    private val sessionKeys = ConcurrentHashMap<String, SessionInfo>()
    private val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    data class SessionInfo(
        val sessionId: String,
        val sessionKey: SecretKey,
        val expiresAt: Long,
        val isActive: Boolean = true
    )
    
    /**
     * Gets the current active session ID.
     */
    fun getCurrentSessionId(): String? {
        val sessionId = sharedPrefs.getString(PREF_SESSION_ID, null)
        val expiry = sharedPrefs.getLong(PREF_SESSION_EXPIRY, 0)
        
        return if (sessionId != null && System.currentTimeMillis() < expiry) {
            sessionId
        } else {
            // Session expired or doesn't exist
            clearSession()
            null
        }
    }
    
    /**
     * Gets the session key for the current session.
     */
    fun getCurrentSessionKey(): SecretKey? {
        val sessionId = getCurrentSessionId() ?: return null
        return sessionKeys[sessionId]?.sessionKey
    }
    
    /**
     * Initiates a new session by generating ECDH key pair.
     * 
     * @return Key exchange request data to send to server
     */
    suspend fun initiateSession(): KeyExchangeService.KeyExchangeRequest? = withContext(Dispatchers.IO) {
        try {
            // Generate new session ID
            val sessionId = UUID.randomUUID().toString()
            
            // Generate ECDH key pair
            val publicKey = keyExchangeService.generateKeyPair(sessionId)
                ?: throw Exception("Failed to generate key pair")
            
            Log.d(TAG, "Initiated new session: $sessionId")
            
            KeyExchangeService.KeyExchangeRequest(
                clientPublicKey = publicKey,
                sessionId = sessionId,
                algorithm = "ECDH-P256"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate session", e)
            null
        }
    }
    
    /**
     * Completes the key exchange by deriving the session key from server's public key.
     * 
     * @param response Server's key exchange response
     * @return True if session was established successfully
     */
    suspend fun establishSession(response: KeyExchangeService.KeyExchangeResponse): Boolean = 
        withContext(Dispatchers.IO) {
            try {
                // Derive session key using ECDH
                val sessionKey = keyExchangeService.deriveSessionKey(
                    response.sessionId,
                    response.serverPublicKey
                ) ?: throw Exception("Failed to derive session key")
                
                // Store session info
                val sessionInfo = SessionInfo(
                    sessionId = response.sessionId,
                    sessionKey = sessionKey,
                    expiresAt = response.expiresAt
                )
                
                sessionKeys[response.sessionId] = sessionInfo
                
                // Save to preferences
                sharedPrefs.edit().apply {
                    putString(PREF_SESSION_ID, response.sessionId)
                    putLong(PREF_SESSION_EXPIRY, response.expiresAt)
                    apply()
                }
                
                // Cleanup ECDH keys (no longer needed after deriving session key)
                keyExchangeService.cleanupSession(response.sessionId)
                
                Log.d(TAG, "Session established successfully: ${response.sessionId}")
                true
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to establish session", e)
                false
            }
        }
    
    /**
     * Checks if there's an active session.
     */
    fun hasActiveSession(): Boolean {
        val sessionId = getCurrentSessionId()
        return sessionId != null && sessionKeys.containsKey(sessionId)
    }
    
    /**
     * Refreshes the current session by initiating a new key exchange.
     * This should be called before the session expires.
     */
    suspend fun refreshSession(): KeyExchangeService.KeyExchangeRequest? {
        // Clear current session
        clearSession()
        
        // Initiate new session
        return initiateSession()
    }
    
    /**
     * Clears the current session and all associated keys.
     */
    fun clearSession() {
        val sessionId = sharedPrefs.getString(PREF_SESSION_ID, null)
        
        // Clear from memory
        sessionId?.let {
            sessionKeys.remove(it)
            keyExchangeService.cleanupSession(it)
        }
        
        // Clear from preferences
        sharedPrefs.edit().apply {
            remove(PREF_SESSION_ID)
            remove(PREF_SESSION_EXPIRY)
            apply()
        }
        
        Log.d(TAG, "Session cleared")
    }
    
    /**
     * Gets the remaining time for the current session in milliseconds.
     * 
     * @return Remaining time, or 0 if no active session
     */
    fun getSessionRemainingTime(): Long {
        val expiry = sharedPrefs.getLong(PREF_SESSION_EXPIRY, 0)
        val remaining = expiry - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0
    }
    
    /**
     * Checks if the session needs refresh (less than 5 minutes remaining).
     */
    fun needsRefresh(): Boolean {
        val remaining = getSessionRemainingTime()
        return remaining in 1..300000 // Between 0 and 5 minutes
    }
}