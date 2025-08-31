package com.aegis.sfe.data.api

import com.aegis.sfe.data.model.*
import com.gradientgeeks.aegis.sfe_client.session.KeyExchangeService
import com.gradientgeeks.aegis.sfe_client.encryption.PayloadEncryptionService
import retrofit2.Response
import retrofit2.http.*

interface BankApiService {
    
    @GET("accounts/{accountNumber}")
    suspend fun getAccount(
        @Path("accountNumber") accountNumber: String
    ): Response<AccountResponse>
    
    @GET("accounts/user/{userId}")
    suspend fun getUserAccounts(
        @Path("userId") userId: String
    ): Response<List<AccountResponse>>
    
    @GET("accounts/{accountNumber}/validate")
    suspend fun validateAccount(
        @Path("accountNumber") accountNumber: String
    ): Response<Boolean>
    
    @POST("transactions/transfer")
    suspend fun transferMoney(
        @Body request: TransferRequest
    ): Response<TransferResponse>
    
    @GET("transactions/account/{accountNumber}")
    suspend fun getTransactionHistory(
        @Path("accountNumber") accountNumber: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<TransactionHistoryResponse>
    
    @GET("transactions/reference/{transactionReference}")
    suspend fun getTransactionByReference(
        @Path("transactionReference") transactionReference: String
    ): Response<TransactionResponse>
    
    @GET("health")
    suspend fun getHealthStatus(): Response<Map<String, Any>>
    
    // Session management endpoints
    @POST("session/key-exchange")
    suspend fun initiateKeyExchange(
        @Body request: KeyExchangeService.KeyExchangeRequest
    ): Response<KeyExchangeService.KeyExchangeResponse>
    
    @DELETE("session/{sessionId}")
    suspend fun terminateSession(
        @Path("sessionId") sessionId: String
    ): Response<Map<String, String>>
    
    @GET("session/{sessionId}/status")
    suspend fun checkSessionStatus(
        @Path("sessionId") sessionId: String
    ): Response<Map<String, Any>>
    
    // Secure transfer endpoint (encrypted payload)
    @POST("transactions/transfer/secure")
    suspend fun secureTransferMoney(
        @Body request: PayloadEncryptionService.SecureRequest,
        @Header("X-Session-Id") sessionId: String
    ): Response<PayloadEncryptionService.SecureResponse>
}