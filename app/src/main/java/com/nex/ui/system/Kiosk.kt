package com.nex.ui.system

import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Isolation helpers.
 *
 * Android doesn't let a normal app disable the system control center outright.
 * The closest we can get without device-owner (kiosk) setup:
 *  - startLockTask(): screen pinning - blocks the notification shade, home and recents
 *    until the user exits pinning with the system gesture.
 *  - immersive mode: hides the status/navigation bars.
 */
object Kiosk {
    fun apply(activity: Activity, enabled: Boolean) {
        try {
            if (enabled) activity.startLockTask() else activity.stopLockTask()
        } catch (_: Exception) {
            // not pinned / pinning unavailable - ignore
        }
    }

    fun immersive(activity: Activity, hide: Boolean) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (hide) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
