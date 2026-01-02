package com.alajemba.paristransitace.data.remote

import com.alajemba.paristransitace.domain.model.ChatMessage
import com.alajemba.paristransitace.domain.model.GameLanguage
import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.ScenariosWrapper
import com.alajemba.paristransitace.domain.model.StoryLine
import com.alajemba.paristransitace.domain.repository.ChatAIResponse

interface RemoteDataSource {
    suspend fun generateScenarios(
        transitRulesJson: String,
        language: GameLanguage,
        plot: String
    ): Result<ScenariosWrapper>

    /**
     *  The most recent message should be the last in the chatHistory list.
     */
    suspend fun sendChatMessage(
        chatHistory: List<ChatMessage>,
        storyLines: List<StoryLine>,
        gameContext: String?
    ): Result<ChatAIResponse>
}