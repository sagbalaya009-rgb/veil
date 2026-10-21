package com.veil.app.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Posts one quiet, local completion notice only after the user has opted in. */
@Singleton
class CooldownCompletionNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun notifyIfAllowed() {
        if (!canPostNotifications()) return
        createChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("Veil")
            .setContentText("Your cooldown is complete. You followed through.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // The user can revoke notification access after the permission check.
            // A missed optional reminder must never crash or block recovery.
        }
    }

    private fun canPostNotifications(): Boolean {
        val systemEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        return systemEnabled && granted
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Cooldown completion",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "A quiet notification when an opted-in Veil cooldown ends."
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "veil_cooldown_completion"
        const val NOTIFICATION_ID = 1002
    }
}
