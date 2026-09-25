package com.example.careflow.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Doctor(
    val id: String,
    val userId: String,
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
    val rating: Double = 4.8,
    val totalReviews: Int = 120,
    val photoUrl: String = ""
)
