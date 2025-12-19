package com.alajemba.paristransitace.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alajemba.paristransitace.ui.pages.GameScreen
import com.alajemba.paristransitace.ui.pages.HomeScreen
import com.alajemba.paristransitace.ui.pages.LandingScreen
import com.alajemba.paristransitace.ui.viewmodels.ChatViewModel
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.viewmodel.sharedKoinViewModel


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val userViewModel = koinViewModel<UserViewModel>()
    val chatViewModel = koinViewModel<ChatViewModel>()

    NavHost(
        navController = navController,
        startDestination = LandingRoute,
    ) {
        composable<LandingRoute> {
            LandingScreen(
                onStartGame = {
                    navController.navigate(HomeRoute)
                }
            )
        }

        composable<HomeRoute> {
            HomeScreen(
                chatViewModel = chatViewModel,
                userViewModel = userViewModel,
                onStartGame = {
                    navController.navigate(GameRoute)
                }
            )
        }

        composable<GameRoute> {
            GameScreen(
                userViewModel = userViewModel,
                gameViewModel = koinViewModel(),
                onGameOver = { }
            )
        }
    }
}

@Serializable
object LandingRoute


@Serializable
object HomeRoute


@Serializable
object GameRoute
