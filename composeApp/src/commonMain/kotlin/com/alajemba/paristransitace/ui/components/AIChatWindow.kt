package com.alajemba.paristransitace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alajemba.paristransitace.ui.model.ChatUiModel
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.theme.AlertRed
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.RetroAmber
import com.alajemba.paristransitace.ui.theme.VoidBlack
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.ai_name
import paristransitace.composeapp.generated.resources.chat_window_initial_message
import paristransitace.composeapp.generated.resources.chat_window_provide_language_instruction
import paristransitace.composeapp.generated.resources.chat_window_title
import paristransitace.composeapp.generated.resources.reply_ellipsis
import paristransitace.composeapp.generated.resources.you

@Composable
fun AIChatWindow(
    chatMessages: List<ChatUiModel>,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(horizontal = Dimens.Space.medium)
            .border(Dimens.Border.thin, RetroAmber.copy(alpha = 0.3f)) // Outer frame
            .padding(Dimens.Space.small) // Inner padding inside the frame
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(RetroAmber.copy(alpha = 0.05f))
                .border(Dimens.Border.thin, RetroAmber)
        ) {
            TitleBar()

            WindowContent(chatMessages)
        }

        Spacer(modifier = Modifier.height(Dimens.Space.medium))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            ChatInputField(stringResource(Res.string.reply_ellipsis), onSend)
        }

        Spacer(modifier = Modifier.height(Dimens.Space.small))

        Footer()
    }
}

@Composable
private fun TitleBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RetroAmber.copy(alpha = 0.1f))
            .padding(Dimens.Space.small),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(AlertRed, shape = RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.width(Dimens.Space.small))
        Text(
            text = stringResource(Res.string.chat_window_title).uppercase(),
            style = MaterialTheme.typography.labelLarge, // VT323 Font
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun WindowContent(chatMessages: List<ChatUiModel>) {

    val lazyListState = rememberLazyListState()

    LaunchedEffect(chatMessages) {
        if (chatMessages.size > 2) {
            lazyListState.animateScrollToItem(chatMessages.size - 1)
        }
    }


    Box(modifier = Modifier.padding(Dimens.Space.medium)) {

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxWidth()
                .border(Dimens.Border.thin, RetroAmber.copy(alpha = 0.5f))
                .padding(Dimens.Space.medium)
        ) {
            items(chatMessages) { message ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    Column(
                        modifier = Modifier
                            .align(
                                if (message.sender == ChatMessageSender.AI)
                                    Alignment.CenterStart
                                else Alignment.CenterEnd
                            )
                            .background(RetroAmber.copy(alpha = 0.1f)),
                    ){
                        Text(
                            text = (
                                    if (message.sender == ChatMessageSender.AI)
                                        stringResource(Res.string.ai_name)
                                    else stringResource(Res.string.you)
                                    ).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = RetroAmber.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = Dimens.Space.tiny)
                        )
                        Text(
                            text = message.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = RetroAmber,
                        )

                    }
                }
            }
        }
    }
}

@Composable
fun Footer() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(Res.string.chat_window_provide_language_instruction).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = RetroAmber.copy(alpha = 0.7f)
        )
    }
}



@Preview
@Composable
fun AIChatWindowPreview() {
    AIChatWindow(
        chatMessages = listOf(
            ChatUiModel(
                sender = ChatMessageSender.AI,
                message = stringResource(Res.string.chat_window_initial_message)
            )
        ),
        onSend = {},
    )
}
