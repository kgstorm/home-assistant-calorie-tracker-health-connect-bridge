package com.kgstorm.healthconnectbridge.stub

import com.kgstorm.healthconnectbridge.models.HomeAssistantState
import kotlinx.coroutines.delay
import retrofit2.Response
import kotlin.random.Random

/**
 * Stub implementation of Home Assistant API for testing without a real HA instance.
 * Provides mock responses for development and testing purposes.
 */
class StubHomeAssistantApi {
    
    /**
     * Get stub entity state response
     * Simulates a calorie tracker sensor response from Home Assistant
     */
    suspend fun getEntityState(entityId: String): Response<HomeAssistantState> {
        // Simulate network delay
        delay(300)
        
        // Generate realistic calorie value
        val calorieValue = Random.nextDouble(1500.0, 2500.0)
        
        val state = HomeAssistantState(
            entity_id = entityId,
            state = String.format("%.1f", calorieValue),
            attributes = mapOf(
                "unit_of_measurement" to "kcal",
                "friendly_name" to "Calorie Tracker",
                "icon" to "mdi:food-apple"
            ),
            last_changed = "2025-12-15T08:00:00.000Z",
            last_updated = "2025-12-15T08:00:00.000Z"
        )
        
        return Response.success(state)
    }
    
    /**
     * Get stub states for all entities
     */
    suspend fun getAllStates(): Response<List<HomeAssistantState>> {
        // Simulate network delay
        delay(300)
        
        val states = listOf(
            HomeAssistantState(
                entity_id = "sensor.calorie_tracker",
                state = String.format("%.1f", Random.nextDouble(1500.0, 2500.0)),
                attributes = mapOf(
                    "unit_of_measurement" to "kcal",
                    "friendly_name" to "Calorie Tracker"
                ),
                last_changed = "2025-12-15T08:00:00.000Z",
                last_updated = "2025-12-15T08:00:00.000Z"
            )
        )
        
        return Response.success(states)
    }
}
