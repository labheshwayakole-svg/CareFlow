package com.example.careflow.domain.model

data class DoctorResetRequest(
    val id: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val doctorEmail: String = "",
    val hospitalId: String = "",
    val otp: String = "",
    val status: String = "PENDING",
    val requestedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 30 * 60 * 1000L,
    val adminEmail: String = "",
    val adminPhone: String = ""
)
