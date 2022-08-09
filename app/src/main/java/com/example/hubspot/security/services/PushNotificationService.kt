package com.example.hubspot.security.services

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.hubspot.R
import com.example.hubspot.security.models.RequestQueueSingleton
import com.example.hubspot.security.ui.PushNotificationMapsActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.util.HashMap

/**
 * The push notification service capable of handling received messages from a subscribed friend and
 * creates a push notification as well as pushing out notifications to the user's subscribers.
 *
 * Various code adapted from:
 * https://medium.com/@mendhie/send-device-to-device-push-notifications-without-server-side-code-238611c143
 * https://developer.android.com/codelabs/advanced-android-kotlin-training-notifications-fcm#4
 */
class PushNotificationService : FirebaseMessagingService() {
    private val notifyId = 1
    private val pushChannel = "ping_channel"
    private val fcmApi = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=" + "AAAAVSHuFvs:APA91bFsGivsHFNl6VZiXpqYdT1JVKX-WPbYeuCmRPDcjQesR9Oku7xbr7YTgZxy8cpOVNL0OsELUjlHtj5T-if6TCQ2cTiihyzdoXZz_9Nk_eydc0Qr9QnY1OD9Gbh_yQKZ2J3BegQP"
    private val contentType = "application/json"
    private val pushNotificationRequestCode = 11


    /**
     * Called when message is received.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        println("From: ${remoteMessage?.from}")

        // Check if message contains a data payload.
        remoteMessage?.data?.let {
            println("Message data payload: " + remoteMessage.data)
            showNotification(it)
        }

        // Check if message contains a notification payload.
        remoteMessage?.notification?.let {
            println( "Message Notification Body: ${it.body}")
        }
    }

    /**
     * Builds and shows a notification from a subscribed friend.
     */
    private fun showNotification(notification: Map<String, String>) {
        val title = notification["title"]
        val body = notification["message"]
        val lat = notification["lat"]
        val long = notification["long"]
        val friendsName = notification["name"]

        val notificationIntent = Intent(this, PushNotificationMapsActivity::class.java)
            .putExtra("name", friendsName)
            .putExtra("lat", lat)
            .putExtra("long", long)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, pushNotificationRequestCode,
            notificationIntent, PendingIntent.FLAG_MUTABLE)

        val notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, pushChannel)

        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setContentTitle(title)
        notificationBuilder.setContentText(body)
        notificationBuilder.setSmallIcon(R.drawable.ic_baseline_security_24)
        val notification = notificationBuilder.build()

        if(Build.VERSION.SDK_INT > 26) {
            val notificationChannel = NotificationChannel(pushChannel, "Ping Location Channel",
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(notifyId, notification)
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        println( "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }

    /**
     * Utilizes the FCM API and Json Object Request to send a Json object notification.
     */
    fun sendNotification(notification: JSONObject?, activity: Activity) {

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(fcmApi, notification,
            Response.Listener { response ->
                println( "onResponse: $response")
            },
            Response.ErrorListener {
                println("onErrorResponse: $it")
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        RequestQueueSingleton.getInstance(activity)!!
            .addToRequestQueue(jsonObjectRequest)
    }

    /**
     * Persist token to third-party servers.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {}

}