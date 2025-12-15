package com.kgstorm.healthconnectbridge.stub

import com.kgstorm.healthconnectbridge.models.CalorieData
import java.time.Instant
import kotlin.random.Random

/**
 * Stub data provider for testing Health Connect integration without requiring Home Assistant.
 * This provides mock calorie data for development and testing purposes.
 */
object StubDataProvider {
    
    /**
     * Get stub calorie data
     * Returns mock calorie values for testing
     */
    fun getStubCalorieData(): CalorieData {
        // Generate realistic calorie values between 1500 and 2500
        val calories = Random.nextDouble(1500.0, 2500.0)
        
        return CalorieData(
            calories = calories,
            timestamp = Instant.now(),
            source = "Stub Data"
        )
    }
    
    /**
     * Get a list of stub calorie entries for testing
     * @param count Number of entries to generate
     */
    fun getStubCalorieDataList(count: Int = 5): List<CalorieData> {
        return List(count) { index ->
            CalorieData(
                calories = Random.nextDouble(200.0, 800.0), // Individual meal calories
                timestamp = Instant.now().minusSeconds(index * 3600L), // Each entry 1 hour apart
                source = "Stub Data"
            )
        }
    }
    
    /**
     * Get total calories for today (stub)
     */
    fun getStubTodayTotalCalories(): Double {
        return Random.nextDouble(1800.0, 2200.0)
    }
}
