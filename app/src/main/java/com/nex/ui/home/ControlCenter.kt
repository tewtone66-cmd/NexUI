package com.nex.ui.home

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.nex.ui.data.NexPrefs
import com.nex.ui.system.Kiosk
import com.nex.ui.theme.NexColors
import kotlin.math.roundToInt

/**
 * Nex Control Center - slides down from the top of Nex UI.
 * It's separate from the phone's own control center.
 *
 * Real controls: flashlight, brightness (this window), music volume, isolation mode.
 * Wi-Fi / Bluetooth can't be toggled by apps on modern Android, so those tiles
 * open the system panel/settings.
 */
@Composable
fun ControlCenter(
    visible: Boolean,
    activity: FragmentActivity,
    prefs: NexPrefs,
    lang: String,
    onDismiss: () -> Unit
) {
    val t: (String) -> String = { HomeStr.get(lang, it) }

    var flash by remember { mutableStateOf(false) }
    var isolate by remember { mutableStateOf(prefs.kioskMode) }
    val online = remember(visible) { isOnline(activity) }

    var brightness by remember {
        mutableStateOf(activity.window.attributes.screenBrightness.let { if (it < 0f) 0.6f else it })
    }
    val audio = remember { activity.getSystemService(AudioManager::class.java) }
    val maxVol = remember { audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
    var volume by remember {
        mutableStateOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVol)
    }

    fun openSystem(intent: Intent) {
        // Screen pinning blocks other screens, so release it first.
        // HomeScreen re-applies isolation when we come back.
        if (isolate) Kiosk.apply(activity, false)
        try {
            activity.startActivity(intent)
        } catch (_: Exception) {
        }
        onDismiss()
    }

    fun toggleFlash() {
        try {
            val cm = activity.getSystemService(CameraManager::class.java)
            val id = cm.cameraIdList.firstOrNull {
                cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return
            cm.setTorchMode(id, !flash)
            flash = !flash
        } catch (_: Exception) {
        }
    }

    fun toggleIsolation() {
        isolate = !isolate
        prefs.kioskMode = isolate
        Kiosk.apply(activity, isolate)
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .tap(onDismiss)
            )
        }

        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Color(0xF2121426))
                    .pointerInput(Unit) {
                        var total = 0f
                        detectVerticalDragGestures(
                            onDragStart = { total = 0f },
                            onVerticalDrag = { _, dy ->
                                total += dy
                                if (total < -60f) {
                                    onDismiss()
                                    total = 0f
                                }
                            }
                        )
                    }
                    .safeDrawingPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    t("ctrl_title"),
                    color = NexColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(18.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Tile(Modifier.weight(1f), "📶", t("wifi"), online) {
                        val intent = if (Build.VERSION.SDK_INT >= 29) {
                            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                        } else {
                            Intent(Settings.ACTION_WIFI_SETTINGS)
                        }
                        openSystem(intent)
                    }
                    Tile(Modifier.weight(1f), "🔵", t("bluetooth"), false) {
                        openSystem(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    }
                    Tile(Modifier.weight(1f), "🔦", t("flash"), flash) { toggleFlash() }
                    Tile(Modifier.weight(1f), "🛡️", t("isolate"), isolate) { toggleIsolation() }
                }

                Spacer(Modifier.height(18.dp))

                SliderRow(t("brightness"), "☀️", brightness) {
                    brightness = it
                    val lp = activity.window.attributes
                    lp.screenBrightness = it.coerceIn(0.02f, 1f)
                    activity.window.attributes = lp
                }
                SliderRow(t("volume"), "🔊", volume) {
                    volume = it
                    try {
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, (it * maxVol).roundToInt(), 0)
                    } catch (_: Exception) {
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text(t("ctrl_hint"), color = NexColors.TextDim, fontSize = 12.sp, lineHeight = 18.sp)

                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                    )
                }
            }
        }
    }
}

@Composable
private fun Tile(
    modifier: Modifier,
    emoji: String,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Column(modifier.tap(onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(shape)
                .background(if (active) NexColors.Cyan.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f))
                .border(
                    1.dp,
                    if (active) NexColors.Cyan.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.10f),
                    shape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 28.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = NexColors.TextDim, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun SliderRow(label: String, emoji: String, value: Float, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Text(label, color = NexColors.TextDim, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 20.sp)
            Slider(
                value = value,
                onValueChange = onChange,
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                colors = SliderDefaults.colors(
                    thumbColor = NexColors.Cyan,
                    activeTrackColor = NexColors.Cyan,
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                )
            )
        }
    }
}

private fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
    val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
