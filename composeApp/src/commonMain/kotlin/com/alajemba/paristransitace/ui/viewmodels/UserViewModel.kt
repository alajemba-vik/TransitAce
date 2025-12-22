package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.TransitAceSDK
import com.alajemba.paristransitace.model.GameSetting
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.model.GameSetup.GameLanguage
import com.alajemba.paristransitace.ui.model.GameSetup
import com.alajemba.paristransitace.ui.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class UserViewModel(private val transitAceSDK: TransitAceSDK) : ViewModel() {
    private val _userStatsState = MutableStateFlow(UserStats())
    val userStatsState = _userStatsState.asStateFlow()

    private val _gameSetupState = MutableStateFlow(GameSetup.EMPTY)
    val gameSetupState = _gameSetupState.asStateFlow()

    init {
        viewModelScope.launch {
            transitAceSDK.getSetting(GameSetting.Language).collect {

                println("Loaded language setting: $it")

                _gameSetupState.value = _gameSetupState.value.copy(
                    language = it
                )
            }

            transitAceSDK.getSetting(GameSetting.PlayerName).collect {
                println("Loaded player name setting: $it")
                _gameSetupState.value = _gameSetupState.value.copy(
                    name = it
                )
            }
        }
    }

    fun setupGame(
        input: String= "",
        scenariosGenerationStatus: GameSetup.ScenarioGenerationStatus = GameSetup.ScenarioGenerationStatus.NOT_STARTED
    ): GameSetup {
        when {
            _gameSetupState.value.isOnLanguageStep -> {
                input.trim().lowercase().let { cleanedInput ->
                    val isEnglish = Regex("^(english|anglais|en|e|eng|engl)\$").matches(cleanedInput)
                    val isFrench = Regex("^(français|francais|french|fr|f|fran)\$").matches(cleanedInput)

                    if (isEnglish || isFrench) {
                        _gameSetupState.value = gameSetupState.value.copy(
                            language = if (isEnglish) GameLanguage.ENGLISH else GameLanguage.FRENCH
                        )
                        transitAceSDK.saveSetting(GameSetting.Language, gameSetupState.value.language.name)
                    }
                }

            }

            _gameSetupState.value.isOnNameStep -> {
                _gameSetupState.value = gameSetupState.value.copy(name = input)
                transitAceSDK.saveSetting(GameSetting.PlayerName, gameSetupState.value.name)
            }

            _gameSetupState.value.isOnSelectSimulationStep -> {
                val cleanedInput = input.trim().lowercase()
                val isDefault = Regex("^(default|d|def)\$").matches(cleanedInput)
                val isCustom = Regex("^(custom|c|cust|personalisé|personnalise|perso)\$").matches(cleanedInput)

                val simulationType = when {
                    isDefault -> GameSetup.SimulationType.DEFAULT
                    isCustom -> GameSetup.SimulationType.CUSTOM
                    else -> GameSetup.SimulationType.UNDEFINED
                }

                if (simulationType != GameSetup.SimulationType.UNDEFINED) {
                    _gameSetupState.value = gameSetupState.value.copy(simulationType = simulationType)
                }

                // Default simulation has already been generated
                /*if (!_gameSetupState.value.isCustomSimulation) {
                    _gameSetupState.value = gameSetupState.value.copy(
                        scenariosGenerationStatus = GameSetup.ScenarioGenerationStatus.SUCCESS
                    )
                }*/
            }

            _gameSetupState.value.isOnScenariosGenerationStep-> {
                _gameSetupState.value = gameSetupState.value.copy(scenariosGenerationStatus = scenariosGenerationStatus)
            }

            _gameSetupState.value.isOnScenariosGenerationFailureStep  -> {
                val cleanedInput = input.trim().lowercase()
                val normalizedInput = cleanedInput.replace(Regex("""\p{Punct}+$"""), "")

                val isTryAgain = Regex("^(try again|try|t|a)$").matches(normalizedInput)
                val isUseDefaultScenario = Regex("^(default scenario|default|d|def|b)$").matches(normalizedInput)

                if (isTryAgain) {
                    _gameSetupState.value = gameSetupState.value.copy(scenariosGenerationStatus = GameSetup.ScenarioGenerationStatus.NOT_STARTED)
                } else if (isUseDefaultScenario) {
                    _gameSetupState.value = gameSetupState.value.copy(
                        simulationType = GameSetup.SimulationType.DEFAULT,
                        scenariosGenerationStatus = GameSetup.ScenarioGenerationStatus.NOT_STARTED
                    )
                }
            }

        }



        return _gameSetupState.value

    }

    fun sendChatMessage(message: String) {
        viewModelScope.launch {
            val response = transitAceSDK.sendChatMessage(message, ChatMessageSender.USER.name)

            if (response.hasError) {
                // TODO()
            }
        }
    }

    fun updateStats(budgetImpact: Double, moraleImpact: Int, increaseLegalInfractionsBy: Int) {
        // Apply impacts additively: positive increases, negative decreases.
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
        transitAceSDK.clearChat()
        resetUserStats()
    }

}
