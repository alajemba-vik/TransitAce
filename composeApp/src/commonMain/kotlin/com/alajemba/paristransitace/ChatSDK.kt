package com.alajemba.paristransitace

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.alajemba.paristransitace.db.ParisTransitDatabase
import com.alajemba.paristransitace.entity.ChatMessageEntity
import com.alajemba.paristransitace.network.LLMApi
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

internal class ChatSDK(
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

    suspend fun insertChatMessage(message: String, sender: ChatMessageSender) {
        dbQuery.insertChatMessage(
            id = null,
            sender = sender.name,
            message = message,
            timestamp = Clock.System.now().toEpochMilliseconds(),
        )
    }




}