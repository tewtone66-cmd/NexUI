package com.nex.ui.onboarding

data class NexLang(val code: String, val label: String, val rtl: Boolean)

val NexLanguages = listOf(
    NexLang("fa", "فارسی", rtl = true),
    NexLang("en", "English", rtl = false)
)

/** Simple in-code translations for the setup flow. Move to strings.xml later. */
object Str {
    private val fa = mapOf(
        "lang_title" to "زبان خود را انتخاب کن",
        "lang_desc" to "بعداً می‌تونی از تنظیمات عوضش کنی.",
        "continue" to "ادامه",
        "skip" to "فعلاً رد شو",
        "cancel" to "لغو",
        "wifi_title" to "اتصال به اینترنت",
        "wifi_desc" to "برای ورود و همگام‌سازی Nex ID بهتره به Wi-Fi وصل باشی.",
        "wifi_on" to "به اینترنت وصل هستی",
        "wifi_off" to "به اینترنت وصل نیستی",
        "wifi_open" to "باز کردن تنظیمات Wi-Fi",
        "id_title" to "Nex ID",
        "id_desc" to "یه حساب بساز یا وارد شو. این حساب هویت تو توی کل Nex UI هست.",
        "tab_create" to "ساخت حساب",
        "tab_login" to "ورود",
        "username" to "نام کاربری",
        "password" to "رمز عبور",
        "confirm" to "تکرار رمز عبور",
        "create_btn" to "ساخت Nex ID",
        "login_btn" to "ورود",
        "err_user_short" to "نام کاربری باید حداقل ۳ حرف باشه",
        "err_pass_short" to "رمز عبور باید حداقل ۶ کاراکتر باشه",
        "err_pass_mismatch" to "رمزها یکسان نیستن",
        "err_no_account" to "هنوز حسابی ساخته نشده، اول یه حساب بساز",
        "err_wrong" to "نام کاربری یا رمز اشتباهه",
        "bio_title" to "قفل زیستی",
        "bio_desc" to "با اثر انگشت یا تشخیص چهره (هر کدوم که توی گوشیت ثبت شده) وارد Nex UI شو.",
        "bio_enable" to "فعال‌سازی",
        "bio_unavailable" to "هیچ اثر انگشت یا چهره‌ای توی گوشی ثبت نشده. از تنظیمات سیستم اضافه کن و برگرد.",
        "bio_open" to "باز کردن تنظیمات",
        "bio_prompt_title" to "تأیید هویت",
        "bio_prompt_sub" to "برای فعال‌سازی قفل Nex UI",
        "finish" to "شروع"
    )

    private val en = mapOf(
        "lang_title" to "Choose your language",
        "lang_desc" to "You can change it later in settings.",
        "continue" to "Continue",
        "skip" to "Skip for now",
        "cancel" to "Cancel",
        "wifi_title" to "Connect to the internet",
        "wifi_desc" to "Being on Wi-Fi is best for signing in and syncing your Nex ID.",
        "wifi_on" to "You're connected",
        "wifi_off" to "You're not connected",
        "wifi_open" to "Open Wi-Fi settings",
        "id_title" to "Nex ID",
        "id_desc" to "Create an account or sign in. It's your identity across Nex UI.",
        "tab_create" to "Create account",
        "tab_login" to "Sign in",
        "username" to "Username",
        "password" to "Password",
        "confirm" to "Confirm password",
        "create_btn" to "Create Nex ID",
        "login_btn" to "Sign in",
        "err_user_short" to "Username must be at least 3 characters",
        "err_pass_short" to "Password must be at least 6 characters",
        "err_pass_mismatch" to "Passwords don't match",
        "err_no_account" to "No account yet - create one first",
        "err_wrong" to "Wrong username or password",
        "bio_title" to "Biometric lock",
        "bio_desc" to "Unlock Nex UI with your fingerprint or face, whichever is enrolled on your phone.",
        "bio_enable" to "Enable",
        "bio_unavailable" to "No fingerprint or face is enrolled on this phone. Add one in system settings, then come back.",
        "bio_open" to "Open settings",
        "bio_prompt_title" to "Verify it's you",
        "bio_prompt_sub" to "To enable Nex UI lock",
        "finish" to "Get started"
    )

    fun get(lang: String, key: String): String =
        (if (lang == "fa") fa else en)[key] ?: key
}
