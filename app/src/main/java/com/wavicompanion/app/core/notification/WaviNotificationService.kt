package com.wavicompanion.app.core.notification

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import com.wavicompanion.app.core.webview.WebViewManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WaviNotificationService : Service() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var webViewManager: WebViewManager

    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = notificationHelper.buildServiceNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NotificationHelper.SERVICE_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NotificationHelper.SERVICE_NOTIFICATION_ID, notification)
        }

        acquireWakeLock()

        if (!webViewManager.isInitialized) {
            handler.postDelayed({
                if (!webViewManager.isInitialized) {
                    stopSelf()
                }
            }, WEBVIEW_CHECK_DELAY_MS)
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        releaseWakeLock()
        webViewManager.stopKeepAlive()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    @Suppress("WakelockTimeout")
    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "WaviCompanion::NotificationWakeLock",
        ).apply {
            acquire()
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val WEBVIEW_CHECK_DELAY_MS = 5000L

        fun start(context: Context) {
            val intent = Intent(context, WaviNotificationService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WaviNotificationService::class.java))
        }
    }
}
