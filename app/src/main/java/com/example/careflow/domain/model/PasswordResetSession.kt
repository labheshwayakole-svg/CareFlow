package com.example.careflow.domain.model

data class PasswordResetSession(
    val resetId: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.PATIENT,
    val otp: String = "",
    val status: String = "PENDING", // PENDING, COMPLETED, EXPIRED
    val requestedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 15 * 60 * 1000L
)
