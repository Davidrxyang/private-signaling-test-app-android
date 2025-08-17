package com.example.private_signaling_test_app_android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.private_signaling_test_app_android.ui.theme.Private_signaling_test_app_androidTheme
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.*
import me.pushy.sdk.Pushy



class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "POST_NOTIFICATIONS permission granted.")
            } else {
                Log.w(TAG, "POST_NOTIFICATIONS permission denied.")
            }
        }

    // this function generates a pop up on the device and asks the user to enable
    // push notifications
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "POST_NOTIFICATIONS permission already granted.")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Log.i(TAG, "Showing rationale for POST_NOTIFICATIONS permission.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.i(TAG, "Requesting POST_NOTIFICATIONS permission.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()

        Firebase.messaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d(TAG, "FCM Registration Token: $token")
        }

        // pushy
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!Pushy.isRegistered(applicationContext)) {
                    val pushyToken = Pushy.register(applicationContext)
                    Log.d(TAG, "Pushy Registration Token: $pushyToken")
                    // Optional: send to your backend under a "pushy_token" field
                } else {
                    Thread {
                        try {
                            val pushyToken = Pushy.register(this@MainActivity)
                            Log.d("Pushy", "Device token: $pushyToken")
                        } catch (e: Exception) {
                            Log.e("Pushy", "Pushy registration failed", e)
                        }
                    }.start()
                }

                // Ensure notifications are shown when app is in background
                withContext(Dispatchers.Main) {
                    Pushy.toggleNotifications(true, applicationContext)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Pushy registration failed", e)
            }
        }

        setContent {
            Private_signaling_test_app_androidTheme {
                NotificationTestUI()
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun NotificationTestUI() {
    val context = LocalContext.current
    var message by remember { mutableStateOf(TextFieldValue("You pressed the button!")) }
    var delaySeconds by remember { mutableStateOf(TextFieldValue("0")) }
    var lastNotificationTime by remember { mutableStateOf(0L) }

    val scope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text(text = "Send Yourself a Notification")

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Notification Message") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = delaySeconds,
                    onValueChange = { delaySeconds = it },
                    label = { Text("Delay (seconds)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(onClick = {
                    val now = System.currentTimeMillis()
                    if (now - lastNotificationTime > 1000) {
                        val delay = delaySeconds.text.toIntOrNull()?.coerceAtLeast(0) ?: 0
                        Log.d("MainActivity", "Scheduling notification in $delay seconds")

                        scope.launch {
                            delay(delay * 1000L)
                            NotificationUtils.sendNotification(
                                context = context,
                                messageTitle = "Local Notification",
                                messageBody = message.text
                            )
                        }

                        lastNotificationTime = now
                    } else {
                        Log.d("MainActivity", "Notification throttled to prevent spamming.")
                    }
                }) {
                    Text("Send Push Notification")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Private_signaling_test_app_androidTheme {
        NotificationTestUI()
    }
}
