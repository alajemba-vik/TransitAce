package com.alajemba.paristransitace.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alajemba.paristransitace.ui.chat.AIChatWindow
import com.alajemba.paristransitace.ui.game.components.AnimatedGameOverOverlay
import com.alajemba.paristransitace.ui.game.components.AnimatedScenarioImage
import com.alajemba.paristransitace.ui.components.HomeButton
import com.alajemba.paristransitace.ui.components.StatsBar
import com.alajemba.paristransitace.ui.components.TypewriterText
import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.ScenarioOption
import com.alajemba.paristransitace.ui.mapper.toDrawable
import com.alajemba.paristransitace.ui.theme.*
import com.alajemba.paristransitace.ui.viewmodels.ChatViewModel
import com.alajemba.paristransitace.ui.viewmodels.GameViewModel
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import paristransitace.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GameScreen(
    gameViewModel: GameViewModel,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    onNavigateHome: () -> Unit,
) {

    val currentScenarioState by gameViewModel.currentScenario.collectAsStateWithLifecycle()
    val userStatsState by gameViewModel.userStatsState.collectAsStateWithLifecycle()
    val scenarioProgressState by gameViewModel.scenarioProgress.collectAsStateWithLifecycle(0f)

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val coroutineScope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        containerColor = VoidBlack,
        topBar = {
            ScreenHeader(
                modifier = Modifier.statusBarsPadding(),
                scenarioTitle = "${stringResource(Res.string.scenario).uppercase()} ${currentScenarioState?.id}",
                progress = scenarioProgressState,
                progressText = gameViewModel.scenarioProgressText.collectAsStateWithLifecycle("").value,
                goHome = onNavigateHome,
            )
        },
        sheetPeekHeight = 0.dp,
        sheetShape = RectangleShape,
        sheetContentColor = Color(0xFFEBDBB2),
        sheetContainerColor = VoidBlack,
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFFD79921), RoundedCornerShape(2.dp))
            )
        },
        sheetTonalElevation = 0.dp,
        modifier = Modifier.fillMaxSize(),
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {

                Text(
                    text = stringResource(Res.string.secure_uplink_established),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Green,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )

                AIChatWindow(
                    chatMessages = chatViewModel.chatMessages.collectAsState().value,
                    onSend = { msg -> chatViewModel.sendInGameMessage(
                        msg,
                        userViewModel.gameSetupState.value,
                        gameContext = gameViewModel.getGameContext()
                    ) },
                    isNewMessageEnabled = true,
                    modifier = Modifier.padding(bottom = 16.dp),
                    onChatMessageAction = {_,_ -> },
                    bgColor = VoidBlack,
                    isInActiveGame = true
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack)
                .consumeWindowInsets(padding)
                .padding(top = Dimens.Space.medium)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = Dimens.Space.medium)
            ) {

                StatsBar(
                    userStatsState,
                    onCommsClicked = {
                        coroutineScope.launch {
                            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                scaffoldState.bottomSheetState.hide()
                            } else {
                                scaffoldState.bottomSheetState.expand()
                            }
                        }
                    }
                )

                var isGameOver by remember { mutableStateOf(false) }

                when {
                    !isGameOver -> {
                        ScreenContent(
                            currentScenarioState,
                            onOptionSelected = { option ->
                                gameViewModel.updateStats(
                                    budgetImpact = option.budgetImpact,
                                    moraleImpact = option.moraleImpact,
                                    increaseLegalInfractionsBy = option.increaseLegalInfractionsBy
                                )
                            },
                            loadNextScenario = {
                                if (!gameViewModel.nextScenario()) {
                                    isGameOver = true
                                    gameViewModel.onGameOver(userStatsState)
                                }
                            }
                        )
                    }

                    else -> {
                        with(userStatsState) {

                            AnimatedGameOverOverlay(
                                moneyRemaining = budget,
                                moraleRemaining = morale,
                                legalInfractionsCount = legalInfractionsCount,
                                grade = gameViewModel.gameReport.value.grade,
                                reason = gameViewModel.gameReport.value.summary,
                                onRestart = {
                                    isGameOver = false
                                    gameViewModel.onRestart()
                                }
                            )
                        }
                    }
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
                        .padding(vertical = Dimens.Space.medium)
                        .fillMaxWidth()
                        .height(Dimens.Space.screenHeroHeight)
                        .border(
                            Dimens.Border.thin,
                            RetroAmber.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                ){
                    AnimatedScenarioImage(
                        currentScenario.scenarioTheme.toDrawable(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
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
                    modifier = Modifier.padding(top = Dimens.Space.tiny),
                    thickness = Dimens.Border.thin,
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
                    Box(modifier = Modifier.size(Dimens.Space.small).background(RetroAmber.copy(alpha = 0.5f)))
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
private fun ScreenHeader(
    scenarioTitle: String,
    progress: Float,
    progressText: String,
    goHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = Dimens.Space.medium, start = Dimens.Space.small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = Dimens.Space.small)
            ) {
                HomeButton(
                    goHome,
                    progress >= 1f
                    )

                Spacer(modifier = Modifier.width(Dimens.Space.small))

                if (scenarioTitle.isNotBlank()) {
                    Text(
                        text = scenarioTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f),
                    color = RetroAmber.copy(alpha = 0.5f),
                    trackColor = Color.DarkGray,
                )
                Spacer(modifier = Modifier.width(Dimens.Space.small))
                Text(progressText, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = Dimens.Space.small))
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
                .width(Dimens.Space.optionIndexWidth)
                .fillMaxHeight()
                .background(RetroAmber.copy(alpha = if (wasClicked) 0.5f else 0.05f))
                .border(width = Dimens.Space.zero, color = Color.Transparent)
                .padding(vertical = Dimens.Space.medium)
            ,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = RetroAmber
            )
        }

        Box(modifier = Modifier.width(Dimens.Space.thin).fillMaxHeight().background(Color.Gray.copy(alpha = 0.3f)))

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(Dimens.Space.medium),
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

        HorizontalDivider(color = RetroAmber.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = Dimens.Space.mediumSmall))

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