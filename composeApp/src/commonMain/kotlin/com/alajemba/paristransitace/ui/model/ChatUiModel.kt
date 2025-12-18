package com.alajemba.paristransitace.ui.model

class ChatUiModel(
    val sender: ChatMessageSender,
    val message: String
)


enum class ChatMessageSender {
    AI, USER
}