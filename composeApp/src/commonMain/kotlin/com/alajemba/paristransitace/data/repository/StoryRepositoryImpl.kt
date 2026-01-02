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
        return StoryLine(
            id = entity.id,
            title = entity.title,
            description = entity.description ?: "",
            timeCreated = entity.timeCreated,
            initialBudget = entity.initialBudget ?: .0,
            initialMorale = entity.initialMorale?.toInt() ?: 0
        )
    }

    override fun saveStoryLine(storyLine: StoryLine, scenarios: List<Scenario>) {
        localDataSource.runInTransaction {
            val storyId = localDataSource.insertStory(
                title = storyLine.title,
                description = storyLine.description,
                initialBudget = storyLine.initialBudget,
                initialMorale = storyLine.initialMorale.toLong()
            )

            localDataSource.deleteScenariosForStory(storyId)

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
                    parentStoryId = storyId
                )
            }
        }
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

    override fun clearAllStoriesAndScenarios() {
        localDataSource.clearAllStoriesAndScenarios()
    }
}