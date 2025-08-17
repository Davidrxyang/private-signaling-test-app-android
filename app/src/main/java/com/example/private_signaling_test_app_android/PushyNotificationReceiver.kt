package com.example.private_signaling_test_app_android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PushyNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("PushyReceiver", "onReceive payload=${intent.extras}")
        val title = intent.getStringExtra("title") ?: "Pushy Message"
        val body  = intent.getStringExtra("body") ?: intent.getStringExtra("message") ?: ""
        NotificationUtils.sendNotification(context, title, body)
    }
}
