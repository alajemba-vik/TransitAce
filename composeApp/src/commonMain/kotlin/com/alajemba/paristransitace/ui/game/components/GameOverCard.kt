package com.alajemba.paristransitace.ui.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alajemba.paristransitace.ui.theme.AlertRed
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.RetroAmber
import com.alajemba.paristransitace.ui.theme.VoidBlack
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.*
import kotlin.random.Random
import kotlin.time.Clock

@Composable
fun AnimatedGameOverOverlay(
    moneyRemaining: Double,
    moraleRemaining: Int,
    legalInfractionsCount: Int,
    grade: String,
    reason: String,
    onRestart: () -> Unit
) {
    var animationStage by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        delay(500)
        animationStage = 1
        delay(800)
        animationStage = 2
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (animationStage == 1) {
            GlitchEffect(modifier = Modifier.fillMaxSize())
        }

        AnimatedVisibility(
            visible = animationStage == 2,
            enter = fadeIn(animationSpec = tween(1000))
        ) {
            GameOverCard(
                money = moneyRemaining,
                morale = moraleRemaining,
                legalInfractions = legalInfractionsCount,
                grade = grade,
                reason = reason,
                onRestart = onRestart
            )
        }
    }
}

@Composable
private fun GlitchEffect(modifier: Modifier = Modifier) {
    val random = remember { Random(Clock.System.now().toEpochMilliseconds()) }
    var trigger by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while(true) {
            trigger++
            delay(50)
        }
    }

    Canvas(modifier = modifier.background(VoidBlack)) {
        repeat(50) {
            val w = size.width
            val h = size.height
            val x = random.nextFloat() * w
            val y = random.nextFloat() * h
            val blockW = random.nextFloat() * (w / 2)
            val blockH = random.nextFloat() * (h / 10)

            drawRect(
                color = if (random.nextBoolean()) RetroAmber else AlertRed,
                topLeft = Offset(x, y),
                size = Size(blockW, blockH),
                alpha = random.nextFloat() * 0.5f
            )
        }

        repeat(20) {
            val y = random.nextFloat() * size.height
            drawLine(
                color = RetroAmber,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2f,
                alpha = 0.3f
            )
        }
    }
}

@Composable
fun GameOverCard(
    money: Double,
    morale: Int,
    legalInfractions: Int,
    grade: String,
    reason: String,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        Spacer(modifier = Modifier.height(Dimens.Space.large))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(Dimens.Border.thin, RetroAmber)
                .padding(vertical = Dimens.Space.large, horizontal = Dimens.Space.medium)
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.gameover_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = RetroAmber,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(Res.string.gameover_subtitle),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                Spacer(modifier = Modifier.height(24.dp))

                ReportRow(
                    label = stringResource(Res.string.gameover_budget_label),
                    value = "€${money}",
                    valueColor = if (money < 0) AlertRed else RetroAmber
                )

                ReportRow(
                    label = stringResource(Res.string.gameover_confidence_label),
                    value = "$morale%",
                    valueColor = RetroAmber
                )

                ReportRow(
                    label = stringResource(Res.string.gameover_legal_label),
                    value = "$legalInfractions",
                    valueColor = if (legalInfractions > 0) AlertRed else RetroAmber
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "\"$reason\"",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif
                    ),
                    color = AlertRed,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = onRestart,
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(Dimens.Border.thin, RetroAmber)
                ) {
                    Text(
                        text = stringResource(Res.string.gameover_restart_button),
                        style = MaterialTheme.typography.titleMedium,
                        color = RetroAmber
                    )
                }
            }

            Text(
                text = grade,
                fontSize = 200.sp,
                color = AlertRed.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = 40.dp)
                    .rotate(-15f)
            )
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f), thickness = 1.dp)
}


@Preview
@Composable
fun GameOverCardPreview() {
    GameOverCard(
        money = -150000.0,
        morale = 45,
        legalInfractions = 3,
        grade = "D",
        reason = "Your mismanagement has led to widespread dissatisfaction among the citizens.",
        onRestart = {}
    )
}