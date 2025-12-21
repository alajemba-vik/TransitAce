package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.TransitAceSDK
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.model.ChatUiModel
import com.alajemba.paristransitace.ui.model.UIDataState
import com.alajemba.paristransitace.ui.model.GameSetup
import com.alajemba.paristransitace.ui.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class ChatViewModel(private val transitAceSDK: TransitAceSDK) : ViewModel() {
    private val _userStatsState = MutableStateFlow(UserStats())
    val userStatsState = _userStatsState.asStateFlow()

    private val _chatMessages = MutableStateFlow(emptyList<ChatUiModel>())
    val chatMessages = _chatMessages.asStateFlow()

    private val _uiDataState: MutableStateFlow<UIDataState> =  MutableStateFlow(UIDataState.Idle)
    val isLoading = _uiDataState.asStateFlow().map { it is UIDataState.Loading }

    fun setWelcomeMessage(initialMessageFromAI: String) {
        attachNewMessage(initialMessageFromAI, ChatMessageSender.AI)
    }

    fun attachNewMessage(input: String, sender: ChatMessageSender){
        _chatMessages.value += ChatUiModel(sender, input)
    }

    fun setupGame(updatedGameSetup: GameSetup) {

        var chatMessage: String

        when {
            updatedGameSetup.isOnLanguageStep -> chatMessage = "English of Français? Keep it simple, kid"
            updatedGameSetup.isOnNameStep -> chatMessage = when (updatedGameSetup.isEnglish) {
                true -> "Great. English it is. And what is your name you poor soul?"
                false -> "Super. Français it is. Et quel est ton nom, pauvre âme?"
            }
            updatedGameSetup.isOnSelectSimulationStep -> chatMessage = when (updatedGameSetup.isEnglish) {
                true -> "Alright ${updatedGameSetup.name}, last step. Select your simulation type: Default or Custom?"
                false -> "D'accord ${updatedGameSetup.name}, dernière étape. Sélectionnez votre type de simulation : Par défaut ou Personnalisé ?"
            }
            updatedGameSetup.isOnScenariosGenerationStep -> chatMessage = when {
                /*updatedGameSetup.isEnglish && !updatedGameSetup.isCustomSimulation -> """Welcome, ${updatedGameSetup.name}. You just arrived from Tanzania. 20 years old, low confidence, €100 in your pocket. Initializing..."""
                !updatedGameSetup.isEnglish && !updatedGameSetup.isCustomSimulation -> """Bienvenue, ${updatedGameSetup.name}. Vous venez d'arriver de Tanzanie. 20 ans,
                    |peu de confiance, 100 € dans votre poche. Initialisation...
                """.trimMargin()*/
                updatedGameSetup.isEnglish -> "Simulation selected. Generating scenarios..."
                else -> "Simulation sélectionnée. Génération des scénarios..."
            }.also {
                if (updatedGameSetup.isCustomSimulation) {
                    _uiDataState.value = UIDataState.Loading
                }
            }
            updatedGameSetup.isOnScenariosGenerationFailureStep -> chatMessage = when(updatedGameSetup.isEnglish) {
                true -> """Hmmm. That's unusual. I couldn't get things ready for you. Oh well... delays are normal in Paris. 
                    |Should A) I try again, or B) would you prefer to just do the default scenario, little one?""".trimMargin()
                false -> """Hmmm. C'est inhabituel. Je n'ai pas réussi à préparer le terrain. Enfin bon... les retards, c'est la norme à Paris. A) Je réessaie, ou B) tu préfères le scénario par défaut, mon petit?"""
            }.also {
                _uiDataState.value = UIDataState.Idle
            }
            updatedGameSetup.isSetupComplete -> chatMessage = when (updatedGameSetup.isEnglish) {
                true -> """Okay, I am done. Welcome ${updatedGameSetup.name}, to the city of Paris. I wish you well on your journey. You'll need all the luck you can get."""
                false -> """D'accord, j'ai terminé. Bienvenue ${updatedGameSetup.name}, dans la ville de Paris. Je vous souhaite bonne chance pour votre voyage. Vous aurez besoin de toute la chance possible."""
            }.trimMargin()
            else -> chatMessage = when (updatedGameSetup.isEnglish) {
                true -> "I refuse to respond to that, kid"
                false -> "Je refuse de répondre à cela, gamin"
            }
        }

        attachNewMessage(chatMessage, ChatMessageSender.AI)
    }


    fun sendChatMessage(message: String) {
        viewModelScope.launch {
            val response = transitAceSDK.sendChatMessage(message, ChatMessageSender.USER.name)

            if (response.hasError) {
                // TODO()
            }
        }
    }

}
