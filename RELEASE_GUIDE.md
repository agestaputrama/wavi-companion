# Release Guide

## Debug Build

Debug builds are signed automatically with the Android debug keystore. No configuration needed.

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Release Build

### 1. Create a Signing Keystore

Generate a release keystore (do this once):

```bash
keytool -genkey -v \
  -keystore wavi-release-keystore.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias wavi
```

Store the keystore file in a secure location **outside** the project directory. Never commit it to version control.

### 2. Configure Signing

Create or edit `keystore.properties` in the project root (this file is gitignored):

```properties
storeFile=/path/to/wavi-release-keystore.jks
storePassword=your_store_password
keyAlias=wavi
keyPassword=your_key_password
```

The `app/build.gradle.kts` is already configured to read from this file when it exists.

### 3. Build Release APK

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### 4. Build Release AAB (for Google Play)

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## Version Management

Update version numbers in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 1        // Increment for each release
    versionName = "1.0.0"  // Semantic versioning
}
```

- `versionCode`: Integer, must increase with every release uploaded to Google Play.
- `versionName`: User-facing version string.

## APK Size Optimization

The release build is configured with:
- `minifyEnabled = true` — R8 code shrinking
- `shrinkResources = true` — Unused resource removal
- ProGuard rules for WebView and Hilt compatibility

## Installation (Sideloading)

1. Transfer the APK to the target device (USB, cloud storage, or direct download).
2. On the device, open the APK file.
3. Allow installation from unknown sources if prompted.
4. Tap **Install**.
5. Open Wavi Companion.

## Security Notes

- Never commit `keystore.properties` or `.jks` files to version control.
- Store keystore passwords in a password manager.
- Back up the keystore file — losing it means you cannot update a published app.
- For Google Play, consider using Play App Signing (Google manages your upload key).
