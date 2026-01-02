package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.data.local.DefaultScenariosProvider
import com.alajemba.paristransitace.domain.model.GameLanguage
import com.alajemba.paristransitace.domain.model.GameReport
import com.alajemba.paristransitace.domain.model.GameSetup
import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.StoryLine
import com.alajemba.paristransitace.domain.repository.StoryRepository
import com.alajemba.paristransitace.domain.usecase.game.CalculateFinalGradeUseCase
import com.alajemba.paristransitace.ui.model.UIDataState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class GameViewModel(
    private val calculateFinalGrade: CalculateFinalGradeUseCase,
    private val defaultScenariosProvider: DefaultScenariosProvider,
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _uiDataState = MutableStateFlow<UIDataState>(UIDataState.Loading)
    val gameDataState = _uiDataState.asStateFlow()

    private var _currentStoryLine = StoryLine.EMPTY
    val storyLine: StoryLine
        get() = _currentStoryLine

    private val _scenariosState = MutableStateFlow(emptyList<Scenario>())

    private val _currentScenario = MutableStateFlow<Scenario?>(null)
    val currentScenario = _currentScenario.asStateFlow()

    private val _scenarioProgress = MutableStateFlow(0f)
    val scenarioProgress = _scenarioProgress.asStateFlow()

    val scenarioProgressText = _currentScenario.asStateFlow().map { current ->
        val index = current?.currentIndexInGame ?: 0
        val total = _scenariosState.value.size
        "$index/$total"
    }

    private val _gameReport = MutableStateFlow(GameReport.EMPTY)
    val gameReport = _gameReport.asStateFlow()

    private var lastUsedTransitRulesJson: String? = null

    fun getGameContext(): String {
        if (_currentStoryLine.title.isBlank()) return ""
        return """
            Context title: ${_currentStoryLine.title} 
            Context description: ${_currentStoryLine.description}
        """.trimIndent()
    }

    fun clearUIState() {
        _uiDataState.value = UIDataState.Idle
    }

    fun generateGameScenarios(
        gameSetup: GameSetup,
        transitRulesJson: String? = null,
        plot: String
    ) {
        _uiDataState.value = UIDataState.Loading
        lastUsedTransitRulesJson = transitRulesJson ?: lastUsedTransitRulesJson

        val language = if (gameSetup.isEnglish) GameLanguage.ENGLISH else GameLanguage.FRENCH
        val isCustom = gameSetup.isCustomSimulation

        viewModelScope.launch {
            if (isCustom) {
                val result = storyRepository.generateScenarios(
                    transitRulesJson = transitRulesJson ?: "",
                    language = language,
                    plot = plot
                )

                result.onSuccess { wrapper ->
                    _scenariosState.value = wrapper.scenarios
                    _currentStoryLine = wrapper.storyLine
                    _uiDataState.value = UIDataState.Success.ScenariosGenerated
                }.onFailure {
                    _scenariosState.value = emptyList()
                    _uiDataState.value = UIDataState.Error.AIError
                }
            } else {
                _scenariosState.value = defaultScenariosProvider.getDefaultScenarios(!gameSetup.isEnglish)
                _uiDataState.value = UIDataState.Success.ScenariosGenerated
            }
        }
    }

    fun startGame() {
        clearUIState()
        _currentScenario.value = null
        _scenarioProgress.value = 0f
        nextScenario()
    }

    fun nextScenario(): Boolean {
        val currentIndex = _currentScenario.value?.currentIndexInGame ?: -1
        val nextIndex = currentIndex + 1

        val nextScenario = _scenariosState.value.getOrNull(nextIndex)?.copy(
            currentIndexInGame = nextIndex
        )

        return if (nextScenario != null) {
            _currentScenario.value = nextScenario
            _scenarioProgress.value = (nextIndex.toFloat() + 1) / _scenariosState.value.size
            true
        } else {
            false
        }
    }

    fun calculateGameFinalGrade(
        budgetRemaining: Double,
        moraleRemaining: Int,
        legalInfractionsCount: Int
    ) {
        _gameReport.value = calculateFinalGrade(budgetRemaining, moraleRemaining, legalInfractionsCount)
    }

    fun saveCurrentStoryLine() {
        if (_currentStoryLine != StoryLine.EMPTY && _scenariosState.value.isNotEmpty()) {
            storyRepository.saveStoryLine(_currentStoryLine, _scenariosState.value)
        }
    }

    fun loadStory(storyId: Long) {
        val scenarios = storyRepository.loadScenariosForStory(storyId)
        if (scenarios.isNotEmpty()) {
            _scenariosState.value = scenarios
        }
    }
}