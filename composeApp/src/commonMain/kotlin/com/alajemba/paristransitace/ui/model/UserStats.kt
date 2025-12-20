package com.alajemba.paristransitace.ui.model

data class UserStats(
    val budget: Double = DEFAULT_BUDGET,
    val morale: Int = DEFAULT_MORALE,
    val legalInfractionsCount: Int = 0
){

    companion object {
        const val DEFAULT_MORALE = 30
        const val DEFAULT_BUDGET = 100.00
    }
}