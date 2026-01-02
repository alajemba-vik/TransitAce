package com.alajemba.paristransitace.data.mapper

import com.alajemba.paristransitace.domain.model.ChatMessage
import com.alajemba.paristransitace.domain.model.MessageSender

fun chatMessageFromDb(
    id: Long,
    sender: String,
    message: String,
    timestamp: Long?,
    actions: String?,
    selectedActionName: String?
) = ChatMessage(
    id = id,
    sender = MessageSender.entries.find { it.name == sender } ?: MessageSender.AI,
    message = message,
    timeSent = timestamp ?: 0L,
    actions = actions?.split(",")?.map { it.trim() } ?: emptyList(),
    selectedActionName = selectedActionName
)