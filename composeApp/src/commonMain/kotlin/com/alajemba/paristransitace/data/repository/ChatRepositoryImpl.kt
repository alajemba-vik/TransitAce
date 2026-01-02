package com.alajemba.paristransitace.data.repository

import com.alajemba.paristransitace.data.local.LocalDataSource
import com.alajemba.paristransitace.data.mapper.chatMessageFromDb
import com.alajemba.paristransitace.domain.model.ChatMessage
import com.alajemba.paristransitace.domain.model.MessageSender
import com.alajemba.paristransitace.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(
    private val localDataSource: LocalDataSource
) : ChatRepository {

    override fun getAllMessages(): Flow<List<ChatMessage>> {
        return localDataSource.getAllChatMessages().map { list ->
            list.map { entity ->
                chatMessageFromDb(
                    id = entity.id,
                    sender = entity.sender,
                    message = entity.message,
                    timestamp = entity.timestamp,
                    actions = entity.actions,
                    selectedActionName = entity.selectedActionName
                )
            }
        }
    }

    override fun getAllMessagesSync(): List<ChatMessage> {
        return localDataSource.getAllChatMessagesSync().map { entity ->
            chatMessageFromDb(
                id = entity.id,
                sender = entity.sender,
                message = entity.message,
                timestamp = entity.timestamp,
                actions = entity.actions,
                selectedActionName = entity.selectedActionName
            )
        }
    }

    override fun insertMessage(message: String, sender: MessageSender, actions: List<String>) {
        localDataSource.insertChatMessage(
            message = message,
            sender = sender.name,
            actions = if (actions.isNotEmpty()) actions.joinToString(",") else null
        )
    }

    override suspend fun updateMessageSelectedAction(selectedActionName: String, messageId: Long) {
        localDataSource.updateChatMessageSelectedAction(selectedActionName, messageId)
    }

    override fun clearAllMessages() {
        localDataSource.clearAllChatMessages()
    }
}