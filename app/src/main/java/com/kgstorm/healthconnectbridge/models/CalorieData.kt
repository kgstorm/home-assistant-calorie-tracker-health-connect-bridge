package com.kgstorm.healthconnectbridge.models

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Represents calorie intake data
 */
data class CalorieData(
    val calories: Double,
    val timestamp: Instant,
    val source: String = "Home Assistant"
)

/**
 * Home Assistant sensor state response
 */
@Serializable
data class HomeAssistantState(
    val entity_id: String,
    val state: String,
    val attributes: Map<String, String> = emptyMap(),
    val last_changed: String,
    val last_updated: String
)

/**
 * Response from Home Assistant API
 */
@Serializable
data class HomeAssistantResponse(
    val message: String? = null
)
