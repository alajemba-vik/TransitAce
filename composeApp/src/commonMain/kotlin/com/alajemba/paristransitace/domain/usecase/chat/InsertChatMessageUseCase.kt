package com.alajemba.paristransitace.domain.usecase.chat

import com.alajemba.paristransitace.domain.model.MessageSender
import com.alajemba.paristransitace.domain.repository.ChatRepository

class InsertChatMessageUseCase(private val chatRepository: ChatRepository) {
    operator fun invoke(message: String, sender: MessageSender, actions: List<String> = emptyList()) {
        if (message.isBlank()) return
        chatRepository.insertMessage(message, sender, actions)
    }
}