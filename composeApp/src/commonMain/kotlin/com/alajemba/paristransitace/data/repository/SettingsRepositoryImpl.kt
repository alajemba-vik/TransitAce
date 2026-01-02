package com.alajemba.paristransitace.data.repository

import com.alajemba.paristransitace.data.local.LocalDataSource
import com.alajemba.paristransitace.domain.model.GameLanguage
import com.alajemba.paristransitace.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val localDataSource: LocalDataSource
) : SettingsRepository {

    companion object {
        private const val KEY_LANGUAGE = "language"
        private const val KEY_PLAYER_NAME = "player_name"
        private const val KEY_LAST_SESSION_CHECKPOINT = "initial_load"
    }

    override fun getLanguage(): Flow<GameLanguage?> {
        return localDataSource.getSetting(KEY_LANGUAGE).map { value ->
            when (value?.uppercase()) {
                GameLanguage.ENGLISH.name -> GameLanguage.ENGLISH
                GameLanguage.FRENCH.name -> GameLanguage.FRENCH
                else -> null
            }
        }
    }

    override fun saveLanguage(language: GameLanguage) {
        localDataSource.saveSetting(KEY_LANGUAGE, language.name)
    }

    override suspend fun getPlayerName(): String? {
        return localDataSource.getSettingSync(KEY_PLAYER_NAME)
    }

    override fun savePlayerName(name: String) {
        localDataSource.saveSetting(KEY_PLAYER_NAME, name)
    }

    override fun clearAllSettings() {
        localDataSource.clearAllSettings()
    }

    override fun saveSessionCheckpoint(checkpointLabel: String) {
        localDataSource.saveSetting(KEY_LAST_SESSION_CHECKPOINT, checkpointLabel)
    }

    override suspend fun lastSessionCheckpoint(): String? {
        return  localDataSource.getSettingSync(KEY_LAST_SESSION_CHECKPOINT)
    }
}