package com.example.careflow.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Doctor(
    val id: String,
    val userId: String,
    val hospitalId: String = "",
    val hospitalName: String = "",
    val roomNumber: String = "102",
    val name: String,
    val specialization: String,
    val departmentId: String,
    val departmentName: String,
    val qualification: String,
    val experienceYears: Int,
    val consultationFee: Double,
    val avgConsultationMinutes: Int = 15,
    val availableDays: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
    val shiftStartTime: String = "09:00 AM",
    val shiftEndTime: String = "02:00 PM",
    val maxTokensPerDay: Int = 25,
    val isAvailable: Boolean = true,
    val isInOPD: Boolean = false,
    val isOnBreak: Boolean = false,
    val breakMessage: String = "",
    val breakStartedAt: Long? = null,
    val rating: Double = 4.8,
    val totalReviews: Int = 0,
    val photoUrl: String = "",
    val loginEmail: String = "",
    val password: String = "",
    val passwordChanged: Boolean = false,
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
