package com.nex.ui.onboarding

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nex.ui.data.NexPrefs
import com.nex.ui.security.Biometrics
import com.nex.ui.theme.NexColors

private const val STEP_COUNT = 4

/**
 * First-run setup: Language -> Wi-Fi -> Nex ID -> Biometric -> Home
 */
@Composable
fun OnboardingFlow(
    prefs: NexPrefs,
    activity: FragmentActivity,
    onFinished: () -> Unit
) {
    var lang by rememberSaveable { mutableStateOf(prefs.language) }
    var step by rememberSaveable { mutableStateOf(0) }
    val rtl = NexLanguages.firstOrNull { it.code == lang }?.rtl ?: false

    CompositionLocalProvider(
        LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NexColors.Background)
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            StepDots(current = step, total = STEP_COUNT)
            Spacer(Modifier.height(28.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
                label = "setup-step",
                modifier = Modifier.weight(1f)
            ) { s ->
                val t: (String) -> String = { key -> Str.get(lang, key) }
                when (s) {
                    0 -> LanguageStep(
                        lang = lang,
                        t = t,
                        onSelect = { lang = it; prefs.language = it },
                        onNext = { step = 1 }
                    )
                    1 -> WifiStep(activity, t, onNext = { step = 2 })
                    2 -> NexIdStep(prefs, t, onNext = { step = 3 })
                    else -> BiometricStep(
                        prefs = prefs,
                        activity = activity,
                        t = t,
                        onFinish = {
                            prefs.setupDone = true
                            onFinished()
                        }
                    )
                }
            }
        }
    }
}

/* ------------------------------ Steps ------------------------------ */

@Composable
private fun LanguageStep(
    lang: String,
    t: (String) -> String,
    onSelect: (String) -> Unit,
    onNext: () -> Unit
) {
    StepScaffold(
        title = t("lang_title"),
        desc = t("lang_desc"),
        primaryText = t("continue"),
        onPrimary = onNext
    ) {
        NexLanguages.forEach { l ->
            NexCard(selected = l.code == lang, onClick = { onSelect(l.code) }) {
                Text(
                    l.label,
                    color = NexColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                if (l.code == lang) Text("✓", color = NexColors.Cyan, fontSize = 20.sp)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun WifiStep(
    activity: FragmentActivity,
    t: (String) -> String,
    onNext: () -> Unit
) {
    var online by remember { mutableStateOf(isOnline(activity)) }
    OnResume(activity) { online = isOnline(activity) }

    StepScaffold(
        title = t("wifi_title"),
        desc = t("wifi_desc"),
        primaryText = t("continue"),
        primaryEnabled = online,
        onPrimary = onNext,
        secondaryText = if (online) null else t("skip"),
        onSecondary = onNext
    ) {
        NexCard(selected = online, onClick = {}) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (online) Color(0xFF34D399) else Color(0xFFF87171))
            )
            Spacer(Modifier.size(12.dp))
            Text(
                if (online) t("wifi_on") else t("wifi_off"),
                color = NexColors.TextPrimary,
                fontSize = 16.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        // Android doesn't let apps list/connect to Wi-Fi networks directly,
        // so we open the system Wi-Fi screen and re-check on return.
        TextButton(
            onClick = { activity.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(t("wifi_open"), color = NexColors.Cyan, fontSize = 15.sp)
        }
    }
}

@Composable
private fun NexIdStep(
    prefs: NexPrefs,
    t: (String) -> String,
    onNext: () -> Unit
) {
    var creating by rememberSaveable { mutableStateOf(!prefs.hasAccount) }
    var user by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var errorKey by rememberSaveable { mutableStateOf<String?>(null) }

    val submit: () -> Unit = {
        errorKey = when {
            creating && user.trim().length < 3 -> "err_user_short"
            creating && pass.length < 6 -> "err_pass_short"
            creating && pass != confirm -> "err_pass_mismatch"
            !creating && !prefs.hasAccount -> "err_no_account"
            !creating && !prefs.verify(user, pass) -> "err_wrong"
            else -> null
        }
        if (errorKey == null) {
            if (creating) prefs.createAccount(user, pass)
            onNext()
        }
    }

    StepScaffold(
        title = t("id_title"),
        desc = t("id_desc"),
        primaryText = if (creating) t("create_btn") else t("login_btn"),
        onPrimary = submit
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .padding(4.dp)
        ) {
            Segment(t("tab_create"), creating, Modifier.weight(1f)) { creating = true; errorKey = null }
            Segment(t("tab_login"), !creating, Modifier.weight(1f)) { creating = false; errorKey = null }
        }
        Spacer(Modifier.height(20.dp))

        NexField(user, { user = it }, t("username"))
        Spacer(Modifier.height(12.dp))
        NexField(pass, { pass = it }, t("password"), password = true)
        if (creating) {
            Spacer(Modifier.height(12.dp))
            NexField(confirm, { confirm = it }, t("confirm"), password = true)
        }

        errorKey?.let {
            Spacer(Modifier.height(12.dp))
            Text(t(it), color = Color(0xFFF87171), fontSize = 14.sp)
        }
    }
}

@Composable
private fun BiometricStep(
    prefs: NexPrefs,
    activity: FragmentActivity,
    t: (String) -> String,
    onFinish: () -> Unit
) {
    var enabled by rememberSaveable { mutableStateOf(prefs.biometricEnabled) }
    var available by remember { mutableStateOf(Biometrics.isAvailable(activity)) }
    OnResume(activity) { available = Biometrics.isAvailable(activity) }

    StepScaffold(
        title = t("bio_title"),
        desc = t("bio_desc"),
        primaryText = if (enabled) t("finish") else t("bio_enable"),
        primaryEnabled = enabled || available,
        onPrimary = {
            if (enabled) {
                onFinish()
            } else {
                Biometrics.prompt(
                    activity = activity,
                    title = t("bio_prompt_title"),
                    subtitle = t("bio_prompt_sub"),
                    negative = t("cancel")
                ) { ok ->
                    if (ok) {
                        prefs.biometricEnabled = true
                        enabled = true
                    }
                }
            }
        },
        secondaryText = if (enabled) null else t("skip"),
        onSecondary = onFinish
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(if (enabled) "✅" else "🔐", fontSize = 72.sp)
        }

        if (!available && !enabled) {
            Text(t("bio_unavailable"), color = NexColors.TextDim, fontSize = 14.sp, lineHeight = 20.sp)
            TextButton(
                onClick = {
                    val action = if (Build.VERSION.SDK_INT >= 30) {
                        Settings.ACTION_BIOMETRIC_ENROLL
                    } else {
                        Settings.ACTION_SECURITY_SETTINGS
                    }
                    activity.startActivity(Intent(action))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(t("bio_open"), color = NexColors.Cyan, fontSize = 15.sp)
            }
        }
    }
}

/* --------------------------- Shared pieces --------------------------- */

@Composable
private fun StepScaffold(
    title: String,
    desc: String,
    primaryText: String,
    primaryEnabled: Boolean = true,
    onPrimary: () -> Unit,
    secondaryText: String? = null,
    onSecondary: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text(title, color = NexColors.TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(desc, color = NexColors.TextDim, fontSize = 15.sp, lineHeight = 22.sp)
        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            content = content
        )

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onPrimary,
            enabled = primaryEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NexColors.Cyan,
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.08f),
                disabledContentColor = NexColors.TextDim
            )
        ) {
            Text(primaryText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        if (secondaryText != null) {
            TextButton(onClick = onSecondary, modifier = Modifier.fillMaxWidth()) {
                Text(secondaryText, color = NexColors.TextDim)
            }
        }
    }
}

@Composable
private fun StepDots(current: Int, total: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i <= current) NexColors.Cyan else Color.White.copy(alpha = 0.1f))
            )
        }
    }
}

@Composable
private fun NexCard(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.5.dp, if (selected) NexColors.Cyan else Color.White.copy(alpha = 0.08f), shape)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun Segment(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) NexColors.Cyan.copy(alpha = 0.18f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) NexColors.Cyan else NexColors.TextDim,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun NexField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    password: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (password) KeyboardType.Password else KeyboardType.Text
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = NexColors.TextPrimary,
            unfocusedTextColor = NexColors.TextPrimary,
            focusedBorderColor = NexColors.Cyan,
            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
            focusedLabelColor = NexColors.Cyan,
            unfocusedLabelColor = NexColors.TextDim,
            cursorColor = NexColors.Cyan
        )
    )
}

@Composable
private fun OnResume(activity: FragmentActivity, block: () -> Unit) {
    val current by rememberUpdatedState(block)
    DisposableEffect(activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) current()
        }
        activity.lifecycle.addObserver(observer)
        onDispose { activity.lifecycle.removeObserver(observer) }
    }
}

private fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
    val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
