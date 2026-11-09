package com.wavicompanion.app.core.notification

import android.webkit.JavascriptInterface

class WebViewNotificationBridge(
    private val notificationHelper: NotificationHelper,
) {
    @Suppress("UNUSED_PARAMETER")
    @JavascriptInterface
    fun onNotification(title: String, body: String, tag: String, icon: String) {
        val safeTitle = title.take(MAX_TITLE_LENGTH)
        val safeBody = body.take(MAX_BODY_LENGTH)
        val safeTag = tag.take(MAX_TAG_LENGTH)
        if (safeTitle.isBlank() && safeBody.isBlank()) return
        notificationHelper.showMessageNotification(
            title = safeTitle.ifBlank { "WhatsApp" },
            body = safeBody,
            tag = safeTag.ifBlank { null },
        )
    }

    companion object {
        const val JS_INTERFACE_NAME = "WaviNotificationBridge"
        private const val MAX_TITLE_LENGTH = 200
        private const val MAX_BODY_LENGTH = 1000
        private const val MAX_TAG_LENGTH = 100

        val NOTIFICATION_INTERCEPT_JS = """
            (function() {
                if (window._waviNotificationPatched) return;
                window._waviNotificationPatched = true;

                function WaviNotification(title, options) {
                    options = options || {};
                    try {
                        window.WaviNotificationBridge.onNotification(
                            title || '',
                            options.body || '',
                            options.tag || '',
                            options.icon || ''
                        );
                    } catch(e) {}
                    this.title = title;
                    this.body = options.body || '';
                    this.tag = options.tag || '';
                    this.icon = options.icon || '';
                    this.onclick = null;
                    this.onclose = null;
                    this.onerror = null;
                    this.onshow = null;
                }
                WaviNotification.permission = 'granted';
                WaviNotification.requestPermission = function(cb) {
                    if (cb) cb('granted');
                    return Promise.resolve('granted');
                };
                WaviNotification.maxActions = 2;
                WaviNotification.prototype.close = function() {};
                WaviNotification.prototype.addEventListener = function() {};
                WaviNotification.prototype.removeEventListener = function() {};

                window.Notification = WaviNotification;

                if (navigator.serviceWorker) {
                    try {
                        ServiceWorkerRegistration.prototype.showNotification = function(title, options) {
                            options = options || {};
                            try {
                                window.WaviNotificationBridge.onNotification(
                                    title || '',
                                    options.body || '',
                                    options.tag || '',
                                    options.icon || ''
                                );
                            } catch(e) {}
                            return Promise.resolve();
                        };
                        ServiceWorkerRegistration.prototype.getNotifications = function() {
                            return Promise.resolve([]);
                        };
                    } catch(e) {}
                }
            })();
        """.trimIndent()
    }
}
