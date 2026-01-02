package com.alajemba.paristransitace.domain.repository

import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.StoryLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds the current game session state in memory.
 */
interface GameSessionRepository {

    val currentStoryLine: StoryLine
    val scenarios: List<Scenario>

    val currentScenario: StateFlow<Scenario?>

    val scenarioProgress: Flow<Float>

    val scenarioProgressText: Flow<String>

    fun setNewSession(storyLine: StoryLine, scenarios: List<Scenario>)

    fun loadStoryForSession(storyId: Long): Boolean


    fun nextScenario(): Boolean

    fun clearCurrentScenario()

    fun clearSession()

}
