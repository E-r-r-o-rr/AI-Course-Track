package com.example.automation.data.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.automation.R

object NotificationChannels {
    private const val CHANNEL_ID = "learning_reminder"

    fun ensure(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                channel.description = context.getString(R.string.notification_body)
                manager.createNotificationChannel(channel)
            }
        }
        return CHANNEL_ID
    }
}
