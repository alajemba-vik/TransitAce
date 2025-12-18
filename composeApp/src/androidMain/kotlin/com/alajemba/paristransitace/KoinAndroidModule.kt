package com.alajemba.paristransitace

import com.alajemba.paristransitace.db.AndroidDatabaseDriverFactory
import com.alajemba.paristransitace.db.DatabaseDriverFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        useAlternativeNames = false
                    }
                )
            }
        }
    }
    single<DatabaseDriverFactory>{ AndroidDatabaseDriverFactory(androidContext()) }

}