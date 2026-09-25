package com.example.careflow.domain.model

import androidx.compose.runtime.Immutable

enum class QueueStatus {
    NOT_STARTED,
    ACTIVE,
    PAUSED,
    BREAK,
    CLOSED
}

@Immutable
data class DailyQueue(
    val queueId: String,
    val doctorId: String,
    val doctorName: String,
    val hospitalId: String = "",
    val date: String, // Format: YYYY-MM-DD
    val currentToken: Int = 0,
    val currentPatientId: String? = null,
    val totalTokensIssued: Int = 0,
    val totalCompleted: Int = 0,
    val totalNoShow: Int = 0,
    val totalCancelled: Int = 0,
    val status: QueueStatus = QueueStatus.NOT_STARTED,
    val breakMessage: String? = null,
    val queueStartedAt: Long? = null,
    val lastAdvancedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
