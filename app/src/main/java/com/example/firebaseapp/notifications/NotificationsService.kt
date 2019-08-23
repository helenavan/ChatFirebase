package com.example.firebaseapp.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.media.RingtoneManager

import android.app.PendingIntent
import android.content.Context
import com.example.firebaseapp.MainActivity
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.firebaseapp.R
import com.google.firebase.messaging.RemoteMessage



class NotificationsService: FirebaseMessagingService() {

    private val NOTIFICATION_ID = 7
    private val NOTIFICATION_TAG = "FIREBASEOC"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            val message = remoteMessage.notification!!.body
            // Show notification after received message
            this.sendVisualNotification(message)
        }
    }

    // ---

    private fun sendVisualNotification(messageBody: String?) {

        // Create an Intent that will be shown when user will click on the Notification
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        // Create a Style for the Notification
        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.setBigContentTitle(getString(R.string.notification_title))
        inboxStyle.addLine(messageBody)

        // Create a Channel (Android 8)
        val channelId = getString(R.string.default_notification_channel_id)

        // Build a Notification object
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_image_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_title))
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setStyle(inboxStyle)

        // Add the Notification to the Notification Manager and show it.
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Support Version >= Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Message provenant de Firebase"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(mChannel)
        }

        // Show notification
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build())
    }
}