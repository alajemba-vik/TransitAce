package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.TransitAceSDK
import com.alajemba.paristransitace.ui.model.ChatMessageAction
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

    private val _uiDataState: MutableStateFlow<UIDataState> = MutableStateFlow(UIDataState.Idle)
    val isLoading = _uiDataState.asStateFlow().map { it is UIDataState.Loading }

    init {
        viewModelScope.launch {
            transitAceSDK.getAllChatMessages().collect { savedChatMessages ->
                _chatMessages.value = listOf(
                    ChatUiModel(
                        sender = ChatMessageSender.AI,
                        message = "English(E) / Français(F) ?",
                        actions = emptyList()
                    )
                ) + savedChatMessages.map { chatMessageEntity ->
                    ChatUiModel(
                        sender = when (chatMessageEntity.sender) {
                            ChatMessageSender.AI.name -> ChatMessageSender.AI
                            ChatMessageSender.USER.name -> ChatMessageSender.USER
                            else -> ChatMessageSender.AI
                        },
                        message = chatMessageEntity.message,
                        selectedActionName = chatMessageEntity.selectedActionName,
                        actions = chatMessageEntity.actions?.mapNotNull { ChatMessageAction.fromString(it) }
                            ?: emptyList()
                    )
                }

            }
        }

    }

    fun attachUserNewMessage(input: String) {
        attachChatMessage(input, ChatMessageSender.USER)
    }

    private fun attachChatMessage(
        input: String,
        sender: ChatMessageSender,
        actions: List<ChatMessageAction> = emptyList()
    ) {
        if (input.isBlank()) return
        transitAceSDK.insertChatMessage(
            input,
            sender.name,
            actions
        )
        /*_chatMessages.value += ChatUiModel(
            sender,
            input,
            actions
        )*/
    }

    fun setupGame(updatedGameSetup: GameSetup) {

        var chatMessage: String
        val actions = mutableListOf<ChatMessageAction>()

        when {
            updatedGameSetup.isOnLanguageStep -> chatMessage = "English or Français? Keep it simple, kid"
            updatedGameSetup.isOnNameStep -> chatMessage = when (updatedGameSetup.isEnglish) {
                true -> "Connection established. Welcome to Transit Ace.\n\n" +
                        "This is a survival simulation. I will generate random crisis scenarios in the Paris Metro—from strikes to supernatural events.\n" +
                        "Your Goal: Make the right choices to protect your Money, Sanity, and Reputation.\n" +
                        "What is your name you poor soul?"

                false -> "Connexion établie. Bienvenue sur Transit Ace.\n\n" +
                        "Ceci est une simulation de survie. Je générerai des scénarios de crise aléatoires dans le métro—des grèves aux événements surnaturels.\n" +
                        "Votre But : Faire les bons choix pour protéger votre Argent, votre Santé Mentale et votre Réputation.\n" +
                        "Quel est ton nom, pauvre âme?"
            }

            updatedGameSetup.isOnSelectSimulationStep -> chatMessage = when (updatedGameSetup.isEnglish) {
                true -> "Alright ${updatedGameSetup.name}. Select your simulation type: Default or Custom?"
                false -> "D'accord ${updatedGameSetup.name}. Sélectionnez votre type de simulation : Par défaut ou Personnalisé ?"
            }

            updatedGameSetup.isOnScenariosGenRequirementsStep -> chatMessage = when(updatedGameSetup.isEnglish) {
                true -> """So, what is the situation? 
                |
                |Briefly describe the plot or the incident for your custom scenario. We'll generate your scenarios based on your story.""".trimMargin()

                false -> """Alors, quelle est la situation ?
                |
                |Décris brièvement l'intrigue ou l'incident pour ton scénario personnalisé. Nous générerons tes scénarios à partir de ton histoire.""".trimMargin()
            }

            updatedGameSetup.isOnScenariosGenerationStep && updatedGameSetup.isCustomSimulation -> chatMessage = when {
                updatedGameSetup.isEnglish -> "Simulation selected. Generating scenarios..."
                else -> "Simulation sélectionnée. Génération des scénarios..."
            }.also {
                _uiDataState.value = UIDataState.Loading
            }

            updatedGameSetup.isOnScenariosGenerationSuccessStep && updatedGameSetup.isCustomSimulation -> {
                chatMessage = when {
                        updatedGameSetup.isEnglish -> """I've prepared your custom scenarios. 
                    |
                    |Do you want to archive them to play another time, or continue to start the game right now? """
                            .trimMargin()

                        else -> """J'ai préparé tes scénarios personnalisés. 
                    |
                    |Tu veux les **Archiver** pour y jouer une autre fois, ou **Continuer** pour lancer le jeu maintenant ? """
                            .trimMargin()
                    }
                actions.addAll(listOf(ChatMessageAction.PLAY_SCENARIO, ChatMessageAction.SAVE_SCENARIO))
            }

            updatedGameSetup.isOnScenariosGenerationFailureStep -> chatMessage = when (updatedGameSetup.isEnglish) {
                true -> """Hmmm. That's unusual. I couldn't get things ready for you. Oh well... delays are normal in Paris. 
                    |Should A) I try again, or B) would you prefer to just do the default scenario, little one?""".trimMargin()

                false -> """Hmmm. C'est inhabituel. Je n'ai pas réussi à préparer le terrain. Enfin bon... les retards, c'est la norme à Paris. A) Je réessaie, ou B) tu préfères le scénario par défaut, mon petit?"""
            }.also {
                _uiDataState.value = UIDataState.Idle
            }

            updatedGameSetup.isSetupComplete -> chatMessage = when {
                updatedGameSetup.isEnglish && !updatedGameSetup.isCustomSimulation -> """Welcome, ${updatedGameSetup.name}. You just arrived to France. 20 years old, low confidence, little funds. Initializing..."""
                !updatedGameSetup.isEnglish && !updatedGameSetup.isCustomSimulation -> """Bienvenue, ${updatedGameSetup.name}. Vous venez d'arriver en France. 20 ans,
                    |peu de confiance, peu d'argent. Initialisation...
                """.trimMargin()

                else -> {
                    if (updatedGameSetup.isEnglish) {
                        """Okay, I am done. Welcome ${updatedGameSetup.name}, to the city of Paris. I wish you well on your journey. You'll need all the luck you can get."""
                    } else {
                        """D'accord, j'ai terminé. Bienvenue ${updatedGameSetup.name}, dans la ville de Paris. Je vous souhaite bonne chance pour votre voyage. Vous aurez besoin de toute la chance possible."""
                    }
                }
            }.trimMargin()

            else -> chatMessage = when (updatedGameSetup.isEnglish) {
                true -> "I refuse to respond to that, kid"
                false -> "Je refuse de répondre à cela, gamin"
            }
        }

        attachChatMessage(chatMessage, ChatMessageSender.AI, actions)
    }


    fun sendChatMessage(message: String) {
        viewModelScope.launch {
            val response = transitAceSDK.sendChatMessage(message, ChatMessageSender.USER.name)

            if (response.hasError) {
                // TODO()
            }
        }
    }

    fun clearAllChats() {
        transitAceSDK.clearChat()
    }

}
