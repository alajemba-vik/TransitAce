package com.alajemba.paristransitace.domain.usecase.app

import com.alajemba.paristransitace.domain.repository.ChatRepository
import com.alajemba.paristransitace.domain.repository.GameSessionRepository
import com.alajemba.paristransitace.domain.repository.SettingsRepository
import com.alajemba.paristransitace.domain.repository.StoryRepository

class ClearAppStateUseCase(
    private val settingsRepository: SettingsRepository,
    private val chatRepository: ChatRepository,
    private val storyRepository: StoryRepository,
    private val gameSessionRepository: GameSessionRepository
) {
    operator fun invoke(fullClear: Boolean = false) {
        chatRepository.clearAllMessages()
        settingsRepository.clearAllSettings()
        gameSessionRepository.clearAll()
        if (fullClear){
            storyRepository.clearAllStoriesAndScenarios()
        }
    }
}