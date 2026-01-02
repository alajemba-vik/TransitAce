package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.data.local.DefaultScenariosProvider
import com.alajemba.paristransitace.domain.model.GameLanguage
import com.alajemba.paristransitace.domain.model.GameReport
import com.alajemba.paristransitace.domain.model.GameSetup
import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.StoryLine
import com.alajemba.paristransitace.domain.repository.GameSessionRepository
import com.alajemba.paristransitace.domain.repository.StoryRepository
import com.alajemba.paristransitace.domain.usecase.game.CalculateFinalGradeUseCase
import com.alajemba.paristransitace.ui.model.UIDataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class GameViewModel(
    private val calculateFinalGrade: CalculateFinalGradeUseCase,
    private val defaultScenariosProvider: DefaultScenariosProvider,
    private val storyRepository: StoryRepository,
    private val gameSessionRepository: GameSessionRepository
) : ViewModel() {

    private val _uiDataState = MutableStateFlow<UIDataState>(UIDataState.Loading)
    val gameDataState = _uiDataState.asStateFlow()

    val storyLine: StoryLine get() = gameSessionRepository.currentStoryLine
    val currentScenario: StateFlow<Scenario?> = gameSessionRepository.currentScenario
    val scenarioProgress: Flow<Float> = gameSessionRepository.scenarioProgress.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        0f)
    val scenarioProgressText: Flow<String> = gameSessionRepository.scenarioProgressText.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ""
    )

    private val _gameReport = MutableStateFlow(GameReport.EMPTY)
    val gameReport = _gameReport.asStateFlow()

    fun getGameContext(): String {
        if (storyLine.title.isBlank()) return ""

        return """
            Context title: ${storyLine.title} 
            Context description: ${storyLine.description}
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
                    gameSessionRepository.setNewSession(wrapper.storyLine, wrapper.scenarios)
                    _uiDataState.value = UIDataState.Success.ScenariosGenerated
                }.onFailure {
                    gameSessionRepository.clearSession()
                    _uiDataState.value = UIDataState.Error.AIError
                }
            } else {
                val defaultScenarios = defaultScenariosProvider.getDefaultScenarios(!gameSetup.isEnglish)
                gameSessionRepository.setNewSession(StoryLine.EMPTY, defaultScenarios)
                _uiDataState.value = UIDataState.Success.ScenariosGenerated
            }
        }
    }

    fun startGame() {
        clearUIState()
        gameSessionRepository.clearCurrentScenario()
        nextScenario()
    }

    fun nextScenario(): Boolean {
        return gameSessionRepository.nextScenario()
    }

    fun calculateGameFinalGrade(
        budgetRemaining: Double,
        moraleRemaining: Int,
        legalInfractionsCount: Int
    ) {
        _gameReport.value = calculateFinalGrade(budgetRemaining, moraleRemaining, legalInfractionsCount)
    }

    fun saveCurrentStoryLine() {
        val scenarios = gameSessionRepository.scenarios
        if (storyLine != StoryLine.EMPTY && scenarios.isNotEmpty()) {
            storyRepository.saveStoryLine(storyLine, scenarios)
        }
    }
}