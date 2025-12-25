package com.alajemba.paristransitace.model

import com.alajemba.paristransitace.ui.model.GameSetup

sealed class GameSetting<T>(
    val key: String
) {

    open fun toValue(value: String?): T? = null

    data object Language : GameSetting<GameSetup.GameLanguage>("language") {
        override fun toValue(value: String?): GameSetup.GameLanguage? =
            when (value?.uppercase()) {
                GameSetup.GameLanguage.ENGLISH.name -> GameSetup.GameLanguage.ENGLISH
                GameSetup.GameLanguage.FRENCH.name -> GameSetup.GameLanguage.FRENCH
                else -> null
            }
    }
    data object PlayerName : GameSetting<String>("player_name")

    companion object {
        fun all(): List<GameSetting<*>> = listOf(
            Language,
            PlayerName,
        )
    }
}