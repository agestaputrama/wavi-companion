package com.wavicompanion.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.wavicompanion.app.MainActivity
import com.wavicompanion.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val manager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun createChannels() {
        val messagesChannel = NotificationChannel(
            CHANNEL_MESSAGES,
            "WhatsApp Messages",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Notifications for incoming WhatsApp messages"
            enableVibration(true)
            setShowBadge(true)
        }

        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE,
            "Background Service",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Keeps Wavi running to receive notifications"
            setShowBadge(false)
        }

        manager.createNotificationChannels(listOf(messagesChannel, serviceChannel))
    }

    fun showMessageNotification(title: String, body: String, tag: String?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val notificationId = nextNotificationId()
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setGroup(GROUP_MESSAGES)
            .build()

        manager.notify(tag, notificationId, notification)

        val summaryIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val summary = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setGroup(GROUP_MESSAGES)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setContentIntent(summaryIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setStyle(NotificationCompat.InboxStyle()
                .setSummaryText("WhatsApp Messages"))
            .build()
        manager.notify(SUMMARY_TAG, SUMMARY_NOTIFICATION_ID, summary)
    }

    fun buildServiceNotification(): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(context, CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Wavi Companion")
            .setContentText("Running in background for notifications")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        const val CHANNEL_MESSAGES = "wavi_messages"
        const val CHANNEL_SERVICE = "wavi_service"
        const val SERVICE_NOTIFICATION_ID = 1
        private const val SUMMARY_NOTIFICATION_ID = 2
        private const val GROUP_MESSAGES = "wavi_message_group"
        private const val SUMMARY_TAG = "wavi_summary"
        private const val MIN_NOTIFICATION_ID = 100
        private const val MAX_NOTIFICATION_ID = 50000
        private val counter = AtomicInteger(MIN_NOTIFICATION_ID)

        fun nextNotificationId(): Int {
            val id = counter.getAndIncrement()
            if (id >= MAX_NOTIFICATION_ID) {
                counter.compareAndSet(id + 1, MIN_NOTIFICATION_ID)
                return MIN_NOTIFICATION_ID
            }
            return id
        }
    }
}
