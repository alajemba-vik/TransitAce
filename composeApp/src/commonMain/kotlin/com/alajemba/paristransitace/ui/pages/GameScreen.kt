package com.alajemba.paristransitace.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alajemba.paristransitace.ui.components.AnimatedGameOverOverlay
import com.alajemba.paristransitace.ui.components.StatsBar
import com.alajemba.paristransitace.ui.components.TypewriterText
import com.alajemba.paristransitace.ui.model.Scenario
import com.alajemba.paristransitace.ui.model.ScenarioOption
import com.alajemba.paristransitace.ui.theme.AlertRed
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.RetroAmber
import com.alajemba.paristransitace.ui.theme.SuccessGreen
import com.alajemba.paristransitace.ui.theme.VoidBlack
import com.alajemba.paristransitace.ui.viewmodels.GameViewModel
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.resources.stringResource
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.*

@Composable
internal fun GameScreen(
    gameViewModel: GameViewModel,
    userViewModel: UserViewModel,
    onNavigateHome: () -> Unit
) {

    LaunchedEffect(Unit) {
        println("GameScreen LaunchedEffect: Starting game with isEnglish = ${userViewModel.gameSetupState.value.isEnglish}")
        gameViewModel.startGame(userViewModel.gameSetupState.value.isEnglish)
    }

    val currentScenario = gameViewModel.currentScenario.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .statusBarsPadding()
            .safeDrawingPadding()
            .padding(Dimens.Space.medium)
    ) {

        ScreenHeader(
            scenarioTitle =  "${stringResource(Res.string.scenario).uppercase()} ${currentScenario?.id}",
            progress = gameViewModel.scenarioProgress.value,
            progressText =gameViewModel.scenarioProgressText.collectAsState("").value,
            onHomeClick = onNavigateHome
        )

        StatsBar(userViewModel.userStatsState.value)

        var isGameOver by remember { mutableStateOf(false) }

        when {
            !isGameOver -> {
                ScreenContent(
                    currentScenario,
                    onOptionSelected = { option ->
                        userViewModel.updateStats(
                            budgetImpact = option.budgetImpact,
                            moraleImpact = option.moraleImpact,
                            increaseLegalInfractionsBy = option.increaseLegalInfractionsBy
                        )
                    },
                    loadNextScenario = {
                        if (!gameViewModel.nextScenario()) {
                            isGameOver = true
                            gameViewModel.calculateFinalGrade(
                                budgetRemaining = userViewModel.userStatsState.value.budget,
                                moraleRemaining = userViewModel.userStatsState.value.morale,
                                legalInfractionsCount = userViewModel.userStatsState.value.legalInfractionsCount
                            )
                        }
                    }
                )
            }
            else -> {
                with(userViewModel.userStatsState) {

                    AnimatedGameOverOverlay(
                        moneyRemaining = value.budget,
                        moraleRemaining = value.morale,
                        legalInfractionsCount = value.legalInfractionsCount,
                        grade = gameViewModel.gameReport.value.grade,
                        reason = gameViewModel.gameReport.value.summary,
                        onRestart = {
                            isGameOver = false
                            gameViewModel.restartGame(userViewModel.gameSetupState.value.isEnglish)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.ScreenContent(
    currentScenario: Scenario?,
    onOptionSelected: (option: ScenarioOption) -> Unit,
    loadNextScenario: () -> Unit,
) {

    if (currentScenario != null) {

        var lastSelection by remember(currentScenario) { mutableStateOf<ScenarioOption?>(null) }
        val lazyListState = rememberLazyListState()


        LazyColumn(
            state = lazyListState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space.medium)
        ) {

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black)
                        .border(Dimens.Border.thin, RetroAmber.copy(alpha = 0.1f))
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .border(Dimens.Border.thin, RetroAmber)
                        .padding(horizontal = Dimens.Space.medium, vertical = Dimens.Space.small)
                ) {
                    Text(
                        text = currentScenario.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = RetroAmber
                    )
                }
            }

            item {
                Text(
                    text = stringResource(Res.string.log).uppercase() + ": " +
                            stringResource(Res.string.scenario).uppercase() +
                            "_${currentScenario.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray.copy(alpha = 0.5f)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp),
                    thickness = 1.dp,
                    color = Color.Gray.copy(alpha = 0.2f)
                )
            }

            item {
                Text(
                    text = currentScenario.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Spacer(modifier = Modifier.height(Dimens.Space.small))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(RetroAmber.copy(alpha = 0.5f)))
                    Spacer(modifier = Modifier.width(Dimens.Space.small))
                    Text(
                        text = stringResource(Res.string.game_screen_scenario_options_description).uppercase() + ":",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }

            lastSelection?.apply {
                item {

                    val autoScroll = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }

                    LaunchedEffect(Unit) {

                        autoScroll.collect {
                            // Small delay to ensure the new line has been rendered
                            delay(300)
                            lazyListState.animateScrollToItem(lazyListState.layoutInfo.totalItemsCount - 1)
                        }
                    }

                    ResultCard(
                        commentary = commentary,
                        budgetImpact = budgetImpact,
                        moraleImpact = moraleImpact,
                        onContinue = loadNextScenario,
                        hasAddedNewLineForTypewriterText = {
                            autoScroll.tryEmit(Unit)
                        }
                    )
                }
            } ?: itemsIndexed(currentScenario.options) { index, option ->
                OptionRow(
                    index = index,
                    text = option.text,
                    onClick = {
                        lastSelection = option
                        onOptionSelected(option)
                    }
                )
                Spacer(modifier = Modifier.height(Dimens.Space.small))
            }

        }
    }
}


@Composable
fun ScreenHeader(
    scenarioTitle: String,
    progress: Float,
    progressText: String,
    onHomeClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onHomeClick) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = stringResource(Res.string.home_button_acc_description),
                        tint = RetroAmber
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = scenarioTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.width(100.dp),
                    color = RetroAmber.copy(alpha = 0.5f),
                    trackColor = Color.DarkGray,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(progressText, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun OptionRow(index: Int, text: String, onClick: () -> Unit) {
    var wasClicked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .border(Dimens.Border.thin, if (wasClicked) RetroAmber else Color.Gray.copy(alpha = 0.3f))
            .clickable {
                wasClicked = true
                onClick()
            }
    ) {

        Box(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight()
                .background(RetroAmber.copy(alpha = if (wasClicked) 0.5f else 0.05f))
                .border(width = 0.dp, color = Color.Transparent)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = RetroAmber
            )
        }

        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.Gray.copy(alpha = 0.3f)))

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun ResultCard(
    commentary: String,
    budgetImpact: Double,
    moraleImpact: Int,
    onContinue: () -> Unit,
    hasAddedNewLineForTypewriterText: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(Dimens.Border.thin, RetroAmber)
            .background(RetroAmber.copy(alpha = 0.05f))
            .padding(Dimens.Space.medium)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.ai_name).uppercase() + ": ",
                style = MaterialTheme.typography.labelSmall,
                color = RetroAmber.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.Space.small))

        TypewriterText(
            text = commentary,
            style = MaterialTheme.typography.bodyLarge,
            color = RetroAmber,
            hasAddedNewLine = hasAddedNewLineForTypewriterText
        )

        HorizontalDivider(color = RetroAmber.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (budgetImpact < 0) "-€${-budgetImpact}" else "€0.00",
                color = if (budgetImpact < 0) AlertRed else Color.Gray,
                style = MaterialTheme.typography.labelLarge
            )

            val moraleString = stringResource(Res.string.morale).uppercase() + (if (moraleImpact < 0) " " else " +") + "$moraleImpact"
            Text(
                text = moraleString,
                color = if (moraleImpact < 0) AlertRed else SuccessGreen,
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(Dimens.Space.medium))

        Button(
            onClick = onContinue,
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(containerColor = RetroAmber)
        ) {
            Text(stringResource(Res.string.next).uppercase() + " >>", color = VoidBlack)
        }
    }
}