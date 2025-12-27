package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.TransitAceSDK
import com.alajemba.paristransitace.network.models.ChatResult
import com.alajemba.paristransitace.network.models.FunctionDeclaration
import com.alajemba.paristransitace.ui.model.ChatMessageAction
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.model.ChatUiModel
import com.alajemba.paristransitace.ui.model.UIDataState
import com.alajemba.paristransitace.ui.model.GameSetup
import com.alajemba.paristransitace.ui.model.StoryLine
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
                        id = -1L,
                        sender = ChatMessageSender.AI,
                        message = "English(E) / Français(F) ?",
                        actions = emptyList()
                    )
                ) + savedChatMessages.map { chatMessageEntity ->
                    ChatUiModel(
                        id = chatMessageEntity.id,
                        sender = when (chatMessageEntity.sender) {
                            ChatMessageSender.AI.name -> ChatMessageSender.AI
                            ChatMessageSender.USER.name -> ChatMessageSender.USER
                            else -> ChatMessageSender.AI
                        },
                        message = chatMessageEntity.message,
                        selectedAction = if (!chatMessageEntity.selectedActionName.isNullOrBlank()) {
                            ChatMessageAction.fromString(chatMessageEntity.selectedActionName)
                        } else {
                            null
                        },
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

    fun attachSystemMessage(input: String) {
        attachChatMessage(input, ChatMessageSender.AI)
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
    }

    fun setupGame(updatedGameSetup: GameSetup, storyLine: StoryLine? = null) {

        var chatMessage = ""
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

            updatedGameSetup.isOnScenariosGenRequirementsStep && updatedGameSetup.isCustomSimulation -> chatMessage = when(updatedGameSetup.isEnglish) {
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
                    |${storyLine?.title}
                    |${storyLine?.description}
                    | 
                    |Do you want to archive them to play another time, or continue to start the game right now? """
                            .trimMargin()

                        else -> """J'ai préparé tes scénarios personnalisés. 
                    |
                    |${storyLine?.title}
                    |${storyLine?.description}
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
        }

        attachChatMessage(chatMessage, ChatMessageSender.AI, actions)
    }

    fun sendInGameMessage(
        message: String,
        gameSetup: GameSetup,
        gameContext: String? = null
    ) {
        viewModelScope.launch {
            val result = transitAceSDK.sendUserChatMessage(message, ChatMessageSender.USER.name, !gameSetup.isEnglish, gameContext = gameContext)
            val isFrench = !gameSetup.isEnglish

            when(result.data){
                is ChatResult.ExecuteCommand -> {
                    val command = result.data.command
                    val aiResponseMsg = if (!isFrench) "> EXECUTING PROTOCOL:" else
                        "> EXÉCUTION DU PROTOCOLE :"

                    // Show a "System Log" so the user sees something happened
                    transitAceSDK.insertChatMessage(
                        aiResponseMsg + command.uppercase(),
                        ChatMessageSender.AI.name
                    )

                    // Do the actual work
                    when (command) {
                        FunctionDeclaration.DECL_GET_ALL_STORYLINES -> {
                            val stories = transitAceSDK.getAllStories().joinToString("\n") { "- ${it.title}" }
                            var responseMessage = ""

                            responseMessage = if (stories.isEmpty()) {
                                if (!isFrench) {
                                    "No stories available at the moment."
                                } else {
                                    "Aucune histoire disponible pour le moment."
                                }
                            } else {
                                if (!isFrench) {
                                    "AVAILABLE FILES:\n$stories"
                                } else {
                                    "FICHIERS DISPONIBLES:\n$stories"
                                }
                            }

                            attachChatMessage(
                                responseMessage,
                                ChatMessageSender.AI
                            )
                        }

                        FunctionDeclaration.DECL_SHOW_HELP -> {
                            attachChatMessage(
                                (if (!isFrench) {
                                    """- "Show me all storylines"
                                    |- "Load [name] scenario"
                                    |- "Reset this level"
                                    |- "Help"
                                    """.trimMargin()
                                } else {
                                    """- "Montre-moi toutes les intrigues"
                                    |- "Charge le scénario [nom]"
                                    |- "Réinitialise ce niveau"
                                    |- "Aide"
                                    """.trimMargin()
                                }).trimIndent(),
                                ChatMessageSender.AI
                            )
                        }

                        else -> {
                            _uiDataState.value = UIDataState.Success.ChatResponse(
                                command = result.data,
                                sentMessage = message
                            )
                        }
                    }
                }

                else -> {}
            }
        }
    }

    fun updateGameMessageWithAction(selectedActionName: String, messageIdToUpdate: Long) {
        viewModelScope.launch {
            transitAceSDK.updateChatMessage(selectedActionName, messageIdToUpdate)
        }
    }

    fun clearAllChats() {
        transitAceSDK.clearChat()
    }

}
