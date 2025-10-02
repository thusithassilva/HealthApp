package com.example.healthapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.SystemClock
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Show notification and toast when reminder triggers
        showNotification(context)
        showToast(context)

        // Schedule the next reminder immediately
        scheduleNextReminder(context)
    }

    private fun showToast(context: Context) {
        Toast.makeText(context, "💧 Time to drink water! Stay hydrated! 💧", Toast.LENGTH_LONG).show()
    }

    private fun showNotification(context: Context) {
        // Create notification channel for Android 8.0+
        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, "water_reminder_channel")
            .setContentTitle("💧 Water Reminder")
            .setContentText("Time to hydrate! Drink a glass of water.")
            .setSmallIcon(R.drawable.ic_water_full)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "water_reminder_channel",
                "Water Reminders",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to drink water"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleNextReminder(context: Context) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val reminderEnabled = sharedPreferences.getBoolean("water_reminder_enabled", false)
        val reminderInterval = sharedPreferences.getInt("water_reminder_interval", 5)

        if (!reminderEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = reminderInterval * 1000L
        val nextTriggerTime = SystemClock.elapsedRealtime() + intervalMillis

        // Use exact timing for precise intervals
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        }
    }
}