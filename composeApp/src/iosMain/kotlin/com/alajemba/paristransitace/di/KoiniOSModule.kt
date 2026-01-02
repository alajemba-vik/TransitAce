package com.alajemba.paristransitace.di

import com.alajemba.paristransitace.db.DatabaseDriverFactory
import com.alajemba.paristransitace.db.IOSDatabaseDriverFactory
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