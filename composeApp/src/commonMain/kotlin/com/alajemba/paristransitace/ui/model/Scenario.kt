package com.alajemba.paristransitace.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class StoryLine(
    val id: Long?,
    val title: String,
    val description: String,
    val timeCreated: Long?,
    val initialBudget: Double,
    val initialMorale: Int
) {
    companion object {
        val EMPTY = StoryLine(
            title = "",
            description = "",
            timeCreated = null,
            initialBudget = .0,
            initialMorale = 0,
            id = null
        )
    }
}

value class ScenariosWrapper (
    val wrapper: Pair<StoryLine, List<Scenario>>
){
    val storyLine: StoryLine
        get() = wrapper.first

    val scenarios: List<Scenario>
        get() = wrapper.second
}

@Serializable
data class Scenario(
    val id: String,
    val title: String,
    val description: String,
    val options: List<ScenarioOption>,
    val correctOptionId: String, // Can be an empty string for some scenarios that have no "correct answer" only repercussions
    val nextScenarioId: String? = null,
    val currentIndexInGame: Int = -1,
    val scenarioTheme: ScenarioTheme
)


@Serializable
data class ScenarioOption(
    val id: String,
    val text: String,
    val budgetImpact: Double = .0, // positive (+) or negative (-) impact on budget
    val moraleImpact: Int = 0, // positive (+) or negative (-) impact on morale
    val commentary: String = "",
    val inventory: List<GameInventory> = emptyList(),
    val increaseLegalInfractionsBy: Int = 0 // positive (+) impact on legal infractions
)
