package com.alajemba.paristransitace.ui.model

data class GameAlert (
    val title: String,
    val message: String,
    val unread: Boolean = true
)