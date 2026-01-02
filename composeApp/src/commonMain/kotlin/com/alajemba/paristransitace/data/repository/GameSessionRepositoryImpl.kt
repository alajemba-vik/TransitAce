package com.alajemba.paristransitace.data.repository

import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.StoryLine
import com.alajemba.paristransitace.domain.repository.GameSessionRepository
import com.alajemba.paristransitace.domain.repository.StoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class GameSessionRepositoryImpl(
    private val storyRepository: StoryRepository
) : GameSessionRepository {

    private var _currentStoryLine = StoryLine.EMPTY
    override val currentStoryLine: StoryLine = _currentStoryLine

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

    override fun clearSession() {
        _currentStoryLine = StoryLine.EMPTY
        _scenarios = emptyList()
        clearCurrentScenario()
    }
}

