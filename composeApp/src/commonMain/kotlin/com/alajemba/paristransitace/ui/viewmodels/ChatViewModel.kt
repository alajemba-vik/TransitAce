package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.data.local.DefaultScenariosProvider
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
import com.alajemba.paristransitace.utils.debugLog
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
    val uiDataState = _uiDataState.asStateFlow()
    val isLoading = uiDataState.map { it is UIDataState.Loading }

    init {
        observeChatMessages()
    }

    private fun observeChatMessages() {
        viewModelScope.launch {
            chatRepository.getAllMessages().collect { savedChatMessages ->

                _chatMessages.value = savedChatMessages.map { it.toChatUiModel() }
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

    fun setupGame(
        updatedGameSetup: GameSetup?,
        storyLine: StoryLine? = null,
        userMessage: String? = null,
        isEnglish: Boolean,
    ) {
        debugLog("ChatViewModel.setupGame: updatedGameSetup=$updatedGameSetup, userMessage=$userMessage, isEnglish=$isEnglish")
        val actions = mutableListOf<String>()

        val chatMessage = if (updatedGameSetup != null) {
            when {
                updatedGameSetup.isOnLanguageStep -> if (isEnglish) "English or Français? Keep it simple, kid"
                else "Anglais ou Français? Fais simple, gamin"

                updatedGameSetup.isOnNameStep -> buildNameStepMessage(isEnglish)

                updatedGameSetup.isOnSelectSimulationStep -> buildSimulationSelectMessage(
                    isEnglish,
                    updatedGameSetup.name
                )

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
        } else {
            ""
        }

        if (chatMessage.isNotBlank()) {
            attachChatMessage(chatMessage, MessageSender.AI, actions)
        } else {
            sendInGameMessage(userMessage ?: return, isEnglish)
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

    private fun buildSimulationSelectMessage(isEnglish: Boolean, name: String) = if (isEnglish) {
        "Alright ${name.capitalizeName()}. Select your simulation type: Default or Custom?"
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
        isEnglish: Boolean,
        gameContext: String? = null
    ) {
        // Check for local commands
        getFunctionToolCommand(message)?.let { (command, arg) ->
            handleCommand(command, arg = arg, isEnglish)
            return
        }

        viewModelScope.launch {

            val result = sendChatMessage(
                message = message,
                isEnglish = isEnglish,
                gameContext = gameContext
            )

            result.onSuccess { response ->
                when (response) {
                    is ChatAIResponse.ExecuteCommand -> {
                        handleCommand(response.command, response.arg, isEnglish)
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

    /**
     * Returns a Pair of (command, arg) if the message matches a local command,
     * null otherwise.
     */
    fun getFunctionToolCommand(message: String): Pair<String, String?>? {
        val cleanedInput = message.trim().lowercase()

        return when {
            // Help command
            cleanedInput.matches(Regex("""^(help|aide|h|\?|besoin d'aide|need help)$""")) ->
                FunctionDeclaration.DECL_SHOW_HELP to null

            // Clear chat command
            cleanedInput.matches(Regex("""^(clear|cls|effacer|vider)(\s*(chat|messages?|conversation)?)?$""")) ->
                FunctionDeclaration.DECL_CLEAR_CHAT to null

            // Delete ALL storylines command (must come before single delete)
            cleanedInput.matches(Regex("""^(delete|remove|supprimer?|effacer)\s+all(\s*(storylines?|scenarios?|scénarios?|stories|histoires?))?$""")) ||
            cleanedInput.matches(Regex("""^(supprimer?|effacer)\s+(tout|tous?)(\s*(les\s*)?(storylines?|scenarios?|scénarios?|histoires?))?$""")) ->
                FunctionDeclaration.DECL_DELETE_ALL_STORYLINES to null

            // Delete single storyline command - extract the storyline name
            cleanedInput.matches(Regex("""^(delete|remove|supprimer?|effacer)\s+(storyline|scenario|scénario|story|histoire)?\s*.+$""")) -> {
                val arg = extractDeleteArg(cleanedInput)
                FunctionDeclaration.DECL_DELETE_STORYLINE to arg
            }

            // Show all storylines - flexible matching
            cleanedInput.matches(Regex("""^(show\s*(me\s*)?(all\s*)?(the\s*)?)?(stories|storylines?|scenarios?|scénarios?|list|liste|parcours).*$""")) ||
            cleanedInput.matches(Regex("""^(affiche|montre|voir|display).*?(stories|storylines?|scenarios?|scénarios?|parcours|liste?).*$""")) ->
                FunctionDeclaration.DECL_GET_ALL_STORYLINES to null

            // Load/start storyline commands - extract the storyline name/id
            cleanedInput.matches(Regex("""^(load|start|begin|charge[rz]?|commence[rz]?|lance[rz]?).*?(storyline|scenario|scénario|parcours|histoire).*$""")) ||
            cleanedInput.matches(Regex("""^(load|charge[rz]?)\s+\[?.+\]?\s*(scenario|scénario)?.*$""")) -> {
                val arg = extractStorylineArg(cleanedInput)
                FunctionDeclaration.DECL_LOAD_STORYLINE to arg
            }

            // Reset/restart commands - flexible matching
            cleanedInput.matches(Regex("""^(reset|restart|recommence[rz]?|réinitialise[rz]?|reinitialise[rz]?)(\s*(this\s*)?(level|scenario|scénario|partie|game)?)?.*$""")) ->
                FunctionDeclaration.DECL_RESTART_GAME to null

            else -> null
        }
    }

    /**
     * Extracts the storyline argument from a load command.
     * E.g., "load [metro strike] scenario" -> "metro strike"
     */
    private fun extractStorylineArg(input: String): String? {
        // Try to extract from brackets first [storyline name]
        val bracketMatch = Regex("""\[([^\]]+)\]""").find(input)
        if (bracketMatch != null) {
            return bracketMatch.groupValues[1].trim()
        }

        // Try to extract after load/charge keywords
        val keywordMatch = Regex("""^(load|charge[rz]?|lance[rz]?|commence[rz]?)\s+(.+?)(\s*(scenario|scénario|storyline|parcours))?$""").find(input)
        return keywordMatch?.groupValues?.get(2)?.trim()
    }

    /**
     * Extracts the storyline name from a delete command.
     * E.g., "delete [metro strike]" -> "metro strike"
     */
    private fun extractDeleteArg(input: String): String? {
        // Try to extract from brackets first [storyline name]
        val bracketMatch = Regex("""\[([^\]]+)\]""").find(input)
        if (bracketMatch != null) {
            return bracketMatch.groupValues[1].trim()
        }

        // Try to extract after delete/remove keywords
        val keywordMatch = Regex("""^(delete|remove|supprimer?|effacer)\s*(storyline|scenario|scénario|story|histoire)?\s+(.+)$""").find(input)
        return keywordMatch?.groupValues?.get(3)?.trim()
    }

    /**
     * Checks if the message is a local command.
     */
    fun isFunctionToolCommand(message: String): Boolean = getFunctionToolCommand(message) != null

    private fun handleCommand(command: String, arg: String?, isEnglish: Boolean): Boolean {
        val protocolPrefix = if (!isEnglish) "> EXÉCUTION DU PROTOCOLE: " else "> EXECUTING PROTOCOL: "
        attachChatMessage(protocolPrefix + command.uppercase(), MessageSender.AI)

        debugLog("ChatViewModel.handleCommand: command=$command, arg=$arg, isEnglish=$isEnglish")

        when (command) {
            FunctionDeclaration.DECL_GET_ALL_STORYLINES -> handleGetAllStorylines(isEnglish)
            FunctionDeclaration.DECL_SHOW_HELP -> handleShowHelp(isEnglish)
            FunctionDeclaration.DECL_CLEAR_CHAT -> handleClearChat(isEnglish)
            FunctionDeclaration.DECL_DELETE_ALL_STORYLINES -> handleDeleteAllStorylines(isEnglish)
            FunctionDeclaration.DECL_DELETE_STORYLINE -> {
                if (!arg.isNullOrBlank()) {
                    handleDeleteStoryline(arg, isEnglish)
                } else {
                    return false
                }
            }
            FunctionDeclaration.DECL_LOAD_STORYLINE-> {
                if (!arg.isNullOrBlank()) {
                    handleLoadStory(arg, isEnglish)

                } else {
                    return false
                }
            }
            FunctionDeclaration.DECL_RESTART_GAME -> handleRestartGame(isEnglish)
            else -> return false
        }

        return true
    }

    private fun handleLoadStory(arg: String, isEnglish: Boolean) {
        var storyId = arg.toLongOrNull()

        print("Loading story with arg: $arg, parsed id: $storyId")

        if (storyId == null) {
            storyId = storyRepository.getStoryByTitle(arg)?.id
        }

        val success = if (storyId == null) false else gameSessionRepository.loadStoryForSession(storyId)
        val message = if (success) {
            _uiDataState.value = UIDataState.Success.StorylineLoaded

            if (!isEnglish) "Scénario chargé." else "Scenario loaded."
        } else {
            if (!isEnglish) "Scénario introuvable." else "Scenario not found."
        }

        attachSystemMessage(message)

        val description = gameSessionRepository.currentStoryLine?.description ?: ""

        if (description.isNotBlank()) {
            attachChatMessage(
                (if (isEnglish) "Description:" else "Description :") + "\n$description",
                sender = MessageSender.AI
            )
            attachChatMessage(
                if (isEnglish) "Starting..." else "Démarrage" + "...",
                sender = MessageSender.AI
            )
        }
    }

    private fun handleRestartGame(isEnglish: Boolean) {
        gameSessionRepository.restartCurrentStoryline()
        val message = if (!isEnglish) "Simulation réinitialisée." else "Simulation reset."
        attachSystemMessage(message)
    }

    private fun handleClearChat(isEnglish: Boolean) {
        viewModelScope.launch {
            chatRepository.clearAllMessages()
            val message = if (!isEnglish) "Conversation effacée." else "Chat cleared."
            attachSystemMessage(message)
        }
    }

    private fun handleDeleteStoryline(title: String, isEnglish: Boolean) {
        val deleted = storyRepository.deleteStoryByTitle(title)
        val message = if (deleted) {
            if (!isEnglish) "Scénario \"$title\" supprimé." else "Storyline \"$title\" deleted."
        } else {
            if (!isEnglish) "Scénario \"$title\" introuvable." else "Storyline \"$title\" not found."
        }
        attachSystemMessage(message)
    }

    private fun handleDeleteAllStorylines(isEnglish: Boolean) {
        storyRepository.clearAllStoriesAndScenarios()
        val message = if (!isEnglish) "Tous les scénarios ont été supprimés." else "All storylines deleted."
        attachSystemMessage(message)
    }

    private fun handleGetAllStorylines(isEnglish: Boolean) {

        val allStories = storyRepository.getAllStories().toMutableList()
        // we do not want to show the default game
        val savedDefaultGameIndex = allStories.indexOfFirst { it.id == DefaultScenariosProvider.DEFAULT_ID }
        if (savedDefaultGameIndex != -1) {
            allStories.removeAt(savedDefaultGameIndex)
        }

        val stories = allStories.joinToString("\n") { "#${it.id} - ${it.title} " }


        val responseMessage = when {
            stories.isEmpty() && !isEnglish -> "Aucune histoire disponible pour le moment."
            stories.isEmpty() -> "No stories available at the moment."
            !isEnglish -> "FICHIERS DISPONIBLES:\n$stories"
            else -> "AVAILABLE FILES:\n$stories"
        }

        attachChatMessage(responseMessage, MessageSender.AI)
    }

    private fun handleShowHelp(isEnglish: Boolean) {
        val helpMessage = if (!isEnglish) {
            """- "Montre-moi toutes les intrigues"
                |- "Charge le scénario [nom / ID]"
                |- "Supprime le scénario [nom / ID]"
                |- "Supprimer tous les scénarios"
                |- "Réinitialise ce niveau"
                |- "Effacer" (vider le chat)
                |- "Aide"""".trimMargin()
        } else {
            """- "Show me all storylines"
                |- "Load [name / ID] scenario"
                |- "Delete [name / ID] storyline"
                |- "Delete all storylines"
                |- "Reset this level"
                |- "Clear" (clear chat)
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

private fun String.capitalizeName(): String = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase() else it.toString()
}