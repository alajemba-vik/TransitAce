package com.alajemba.paristransitace.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alajemba.paristransitace.domain.model.ScenarioGenerationStatus
import com.alajemba.paristransitace.domain.model.UserStats
import com.alajemba.paristransitace.ui.chat.AIChatWindow
import com.alajemba.paristransitace.ui.components.HomeButton
import com.alajemba.paristransitace.ui.components.StatsBar
import com.alajemba.paristransitace.ui.model.ChatMessageAction
import com.alajemba.paristransitace.ui.model.ChatUiModel
import com.alajemba.paristransitace.ui.model.UIDataState
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.VoidBlack
import com.alajemba.paristransitace.ui.viewmodels.ChatViewModel
import com.alajemba.paristransitace.ui.viewmodels.GameViewModel
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import paristransitace.composeapp.generated.resources.Res

@Composable
internal fun HomeScreen(
    chatViewModel: ChatViewModel,
    gameViewModel: GameViewModel,
    onStartGame: () -> Unit,
    userViewModel: UserViewModel,
    goBack: (() -> Unit)?,
) {
    val gameSetupState by userViewModel.gameSetupState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        launch {
            userViewModel.gameSetupState.collect { gameSetupState ->
                when {
                    gameSetupState.isOnScenariosGenerationStep -> {
                        var transiteRulesFile: String? = null

                        if (gameSetupState.isCustomSimulation) {
                            transiteRulesFile = Res.readBytes("files/transit_rules.json").decodeToString()
                        }

                        gameViewModel.generateGameScenarios(
                            gameSetupState,
                            transiteRulesFile,
                            plot = userViewModel.gameSetupPlot
                        )
                    }

                    gameSetupState.isOnScenariosGenerationSuccessStep ||
                            gameSetupState.isOnScenariosGenerationFailureStep || gameSetupState.isSetupComplete -> {
                        chatViewModel.setupGame(
                            gameSetupState,
                            gameViewModel.storyLine
                        )

                        if (gameSetupState.isSetupComplete) {
                            if (!gameSetupState.isCustomSimulation ) delay(3000L)
                            gameViewModel.startGame()
                            onStartGame()
                        }
                    }
                }
            }
        }

        launch {
            gameViewModel.gameDataState.collect { gameDataValue ->
                if (gameDataValue is UIDataState.Success.ScenariosGenerated) {
                    userViewModel.setupGame(scenariosGenerationStatus = ScenarioGenerationStatus.SUCCESS)
                } else if (gameDataValue is UIDataState.Error) {
                    userViewModel.setupGame(scenariosGenerationStatus = ScenarioGenerationStatus.FAILURE)
                }
            }
        }
    }

    ScreenContent(
        chats = chatViewModel.chatMessages.collectAsStateWithLifecycle().value,
        onSend = { message ->
            chatViewModel.attachUserNewMessage(message)
            chatViewModel.setupGame(userViewModel.setupGame(message), gameViewModel.storyLine)
        },
        showFooter = gameSetupState.isOnLanguageStep,
        onBack = goBack,
        isNewMessageEnabled = !gameSetupState.isSetupComplete &&
        !chatViewModel.isLoading.collectAsStateWithLifecycle(false).value,
        onAction = { actionType: ChatMessageAction, chatUiModel: ChatUiModel ->
            chatViewModel.updateGameMessageWithAction(actionType.name, chatUiModel.id)

            if (actionType == ChatMessageAction.SAVE_SCENARIO){
                gameViewModel.saveCurrentStoryLine()
                userViewModel.setupGame(scenariosGenerationStatus = ScenarioGenerationStatus.SCENARIOS_GENERATED_ACTION)
            } else if (actionType == ChatMessageAction.PLAY_SCENARIO) {
                userViewModel.setupGame(scenariosGenerationStatus = ScenarioGenerationStatus.SCENARIOS_GENERATED_ACTION)
                onStartGame()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    chats: List<ChatUiModel>,
    onSend: (String) -> Unit,
    isNewMessageEnabled: Boolean = true,
    onAction: (ChatMessageAction, ChatUiModel) -> Unit,
    onBack: (() -> Unit)?,
    showFooter: Boolean = true
) {
    Scaffold(
        topBar =  {
            TopAppBar(
                navigationIcon = {
                    onBack?.let {
                        HomeButton(
                            goHome = it,
                            canDirectlyGoHome = chats.size <= 1
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VoidBlack),
                title = {
                    StatsBar(
                        userStats = null,
                        isCommsEnabled = false,
                        onCommsClicked = {},
                        modifier = Modifier.padding(end = Dimens.Space.medium, start = Dimens.Space.tiny)
                    )
                }
            )
        },
        modifier = Modifier.safeDrawingPadding()
    ){
        AIChatWindow(
            chatMessages = chats,
            onSend = onSend,
            modifier = Modifier.padding(it),
            isNewMessageEnabled = isNewMessageEnabled,
            onChatMessageAction = onAction,
            showFooter = showFooter
        )

    }
}

