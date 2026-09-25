package com.example.careflow.domain.model

import androidx.compose.runtime.Immutable

enum class AppointmentStatus {
    BOOKED,
    IN_CONSULTATION,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}

@Immutable
data class Appointment(
    val id: String,
    val patientId: String,
    val patientName: String,
    val patientPhone: String,
    val doctorId: String,
    val doctorName: String,
    val departmentName: String,
    val date: String, // Format: YYYY-MM-DD
    val timeSlot: String,
    val tokenNumber: Int,
    val status: AppointmentStatus = AppointmentStatus.BOOKED,
    val bookedAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val doctorNotes: String = ""
)
