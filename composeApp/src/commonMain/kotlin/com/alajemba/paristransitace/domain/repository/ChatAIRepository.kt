package com.alajemba.paristransitace.domain.repository

import com.alajemba.paristransitace.domain.model.ChatMessage
import com.alajemba.paristransitace.domain.model.StoryLine

interface ChatAIRepository {
    suspend fun sendMessage(
        message: String,
        chatHistory: List<ChatMessage>,
        storyLines: List<StoryLine>,
        gameContext: String?
    ): Result<ChatAIResponse>
}

sealed class ChatAIResponse {
    data class TextResponse(val content: String) : ChatAIResponse()
    data class ExecuteCommand(val command: String, val arg: String? = null) : ChatAIResponse()
    data object NoResponse : ChatAIResponse()
}