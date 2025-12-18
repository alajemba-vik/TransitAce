package com.alajemba.paristransitace.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.RetroAmber
import com.alajemba.paristransitace.ui.theme.VoidBlack
import com.alajemba.paristransitace.ui.viewmodels.GameViewModel
import com.alajemba.paristransitace.ui.viewmodels.UserViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun GameScreen(
    gameViewModel: GameViewModel,
    userViewModel: UserViewModel,
    onGameOver: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .statusBarsPadding()
            .safeDrawingPadding()
            .padding(Dimens.Space.medium)
    ) {

        GameHeader(
            scenarioTitle = "SCENARIO 1", // You can map this from state.scenarioIndex
            progress = 0.12f, // 1/8
            money = state.money,
            morale = state.morale,
            alerts = state.alerts
        )

        Spacer(modifier = Modifier.height(Dimens.Space.large))

        // 2. SCROLLABLE CONTENT (Title + Story + Actions)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space.medium)
        ) {
            // A. The Black Void / Image Placeholder
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Height from screenshot
                        .background(Color.Black)
                        .border(Dimens.Border.thin, RetroAmber.copy(alpha = 0.1f))
                )
            }

            // B. The Boxed Title ("The Morning in Orly")
            item {
                Box(
                    modifier = Modifier
                        .border(Dimens.Border.thin, RetroAmber)
                        .padding(horizontal = Dimens.Space.medium, vertical = Dimens.Space.small)
                ) {
                    Text(
                        text = state.currentScenarioTitle,
                        style = MaterialTheme.typography.labelLarge, // VT323
                        color = RetroAmber
                    )
                }
            }

            // C. Log Label
            item {
                Text(
                    text = "LOG: SCENARIO_0",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray.copy(alpha = 0.5f)
                )
                Divider(
                    color = Color.Gray.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // D. The Story Text
            item {
                // This uses the "Crimson Pro" font (bodyLarge) per your theme
                Text(
                    text = "You wake up in your tiny apartment in Orly. That Uber from the airport last night drained your bank account. You have €100 left. Cardboard tickets are history. How do you prepare for the day?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground // PaperText
                )
            }

            // E. "AVAILABLE ACTIONS" Label
            item {
                Spacer(modifier = Modifier.height(Dimens.Space.small))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(RetroAmber.copy(alpha = 0.5f)))
                    Spacer(modifier = Modifier.width(Dimens.Space.small))
                    Text(
                        text = "AVAILABLE ACTIONS:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }

            // F. The Buttons
            items(state.availableOptions) { option ->
                ActionRow(
                    index = "1", // You can calculate this: state.availableOptions.indexOf(option) + 1
                    text = option.text,
                    onClick = { gameViewModel.onOptionSelected(option) }
                )
                Spacer(modifier = Modifier.height(Dimens.Space.small))
            }
        }
    }
}


@Composable
fun GameHeader(
    scenarioTitle: String,
    progress: Float,
    money: Double,
    morale: Int,
    alerts: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Row 1: Title + Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = scenarioTitle,
                style = MaterialTheme.typography.headlineMedium, // Big VT323
                color = Color.Gray
            )

            // The Progress Lines (Visual only)
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.width(100.dp),
                    color = RetroAmber.copy(alpha = 0.5f),
                    trackColor = Color.DarkGray,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("1/8", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }

        Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

        // Row 2: Stats + Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, // Pushes Map buttons to right
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stats Group
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatItem(value = "$money", label = "", color = RetroAmber) // Money
                StatItem(value = "$morale%", label = "", color = RetroAmber) // Morale
                StatItem(value = "$alerts", label = "", color = if(alerts>0) MaterialTheme.colorScheme.error else RetroAmber) // Alerts
            }

            // Toggles Group (Map / Comms)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToggleButton(text = "MAP", isActive = false)
                ToggleButton(text = "COMMS", isActive = false)
            }
        }
    }
}

@Composable
fun ActionRow(index: String, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Forces same height
            .border(Dimens.Border.thin, Color.Gray.copy(alpha = 0.3f))
            .clickable { onClick() }
    ) {
        // 1. The Number Box
        Box(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight()
                .background(RetroAmber.copy(alpha = 0.05f))
                .border(width = 0.dp, color = Color.Transparent) // Right border logic could go here
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index,
                style = MaterialTheme.typography.headlineSmall, // Big Number
                color = RetroAmber
            )
        }

        // Vertical Divider
        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.Gray.copy(alpha = 0.3f)))

        // 2. The Text
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge, // Serif font for the action description
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

// Small Helper for the "100.00 | 30%" text
@Composable
fun StatItem(value: String, label: String, color: Color) {
    Text(
        text = value, // You can prepend label if you want
        style = MaterialTheme.typography.labelLarge,
        color = color,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    )
}

@Composable
fun ToggleButton(text: String, isActive: Boolean) {
    Box(
        modifier = Modifier
            .border(1.dp, if (isActive) RetroAmber else Color.Gray.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) RetroAmber else Color.Gray
        )
    }
}

@Preview
@Composable
private fun GameScreenPreview() {
    GameScreen(onGameOver = { _, _ -> })

}
