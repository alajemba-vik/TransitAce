package com.alajemba.paristransitace.db

import com.alajemba.paristransitace.entity.ChatMessageEntity

internal class AppDatabase(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = ParisTransitDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.parisTransitDatabaseQueries

    internal fun getAllChatMessages(): List<ChatMessageEntity> {
        return dbQuery.selectAllChatMessages({ id, sender, message, timestamp ->
            ChatMessageEntity(
                sender = sender,
                message = message,
                timeSent = timestamp ?: 0L
            )
        }).executeAsList()
    }

    internal fun clearChat() {
        dbQuery.removeAllChatMessages()
    }

    internal fun insertChatMessage(message: ChatMessageEntity) {
        dbQuery.insertChatMessage(
            id = null,
            sender = message.sender,
            message = message.message,
            timestamp = message.timeSent
        )
    }
}