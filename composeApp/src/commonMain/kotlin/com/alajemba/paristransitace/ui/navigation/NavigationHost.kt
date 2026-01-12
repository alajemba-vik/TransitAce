package com.alajemba.paristransitace.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.model.ChatUiModel
import com.alajemba.paristransitace.ui.viewmodels.ChatViewModel
import com.alajemba.paristransitace.ui.viewmodels.GameViewModel
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
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
        Locale.current.toLanguageTag().startsWith(GameLanguage.FRENCH.languageTag, ignoreCase = true).let { isFrench ->
            userViewModel.setDeviceLanguage(
                if (isFrench) GameLanguage.FRENCH else GameLanguage.ENGLISH
            )
        }
    }

    when(val lastSessionCheckpoint = userViewModel.lastSessionCheckpoint.collectAsStateWithLifecycle().value) {
        null -> {
            // Still loading
        }
        else -> {
            NavHost(
                navController = navController,
                startDestination = when(lastSessionCheckpoint.lowercase()) {
                    GameRoute.label -> GameRoute
                    HomeRoute.label -> HomeRoute
                    else -> LandingRoute
                },
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { enterTransition() },
                popExitTransition = { exitTransition() }
            ) {
                composable<LandingRoute> {
                    LandingScreen(
                        onStartGame = {
                            if (userViewModel.gameSetupState.value.isOnLanguageStep) {
                                chatViewModel.attachSystemMessage("English(E) / Français(F) ?")
                            }
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

                    LaunchedEffect(Unit){
                        userViewModel.wasOnHomeScreen()
                    }
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

                    LaunchedEffect(Unit){
                        userViewModel.wasOnGameScreen()
                    }
                }
            }
        }
    }
}

private fun enterTransition(): EnterTransition {
    return fadeIn(
        animationSpec = tween(durationMillis = 500, delayMillis = 300)
    ) + scaleIn(
        initialScale = 0.95f,
        animationSpec = tween(durationMillis = 500, delayMillis = 300)
    )
}

private fun exitTransition(): ExitTransition {
    return fadeOut(
        animationSpec = tween(durationMillis = 400)
    ) + scaleOut(
        targetScale = 1.02f,
        animationSpec = tween(durationMillis = 400)
    )
}