package com.vektortelekom.android.vservice.utils.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vektor.ktx.utils.NotificationBuilder
import com.vektortelekom.android.vservice.BuildConfig
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.data.model.NotificationModel
import com.vektortelekom.android.vservice.ui.splash.SplashActivity
import com.vektortelekom.android.vservice.utils.AppConstants
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

class AppFirebaseMessagingService : FirebaseMessagingService() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        try {
            val data = remoteMessage.data
            val notification = remoteMessage.notification

            if (data.isNotEmpty()) {

                if(data.containsKey("extra")) {

                    val model = Gson().fromJson<NotificationModel>(data["extra"], object : TypeToken<NotificationModel>() {}.type)

                    if (model.subCategory == "CARPOOL_MATCHED" || model.subCategory == "STATION_ARRIVAL_EVENT")
                        sendNotificationForMatch(model.message, model.subCategory)
                    else
                        sendNotification(model.message)

                }

                EventBus.getDefault().postSticky(NotificationEvent(data, /*notificationMessage*/""))
            } else if (notification != null && notification.body != null) {
                sendNotification(notification.body!!)
            }
        } catch (e: Exception) {
            Timber.e(e, "Notification parse error.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun sendNotificationForMatch(message: String, subCategory: String) {
        val intent = Intent(this, SplashActivity::class.java)
        intent.putExtra("notification", message)
        intent.putExtra("subCategory", subCategory)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = BuildConfig.APPLICATION_ID // packageName
        val mBuilder = NotificationCompat.Builder(this, notificationChannelId)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Changing Default mode of notification
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE)

            val mChannel = NotificationChannel(notificationChannelId, resources.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(mChannel)
        }

        val notification = mBuilder.setSmallIcon(R.drawable.ic_notification)
            .setTicker(applicationContext.resources.getString(R.string.app_name))
            .setWhen(0)
            .setContentTitle(applicationContext.resources.getString(R.string.app_name))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
            .setAutoCancel(true)
            .setContentText(message)
            .build()

        notificationManager.notify(0, notification)

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun sendNotification(message: String) {
        val intent = Intent(this, SplashActivity::class.java)
        intent.putExtra("notification", message)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

        val notificationBuilder = NotificationBuilder.newInstance(this, pendingIntent)
        notificationBuilder.sendBundledNotification(
                AppConstants.System.APP_NAME_STD,
                getString(R.string.app_name),
                message,
                R.drawable.ic_notification,
                R.drawable.ic_notification,
            R.color.colorPrimary
        )

    }

    class NotificationEvent(var data: Map<String, String>, var notificationMessage: String?)


    @RequiresApi(Build.VERSION_CODES.M)
    private fun sendNotification(message: String, soundName: String?) {
        val intent = Intent(this, SplashActivity::class.java)
        intent.putExtra("notification", message)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = BuildConfig.APPLICATION_ID // packageName
        val mBuilder = NotificationCompat.Builder(this, notificationChannelId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Changing Default mode of notification
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE)
            val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            val mChannel = NotificationChannel(notificationChannelId, resources.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(mChannel)
        }

        val notification = mBuilder.setSmallIcon(R.drawable.ic_notification)
                .setTicker(applicationContext.resources.getString(R.string.app_name))
                .setWhen(0)
                .setContentTitle(applicationContext.resources.getString(R.string.app_name))
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                .setAutoCancel(true)
                .setContentText(message)
                .build()

        notificationManager.notify(0, notification)
    }

}
