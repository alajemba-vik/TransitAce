package com.alajemba.paristransitace.domain.usecase.app

import com.alajemba.paristransitace.domain.repository.ChatRepository
import com.alajemba.paristransitace.domain.repository.SettingsRepository
import com.alajemba.paristransitace.domain.repository.StoryRepository

class ClearAppStateUseCase(
    private val settingsRepository: SettingsRepository,
    private val chatRepository: ChatRepository,
    private val storyRepository: StoryRepository
) {
    operator fun invoke() {
        chatRepository.clearAllMessages()
        settingsRepository.clearAllSettings()
        storyRepository.clearAllStoriesAndScenarios()
    }
}