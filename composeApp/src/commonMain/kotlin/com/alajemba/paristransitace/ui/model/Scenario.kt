package com.alajemba.paristransitace.ui.model

data class Scenario(
    val id: String,
    val title: String,
    val description: String,
    val options: List<ScenarioOption>,
    val correctOptionId: Int,
    val nextScenarioId: String? = null, // TODO("Use once data is fetched from backend"
    val currentIndexInGame: Int = 0
)


data class ScenarioOption(
    val id: String,
    val text: String,
    val budgetImpact: Double = .0, // positive (+) or negative (-) impact on budget
    val moraleImpact: Int = 0, // positive (+) or negative (-) impact on morale
    val commentary: String,
    val inventory: List<GameInventory>
)