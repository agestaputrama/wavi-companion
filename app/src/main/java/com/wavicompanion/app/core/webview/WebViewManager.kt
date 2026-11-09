package com.wavicompanion.app.core.webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import com.wavicompanion.app.core.notification.NotificationHelper
import com.wavicompanion.app.core.notification.WebViewNotificationBridge
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

@Singleton
class WebViewManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationHelper: NotificationHelper,
) {
    private val notificationBridge = WebViewNotificationBridge(notificationHelper)
    private var _webView: WebView? = null
    private val keepAliveHandler = Handler(Looper.getMainLooper())
    private var keepAliveRunning = false

    private val keepAliveRunnable = object : Runnable {
        override fun run() {
            if (!keepAliveRunning) return
            injectHeartbeat()
            keepAliveHandler.postDelayed(this, KEEP_ALIVE_INTERVAL_MS)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun getOrCreate(): WebView {
        return _webView ?: createWebView().also { _webView = it }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView {
        WebView.setWebContentsDebuggingEnabled(true)

        return WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                setSupportMultipleWindows(false)
                javaScriptCanOpenWindowsAutomatically = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                allowFileAccess = false
                allowContentAccess = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
                userAgentString = DESKTOP_USER_AGENT
                @Suppress("SetTextZoom")
                textZoom = 100
            }

            isFocusable = true
            isFocusableInTouchMode = true
            overScrollMode = View.OVER_SCROLL_NEVER
            scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
            setBackgroundColor(android.graphics.Color.parseColor("#111b21"))

            CookieManager.getInstance().let { cm ->
                cm.setAcceptCookie(true)
                cm.setAcceptThirdPartyCookies(this, true)
            }

            addJavascriptInterface(notificationBridge, WebViewNotificationBridge.JS_INTERFACE_NAME)
        }
    }

    fun injectNotificationJs() {
        _webView?.evaluateJavascript(WebViewNotificationBridge.NOTIFICATION_INTERCEPT_JS, null)
    }

    fun injectMediaDeviceFix() {
        _webView?.evaluateJavascript(MEDIA_DEVICE_FIX_JS, null)
    }

    fun injectLayoutFix(isDark: Boolean = true) {
        val bg = if (isDark) "#111b21" else "#f0f2f5"
        _webView?.evaluateJavascript(buildLayoutFixJs(bg), null)
    }

    fun injectViewportFix() {
        _webView?.evaluateJavascript(VIEWPORT_FIX_JS, null)
    }

    fun injectPopupFocusFix() {
        _webView?.evaluateJavascript(POPUP_FOCUS_FIX_JS, null)
    }

    fun injectBackgroundState() {
        _webView?.evaluateJavascript("""
            (function() {
                if (!window._waviOrigHasFocus) {
                    window._waviOrigHasFocus = document.hasFocus.bind(document);
                }
                Object.defineProperty(document, 'visibilityState', {
                    get: function() { return 'hidden'; }, configurable: true
                });
                Object.defineProperty(document, 'hidden', {
                    get: function() { return true; }, configurable: true
                });
                document.hasFocus = function() { return false; };
                document.dispatchEvent(new Event('visibilitychange'));
            })();
        """.trimIndent(), null)
    }

    fun injectForegroundState() {
        _webView?.evaluateJavascript("""
            (function() {
                Object.defineProperty(document, 'visibilityState', {
                    get: function() { return 'visible'; }, configurable: true
                });
                Object.defineProperty(document, 'hidden', {
                    get: function() { return false; }, configurable: true
                });
                document.hasFocus = function() { return true; };
                document.dispatchEvent(new Event('visibilitychange'));
                document.dispatchEvent(new Event('focus'));
                window.dispatchEvent(new Event('focus'));
            })();
        """.trimIndent(), null)
    }

    fun startKeepAlive() {
        if (keepAliveRunning) return
        keepAliveRunning = true
        keepAliveHandler.postDelayed(keepAliveRunnable, KEEP_ALIVE_INTERVAL_MS)
    }

    fun stopKeepAlive() {
        keepAliveRunning = false
        keepAliveHandler.removeCallbacks(keepAliveRunnable)
    }

    private fun injectHeartbeat() {
        _webView?.evaluateJavascript("""
            (function() {
                try {
                    document.dispatchEvent(new MouseEvent('mousemove', {clientX: 100, clientY: 100}));
                    document.dispatchEvent(new Event('focus'));
                    window.dispatchEvent(new Event('online'));
                } catch(e) {}

                if (document.visibilityState !== 'hidden') {
                    try {
                        Object.defineProperty(document, 'visibilityState', {
                            get: function() { return 'hidden'; }, configurable: true
                        });
                        Object.defineProperty(document, 'hidden', {
                            get: function() { return true; }, configurable: true
                        });
                        document.dispatchEvent(new Event('visibilitychange'));
                    } catch(e) {}
                }

            })();
        """.trimIndent(), null)

        injectNotificationJs()
    }

    fun clearSessionData() {
        _webView?.let { wv ->
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            android.webkit.WebStorage.getInstance().deleteAllData()
            wv.clearCache(true)
            wv.clearHistory()
            wv.loadUrl("about:blank")
        }
    }

    fun clearCache() {
        _webView?.clearCache(true)
    }

    fun destroy() {
        stopKeepAlive()
        _webView?.let { wv ->
            wv.stopLoading()
            (wv.parent as? ViewGroup)?.removeView(wv)
            wv.removeAllViews()
            wv.destroy()
        }
        _webView = null
    }

    val isInitialized: Boolean get() = _webView != null

    companion object {
        private const val KEEP_ALIVE_INTERVAL_MS = 2 * 60 * 1000L

        private val POPUP_FOCUS_FIX_JS = """
            (function() {
                if (window._waviPopupFix) return;
                window._waviPopupFix = true;
                var userTapped = false;
                document.addEventListener('pointerdown', function(e) {
                    var t = e.target;
                    if (!t) return;
                    var tag = t.tagName && t.tagName.toUpperCase();
                    var editable = t.closest && t.closest('[contenteditable="true"]');
                    var isInput = (tag === 'INPUT' || tag === 'TEXTAREA');
                    userTapped = !!(editable || isInput);
                    if (editable) {
                        editable.removeAttribute('inputmode');
                    } else if (!isInput) {
                        var compose = document.querySelector('footer div[contenteditable="true"]') ||
                                      document.querySelector('div[contenteditable="true"][data-tab]');
                        if (compose) compose.setAttribute('inputmode', 'none');
                    }
                }, true);
                document.addEventListener('focusin', function(e) {
                    if (userTapped) return;
                    var el = e.target;
                    if (!el) return;
                    var tag = el.tagName && el.tagName.toUpperCase();
                    var isEditable = el.getAttribute && el.getAttribute('contenteditable') === 'true';
                    var isInput = (tag === 'INPUT' || tag === 'TEXTAREA');
                    if (isEditable) {
                        el.setAttribute('inputmode', 'none');
                    } else if (isInput) {
                        el.blur();
                    }
                }, true);
            })();
        """.trimIndent()

        private val VIEWPORT_FIX_JS = """
            (function() {
                if (window._waviViewport) return;
                window._waviViewport = true;
                function setVP() {
                    var head = document.head || document.getElementsByTagName('head')[0];
                    if (!head) { setTimeout(setVP, 50); return; }
                    var meta = document.querySelector('meta[name="viewport"]');
                    if (!meta) {
                        meta = document.createElement('meta');
                        meta.name = 'viewport';
                        head.appendChild(meta);
                    }
                    meta.setAttribute('content', 'width=1280, minimum-scale=0.1, maximum-scale=5, user-scalable=yes');
                }
                setVP();
            })();
        """.trimIndent()

        private fun buildLayoutFixJs(bg: String) = """
            (function() {
                var style = document.getElementById('wavi-layout-fix');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'wavi-layout-fix';
                }
                style.textContent = [
                    'html, body { overflow-x: hidden !important; height: 100vh !important; min-height: 100% !important; margin: 0 !important; padding: 0 !important; background: $bg !important; }',
                    '#app { height: 100vh !important; min-height: 100% !important; }',
                    '.copyable-text, span.selectable-text {',
                    '  word-break: break-word !important;',
                    '  overflow-wrap: break-word !important;',
                    '  white-space: pre-wrap !important;',
                    '  max-width: 100% !important;',
                    '}',
                    'div[role="row"] > div { max-width: 100% !important; }',
                    '#main { min-width: 0 !important; flex: 1 !important; }'
                ].join('\n');
                if (document.head && !style.parentNode) {
                    document.head.appendChild(style);
                }
            })();
        """.trimIndent()

        private val MEDIA_DEVICE_FIX_JS = """
            (function() {
                if (window._waviMediaFixed) return;
                window._waviMediaFixed = true;

                function addPlaceholderDevices(devices) {
                    if (!devices.some(function(d) { return d.kind === 'audioinput'; })) {
                        devices.push({
                            deviceId: 'default', kind: 'audioinput', label: '', groupId: 'default',
                            toJSON: function() { return {deviceId:'default',kind:'audioinput',label:'',groupId:'default'}; }
                        });
                    }
                    if (!devices.some(function(d) { return d.kind === 'videoinput'; })) {
                        devices.push({
                            deviceId: 'default', kind: 'videoinput', label: '', groupId: 'default',
                            toJSON: function() { return {deviceId:'default',kind:'videoinput',label:'',groupId:'default'}; }
                        });
                    }
                    return devices;
                }

                if (typeof MediaDevices !== 'undefined' && MediaDevices.prototype.enumerateDevices) {
                    var _origProto = MediaDevices.prototype.enumerateDevices;
                    MediaDevices.prototype.enumerateDevices = function() {
                        return _origProto.call(this).then(addPlaceholderDevices);
                    };
                }
                if (navigator.mediaDevices && navigator.mediaDevices.enumerateDevices) {
                    var _origInst = navigator.mediaDevices.enumerateDevices.bind(navigator.mediaDevices);
                    navigator.mediaDevices.enumerateDevices = function() {
                        return _origInst().then(addPlaceholderDevices);
                    };
                }

                if (navigator.permissions && navigator.permissions.query) {
                    var _origQuery = navigator.permissions.query.bind(navigator.permissions);
                    navigator.permissions.query = function(desc) {
                        if (desc && (desc.name === 'microphone' || desc.name === 'camera')) {
                            return Promise.resolve({
                                state: 'prompt', name: desc.name, onchange: null,
                                addEventListener: function(){}, removeEventListener: function(){}
                            });
                        }
                        return _origQuery(desc);
                    };
                }
            })();
        """.trimIndent()
    }
}
