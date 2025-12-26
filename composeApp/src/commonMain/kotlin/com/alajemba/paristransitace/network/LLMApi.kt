package com.alajemba.paristransitace.network

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import com.alajemba.paristransitace.BuildConfig
import com.alajemba.paristransitace.db.ChatMessages
import com.alajemba.paristransitace.network.models.*
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.model.GameSetup.GameLanguage
import com.alajemba.paristransitace.ui.model.Scenario
import com.alajemba.paristransitace.ui.model.ScenarioTheme
import com.alajemba.paristransitace.ui.model.ScenariosWrapper
import com.alajemba.paristransitace.ui.model.StoryLine
import com.alajemba.paristransitace.ui.viewmodels.buildDefaultScenarios
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock

class LLMApi(
    private val httpClient: HttpClient
) {

    // kept for potential future direct HTTP use
    private val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    private val apiKey = BuildConfig.GEMINI_API_KEY


    private val scenarioSystemPrompt = { transitRulesJson: String, isFrench: Boolean, plot: String ->
        // build a few default scenarios to show as an example array
        val defaultScenarios = buildDefaultScenarios(isFrench)

        // example as a JSON array to force the model to emit a list
        val exampleJsonArray = Json.encodeToString(ListSerializer(Scenario.serializer()), defaultScenarios.take(3))

        """
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
            5. Output MUST be a valid JSON object matching the Example.
        """.trimIndent()
    }

    private lateinit var scenarioGenerationAgent: AIAgent<String, String>

    suspend fun generateScenarios(transitRulesJson: String, plot: String, language: GameLanguage): ApiResponse<ScenariosWrapper> {
        try {
            println("Starting scenario generation agent...")
            scenarioGenerationAgent = AIAgent(
                promptExecutor = simpleGoogleAIExecutor(apiKey),
                agentConfig = AIAgentConfig.withSystemPrompt(
                    llm = GoogleModels.Gemini2_5FlashLite,
                    prompt = scenarioSystemPrompt(transitRulesJson, language == GameLanguage.FRENCH,  plot),
                    maxAgentIterations = 3,
                    id = "scenario-generation-agent"
                ),
            )

            println("Running scenario generation agent...")

            val agentResponse = scenarioGenerationAgent.run(
                when (language) {
                    GameLanguage.ENGLISH -> "Generate scenario in English."
                    else -> "Générer le scénario en français."
                }
            )

            val json = Json { ignoreUnknownKeys = true }
            try {
                println("Parsing generated scenarios... $agentResponse")
                val jsonResponse = json.parseToJsonElement(agentResponse)
                val storyLineTitle = jsonResponse.jsonObject["storyLineTitle"]?.jsonPrimitive?.content ?: "Untitled Story"
                val storyLineDescription = jsonResponse.jsonObject["storyLineDescription"]?.jsonPrimitive?.content ?: ""
                val initialBudget = jsonResponse.jsonObject["initialBudget"]?.jsonPrimitive?.doubleOrNull ?: .0
                val initialMorale = jsonResponse.jsonObject["initialMorale"]?.jsonPrimitive?.intOrNull ?: 0
                val scenariosJson = jsonResponse.jsonObject["scenarios"]?.toString() ?: "[]"

                println("Story Line Title: $storyLineTitle")
                println("Scenarios: $scenariosJson")

                val scenarios = json.decodeFromString(ListSerializer(Scenario.serializer()), scenariosJson)

                val response = ScenariosWrapper(
                    wrapper = Pair(
                        first = StoryLine(
                            title = storyLineTitle,
                            description = storyLineDescription,
                            timeCreated = Clock.System.now().toEpochMilliseconds(),
                            initialBudget = initialBudget,
                            initialMorale = initialMorale
                        ),
                        second = scenarios
                    )
                )
                return ApiResponse(data = response)
            } catch (serEx: Exception) {
                println(
                    "Failed Scenario Agent Response: $serEx"
                )
                return ApiResponse(errorMessage = serEx.message)
            }

        } catch (e: Exception) {
            println("Scenario generation error: $e")
            return ApiResponse(errorMessage = e.message ?: "404")
        }
    }

    suspend fun sendUserChatMessage(message: String, history: List<ChatMessages>, gameContext: String? = null): ApiResponse<String> {
        var conversationContent = history.map { chatMessage ->
            Content(
                role = if (ChatMessageSender.USER.name == chatMessage.sender) "user" else "model",
                parts = listOf(Part(text = chatMessage.message))
            )
        }

        conversationContent = conversationContent + listOf(
            Content(
                parts = listOf(Part(message)),
                role = "user"
            )
        )

        val requestBody = GeminiRequest(
            systemInstruction = Parts(
                parts = listOf(Part(getSophiaChatPrompt(gameContext)))
            ),
            contents = conversationContent
        )

        try {
            val response = httpClient.post {
                url(ENDPOINT)
                contentType(ContentType.Application.Json)
                header("x-goog-api-key", apiKey)
                setBody(requestBody)
            }.body<GeminiResponse>()

            val reply = response.candidates?.first()?.content?.parts?.first()?.text ?: ""

            return ApiResponse(data = reply)

        } catch (e: Exception) {
            return ApiResponse(errorMessage = e.message ?: "Connection error ${e.message}")
        }

    }
}


private fun getSophiaChatPrompt(gameContext: String?): String {
    return """
             You are Sophia, a 68-year-old Parisian grandmother working at a transit help desk.
        
            Your Personality:
            - You are impatient, sarcastic, and bluntly honest.
            - You can use French slang like "Bof", "Putain", "Merde", "Oulala".
            - You sometimes mention that the user is wasting your time.
            
            Current Game Status:
            ${gameContext ?: "The user is currently in the main menu."}
            
            IMPORTANT OUTPUT RULES:
            1. Speak DIRECTLY to the user.
            2. Do NOT use asterisks (*) to describe actions, looks, or tone.
            3. Just output the text of what you say.
            
            Goal: Answer the user's question, but make them feel slightly stupid for asking it. Keep it short (under 50 words).
        """.trimIndent()
}