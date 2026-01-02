package com.alajemba.paristransitace.ui.components
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import com.alajemba.paristransitace.ui.theme.CrimsonRed
import com.alajemba.paristransitace.ui.theme.Dimens
import com.alajemba.paristransitace.ui.theme.VoidBlack

@Composable
fun AppDialog(
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    titleLabel: String,
    confirmLabel: String,
    dismissLabel: String,
) {
    // We start visible = false and set it to true immediately to trigger the "Enter" animation
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Spring Animation
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, // Makes it bounce
            stiffness = Spring.StiffnessLow // Makes it slow enough to see
        ),
        label = "BubblePop"
    )

    val shape = remember { RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp) }

    Column(
        modifier = Modifier
            .width(320.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = scale
                // This makes it grow FROM the tail (approx bottom-left)
                transformOrigin = TransformOrigin(0.2f, 1f)
            }
            .background(MaterialTheme.colorScheme.secondaryContainer, shape = shape)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),shape = shape)
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)
            .clickable(enabled = false) {},
    ) {

        Text(
            text = titleLabel,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = Dimens.Space.mediumSmall)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = VoidBlack),
                shape = RectangleShape
            ) {
                Text(confirmLabel, color = MaterialTheme.colorScheme.onBackground)
            }

            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                shape = RectangleShape
            ) {
                Text(dismissLabel, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}