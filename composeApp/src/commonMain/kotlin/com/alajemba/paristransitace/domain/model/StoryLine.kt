package com.alajemba.paristransitace.domain.model

data class StoryLine(
    val id: Long?,
    val title: String,
    val description: String,
    val timeCreated: Long?,
    val initialBudget: Double,
    val initialMorale: Int
)