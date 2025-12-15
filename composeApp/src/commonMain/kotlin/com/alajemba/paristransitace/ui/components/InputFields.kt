package com.alajemba.paristransitace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.RetroAmber
import com.alajemba.paristransitace.ui.theme.TerminalPalette.Separator
import com.alajemba.paristransitace.ui.theme.VoidBlack

@Composable
fun ChatInputField(
    hint: String,
    onSend: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(Dimens.Border.thin, Separator)
                .background(VoidBlack)
                .padding(horizontal = Dimens.Space.small),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            BasicTextField(
                value = "",
                onValueChange = {

                },
                textStyle = TextStyle(
                    color = RetroAmber,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(RetroAmber),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.width(Dimens.Space.small))

        ActionButton(onSend)
    }
}