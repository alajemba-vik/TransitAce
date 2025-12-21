package com.alajemba.paristransitace.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

@Composable
fun ParisTransitTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        typography = appTypography(),
        colorScheme = TerminalColors
    ){
        Surface(
            color = TerminalColors.background,
            contentColor = TerminalColors.onBackground,
            content = content
        )
    }
}