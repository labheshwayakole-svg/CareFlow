package com.example.careflow.domain.repository

import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.User
import com.example.careflow.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeCurrentUser(): Flow<User?>
    suspend fun getCurrentUser(): User?
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole
    ): Resource<User>
    suspend fun logout(): Resource<Unit>
    suspend fun switchDemoUser(role: UserRole): Resource<User>
}
