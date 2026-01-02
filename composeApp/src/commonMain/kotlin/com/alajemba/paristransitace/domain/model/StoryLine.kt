package com.alajemba.paristransitace.domain.model

data class StoryLine(
    val id: Long?,
    val title: String,
    val description: String,
    val timeCreated: Long?,
    val initialBudget: Double,
    val initialMorale: Int
) {
    companion object {
        val EMPTY = StoryLine(
            id = null,
            title = "",
            description = "",
            timeCreated = null,
            initialBudget = 0.0,
            initialMorale = 0
        )
    }
}