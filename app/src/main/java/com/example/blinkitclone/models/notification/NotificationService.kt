package com.example.blinkitclone.models.notification


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.example.blinkitclone.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class NotificationService : FirebaseMessagingService() {
    val channelId = "blink"
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)


        val manager = getSystemService(Context.NOTIFICATION_SERVICE)
        createNotificationChannel(manager as NotificationManager)
        val notification  = NotificationCompat.Builder(this,channelId)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setAutoCancel(false)
            .setSmallIcon(R.drawable.icon)
            .setContentIntent(null)
            .build()
        manager.notify(Random.nextInt() , notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(channelId,"blinkit",NotificationManager.IMPORTANCE_HIGH)
        channel.description = "New order"
        channel.enableLights(true)
        notificationManager.createNotificationChannel(channel)
    }
}