package com.nex.ui.home

object HomeStr {
    private val fa = mapOf(
        "hello" to "سلام",
        "test_app" to "این یه اپ تستیه و باز نمی‌شه",
        "search" to "جستجو در Nex",
        "ctrl_title" to "مرکز کنترل Nex",
        "wifi" to "Wi-Fi",
        "bluetooth" to "بلوتوث",
        "flash" to "چراغ قوه",
        "isolate" to "حالت ایزوله",
        "brightness" to "روشنایی",
        "volume" to "صدا",
        "ctrl_hint" to "این مرکز کنترل مخصوص Nex UI هست و از مرکز کنترل گوشی جداست."
    )

    private val en = mapOf(
        "hello" to "Hello",
        "test_app" to "test app - can't be opened",
        "search" to "Search Nex",
        "ctrl_title" to "Nex Control Center",
        "wifi" to "Wi-Fi",
        "bluetooth" to "Bluetooth",
        "flash" to "Flashlight",
        "isolate" to "Isolation",
        "brightness" to "Brightness",
        "volume" to "Volume",
        "ctrl_hint" to "This control center belongs to Nex UI and is separate from the phone's own."
    )

    fun get(lang: String, key: String): String =
        (if (lang == "fa") fa else en)[key] ?: key
}
