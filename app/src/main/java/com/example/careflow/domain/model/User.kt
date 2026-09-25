package com.example.careflow.domain.model

import androidx.compose.runtime.Immutable

enum class UserRole {
    PATIENT,
    DOCTOR,
    ADMIN
}

@Immutable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val hospitalId: String? = null,
    val selectedHospitalId: String? = null,
    val profileImageUrl: String = "",
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
