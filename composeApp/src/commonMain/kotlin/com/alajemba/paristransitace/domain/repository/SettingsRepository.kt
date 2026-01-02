package com.alajemba.paristransitace.domain.repository

import com.alajemba.paristransitace.domain.model.GameLanguage
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getLanguage(): Flow<GameLanguage?>
    fun saveLanguage(language: GameLanguage)
    suspend fun getPlayerName(): String?
    fun savePlayerName(name: String)
    fun clearAllSettings()

    fun saveSessionCheckpoint(checkpointLabel: String)

    suspend fun lastSessionCheckpoint(): String?
}