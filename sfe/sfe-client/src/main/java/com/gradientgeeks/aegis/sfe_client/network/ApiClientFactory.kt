package com.gradientgeeks.aegis.sfe_client.network

import com.gradientgeeks.aegis.sfe_client.api.AegisApiService
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Factory for creating API clients with proper configuration.
 * 
 * Handles the creation of Retrofit instances and OkHttp clients
 * with appropriate timeouts, logging, interceptors, and certificate pinning.
 */
object ApiClientFactory {
    
    private const val DEFAULT_TIMEOUT_SECONDS = 30L
    private const val DEFAULT_READ_TIMEOUT_SECONDS = 60L
    private const val DEFAULT_WRITE_TIMEOUT_SECONDS = 60L
    
    /**
     * Creates an AegisApiService instance for the given base URL.
     * 
     * @param baseUrl The base URL of the Aegis Security API
     * @param enableLogging Whether to enable HTTP request/response logging
     * @param certificatePin Optional certificate pin for the Aegis API
     * @return Configured AegisApiService instance
     */
    fun createAegisApiService(
        baseUrl: String,
        enableLogging: Boolean = false,
        certificatePin: String? = null
    ): AegisApiService {
        
        val okHttpClient = createOkHttpClient(
            baseUrl = baseUrl,
            certificatePin = certificatePin,
            enableLogging = enableLogging
        )
        
        val retrofit = Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(baseUrl))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return retrofit.create(AegisApiService::class.java)
    }
    
    /**
     * Creates a secure OkHttp client with certificate pinning for bank APIs.
     * 
     * @param baseUrl The base URL of the bank API
     * @param certificatePin The SHA256 certificate pin for the bank's server
     * @param enableLogging Whether to enable HTTP request/response logging
     * @param additionalInterceptors Optional additional interceptors to add
     * @return Configured OkHttpClient with certificate pinning
     */
    fun createSecureBankApiClient(
        baseUrl: String,
        certificatePin: String,
        enableLogging: Boolean = false,
        additionalInterceptors: List<Interceptor>? = null
    ): OkHttpClient {
        return createOkHttpClient(
            baseUrl = baseUrl,
            certificatePin = certificatePin,
            enableLogging = enableLogging,
            additionalInterceptors = additionalInterceptors
        )
    }
    
    /**
     * Creates a configured OkHttp client with optional certificate pinning.
     * 
     * @param baseUrl The base URL for extracting hostname
     * @param certificatePin Optional SHA256 certificate pin
     * @param enableLogging Whether to enable request/response logging
     * @param additionalInterceptors Optional additional interceptors
     * @return Configured OkHttpClient
     */
    private fun createOkHttpClient(
        baseUrl: String? = null,
        certificatePin: String? = null,
        enableLogging: Boolean = false,
        additionalInterceptors: List<Interceptor>? = null
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
        
        // Add certificate pinning if provided
        if (baseUrl != null && certificatePin != null && certificatePin.isNotEmpty()) {
            try {
                val hostname = extractHostname(baseUrl)
                val certificatePinner = CertificatePinner.Builder()
                    .add(hostname, certificatePin)
                    .build()
                builder.certificatePinner(certificatePinner)
            } catch (e: Exception) {
                // Log error but don't fail - allows HTTP URLs in development
                println("Certificate pinning not configured: ${e.message}")
            }
        }
        
        // Add additional interceptors if provided
        additionalInterceptors?.forEach { interceptor ->
            builder.addInterceptor(interceptor)
        }
        
        // Add logging interceptor if enabled
        if (enableLogging) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }
        
        return builder.build()
    }
    
    /**
     * Extracts hostname from a URL string.
     * 
     * @param urlString The URL string
     * @return The hostname
     */
    private fun extractHostname(urlString: String): String {
        return try {
            val url = URL(urlString)
            url.host
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid URL: $urlString", e)
        }
    }
    
    /**
     * Ensures the base URL has a trailing slash.
     * 
     * @param url The URL to check
     * @return URL with trailing slash
     */
    private fun ensureTrailingSlash(url: String): String {
        return if (url.endsWith("/")) url else "$url/"
    }
}