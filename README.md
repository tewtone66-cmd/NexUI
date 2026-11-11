# NexUI ✨

Nex UI یک لانچر/شل موبایل‌محور برای اندروید، ساخته‌شده با **Kotlin + Jetpack Compose**.

## وضعیت
- [x] بوت انیمیشن اولیه
- [ ] هوم‌اسکرین
- [ ] سیستم کارت‌ها
- [ ] Nex Bar
- [ ] اپ‌های داخلی

## ساختار
```
app/src/main/java/com/nex/ui/
├── MainActivity.kt
├── boot/BootAnimation.kt
└── theme/NexTheme.kt
```

## لانچر کردن اپ
در `AndroidManifest.xml` برای Activity اصلی:
```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.HOME" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
```
