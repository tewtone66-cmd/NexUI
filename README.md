# NexUI ✨

Nex UI یک لانچر/شل موبایل‌محور برای اندروید، ساخته‌شده با **Kotlin + Jetpack Compose**.

## وضعیت
- [x] بوت انیمیشن
- [x] Setup اولیه: زبان ← Wi-Fi ← Nex ID ← قفل زیستی (اثر انگشت / چهره)
- [ ] هوم‌اسکرین
- [ ] سیستم کارت‌ها
- [ ] Nex Bar
- [ ] اپ‌های داخلی

## ساختار
```
app/src/main/java/com/nex/ui/
├── MainActivity.kt          (boot -> setup -> home)
├── boot/BootAnimation.kt
├── onboarding/
│   ├── OnboardingFlow.kt
│   └── NexStrings.kt        (fa / en)
├── data/NexPrefs.kt         (Nex ID محلی، رمز با PBKDF2 هش می‌شه)
├── security/Biometrics.kt
└── theme/NexTheme.kt
```

## Dependencyها (`app/build.gradle.kts`)
```kotlin
implementation("androidx.appcompat:appcompat:1.7.0")
implementation("androidx.biometric:biometric:1.1.0")
```

## Manifest
```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.USE_BIOMETRIC" />

<activity android:name=".MainActivity" android:exported="true"
    android:theme="@style/Theme.AppCompat.DayNight.NoActionBar">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

## نکات
- اندروید اجازه نمی‌ده اپ مستقیم به Wi-Fi وصل بشه؛ پس صفحه‌ی تنظیمات Wi-Fi سیستم باز می‌شه.
- روش قفل زیستی (چهره یا اثر انگشت) رو خود سیستم بر اساس چیزی که توی گوشی ثبت شده انتخاب می‌کنه.
- Nex ID فعلاً کاملاً محلیه (روی خود گوشی)؛ بعداً می‌شه به بک‌اند وصلش کرد.
