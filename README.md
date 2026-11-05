# Wavi Companion

A premium Android companion application for accessing your existing WhatsApp account through the official WhatsApp Web experience.

> **Disclaimer:** Wavi Companion is an independent third-party application. It is not affiliated with, endorsed by, or connected to WhatsApp LLC or Meta Platforms, Inc.

## Features

- **Native Android Experience** — Built with Kotlin, Jetpack Compose, and Material 3 for a polished, modern interface.
- **Official WhatsApp Web Integration** — Connects through the official Linked Devices / WhatsApp Web mechanism using Android WebView.
- **QR Authentication** — Scan the QR code from your primary WhatsApp device to link.
- **Session Persistence** — Maintains your linked session across app restarts.
- **Premium Dashboard** — Displays connection status and quick actions in a clean, card-based layout.
- **Light & Dark Themes** — Supports light, dark, and system-default theme modes.
- **Security First** — HTTPS only, no data collection, no third-party analytics, no credential storage.
- **Optimized for Samsung Galaxy A52s 5G** — Tuned for Snapdragon 778G with smooth 120Hz performance.

## Screenshots

<!-- Add screenshots here after building -->
| Splash | Onboarding | Dashboard | WhatsApp | Settings |
|--------|------------|-----------|----------|----------|
| *TBD*  | *TBD*      | *TBD*     | *TBD*    | *TBD*    |

## Requirements

- Android 8.0 (API 26) or higher
- Active internet connection
- An existing WhatsApp account on another device

## Building

### Prerequisites

- JDK 17
- Android SDK (API 34)

The project uses the Gradle Wrapper, so no separate Gradle installation is needed.

### Build Debug APK

```bash
./gradlew assembleDebug
```

The debug APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK

Configure your signing keystore (see [RELEASE_GUIDE.md](RELEASE_GUIDE.md)), then:

```bash
./gradlew assembleRelease
```

## Installation

### From APK (Debug)

1. Transfer `app-debug.apk` to your Android device.
2. Open the APK file on your device.
3. If prompted, enable "Install from unknown sources" for your file manager.
4. Tap **Install**.
5. Open **Wavi Companion** from your app launcher.

### First Use

1. Launch Wavi Companion.
2. Complete the onboarding screens.
3. Tap **Connect WhatsApp**.
4. On your primary WhatsApp device, go to **Settings → Linked Devices → Link a Device**.
5. Scan the QR code displayed in Wavi Companion.
6. Your WhatsApp messages will appear within seconds.

## Project Structure

```
app/
└── src/main/java/com/wavicompanion/app/
    ├── core/
    │   ├── common/        # Constants, utilities
    │   ├── designsystem/  # Theme, colors, typography
    │   ├── navigation/    # App navigation graph
    │   ├── datastore/     # Preferences (DataStore)
    │   └── webview/       # WebView configuration and management
    ├── feature/
    │   ├── onboarding/    # Welcome and setup screens
    │   ├── dashboard/     # Main dashboard
    │   ├── whatsapp/      # WhatsApp Web workspace
    │   ├── settings/      # App settings
    │   └── about/         # About and privacy info
    └── WaviApplication.kt
```

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) — Technical architecture overview
- [FUTURE_ROADMAP.md](FUTURE_ROADMAP.md) — Planned feature roadmap
- [RELEASE_GUIDE.md](RELEASE_GUIDE.md) — Release signing and distribution guide
- [PLAY_STORE_CHECKLIST.md](PLAY_STORE_CHECKLIST.md) — Google Play readiness checklist
- [PRIVACY_POLICY_DRAFT.md](PRIVACY_POLICY_DRAFT.md) — Privacy policy draft

## Known Limitations

- WhatsApp Web notifications are not natively forwarded to Android notifications.
- File upload/download support depends on WhatsApp Web and Android WebView capabilities.
- The linked session may disconnect if the primary device is offline for extended periods (this is a WhatsApp platform limitation).
- Camera and microphone access within WhatsApp Web depends on Android WebView permissions.

## License

This project is provided for personal use. Commercial distribution is subject to WhatsApp terms of service, Google Play policies, and applicable legal requirements.
