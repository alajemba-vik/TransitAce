package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.domain.model.GameLanguage
import com.alajemba.paristransitace.domain.model.GameSetup
import com.alajemba.paristransitace.domain.model.ScenarioGenerationStatus
import com.alajemba.paristransitace.domain.model.SimulationType
import com.alajemba.paristransitace.domain.model.UserStats
import com.alajemba.paristransitace.domain.repository.SettingsRepository
import com.alajemba.paristransitace.domain.usecase.app.ClearAppStateUseCase
import com.alajemba.paristransitace.ui.navigation.GameRoute
import com.alajemba.paristransitace.ui.navigation.HomeRoute
import com.alajemba.paristransitace.ui.navigation.LandingRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class UserViewModel(
    private val settingsRepository: SettingsRepository,
    private val clearAppStateUseCase: ClearAppStateUseCase
) : ViewModel() {

    private val _userStatsState = MutableStateFlow(UserStats())
    val userStatsState = _userStatsState.asStateFlow()

    private val _gameSetupState = MutableStateFlow(GameSetup.EMPTY)
    val gameSetupState = _gameSetupState.asStateFlow()

    private val _lastSessionCheckpoint = MutableStateFlow<String?>(null)
    val lastSessionCheckpoint = _lastSessionCheckpoint.asStateFlow()

    var gameSetupPlot = ""
        private set

    init {
        loadSavedSettings()
    }

    private fun loadSavedSettings() {
        viewModelScope.launch {
            _lastSessionCheckpoint.value = settingsRepository.lastSessionCheckpoint()
        }

        viewModelScope.launch {
            settingsRepository.getLanguage().collect{ language ->
                _gameSetupState.value = _gameSetupState.value.copy(
                    language = language ?: GameLanguage.UNDEFINED
                )
            }
        }
        viewModelScope.launch {
            settingsRepository.getPlayerName()?.let { name ->
                _gameSetupState.value = _gameSetupState.value.copy(
                    name = name
                )
            }
        }
    }

    fun setupGame(
        input: String = "",
        scenariosGenerationStatus: ScenarioGenerationStatus = ScenarioGenerationStatus.NOT_STARTED
    ): GameSetup {
        when {
            _gameSetupState.value.isOnLanguageStep -> handleLanguageStep(input)
            _gameSetupState.value.isOnNameStep -> handleNameStep(input)
            _gameSetupState.value.isOnSelectSimulationStep -> handleSimulationTypeStep(input)
            _gameSetupState.value.isOnScenariosGenerationStep -> handleScenariosGenerationStep(scenariosGenerationStatus)
            _gameSetupState.value.isOnScenariosGenRequirementsStep -> handleScenariosRequirementsStep(input)
            _gameSetupState.value.isOnScenariosGenerationFailureStep -> handleFailureStep(input)
            _gameSetupState.value.isOnScenariosGenerationSuccessStep -> handleSuccessStep(scenariosGenerationStatus)
        }

        return _gameSetupState.value
    }

    private fun handleLanguageStep(input: String) {
        val cleanedInput = input.trim().lowercase()
        val isEnglish = Regex("^(english|anglais|en|e|eng|engl)$").matches(cleanedInput)
        val isFrench = Regex("^(français|francais|french|fr|f|fran)$").matches(cleanedInput)

        if (isEnglish || isFrench) {
            val language = if (isEnglish) GameLanguage.ENGLISH else GameLanguage.FRENCH
            _gameSetupState.value = _gameSetupState.value.copy(language = language)
            settingsRepository.saveLanguage(language)
        }
    }

    private fun handleNameStep(input: String) {
        _gameSetupState.value = _gameSetupState.value.copy(name = input)
        settingsRepository.savePlayerName(input)
    }

    private fun handleSimulationTypeStep(input: String) {
        val cleanedInput = input.trim().lowercase()
        val isDefault = Regex("^(default|d|def)$").matches(cleanedInput)
        val isCustom = Regex("^(custom|c|cust|personalisé|personnalise|perso)$").matches(cleanedInput)

        val simulationType = when {
            isDefault -> SimulationType.DEFAULT
            isCustom -> SimulationType.CUSTOM
            else -> SimulationType.UNDEFINED
        }

        if (simulationType != SimulationType.UNDEFINED) {
            _gameSetupState.value = _gameSetupState.value.copy(
                simulationType = simulationType,
                scenariosGenerationStatus = if (isCustom) ScenarioGenerationStatus.NOT_STARTED else ScenarioGenerationStatus.PROCESSING
            )
        }
    }

    private fun handleScenariosGenerationStep(status: ScenarioGenerationStatus) {
        _gameSetupState.value = _gameSetupState.value.copy(scenariosGenerationStatus = status)
    }

    private fun handleScenariosRequirementsStep(input: String) {
        if (input.isNotBlank()) {
            gameSetupPlot = input
            _gameSetupState.value = _gameSetupState.value.copy(
                scenariosGenerationStatus = ScenarioGenerationStatus.PROCESSING
            )
        }
    }

    private fun handleFailureStep(input: String) {
        val cleanedInput = input.trim().lowercase().replace(Regex("""\p{Punct}+$"""), "")
        val isTryAgain = Regex("^(try again|try|t|a)$").matches(cleanedInput)
        val isUseDefault = Regex("^(default scenario|default|d|def|b)$").matches(cleanedInput)

        when {
            isTryAgain -> _gameSetupState.value = _gameSetupState.value.copy(
                scenariosGenerationStatus = ScenarioGenerationStatus.NOT_STARTED
            )
            isUseDefault -> _gameSetupState.value = _gameSetupState.value.copy(
                simulationType = SimulationType.DEFAULT,
                scenariosGenerationStatus = ScenarioGenerationStatus.NOT_STARTED
            )
        }
    }

    private fun handleSuccessStep(status: ScenarioGenerationStatus) {
        _gameSetupState.value = _gameSetupState.value.copy(scenariosGenerationStatus = status)
    }

    fun updateStats(budgetImpact: Double, moraleImpact: Int, increaseLegalInfractionsBy: Int) {
        _userStatsState.value = _userStatsState.value.copy(
            budget = _userStatsState.value.budget + budgetImpact,
            morale = _userStatsState.value.morale + moraleImpact,
            legalInfractionsCount = _userStatsState.value.legalInfractionsCount + increaseLegalInfractionsBy
        )
    }

    fun resetUserStats() {
        _userStatsState.value = UserStats()
    }

    fun clearAllInfo() {
        clearAppStateUseCase()
        resetUserStats()
        _gameSetupState.value = GameSetup.EMPTY
    }

    fun setHasSeenLandingScreen() {
        settingsRepository.saveSessionCheckpoint(LandingRoute.label)
        _lastSessionCheckpoint.value = LandingRoute.label
    }

    fun wasOnHomeScreen() {
        settingsRepository.saveSessionCheckpoint(HomeRoute.label)
        _lastSessionCheckpoint.value = HomeRoute.label
    }

    fun wasOnGameScreen() {
        settingsRepository.saveSessionCheckpoint(GameRoute.label)
        _lastSessionCheckpoint.value = GameRoute.label
    }

    fun setDeviceLanguage(language: GameLanguage) {
        _gameSetupState.value = _gameSetupState.value.copy(deviceLanguage = language)
    }

    fun setLastSessionCheckpoint() {
        TODO("Not yet implemented")
    }
}