package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.ChatSDK
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.model.ChatUiModel
import com.alajemba.paristransitace.ui.model.GameLanguage
import com.alajemba.paristransitace.ui.model.GameSetup
import com.alajemba.paristransitace.ui.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ChatViewModel(private val chatSDK: ChatSDK) : ViewModel() {
    private val _userStatsState = MutableStateFlow(UserStats())
    val userStatsState = _userStatsState.asStateFlow()

    private val _chatMessages = MutableStateFlow(emptyList<ChatUiModel>())
    val chatMessages = _chatMessages.asStateFlow()

    private val _alertsCount = MutableStateFlow(0)
    val alertsCount = _alertsCount.asStateFlow()

    /*init {
        viewModelScope.launch {
            chatSDK.getAllChatMessages().collect { chats ->
                _chatMessages.value = chats.map {
                    ChatUiModel(
                        sender = if (it.sender == ChatMessageSender.AI.name) ChatMessageSender.AI else ChatMessageSender.USER,
                        message = it.message
                    )
                }
            }
        }
    }*/

    fun setWelcomeMessage(initialMessageFromAI: String) {
        _chatMessages.value = listOf(ChatUiModel(ChatMessageSender.AI, initialMessageFromAI))
    }

    fun attachNewUserMessage(input: String){
        _chatMessages.value += ChatUiModel(ChatMessageSender.AI, input)
    }

    fun setupGame(
        updatedGameSetup: GameSetup
    ) {

        var chatMessage = ""

        when {
            updatedGameSetup.isOnLanguageStep -> chatMessage = "English of Français? Keep it simple, kid"
            updatedGameSetup.isOnNameStep -> chatMessage = when (updatedGameSetup.isEnglish) {
                true -> "Great. English it is. And what is your name you poor soul?"
                false -> "Super. Français it is. Et quel est ton nom, pauvre âme?"
            }
            updatedGameSetup.isSetupComplete -> chatMessage = when (updatedGameSetup.isEnglish) {
                true -> """Welcome, ${updatedGameSetup.name}. You just arrived from Tanzania. 20 years old, low confidence, €100 in 
                    |your pocket. Initializing...
                """
                false -> """Bienvenue, ${updatedGameSetup.name}. Vous venez d'arriver de Tanzanie. 20 ans, 
                    |peu de confiance, 100 € dans votre poche. Initialisation...
                """
            }.trimMargin()
        }

        addNewAIMessage(chatMessage)
    }

    private fun addNewAIMessage(message: String) {
        _chatMessages.value += ChatUiModel(ChatMessageSender.AI, message)
    }

    fun sendChatMessage(message: String) {
        viewModelScope.launch {
            val response = chatSDK.sendChatMessage(message, ChatMessageSender.USER.name)

            if (response.hasError) {
                // TODO()
            }
        }
    }

}
