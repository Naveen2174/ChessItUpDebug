package com.example.chessitup.webview

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

/**
 * Custom WebViewClient that:
 * 1. Overrides [shouldInterceptRequest] to block ad networks with an empty WebResourceResponse.
 * 2. Overrides [onPageFinished] to inject CSS styling and JavaScript mutation observers
 *    to hide/purge ad containers from the DOM.
 */
class AdBlockWebViewClient(
    private val onAdBlocked: ((url: String, domain: String) -> Unit)? = null,
    private val onPageLoaded: ((url: String) -> Unit)? = null
) : WebViewClient() {

    companion object {
        private const val TAG = "AdBlockWebViewClient"

        // Common ad servers, trackers, and popup networks
        val BLOCKED_DOMAINS = hashSetOf(
            "doubleclick.net",
            "googleads.g.doubleclick.net",
            "pagead2.googlesyndication.com",
            "adservice.google.com",
            "googletagservices.com",
            "adnxs.com",
            "criteo.com",
            "amazon-adsystem.com",
            "taboola.com",
            "outbrain.com",
            "popads.net",
            "adcolony.com",
            "vungle.com",
            "applovin.com",
            "unityads.unity3d.com",
            "admob.com",
            "scorecardresearch.com",
            "advertising.com",
            "rubiconproject.com",
            "pubmatic.com",
            "moatads.com",
            "quantserve.com",
            "serving-sys.com",
            "smartadserver.com",
            "casalemedia.com",
            "openx.net",
            "adform.net",
            "bidswitch.net",
            "facebook.net/en_US/fbevents.js",
            "analytics.tiktok.com",
            "ads-twitter.com",
            "adsystem.com",
            "zedo.com",
            "exponential.com",
            "adblade.com"
        )

        // JavaScript snippet to inject into WebView on page load
        private val AD_HIDING_JS = """
            (function() {
                // Prevent duplicate injection
                if (window.__adBlockInjected) return;
                window.__adBlockInjected = true;

                // 1. Inject aggressive CSS to immediately hide ad elements & prevent layout reflow
                var css = `
                    .ad, .ads, .ad-banner, .advertisement, .ad-container, .adsbygoogle,
                    div[id*="google_ads"], div[id*="ad-slot"], div[class*="ad-slot"],
                    iframe[src*="doubleclick"], iframe[src*="googleads"],
                    .banner-ad, .sponsor-ad, .ad-wrapper, .google-ad, .chess-ad,
                    [data-ad-slot], [data-ad-client], [aria-label="advertisement"],
                    .leaderboard-ad, .sidebar-ad, .popup-ad, .sticky-ad, .bottom-ad,
                    #ad-header, #ad-footer, #ad-sidebar, .ad_unit, .ad-box {
                        display: none !important;
                        visibility: hidden !important;
                        height: 0px !important;
                        width: 0px !important;
                        opacity: 0 !important;
                        pointer-events: none !important;
                        overflow: hidden !important;
                    }
                `;

                var style = document.createElement('style');
                style.type = 'text/css';
                style.id = 'android-adblock-injected-style';
                style.appendChild(document.createTextNode(css));
                (document.head || document.documentElement).appendChild(style);

                // 2. Remove matching ad nodes and observe dynamic DOM insertions
                function purgeAdNodes() {
                    var selectors = [
                        'iframe[src*="ads"]',
                        'iframe[src*="doubleclick"]',
                        'div[id*="google_ads"]',
                        '.adsbygoogle',
                        '.ad-banner',
                        '.advertisement',
                        '[data-ad-client]',
                        '.ad_unit'
                    ];
                    selectors.forEach(function(selector) {
                        var elements = document.querySelectorAll(selector);
                        elements.forEach(function(el) {
                            el.remove();
                        });
                    });
                }

                // Initial purge
                purgeAdNodes();

                // Observe asynchronous DOM mutations (e.g. lazy-loaded banner scripts)
                var observer = new MutationObserver(function() {
                    purgeAdNodes();
                });

                if (document.body) {
                    observer.observe(document.body, { childList: true, subtree: true });
                } else {
                    document.addEventListener('DOMContentLoaded', function() {
                        observer.observe(document.body, { childList: true, subtree: true });
                    });
                }
            })();
        """.trimIndent()
    }

    /**
     * Intercepts HTTP/HTTPS requests on Android API 21+
     * Returns an empty 200 OK WebResourceResponse if the URL matches an ad domain.
     */
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val requestUrl = request?.url?.toString() ?: return null
        return handleRequestInterception(view, requestUrl)
    }

    /**
     * Backward-compatibility for older Android API levels
     */
    @Deprecated("Deprecated in Java")
    override fun shouldInterceptRequest(
        view: WebView?,
        url: String?
    ): WebResourceResponse? {
        if (url == null) return null
        return handleRequestInterception(view, url)
    }

    private fun handleRequestInterception(view: WebView?, url: String): WebResourceResponse? {
        val lowerUrl = url.lowercase()

        for (domain in BLOCKED_DOMAINS) {
            if (lowerUrl.contains(domain)) {
                Log.d(TAG, "BLOCKED Ad Request: $url [matched: $domain]")
                
                // Notify listener on main thread
                view?.post {
                    onAdBlocked?.invoke(url, domain)
                }

                // Return an empty WebResourceResponse (0 bytes) to cancel the network request
                return createEmptyResourceResponse()
            }
        }

        // Allow legitimate requests to proceed normally
        return null
    }

    /**
     * Creates an empty WebResourceResponse with "text/plain" and UTF-8 charset
     */
    private fun createEmptyResourceResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            200,
            "OK",
            mapOf("Access-Control-Allow-Origin" to "*"),
            ByteArrayInputStream(ByteArray(0))
        )
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Log.d(TAG, "Page started loading: $url")
    }

    /**
     * Injects CSS and JavaScript ad-hiding scripts when the page finishes loading
     */
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.d(TAG, "Page finished loading: $url -> Injecting AdBlock JS")

        // Execute Javascript to hide banners and attach DOM MutationObserver
        view?.evaluateJavascript(AD_HIDING_JS) { result ->
            Log.d(TAG, "AdBlock JS injection result: $result")
        }

        url?.let { onPageLoaded?.invoke(it) }
    }
}
