package com.example.careflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.careflow.theme.CareFlowTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Stop alert ringing and clear notification immediately when app is opened or tapped
    com.example.careflow.core.notification.CallNotificationService.stopAlert(this)
    com.example.careflow.core.notification.CallNotificationService.cancelNotification(this)

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true)
      setTurnScreenOn(true)
    } else {
      @Suppress("DEPRECATION")
      window.addFlags(
        android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
        android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
      )
    }

    enableEdgeToEdge()
    setContent {
      CareFlowTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    // Immediately stop alert chime and dismiss system notification when tapped
    com.example.careflow.core.notification.CallNotificationService.stopAlert(this)
    com.example.careflow.core.notification.CallNotificationService.cancelNotification(this)
  }
}
