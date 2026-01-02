package com.alajemba.paristransitace.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ScenarioResponse(
    val id: String,
    val title: String,
    val description: String,
    val options: List<ScenarioOptionResponse>,
    val correctOptionId: String,
    val nextScenarioId: String? = null,
    val currentIndexInGame: Int = -1,
    val scenarioTheme: String = "DEFAULT"
)

@Serializable
data class ScenarioOptionResponse(
    val id: String,
    val text: String,
    val budgetImpact: Double = 0.0,
    val moraleImpact: Int = 0,
    val commentary: String = "",
    val inventory: List<GameInventoryResponse> = emptyList(),
    val increaseLegalInfractionsBy: Int = 0
)

@Serializable
data class GameInventoryResponse(
    val name: String,
    val description: String,
    val imageUrl: String = ""
)

@Serializable
data class StoryLineResponse(
    val id: Long?,
    val title: String,
    val description: String,
    val timeCreated: Long?,
    val initialBudget: Double,
    val initialMorale: Int
)