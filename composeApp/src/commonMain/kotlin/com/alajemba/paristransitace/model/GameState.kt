package com.alajemba.paristransitace.model

data class GameState(
    val money: Double = 100.00,
    val morale: Int = 30,
    val alerts: Int = 0,
    val currentScenarioId: String = "SCENARIO_1",
    val logHistory: List<String> = emptyList()
)

data class GameOption(
    val id: String,
    val text: String,
    val cost: Double = .0,
    val moraleImpact: Int = 0
)