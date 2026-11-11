package com.nex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object NexColors {
    val Background = Color(0xFF05060A)
    val Cyan = Color(0xFF22D3EE)
    val Violet = Color(0xFF8B5CF6)
    val TextPrimary = Color(0xFFF2F4F8)
    val TextDim = Color(0xFF8A90A0)
}

private val NexDarkScheme = darkColorScheme(
    primary = NexColors.Cyan,
    secondary = NexColors.Violet,
    background = NexColors.Background,
    surface = NexColors.Background,
    onBackground = NexColors.TextPrimary,
    onSurface = NexColors.TextPrimary
)

@Composable
fun NexTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NexDarkScheme, content = content)
}
