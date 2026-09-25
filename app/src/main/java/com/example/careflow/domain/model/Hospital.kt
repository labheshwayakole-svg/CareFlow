package com.example.careflow.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Hospital(
    val id: String,
    val name: String,
    val opdName: String = "",
    val address: String = "",
    val city: String = "",
    val phone: String = "",
    val adminId: String,
    val departmentIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
