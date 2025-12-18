package com.alajemba.paristransitace.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alajemba.paristransitace.ui.pages.GameScreen
import com.alajemba.paristransitace.ui.pages.LandingScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
    ) {
        composable<HomeRoute> {
            LandingScreen(
                chatViewModel = koinViewModel(),
                userViewModel = koinViewModel(),
                onStartGame = {
                    navController.navigate(GameRoute)
                }
            )
        }

        composable<GameRoute> {
            GameScreen(
                userViewModel = koinViewModel(),
                gameViewModel = koinViewModel(),
                onGameOver = { _, _ -> }
            )
        }
    }
}


@Serializable
object HomeRoute


@Serializable
object GameRoute
