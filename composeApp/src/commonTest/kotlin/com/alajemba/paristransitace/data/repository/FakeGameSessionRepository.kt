package com.alajemba.paristransitace.data.repository

import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.StoryLine
import com.alajemba.paristransitace.domain.model.UserStats
import com.alajemba.paristransitace.domain.repository.GameSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of GameSessionRepository for testing.
 * Mimics the behavior of GameSessionRepositoryImpl without needing real data sources.
 */
class FakeGameSessionRepository : GameSessionRepository {

    // Internal state
    private var _currentStoryLine: StoryLine? = null
    override val currentStoryLine: StoryLine? get() = _currentStoryLine

    private var _scenarios: List<Scenario> = emptyList()
    override val scenarios: List<Scenario> get() = _scenarios

    private val _currentScenario = MutableStateFlow<Scenario?>(null)
    override val currentScenario: StateFlow<Scenario?> = _currentScenario.asStateFlow()

    override val scenarioProgress: Flow<Float> = _currentScenario.map { current ->
        val total = _scenarios.size
        val nextIndex = (current?.currentIndexInGame ?: -1) + 1
        if (total == 0 || nextIndex < 0) 0f else nextIndex.toFloat() / total
    }

    override val scenarioProgressText: Flow<String> = _currentScenario.map { current ->
        val index = current?.currentIndexInGame ?: 0
        val total = _scenarios.size
        "$index/$total"
    }

    // Test configuration
    var storyLineForLoading: StoryLine? = null
    var scenariosForLoading: List<Scenario> = emptyList()
    var canLoadStory: Boolean = true

    // For saved game simulation
    var savedUserStats: UserStats? = null
    var savedStoryId: Long? = null
    var savedScenarioIndex: Int = 0

    // Track what was saved
    var lastSavedUserStats: UserStats? = null
    var lastSavedScenarioIndex: Int = -1

    override fun setNewSession(storyLine: StoryLine, scenarios: List<Scenario>) {
        _currentStoryLine = storyLine
        _scenarios = scenarios
        clearCurrentScenario()
    }

    override fun loadStoryForSession(storyId: Long): Boolean {
        if (scenariosForLoading.isEmpty()) return false
        val storyLine = storyLineForLoading ?: return false

        setNewSession(storyLine, scenariosForLoading)
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

    override fun clearSession() {
        _currentStoryLine = null
        _scenarios = emptyList()
        clearCurrentScenario()
    }

    override fun saveGameState(userStats: UserStats,currentStoryId: Long) {
        lastSavedUserStats = userStats
        lastSavedScenarioIndex = _currentScenario.value?.currentIndexInGame ?: -1

        // Also update the "saved" state for load testing
        savedUserStats = userStats
        savedStoryId = _currentStoryLine?.id
        savedScenarioIndex = lastSavedScenarioIndex
    }

    override fun loadSavedGame(): UserStats? {
        val stats = savedUserStats ?: return null
        val storyId = savedStoryId

        if (storyId != null && canLoadStory) {
            // Simulate loading story
            storyLineForLoading?.let { _currentStoryLine = it }
            _scenarios = scenariosForLoading

            // Navigate to saved scenario
            repeat(savedScenarioIndex + 1) {
                nextScenario()
            }
        } else if (storyId != null) {
            return null
        }

        return stats
    }

    override fun deleteSavedGame() {
        savedUserStats = null
        savedStoryId = null
        savedScenarioIndex = 0
        lastSavedUserStats = null
        lastSavedScenarioIndex = -1
    }

    override fun clearAll() {
        deleteSavedGame()
        clearSession()
    }
}

