package com.alajemba.paristransitace.domain.repository

import com.alajemba.paristransitace.domain.model.ChatMessage
import com.alajemba.paristransitace.domain.model.MessageSender
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getAllMessages(): Flow<List<ChatMessage>>
    fun getAllMessagesSync(): List<ChatMessage>
    fun insertMessage(message: String, sender: MessageSender, actions: List<String> = emptyList())
    suspend fun updateMessageSelectedAction(selectedActionName: String, messageId: Long)
    fun clearAllMessages()
}