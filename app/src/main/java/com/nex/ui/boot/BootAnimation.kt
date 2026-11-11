package com.nex.ui.boot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nex.ui.theme.NexColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Nex UI boot animation (~2.6s), monochrome.
 * A thin white ring draws clockwise, "N" fades in, then the wordmark spreads out.
 */
@Composable
fun BootAnimation(onFinished: () -> Unit) {
    val ring = remember { Animatable(0f) }
    val logo = remember { Animatable(0f) }
    val title = remember { Animatable(0f) }
    val spacing = remember { Animatable(0f) }
    val screen = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { ring.animateTo(1f, tween(1200, easing = FastOutSlowInEasing)) }
            launch {
                delay(500)
                logo.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
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
                Canvas(modifier = Modifier.size(160.dp)) {
                    val stroke = 1.5.dp.toPx()
                    val arcSize = Size(size.width - stroke * 2, size.height - stroke * 2)
                    val topLeft = Offset(stroke, stroke)

                    // faint track
                    drawArc(
                        color = Color.White.copy(alpha = 0.10f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke)
                    )
                    // animated arc, starts at 12 o'clock
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f * ring.value,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "N",
                    color = NexColors.TextPrimary,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.alpha(logo.value)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "NEX UI",
                color = NexColors.TextDim,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (3f + 9f * spacing.value).sp,
                modifier = Modifier.alpha(title.value)
            )
        }
    }
}
