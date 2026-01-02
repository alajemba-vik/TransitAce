package com.alajemba.paristransitace.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.alajemba.paristransitace.ui.theme.RetroAmber
import org.jetbrains.compose.resources.stringResource
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.ai_says
import paristransitace.composeapp.generated.resources.game_screen_on_home_click_dialog_text
import paristransitace.composeapp.generated.resources.home_button_acc_description
import paristransitace.composeapp.generated.resources.leave
import paristransitace.composeapp.generated.resources.stay


@Composable
fun HomeButton(
    goHome: () -> Unit,
    canDirectlyGoHome: Boolean,
) {
    val iconSize = 38.dp

    var showOnHomeClickDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            if (canDirectlyGoHome) {
                goHome()
            } else {
                showOnHomeClickDialog = true
            }
        }, modifier = Modifier.size(iconSize)
    ) {
        Icon(
            imageVector = Icons.Outlined.Home,
            contentDescription = stringResource(Res.string.home_button_acc_description),
            tint = RetroAmber.copy(alpha = 0.7f)
        )
    }

    if (showOnHomeClickDialog) {
        Dialog(onDismissRequest = { showOnHomeClickDialog = false }) {
            AppDialog(
                text = stringResource(Res.string.game_screen_on_home_click_dialog_text),
                onConfirm = {
                    showOnHomeClickDialog = false
                    goHome()
                },
                onDismiss = { showOnHomeClickDialog = false },
                titleLabel = stringResource(Res.string.ai_says),
                confirmLabel = stringResource(Res.string.stay),
                dismissLabel = stringResource(Res.string.leave)
            )
        }
    }
}