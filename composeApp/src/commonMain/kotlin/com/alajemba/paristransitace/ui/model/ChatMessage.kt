package com.alajemba.paristransitace.ui.model

class ChatMessage(
    val sender: ChatMessageSender,
    val message: String
)


enum class ChatMessageSender {
    AI, USER
}