package com.alajemba.paristransitace.domain.usecase.chat

import com.alajemba.paristransitace.domain.model.MessageSender
import com.alajemba.paristransitace.domain.repository.ChatAIRepository
import com.alajemba.paristransitace.domain.repository.ChatAIResponse
import com.alajemba.paristransitace.domain.repository.ChatRepository
import com.alajemba.paristransitace.domain.repository.StoryRepository
import com.alajemba.paristransitace.utils.debugLog

class SendChatMessageUseCase(
    private val chatRepository: ChatRepository,
    private val chatAIRepository: ChatAIRepository,
    private val storyRepository: StoryRepository
) {

    suspend operator fun invoke(
        message: String,
        isEnglish: Boolean,
        gameContext: String? = null
    ): Result<ChatAIResponse> {
        chatRepository.insertMessage(message, MessageSender.USER)

        val chatHistory = chatRepository.getAllMessagesSync()
        val storyLines = storyRepository.getAllStories()

        debugLog("Sending message to ChatAIRepository: $message")
        debugLog(" with gameContext: ${chatHistory.joinToString { it.message }}")

        val result = chatAIRepository.sendChatMessage(
            chatHistory = chatHistory,
            storyLines = storyLines,
            gameContext = gameContext
        )

        result.onSuccess { response ->
            if (response is ChatAIResponse.TextResponse) {
                chatRepository.insertMessage(response.content, MessageSender.AI)
            }
        }

        result.onFailure {
            val errorMessage = if (!isEnglish) {
                "Une erreur est survenue. Sophia pourrait être en pause cigarette. (Erreur de connexion)"
            } else {
                "There was an error. Sophia might be on a cigarette break. (Connection Error)"
            }
            chatRepository.insertMessage(errorMessage, MessageSender.AI)
        }

        return result
    }
}