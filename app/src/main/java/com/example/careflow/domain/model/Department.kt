package com.example.careflow.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Department(
    val id: String,
    val name: String,
    val description: String,
    val doctorCount: Int = 0,
    val iconName: String = "stethoscope"
)
