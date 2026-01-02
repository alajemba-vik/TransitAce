package com.alajemba.paristransitace.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alajemba.paristransitace.domain.model.GameLanguage
import com.alajemba.paristransitace.ui.game.GameScreen
import com.alajemba.paristransitace.ui.home.HomeScreen
import com.alajemba.paristransitace.ui.landing.LandingScreen
import com.alajemba.paristransitace.ui.viewmodels.ChatEvent
import com.alajemba.paristransitace.ui.viewmodels.ChatViewModel
import com.alajemba.paristransitace.ui.viewmodels.GameViewModel
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val userViewModel = koinViewModel<UserViewModel>()
    val chatViewModel = koinViewModel<ChatViewModel>()
    val gameViewModel = koinViewModel<GameViewModel>()



    fun clearAppState() {
        userViewModel.clearAllInfo()
        gameViewModel.clearUIState()
        chatViewModel.clearUIState()
    }

    LaunchedEffect(Unit) {

        launch {
            chatViewModel.events.collect { event ->
                when(event){
                    is ChatEvent.LoadStory -> {
                        gameViewModel.loadStory(event.storyId)
                        chatViewModel.attachSystemMessage(
                            if (!event.isFrench) "Scenario loaded." else "Scénario chargé."
                        )
                    }
                    is ChatEvent.RestartGame -> {
                        gameViewModel.startGame()
                        chatViewModel.attachSystemMessage(
                            if (!event.isFrench) "Simulation reset." else "Simulation réinitialisée."
                        )
                    }
                }
            }
        }
    }


    when(val hasSeenLanding = userViewModel.hasSeenLandingScreen.collectAsStateWithLifecycle().value) {
        null -> {
            // Still loading
        }
        else -> {
            NavHost(
                navController = navController,
                startDestination = if (hasSeenLanding) HomeRoute else LandingRoute,
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

                    LaunchedEffect(Unit) {
                        launch {
                            Locale.current.toLanguageTag().startsWith(GameLanguage.FRENCH.languageTag, ignoreCase = true).let { isFrench ->
                                userViewModel.setDeviceLanguage(
                                    if (isFrench) GameLanguage.FRENCH else GameLanguage.ENGLISH
                                )
                            }
                        }

                        userViewModel.setHasSeenLandingScreen()
                    }
                }

                composable<HomeRoute> {
                    HomeScreen(
                        chatViewModel = chatViewModel,
                        userViewModel = userViewModel,
                        gameViewModel = gameViewModel,
                        goBack = {
                            clearAppState()
                            navController.navigate(LandingRoute) {
                                popUpTo(HomeRoute) {
                                    inclusive = true
                                }
                            }
                        },
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
                        chatViewModel = chatViewModel,
                        onNavigateHome = {
                            clearAppState()
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
    }
}