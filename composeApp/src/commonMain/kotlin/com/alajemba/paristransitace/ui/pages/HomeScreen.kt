package com.alajemba.paristransitace.ui.pages

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.alajemba.paristransitace.ui.components.AIChatWindow
import com.alajemba.paristransitace.ui.components.StatsBar
import com.alajemba.paristransitace.ui.model.AIStatus
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.model.ChatUiModel
import com.alajemba.paristransitace.ui.model.GameSetup
import com.alajemba.paristransitace.ui.model.UIDataState
import com.alajemba.paristransitace.ui.model.UserStats
import com.alajemba.paristransitace.ui.viewmodels.ChatViewModel
import com.alajemba.paristransitace.ui.viewmodels.GameViewModel
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.chat_window_initial_message
import paristransitace.composeapp.generated.resources.connected
import paristransitace.composeapp.generated.resources.connecting_ellipsis
import paristransitace.composeapp.generated.resources.disconnected

@Composable
internal fun HomeScreen(
    chatViewModel: ChatViewModel,
    gameViewModel: GameViewModel,
    onStartGame: () -> Unit,
    userViewModel: UserViewModel,
) {

    val welcomeMessage = stringResource(Res.string.chat_window_initial_message)

    LaunchedEffect(Unit) {
        chatViewModel.setWelcomeMessage(welcomeMessage)

        launch {
            userViewModel.gameSetupState.collect { gameSetupState ->
                when {
                    gameSetupState.isOnScenariosGenerationStep -> {
                        var transiteRulesFile: String? = null

                        if (gameSetupState.isCustomSimulation) {
                            transiteRulesFile = Res.readBytes("files/transit_rules.json").decodeToString()
                        }

                        gameViewModel.generateScenarios(gameSetupState, transiteRulesFile)
                    }

                    gameSetupState.isOnScenariosGenerationFailureStep -> {
                        chatViewModel.setupGame(gameSetupState)
                    }

                    gameSetupState.isSetupComplete -> {
                        chatViewModel.setupGame(gameSetupState)
                        delay(3000L)
                        onStartGame()
                    }
                }
            }
        }

        launch {
            gameViewModel.gameDataState.collect { gameDataValue ->
                if (gameDataValue is UIDataState.Success.ScenariosGenerated) {
                    userViewModel.setupGame(scenariosGenerationStatus = GameSetup.ScenarioGenerationStatus.SUCCESS)
                } else if (gameDataValue is UIDataState.Error) {
                    userViewModel.setupGame(scenariosGenerationStatus = GameSetup.ScenarioGenerationStatus.FAILURE)
                }
            }
        }
    }

    ScreenContent(
        budget = chatViewModel.userStatsState.collectAsState().value.budget,
        morale = chatViewModel.userStatsState.collectAsState().value.morale,
        legalInfractionsCount = chatViewModel.userStatsState.collectAsState().value.legalInfractionsCount,
        chats = chatViewModel.chatMessages.collectAsState().value,
        onSend = { message ->
            chatViewModel.attachNewMessage(message, ChatMessageSender.USER)
            chatViewModel.setupGame(userViewModel.setupGame(message))
        },
        isNewMessageEnabled = !userViewModel.gameSetupState.collectAsState().value.isSetupComplete &&
        !chatViewModel.isLoading.collectAsState(false).value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    budget: Double,
    morale: Int,
    legalInfractionsCount: Int,
    chats: List<ChatUiModel>,
    onSend: (String) -> Unit,
    isNewMessageEnabled: Boolean = true
) {
    Scaffold(
        topBar =  {
            TopAppBar(
                title = {
                    StatsBar(
                        UserStats(
                            budget = budget,
                            morale = morale,
                            legalInfractionsCount = legalInfractionsCount
                        )
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
            isNewMessageEnabled = isNewMessageEnabled
        )

    }
}


@Composable
fun AIConnectionStatusBar(status: AIStatus){
    val text = "SOPHIE_LINK: " + when(status){
        AIStatus.CONNECTED -> stringResource(Res.string.connected).uppercase()
        AIStatus.CONNECTING -> stringResource(Res.string.connecting_ellipsis).uppercase()
        AIStatus.DISCONNECTED -> stringResource(Res.string.disconnected).uppercase()
    }

    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    )
}


@Preview
@Composable
private fun HomeScreenPreview() {
    ScreenContent(
        budget = 100.0,
        morale = 100,
        legalInfractionsCount = 0,
        chats = listOf(
            ChatUiModel(
                sender = ChatMessageSender.AI,
                message = "Hello"
            )
        ),
        onSend = {}
    )

}
