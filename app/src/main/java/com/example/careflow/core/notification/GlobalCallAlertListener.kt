package com.example.careflow.core.notification

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Application-level background listener that observes real-time patient call notifications
 * directly from Cloud Firestore regardless of whether the user is on the dashboard, in background,
 * or on any screen in the application.
 */
object GlobalCallAlertListener {

    private var listenerRegistration: ListenerRegistration? = null
    private val handledNotificationIds = mutableSetOf<String>()
    private var lastHandledTimestamp: Long = System.currentTimeMillis() - 30_000L

    fun start(context: Context) {
        try {
            FirebaseAuth.getInstance().addAuthStateListener { auth ->
                val user = auth.currentUser
                if (user != null) {
                    attachListener(context.applicationContext, user.uid)
                } else {
                    listenerRegistration?.remove()
                    listenerRegistration = null
                }
            }
        } catch (_: Exception) {}
    }

    private fun attachListener(context: Context, userId: String) {
        listenerRegistration?.remove()
        try {
            val firestore = FirebaseFirestore.getInstance()
            listenerRegistration = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    for (docChange in snapshot.documentChanges) {
                        if (docChange.type == DocumentChange.Type.ADDED || docChange.type == DocumentChange.Type.MODIFIED) {
                            val data = docChange.document.data
                            val id = docChange.document.id
                            val type = data["type"] as? String ?: ""
                            val isRead = data["isRead"] as? Boolean ?: false
                            val createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L

                            if (type == "QUEUE_CALLED" && !isRead && id !in handledNotificationIds && createdAt >= lastHandledTimestamp) {
                                handledNotificationIds.add(id)
                                lastHandledTimestamp = maxOf(lastHandledTimestamp, createdAt)

                                val title = data["title"] as? String ?: "It's Your Turn!"
                                val body = data["body"] as? String ?: "Doctor is calling your token now."
                                val appointmentId = data["appointmentId"] as? String ?: "apt_$id"

                                val tokenNumber = title.substringAfter("Token #", "")
                                    .substringBefore(")").trim().toIntOrNull()
                                    ?: title.filter { it.isDigit() }.toIntOrNull()
                                    ?: 0

                                val doctorName = if (body.contains("Dr. ")) {
                                    body.substringAfter("Dr. ").substringBefore(" is calling").trim()
                                } else {
                                    "Doctor"
                                }

                                // Unified 3.5s chime, vibration, and swipe-dismissable heads-up notification
                                CallNotificationService.triggerAlert(
                                    context = context,
                                    alertId = id,
                                    tokenNumber = tokenNumber,
                                    doctorName = doctorName,
                                    appointmentId = appointmentId
                                )
                            }
                        }
                    }
                }
        } catch (_: Exception) {}
    }
}
