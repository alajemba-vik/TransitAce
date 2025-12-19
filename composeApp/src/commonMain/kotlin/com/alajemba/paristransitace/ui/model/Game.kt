package com.alajemba.paristransitace.ui.model

import com.alajemba.paristransitace.ui.model.GameLanguage.ENGLISH

data class GameSetup(
    val language: GameLanguage,
    val name: String
) {

    val isSetupComplete: Boolean get() = language != GameLanguage.UNDEFINED && name.isNotBlank()

    val isOnLanguageStep: Boolean get() = language == GameLanguage.UNDEFINED
    val isOnNameStep: Boolean get() = language != GameLanguage.UNDEFINED && name.isBlank()

    val isEnglish: Boolean get() = language == ENGLISH

    companion object {
        val EMPTY = GameSetup(GameLanguage.UNDEFINED, "")
    }
}

enum class GameLanguage {
    ENGLISH, FRENCH, UNDEFINED;
}