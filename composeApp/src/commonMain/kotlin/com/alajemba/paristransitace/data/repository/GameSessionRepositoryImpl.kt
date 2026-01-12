package com.alajemba.paristransitace.data.repository

import com.alajemba.paristransitace.data.local.LocalDataSource
import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.StoryLine
import com.alajemba.paristransitace.domain.model.UserStats
import com.alajemba.paristransitace.domain.repository.GameSessionRepository
import com.alajemba.paristransitace.domain.repository.StoryRepository
import com.alajemba.paristransitace.utils.debugLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlin.collections.get

class GameSessionRepositoryImpl(
    private val storyRepository: StoryRepository,
    private val localDataSource: LocalDataSource
) : GameSessionRepository {

    private var _currentStoryLine: StoryLine? = null
    override val currentStoryLine: StoryLine? get() = _currentStoryLine

    private var _scenarios: List<Scenario> = emptyList()
    override val scenarios: List<Scenario> get() = _scenarios

    private val _currentScenario = MutableStateFlow<Scenario?>(null)
    override val currentScenario: StateFlow<Scenario?> = _currentScenario.asStateFlow()

    override val scenarioProgress = _currentScenario.map { current ->
        val total = _scenarios.size
        val nextIndex = (current?.currentIndexInGame ?: -1) + 1
        if (total == 0 || nextIndex < 0) 0f else nextIndex.toFloat() / total
    }

    override val scenarioProgressText = _currentScenario.map { current ->
        val index = current?.currentIndexInGame ?: 0
        val total = _scenarios.size
        "$index/$total"
    }

    override fun setNewSession(storyLine: StoryLine, scenarios: List<Scenario>) {
        _currentStoryLine = storyLine
        _scenarios = scenarios
        clearCurrentScenario()
    }

    override fun updateCurrentStoryLineId(id: Long) {
        _currentStoryLine = _currentStoryLine?.copy(id = id)
    }

    override fun loadStoryForSession(storyId: Long): Boolean {
        val scenarios = storyRepository.loadScenariosForStory(storyId)
        if (scenarios.isEmpty()) return false

        val storyLine = storyRepository.getStory(storyId) ?: return false

        setNewSession(storyLine, scenarios)
        return true
    }

    override fun nextScenario(): Boolean {
        val nextIndex = (currentScenario.value?.currentIndexInGame ?: -1) + 1

        return if (nextIndex < _scenarios.size) {
            _currentScenario.value = _scenarios.getOrNull(nextIndex)?.copy(currentIndexInGame = nextIndex)
            true
        } else {
            false
        }
    }

    override fun clearCurrentScenario() {
        _currentScenario.value = null
    }

    override fun restartCurrentStoryline() {
        if (_scenarios.isNotEmpty()) {
            _currentScenario.value = _scenarios.first().copy(currentIndexInGame = 0)
        } else {
            _currentScenario.value = null
        }
    }

    override fun clearSession() {
        _currentStoryLine = null
        _scenarios = emptyList()
        clearCurrentScenario()
    }

    override fun saveGameState(
        userStats: UserStats,
        currentStoryId: Long
    ) {

        localDataSource.saveGameState(
            storyId = currentStoryId,
            currentScenarioIndex = _currentScenario.value?.currentIndexInGame ?: -1,
            budget = userStats.budget,
            morale = userStats.morale,
            legalInfractionsCount = userStats.legalInfractionsCount
        )
    }

    override fun loadSavedGame(): UserStats? {
        val saved = localDataSource.getSavedGame() ?: return null

        debugLog("Loaded saved game: $saved")

        val storyId = saved.storyId
        if (storyId != null) {
            if (!loadStoryForSession(storyId)) return null
        }

        debugLog("Fast forwarding to scenario index: ${saved.currentScenarioIndex}")
        val targetIndex = saved.currentScenarioIndex.toInt()
        if (targetIndex in _scenarios.indices) {
            _currentScenario.value = _scenarios[targetIndex].copy(currentIndexInGame = targetIndex)
        }

        return UserStats(
            budget = saved.budget,
            morale = saved.morale.toInt(),
            legalInfractionsCount = saved.legalInfractionsCount.toInt()
        )
    }

    override fun deleteSavedGame() {
        localDataSource.deleteSavedGame()
    }

    override fun clearAll() {
        deleteSavedGame()
        clearSession()
        clearCurrentScenario()
    }
}
