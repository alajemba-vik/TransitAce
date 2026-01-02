package com.alajemba.paristransitace.domain.model

data class Scenario(
    val id: String,
    val title: String,
    val description: String,
    val options: List<ScenarioOption>,
    val correctOptionId: String,
    val nextScenarioId: String? = null,
    val currentIndexInGame: Int = -1,
    val scenarioTheme: ScenarioTheme
)

data class ScenarioOption(
    val id: String,
    val text: String,
    val budgetImpact: Double = 0.0,
    val moraleImpact: Int = 0,
    val commentary: String = "",
    val inventory: List<GameInventory> = emptyList(),
    val increaseLegalInfractionsBy: Int = 0
)

data class GameInventory(
    val name: String,
    val description: String,
    val imageUrl: String
)

enum class ScenarioTheme {
    MORNING,
    METRO_PLATFORM,
    BUS_INTERIOR,
    RER_PACKED,
    TICKET_MACHINE,
    TURNSTILE_SUCCESS,
    INSPECTORS,
    GATE_JUMP,
    GETTING_FINED,
    BROKEN_GATE,
    STRIKE_CROWD,
    EIFFEL_TOWER,
    UNIVERSITY,
    SUBURBAN_STATION,
    AIRPORT,
    NIGHT_STREET,
    ROMANCE,
    CONFUSED_MAP,
    LUGGAGE_STRUGGLE,
    CAFE_REWARD,
    EMPTY_LATE,
    DEFAULT;

    companion object {
        fun fromKey(key: String): ScenarioTheme {
            return entries.find { it.name.equals(key, ignoreCase = true) } ?: DEFAULT
        }
    }
}

data class ScenariosWrapper(
    val storyLine: StoryLine,
    val scenarios: List<Scenario>
)