package com.alajemba.paristransitace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.RetroAmber
import com.alajemba.paristransitace.ui.theme.TerminalPalette.Separator
import com.alajemba.paristransitace.ui.theme.VoidBlack
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ChatInputField(
    hint: String,
    isEnabled: Boolean = true,
    onSend: (String) -> Unit
) {

    var textFieldValue by remember { mutableStateOf("") }
    val canSend by remember(textFieldValue, isEnabled) {
        mutableStateOf(isEnabled && textFieldValue.isNotBlank())
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(50.dp)
                .border(Dimens.Border.thin, Separator)
                .background(VoidBlack)
                .padding(horizontal = Dimens.Space.small),
            contentAlignment = Alignment.CenterStart
        ) {

            BasicTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (canSend) {
                        onSend(textFieldValue)
                        textFieldValue = ""
                    }
                }),
                textStyle = TextStyle(
                    color = RetroAmber,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(RetroAmber),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    innerTextField()

                    if (textFieldValue.isEmpty()) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                }
            )
        }

        Spacer(modifier = Modifier.width(Dimens.Space.small))

        ActionButton(
            onSend = {
                onSend(textFieldValue)
                textFieldValue = ""
            },
            isEnabled = canSend
        )
    }
}


@Preview
@Composable
fun ChatInputFieldPreview() {
    ChatInputField(hint = "Enter your message here", onSend = {})
}