package com.alajemba.paristransitace.ui.mapper

import com.alajemba.paristransitace.domain.model.ChatMessage
import com.alajemba.paristransitace.domain.model.MessageSender
import com.alajemba.paristransitace.ui.model.ChatMessageAction
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.model.ChatUiModel

fun ChatMessage.toChatUiModel() = ChatUiModel(
    id = id,
    sender = when (sender) {
        MessageSender.AI -> ChatMessageSender.AI
        MessageSender.USER -> ChatMessageSender.USER
    },
    message = message,
    selectedAction = selectedActionName?.takeIf { it.isNotBlank() }?.let { ChatMessageAction.fromString(it) },
    actions = actions.mapNotNull { ChatMessageAction.fromString(it) }
)

fun ChatMessageSender.toDomain(): MessageSender = when (this) {
    ChatMessageSender.AI -> MessageSender.AI
    ChatMessageSender.USER -> MessageSender.USER
}