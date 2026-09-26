package com.example.careflow.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class QueueEstimate(
    val doctorName: String,
    val date: String,
    val currentServingToken: Int,
    val patientToken: Int,
    val tokensAhead: Int,
    val estimatedWaitMinutes: Int,
    val queueStatus: QueueStatus,
    val isYourTurn: Boolean,
    val currentCallingToken: Int = currentServingToken,
    val roomNumber: String = "102"
)
