package com.alajemba.paristransitace.domain.model

data class UserStats(
    val budget: Double = 100.0,
    val morale: Int = 30,
    val legalInfractionsCount: Int = 0
)