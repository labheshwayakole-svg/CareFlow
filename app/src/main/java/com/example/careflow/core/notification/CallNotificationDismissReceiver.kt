package com.example.careflow.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver triggered when the patient swipes away / dismisses the
 * queue call notification from the Android notification shade or heads-up banner.
 * Immediately halts all audio playback, vibration, and releases media resources.
 */
class CallNotificationDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            CallNotificationService.stopAlert(context)
            CallNotificationService.cancelNotification(context)
        }
    }
}
