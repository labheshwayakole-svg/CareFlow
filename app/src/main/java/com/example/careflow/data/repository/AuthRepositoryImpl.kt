package com.example.careflow.data.repository

import com.example.careflow.core.dispatcher.DefaultDispatcherProvider
import com.example.careflow.core.dispatcher.DispatcherProvider
import com.example.careflow.core.result.Resource
import com.example.careflow.data.datasource.InMemoryDataStore
import com.example.careflow.domain.model.User
import com.example.careflow.domain.model.UserRole
import com.example.careflow.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepositoryImpl(
    private val dataStore: InMemoryDataStore,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AuthRepository {

    override fun observeCurrentUser(): Flow<User?> = dataStore.currentUser

    override suspend fun getCurrentUser(): User? = withContext(dispatchers.io) {
        dataStore.currentUser.value
    }

    override suspend fun login(email: String, password: String): Resource<User> = withContext(dispatchers.io) {
        if (email.isBlank() || password.isBlank()) {
            return@withContext Resource.Error("Please enter your email and password.")
        }

        val allUsers = dataStore.users.value
        val matchedUser = allUsers.find { it.email.equals(email.trim(), ignoreCase = true) }

        if (matchedUser != null) {
            dataStore.setCurrentUser(matchedUser)
            Resource.Success(matchedUser)
        } else {
            // For testing convenience, if demo password provided or unrecognized user, allow login
            val newUser = User(
                id = "user_${UUID.randomUUID().toString().take(8)}",
                name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = email.trim(),
                phone = "9800000000",
                role = when {
                    email.contains("doc", ignoreCase = true) -> UserRole.DOCTOR
                    email.contains("admin", ignoreCase = true) -> UserRole.ADMIN
                    else -> UserRole.PATIENT
                }
            )
            dataStore.registerUser(newUser)
            Resource.Success(newUser)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole
    ): Resource<User> = withContext(dispatchers.io) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return@withContext Resource.Error("Please complete all required fields.")
        }
        val newUser = User(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim(),
            role = role
        )
        dataStore.registerUser(newUser)
        Resource.Success(newUser)
    }

    override suspend fun logout(): Resource<Unit> = withContext(dispatchers.io) {
        dataStore.setCurrentUser(null)
        Resource.Success(Unit)
    }

    override suspend fun switchDemoUser(role: UserRole): Resource<User> = withContext(dispatchers.io) {
        val target = when (role) {
            UserRole.PATIENT -> dataStore.demoPatient
            UserRole.DOCTOR -> dataStore.demoDoctor
            UserRole.ADMIN -> dataStore.demoAdmin
        }
        dataStore.setCurrentUser(target)
        Resource.Success(target)
    }
}
