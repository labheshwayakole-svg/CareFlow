package com.example.careflow.domain.model

import androidx.compose.runtime.Immutable

enum class AppointmentStatus {
    BOOKED,
    WAITING,
    CALLED,
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
    val hospitalId: String = "",
    val hospitalName: String = "",
    val departmentId: String = "",
    val departmentName: String,
    val date: String, // Format: YYYY-MM-DD
    val timeSlot: String,
    val estimatedTime: String = "",
    val tokenNumber: Int,
    val status: AppointmentStatus = AppointmentStatus.BOOKED,
    val symptoms: String = "",
    val notes: String = symptoms,
    val doctorNotes: String = "",
    val calledAt: Long? = null,
    val completedAt: Long? = null,
    val bookedAt: Long = System.currentTimeMillis(),
    val reminder1hrSent: Boolean = false,
    val reminder30minSent: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
