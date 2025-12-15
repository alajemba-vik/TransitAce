package com.alajemba.paristransitace.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    @SerialName("sender")
    val sender: String,
    @SerialName("message")
    val message: String,
    @SerialName("timestamp")
    val timeSent: Long
)