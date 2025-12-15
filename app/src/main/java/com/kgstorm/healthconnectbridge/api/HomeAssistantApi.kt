package com.kgstorm.healthconnectbridge.api

import com.kgstorm.healthconnectbridge.models.HomeAssistantState
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * Home Assistant API interface
 */
interface HomeAssistantApi {
    
    /**
     * Get state of a specific entity
     */
    @GET("api/states/{entity_id}")
    suspend fun getEntityState(
        @Path("entity_id") entityId: String,
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<HomeAssistantState>
    
    /**
     * Get all states
     */
    @GET("api/states")
    suspend fun getAllStates(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<List<HomeAssistantState>>
}
