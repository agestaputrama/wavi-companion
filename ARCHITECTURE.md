# Architecture

## Overview

Wavi Companion follows a clean MVVM architecture using Kotlin, Jetpack Compose, and modern Android development practices. The codebase is organized into `core` and `feature` modules within a single-module app, designed for clarity and future modularization.

## Architecture Diagram

```
┌───────────────────────────────────────────────────┐
│                  UI Layer (Compose)                │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │Onboarding│ │Dashboard │ │ WhatsApp │ │Settings││
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └───┬────┘│
│       │            │            │            │     │
│  ┌────▼────────────▼────────────▼────────────▼───┐ │
│  │              ViewModels (StateFlow)            │ │
│  └────────────────────┬──────────────────────────┘ │
├───────────────────────┼────────────────────────────┤
│                  Core Layer                        │
│  ┌──────────┐ ┌──────┴─────┐ ┌──────────────────┐ │
│  │DataStore │ │  WebView   │ │  Design System   │ │
│  │(Prefs)   │ │  Manager   │ │  (Theme/Colors)  │ │
│  └──────────┘ └────────────┘ └──────────────────┘ │
└───────────────────────────────────────────────────┘
```

## Technology Stack

| Layer | Technology |
|-------|-----------|
| UI Framework | Jetpack Compose + Material 3 |
| Architecture | MVVM with unidirectional data flow |
| Navigation | Navigation Compose |
| State | StateFlow + Compose State |
| Persistence | DataStore Preferences |
| DI | Hilt |
| WebView | Android WebView (system) |
| Build | Gradle Kotlin DSL |
| Language | Kotlin 1.9.x |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |

## Package Structure

### `core/`

- **`common/`** — App-wide constants (`AppConstants`), including URLs, package name, and version info.
- **`designsystem/`** — Compose theme definition (`WaviTheme`), color schemes, and typography.
- **`navigation/`** — Navigation graph and route definitions. Single `NavHost` with typed route objects.
- **`datastore/`** — `UserPreferencesRepository` backed by DataStore. Stores theme preference, onboarding completion, and screen-awake setting.
- **`webview/`** — `WaviWebViewManager` managing a singleton `WebView` instance with proper configuration, lifecycle handling, cookie persistence, and security settings.

### `feature/`

Each feature package contains:
- A `*Screen.kt` composable (the UI)
- A `*ViewModel.kt` (business logic + state)

Features:
- **`onboarding/`** — Pager-based onboarding with animated transitions.
- **`dashboard/`** — Connection status cards, quick actions, and the primary entry point to WhatsApp.
- **`whatsapp/`** — The WhatsApp Web workspace. Hosts the singleton WebView within Compose using `AndroidView`.
- **`settings/`** — Theme, display, data management, and account settings.
- **`about/`** — App info, privacy policy, and disclaimer.

## Key Design Decisions

### Singleton WebView

The WebView is managed as a singleton (`WaviWebViewManager`) to preserve the authenticated WhatsApp Web session across navigation. Recreating the WebView would force a re-authentication. The WebView is created once and reattached to Compose via `AndroidView` when the WhatsApp screen is displayed.

### No Script Injection

The app does not inject JavaScript into WhatsApp Web. All interaction happens through the official web interface rendered in the WebView. This respects WhatsApp's terms and avoids fragile DOM dependencies.

### Security Model

- HTTPS enforced via `network_security_config.xml` (cleartext traffic disabled).
- SSL errors are never bypassed — `onReceivedSslError` calls `handler.cancel()`.
- Navigation is restricted to WhatsApp-owned domains.
- No cookies, tokens, or credentials are extracted or stored outside the WebView.
- No third-party analytics or tracking SDKs.

### Theme System

The Compose theme (`WaviTheme`) wraps Material 3 with custom green-accented color schemes for light and dark modes. Theme preference is persisted in DataStore and observed as a `StateFlow` to enable real-time switching.

## Data Flow

```
User Action → Compose UI → ViewModel → Repository/Manager → State Update → UI Recomposition
```

All state flows downstream. ViewModels expose `StateFlow` objects that Compose observes. Side effects (WebView commands, DataStore writes) are triggered from ViewModels via coroutines.

## WebView Lifecycle

1. `WaviWebViewManager` creates the WebView on first access.
2. WebView loads `https://web.whatsapp.com/` with configured settings (DOM storage, JS enabled, safe browsing).
3. On app backgrounding, `onPause()` is called on the WebView.
4. On app foregrounding, `onResume()` is called.
5. On explicit logout, cookies and WebView data are cleared.
6. The WebView is destroyed only when the application process exits.
