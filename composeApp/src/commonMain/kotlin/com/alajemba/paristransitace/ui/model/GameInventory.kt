package com.alajemba.paristransitace.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class GameInventory(
    val name: String,
    val description: String,
    val imageUrl: String
)