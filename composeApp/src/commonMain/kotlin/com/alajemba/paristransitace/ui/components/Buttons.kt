package com.alajemba.paristransitace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import org.jetbrains.compose.resources.stringResource
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.reply_ellipsis
import paristransitace.composeapp.generated.resources.send

@Composable
fun ActionButton(onSend: (String) -> Unit) {
    Button(
        onClick = {
            onSend(
                ""
            )
        },
        shape = RoundedCornerShape(0.dp), // Square button
        colors = ButtonDefaults.buttonColors(
            containerColor = RetroAmber,
            contentColor = VoidBlack
        ),
        modifier = Modifier.fillMaxHeight().width(80.dp)
    ) {
        Text(
            text = stringResource(Res.string.send).uppercase(),
            style = MaterialTheme.typography.labelLarge
        )
    }
}