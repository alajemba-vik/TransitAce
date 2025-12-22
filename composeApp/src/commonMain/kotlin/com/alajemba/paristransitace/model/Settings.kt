package com.alajemba.paristransitace.model

import com.alajemba.paristransitace.ui.model.GameSetup

sealed class GameSetting<T>(
    val key: String,
    val defaultValue: T
) {

    open fun toValue(value: String): T = defaultValue

    data object Language : GameSetting<GameSetup.GameLanguage>("language", GameSetup.GameLanguage.ENGLISH) {
        override fun toValue(value: String): GameSetup.GameLanguage =
            when (value.uppercase()) {
                GameSetup.GameLanguage.ENGLISH.name -> GameSetup.GameLanguage.ENGLISH
                GameSetup.GameLanguage.FRENCH.name -> GameSetup.GameLanguage.FRENCH
                else -> defaultValue
            }
    }
    data object PlayerName : GameSetting<String>("player_name", "")

    companion object {
        fun all(): List<GameSetting<*>> = listOf(
            Language,
            PlayerName,
        )

        fun fromKey(key: String): GameSetting<*>? =
            all().find { it.key == key }
    }
}