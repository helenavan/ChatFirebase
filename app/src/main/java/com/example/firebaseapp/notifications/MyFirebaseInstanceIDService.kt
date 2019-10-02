package com.example.firebaseapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.firebaseapp.MentorChatActivity
import com.example.firebaseapp.R
import com.example.firebaseapp.api.getFCMRegistrationTokens
import com.example.firebaseapp.api.setFCMRegistrationTokens
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random

import java.net.HttpURLConnection
import java.net.URL

private const val NOTIFICATION_ID_EXTRA = "notificationId"
private const val IMAGE_URL_EXTRA = "imageUrl"
private const val ADMIN_CHANNEL_ID = "admin_channel"
const val TAG:String = "MyFirebaseIIDService"

class MyFirebaseInstanceIDService: FirebaseMessagingService() {

    private var notificationManager:NotificationManager? = null

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notificationIntent = Intent(this,MentorChatActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,
            0 , notificationIntent,
            PendingIntent.FLAG_ONE_SHOT)

        //You should use an actual ID instead
        val notificationId = Random().nextInt(60000)
        remoteMessage?.let {
            val bitmap = getBitmapFromUrl(remoteMessage.data["image-url"])

            val likeIntent = Intent(this, LikeService::class.java)
            likeIntent.putExtra(NOTIFICATION_ID_EXTRA, notificationId)
            likeIntent.putExtra(IMAGE_URL_EXTRA, remoteMessage.data["image-url"])
            val likePendingIntent = PendingIntent.getService(this,
                notificationId + 1, likeIntent, PendingIntent.FLAG_ONE_SHOT)

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setupChannels()
            }

            val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setLargeIcon(bitmap)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(remoteMessage.data["title"])
                .setStyle(NotificationCompat.BigPictureStyle()
                    .setSummaryText(remoteMessage.data["message"])
                    .bigPicture(bitmap))/*Notification with Image*/
                .setContentText(remoteMessage.data["message"])
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .addAction(R.drawable.ic_delete,
                    getString(R.string.notification_add_to_cart_button), likePendingIntent)
                .setContentIntent(pendingIntent)

            notificationManager?.notify(notificationId, notificationBuilder.build())
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels() {
        val adminChannelName = getString(R.string.notifications_admin_channel_name)
        val adminChannelDescription = getString(R.string.notifications_admin_channel_description)

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)

    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val newToken = instanceIdResult.token
            Log.e("newToken", newToken)
            val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            preferences.edit().putString("truc",newToken).apply()
        }

    }


    private fun getBitmapFromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        fun addTokenToFirestore(newRegistrationToken: String?) {
            if (newRegistrationToken == null) throw NullPointerException("FCM token is null.")

            getFCMRegistrationTokens { tokens ->
                if (tokens.contains(newRegistrationToken))
                    return@getFCMRegistrationTokens

                tokens.add(newRegistrationToken)
                setFCMRegistrationTokens(tokens)
            }
        }
    }
}