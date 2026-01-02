package com.alajemba.paristransitace.ui.chat

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alajemba.paristransitace.ui.components.ActionButton
import com.alajemba.paristransitace.ui.chat.components.ChatInputField
import com.alajemba.paristransitace.ui.model.ChatMessageAction
import com.alajemba.paristransitace.ui.model.ChatUiModel
import com.alajemba.paristransitace.ui.model.ChatMessageSender
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.PaperText
import com.alajemba.paristransitace.ui.theme.RetroAmber
import com.alajemba.paristransitace.ui.theme.SuccessGreen
import com.alajemba.paristransitace.ui.theme.VoidBlack
import org.jetbrains.compose.resources.stringResource
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.ai_name
import paristransitace.composeapp.generated.resources.chat_window_provide_language_instruction
import paristransitace.composeapp.generated.resources.chat_window_title
import paristransitace.composeapp.generated.resources.reply_ellipsis

@Composable
fun AIChatWindow(
    chatMessages: List<ChatUiModel>,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
    isNewMessageEnabled: Boolean = true,
    onChatMessageAction: (ChatMessageAction, ChatUiModel) -> Unit,
    bgColor: Color = VoidBlack,
    isInActiveGame: Boolean = false,
    showFooter: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = Dimens.Space.medium)
            .border(Dimens.Border.thick, RetroAmber)
    ) {
        HorizontalDivider(
            modifier = Modifier
                .height(Dimens.Space.mediumSmall),
            thickness = Dimens.Space.mediumSmall,
            color = RetroAmber
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(RetroAmber.copy(alpha = 0.05f))
        ) {
            TitleBar()

            WindowContent(
                chatMessages,
                onChatMessageAction = onChatMessageAction
            )
        }

        Spacer(modifier = Modifier.height(Dimens.Space.medium))

        Box(
            modifier = Modifier
                .padding(horizontal = Dimens.Space.medium)
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            ChatInputField(stringResource(Res.string.reply_ellipsis), isNewMessageEnabled, onSend)
        }

        if (showFooter) {
            Spacer(modifier = Modifier.height(Dimens.Space.small))

            Footer(
                if (!isInActiveGame) null else "Type \"help\" to see available actions | Tapez \"aide\" pour voir les actions"
            )
        }

        Spacer(modifier = Modifier.height(Dimens.Space.medium))
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
                .background(SuccessGreen, shape = RoundedCornerShape(50))
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
private fun WindowContent(
    chatMessages: List<ChatUiModel>,
    onChatMessageAction: (ChatMessageAction, ChatUiModel) -> Unit,
) {

    val lazyListState = rememberLazyListState()

    LaunchedEffect(chatMessages) {
        if (chatMessages.size > 2) {
            lazyListState.animateScrollToItem(chatMessages.size - 1)
        }
    }


    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space.mediumSmall),
        contentPadding = PaddingValues(Dimens.Space.medium)
    ) {
        items(
            chatMessages,
            itemContent = { message ->
                MessageCard(
                    message,
                    onAction = { action ->
                        onChatMessageAction(action, message)
                    },
                    canDisplayActions = chatMessages.last().id == message.id
                )
            }
        )
    }
}

@Composable
private fun MessageCard(
    message: ChatUiModel,
    onAction: (ChatMessageAction) -> Unit,
    canDisplayActions: Boolean = true
) {
    val isUserMessage = message.sender == ChatMessageSender.USER

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .align(
                    if (!isUserMessage)
                        Alignment.CenterStart
                    else Alignment.CenterEnd
                )
                .then(
                    if (!isUserMessage)
                        Modifier.background( RetroAmber.copy(alpha = 0.1f))
                    else Modifier
                )
                .border(
                    Dimens.Border.thin,
                    if (!isUserMessage) RetroAmber.copy(alpha = 0.6f) else PaperText.copy(alpha = 0.6f)
                )
                .padding(Dimens.Space.mediumSmall)
        ) {
            if (!isUserMessage) {
                Text(
                    text = stringResource(Res.string.ai_name).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = PaperText.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = Dimens.Space.tiny)
                )
            }
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyLarge,
                color = if (!isUserMessage) RetroAmber else PaperText
            )

            if (message.actions.isNotEmpty() && canDisplayActions) {
                Spacer(modifier = Modifier.height(Dimens.Space.medium))
                if (message.selectedAction != null) {
                    Text(
                        text = stringResource(message.selectedAction.label),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.5f)
                    )
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.Space.medium)) {
                        message.actions.forEach { action ->
                            ActionButton(
                                onClick = { onAction(action) },
                                colors = ButtonDefaults.buttonColors(containerColor = action.color),
                                label = stringResource(action.label)
                            ) {
                                Icon(action.icon, null)
                            }
                        }
                    }
                }


            }
        }
    }
}

@Composable
fun Footer(text: String?) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = text?.uppercase() ?: stringResource(Res.string.chat_window_provide_language_instruction),
            style = MaterialTheme.typography.labelMedium,
            color = RetroAmber.copy(alpha = 0.7f)
        )
    }
}
