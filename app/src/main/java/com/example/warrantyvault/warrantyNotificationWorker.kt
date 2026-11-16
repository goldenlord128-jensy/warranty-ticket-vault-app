package com.example.warrantyvault

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

class WarrantyNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = VaultDatabase.getDatabase(applicationContext)

        // Fetch the current list directly from Room Flow using first()
        val items = database.itemDao().getAllItems().first()

        var expiringCount = 0
        items.forEach { item ->
            val info = ExpirationHelper.getExpirationInfo(item.purchaseDate, item.warrantyMonths)
            if (info.status == ExpirationStatus.EXPIRING_SOON) {
                expiringCount++
            }
        }

        if (expiringCount > 0) {
            showNotification(expiringCount)
        }

        return Result.success()
    }

    private fun showNotification(expiringCount: Int) {
        val context = applicationContext
        val channelId = "warranty_expiration_channel"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Warranty Expiration Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Warranty Expiration Alert")
            .setContentText("You have $expiringCount item(s) expiring within the next 30 days!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}