package com.alajemba.paristransitace.data.repository

import com.alajemba.paristransitace.data.remote.RemoteDataSource
import com.alajemba.paristransitace.domain.model.ChatMessage
import com.alajemba.paristransitace.domain.model.StoryLine
import com.alajemba.paristransitace.domain.repository.ChatAIRepository
import com.alajemba.paristransitace.domain.repository.ChatAIResponse

class ChatAIRepositoryImpl(
    private val remoteDataSource: RemoteDataSource
) : ChatAIRepository {

    override suspend fun sendChatMessage(
        chatHistory: List<ChatMessage>,
        storyLines: List<StoryLine>,
        gameContext: String?
    ): Result<ChatAIResponse> {
        return remoteDataSource.sendChatMessage(
            chatHistory = chatHistory,
            storyLines = storyLines,
            gameContext = gameContext
        )
    }
}