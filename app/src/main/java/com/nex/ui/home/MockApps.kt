package com.nex.ui.home

/** Placeholder apps: a short monogram and a name; they can't be opened. */
data class MockApp(
    val en: String,
    val fa: String,
    val mark: String
) {
    fun label(lang: String): String = if (lang == "fa") fa else en
}

val DockApps = listOf(
    MockApp("Phone", "تلفن", "Ph"),
    MockApp("Messages", "پیام‌ها", "Ms"),
    MockApp("Browser", "مرورگر", "Br"),
    MockApp("Camera", "دوربین", "Cm")
)

val GridApps = listOf(
    MockApp("Photos", "گالری", "Pt"),
    MockApp("Maps", "نقشه", "Mp"),
    MockApp("Music", "موزیک", "Mu"),
    MockApp("Files", "فایل‌ها", "Fl"),
    MockApp("Notes", "یادداشت", "Nt"),
    MockApp("Calendar", "تقویم", "Cl"),
    MockApp("Weather", "آب‌وهوا", "Wx"),
    MockApp("Clock", "ساعت", "Ck"),
    MockApp("Calculator", "ماشین‌حساب", "Cc"),
    MockApp("Settings", "تنظیمات", "St"),
    MockApp("Store", "فروشگاه", "Sr"),
    MockApp("Terminal", "ترمینال", ">_"),
    MockApp("Wallet", "کیف پول", "Wl"),
    MockApp("Health", "سلامت", "Hl"),
    MockApp("Mail", "ایمیل", "Ml"),
    MockApp("Videos", "ویدیو", "Vd")
)
