package com.alajemba.paristransitace.ui.model

data class ChatUiModel(
    val sender: ChatMessageSender,
    val message: String
)


enum class ChatMessageSender {
    AI, USER
}