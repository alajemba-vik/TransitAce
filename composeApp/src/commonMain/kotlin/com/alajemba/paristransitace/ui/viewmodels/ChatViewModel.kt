package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.domain.model.GameSetup
import com.alajemba.paristransitace.domain.model.MessageSender
import com.alajemba.paristransitace.domain.model.StoryLine
import com.alajemba.paristransitace.domain.repository.ChatAIResponse
import com.alajemba.paristransitace.domain.usecase.chat.InsertChatMessageUseCase
import com.alajemba.paristransitace.domain.usecase.chat.SendChatMessageUseCase
import com.alajemba.paristransitace.data.remote.model.FunctionDeclaration
import com.alajemba.paristransitace.domain.repository.ChatRepository
import com.alajemba.paristransitace.domain.repository.GameSessionRepository
import com.alajemba.paristransitace.domain.repository.StoryRepository
import com.alajemba.paristransitace.ui.mapper.toChatUiModel
import com.alajemba.paristransitace.ui.model.ChatMessageAction
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.model.ChatUiModel
import com.alajemba.paristransitace.ui.model.UIDataState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class ChatViewModel(
    private val insertChatMessage: InsertChatMessageUseCase,
    private val sendChatMessage: SendChatMessageUseCase,
    private val storyRepository: StoryRepository,
    private val chatRepository: ChatRepository,
    private val gameSessionRepository: GameSessionRepository
) : ViewModel() {

    private val _chatMessages = MutableStateFlow(emptyList<ChatUiModel>())
    val chatMessages = _chatMessages.asStateFlow()

    private val _uiDataState: MutableStateFlow<UIDataState> = MutableStateFlow(UIDataState.Idle)
    val isLoading = _uiDataState.asStateFlow().map { it is UIDataState.Loading }

    private val initialMessage = ChatUiModel(
        id = -1L,
        sender = ChatMessageSender.AI,
        message = "English(E) / Français(F) ?",
        actions = emptyList()
    )

    init {
        observeChatMessages()
    }

    private fun observeChatMessages() {
        viewModelScope.launch {
            chatRepository.getAllMessages().collect { savedChatMessages ->
                _chatMessages.value = listOf(initialMessage) + savedChatMessages.map { it.toChatUiModel() }
            }
        }
    }

    fun attachUserNewMessage(input: String) {
        attachChatMessage(input, MessageSender.USER)
    }

    fun attachSystemMessage(input: String) {
        attachChatMessage(input, MessageSender.AI)
    }

    private fun attachChatMessage(
        input: String,
        sender: MessageSender,
        actions: List<String> = emptyList()
    ) {
        if (input.isBlank()) return
        insertChatMessage(input, sender, actions)
    }

    fun setupGame(updatedGameSetup: GameSetup, storyLine: StoryLine? = null) {
        val isEnglish = updatedGameSetup.isEnglish
        val actions = mutableListOf<String>()

        val chatMessage = when {
            updatedGameSetup.isOnLanguageStep -> if (isEnglish) "English or Français? Keep it simple, kid"
            else "Anglais ou Français? Fais simple, gamin"

            updatedGameSetup.isOnNameStep -> buildNameStepMessage(isEnglish)

            updatedGameSetup.isOnSelectSimulationStep -> buildSimulationSelectMessage(isEnglish, updatedGameSetup.name)

            updatedGameSetup.isOnScenariosGenRequirementsStep && updatedGameSetup.isCustomSimulation ->
                buildScenariosRequirementsMessage(isEnglish)

            updatedGameSetup.isOnScenariosGenerationStep && updatedGameSetup.isCustomSimulation -> {
                _uiDataState.value = UIDataState.Loading
                if (isEnglish) "Simulation selected. Generating scenarios..."
                else "Simulation sélectionnée. Génération des scénarios..."
            }

            updatedGameSetup.isOnScenariosGenerationSuccessStep && updatedGameSetup.isCustomSimulation -> {
                actions.addAll(listOf(ChatMessageAction.PLAY_SCENARIO.name, ChatMessageAction.SAVE_SCENARIO.name))
                buildScenariosSuccessMessage(isEnglish, storyLine)
            }

            updatedGameSetup.isOnScenariosGenerationFailureStep -> {
                _uiDataState.value = UIDataState.Idle
                buildScenariosFailureMessage(isEnglish)
            }

            updatedGameSetup.isSetupComplete -> buildSetupCompleteMessage(updatedGameSetup)

            else -> ""
        }

        if (chatMessage.isNotBlank()) {
            attachChatMessage(chatMessage, MessageSender.AI, actions)
        }
    }

    private fun buildNameStepMessage(isEnglish: Boolean) = if (isEnglish) {
        """English it is! Connection established. Welcome to Transit Ace.
            |
            |This is a survival simulation. I will generate random crisis scenarios in the Paris Metro—from strikes to supernatural events.
            |Your Goal: Make the right choices to protect your Money, Sanity, and Reputation.
            |What is your name you poor soul?""".trimMargin()
    } else {
        """C'est français! Connexion établie. Bienvenue sur Transit Ace.
            |
            |Ceci est une simulation de survie. Je générerai des scénarios de crise aléatoires dans le métro—des grèves aux événements surnaturels.
            |Votre But : Faire les bons choix pour protéger votre Argent, votre Santé Mentale et votre Réputation.
            |Quel est ton nom, pauvre âme?""".trimMargin()
    }

    private fun buildSimulationSelectMessage(isEnglish: Boolean, name: String?) = if (isEnglish) {
        "Alright $name. Select your simulation type: Default or Custom?"
    } else {
        "D'accord $name. Sélectionnez votre type de simulation : Par défaut ou Personnalisé ?"
    }

    private fun buildScenariosRequirementsMessage(isEnglish: Boolean) = if (isEnglish) {
        """So, what is the situation? 
            |
            |Briefly describe the plot or the incident for your custom scenario. We'll generate your scenarios based on your story.""".trimMargin()
    } else {
        """Alors, quelle est la situation ?
            |
            |Décris brièvement l'intrigue ou l'incident pour ton scénario personnalisé. Nous générerons tes scénarios à partir de ton histoire.""".trimMargin()
    }

    private fun buildScenariosSuccessMessage(isEnglish: Boolean, storyLine: StoryLine?) = if (isEnglish) {
        """I've prepared your custom scenarios. 
            | 
            |${storyLine?.title}
            |${storyLine?.description}
            | 
            |Do you want to archive them to play another time, or continue to start the game right now?""".trimMargin()
    } else {
        """J'ai préparé tes scénarios personnalisés. 
            |
            |${storyLine?.title}
            |${storyLine?.description}
            |
            |Tu veux les **Archiver** pour y jouer une autre fois, ou **Continuer** pour lancer le jeu maintenant ?""".trimMargin()
    }

    private fun buildScenariosFailureMessage(isEnglish: Boolean) = if (isEnglish) {
        """Hmmm. That's unusual. I couldn't get things ready for you. Oh well... delays are normal in Paris. 
            |Should A) I try again, or B) would you prefer to just do the default scenario, little one?""".trimMargin()
    } else {
        "Hmmm. C'est inhabituel. Je n'ai pas réussi à préparer le terrain. Enfin bon... les retards, c'est la norme à Paris. A) Je réessaie, ou B) tu préfères le scénario par défaut, mon petit?"
    }

    private fun buildSetupCompleteMessage(gameSetup: GameSetup) = when {
        gameSetup.isEnglish && !gameSetup.isCustomSimulation ->
            "Welcome, ${gameSetup.name}. You just arrived in France. 20 years old, low confidence, little funds. Initializing..."

        !gameSetup.isEnglish && !gameSetup.isCustomSimulation ->
            """Bienvenue, ${gameSetup.name}. Vous venez d'arriver en France. 20 ans,
                |peu de confiance, peu d'argent. Initialisation...""".trimMargin()

        gameSetup.isEnglish ->
            "Okay, I am done. Welcome ${gameSetup.name}, to the city of Paris. I wish you well on your journey. You'll need all the luck you can get."

        else ->
            "D'accord, j'ai terminé. Bienvenue ${gameSetup.name}, dans la ville de Paris. Je vous souhaite bonne chance pour votre voyage. Vous aurez besoin de toute la chance possible."
    }

    fun sendInGameMessage(
        message: String,
        gameSetup: GameSetup,
        gameContext: String? = null
    ) {
        viewModelScope.launch {
            val isFrench = !gameSetup.isEnglish

            val result = sendChatMessage(
                message = message,
                isFrench = isFrench,
                gameContext = gameContext
            )

            result.onSuccess { response ->
                when (response) {
                    is ChatAIResponse.ExecuteCommand -> {
                        handleCommand(response.command, response.arg, isFrench)
                    }
                    is ChatAIResponse.TextResponse -> {
                        // Already saved by use case
                    }
                    is ChatAIResponse.NoResponse -> {
                        // Do nothing
                    }
                }
            }
        }
    }

    private fun handleCommand(command: String, arg: String?, isFrench: Boolean) {
        val protocolPrefix = if (isFrench) "> EXÉCUTION DU PROTOCOLE: " else "> EXECUTING PROTOCOL: "
        attachChatMessage(protocolPrefix + command.uppercase(), MessageSender.AI)

        when (command) {
            FunctionDeclaration.DECL_GET_ALL_STORYLINES -> handleGetAllStorylines(isFrench)
            FunctionDeclaration.DECL_SHOW_HELP -> handleShowHelp(isFrench)
            FunctionDeclaration.DECL_LOAD_STORYLINE -> {
                arg?.toLongOrNull()?.let { storyId ->
                    handleLoadStory(storyId, isFrench)
                }
            }
            FunctionDeclaration.DECL_RESTART_GAME -> handleRestartGame(isFrench)
        }
    }

    private fun handleLoadStory(storyId: Long, isFrench: Boolean) {
        val success = gameSessionRepository.loadStoryForSession(storyId)
        val message = if (success) {
            if (isFrench) "Scénario chargé." else "Scenario loaded."
        } else {
            if (isFrench) "Scénario introuvable." else "Scenario not found."
        }
        attachSystemMessage(message)
    }

    private fun handleRestartGame(isFrench: Boolean) {
        gameSessionRepository.clearCurrentScenario()
        val message = if (isFrench) "Simulation réinitialisée." else "Simulation reset."
        attachSystemMessage(message)
    }

    private fun handleGetAllStorylines(isFrench: Boolean) {
        val stories = storyRepository.getAllStories().joinToString("\n") { "- ${it.title}" }

        val responseMessage = when {
            stories.isEmpty() && isFrench -> "Aucune histoire disponible pour le moment."
            stories.isEmpty() -> "No stories available at the moment."
            isFrench -> "FICHIERS DISPONIBLES:\n$stories"
            else -> "AVAILABLE FILES:\n$stories"
        }

        attachChatMessage(responseMessage, MessageSender.AI)
    }

    private fun handleShowHelp(isFrench: Boolean) {
        val helpMessage = if (isFrench) {
            """- "Montre-moi toutes les intrigues"
                |- "Charge le scénario [nom]"
                |- "Réinitialise ce niveau"
                |- "Aide"""".trimMargin()
        } else {
            """- "Show me all storylines"
                |- "Load [name] scenario"
                |- "Reset this level"
                |- "Help"""".trimMargin()
        }
        attachChatMessage(helpMessage, MessageSender.AI)
    }

    fun updateGameMessageWithAction(selectedActionName: String, messageIdToUpdate: Long) {
        viewModelScope.launch {
            chatRepository.updateMessageSelectedAction(selectedActionName, messageIdToUpdate)
        }
    }

    fun clearUIState() {
        _uiDataState.value = UIDataState.Idle
    }
}