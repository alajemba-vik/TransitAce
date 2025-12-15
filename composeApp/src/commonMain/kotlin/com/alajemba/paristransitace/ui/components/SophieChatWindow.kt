package com.alajemba.paristransitace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alajemba.paristransitace.ui.model.ChatMessage
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.theme.AlertRed
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.RetroAmber
import com.alajemba.paristransitace.ui.theme.TerminalPalette.Separator
import com.alajemba.paristransitace.ui.theme.VoidBlack
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.ai_name
import paristransitace.composeapp.generated.resources.chat_window_initial_message
import paristransitace.composeapp.generated.resources.chat_window_provide_language_instruction
import paristransitace.composeapp.generated.resources.chat_window_title
import paristransitace.composeapp.generated.resources.reply_ellipsis
import paristransitace.composeapp.generated.resources.send
import paristransitace.composeapp.generated.resources.you

@Composable
fun AIChatWindow(
    chatMessages: List<ChatMessage>,
    onSend: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(Dimens.Space.medium)
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

        ChatInputField(stringResource(Res.string.reply_ellipsis), onSend)

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
private fun WindowContent(chatMessages: List<ChatMessage>) {
    Box(modifier = Modifier.padding(Dimens.Space.medium)) {

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .border(Dimens.Border.thin, RetroAmber.copy(alpha = 0.5f))
                .padding(Dimens.Space.medium)
        ) {
            items(chatMessages) { message ->
                Column(

                ) {
                    Text(
                        text = (if (message.sender == ChatMessageSender.AI) stringResource(Res.string.ai_name) else stringResource(
                            Res.string.you
                        )).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = RetroAmber.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = Dimens.Space.tiny)
                    )
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyLarge, // Crimson Pro Font
                        color = RetroAmber
                    )

                }
            }
        }
    }
}

@Composable
private fun Footer() {
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
            ChatMessage(
                sender = ChatMessageSender.AI,
                message = stringResource(Res.string.chat_window_initial_message)
            )
        ),
        onSend = {}
    )
}
