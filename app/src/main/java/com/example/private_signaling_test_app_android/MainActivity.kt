package com.example.private_signaling_test_app_android

import android.Manifest // Required for Manifest.permission
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts // Required for registerForActivityResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat // Required for ContextCompat
import com.example.private_signaling_test_app_android.ui.theme.Private_signaling_test_app_androidTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging


class MainActivity : ComponentActivity() {

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // FCM SDK (and your app) can post notifications.
                Log.d(TAG, "POST_NOTIFICATIONS permission granted.")
            } else {
                // TODO: Inform user that that your app will not show notifications.
                Log.w(TAG, "POST_NOTIFICATIONS permission denied.")
                // You could show a dialog explaining why the permission is needed and offer to open settings.
            }
        }

    private fun askNotificationPermission() {
        // This is only necessary for API level 33 and higher.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
                Log.d(TAG, "POST_NOTIFICATIONS permission already granted.")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: Display an educational UI explaining to the user the features that will be enabled
                // by granting the POST_NOTIFICATION permission. This UI should provide the user
                // "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                // If the user selects "No thanks," allow the user to continue without notifications.
                Log.i(TAG, "Showing rationale for POST_NOTIFICATIONS permission.")
                // For this example, we'll just request it directly again. In a real app, show UI.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly ask for the permission
                Log.i(TAG, "Requesting POST_NOTIFICATIONS permission.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission() // Ask for notification permission on create

        // Get the FCM registration token (optional, but good for testing/debugging)
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            // Log and toast
            Log.d(TAG, "FCM Registration Token: $token")
            // You would typically send this token to your backend server.
        }


        setContent {
            Private_signaling_test_app_androidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android User with FCM!",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Private_signaling_test_app_androidTheme {
        Greeting("Android")
    }
}
    