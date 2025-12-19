package com.alajemba.paristransitace.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alajemba.paristransitace.ui.components.TypewriterText
import com.alajemba.paristransitace.ui.theme.AlertRed
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.RetroAmber
import com.alajemba.paristransitace.ui.theme.VoidBlack
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.*

@Composable
internal fun LandingScreen(
    onStartGame: () -> Unit,
) {
    var animationStage by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        delay(500)
        animationStage = 1
        delay(1500)
        animationStage = 2
        delay(1000)
        animationStage = 3
        delay(1500)
        animationStage = 4
        delay(1000)
        animationStage = 5
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .safeDrawingPadding()
                .padding(Dimens.Space.medium)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(1000))
            ) {
                LandingHeader()
            }

            Spacer(modifier = Modifier.height(Dimens.Space.large))

            if (animationStage >= 1) {
                TypewriterText(
                    text = stringResource(Res.string.landing_wake_up),
                    style = MaterialTheme.typography.headlineSmall,
                    color = RetroAmber,
                    modifier = Modifier.fillMaxWidth(),
                    delayMillis = 100
                )
            }

            if (animationStage >= 2) {
                Spacer(modifier = Modifier.height(Dimens.Space.medium))
                TypewriterText(
                    text = stringResource(Res.string.landing_landed_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    color = RetroAmber,
                    modifier = Modifier.fillMaxWidth(),
                    delayMillis = 30
                )
            }

            Spacer(modifier = Modifier.height(Dimens.Space.large))

            if (animationStage >= 3) {
                BulletPointList()
            }

            Spacer(modifier = Modifier.height(Dimens.Space.medium))

            AnimatedVisibility(
                visible = animationStage >= 4,
                enter = fadeIn(tween(1000))
            ) {
                InstructionsSection()
            }

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = animationStage >= 5,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
            ) {
                LandingFooter(onStartGame = onStartGame)
            }
        }
    }
}



@Composable
private fun LandingHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(Res.string.landing_official),
                style = MaterialTheme.typography.labelSmall,
                color = RetroAmber.copy(alpha = 0.7f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.landing_build),
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroAmber.copy(alpha = 0.7f)
                )
                Text(
                    text = stringResource(Res.string.landing_id),
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroAmber.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space.small))

        Text(
            text = stringResource(Res.string.landing_title),
            style = MaterialTheme.typography.headlineMedium,
            color = RetroAmber,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BulletPointList() {
    var listStage by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        delay(200)
        listStage = 1
        delay(300)
        listStage = 2
        delay(300)
        listStage = 3
        delay(300)
        listStage = 4
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(visible = listStage >= 1, enter = fadeIn()) {
            Text(
                text = stringResource(Res.string.landing_year_changed),
                style = MaterialTheme.typography.titleMedium,
                color = RetroAmber,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space.small))

        if (listStage >= 2) HighlightedBulletPoint(stringResource(Res.string.landing_bullet1), stringResource(Res.string.landing_bullet1_highlight))
        if (listStage >= 3) HighlightedBulletPoint(stringResource(Res.string.landing_bullet2), stringResource(Res.string.landing_bullet2_highlight))
        if (listStage >= 4) HighlightedBulletPoint(stringResource(Res.string.landing_bullet3), stringResource(Res.string.landing_bullet3_highlight))
    }
}

@Composable
private fun HighlightedBulletPoint(text: String, highlight: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "▶ ", style = MaterialTheme.typography.bodyLarge, color = RetroAmber)

        val annotatedString = buildAnnotatedString {
            val startIndex = text.indexOf(highlight)
            if (startIndex >= 0) {
                append(text.substring(0, startIndex))
                withStyle(style = SpanStyle(color = AlertRed)) {
                    append(highlight)
                }
                append(text.substring(startIndex + highlight.length))
            } else {
                append(text)
            }
        }
        Text(text = annotatedString, style = MaterialTheme.typography.bodyLarge, color = RetroAmber)
    }
}

@Composable
private fun InstructionsSection() {
    Column {
        Text(
            text = stringResource(Res.string.landing_instructions),
            style = MaterialTheme.typography.bodyLarge,
            color = RetroAmber,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(Dimens.Space.large))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val color = RetroAmber
                    drawLine(
                        color = color,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = color,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = strokeWidth
                    )
                }
                .padding(horizontal = Dimens.Space.medium, vertical = Dimens.Space.small)
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.landing_guide),
                    style = MaterialTheme.typography.bodyLarge,
                    color = RetroAmber
                )
                Text(
                    text = stringResource(Res.string.landing_guide_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    color = RetroAmber.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun LandingFooter(onStartGame: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.landing_click_to_begin),
            style = MaterialTheme.typography.labelMedium,
            color = RetroAmber.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.Start).padding(bottom = Dimens.Space.small)
        )

        Button(
            onClick = onStartGame,
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RetroAmber),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("►►► ", color = VoidBlack, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(Res.string.landing_begin_simulation),
                    style = MaterialTheme.typography.titleMedium,
                    color = VoidBlack,
                    fontWeight = FontWeight.Bold
                )
                Text(" ◄◄◄", color = VoidBlack, style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space.medium))

        Text(
            text = stringResource(Res.string.landing_footer),
            style = MaterialTheme.typography.labelSmall,
            color = RetroAmber.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
    }
}
