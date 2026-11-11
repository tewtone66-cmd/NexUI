package com.nex.ui.data

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Local storage for setup state + Nex ID (local-only for now).
 * Password is never stored in plain text: PBKDF2-SHA256 + random salt.
 * Later this can be replaced with a real backend login.
 */
class NexPrefs(context: Context) {
    private val sp = context.applicationContext
        .getSharedPreferences("nex_prefs", Context.MODE_PRIVATE)

    var language: String
        get() = sp.getString("lang", "fa") ?: "fa"
        set(v) { sp.edit().putString("lang", v).apply() }

    var setupDone: Boolean
        get() = sp.getBoolean("setup_done", false)
        set(v) { sp.edit().putBoolean("setup_done", v).apply() }

    var biometricEnabled: Boolean
        get() = sp.getBoolean("biometric_enabled", false)
        set(v) { sp.edit().putBoolean("biometric_enabled", v).apply() }

    /** Isolation mode: screen pinning so the phone's own notification shade / control center stay out. */
    var kioskMode: Boolean
        get() = sp.getBoolean("kiosk_mode", true)
        set(v) { sp.edit().putBoolean("kiosk_mode", v).apply() }

    val username: String? get() = sp.getString("nexid_user", null)
    val hasAccount: Boolean get() = username != null

    fun createAccount(user: String, password: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        sp.edit()
            .putString("nexid_user", user.trim())
            .putString("nexid_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("nexid_hash", Base64.encodeToString(hash(password, salt), Base64.NO_WRAP))
            .apply()
    }

    fun verify(user: String, password: String): Boolean {
        val savedUser = sp.getString("nexid_user", null) ?: return false
        val salt = sp.getString("nexid_salt", null)?.let { Base64.decode(it, Base64.NO_WRAP) } ?: return false
        val saved = sp.getString("nexid_hash", null)?.let { Base64.decode(it, Base64.NO_WRAP) } ?: return false
        if (!savedUser.equals(user.trim(), ignoreCase = true)) return false
        return MessageDigest.isEqual(saved, hash(password, salt))
    }

    private fun hash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}
