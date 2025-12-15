package com.kgstorm.healthconnectbridge

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Manages Health Connect client and permissions
 */
class HealthConnectManager(private val context: Context) {

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    companion object {
        // Required permissions for nutrition data
        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(NutritionRecord::class),
            HealthPermission.getWritePermission(NutritionRecord::class)
        )
    }

    /**
     * Check if Health Connect is available on this device
     */
    suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    /**
     * Check if all required permissions are granted
     */
    suspend fun hasAllPermissions(): Boolean = withContext(Dispatchers.IO) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        PERMISSIONS.all { it in granted }
    }

    /**
     * Create permission request contract
     */
    fun createPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    /**
     * Check if nutrition records already exist for the given time range
     * @param startTime Start of the time range
     * @param endTime End of the time range
     * @return true if any records exist in the time range, false otherwise
     */
    suspend fun hasNutritionRecordsInRange(
        startTime: Instant,
        endTime: Instant
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = readNutritionRecords(startTime, endTime)
            result.fold(
                onSuccess = { records -> records.isNotEmpty() },
                onFailure = { false }
            )
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Write nutrition record to Health Connect
     * @param calories Total calories consumed
     * @param startTime Start time of the meal
     * @param endTime End time of the meal
     */
    suspend fun writeNutritionRecord(
        calories: Double,
        startTime: Instant,
        endTime: Instant
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val nutritionRecord = NutritionRecord(
                startTime = startTime,
                startZoneOffset = null,
                endTime = endTime,
                endZoneOffset = null,
                energy = androidx.health.connect.client.units.Energy.kilocalories(calories)
            )
            
            healthConnectClient.insertRecords(listOf(nutritionRecord))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Read nutrition records from Health Connect for a time range
     * @param startTime Start of the time range
     * @param endTime End of the time range
     */
    suspend fun readNutritionRecords(
        startTime: Instant,
        endTime: Instant
    ): Result<List<NutritionRecord>> = withContext(Dispatchers.IO) {
        try {
            val request = ReadRecordsRequest(
                recordType = NutritionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            
            val response = healthConnectClient.readRecords(request)
            Result.success(response.records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get total calories for today
     */
    suspend fun getTodayCalories(): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS)
            val endOfDay = startOfDay.plus(1, ChronoUnit.DAYS)
            
            val result = readNutritionRecords(startOfDay, endOfDay)
            result.fold(
                onSuccess = { records ->
                    val totalCalories = records.sumOf { 
                        it.energy?.inKilocalories ?: 0.0 
                    }
                    Result.success(totalCalories)
                },
                onFailure = { e -> Result.failure(e) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Open Health Connect settings
     */
    fun openHealthConnectSettings() {
        val intent = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
