package com.example.automation.data.work

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.automation.MainActivity
import com.example.automation.R

class ReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val channel = NotificationChannels.ensure(applicationContext)

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(applicationContext)
            .addNextIntentWithParentStack(intent)
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(applicationContext, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(applicationContext.getString(R.string.notification_body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1001, notification)
        return Result.success()
    }
}
