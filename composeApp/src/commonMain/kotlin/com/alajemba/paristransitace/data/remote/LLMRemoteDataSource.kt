package com.alajemba.paristransitace.data.remote

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
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
import com.alajemba.paristransitace.data.remote.model.Part
import com.alajemba.paristransitace.data.remote.model.Parts
import com.alajemba.paristransitace.data.remote.model.Tool
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

    private val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generateScenarios(
        transitRulesJson: String,
        language: GameLanguage,
        plot: String
    ): Result<ScenariosWrapper> {
        return try {
            val isFrench = language == GameLanguage.FRENCH
            val systemPrompt = buildScenarioSystemPrompt(transitRulesJson, isFrench, plot)

            val agent = AIAgent(
                promptExecutor = simpleGoogleAIExecutor(apiKey),
                agentConfig = AIAgentConfig.withSystemPrompt(
                    llm = GoogleModels.Gemini2_5FlashLite,
                    prompt = systemPrompt,
                    maxAgentIterations = 3,
                    id = "scenario-generation-agent"
                )
            )

            val agentResponse = agent.run(
                if (language == GameLanguage.ENGLISH) "Generate scenario in English."
                else "Générer le scénario en français."
            )

            parseScenarioResponse(agentResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendChatMessage(
        chatHistory: List<ChatMessage>,
        storyLines: List<StoryLine>,
        gameContext: String?
    ): Result<ChatAIResponse> {
        return try {
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
                url(endpoint)
                contentType(ContentType.Application.Json)
                header("x-goog-api-key", apiKey)
                setBody(requestBody)
            }.body<GeminiResponse>()

            val candidate = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()

            when {
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
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseScenarioResponse(response: String): Result<ScenariosWrapper> {

        return try {
            val jsonResponse = json.parseToJsonElement(response)
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