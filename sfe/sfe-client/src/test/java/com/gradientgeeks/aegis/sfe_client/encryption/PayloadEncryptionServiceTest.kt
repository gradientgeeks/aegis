package com.gradientgeeks.aegis.sfe_client.encryption

import org.junit.Assert.*
import org.junit.Test
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class PayloadEncryptionServiceTest {
    
    private val encryptionService = PayloadEncryptionService()
    
    private fun generateTestKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }
    
    @Test
    fun testEncryptDecryptPayload() {
        // Arrange
        val testPayload = """
            {
                "fromAccount": "123456789012",
                "toAccount": "987654321098",
                "amount": 1000.00,
                "currency": "INR",
                "description": "Test transfer"
            }
        """.trimIndent()
        
        val sessionKey = generateTestKey()
        val associatedData = "POST|/api/v1/transfer|1234567890"
        
        // Act - Encrypt
        val encryptedPayload = encryptionService.encryptPayload(
            testPayload,
            sessionKey,
            associatedData
        )
        
        // Assert encryption
        assertNotNull("Encrypted payload should not be null", encryptedPayload)
        assertEquals("AES-256-GCM", encryptedPayload?.algorithm)
        assertEquals(1, encryptedPayload?.version)
        assertNotNull(encryptedPayload?.iv)
        assertNotNull(encryptedPayload?.ciphertext)
        assertEquals(associatedData, encryptedPayload?.aad)
        
        // Act - Decrypt
        val decryptedPayload = encryptionService.decryptPayload(
            encryptedPayload!!,
            sessionKey
        )
        
        // Assert decryption
        assertNotNull("Decrypted payload should not be null", decryptedPayload)
        assertEquals("Decrypted payload should match original", testPayload, decryptedPayload)
    }
    
    @Test
    fun testEncryptionProducesUniqueOutputs() {
        // Arrange
        val testPayload = "Secret message"
        val sessionKey = generateTestKey()
        
        // Act - Encrypt same payload twice
        val encrypted1 = encryptionService.encryptPayload(testPayload, sessionKey)
        val encrypted2 = encryptionService.encryptPayload(testPayload, sessionKey)
        
        // Assert - Different IVs and ciphertexts
        assertNotNull(encrypted1)
        assertNotNull(encrypted2)
        assertNotEquals("IVs should be different", encrypted1?.iv, encrypted2?.iv)
        assertNotEquals("Ciphertexts should be different", encrypted1?.ciphertext, encrypted2?.ciphertext)
    }
    
    @Test
    fun testDecryptionFailsWithWrongKey() {
        // Arrange
        val testPayload = "Secret message"
        val encryptionKey = generateTestKey()
        val wrongKey = generateTestKey()
        
        // Act
        val encryptedPayload = encryptionService.encryptPayload(testPayload, encryptionKey)
        val decryptedPayload = encryptionService.decryptPayload(encryptedPayload!!, wrongKey)
        
        // Assert
        assertNull("Decryption with wrong key should fail", decryptedPayload)
    }
    
    @Test
    fun testAuthenticatedDataValidation() {
        // Arrange
        val testPayload = "Secret message"
        val sessionKey = generateTestKey()
        val originalAAD = "POST|/api/v1/transfer|1234567890"
        
        // Act - Encrypt with AAD
        val encryptedPayload = encryptionService.encryptPayload(
            testPayload,
            sessionKey,
            originalAAD
        )
        
        // Tamper with AAD
        val tamperedPayload = encryptedPayload!!.copy(aad = "GET|/api/v1/transfer|1234567890")
        
        // Act - Try to decrypt with tampered AAD
        val decryptedPayload = encryptionService.decryptPayload(tamperedPayload, sessionKey)
        
        // Assert
        assertNull("Decryption should fail with tampered AAD", decryptedPayload)
    }
    
    @Test
    fun testSecureRequestCreation() {
        // Arrange
        val payload = """{"amount": 1000}"""
        val sessionKey = generateTestKey()
        val metadata = PayloadEncryptionService.RequestMetadata(
            method = "POST",
            uri = "/api/v1/transfer",
            timestamp = System.currentTimeMillis(),
            sessionId = "test-session-123"
        )
        
        // Act
        val secureRequest = encryptionService.createSecureRequest(payload, sessionKey, metadata)
        
        // Assert
        assertNotNull("Secure request should not be null", secureRequest)
        assertEquals(metadata, secureRequest?.metadata)
        assertNotNull(secureRequest?.encryptedPayload)
        
        // Verify AAD was set correctly
        val expectedAAD = "${metadata.method}|${metadata.uri}|${metadata.timestamp}"
        assertEquals(expectedAAD, secureRequest?.encryptedPayload?.aad)
    }
    
    @Test
    fun testSecureResponseExtraction() {
        // Arrange
        val responsePayload = """{"status": "success", "transactionId": "12345"}"""
        val sessionKey = generateTestKey()
        val responseMetadata = PayloadEncryptionService.ResponseMetadata(
            statusCode = 200,
            timestamp = System.currentTimeMillis(),
            sessionId = "test-session-123"
        )
        
        // Create encrypted response
        val aad = "${responseMetadata.statusCode}|${responseMetadata.timestamp}|${responseMetadata.sessionId}"
        val encryptedPayload = encryptionService.encryptPayload(responsePayload, sessionKey, aad)
        
        val secureResponse = PayloadEncryptionService.SecureResponse(
            encryptedPayload = encryptedPayload!!,
            metadata = responseMetadata
        )
        
        // Act
        val extractedPayload = encryptionService.extractSecureResponse(
            secureResponse,
            sessionKey,
            responseMetadata
        )
        
        // Assert
        assertNotNull("Extracted payload should not be null", extractedPayload)
        assertEquals("Extracted payload should match original", responsePayload, extractedPayload)
    }
    
    @Test
    fun testEmptyPayloadEncryption() {
        // Arrange
        val emptyPayload = ""
        val sessionKey = generateTestKey()
        
        // Act
        val encryptedPayload = encryptionService.encryptPayload(emptyPayload, sessionKey)
        val decryptedPayload = encryptionService.decryptPayload(encryptedPayload!!, sessionKey)
        
        // Assert
        assertNotNull(encryptedPayload)
        assertEquals(emptyPayload, decryptedPayload)
    }
    
    @Test
    fun testLargePayloadEncryption() {
        // Arrange - Create a large JSON payload
        val largePayload = buildString {
            append("{\"transactions\": [")
            repeat(100) { i ->
                if (i > 0) append(",")
                append("""{"id": $i, "amount": ${i * 100}, "description": "Transaction $i"}""")
            }
            append("]}")
        }
        
        val sessionKey = generateTestKey()
        
        // Act
        val encryptedPayload = encryptionService.encryptPayload(largePayload, sessionKey)
        val decryptedPayload = encryptionService.decryptPayload(encryptedPayload!!, sessionKey)
        
        // Assert
        assertNotNull(encryptedPayload)
        assertEquals(largePayload, decryptedPayload)
    }
}