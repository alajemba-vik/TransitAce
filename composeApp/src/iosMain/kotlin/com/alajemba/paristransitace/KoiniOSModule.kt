package com.alajemba.paristransitace

import com.alajemba.paristransitace.db.DatabaseDriverFactory
import com.alajemba.paristransitace.db.IOSDatabaseDriverFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun initKoin(){
    startKoin {
        modules(
            commonModule + iOSModule
        )
    }
}


val iOSModule = module {
    single<DatabaseDriverFactory>{ IOSDatabaseDriverFactory() }
}