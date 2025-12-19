package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.ChatSDK
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.model.GameLanguage
import com.alajemba.paristransitace.ui.model.GameSetup
import com.alajemba.paristransitace.ui.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class UserViewModel(private val chatSDK: ChatSDK) : ViewModel() {
    private val _userStatsState = MutableStateFlow(UserStats())
    val userStatsState = _userStatsState.asStateFlow()

    private val _gameSetupState =  MutableStateFlow(GameSetup.EMPTY)
    val gameSetupState = _gameSetupState.asStateFlow()

    fun setupGame(input: String): GameSetup {
        when {
             _gameSetupState.value.language == GameLanguage.UNDEFINED -> {
                 input.trim().lowercase().let { cleanedInput ->
                     val isEnglish = cleanedInput == "english"
                     val isFrench = cleanedInput == "french"

                     if (isEnglish || isFrench) {
                         _gameSetupState.value =
                             gameSetupState.value.copy(language = if (isEnglish) GameLanguage.ENGLISH else GameLanguage.FRENCH)
                     }
                 }

             }
            else -> {
                _gameSetupState.value = gameSetupState.value.copy(name = input)
            }
        }

        return _gameSetupState.value

    }

    fun sendChatMessage(message: String) {
        viewModelScope.launch {
            val response = chatSDK.sendChatMessage(message, ChatMessageSender.USER.name)

            if (response.hasError) {
                // TODO()
            }
        }
    }

    fun updateStats(cost: Double, moraleImpact: Int) {
        _userStatsState.value = _userStatsState.value.copy(
            budget = _userStatsState.value.budget - cost,
            morale = _userStatsState.value.morale - moraleImpact
        )
    }

}
