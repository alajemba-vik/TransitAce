package com.alajemba.paristransitace

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.alajemba.paristransitace.db.ParisTransitDatabase
import com.alajemba.paristransitace.entity.ChatMessageEntity
import com.alajemba.paristransitace.network.LLMApi
import com.alajemba.paristransitace.network.models.ApiResponse
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.model.GameSetup.GameLanguage
import com.alajemba.paristransitace.ui.model.Scenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

internal class TransitAceSDK(
    private val database: ParisTransitDatabase,
    private val llmApi: LLMApi
) {
    val queries = database.parisTransitDatabaseQueries

    private val dbQuery = database.parisTransitDatabaseQueries

    internal fun getAllChatMessages(): Flow<List<ChatMessageEntity>> {
        return dbQuery.selectAllChatMessages({ id, sender, message, timestamp ->
            ChatMessageEntity(
                sender = sender,
                message = message,
                timeSent = timestamp ?: 0L
            )
        }).asFlow().mapToList(Dispatchers.Default)
    }

    internal fun clearChat() {
        dbQuery.removeAllChatMessages()
    }

    suspend fun sendChatMessage(message: String, sender: String): ApiResponse<*> {
        dbQuery.insertChatMessage(
            id = null,
            sender = sender,
            message = message,
            timestamp = Clock.System.now().toEpochMilliseconds(),
        )

        val response = llmApi.sendChatMessage(message)

        println(response.data)

        val message = if (response.data.isNullOrBlank()) "Sophia is on a cigarette break. (Connection Error)" else response.data

        dbQuery.insertChatMessage(
            id = null,
            sender = ChatMessageSender.AI.name,
            message = message,
            timestamp = Clock.System.now().toEpochMilliseconds(),
        )

        return response
    }

    suspend fun generateScenarios(transitRulesJson: String, language: GameLanguage): ApiResponse<List<Scenario>> {
        return llmApi.generateScenario(
            transitRulesJson = transitRulesJson,
            language = language
        )
    }





}