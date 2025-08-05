package com.example.private_signaling_test_app_android

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            if (title != null && body != null) {
                NotificationUtils.sendNotification(this, title, body)
            }
        }

        // Handle notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            NotificationUtils.sendNotification(this, it.title, it.body)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationToServer($token)")
        // TODO: Send token to your backend if needed
    }
}
