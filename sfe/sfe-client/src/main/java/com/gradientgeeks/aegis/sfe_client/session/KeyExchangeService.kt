package com.gradientgeeks.aegis.sfe_client.session

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Service for performing Elliptic Curve Diffie-Hellman (ECDH) key exchange
 * to establish temporary session keys for payload encryption.
 */
class KeyExchangeService {
    companion object {
        private const val TAG = "KeyExchangeService"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val EC_CURVE = "secp256r1" // NIST P-256 curve
        private const val KEY_AGREEMENT_ALGORITHM = "ECDH"
        private const val SESSION_KEY_ALGORITHM = "AES"
        private const val SESSION_KEY_ALIAS_PREFIX = "aegis_session_keypair_"
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    
    /**
     * Generates a new ECDH key pair for key exchange.
     * The private key is stored in Android Keystore, public key is returned.
     * 
     * @param sessionId Unique identifier for this session
     * @return Base64-encoded public key to send to server
     */
    fun generateKeyPair(sessionId: String): String? {
        return try {
            val alias = "$SESSION_KEY_ALIAS_PREFIX$sessionId"
            
            // Delete any existing key pair for this session
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            }
            
            // Generate ECDH key pair in Android Keystore
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                ANDROID_KEYSTORE
            )
            
            val paramSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_AGREE_KEY
            ).apply {
                setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
                setUserAuthenticationRequired(false)
                setRandomizedEncryptionRequired(false)
            }.build()
            
            keyPairGenerator.initialize(paramSpec)
            val keyPair = keyPairGenerator.generateKeyPair()
            
            // Export public key
            val publicKeyBytes = keyPair.public.encoded
            val encodedPublicKey = Base64.encodeToString(publicKeyBytes, Base64.NO_WRAP)
            
            Log.d(TAG, "Generated ECDH key pair for session: $sessionId")
            encodedPublicKey
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate ECDH key pair", e)
            null
        }
    }
    
    /**
     * Performs ECDH key agreement to derive a shared secret.
     * 
     * @param sessionId Session identifier
     * @param serverPublicKeyBase64 Server's public key in Base64 format
     * @return Derived AES session key, or null on failure
     */
    fun deriveSessionKey(sessionId: String, serverPublicKeyBase64: String): SecretKey? {
        return try {
            val alias = "$SESSION_KEY_ALIAS_PREFIX$sessionId"
            
            // Get our private key from keystore
            val privateKey = keyStore.getKey(alias, null) as? PrivateKey
                ?: throw IllegalStateException("Private key not found for session: $sessionId")
            
            // Decode server's public key
            val serverPublicKeyBytes = Base64.decode(serverPublicKeyBase64, Base64.NO_WRAP)
            val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
            val serverPublicKey = keyFactory.generatePublic(
                X509EncodedKeySpec(serverPublicKeyBytes)
            )
            
            // Perform ECDH key agreement
            val keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM)
            keyAgreement.init(privateKey)
            keyAgreement.doPhase(serverPublicKey, true)
            
            // Generate shared secret
            val sharedSecret = keyAgreement.generateSecret()
            
            // Derive AES-256 session key from shared secret using SHA-256
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val sessionKeyBytes = messageDigest.digest(sharedSecret)
            
            val sessionKey = SecretKeySpec(sessionKeyBytes, SESSION_KEY_ALGORITHM)
            Log.d(TAG, "Successfully derived session key for session: $sessionId")
            
            sessionKey
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to derive session key", e)
            null
        }
    }
    
    /**
     * Cleans up ECDH key pair for a session.
     * 
     * @param sessionId Session identifier
     */
    fun cleanupSession(sessionId: String) {
        try {
            val alias = "$SESSION_KEY_ALIAS_PREFIX$sessionId"
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
                Log.d(TAG, "Cleaned up ECDH key pair for session: $sessionId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup session keys", e)
        }
    }
    
    /**
     * Data class for key exchange request
     */
    data class KeyExchangeRequest(
        val clientPublicKey: String,
        val sessionId: String,
        val algorithm: String = "ECDH-P256"
    )
    
    /**
     * Data class for key exchange response
     */
    data class KeyExchangeResponse(
        val serverPublicKey: String,
        val sessionId: String,
        val algorithm: String,
        val expiresAt: Long
    )
}