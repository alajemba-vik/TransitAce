package com.alajemba.paristransitace.ui.model

data class GameReport(
    val grade: String,
    val summary: String
) {
    companion object {
        val EMPTY = GameReport(
            grade = "",
            summary = ""
        )
    }
}