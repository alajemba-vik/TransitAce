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
import androidx.compose.ui.Modifier
import com.alajemba.paristransitace.ui.components.AIChatWindow
import com.alajemba.paristransitace.ui.components.StatsBar
import com.alajemba.paristransitace.ui.model.AIStatus
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.model.ChatUiModel
import com.alajemba.paristransitace.ui.model.UserStats
import com.alajemba.paristransitace.ui.viewmodels.ChatViewModel
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.chat_window_initial_message
import paristransitace.composeapp.generated.resources.connected
import paristransitace.composeapp.generated.resources.connecting_ellipsis
import paristransitace.composeapp.generated.resources.disconnected

@Composable
internal fun LandingScreen(
    chatViewModel: ChatViewModel,
    onStartGame: () -> Unit,
    userViewModel: UserViewModel,
) {

    val welcomeMessage = stringResource(Res.string.chat_window_initial_message)

    LaunchedEffect(Unit) {
        chatViewModel.setWelcomeMessage(welcomeMessage)

        userViewModel.gameSetupState.collect { state ->
            if (state.isSetupComplete) {
                delay(3000L)
                onStartGame()
            }
        }
    }

    ScreenContent(
        budget = chatViewModel.userStatsState.value.budget,
        morale = chatViewModel.userStatsState.value.morale,
        alertsCount = chatViewModel.alertsCount.value,
        chats = chatViewModel.chatMessages.value,
        onSend = { message ->
            chatViewModel.attachNewUserMessage(message)
            chatViewModel.setupGame(userViewModel.setupGame(message))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    budget: Double,
    morale: Int,
    alertsCount: Int,
    chats: List<ChatUiModel>,
    onSend: (String) -> Unit
) {
    Scaffold(
        topBar =  {
            TopAppBar(
                title = {
                    StatsBar(
                        UserStats(
                            budget = budget,
                            morale = morale
                        ),
                        alertsCount = alertsCount
                    )
                }
            )
        },
        modifier = Modifier.safeDrawingPadding()
    ){
        AIChatWindow(
            chatMessages = chats,
            onSend = onSend,
            modifier = Modifier.padding(it)
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
private fun LandingScreenPreview() {
    ScreenContent(
        budget = 100.0,
        morale = 100,
        alertsCount = 0,
        chats = listOf(
            ChatUiModel(
                sender = ChatMessageSender.AI,
                message = "Hello"
            )
        ),
        onSend = {}
    )

}
