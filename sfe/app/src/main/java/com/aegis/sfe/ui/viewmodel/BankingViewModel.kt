package com.aegis.sfe.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.sfe.data.model.*
import com.aegis.sfe.data.repository.BankRepository
import com.aegis.sfe.UCOBankApplication
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gradientgeeks.aegis.sfe_client.encryption.PayloadEncryptionService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

class BankingViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "BankingViewModel"
        // Default user ID for demo purposes
        const val DEFAULT_USER_ID = "USER001"
    }
    
    private val repository = BankRepository()
    
    // UI State
    private val _uiState = MutableStateFlow(BankingUiState())
    val uiState: StateFlow<BankingUiState> = _uiState.asStateFlow()
    
    // User accounts
    private val _userAccounts = MutableStateFlow<List<Account>>(emptyList())
    val userAccounts: StateFlow<List<Account>> = _userAccounts.asStateFlow()
    
    // Selected account
    private val _selectedAccount = MutableStateFlow<Account?>(null)
    val selectedAccount: StateFlow<Account?> = _selectedAccount.asStateFlow()
    
    // Transaction history
    private val _transactionHistory = MutableStateFlow<TransactionHistoryResponse?>(null)
    val transactionHistory: StateFlow<TransactionHistoryResponse?> = _transactionHistory.asStateFlow()
    
    // Transfer state
    private val _transferState = MutableStateFlow(TransferState())
    val transferState: StateFlow<TransferState> = _transferState.asStateFlow()
    
    init {
        // Load accounts if user is already logged in
        viewModelScope.launch {
            if (com.aegis.sfe.UCOBankApplication.currentUser != null) {
                android.util.Log.d(TAG, "User is logged in, waiting before loading accounts...")
                // Small delay to ensure navigation and auth token are properly set
                kotlinx.coroutines.delay(500)
                loadUserAccounts()
            } else {
                android.util.Log.d(TAG, "No user logged in, skipping account load")
            }
        }
    }
    
    fun loadUserAccounts() {
        viewModelScope.launch {
            try {
                // Use the actual logged-in user ID instead of default
                val userId = com.aegis.sfe.UCOBankApplication.currentUser?.id?.toString() ?: DEFAULT_USER_ID
                Log.d(TAG, "Loading accounts for userId: $userId")
                repository.getUserAccounts(userId).collect { result ->
                    when (result) {
                        is ApiResult.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        }
                        is ApiResult.Success -> {
                            _userAccounts.value = result.data
                            _selectedAccount.value = result.data.firstOrNull()
                            _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                            Log.d(TAG, "Loaded ${result.data.size} user accounts")
                        }
                        is ApiResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                            Log.e(TAG, "Error loading user accounts: ${result.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadUserAccounts", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load accounts: ${e.message}"
                )
            }
        }
    }
    
    fun selectAccount(account: Account) {
        _selectedAccount.value = account
        loadTransactionHistory(account.accountNumber)
    }
    
    fun loadTransactionHistory(accountNumber: String, page: Int = 0) {
        viewModelScope.launch {
            repository.getTransactionHistory(accountNumber, page).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is ApiResult.Success -> {
                        _transactionHistory.value = result.data
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        Log.d(TAG, "Loaded transaction history for account $accountNumber")
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    fun validateAccount(accountNumber: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.validateAccount(accountNumber).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        callback(result.data)
                    }
                    is ApiResult.Error -> {
                        callback(false)
                    }
                    is ApiResult.Loading -> {
                        // Handle loading state if needed
                    }
                }
            }
        }
    }
    
    fun transferMoney(transferRequest: TransferRequest) {
        viewModelScope.launch {
            repository.transferMoney(transferRequest).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _transferState.value = _transferState.value.copy(
                            isLoading = true,
                            error = null,
                            success = null
                        )
                    }
                    is ApiResult.Success -> {
                        _transferState.value = _transferState.value.copy(
                            isLoading = false,
                            success = result.data,
                            error = null
                        )
                        
                        // Refresh account balance after successful transfer
                        loadUserAccounts()
                        
                        Log.d(TAG, "Transfer completed: ${result.data.transactionReference}")
                    }
                    is ApiResult.Error -> {
                        _transferState.value = _transferState.value.copy(
                            isLoading = false,
                            error = result.message,
                            success = null
                        )
                        Log.e(TAG, "Transfer failed: ${result.message}")
                    }
                }
            }
        }
    }
    
    fun clearTransferState() {
        _transferState.value = TransferState()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun refreshData() {
        loadUserAccounts()
        _selectedAccount.value?.let { account ->
            loadTransactionHistory(account.accountNumber)
        }
    }
    
    // Session management and encrypted transfer methods
    
    private val objectMapper = jacksonObjectMapper()
    
    /**
     * Initiates a secure session for encrypted communication.
     */
    fun initiateSecureSession() {
        viewModelScope.launch {
            try {
                val aegisClient = UCOBankApplication.aegisClient
                
                // Initiate key exchange
                val keyExchangeRequest = aegisClient.initiateSession()
                if (keyExchangeRequest == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to initiate session: SDK not ready"
                    )
                    return@launch
                }
                
                // Send key exchange request to server
                repository.initiateKeyExchange(keyExchangeRequest!!).collect { result ->
                    when (result) {
                        is ApiResult.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        is ApiResult.Success -> {
                            // Establish session with server response
                            val established = aegisClient.establishSession(result.data)
                            if (established) {
                                Log.d(TAG, "Secure session established: ${result.data.sessionId}")
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = null
                                )
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = "Failed to establish session"
                                )
                            }
                        }
                        is ApiResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Session error: ${result.message}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initiating secure session", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Session error: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Performs a money transfer with encrypted payload.
     */
    fun transferMoneySecure(transferRequest: TransferRequest) {
        viewModelScope.launch {
            val aegisClient = UCOBankApplication.aegisClient
            
            // Check if we have an active session
            if (!aegisClient.hasActiveSession()) {
                // Initiate session first
                initiateSecureSession()
                // Wait for session to be established
                kotlinx.coroutines.delay(2000)
                
                if (!aegisClient.hasActiveSession()) {
                    _transferState.value = _transferState.value.copy(
                        isLoading = false,
                        error = "No secure session available"
                    )
                    return@launch
                }
            }
            
            try {
                // Convert transfer request to JSON
                val requestJson = objectMapper.writeValueAsString(transferRequest)
                
                // Create secure request with encrypted payload
                val secureRequest = aegisClient.createSecureRequest(
                    payload = requestJson,
                    method = "POST",
                    uri = "/api/v1/transactions/transfer/secure"
                )
                
                if (secureRequest == null) {
                    _transferState.value = _transferState.value.copy(
                        isLoading = false,
                        error = "Failed to encrypt request"
                    )
                    return@launch
                }
                
                val sessionId = aegisClient.getCurrentSessionId() ?: return@launch
                
                // Send encrypted request
                repository.secureTransferMoney(secureRequest!!, sessionId).collect { result ->
                    when (result) {
                        is ApiResult.Loading -> {
                            _transferState.value = _transferState.value.copy(
                                isLoading = true,
                                error = null
                            )
                        }
                        is ApiResult.Success -> {
                            // Decrypt the response
                            val decryptedJson = aegisClient.extractSecureResponse(result.data)
                            if (decryptedJson != null) {
                                val transferResponse: TransferResponse = objectMapper.readValue(decryptedJson)
                                
                                _transferState.value = _transferState.value.copy(
                                    isLoading = false,
                                    success = transferResponse,
                                    error = null
                                )
                                
                                // Refresh account balance
                                loadUserAccounts()
                                
                                Log.d(TAG, "Secure transfer completed: ${transferResponse.transactionReference}")
                            } else {
                                _transferState.value = _transferState.value.copy(
                                    isLoading = false,
                                    error = "Failed to decrypt response"
                                )
                            }
                        }
                        is ApiResult.Error -> {
                            _transferState.value = _transferState.value.copy(
                                isLoading = false,
                                error = result.message,
                                success = null
                            )
                            Log.e(TAG, "Secure transfer failed: ${result.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing secure transfer", e)
                _transferState.value = _transferState.value.copy(
                    isLoading = false,
                    error = "Transfer error: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Checks if encrypted transfers are enabled (session active).
     */
    fun isSecureTransferEnabled(): Boolean {
        return UCOBankApplication.aegisClient.hasActiveSession()
    }
    
    /**
     * Clears the secure session.
     */
    fun clearSecureSession() {
        UCOBankApplication.aegisClient.clearSession()
    }
}

data class BankingUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

data class TransferState(
    val isLoading: Boolean = false,
    val success: TransferResponse? = null,
    val error: String? = null
)