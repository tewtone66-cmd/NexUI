package com.nex.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.nex.ui.boot.BootAnimation
import com.nex.ui.data.NexPrefs
import com.nex.ui.onboarding.OnboardingFlow
import com.nex.ui.theme.NexColors
import com.nex.ui.theme.NexTheme

// AppCompatActivity (a FragmentActivity) is required by BiometricPrompt.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexTheme {
                NexRoot(activity = this)
            }
        }
    }
}

private const val PHASE_BOOT = 0
private const val PHASE_SETUP = 1
private const val PHASE_HOME = 2

@Composable
fun NexRoot(activity: FragmentActivity) {
    val prefs = remember { NexPrefs(activity) }
    // rememberSaveable: survives rotation, so boot/setup don't restart
    var phase by rememberSaveable { mutableStateOf(PHASE_BOOT) }

    Crossfade(targetState = phase, label = "phase") { p ->
        when (p) {
            PHASE_BOOT -> BootAnimation(
                onFinished = { phase = if (prefs.setupDone) PHASE_HOME else PHASE_SETUP }
            )
            PHASE_SETUP -> OnboardingFlow(
                prefs = prefs,
                activity = activity,
                onFinished = { phase = PHASE_HOME }
            )
            else -> HomePlaceholder(prefs)
        }
    }
}

@Composable
fun HomePlaceholder(prefs: NexPrefs) {
    Box(
        modifier = Modifier.fillMaxSize().background(NexColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Nex UI Home — ${prefs.username ?: "guest"}",
            color = NexColors.TextDim
        )
    }
}
