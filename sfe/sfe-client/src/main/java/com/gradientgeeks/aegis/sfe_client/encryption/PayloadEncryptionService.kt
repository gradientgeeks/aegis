package com.gradientgeeks.aegis.sfe_client.encryption

import android.util.Base64
import android.util.Log
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Service for encrypting and decrypting request/response payloads using AES-256-GCM.
 * Provides authenticated encryption with associated data (AEAD).
 */
class PayloadEncryptionService {
    companion object {
        private const val TAG = "PayloadEncryptionService"
        private const val AES_GCM_ALGORITHM = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12 // 96 bits recommended for GCM
        private const val GCM_TAG_LENGTH = 128 // 128 bits for authentication tag
        private const val ENCRYPTED_PAYLOAD_VERSION = 1
    }
    
    private val secureRandom = SecureRandom()
    
    /**
     * Encrypts a payload using AES-256-GCM with the provided session key.
     * 
     * @param payload The data to encrypt (as string)
     * @param sessionKey The AES-256 session key
     * @param associatedData Optional additional authenticated data (AAD)
     * @return Encrypted payload with metadata, or null on failure
     */
    fun encryptPayload(
        payload: String,
        sessionKey: SecretKey,
        associatedData: String? = null
    ): EncryptedPayload? {
        return try {
            // Generate random IV
            val iv = ByteArray(GCM_IV_LENGTH)
            secureRandom.nextBytes(iv)
            
            // Initialize cipher
            val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, sessionKey, gcmSpec)
            
            // Add associated data if provided
            associatedData?.let {
                cipher.updateAAD(it.toByteArray(StandardCharsets.UTF_8))
            }
            
            // Encrypt the payload
            val payloadBytes = payload.toByteArray(StandardCharsets.UTF_8)
            val ciphertext = cipher.doFinal(payloadBytes)
            
            // Create encrypted payload object
            val encryptedPayload = EncryptedPayload(
                version = ENCRYPTED_PAYLOAD_VERSION,
                iv = Base64.encodeToString(iv, Base64.NO_WRAP),
                ciphertext = Base64.encodeToString(ciphertext, Base64.NO_WRAP),
                algorithm = "AES-256-GCM",
                aad = associatedData
            )
            
            Log.d(TAG, "Successfully encrypted payload (${payloadBytes.size} bytes)")
            encryptedPayload
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt payload", e)
            null
        }
    }
    
    /**
     * Decrypts a payload using AES-256-GCM with the provided session key.
     * 
     * @param encryptedPayload The encrypted payload object
     * @param sessionKey The AES-256 session key
     * @return Decrypted payload as string, or null on failure
     */
    fun decryptPayload(
        encryptedPayload: EncryptedPayload,
        sessionKey: SecretKey
    ): String? {
        return try {
            // Validate version
            if (encryptedPayload.version != ENCRYPTED_PAYLOAD_VERSION) {
                Log.e(TAG, "Unsupported encryption version: ${encryptedPayload.version}")
                return null
            }
            
            // Decode IV and ciphertext
            val iv = Base64.decode(encryptedPayload.iv, Base64.NO_WRAP)
            val ciphertext = Base64.decode(encryptedPayload.ciphertext, Base64.NO_WRAP)
            
            // Initialize cipher
            val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, sessionKey, gcmSpec)
            
            // Add associated data if provided
            encryptedPayload.aad?.let {
                cipher.updateAAD(it.toByteArray(StandardCharsets.UTF_8))
            }
            
            // Decrypt the payload
            val decryptedBytes = cipher.doFinal(ciphertext)
            val decryptedPayload = String(decryptedBytes, StandardCharsets.UTF_8)
            
            Log.d(TAG, "Successfully decrypted payload (${decryptedBytes.size} bytes)")
            decryptedPayload
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt payload", e)
            null
        }
    }
    
    /**
     * Creates a secure request envelope containing both encrypted and metadata.
     * 
     * @param payload Original request payload
     * @param sessionKey Session key for encryption
     * @param requestMetadata Additional metadata (method, URI, etc.)
     * @return Secure request envelope
     */
    fun createSecureRequest(
        payload: String,
        sessionKey: SecretKey,
        requestMetadata: RequestMetadata
    ): SecureRequest? {
        // Create AAD from request metadata for additional authentication
        val aad = "${requestMetadata.method}|${requestMetadata.uri}|${requestMetadata.timestamp}"
        
        // Encrypt the payload
        val encryptedPayload = encryptPayload(payload, sessionKey, aad) ?: return null
        
        return SecureRequest(
            encryptedPayload = encryptedPayload,
            metadata = requestMetadata
        )
    }
    
    /**
     * Validates and extracts payload from a secure response.
     * 
     * @param secureResponse The secure response envelope
     * @param sessionKey Session key for decryption
     * @param expectedMetadata Expected metadata for validation
     * @return Decrypted response payload, or null on failure
     */
    fun extractSecureResponse(
        secureResponse: SecureResponse,
        sessionKey: SecretKey,
        expectedMetadata: ResponseMetadata? = null
    ): String? {
        // Validate metadata if provided
        expectedMetadata?.let {
            if (secureResponse.metadata.statusCode != it.statusCode) {
                Log.e(TAG, "Response metadata mismatch")
                return null
            }
        }
        
        // Decrypt the payload
        return decryptPayload(secureResponse.encryptedPayload, sessionKey)
    }
    
    /**
     * Data class representing an encrypted payload.
     */
    data class EncryptedPayload(
        val version: Int,
        val iv: String,           // Base64 encoded initialization vector
        val ciphertext: String,    // Base64 encoded ciphertext (includes auth tag)
        val algorithm: String,
        val aad: String? = null    // Associated authenticated data (if any)
    )
    
    /**
     * Request metadata for additional security context.
     */
    data class RequestMetadata(
        val method: String,
        val uri: String,
        val timestamp: Long,
        val sessionId: String
    )
    
    /**
     * Response metadata for validation.
     */
    data class ResponseMetadata(
        val statusCode: Int,
        val timestamp: Long,
        val sessionId: String
    )
    
    /**
     * Secure request envelope containing encrypted payload and metadata.
     */
    data class SecureRequest(
        val encryptedPayload: EncryptedPayload,
        val metadata: RequestMetadata
    )
    
    /**
     * Secure response envelope containing encrypted payload and metadata.
     */
    data class SecureResponse(
        val encryptedPayload: EncryptedPayload,
        val metadata: ResponseMetadata
    )
}