package com.example.careflow.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.careflow.MainActivity
import com.example.careflow.R

/**
 * Singleton service responsible for patient call audio alerts, vibration,
 * and high-priority heads-up system notifications.
 *
 * Rules:
 * 1. Plays alert chime for exactly 3.5 seconds (single non-looping track) and stops automatically.
 * 2. Immediately stops ringing & vibration if:
 *    - User taps the notification.
 *    - User swipes away / dismisses the notification.
 *    - User taps "I'm Coming!" or "Live Track Queue" in the in-app overlay.
 * 3. Uses non-looping notification category (CATEGORY_EVENT) so Android OS does not continuously repeat the ringtone.
 */
object CallNotificationService {

    const val CHANNEL_ID = "careflow_queue_call_channel_v4"
    private const val NOTIFICATION_ID = 1001

    private var mediaPlayer: MediaPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null

    private var lastTriggeredAlertId: String? = null
    private var lastTriggeredTimestamp: Long = 0L

    /**
     * Creates the High-Priority notification channel required for Android 8.0+ (including Android 10+).
     * Configures custom sound, vibration pattern, and heads-up alert visibility without looping.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            // Remove legacy channels that may have cached looping settings
            try {
                manager?.deleteNotificationChannel("careflow_queue_call_channel_v3")
                manager?.deleteNotificationChannel("careflow_queue_call_channel_v2")
                manager?.deleteNotificationChannel("careflow_queue_call_channel")
            } catch (_: Exception) {}

            val soundUri = Uri.parse(
                "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.queue_call_alert}"
            )

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 400)

            val channel = NotificationChannel(
                CHANNEL_ID,
                "OPD Queue Call Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when the doctor calls your token number"
                enableLights(true)
                enableVibration(true)
                this.vibrationPattern = vibrationPattern
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
            }

            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * Unified trigger called when a patient is called by the doctor.
     * Deduplicates triggers within 4 seconds so simultaneous background and in-app listeners
     * do not produce duplicate ringtones or notifications.
     */
    fun triggerAlert(
        context: Context,
        alertId: String,
        tokenNumber: Int,
        doctorName: String,
        appointmentId: String
    ) {
        val now = System.currentTimeMillis()
        if (alertId == lastTriggeredAlertId && (now - lastTriggeredTimestamp) < 4000L) {
            return // Skip duplicate trigger within 4s window
        }
        lastTriggeredAlertId = alertId
        lastTriggeredTimestamp = now

        // 1. Play single 3.5s audio alert chime & vibration
        playCallAlert(context, durationMs = 3800L)

        // 2. Post heads-up system notification with swipe-dismiss action
        postSystemNotification(
            context = context,
            tokenNumber = tokenNumber,
            doctorName = doctorName,
            appointmentId = appointmentId
        )
    }

    /**
     * Plays the alert chime for strictly 3.5-3.8 seconds (single playthrough) and vibrates the device.
     * Guaranteed to stop automatically after completion or timeout.
     */
    fun playCallAlert(context: Context, durationMs: Long = 3800L) {
        mainHandler.post {
            try {
                stopAlert(context) // Stop any previous playback cleanly

                val soundUri = Uri.parse(
                    "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.queue_call_alert}"
                )

                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                            .build()
                    )
                    setDataSource(context, soundUri)
                    isLooping = false // NEVER loop continuously! Single 3.5s chime
                    setOnPreparedListener { mp ->
                        try {
                            mp.start()
                        } catch (_: Exception) {}
                    }
                    setOnCompletionListener {
                        stopAlert(context)
                    }
                    setOnErrorListener { mp, _, _ ->
                        try {
                            mp.release()
                        } catch (_: Exception) {}
                        mediaPlayer = null
                        true
                    }
                    prepareAsync()
                }

                // Trigger non-repeating vibration
                triggerVibration(context)

                // Safety timeout auto-stop after durationMs
                stopRunnable = Runnable {
                    stopAlert(context)
                }
                mainHandler.postDelayed(stopRunnable!!, durationMs)

            } catch (_: Exception) {
                // Fallback: If custom sound loading fails, attempt simple notification chime
                try {
                    mediaPlayer?.release()
                    mediaPlayer = null
                    val defaultUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                    val r = android.media.RingtoneManager.getRingtone(context, defaultUri)
                    r?.play()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Vibrates the device using a 2.5-second non-repeating buzz pattern.
     */
    @Suppress("DEPRECATION")
    private fun triggerVibration(context: Context) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                val timings = longArrayOf(0, 400, 200, 400, 200, 400)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, -1)) // -1 = do not repeat
                } else {
                    vibrator.vibrate(timings, -1) // -1 = do not repeat
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Immediately stops and releases the MediaPlayer, cancels vibration, and clears pending auto-stop callbacks.
     */
    @Suppress("DEPRECATION")
    fun stopAlert(context: Context? = null) {
        mainHandler.post {
            stopRunnable?.let { mainHandler.removeCallbacks(it) }
            stopRunnable = null

            try {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        mp.stop()
                    }
                    mp.release()
                }
            } catch (_: Exception) {}
            mediaPlayer = null

            // Cancel vibration immediately
            if (context != null) {
                try {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    vibrator?.cancel()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Dismisses the system notification from the Android status bar / shade.
     */
    fun cancelNotification(context: Context?) {
        if (context == null) return
        mainHandler.post {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.cancel(NOTIFICATION_ID)
            } catch (_: Exception) {}
        }
    }

    /**
     * Posts a heads-up Android system notification with sound, vibration,
     * full-screen intent support, and swipe-to-dismiss receiver.
     */
    fun postSystemNotification(
        context: Context,
        tokenNumber: Int,
        doctorName: String,
        appointmentId: String
    ) {
        try {
            // Android 13+ permission check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                if (permission != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }

            // Tapping notification opens MainActivity to Live Queue Tracker
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_NAVIGATE_TO", "QUEUE_TRACKER")
                putExtra("EXTRA_APPOINTMENT_ID", appointmentId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Swiping away notification triggers CallNotificationDismissReceiver to stop ringing
            val dismissIntent = Intent(context, CallNotificationDismissReceiver::class.java).apply {
                action = "com.example.careflow.ACTION_DISMISS_CALL_ALERT"
            }
            val deletePendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID + 1,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val soundUri = Uri.parse(
                "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.queue_call_alert}"
            )

            val cleanDocName = doctorName.removePrefix("Dr.").removePrefix("Dr. ").trim().let { if (it.isNotBlank()) "Dr. $it" else "Doctor" }
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("It's Your Turn! Token #$tokenNumber")
                .setContentText("$cleanDocName is calling you now. Please proceed to the OPD room.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$cleanDocName is calling you now. Please proceed into the OPD consultation room immediately.")
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_EVENT) // Do NOT use CATEGORY_CALL (which loops indefinitely)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(soundUri)
                .setVibrate(longArrayOf(0, 400, 200, 400, 200, 400))
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deletePendingIntent) // User swipes notification -> immediately stop ringing
                .setFullScreenIntent(pendingIntent, false)
                .setAutoCancel(true)
                .setTimeoutAfter(30000L) // Auto-clear after 30 seconds
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {}
    }
}
