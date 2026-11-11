package com.nex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Strict monochrome palette: black, white and greys only. */
object NexColors {
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF0D0D0D)
    val SurfaceHigh = Color(0xFF1A1A1A)
    val Line = Color(0xFF262626)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextDim = Color(0xFF8A8A8A)
    val Danger = Color(0xFFFF6B6B)
}

private val NexDarkScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = Color.White,
    onSecondary = Color.Black,
    background = NexColors.Background,
    surface = NexColors.Surface,
    onBackground = NexColors.TextPrimary,
    onSurface = NexColors.TextPrimary,
    outline = NexColors.Line
)

@Composable
fun NexTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NexDarkScheme, content = content)
}
