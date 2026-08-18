package com.example.chessitup.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.chessitup.webview.AdBlockWebViewClient

/**
 * Fullscreen Jetpack Compose WebView Screen
 * Displays 'https://chessitup.com' with hardware acceleration, audio support,
 * custom AdBlockWebViewClient, and back button navigation handling.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ChessWebViewScreen(
    targetUrl: String = "https://chessitup.com",
    onAdBlocked: ((url: String, domain: String) -> Unit)? = null
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }

    // Intercept hardware/gesture Back button to navigate WebView history
    BackHandler(enabled = canGoBack) {
        webViewInstance?.let { webView ->
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }
    }

    // Clean up WebView when Composable leaves the composition
    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.apply {
                stopLoading()
                clearHistory()
                removeAllViews()
                destroy()
            }
            webViewInstance = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Embed the Android WebView inside Jetpack Compose
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // 1. Configure WebSettings for Chess game interactivity
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        loadsImagesAutomatically = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        
                        // Enable sound playback for chess piece moves without requiring user tap
                        mediaPlaybackRequiresUserGesture = false
                        
                        // Mixed content mode for assets
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        
                        // Performance and zoom settings
                        setSupportZoom(true)
                        builtInZoomControls = false
                        displayZoomControls = false
                    }

                    // 2. Attach our custom AdBlockWebViewClient
                    webViewClient = AdBlockWebViewClient(
                        onAdBlocked = onAdBlocked,
                        onPageLoaded = {
                            isLoading = false
                            canGoBack = canGoBack()
                        }
                    )

                    // 3. Attach WebChromeClient for page loading progress & full-screen media
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            loadProgress = newProgress / 100f
                            isLoading = newProgress < 100
                            canGoBack = view?.canGoBack() == true
                        }
                    }

                    // 4. Load the target URL
                    loadUrl(targetUrl)
                    webViewInstance = this
                }
            },
            update = { webView ->
                webViewInstance = webView
                canGoBack = webView.canGoBack()
            }
        )

        // Subtle top progress bar during initial page loading
        if (isLoading && loadProgress > 0f && loadProgress < 1f) {
            LinearProgressIndicator(
                progress = { loadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}
