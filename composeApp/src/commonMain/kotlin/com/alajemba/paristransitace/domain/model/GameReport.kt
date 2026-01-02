package com.alajemba.paristransitace.domain.model

data class GameReport(
    val grade: String,
    val summary: String
) {
    companion object {
        val EMPTY = GameReport(grade = "", summary = "")
    }
}