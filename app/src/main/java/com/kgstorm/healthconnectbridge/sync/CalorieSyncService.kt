package com.kgstorm.healthconnectbridge.sync

import android.content.Context
import android.util.Log
import com.kgstorm.healthconnectbridge.HealthConnectManager
import com.kgstorm.healthconnectbridge.PreferencesManager
import com.kgstorm.healthconnectbridge.api.HomeAssistantClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Service for syncing calorie data between Home Assistant and Health Connect
 */
class CalorieSyncService(private val context: Context) {
    
    private val healthConnectManager = HealthConnectManager(context)
    private val preferencesManager = PreferencesManager(context)
    
    companion object {
        private const val TAG = "CalorieSyncService"
        private const val CALORIE_ENTITY_ID = "sensor.calorie_tracker"
    }
    
    /**
     * Perform a sync operation
     * @return Result indicating success or failure with error message
     */
    suspend fun performSync(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting calorie sync")
            
            // Check if Health Connect is available
            if (!healthConnectManager.isAvailable()) {
                return@withContext Result.failure(Exception("Health Connect is not available"))
            }
            
            // Check permissions
            if (!healthConnectManager.hasAllPermissions()) {
                return@withContext Result.failure(Exception("Health Connect permissions not granted"))
            }
            
            // Get Home Assistant credentials
            val haUrl = preferencesManager.homeAssistantUrl.first()
            val haToken = preferencesManager.homeAssistantToken.first()
            
            if (haUrl.isNullOrBlank() || haToken.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Home Assistant credentials not configured"))
            }
            
            // Create API service
            val apiService = HomeAssistantClient.createService(haUrl, enableLogging = true)
            val authHeader = "Bearer $haToken"
            
            // Fetch calorie data from Home Assistant
            Log.d(TAG, "Fetching data from Home Assistant")
            val response = apiService.getEntityState(CALORIE_ENTITY_ID, authHeader)
            
            if (!response.isSuccessful) {
                val errorMsg = "Failed to fetch data from Home Assistant: ${response.code()} ${response.message()}"
                Log.e(TAG, errorMsg)
                return@withContext Result.failure(Exception(errorMsg))
            }
            
            val entityState = response.body()
            if (entityState == null) {
                return@withContext Result.failure(Exception("Empty response from Home Assistant"))
            }
            
            // Parse calorie value
            val caloriesStr = entityState.state
            val calories = caloriesStr.toDoubleOrNull()
            
            if (calories == null || calories <= 0) {
                Log.d(TAG, "No valid calorie data to sync (value: $caloriesStr)")
                return@withContext Result.success("No data to sync")
            }
            
            // Write to Health Connect
            Log.d(TAG, "Writing $calories calories to Health Connect")
            val now = Instant.now()
            val startTime = now.minus(1, ChronoUnit.MINUTES) // 1 minute ago
            
            val writeResult = healthConnectManager.writeNutritionRecord(
                calories = calories,
                startTime = startTime,
                endTime = now
            )
            
            writeResult.fold(
                onSuccess = {
                    Log.d(TAG, "Successfully synced $calories calories")
                    preferencesManager.saveLastSyncTime(System.currentTimeMillis())
                    Result.success("Synced $calories calories successfully")
                },
                onFailure = { e ->
                    val errorMsg = "Failed to write to Health Connect: ${e.message}"
                    Log.e(TAG, errorMsg, e)
                    Result.failure(Exception(errorMsg, e))
                }
            )
        } catch (e: Exception) {
            val errorMsg = "Sync failed: ${e.message}"
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        }
    }
    
    /**
     * Get the total calories for today from Health Connect
     */
    suspend fun getTodayCalories(): Result<Double> {
        return healthConnectManager.getTodayCalories()
    }
}
