package com.example.careflow.domain.model

import androidx.compose.runtime.Immutable

enum class NotificationType {
    APPOINTMENT_REMINDER_1HR,
    APPOINTMENT_REMINDER_30MIN,
    QUEUE_CALLED,
    DOCTOR_ON_BREAK,
    DOCTOR_BACK_FROM_BREAK,
    DOCTOR_AVAILABLE,
    DOCTOR_LEFT_OPD,
    QUEUE_POSITION_UPDATE
}

@Immutable
data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val appointmentId: String? = null,
    val doctorId: String? = null,
    val hospitalId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
