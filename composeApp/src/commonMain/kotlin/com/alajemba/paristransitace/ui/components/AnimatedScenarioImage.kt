package com.alajemba.paristransitace.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.alajemba.paristransitace.ui.theme.RetroAmber
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource


@Composable
fun AnimatedScenarioImage(
    imageRes: DrawableResource,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    DreamyAmoebaImage(
        imageRes = imageRes,
        contentDescription = contentDescription,
        modifier = modifier
    )
}
@Composable
fun DreamyAmoebaImage(
    imageRes: DrawableResource,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    // These values animate between negative and positive to create concave/convex warping
    val m1 by infiniteTransition.animateFloat(
        initialValue = -0.8f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse)
    )
    val m2 by infiniteTransition.animateFloat(
        initialValue = -0.6f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Reverse)
    )
    val m3 by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = -0.7f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse)
    )
    val m4 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = -0.9f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse)
    )

    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 1.05f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse)
    )
    val panXAnim by infiniteTransition.animateFloat(
        initialValue = -15f, targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(12000, easing = EaseInOutQuad), RepeatMode.Reverse)
    )

    val wobblyShape = remember(m1, m2, m3, m4) {
        WobblySquareShape(m1, m2, m3, m4)
    }

    Box(
        modifier = modifier
            .padding(12.dp) // Padding to allow wobble room
            .clip(wobblyShape)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(scaleAnim)
                .offset(x = panXAnim.dp)
        )

        // Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                    )
                )
        )

        // Retro Amber Tint
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RetroAmber.copy(alpha = 0.05f))
        )
    }
}

class WobblySquareShape(
    private val m1: Float,
    private val m2: Float,
    private val m3: Float,
    private val m4: Float
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        // How wildly the edges distort
        val intensity = 25f * density.density

        val path = Path().apply {
            moveTo(0f, 0f)
            // Top Edge
            quadraticTo(w / 2f, 0f + (m1 * intensity), w, 0f)
            // Right Edge
            quadraticTo(w + (m2 * intensity), h / 2f, w, h)
            // Bottom Edge
            quadraticTo(w / 2f, h + (m3 * intensity), 0f, h)
            // Left Edge
            quadraticTo(0f - (m4 * intensity), h / 2f, 0f, 0f)
            close()


        }
        return Outline.Generic(path)
    }
}