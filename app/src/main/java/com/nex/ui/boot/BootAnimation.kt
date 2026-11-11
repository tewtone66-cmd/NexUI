package com.nex.ui.boot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nex.ui.theme.NexColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Nex UI boot animation (~2.6s)
 *
 * 0.0s  glow starts fading in
 * 0.0s  ring draws clockwise (1.2s)
 * 0.5s  "N" fades + scales in
 * 0.8s  "NEX UI" fades in while letter-spacing expands
 * 2.2s  whole screen fades out -> onFinished()
 */
@Composable
fun BootAnimation(onFinished: () -> Unit) {
    val ring = remember { Animatable(0f) }
    val glow = remember { Animatable(0f) }
    val logo = remember { Animatable(0f) }
    val title = remember { Animatable(0f) }
    val spacing = remember { Animatable(0f) }
    val screen = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { ring.animateTo(1f, tween(1200, easing = FastOutSlowInEasing)) }
            launch { glow.animateTo(1f, tween(1500, easing = LinearEasing)) }
            launch {
                delay(500)
                logo.animateTo(1f, tween(600, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)))
            }
            launch {
                delay(800)
                title.animateTo(1f, tween(700))
            }
            launch {
                delay(800)
                spacing.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
            }
        }
        delay(500)
        screen.animateTo(0f, tween(400))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexColors.Background)
            .alpha(screen.value),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val stroke = 5.dp.toPx()
                    val inset = stroke * 2
                    val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                    val radius = arcSize.minDimension / 2

                    // soft glow behind the ring
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NexColors.Cyan.copy(alpha = 0.22f * glow.value),
                                NexColors.Violet.copy(alpha = 0.10f * glow.value),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension * 0.9f
                        ),
                        radius = size.minDimension * 0.9f,
                        center = center
                    )

                    // faint track
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = stroke)
                    )

                    // animated gradient arc (starts at 12 o'clock)
                    rotate(degrees = -90f, pivot = center) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(NexColors.Violet, NexColors.Cyan, NexColors.Violet),
                                center = center
                            ),
                            startAngle = 0f,
                            sweepAngle = 360f * ring.value,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }

                Text(
                    text = "N",
                    color = NexColors.TextPrimary,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .alpha(logo.value)
                        .scale(0.7f + 0.3f * logo.value)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "NEX UI",
                color = NexColors.TextDim,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (3f + 9f * spacing.value).sp,
                modifier = Modifier.alpha(title.value)
            )
        }
    }
}
