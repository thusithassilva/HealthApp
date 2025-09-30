package com.example.healthapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Show a simple toast when reminder triggers
        Toast.makeText(context, "💧 Time to drink water!", Toast.LENGTH_SHORT).show()

        // You can add notification here if needed
        showNotification(context)
    }

    private fun showNotification(context: Context) {
        val notification = NotificationCompat.Builder(context, "water_reminder_channel")
            .setContentTitle("💧 Water Reminder")
            .setContentText("Time to hydrate! Drink a glass of water.")
            .setSmallIcon(R.drawable.ic_water_full)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(1, notification)
    }
}