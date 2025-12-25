package com.alajemba.paristransitace

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.alajemba.paristransitace.db.ParisTransitDatabase
import com.alajemba.paristransitace.entity.ChatMessageEntity
import com.alajemba.paristransitace.model.GameSetting
import com.alajemba.paristransitace.network.LLMApi
import com.alajemba.paristransitace.network.models.ApiResponse
import com.alajemba.paristransitace.ui.model.ChatMessageAction
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.model.GameSetup.GameLanguage
import com.alajemba.paristransitace.ui.model.Scenario
import com.alajemba.paristransitace.ui.model.ScenarioTheme
import com.alajemba.paristransitace.ui.model.ScenariosWrapper
import com.alajemba.paristransitace.ui.model.StoryLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlin.time.Clock

internal class TransitAceSDK(
    private val database: ParisTransitDatabase,
    private val llmApi: LLMApi
) {
    private val dbQueries = database.parisTransitDatabaseQueries
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // Settings
    fun <T> saveSetting(key: GameSetting<T>, value: String) {
        println("Saving setting: ${key.key} = $value")
        dbQueries.insertSetting(key.key, value)
    }

    fun <T> getSetting(key: GameSetting<T>): Flow<T?> {
        return dbQueries.getSetting(key.key).asFlow().mapToOneOrNull(Dispatchers.Default).map {
            value ->  key.toValue(value).also {
                println("In sdk Loaded setting: ${key.key} = $it")
            }
        }
    }

    fun clearSettings() {
        dbQueries.removeAllSettings()
    }


    // Chat
    internal fun getAllChatMessages(): Flow<List<ChatMessageEntity>> {
        return dbQueries.selectAllChatMessages { id, sender, message, timestamp, actions, selectedActionName ->
            ChatMessageEntity(
                sender = sender,
                message = message,
                timeSent = timestamp ?: 0L,
                selectedActionName = selectedActionName,
                actions = actions?.split(",")?.map { it.trim() } ?: emptyList()
            )
        }.asFlow().mapToList(Dispatchers.Default)
    }

    internal fun clearChat() {
        dbQueries.removeAllChatMessages()
    }
    fun insertChatMessage(message: String, sender: String, actions: List<ChatMessageAction> = emptyList()){
        dbQueries.insertChatMessage(
            id = null,
            sender = sender,
            message = message,
            timestamp = Clock.System.now().toEpochMilliseconds(),
            actions = actions.joinToString(),
            selectedActionName = null
        )
    }
    
    suspend fun updateChatMessage(selectedActionDescription: String?) {

    }

    suspend fun sendChatMessage(message: String, sender: String): ApiResponse<*> {
        insertChatMessage(message, sender)

        val response = llmApi.sendChatMessage(message)

        println(response.data)

        val message = if (response.data.isNullOrBlank()) "Sophia is on a cigarette break. (Connection Error)" else response.data

        insertChatMessage(message, ChatMessageSender.AI.name)

        return response
    }



    // Scenarios
    suspend fun generateScenarios(transitRulesJson: String, language: GameLanguage, plot: String): ApiResponse<ScenariosWrapper> {
        println("Generating scenarios for language: $transitRulesJson")
        return llmApi.generateScenarios(
            transitRulesJson = transitRulesJson,
            language = language,
            plot = plot
        )
    }

    fun saveStoryLine(storyLine: StoryLine, scenarios: List<Scenario>){
        database.transaction {
            dbQueries.insertStory(
                title = storyLine.title,
                timeCreated = Clock.System.now().toEpochMilliseconds(),
                description = storyLine.description
            )

            dbQueries.deleteScenariosForStory(storyLine.title)

            scenarios.forEach { scenario ->
                val optionsJson = json.encodeToString(scenario.options)

                dbQueries.insertScenario(
                    id = scenario.id,
                    title = scenario.title,
                    description = scenario.description,
                    options_json = optionsJson,
                    correctOptionId = scenario.correctOptionId,
                    nextScenarioId = scenario.nextScenarioId,
                    currentIndexInGame = scenario.currentIndexInGame.toLong(),
                    scenarioTheme = scenario.scenarioTheme.name,
                    parent_story_title = storyLine.title,
                )
            }
        }
    }

    fun loadScenariosForStoryLine(storyTitle: String): List<Scenario> {
        return dbQueries.selectAllScenariosForStory(storyTitle).executeAsList().map { entity ->
            Scenario(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                options = json.decodeFromString(entity.options_json),
                correctOptionId = entity.correctOptionId,
                nextScenarioId = entity.nextScenarioId,
                currentIndexInGame = entity.currentIndexInGame.toInt(),
                scenarioTheme = try {
                    ScenarioTheme.valueOf(entity.scenarioTheme)
                } catch (e: Exception) {
                    ScenarioTheme.DEFAULT
                }
            )
        }
    }


    fun getAllStories(): List<StoryLine> {
        return dbQueries.selectAllStories().executeAsList().map { entity ->
            StoryLine(
                title = entity.title,
                description = entity.description ?: "",
                timeCreated = entity.timeCreated,
            )
        }
    }


    fun deleteStory(storyTitle: String) {
        database.transaction {
            dbQueries.deleteScenariosForStory(storyTitle)
            dbQueries.deleteStory(storyTitle)
        }
    }

}