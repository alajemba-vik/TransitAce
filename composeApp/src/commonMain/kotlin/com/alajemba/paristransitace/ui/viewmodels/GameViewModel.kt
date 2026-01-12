package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.data.local.DefaultScenariosProvider
import com.alajemba.paristransitace.domain.model.GameLanguage
import com.alajemba.paristransitace.domain.model.GameReport
import com.alajemba.paristransitace.domain.model.GameSetup
import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.StoryLine
import com.alajemba.paristransitace.domain.model.UserStats
import com.alajemba.paristransitace.domain.repository.GameSessionRepository
import com.alajemba.paristransitace.domain.repository.StoryRepository
import com.alajemba.paristransitace.domain.usecase.game.CalculateFinalGradeUseCase
import com.alajemba.paristransitace.ui.model.UIDataState
import com.alajemba.paristransitace.utils.debugLog
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

    private val _userStatsState = MutableStateFlow(UserStats.EMPTY)
    val userStatsState = _userStatsState.asStateFlow()

    private val _uiDataState = MutableStateFlow<UIDataState>(UIDataState.Loading)
    val gameDataState = _uiDataState.asStateFlow()

    val storyLine: StoryLine? get() = gameSessionRepository.currentStoryLine
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

    init {
        viewModelScope.launch {
            gameSessionRepository.loadSavedGame()?.let { savedStats ->
                _userStatsState.value = savedStats
                _uiDataState.value = UIDataState.Success.ScenariosGenerated
            }
        }
    }

    fun updateStats(budgetImpact: Double, moraleImpact: Int, increaseLegalInfractionsBy: Int) {
        _userStatsState.value = _userStatsState.value.copy(
            budget = _userStatsState.value.budget + budgetImpact,
            morale = _userStatsState.value.morale + moraleImpact,
            legalInfractionsCount = _userStatsState.value.legalInfractionsCount + increaseLegalInfractionsBy
        )
    }

    fun resetUserStats(storyLine: StoryLine) {
        _userStatsState.value = UserStats(
            budget = storyLine.initialBudget,
            morale = storyLine.initialMorale,
            legalInfractionsCount = 0
        )
    }

    fun getGameContext(): String {
        if (storyLine?.title.isNullOrBlank()) return ""

        return """
            Context title: ${storyLine?.title} 
            Context description: ${storyLine?.description}
        """.trimIndent()
    }

    fun clearUIState() {
        _uiDataState.value = UIDataState.Idle
        resetUserStats(storyLine ?: return)
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
                }.onFailure { exception ->
                    gameSessionRepository.clearSession()
                    val errorMessage = extractErrorMessage(exception)
                    _uiDataState.value = UIDataState.Error.AIError(errorMessage)
                }
            } else {
                val defaultScenarios = defaultScenariosProvider.getDefaultScenarios(!gameSetup.isEnglish)
                val storyLine = defaultScenariosProvider.getDefaultStoryLine()
                gameSessionRepository.setNewSession(storyLine, defaultScenarios)
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
    fun endIfGameOver(): Boolean {
        val stats = _userStatsState.value

        // Check game over conditions
        if (stats.budget < 0 || stats.morale <= 0 || stats.legalInfractionsCount >= 3) {
            onGameOver(stats)
            return true // Game over
        }

        return false
    }

    fun calculateGameFinalGrade(
        budgetRemaining: Double,
        moraleRemaining: Int,
        legalInfractionsCount: Int
    ) {
        val initialBudget = storyLine?.initialBudget ?: 0.0
        val initialMorale = storyLine?.initialMorale ?: 0

        _gameReport.value = calculateFinalGrade(
            budgetRemaining = budgetRemaining,
            moraleRemaining = moraleRemaining,
            initialMorale = initialMorale,
            initialBudget = initialBudget,
            legalInfractionsCount = legalInfractionsCount
        )
    }

    fun saveCurrentStoryLine() {
        debugLog("Saving current story line with id '${storyLine?.id}'")
        val scenarios = gameSessionRepository.scenarios
        if (scenarios.isNotEmpty()) {
            val savedId = storyRepository.saveStoryLine(storyLine ?: return, scenarios)
            // Update the story line ID in the session if it was null (new story)
            if (storyLine?.id == null) {
                gameSessionRepository.updateCurrentStoryLineId(savedId)
            }
        }
    }

    fun saveGameState(userStats: UserStats) {
        saveCurrentStoryLine()
        gameSessionRepository.saveGameState(
            userStats,
            storyLine?.id ?: return
        )
    }

    fun deleteSavedGame() {
        gameSessionRepository.deleteSavedGame()
    }

    fun onGameOver(userStatsState: UserStats) {
        calculateGameFinalGrade(
            budgetRemaining = userStatsState.budget,
            moraleRemaining = userStatsState.morale,
            legalInfractionsCount = userStatsState.legalInfractionsCount
        )
    }

    fun onRestart() {
        startGame()
        resetUserStats(storyLine ?: return)
    }

    private fun extractErrorMessage(exception: Throwable): String {
        val rawMessage = exception.message ?: return ""

        // Try to find JSON error object in the message
        val errorPatterns = listOf(
            // Matches: "message": "..."
            """"message"\s*:\s*"([^"]+)"""".toRegex(),
            // Matches: "error": { ... "message": "..." }
            """"error"\s*:\s*\{[^}]*"message"\s*:\s*"([^"]+)"""".toRegex(),
        )

        for (pattern in errorPatterns) {
            pattern.find(rawMessage)?.groupValues?.getOrNull(1)?.let { message ->
                if (message.isNotBlank()) return message
            }
        }
        return ""
    }
}