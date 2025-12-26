package com.alajemba.paristransitace.ui.pages

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.alajemba.paristransitace.ui.components.AIChatWindow
import com.alajemba.paristransitace.ui.components.HomeButton
import com.alajemba.paristransitace.ui.components.StatsBar
import com.alajemba.paristransitace.ui.model.*
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
    goBack: (() -> Unit)?
) {

    LaunchedEffect(Unit) {
        launch {
            userViewModel.gameSetupState.collect { gameSetupState ->
                when {
                    gameSetupState.isOnScenariosGenerationStep -> {
                        var transiteRulesFile: String? = null

                        if (gameSetupState.isCustomSimulation) {
                            transiteRulesFile = Res.readBytes("files/transit_rules.json").decodeToString()
                        }

                        gameViewModel.generateScenarios(
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
                            onStartGame()
                        }
                    }
                }
            }
        }

        launch {
            gameViewModel.gameDataState.collect { gameDataValue ->
                if (gameDataValue is UIDataState.Success.ScenariosGenerated) {
                    println("Scenarios generated successfully.")
                    userViewModel.setupGame(scenariosGenerationStatus = GameSetup.ScenarioGenerationStatus.SUCCESS)
                } else if (gameDataValue is UIDataState.Error) {
                    userViewModel.setupGame(scenariosGenerationStatus = GameSetup.ScenarioGenerationStatus.FAILURE)
                }
            }
        }
    }

    ScreenContent(
        userStats = chatViewModel.userStatsState.collectAsState().value,
        chats = chatViewModel.chatMessages.collectAsState().value,
        onSend = { message ->
            chatViewModel.attachUserNewMessage(message)
            chatViewModel.setupGame(userViewModel.setupGame(message), gameViewModel.storyLine)
        },
        onBack = goBack,
        isNewMessageEnabled = !userViewModel.gameSetupState.collectAsState().value.isSetupComplete &&
        !chatViewModel.isLoading.collectAsState(false).value,
        onAction = { actionType: ChatMessageAction, chatUiModel: ChatUiModel ->
            chatViewModel.updateGameMessageWithAction(actionType.name, chatUiModel.id)

            if (actionType == ChatMessageAction.SAVE_SCENARIO){
                gameViewModel.saveGeneratedCustomScenarios()
                userViewModel.setupGame(scenariosGenerationStatus = GameSetup.ScenarioGenerationStatus.SCENARIOS_GENERATED_ACTION)
            } else if (actionType == ChatMessageAction.PLAY_SCENARIO) {
                userViewModel.setupGame(scenariosGenerationStatus = GameSetup.ScenarioGenerationStatus.SCENARIOS_GENERATED_ACTION)
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
    userStats: UserStats,
    onAction: (ChatMessageAction, ChatUiModel) -> Unit,
    onBack: (() -> Unit)?,
) {
    Scaffold(
        topBar =  {
            TopAppBar(
                navigationIcon = {
                    onBack?.let {  HomeButton(onHomeClick = it) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VoidBlack),
                title = {
                    StatsBar(
                        userStats = userStats,
                        showUnknownState = true,
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
            onChatMessageAction = onAction
        )

    }
}

