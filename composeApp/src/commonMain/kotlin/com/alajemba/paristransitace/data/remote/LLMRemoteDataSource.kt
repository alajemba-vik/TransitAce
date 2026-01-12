package com.alajemba.paristransitace.data.remote

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.mistralai.MistralAIModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.llms.all.simpleMistralAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import com.alajemba.paristransitace.BuildConfig
import com.alajemba.paristransitace.data.local.DefaultScenariosProvider
import com.alajemba.paristransitace.data.mapper.toDomain
import com.alajemba.paristransitace.data.mapper.toResponse
import com.alajemba.paristransitace.data.remote.model.ScenarioResponse
import com.alajemba.paristransitace.domain.model.ChatMessage
import com.alajemba.paristransitace.domain.model.GameLanguage
import com.alajemba.paristransitace.domain.model.MessageSender
import com.alajemba.paristransitace.domain.model.ScenarioTheme
import com.alajemba.paristransitace.domain.model.ScenariosWrapper
import com.alajemba.paristransitace.domain.model.StoryLine
import com.alajemba.paristransitace.domain.repository.ChatAIResponse
import com.alajemba.paristransitace.data.remote.model.Content
import com.alajemba.paristransitace.data.remote.model.FunctionDeclaration
import com.alajemba.paristransitace.data.remote.model.FunctionParameters
import com.alajemba.paristransitace.data.remote.model.GeminiRequest
import com.alajemba.paristransitace.data.remote.model.GeminiResponse
import com.alajemba.paristransitace.data.remote.model.MistralFunction
import com.alajemba.paristransitace.data.remote.model.MistralFunctionParameters
import com.alajemba.paristransitace.data.remote.model.MistralMessage
import com.alajemba.paristransitace.data.remote.model.MistralRequest
import com.alajemba.paristransitace.data.remote.model.MistralResponse
import com.alajemba.paristransitace.data.remote.model.MistralTool
import com.alajemba.paristransitace.data.remote.model.Part
import com.alajemba.paristransitace.data.remote.model.Parts
import com.alajemba.paristransitace.data.remote.model.Tool
import com.alajemba.paristransitace.utils.debugLog
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import kotlin.time.Clock

class LLMRemoteDataSource(
    private val httpClient: HttpClient,
    private val defaultScenariosProvider: DefaultScenariosProvider
) : RemoteDataSource {

    private val googleGeminiEndpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    private val mistralEndpoint = "https://api.mistral.ai/v1/chat/completions"
    private val geminiKey = BuildConfig.GEMINI_API_KEY
    private val mistralAIKey = BuildConfig.MISTRALAI_API_KEY

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generateScenarios(
        transitRulesJson: String,
        language: GameLanguage,
        plot: String
    ): Result<ScenariosWrapper> {
        return try {
            val isFrench = language == GameLanguage.FRENCH
            val systemPrompt = buildScenarioSystemPrompt(transitRulesJson, isFrench, plot)

            val promptExecutor: PromptExecutor = when{
                geminiKey.isNotBlank() -> simpleGoogleAIExecutor(geminiKey)
                mistralAIKey.isNotBlank() -> simpleMistralAIExecutor(mistralAIKey)
                else -> throw IllegalStateException("No API key configured. Please add GEMINI_API_KEY or MISTRALAI_API_KEY to local.properties")
            }

            val  llm: LLModel = when{
                geminiKey.isNotBlank() -> GoogleModels.Gemini2_5FlashLite
                mistralAIKey.isNotBlank() -> MistralAIModels.Chat.MistralSmall2
                else -> throw IllegalStateException("No API key configured")
            }


            val agent = AIAgent(
                promptExecutor = promptExecutor,
                agentConfig = AIAgentConfig.withSystemPrompt(
                    llm = llm,
                    prompt = systemPrompt,
                    maxAgentIterations = 3,
                    id = "scenario-generation-agent"
                )
            )

            val agentResponse = agent.run(
                if (language == GameLanguage.ENGLISH) "Generate scenario in English."
                else "Générer le scénario en français."
            )

            debugLog("Successfully generated scenarios from LLM: $agentResponse")
            parseScenarioResponse(agentResponse)
        } catch (e: Exception) {
            debugLog("Error generating scenarios: ${e.message}")
            Result.failure(e)
        }
    }

    // Supports Gemini AI and Mistral AI
    override suspend fun sendChatMessage(
        chatHistory: List<ChatMessage>,
        storyLines: List<StoryLine>,
        gameContext: String?
    ): Result<ChatAIResponse> {
        return try {
            if (geminiKey.isNotBlank()) {
                sendChatMessageGemini(chatHistory, storyLines, gameContext)
            } else if (mistralAIKey.isNotBlank()) {
                sendChatMessageMistral(chatHistory, storyLines, gameContext)
            } else {
                Result.failure(Exception("No API key configured for chat"))
            }
        } catch (e: Exception) {
            debugLog("Error sending chat message to LLM: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun sendChatMessageGemini(
        chatHistory: List<ChatMessage>,
        storyLines: List<StoryLine>,
        gameContext: String?
    ): Result<ChatAIResponse> {
        val conversationContent = chatHistory.map { chatMessage ->
            Content(
                role = if (chatMessage.sender == MessageSender.USER) "user" else "model",
                parts = listOf(Part(text = chatMessage.message))
            )
        }

        val requestBody = GeminiRequest(
            systemInstruction = Parts(
                parts = listOf(Part(text = buildSophiaChatPrompt(gameContext, storyLines)))
            ),
            contents = conversationContent,
            tools = listOf(gameTools)
        )

        val response = httpClient.post {
            url(googleGeminiEndpoint)
            contentType(ContentType.Application.Json)
            header("x-goog-api-key", geminiKey)
            setBody(requestBody)
        }.body<GeminiResponse>()

        val candidate = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()

        return when {
            candidate?.functionCall != null -> {
                Result.success(ChatAIResponse.ExecuteCommand(
                    command = candidate.functionCall.name,
                    arg = candidate.functionCall.args[FunctionParameters.PARAM_SCENARIO_ID]
                ))
            }
            !candidate?.text.isNullOrBlank() -> {
                Result.success(ChatAIResponse.TextResponse(candidate.text))
            }
            else -> {
                Result.success(ChatAIResponse.NoResponse)
            }
        }.also {
            debugLog("Successfully sent chat message to Gemini: $response")
        }
    }

    private suspend fun sendChatMessageMistral(
        chatHistory: List<ChatMessage>,
        storyLines: List<StoryLine>,
        gameContext: String?
    ): Result<ChatAIResponse> {
        val systemMessage = MistralMessage(
            role = "system",
            content = buildSophiaChatPrompt(gameContext, storyLines)
        )

        val conversationMessages = chatHistory.map { chatMessage ->
            MistralMessage(
                role = if (chatMessage.sender == MessageSender.USER) "user" else "assistant",
                content = chatMessage.message
            )
        }

        val requestBody = MistralRequest(
            model = "mistral-small-latest",
            messages = listOf(systemMessage) + conversationMessages,
            tools = mistralGameTools,
            toolChoice = "auto",
            parallelToolCalls = false
        )

        val response = httpClient.post {
            url(mistralEndpoint)
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $mistralAIKey")
            setBody(requestBody)
        }.body<MistralResponse>()

        // Check for API error
        response.error?.let { error ->
            return Result.failure(Exception(error.message ?: "Mistral API error"))
        }

        val choice = response.choices?.firstOrNull()
        val message = choice?.message

        return when {
            message?.toolCalls?.isNotEmpty() == true -> {
                val toolCall = message.toolCalls.first()
                val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                val scenarioId = args[FunctionParameters.PARAM_SCENARIO_ID]?.jsonPrimitive?.contentOrNull

                Result.success(ChatAIResponse.ExecuteCommand(
                    command = toolCall.function.name,
                    arg = scenarioId
                ))
            }
            !message?.content.isNullOrBlank() -> {
                Result.success(ChatAIResponse.TextResponse(message.content))
            }
            else -> {
                Result.success(ChatAIResponse.NoResponse)
            }
        }.also {
            debugLog("Successfully sent chat message to Mistral: $response")
        }
    }

    private fun parseScenarioResponse(response: String): Result<ScenariosWrapper> {

        return try {
            // Strip markdown code fences if present (handles newlines)
            val cleanedResponse = response
                .trim()
                .replace(Regex("^```json\\s*"), "")
                .replace(Regex("^```\\s*"), "")
                .replace(Regex("\\s*```$"), "")
                .trim()

            val jsonResponse = json.parseToJsonElement(cleanedResponse)
            val storyLineTitle = jsonResponse.jsonObject["storyLineTitle"]?.jsonPrimitive?.content ?: "Untitled Story"
            val storyLineDescription = jsonResponse.jsonObject["storyLineDescription"]?.jsonPrimitive?.content ?: ""
            val initialBudget = jsonResponse.jsonObject["initialBudget"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            val initialMorale = jsonResponse.jsonObject["initialMorale"]?.jsonPrimitive?.intOrNull ?: 0
            val scenariosJson = jsonResponse.jsonObject["scenarios"]?.toString() ?: "[]"

            val scenarios = json.decodeFromString(
                ListSerializer(ScenarioResponse.serializer()),
                scenariosJson
            ).map { it.toDomain() }

            Result.success(
                ScenariosWrapper(
                    storyLine = StoryLine(
                        id = null,
                        title = storyLineTitle,
                        description = storyLineDescription,
                        timeCreated = Clock.System.now().toEpochMilliseconds(),
                        initialBudget = initialBudget,
                        initialMorale = initialMorale
                    ),
                    scenarios = scenarios
                )
            )
        } catch (e: Exception) {
            debugLog("Error parsing LLM response: ${e.message}")
            Result.failure(e)
        }
    }

    private fun buildScenarioSystemPrompt(transitRulesJson: String, isFrench: Boolean, plot: String): String {

        // build a few default scenarios to show as an example array
        val defaultScenarios = defaultScenariosProvider.getDefaultScenarios(isFrench)
            .take(3)
            .map { it.toResponse() }

        // example as a JSON array to force the model to emit a list
        val exampleJsonArray = Json.encodeToString(ListSerializer(ScenarioResponse.serializer()), defaultScenarios.take(3))

        return """
            You are a game scenario generator for 'Paris Transit Ace'.
            Use the following TRANSIT RULES JSON as your absolute source of truth:
            $transitRulesJson
            Generate one or more Paris transit scenarios using ONLY this data and based on this plot: 
            $plot

            Output Requirements:
            - Respond with a single valid JSON OBJECT. No commentary. No markdown. No code fences.
            - The JSON Object MUST contain the following fields:
              1. `storyLineTitle` (String)
              2. `storyLineDescription` (String)
              3. `initialBudget` (Double): The initial money based on the plot difficulty and best case scenario where the user makes all the right choices.
              4. `initialMorale` (Integer): The initial morale level based on the plot. The maximum is 100 and the minimum is 0.
              5. `scenarios` (Array): A list of Scenario objects.
            - Each array element must match the Scenario data class exactly (id, title, description, options, correctOptionId, nextScenarioId (optional), scenarioTheme).
            - Each option must include: id, text, budgetImpact (number), moraleImpact (integer), commentary (string), inventory (array of inventory objects with name/description/imageUrl), increaseLegalInfractionsBy (integer).

            - Each scenario object MUST include the field `scenarioTheme` with a STRING value matching exactly one of the ScenarioTheme enum keys listed below (case-sensitive). If you are unsure which theme fits, use `DEFAULT`.
              Allowed values for scenarioTheme: ${ScenarioTheme.entries.joinToString(", ") { it.name }}

            Example of a valid output (only JSON array):
            {
                "storyLineTitle": "A Day in Paris",
                "storyLineDescription": "As a 20 year old student from Tanzania who just moved to Paris, you need to improve your love life and not miss classes. You discover that the Metro system is actually a matchmaking algorithm: your crush rides the prestigious Line 1, but your university is stuck on the chaotic Line 13. You must master the 'Correspondence' to survive both.",
                "initialBudget": 100.0,
                "initialMorale": 35,
                "scenarios": $exampleJsonArray
            }

            Rules for generation:
            1. Create commute scenarios in Paris that could be realistic, but if the user's plot is an unrealistic one, YOU MUST comply.
            2. Set `initialBudget` and `initialMorale` to values that match the difficulty of the plot.
            3. For each scenario provide exactly 2 options: one correct and one 'trap' based on the rules.
            4. Include 'Sophia', a sarcastic 68-year-old Parisian, in the commentary of each scenario.
            5. Output MUST be a valid raw JSON object matching the Example. Do not wrap it in markdown code blocks.
            6. Do not include ```json or ``` in your response.
        """.trimIndent()
    }
}

fun buildSophiaChatPrompt(gameContext: String?, availableStoryLines: List<StoryLine>): String {
    val filesList = if (availableStoryLines.isEmpty()) {
        "No scenarios available."
    } else {
        availableStoryLines.joinToString("\n") { "- [ID: ${it.id}] ${it.title} (${it.description.take(50)}...)" }
    }

    return """
             You are Sophia, a 68-year-old Parisian grandmother working at a transit help desk.
        
            Your Personality:
            - You are impatient, sarcastic, and bluntly honest.
            - You can use French slang like "Bof", "Putain", "Merde", "Oulala".
            - You sometimes mention that the user is wasting your time.
            
            Current Game Status:
            ${gameContext ?: "The user is currently in the main menu."}
            
            AVAILABLE SCENARIOS (DATABASE):
            $filesList
          
            IMPORTANT OUTPUT RULES:
            1. Speak DIRECTLY to the user.
            2. Do NOT use asterisks (*) to describe actions, looks, or tone.
            3. Just output the text of what you say.
            
            Goal: Answer the user's question, but make them feel slightly stupid for asking it. Keep it short (under 50 words).
        """.trimIndent()
}

val gameTools = Tool(
    functionDeclarations = listOf(
        // Action 1: See All Storylines
        FunctionDeclaration(
            name = FunctionDeclaration.DECL_GET_ALL_STORYLINES,
            description = "Retrieves and displays a list of all available game storylines or scenarios.",
            parameters = FunctionParameters(
                type = "object",
                properties = emptyMap(),
                required = emptyList()
            )
        ),
        // Action 2: Load New Storyline
        FunctionDeclaration(
            name = FunctionDeclaration.DECL_LOAD_STORYLINE,
            description = "Clears the current session and loads a specific storyline. Requires a keyword or ID.",
            parameters = FunctionParameters(
                type = "object",
                properties = mapOf(
                    FunctionParameters.PARAM_SCENARIO_ID to JsonObject(
                        mapOf(
                            "type" to JsonPrimitive("integer"),        // Matches doc: "type (string)"
                            "description" to JsonPrimitive("The exact ID of the scenario to load.") // Matches doc: "description (string)"
                        )
                    ),
                ),
                required = listOf(FunctionParameters.PARAM_SCENARIO_ID)
            )
        ),
        // Action 3: Reset Game
        FunctionDeclaration(
            name = FunctionDeclaration.DECL_RESTART_GAME,
            description = "Resets the current storyline progress back to the beginning (0% completion).",
            parameters = FunctionParameters(
                type = "object",
                properties = emptyMap(),
                required = emptyList()
            )
        ),
        // Action 4: Help
        FunctionDeclaration(
            name = FunctionDeclaration.DECL_SHOW_HELP,
            description = "Displays a help menu listing all available commands and actions Sophia can perform.",
            parameters = FunctionParameters(
                type = "object",
                properties = emptyMap(),
                required = emptyList()
            )
        )
    )
)

// Mistral AI tools - same functions but in Mistral's format
val mistralGameTools = listOf(
    // Action 1: See All Storylines
    MistralTool(
        type = "function",
        function = MistralFunction(
            name = FunctionDeclaration.DECL_GET_ALL_STORYLINES,
            description = "Retrieves and displays a list of all available game storylines or scenarios.",
            parameters = MistralFunctionParameters(
                type = "object",
                properties = emptyMap(),
                required = emptyList()
            )
        )
    ),
    // Action 2: Load New Storyline
    MistralTool(
        type = "function",
        function = MistralFunction(
            name = FunctionDeclaration.DECL_LOAD_STORYLINE,
            description = "Clears the current session and loads a specific storyline. Requires a keyword or ID.",
            parameters = MistralFunctionParameters(
                type = "object",
                properties = mapOf(
                    FunctionParameters.PARAM_SCENARIO_ID to JsonObject(
                        mapOf(
                            "type" to JsonPrimitive("integer"),
                            "description" to JsonPrimitive("The exact ID of the scenario to load.")
                        )
                    )
                ),
                required = listOf(FunctionParameters.PARAM_SCENARIO_ID)
            )
        )
    ),
    // Action 3: Reset Game
    MistralTool(
        type = "function",
        function = MistralFunction(
            name = FunctionDeclaration.DECL_RESTART_GAME,
            description = "Resets the current storyline progress back to the beginning (0% completion).",
            parameters = MistralFunctionParameters(
                type = "object",
                properties = emptyMap(),
                required = emptyList()
            )
        )
    ),
    // Action 4: Help
    MistralTool(
        type = "function",
        function = MistralFunction(
            name = FunctionDeclaration.DECL_SHOW_HELP,
            description = "Displays a help menu listing all available commands and actions Sophia can perform.",
            parameters = MistralFunctionParameters(
                type = "object",
                properties = emptyMap(),
                required = emptyList()
            )
        )
    )
)

