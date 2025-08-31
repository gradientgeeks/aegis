package com.gradientgeeks.aegis.sfe_client.metadata

import android.content.Context
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Collects user metadata for policy enforcement while maintaining privacy.
 * Only collects minimal, anonymized data required for security policies.
 */
class UserMetadataCollector(private val context: Context) {
    
    companion object {
        private const val TAG = "UserMetadataCollector"
    }
    
    // Thread-safe storage for user context
    private val userContext = ConcurrentHashMap<String, Any>()
    private val sessionContext = ConcurrentHashMap<String, Any>()
    private val transactionContext = ConcurrentHashMap<String, Any>()
    private val riskFactors = ConcurrentHashMap<String, Boolean>()
    
    /**
     * Sets user session context from bank app
     */
    fun setSessionContext(
        accountTier: String?,
        accountAge: Int?,
        kycLevel: String?,
        hasDeviceBinding: Boolean = false,
        deviceBindingCount: Int = 0
    ) {
        sessionContext.apply {
            accountTier?.let { put("accountTier", it) }
            accountAge?.let { put("accountAge", it) }
            kycLevel?.let { put("kycLevel", it) }
            put("hasDeviceBinding", hasDeviceBinding)
            put("deviceBindingCount", deviceBindingCount)
            put("lastLoginTimestamp", System.currentTimeMillis())
        }
        
        Log.d(TAG, "Session context updated: accountTier=$accountTier, kycLevel=$kycLevel")
    }
    
    /**
     * Sets transaction context for current transaction
     */
    fun setTransactionContext(
        transactionType: String,
        amountRange: String?,
        beneficiaryType: String?,
        timeOfDay: String? = getCurrentTimeOfDay()
    ) {
        transactionContext.apply {
            put("transactionType", transactionType)
            amountRange?.let { put("amountRange", it) }
            beneficiaryType?.let { put("beneficiaryType", it) }
            timeOfDay?.let { put("timeOfDay", it) }
        }
        
        Log.d(TAG, "Transaction context updated: type=$transactionType, amount=$amountRange")
    }
    
    /**
     * Sets risk factors detected during session
     */
    fun setRiskFactors(
        isLocationChanged: Boolean = false,
        isDeviceChanged: Boolean = false,
        isDormantAccount: Boolean = false,
        requiresDeviceRebinding: Boolean = false
    ) {
        riskFactors.apply {
            put("isLocationChanged", isLocationChanged)
            put("isDeviceChanged", isDeviceChanged)
            put("isDormantAccount", isDormantAccount)
            put("requiresDeviceRebinding", requiresDeviceRebinding)
        }
        
        Log.d(TAG, "Risk factors updated: location=$isLocationChanged, device=$isDeviceChanged")
    }
    
    /**
     * Sets anonymized user ID (provided by backend)
     */
    fun setAnonymizedUserId(anonymizedUserId: String) {
        userContext["anonymizedUserId"] = anonymizedUserId
        Log.d(TAG, "Anonymized user ID set")
    }
    
    /**
     * Gets complete metadata for policy enforcement
     */
    fun getMetadata(): Map<String, Any> {
        val metadata = mutableMapOf<String, Any>()
        
        // Add anonymized user ID if available
        userContext["anonymizedUserId"]?.let { 
            metadata["anonymizedUserId"] = it 
        }
        
        // Add session context
        if (sessionContext.isNotEmpty()) {
            metadata["sessionContext"] = HashMap(sessionContext)
        }
        
        // Add transaction context
        if (transactionContext.isNotEmpty()) {
            metadata["transactionContext"] = HashMap(transactionContext)
        }
        
        // Add risk factors
        if (riskFactors.isNotEmpty()) {
            metadata["riskFactors"] = HashMap(riskFactors)
        }
        
        Log.d(TAG, "Metadata collected with ${metadata.keys.size} categories")
        return metadata
    }
    
    /**
     * Gets metadata for specific transaction type
     */
    fun getTransactionMetadata(
        transactionType: String,
        amountRange: String?,
        beneficiaryType: String?
    ): Map<String, Any> {
        // Update transaction context
        setTransactionContext(transactionType, amountRange, beneficiaryType)
        
        // Return complete metadata
        return getMetadata()
    }
    
    /**
     * Clears transaction-specific context
     */
    fun clearTransactionContext() {
        transactionContext.clear()
        Log.d(TAG, "Transaction context cleared")
    }
    
    /**
     * Clears all metadata (for logout)
     */
    fun clearAllMetadata() {
        userContext.clear()
        sessionContext.clear()
        transactionContext.clear()
        riskFactors.clear()
        Log.d(TAG, "All metadata cleared")
    }
    
    /**
     * Updates account tier based on user actions
     */
    fun updateAccountTier(accountTier: String) {
        sessionContext["accountTier"] = accountTier
        Log.d(TAG, "Account tier updated to: $accountTier")
    }
    
    /**
     * Updates KYC level
     */
    fun updateKycLevel(kycLevel: String) {
        sessionContext["kycLevel"] = kycLevel
        Log.d(TAG, "KYC level updated to: $kycLevel")
    }
    
    /**
     * Reports device change detection
     */
    fun reportDeviceChange(isChanged: Boolean) {
        riskFactors["isDeviceChanged"] = isChanged
        if (isChanged) {
            Log.w(TAG, "Device change detected")
        }
    }
    
    /**
     * Reports location change detection
     */
    fun reportLocationChange(isChanged: Boolean) {
        riskFactors["isLocationChanged"] = isChanged
        if (isChanged) {
            Log.w(TAG, "Location change detected")
        }
    }
    
    /**
     * Gets current time of day category
     */
    private fun getCurrentTimeOfDay(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..17 -> "BUSINESS_HOURS"
            in 18..21 -> "AFTER_HOURS"
            else -> "NIGHT"
        }
    }
    
    /**
     * Gets metadata summary for debugging
     */
    fun getMetadataSummary(): String {
        return "UserMetadata: " +
                "session=${sessionContext.keys.joinToString(",")}, " +
                "transaction=${transactionContext.keys.joinToString(",")}, " +
                "risks=${riskFactors.entries.filter { it.value }.map { it.key }.joinToString(",")}"
    }
    
    /**
     * Validates that required metadata is present
     */
    fun hasRequiredMetadata(): Boolean {
        return userContext.containsKey("anonymizedUserId") && 
               sessionContext.isNotEmpty()
    }
    
    /**
     * Creates minimal metadata for non-authenticated requests
     */
    fun getAnonymousMetadata(): Map<String, Any> {
        return mapOf(
            "sessionContext" to mapOf(
                "accountTier" to "ANONYMOUS",
                "accountAge" to 0,
                "kycLevel" to "NONE",
                "hasDeviceBinding" to false,
                "deviceBindingCount" to 0
            ),
            "riskFactors" to mapOf(
                "isLocationChanged" to false,
                "isDeviceChanged" to false,
                "isDormantAccount" to false,
                "requiresDeviceRebinding" to false
            )
        )
    }
}