package com.alajemba.paristransitace.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.alajemba.paristransitace.ui.components.StatsBar
import com.alajemba.paristransitace.ui.model.Scenario
import com.alajemba.paristransitace.ui.model.ScenarioOption
import com.alajemba.paristransitace.ui.model.UserStats
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.RetroAmber
import com.alajemba.paristransitace.ui.theme.VoidBlack
import com.alajemba.paristransitace.ui.viewmodels.GameViewModel
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
import org.jetbrains.compose.resources.stringResource
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.game_screen_scenario_options_description
import paristransitace.composeapp.generated.resources.scenario

@Composable
internal fun GameScreen(
    gameViewModel: GameViewModel,
    userViewModel: UserViewModel,
    onGameOver: (Int, String) -> Unit
) {

    LaunchedEffect(Unit) {
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

        ScreenContent(
            currentScenario,
            progress = gameViewModel.scenarioProgress.value,
            progressText = gameViewModel.scenarioProgressText.collectAsState("").value,
            unreadAlertsCount = gameViewModel.unreadAlertsCount.collectAsState(0).value,
            userStats = userViewModel.userStatsState.value,
            onOptionSelected = { option ->
                gameViewModel.onOptionSelected(option)
                userViewModel.updateStats(
                    cost = option.cost,
                    moraleImpact = option.moraleImpact
                )
            }


        )
    }
}

@Composable
private fun ColumnScope.ScreenContent(
    currentScenario: Scenario?,
    progress: Float,
    progressText: String,
    unreadAlertsCount: Int,
    userStats: UserStats,
    onOptionSelected: (option: ScenarioOption) -> Unit,
) {
    ScreenHeader(
        scenarioTitle =  "${stringResource(Res.string.scenario).uppercase()} ${currentScenario?.id}",
        progress = progress,
        progressText = progressText
    )

    StatsBar(
        userStats,
        unreadAlertsCount = unreadAlertsCount
    )

    if (currentScenario != null) {
        LazyColumn(
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
                        style = MaterialTheme.typography.labelLarge, // VT323
                        color = RetroAmber
                    )
                }
            }

            item {
                Text(
                    text = stringResource(Res.string.scenario).uppercase() + ": " +
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
                    color = MaterialTheme.colorScheme.onBackground // PaperText
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

            itemsIndexed(currentScenario.options) { index, option ->
                OptionRow(
                    index = index,
                    text = option.text,
                    onClick = {
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
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = scenarioTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Gray
            )

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

