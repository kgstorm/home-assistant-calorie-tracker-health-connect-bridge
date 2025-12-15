package com.kgstorm.healthconnectbridge.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Home Assistant API client factory
 */
object HomeAssistantClient {
    
    /**
     * Create a Home Assistant API service instance
     * @param baseUrl The base URL of the Home Assistant instance
     * @param enableLogging Enable HTTP logging for debugging
     */
    fun createService(baseUrl: String, enableLogging: Boolean = false): HomeAssistantApi {
        // Ensure URL ends with /
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        
        // Configure OkHttp client
        val httpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        
        // Add logging interceptor if enabled
        if (enableLogging) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            httpClientBuilder.addInterceptor(loggingInterceptor)
        }
        
        val httpClient = httpClientBuilder.build()
        
        // Configure Gson
        val gson = GsonBuilder()
            .setLenient()
            .create()
        
        // Build Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        return retrofit.create(HomeAssistantApi::class.java)
    }
}
