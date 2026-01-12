package com.alajemba.paristransitace.data.local

import ai.koog.utils.io.SuitableForIO
import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.QueryResult
import com.alajemba.paristransitace.db.ParisTransitDatabase
import com.alajemba.paristransitace.db.SavedGameState
import com.alajemba.paristransitace.utils.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class LocalDataSource(private val database: ParisTransitDatabase) {

    private val queries = database.parisTransitDatabaseQueries

    // Chat Messages
    fun getAllChatMessages() = queries.selectAllChatMessages().asFlow().mapToList(Dispatchers.Default)

    fun getAllChatMessagesSync() = queries.selectAllChatMessages().executeAsList()

    fun insertChatMessage(
        message: String,
        sender: String,
        actions: String?,
        selectedActionName: String? = null
    ) {
        queries.insertChatMessage(
            id = null,
            sender = sender,
            message = message,
            timestamp = Clock.System.now().toEpochMilliseconds(),
            actions = actions,
            selectedActionName = selectedActionName
        )
    }

    fun updateChatMessageSelectedAction(selectedActionName: String, messageId: Long) {
        queries.updateChatMessageSelectedAction(
            selectedActionName = selectedActionName,
            id = messageId
        )
    }

    fun clearAllChatMessages() {
        queries.removeAllChatMessages()
    }

    // Settings
    fun getSetting(key: String): Flow<String?> {
        return queries.getSetting(key).asFlow().mapToOneOrNull(Dispatchers.Default)
    }

    suspend fun getSettingSync(key: String): String? {
        return withContext(Dispatchers.SuitableForIO) {
            try {
                queries.getSetting(key).executeAsOneOrNull()
            } catch (_: IllegalStateException) {
                null
            }
        }
    }

    fun saveSetting(key: String, value: String) {
        queries.insertSetting(key, value)
    }

    fun clearAllSettings() {
        queries.removeAllSettings()
    }

    // Stories
    fun getAllStories() = queries.selectAllStories().executeAsList()

    fun getStoryById(storyId: Long) = queries.getStoryById(storyId).executeAsOneOrNull()

    fun getStoryByTitle(storyTitle: String) = queries.getStoryByTitle(storyTitle).executeAsOneOrNull()

    fun countStoriesByTitlePattern(title: String): Long = queries.countStoriesByTitlePattern(title).executeAsOne()

    fun insertStory(
        title: String,
        description: String,
        initialBudget: Double,
        initialMorale: Long,
        id: Long? = null
    ): Long {
        queries.insertStory(
            id = id,
            title = title,
            description = description,
            timeCreated = Clock.System.now().toEpochMilliseconds(),
            initialBudget = initialBudget,
            initialMorale = initialMorale
        )
        return queries.lastInsertedRowId().executeAsOne()
    }

    fun deleteStory(storyId: Long) {
        queries.deleteStory(storyId)
    }

    fun clearAllStoriesAndScenarios() {
        database.transaction {
            queries.removeAllScenarios()
            queries.removeAllStories()
        }
    }

    // Scenarios
    fun getScenariosForStory(storyId: Long) = queries.selectAllScenariosForStory(storyId).executeAsList()

    fun insertScenario(
        id: String,
        title: String,
        description: String,
        optionsJson: String,
        correctOptionId: String,
        nextScenarioId: String?,
        currentIndexInGame: Long,
        scenarioTheme: String,
        parentStoryId: Long
    ) {
        queries.insertScenario(
            id = id,
            title = title,
            description = description,
            options_json = optionsJson,
            correctOptionId = correctOptionId,
            nextScenarioId = nextScenarioId,
            currentIndexInGame = currentIndexInGame,
            scenarioTheme = scenarioTheme,
            parent_story_id = parentStoryId
        )
    }

    fun deleteScenariosForStory(storyId: Long) {
        queries.deleteScenariosForStory(storyId)
    }

    // Saved Game State
    fun saveGameState(
        storyId: Long,
        currentScenarioIndex: Int,
        budget: Double,
        morale: Int,
        legalInfractionsCount: Int
    ) {
        debugLog("Saving game state for story id '$storyId' at scenario index '$currentScenarioIndex'")
        queries.insertOrUpdateSavedGame(
            storyId = storyId,
            currentScenarioIndex = currentScenarioIndex.toLong(),
            budget = budget,
            morale = morale.toLong(),
            legalInfractionsCount = legalInfractionsCount.toLong(),
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
    }

    fun getSavedGame(): SavedGameState? = queries.getSavedGame().executeAsOneOrNull()

    fun deleteSavedGame(): QueryResult<Long> = queries.deleteSavedGame()

    fun hasSavedGame(): Boolean = queries.hasSavedGame().executeAsOne() > 0

    fun runInTransaction(block: () -> Unit) {
        database.transaction { block() }
    }
}