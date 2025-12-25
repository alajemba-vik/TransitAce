package com.alajemba.paristransitace.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alajemba.paristransitace.ui.theme.RetroAmber
import com.alajemba.paristransitace.ui.theme.VoidBlack
import org.jetbrains.compose.resources.stringResource
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.home_button_acc_description

@Composable
fun ActionButton(
    label: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    colors: ButtonColors? = null,
    icon: @Composable (() -> Unit)? = null
) {

    TextButton(
        enabled = isEnabled,
        colors = colors ?: ButtonDefaults.textButtonColors(
            containerColor = RetroAmber,
            contentColor = VoidBlack
        ),
        onClick = onClick,
        content = {
            icon?.invoke()
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge
            )
        },
        shape = RoundedCornerShape(0.dp),

    )
}

@Composable
fun HomeButton(onHomeClick: () -> Unit) {
    val iconSize = 38.dp

    IconButton(
        onClick = onHomeClick, modifier = Modifier.size(iconSize)
    ) {
        Icon(
            imageVector = Icons.Outlined.Home,
            contentDescription = stringResource(Res.string.home_button_acc_description),
            tint = RetroAmber.copy(alpha = 0.7f)
        )
    }
}