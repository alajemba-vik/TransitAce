package com.alajemba.paristransitace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alajemba.paristransitace.domain.model.UserStats
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.PaperText
import com.alajemba.paristransitace.ui.theme.RetroAmber
import com.alajemba.paristransitace.utils.toEuroString
import org.jetbrains.compose.resources.stringResource
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.budget
import paristransitace.composeapp.generated.resources.comms
import paristransitace.composeapp.generated.resources.gameover_budget_label
import paristransitace.composeapp.generated.resources.gameover_legal_label
import paristransitace.composeapp.generated.resources.infractions
import paristransitace.composeapp.generated.resources.morale

@Composable
fun StatsBar(
    userStats: UserStats?,
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
            Label(
                value = userStats?.budget?.toEuroString() ?: "-",
                title = stringResource(Res.string.budget)
            )
            Divider()
            Label(
                value = if (userStats == null) "-" else (userStats.morale.toString() + "%"),
                title = stringResource(Res.string.morale)
            )
            Divider()
            Label(
                value = userStats?.legalInfractionsCount?.toString() ?: "",
                title = stringResource(Res.string.infractions)
            )
            Spacer(modifier = Modifier.width(Dimens.Space.small))

            HeaderTab(
                stringResource(Res.string.comms).uppercase(),
                active = isCommsEnabled && userStats != null,
                onClick = onCommsClicked
            )
        }
    }
}

@Composable
private fun Label(
    value: String,
    title: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            title.uppercase(),
            color = PaperText.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelMedium,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            value,
            color = RetroAmber,
            style = MaterialTheme.typography.labelMedium
        )
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

