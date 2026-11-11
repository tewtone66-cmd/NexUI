package com.nex.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nex.ui.boot.BootAnimation
import com.nex.ui.theme.NexColors
import com.nex.ui.theme.NexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexTheme {
                NexRoot()
            }
        }
    }
}

@Composable
fun NexRoot() {
    // rememberSaveable: boot plays once, not again on rotation
    var booted by rememberSaveable { mutableStateOf(false) }

    Crossfade(targetState = booted, label = "boot") { done ->
        if (done) HomePlaceholder() else BootAnimation(onFinished = { booted = true })
    }
}

@Composable
fun HomePlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().background(NexColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Text("Nex UI Home — coming soon", color = NexColors.TextDim)
    }
}
