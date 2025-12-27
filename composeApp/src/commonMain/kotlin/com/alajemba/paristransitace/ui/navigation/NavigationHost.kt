package com.alajemba.paristransitace.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alajemba.paristransitace.network.models.FunctionDeclaration
import com.alajemba.paristransitace.ui.model.UIDataState
import com.alajemba.paristransitace.ui.pages.GameScreen
import com.alajemba.paristransitace.ui.pages.HomeScreen
import com.alajemba.paristransitace.ui.pages.LandingScreen
import com.alajemba.paristransitace.ui.viewmodels.ChatViewModel
import com.alajemba.paristransitace.ui.viewmodels.GameViewModel
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val userViewModel = koinViewModel<UserViewModel>()
    val chatViewModel = koinViewModel<ChatViewModel>()
    val gameViewModel = koinViewModel<GameViewModel>()

    LaunchedEffect(Unit) {
        launch {
            gameViewModel.gameDataState.collect { dataState ->
                when(dataState){
                    is UIDataState.Success.ChatResponse -> {
                        val isEnglish = userViewModel.gameSetupState.value.isEnglish

                        if (dataState.command.command == FunctionDeclaration.DECL_LOAD_STORYLINE) {
                            gameViewModel.loadStory(dataState.sentMessage)

                            chatViewModel.attachSystemMessage(
                                if (isEnglish) "Scenario loaded." else "Scénario chargé."
                            )
                        } else if (dataState.command.command == FunctionDeclaration.DECL_RESTART_GAME){
                            gameViewModel.startGame()
                            chatViewModel.attachSystemMessage(
                                if (isEnglish) "Simulation reset." else "Simulation réinitialisée."
                            )
                        }
                    }
                    else -> {

                    }
                }
            }
        }
    }

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

            LaunchedEffect(Unit){
                gameViewModel.clearState()
                userViewModel.clearAllInfo()
                chatViewModel.clearAllChats()
            }
        }

        composable<HomeRoute> {
            HomeScreen(
                chatViewModel = chatViewModel,
                userViewModel = userViewModel,
                gameViewModel = gameViewModel,
                goBack = {
                    navController.navigate(LandingRoute) {
                        popUpTo(HomeRoute) {
                            inclusive = true
                        }
                    }
                },
                onStartGame = {
                    println("Navigating to Game Screen from Home Screen")
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
                chatViewModel = chatViewModel,
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

