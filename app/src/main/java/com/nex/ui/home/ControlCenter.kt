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
import androidx.compose.ui.text.font.FontFamily
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

    val panelShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
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
                    .clip(panelShape)
                    .background(Color(0xFF0A0A0A))
                    .border(1.dp, NexColors.Line, panelShape)
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(18.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Tile(Modifier.weight(1f), t("wifi"), if (online) "ON" else "OFF", online) {
                        val intent = if (Build.VERSION.SDK_INT >= 29) {
                            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                        } else {
                            Intent(Settings.ACTION_WIFI_SETTINGS)
                        }
                        openSystem(intent)
                    }
                    Tile(Modifier.weight(1f), t("bluetooth"), null, false) {
                        openSystem(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    }
                    Tile(Modifier.weight(1f), t("flash"), if (flash) "ON" else "OFF", flash) { toggleFlash() }
                    Tile(Modifier.weight(1f), t("isolate"), if (isolate) "ON" else "OFF", isolate) { toggleIsolation() }
                }

                Spacer(Modifier.height(22.dp))

                SliderRow(t("brightness"), brightness) {
                    brightness = it
                    val lp = activity.window.attributes
                    lp.screenBrightness = it.coerceIn(0.02f, 1f)
                    activity.window.attributes = lp
                }
                SliderRow(t("volume"), volume) {
                    volume = it
                    try {
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, (it * maxVol).roundToInt(), 0)
                    } catch (_: Exception) {
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text(t("ctrl_hint"), color = NexColors.TextDim, fontSize = 12.sp, lineHeight = 18.sp)

                Spacer(Modifier.height(14.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .width(36.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(NexColors.Line)
                    )
                }
            }
        }
    }
}

@Composable
private fun Tile(
    modifier: Modifier,
    label: String,
    state: String?,
    active: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val fill = if (active) Color.White else NexColors.Surface
    val fg = if (active) Color.Black else NexColors.TextPrimary
    val sub = if (active) Color.Black.copy(alpha = 0.55f) else NexColors.TextDim

    Column(
        modifier = modifier
            .height(76.dp)
            .clip(shape)
            .background(fill)
            .border(1.dp, if (active) Color.White else NexColors.Line, shape)
            .tap(onClick)
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        if (state != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                state,
                color = sub,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun SliderRow(label: String, value: Float, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = NexColors.TextDim, fontSize = 12.sp)
            Text(
                "${(value * 100).roundToInt()}",
                color = NexColors.TextDim,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = NexColors.Line
            )
        )
    }
}

private fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
    val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
