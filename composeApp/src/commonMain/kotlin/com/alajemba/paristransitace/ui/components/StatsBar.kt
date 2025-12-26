package com.alajemba.paristransitace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alajemba.paristransitace.ui.model.UserStats
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.RetroAmber
import org.jetbrains.compose.resources.stringResource
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.comms
import paristransitace.composeapp.generated.resources.map

@Composable
fun StatsBar(
    userStats: UserStats,
    showUnknownState: Boolean = false,
    isCommsEnabled: Boolean = true,
    onCommsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (showUnknownState) "-" else userStats.budget.toString(),
                color = RetroAmber,
                style = MaterialTheme.typography.labelMedium
            )
            Divider()
            Text(
                if (showUnknownState) "-" else (userStats.morale.toString() + "%"),
                color = RetroAmber,
                style = MaterialTheme.typography.labelMedium
            )
            Divider()
            Text(
                userStats.legalInfractionsCount.toString(),
                color = RetroAmber,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.width(Dimens.Space.small))

            HeaderTab(
                stringResource(Res.string.comms).uppercase(),
                active = isCommsEnabled && !showUnknownState,
                onClick = onCommsClicked
            )
        }
    }
}

@Composable
private fun Divider() = Text("|", color = Color.Gray)

@Composable
fun HeaderTab(
    text: String,
    active: Boolean,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .border(Dimens.Border.thin, if (active) RetroAmber else Color.Gray.copy(alpha = 0.5f))
            .background(if (active) RetroAmber.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) RetroAmber else Color.Gray
        )
    }
}

