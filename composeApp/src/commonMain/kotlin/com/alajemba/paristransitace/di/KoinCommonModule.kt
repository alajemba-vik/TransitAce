package com.alajemba.paristransitace.di

import com.alajemba.paristransitace.data.local.DefaultScenariosProvider
import com.alajemba.paristransitace.data.local.LocalDataSource
import com.alajemba.paristransitace.data.remote.LLMRemoteDataSource
import com.alajemba.paristransitace.data.remote.RemoteDataSource
import com.alajemba.paristransitace.data.repository.*
import com.alajemba.paristransitace.db.DatabaseDriverFactory
import com.alajemba.paristransitace.db.ParisTransitDatabase
import com.alajemba.paristransitace.domain.repository.*
import com.alajemba.paristransitace.domain.usecase.app.ClearAppStateUseCase
import com.alajemba.paristransitace.domain.usecase.chat.InsertChatMessageUseCase
import com.alajemba.paristransitace.domain.usecase.chat.SendChatMessageUseCase
import com.alajemba.paristransitace.domain.usecase.game.CalculateFinalGradeUseCase
import com.alajemba.paristransitace.ui.viewmodels.ChatViewModel
import com.alajemba.paristransitace.ui.viewmodels.GameViewModel
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val commonModule = module {
    single {
       HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    useAlternativeNames = false
                })
            }
        }
    }

    // Add in Data Sources section
    single { DefaultScenariosProvider() }

    // Data Sources
    single { LocalDataSource(get()) }

    single<RemoteDataSource> { LLMRemoteDataSource(get(), get()) }

    single<UserRepository> { UserRepositoryImpl(get()) }

    single<ChatRepository> { ChatRepositoryImpl(get()) }

    single<ChatAIRepository> { ChatAIRepositoryImpl(get()) }

    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    single<StoryRepository> { StoryRepositoryImpl(get(), get()) }

    // Game Session - single source of truth for current game state
    single<GameSessionRepository> { GameSessionRepositoryImpl(get(), get()) }

    // Use Cases
    // General Use Cases
    factory { ClearAppStateUseCase(get(), get(), get(), get()) }

    // Chat Use Cases
    factory { InsertChatMessageUseCase(get()) }

    factory { SendChatMessageUseCase(get(), get(), get()) }
    // Settings Use Cases

    // Scenario Use Cases
    // Game Use Cases
    factory { CalculateFinalGradeUseCase() }


    single {
        ParisTransitDatabase(get<DatabaseDriverFactory>().createDriver())
    }

    viewModelOf(::ChatViewModel)
    viewModelOf(::UserViewModel)
    viewModelOf(::GameViewModel)

}

