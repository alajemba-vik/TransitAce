package com.alajemba.paristransitace.domain.model

data class UserStats(
    val budget: Double,
    val morale: Int,
    val legalInfractionsCount: Int
){
    companion object {
        val EMPTY = UserStats(0.0, 0, 0)
    }
}