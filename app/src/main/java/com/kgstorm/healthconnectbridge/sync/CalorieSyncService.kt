package com.kgstorm.healthconnectbridge.sync

import android.content.Context
import android.util.Log
import com.kgstorm.healthconnectbridge.HealthConnectManager
import com.kgstorm.healthconnectbridge.PreferencesManager
import com.kgstorm.healthconnectbridge.api.HomeAssistantClient
import com.kgstorm.healthconnectbridge.stub.StubHomeAssistantApi
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
            
            // Check if we should use stub data
            val useStubData = preferencesManager.useStubData.first()
            
            val calories = if (useStubData) {
                // Use stub data for testing
                Log.d(TAG, "Using stub data for sync")
                val stubApi = StubHomeAssistantApi()
                val stubResponse = stubApi.getEntityState(CALORIE_ENTITY_ID)
                
                if (!stubResponse.isSuccessful || stubResponse.body() == null) {
                    return@withContext Result.failure(Exception("Failed to get stub data"))
                }
                
                val caloriesStr = stubResponse.body()!!.state
                caloriesStr.toDoubleOrNull() ?: 0.0
            } else {
                // Use real Home Assistant API
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
                caloriesStr.toDoubleOrNull() ?: 0.0
            }
            
            if (calories <= 0) {
                Log.d(TAG, "No valid calorie data to sync (value: $calories)")
                return@withContext Result.success("No data to sync")
            }
            
            // Calculate time range for this sync
            val now = Instant.now()
            val startTime = now.minus(1, ChronoUnit.MINUTES) // 1 minute ago
            
            // Check for idempotency: get last synced timestamp and check for overlapping records
            val lastSyncedTimestamp = preferencesManager.lastSyncedTimestampNutrition.first()
            
            if (lastSyncedTimestamp != null) {
                val lastSyncedInstant = Instant.ofEpochMilli(lastSyncedTimestamp)
                
                // Check if current sync time range overlaps with the last synced timestamp
                // If the startTime of current sync is before or equal to the last synced time,
                // we need to check if records already exist in this range
                if (startTime.isBefore(lastSyncedInstant) || startTime == lastSyncedInstant) {
                    Log.d(TAG, "Checking for existing records to avoid duplicates (last synced: $lastSyncedInstant)")
                    
                    // Check if records already exist in the overlapping time range
                    val hasExistingRecords = healthConnectManager.hasNutritionRecordsInRange(
                        startTime = startTime,
                        endTime = now
                    )
                    
                    if (hasExistingRecords) {
                        Log.d(TAG, "Records already exist for time range [$startTime, $now]. Skipping write to avoid duplicates.")
                        return@withContext Result.success("Sync skipped - data already exists for this time range")
                    }
                }
            }
            
            // Write to Health Connect
            Log.d(TAG, "Writing $calories calories to Health Connect")
            
            val writeResult = healthConnectManager.writeNutritionRecord(
                calories = calories,
                startTime = startTime,
                endTime = now
            )
            
            // Only update timestamps after successful write to Health Connect
            if (writeResult.isSuccess) {
                Log.d(TAG, "Successfully synced $calories calories")
                
                // Update timestamps - these must complete before we return success
                try {
                    val currentTimeMillis = System.currentTimeMillis()
                    preferencesManager.saveLastSyncTime(currentTimeMillis)
                    // Save the end time of this sync as the last synced timestamp for nutrition records
                    preferencesManager.saveLastSyncedTimestampNutrition(now.toEpochMilli())
                    
                    return@withContext Result.success("Synced $calories calories successfully")
                } catch (e: Exception) {
                    // If timestamp update fails, log but still consider the sync successful
                    // since the data was written to Health Connect
                    Log.w(TAG, "Failed to update timestamps after successful sync: ${e.message}", e)
                    return@withContext Result.success("Synced $calories calories successfully")
                }
            } else {
                val e = writeResult.exceptionOrNull()
                val errorMsg = "Failed to write to Health Connect: ${e?.message}"
                Log.e(TAG, errorMsg, e)
                return@withContext Result.failure(Exception(errorMsg, e))
            }
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
