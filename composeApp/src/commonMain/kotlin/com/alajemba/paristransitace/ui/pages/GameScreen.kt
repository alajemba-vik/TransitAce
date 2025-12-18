package com.alajemba.paristransitace.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alajemba.paristransitace.commonModule
import com.alajemba.paristransitace.ui.components.AIChatWindow
import com.alajemba.paristransitace.ui.components.StatsBar
import com.alajemba.paristransitace.ui.model.AIStatus
import com.alajemba.paristransitace.ui.model.UserStats
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.viewmodels.ChatViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplicationPreview
import org.koin.compose.viewmodel.koinViewModel
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.connected
import paristransitace.composeapp.generated.resources.connecting_ellipsis
import paristransitace.composeapp.generated.resources.disconnected

@Composable
internal fun GameScreen(chatViewModel: ChatViewModel) {
    Column(
        modifier = Modifier.fillMaxSize()
    ){
        AIConnectionStatusBar(
            status = AIStatus.CONNECTED
        )

        Spacer(modifier = Modifier.height(Dimens.Space.medium))

        StatsBar(
            UserStats(
                budget = 100.00,
                confidence = 30,
                morale = 0
            )
        )
        AIChatWindow(
            chatMessages = listOf(),
            onSend = {

            }
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
fun GameScreenPreview() {
    KoinApplicationPreview(application = { modules(commonModule) }) {
        GameScreen(koinViewModel())
    }

}
