package com.nex.ui.home

import androidx.compose.ui.graphics.Color

/** Placeholder apps: they have icons and names but can't be opened. */
data class MockApp(
    val en: String,
    val fa: String,
    val emoji: String,
    val top: Color,
    val bottom: Color
) {
    fun label(lang: String): String = if (lang == "fa") fa else en
}

val DockApps = listOf(
    MockApp("Phone", "تلفن", "📞", Color(0xFF34D399), Color(0xFF059669)),
    MockApp("Messages", "پیام‌ها", "💬", Color(0xFF38BDF8), Color(0xFF2563EB)),
    MockApp("Browser", "مرورگر", "🌐", Color(0xFF60A5FA), Color(0xFF4F46E5)),
    MockApp("Camera", "دوربین", "📷", Color(0xFF6B7280), Color(0xFF1F2937))
)

val GridApps = listOf(
    MockApp("Photos", "گالری", "🖼️", Color(0xFFFB7185), Color(0xFFF59E0B)),
    MockApp("Maps", "نقشه", "🗺️", Color(0xFF4ADE80), Color(0xFF0EA5E9)),
    MockApp("Music", "موزیک", "🎵", Color(0xFFF43F5E), Color(0xFFBE185D)),
    MockApp("Files", "فایل‌ها", "📁", Color(0xFFFBBF24), Color(0xFFD97706)),
    MockApp("Notes", "یادداشت", "📝", Color(0xFFFDE047), Color(0xFFEAB308)),
    MockApp("Calendar", "تقویم", "📅", Color(0xFFF87171), Color(0xFFDC2626)),
    MockApp("Weather", "آب‌وهوا", "⛅", Color(0xFF38BDF8), Color(0xFF0369A1)),
    MockApp("Clock", "ساعت", "⏰", Color(0xFF374151), Color(0xFF111827)),
    MockApp("Calculator", "ماشین‌حساب", "🧮", Color(0xFFFB923C), Color(0xFF9A3412)),
    MockApp("Settings", "تنظیمات", "⚙️", Color(0xFF9CA3AF), Color(0xFF4B5563)),
    MockApp("Store", "فروشگاه", "🛍️", Color(0xFFA78BFA), Color(0xFF6D28D9)),
    MockApp("Terminal", "ترمینال", "💻", Color(0xFF1F2937), Color(0xFF064E3B)),
    MockApp("Wallet", "کیف پول", "💳", Color(0xFF1E3A8A), Color(0xFF0F172A)),
    MockApp("Health", "سلامت", "❤️", Color(0xFFF472B6), Color(0xFFE11D48)),
    MockApp("Mail", "ایمیل", "✉️", Color(0xFF3B82F6), Color(0xFF1D4ED8)),
    MockApp("Videos", "ویدیو", "🎬", Color(0xFFC084FC), Color(0xFF7E22CE))
)
