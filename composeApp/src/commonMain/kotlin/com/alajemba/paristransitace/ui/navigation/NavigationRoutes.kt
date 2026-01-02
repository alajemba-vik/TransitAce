package com.alajemba.paristransitace.ui.navigation

import kotlinx.serialization.Serializable

interface NavigationRoute {
    val label: String
}

@Serializable
object LandingRoute : NavigationRoute {
    override val label = "landing"
}


@Serializable
object HomeRoute : NavigationRoute {
    override val label = "home"
}

@Serializable
object GameRoute : NavigationRoute {
    override val label = "game"
}
