package com.alajemba.paristransitace.domain.model

data class ChatMessage(
    val id: Long,
    val sender: MessageSender,
    val message: String,
    val timeSent: Long,
    val actions: List<String>,
    val selectedActionName: String?
)

enum class MessageSender {
    AI, USER
}