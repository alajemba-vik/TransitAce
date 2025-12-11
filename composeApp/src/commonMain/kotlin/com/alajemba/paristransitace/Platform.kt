package com.alajemba.paristransitace

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform