package com.example.careflow.domain.model

import androidx.compose.runtime.Immutable

enum class QueueStatus {
    NOT_STARTED,
    ACTIVE,
    PAUSED,
    CLOSED
}

@Immutable
data class DailyQueue(
    val queueId: String,
    val doctorId: String,
    val doctorName: String,
    val date: String, // Format: YYYY-MM-DD
    val currentToken: Int = 0,
    val totalTokensIssued: Int = 0,
    val status: QueueStatus = QueueStatus.NOT_STARTED,
    val updatedAt: Long = System.currentTimeMillis()
)
