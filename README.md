# ChessItUp Android Jetpack Compose Fullscreen WebView

This Android application renders **https://chessitup.com** in an immersive fullscreen WebView using **Jetpack Compose**, equipped with an aggressive, low-latency ad blocker and CSS banner remover.

## Key Features

1. **Jetpack Compose WebView Integration**:
   - Uses `AndroidView` to host Android `WebView`.
   - Enabled `javaScriptEnabled`, `domStorageEnabled`, and `mediaPlaybackRequiresUserGesture = false` for seamless chess move sounds and interactive gameplay.
   - Integrated `BackHandler` for responsive page navigation.

2. **Network Ad Blocking (`shouldInterceptRequest`)**:
   - Overrides `shouldInterceptRequest(view, request)` in `AdBlockWebViewClient.kt`.
   - Intercepts requests to DoubleClick, Google Syndication (AdSense), Amazon Ads, Criteo, Taboola, Outbrain, PopAds, and 30+ ad networks.
   - Instantly returns an empty `WebResourceResponse` (0 bytes), preventing tracking, bandwidth drain, and UI clutter.

3. **DOM & CSS Ad Banner Purging (`onPageFinished`)**:
   - Injects a high-specificity CSS stylesheet targeting `.ad`, `.ad-banner`, `div[id*="google_ads"]`, and banner containers with `display: none !important;`.
   - Attaches a `MutationObserver` in JavaScript to immediately purge any dynamically loaded ad iframes or popups.

4. **Edge-to-Edge Immersive Fullscreen**:
   - Implements `enableEdgeToEdge()` and `WindowInsetsControllerCompat` to hide status bars and navigation bars for uninterrupted chess gameplay.

## How to Build in Android Studio

1. Open **Android Studio** (Hedgehog, Iguana, Jellyfish, or newer).
2. Select **Open** and choose the root folder containing `build.gradle.kts` and `settings.gradle.kts`.
3. Allow Gradle to sync dependencies.
4. Connect an Android device or start an emulator running Android API 24+.
5. Click **Run (Shift + F10)** to launch the app!
