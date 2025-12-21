package com.alajemba.paristransitace.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alajemba.paristransitace.ui.pages.GameScreen
import com.alajemba.paristransitace.ui.pages.HomeScreen
import com.alajemba.paristransitace.ui.pages.LandingScreen
import com.alajemba.paristransitace.ui.viewmodels.ChatViewModel
import com.alajemba.paristransitace.ui.viewmodels.GameViewModel
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val userViewModel = koinViewModel<UserViewModel>()
    val chatViewModel = koinViewModel<ChatViewModel>()
    val gameViewModel = koinViewModel<GameViewModel>()

    NavHost(
        navController = navController,
        startDestination = LandingRoute,
    ) {
        composable<LandingRoute> {
            LandingScreen(
                onStartGame = {
                    navController.navigate(
                        if (userViewModel.gameSetupState.value.isSetupComplete) GameRoute else HomeRoute
                    ) {
                        popUpTo(LandingRoute) {
                            inclusive = true
                        }
                    }

                }
            )
        }

        composable<HomeRoute> {
            HomeScreen(
                chatViewModel = chatViewModel,
                userViewModel = userViewModel,
                gameViewModel = gameViewModel,
                onStartGame = {
                    navController.navigate(GameRoute) {
                        popUpTo(HomeRoute) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<GameRoute> {
            GameScreen(
                userViewModel = userViewModel,
                gameViewModel = gameViewModel,
                onReset = {

                },
                onNavigateHome = {
                    navController.navigate(LandingRoute) {
                        popUpTo(LandingRoute) {
                            inclusive = true
                        }
                    }
                }
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
