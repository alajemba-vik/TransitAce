package com.alajemba.paristransitace

import com.alajemba.paristransitace.db.DatabaseDriverFactory
import com.alajemba.paristransitace.db.ParisTransitDatabase
import com.alajemba.paristransitace.network.LLMApi
import com.alajemba.paristransitace.ui.viewmodels.ChatViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
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

    single {
        LLMApi(get())
    }

    single {
        ParisTransitDatabase(get<DatabaseDriverFactory>().createDriver())
    }

    single {
        ChatSDK(get(), get())
    }


    viewModel {
        ChatViewModel(get())
    }
}

