package com.alajemba.paristransitace.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.play_custom_scenario_button_label
import paristransitace.composeapp.generated.resources.save_custom_scenario_button_label

data class ChatUiModel(
    val id: Long,
    val sender: ChatMessageSender,
    val message: String,
    val actions: List<ChatMessageAction> = emptyList(),
    val selectedAction: ChatMessageAction? = null
)

enum class ChatMessageAction(val label: StringResource, val icon: ImageVector, val color: Color) {
    SAVE_SCENARIO(
        Res.string.save_custom_scenario_button_label,
        Icons.Default.Save,
        Color(0xFFD79921)
    ),
    PLAY_SCENARIO(
        Res.string.play_custom_scenario_button_label,
        Icons.Default.PlayArrow,
        Color(0xFF98971A)
    );

    companion object {
        fun fromString(action: String): ChatMessageAction? = entries.find { it.name.equals(action, ignoreCase = true) }
    }
}



enum class ChatMessageSender {
    AI, USER
}