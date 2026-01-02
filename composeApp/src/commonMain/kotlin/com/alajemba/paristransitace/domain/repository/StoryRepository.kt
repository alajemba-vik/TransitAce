package com.alajemba.paristransitace.domain.repository

import com.alajemba.paristransitace.domain.model.GameLanguage
import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.ScenariosWrapper
import com.alajemba.paristransitace.domain.model.StoryLine

interface StoryRepository {

    suspend fun generateScenarios(
        transitRulesJson: String,
        language: GameLanguage,
        plot: String
    ): Result<ScenariosWrapper>

    fun getStory(storyId: Long): StoryLine?

    fun getAllStories(): List<StoryLine>
    fun saveStoryLine(storyLine: StoryLine, scenarios: List<Scenario>)
    fun loadScenariosForStory(storyId: Long): List<Scenario>
    fun deleteStory(storyId: Long)

    fun clearAllStoriesAndScenarios()
}