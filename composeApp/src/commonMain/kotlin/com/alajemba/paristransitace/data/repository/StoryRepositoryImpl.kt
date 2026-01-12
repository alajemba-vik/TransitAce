package com.alajemba.paristransitace.data.repository

import com.alajemba.paristransitace.data.remote.model.ScenarioOptionResponse
import com.alajemba.paristransitace.data.local.LocalDataSource
import com.alajemba.paristransitace.data.mapper.toDomain
import com.alajemba.paristransitace.data.mapper.toResponse
import com.alajemba.paristransitace.data.remote.RemoteDataSource
import com.alajemba.paristransitace.domain.model.GameLanguage
import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.ScenarioTheme
import com.alajemba.paristransitace.domain.model.ScenariosWrapper
import com.alajemba.paristransitace.domain.model.StoryLine
import com.alajemba.paristransitace.domain.repository.StoryRepository
import com.alajemba.paristransitace.utils.debugLog
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class StoryRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : StoryRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun generateScenarios(
        transitRulesJson: String,
        language: GameLanguage,
        plot: String
    ): Result<ScenariosWrapper> {
        return remoteDataSource.generateScenarios(transitRulesJson, language, plot)
    }

    override fun getAllStories(): List<StoryLine> {
        return localDataSource.getAllStories().map { entity ->
            StoryLine(
                id = entity.id,
                title = entity.title,
                description = entity.description ?: "",
                timeCreated = entity.timeCreated,
                initialBudget = entity.initialBudget ?: .0,
                initialMorale = entity.initialMorale?.toInt() ?: 0
            )
        }
    }

    override fun getStory(storyId: Long): StoryLine ? {
        val entity = localDataSource.getStoryById(storyId) ?: return null
        return entity.toDomain()
    }

    override fun getStoryByTitle(storyTitle: String): StoryLine? {
        val entity = localDataSource.getStoryByTitle(storyTitle) ?: return null
        return entity.toDomain()
    }

    override fun saveStoryLine(storyLine: StoryLine, scenarios: List<Scenario>): Long {
        debugLog("Saving story with id '${storyLine.id}' with ${scenarios.size} scenarios.")

        // Only check for duplicate titles when creating a new story (id is null)
        val uniqueTitle = if (storyLine.id == null) {
            val baseTitle = storyLine.title
            val existingCount = countStoriesByTitlePattern(baseTitle)
            if (existingCount > 0) {
                "$baseTitle ${existingCount + 1}"
            } else {
                baseTitle
            }
        } else {
            storyLine.title
        }

        var savedStoryId: Long = storyLine.id ?: -1L

        localDataSource.runInTransaction {
            savedStoryId = localDataSource.insertStory(
                title = uniqueTitle,
                description = storyLine.description,
                initialBudget = storyLine.initialBudget,
                initialMorale = storyLine.initialMorale.toLong(),
                id = storyLine.id
            )

            localDataSource.deleteScenariosForStory(savedStoryId)

            scenarios.forEach { scenario ->
                val optionsJson = json.encodeToString(
                    ListSerializer(ScenarioOptionResponse.serializer()),
                    scenario.options.map { it.toResponse() }
                )

                localDataSource.insertScenario(
                    id = scenario.id,
                    title = scenario.title,
                    description = scenario.description,
                    optionsJson = optionsJson,
                    correctOptionId = scenario.correctOptionId,
                    nextScenarioId = scenario.nextScenarioId,
                    currentIndexInGame = scenario.currentIndexInGame.toLong(),
                    scenarioTheme = scenario.scenarioTheme.name,
                    parentStoryId = savedStoryId
                )
            }
        }

        return savedStoryId
    }

    override fun loadScenariosForStory(storyId: Long): List<Scenario> {
        return localDataSource.getScenariosForStory(storyId).map { entity ->
            val options = json.decodeFromString(
                ListSerializer(ScenarioOptionResponse.serializer()),
                entity.options_json
            ).map { it.toDomain() }

            Scenario(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                options = options,
                correctOptionId = entity.correctOptionId,
                nextScenarioId = entity.nextScenarioId,
                currentIndexInGame = entity.currentIndexInGame.toInt(),
                scenarioTheme = ScenarioTheme.fromKey(entity.scenarioTheme)
            )
        }
    }

    override fun deleteStory(storyId: Long) {
        localDataSource.runInTransaction {
            localDataSource.deleteScenariosForStory(storyId)
            localDataSource.deleteStory(storyId)
        }
    }

    override fun deleteStoryByTitle(title: String): Boolean {
        val story = localDataSource.getStoryByTitle(title) ?: return false
        deleteStory(story.id)
        return true
    }

    override fun countStoriesByTitlePattern(title: String): Long {
        return localDataSource.countStoriesByTitlePattern(title)
    }

    override fun clearAllStoriesAndScenarios() {
        localDataSource.clearAllStoriesAndScenarios()
    }
}