package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalNovaTokens
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NovaLogo(
    size: Dp = 48.dp,
    isAnimated: Boolean = true,
    modifier: Modifier = Modifier
) {
    val tokens = LocalNovaTokens.current
    val infiniteTransition = rememberInfiniteTransition(label = "nova_logo_pulse")

    val pulseScale by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1.0f) }
    }

    val glowAlpha by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.75f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0.5f) }
    }

    val sparkRotation by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spark_rot"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Radiant background orb aura
        Box(
            modifier = Modifier
                .size(size * 0.9f * pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            tokens.primary.copy(alpha = glowAlpha),
                            tokens.secondary.copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Custom drawn Cute Glowing Star & Intelligent Sparks Canvas
        Canvas(modifier = Modifier.size(size * 0.75f)) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f
            val outerRadius = (canvasWidth / 2f) * 0.88f
            val innerRadius = outerRadius * 0.38f

            // 1. Draw 4-point Cute Glowing Star with bezier curves
            val starPath = Path().apply {
                val numPoints = 4
                val angleStep = Math.PI / numPoints

                for (i in 0 until (numPoints * 2)) {
                    val r = if (i % 2 == 0) outerRadius else innerRadius
                    val angle = i * angleStep - (Math.PI / 2.0)
                    val x = (centerX + r * cos(angle)).toFloat()
                    val y = (centerY + r * sin(angle)).toFloat()
                    if (i == 0) {
                        moveTo(x, y)
                    } else {
                        lineTo(x, y)
                    }
                }
                close()
            }

            // Outer subtle stroke
            drawPath(
                path = starPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.9f), tokens.secondary, tokens.primary)
                ),
                style = Fill
            )

            // Inner cute Core Light
            drawCircle(
                color = Color.White.copy(alpha = 0.95f),
                radius = innerRadius * 0.55f,
                center = Offset(centerX, centerY)
            )

            // 2. Intelligent Spark element (top right)
            val radAngle = Math.toRadians(sparkRotation.toDouble() - 45.0)
            val sparkDist = outerRadius * 0.95f
            val sparkX = (centerX + sparkDist * cos(radAngle)).toFloat()
            val sparkY = (centerY + sparkDist * sin(radAngle)).toFloat()

            drawCircle(
                color = tokens.tertiary,
                radius = outerRadius * 0.16f,
                center = Offset(sparkX, sparkY)
            )
            drawCircle(
                color = Color.White,
                radius = outerRadius * 0.08f,
                center = Offset(sparkX, sparkY)
            )

            // Small Spark (bottom left)
            val radAngle2 = Math.toRadians(sparkRotation.toDouble() + 135.0)
            val sparkDist2 = outerRadius * 0.85f
            val sparkX2 = (centerX + sparkDist2 * cos(radAngle2)).toFloat()
            val sparkY2 = (centerY + sparkDist2 * sin(radAngle2)).toFloat()

            drawCircle(
                color = tokens.secondary,
                radius = outerRadius * 0.11f,
                center = Offset(sparkX2, sparkY2)
            )
        }
    }
}
