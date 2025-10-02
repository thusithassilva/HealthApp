package com.example.healthapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Show notification and toast when reminder triggers
        showNotification(context)
        showToast(context)
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
}