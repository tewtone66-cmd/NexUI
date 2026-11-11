package com.nex.ui.home

import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nex.ui.data.NexPrefs
import com.nex.ui.system.Kiosk
import com.nex.ui.theme.NexColors
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(activity: FragmentActivity, prefs: NexPrefs) {
    val lang = prefs.language
    val t: (String) -> String = { HomeStr.get(lang, it) }
    var showControl by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var battery by remember { mutableStateOf(100) }
    val haptic = LocalHapticFeedback.current

    // Re-apply immersive + isolation every time we come back to the foreground.
    DisposableEffect(activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Kiosk.immersive(activity, true)
                if (prefs.kioskMode) Kiosk.apply(activity, true)
            }
        }
        activity.lifecycle.addObserver(observer)
        onDispose { activity.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        val bm = activity.getSystemService(BatteryManager::class.java)
        while (true) {
            now = LocalDateTime.now()
            bm?.let { battery = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) }
            delay(5_000)
        }
    }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(1400)
            toast = null
        }
    }

    val pages = remember { GridApps.chunked(12) }
    val pagerState = rememberPagerState(pageCount = { pages.size })

    val hello = t("hello")
    val comma = if (lang == "fa") "،" else ","
    val name = prefs.username
    val greeting = if (name != null) "$hello$comma $name" else hello
    val testLabel = t("test_app")

    val onAppTap: (MockApp) -> Unit = { app ->
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        toast = app.label(lang) + " · " + testLabel
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (lang == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Box(Modifier.fillMaxSize().wallpaper()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Top zone: swipe down here (or tap the status row) -> Nex Control Center
                Column(
                    Modifier.pointerInput(Unit) {
                        var total = 0f
                        detectVerticalDragGestures(
                            onDragStart = { total = 0f },
                            onVerticalDrag = { _, dy ->
                                total += dy
                                if (total > 70f) {
                                    showControl = true
                                    total = Float.NEGATIVE_INFINITY
                                }
                            }
                        )
                    }
                ) {
                    StatusRow(now, battery) { showControl = true }
                    Spacer(Modifier.height(16.dp))
                    ClockCard(now, lang, greeting)
                }

                Spacer(Modifier.height(20.dp))

                HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                    AppPage(pages[page], lang, onAppTap)
                }

                PageDots(pagerState.currentPage, pages.size)
                Spacer(Modifier.height(10.dp))
                SearchPill(t("search"))
                Spacer(Modifier.height(14.dp))
                Dock(lang, onAppTap)
            }

            AnimatedVisibility(
                visible = toast != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 190.dp),
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut()
            ) {
                Text(
                    text = toast ?: "",
                    color = NexColors.TextPrimary,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .glass(RoundedCornerShape(50), 0.16f)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            ControlCenter(
                visible = showControl,
                activity = activity,
                prefs = prefs,
                lang = lang,
                onDismiss = { showControl = false }
            )
        }
    }
}

/* ---------------------------- Pieces ---------------------------- */

@Composable
private fun StatusRow(now: LocalDateTime, battery: Int, onOpen: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .tap(onOpen)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)),
            color = NexColors.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text("Nex  •  🔋 $battery%", color = NexColors.TextDim, fontSize = 13.sp)
    }
}

@Composable
private fun ClockCard(now: LocalDateTime, lang: String, greeting: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(28.dp))
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(greeting, color = NexColors.TextDim, fontSize = 13.sp)
            Text(
                now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)),
                color = Color.White,
                fontSize = 52.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                now.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.forLanguageTag(lang))),
                color = NexColors.TextDim,
                fontSize = 14.sp
            )
        }
        // demo weather (test data)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("☀️", fontSize = 34.sp)
            Text("24°", color = NexColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun AppPage(apps: List<MockApp>, lang: String, onTap: (MockApp) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        apps.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { app ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        AppIcon(app, lang, showLabel = true) { onTap(app) }
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun AppIcon(app: MockApp, lang: String, showLabel: Boolean, onTap: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "icon-press"
    )
    val shape = RoundedCornerShape(17.dp)

    Column(
        modifier = Modifier
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onTap),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(app.top, app.bottom)))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                        startY = 0f,
                        endY = 80f
                    )
                )
                .border(0.8.dp, Color.White.copy(alpha = 0.15f), shape),
            contentAlignment = Alignment.Center
        ) {
            Text(app.emoji, fontSize = 28.sp)
        }
        if (showLabel) {
            Spacer(Modifier.height(6.dp))
            Text(
                app.label(lang),
                color = NexColors.TextPrimary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PageDots(current: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(total) { i ->
            Box(
                Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (i == current) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (i == current) 0.9f else 0.3f))
            )
        }
    }
}

@Composable
private fun SearchPill(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .glass(RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        Text("🔍  $text", color = NexColors.TextDim, fontSize = 14.sp)
    }
}

@Composable
private fun Dock(lang: String, onTap: (MockApp) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(30.dp), 0.10f)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DockApps.forEach { app ->
            AppIcon(app, lang, showLabel = false) { onTap(app) }
        }
    }
}

/* ---------------------------- Helpers ---------------------------- */

internal fun Modifier.glass(shape: Shape, alpha: Float = 0.08f): Modifier =
    this
        .clip(shape)
        .background(Color.White.copy(alpha = alpha))
        .border(1.dp, Color.White.copy(alpha = 0.12f), shape)

@Composable
internal fun Modifier.tap(onClick: () -> Unit): Modifier =
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )

private fun Modifier.wallpaper(): Modifier =
    this
        .background(
            Brush.verticalGradient(
                listOf(Color(0xFF0B1020), Color(0xFF1B1140), Color(0xFF0A1A2E))
            )
        )
        .drawBehind {
            val r = size.width * 0.8f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NexColors.Violet.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.25f),
                    radius = r
                ),
                radius = r,
                center = Offset(size.width * 0.2f, size.height * 0.25f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NexColors.Cyan.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.78f),
                    radius = r
                ),
                radius = r,
                center = Offset(size.width * 0.85f, size.height * 0.78f)
            )
        }
