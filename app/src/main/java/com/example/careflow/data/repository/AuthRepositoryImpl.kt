package com.example.careflow.data.repository

import android.app.Activity
import com.example.careflow.core.dispatcher.DefaultDispatcherProvider
import com.example.careflow.core.dispatcher.DispatcherProvider
import com.example.careflow.core.result.Resource
import com.example.careflow.data.datasource.InMemoryDataStore
import com.example.careflow.data.firebase.FirebaseDataSource
import com.example.careflow.domain.model.User
import com.example.careflow.domain.model.UserRole
import com.example.careflow.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepositoryImpl(
    private val dataStore: InMemoryDataStore,
    private val firebaseDataSource: FirebaseDataSource = FirebaseDataSource(),
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AuthRepository {

    private val _currentUserFlow = MutableStateFlow<User?>(null)

    override fun observeCurrentUser(): Flow<User?> = _currentUserFlow.asStateFlow()

    override suspend fun getCurrentUser(): User? = withContext(dispatchers.io) {
        _currentUserFlow.value
    }

    override suspend fun login(email: String, password: String): Resource<User> = withContext(dispatchers.io) {
        if (email.isBlank() || password.isBlank()) {
            return@withContext Resource.Error("Please enter your email and password.")
        }

        // 1. Try Firebase Auth
        val fbRes = firebaseDataSource.signInWithEmail(email.trim(), password)
        if (fbRes is Resource.Success) {
            _currentUserFlow.value = fbRes.data
            return@withContext fbRes
        }

        // 2. Fallback to local accounts (ensures demo vivas never break on spotty networks)
        val allUsers = dataStore.users.value
        val matchedUser = allUsers.find { it.email.equals(email.trim(), ignoreCase = true) }

        if (matchedUser != null) {
            _currentUserFlow.value = matchedUser
            Resource.Success(matchedUser)
        } else {
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
            _currentUserFlow.value = newUser
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

        // Register in Firebase Auth & Firestore
        val fbRes = firebaseDataSource.registerWithEmail(name, email, password, phone, role)
        if (fbRes is Resource.Success) {
            _currentUserFlow.value = fbRes.data
            return@withContext fbRes
        }

        // Fallback local registration
        val newUser = User(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim(),
            role = role
        )
        dataStore.registerUser(newUser)
        _currentUserFlow.value = newUser
        Resource.Success(newUser)
    }

    override suspend fun loginWithGoogle(idToken: String, role: UserRole): Resource<User> = withContext(dispatchers.io) {
        val res = firebaseDataSource.signInWithGoogleIdToken(idToken, role)
        if (res is Resource.Success) {
            _currentUserFlow.value = res.data
        }
        res
    }

    override suspend fun loginWithGoogleAccount(email: String, displayName: String, role: UserRole): Resource<User> = withContext(dispatchers.io) {
        val user = User(
            id = "google_${UUID.randomUUID().toString().take(8)}",
            name = displayName.ifBlank { "Google User" },
            email = email,
            phone = "",
            role = role
        )
        dataStore.registerUser(user)
        _currentUserFlow.value = user
        Resource.Success(user)
    }

    override suspend fun verifyPhoneNumber(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onAutoVerified: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        firebaseDataSource.startPhoneVerification(
            activity = activity,
            phoneNumber = phoneNumber,
            onCodeSent = onCodeSent,
            onAutoVerified = { credential ->
                kotlinx.coroutines.CoroutineScope(dispatchers.io).launch {
                    val res = firebaseDataSource.signInWithPhoneCredential(credential, "", UserRole.PATIENT)
                    if (res is Resource.Success<User>) {
                        _currentUserFlow.value = res.data
                        withContext(dispatchers.main) {
                            onAutoVerified(res.data)
                        }
                    }
                }
            },
            onError = onError
        )
    }

    override suspend fun signInWithPhoneCode(
        verificationId: String,
        code: String,
        name: String,
        role: UserRole
    ): Resource<User> = withContext(dispatchers.io) {
        val res = firebaseDataSource.signInWithPhoneCode(verificationId, code, name, role)
        if (res is Resource.Success<User>) {
            _currentUserFlow.value = res.data
            return@withContext res
        }

        // Test fallback for local development or sandbox numbers
        if (code == "123456" || verificationId == "test_verification_id") {
            val phoneUser = User(
                id = "phone_${UUID.randomUUID().toString().take(8)}",
                name = if (name.isNotBlank()) name else "Verified Patient",
                email = "patient@careflow.phone",
                phone = "+919800000000",
                role = role
            )
            dataStore.registerUser(phoneUser)
            _currentUserFlow.value = phoneUser
            return@withContext Resource.Success(phoneUser)
        }

        res
    }

    override suspend fun updateSelectedHospital(hospitalId: String): Resource<Unit> = withContext(dispatchers.io) {
        val current = _currentUserFlow.value
        if (current != null) {
            val updated = current.copy(selectedHospitalId = hospitalId)
            _currentUserFlow.value = updated
            dataStore.setCurrentUser(updated)
            try {
                firebaseDataSource.updateUserSelectedHospital(current.id, hospitalId)
            } catch (_: Exception) {}
        }
        Resource.Success(Unit)
    }

    override suspend fun logout(): Resource<Unit> = withContext(dispatchers.io) {
        firebaseDataSource.signOut()
        _currentUserFlow.value = null
        dataStore.setCurrentUser(null)
        Resource.Success(Unit)
    }

    override suspend fun switchDemoUser(role: UserRole): Resource<User> = withContext(dispatchers.io) {
        val target = when (role) {
            UserRole.PATIENT -> User(
                id = "patient_user_01",
                name = "Patient User",
                email = "patient@careflow.app",
                phone = "",
                role = UserRole.PATIENT
            )
            UserRole.DOCTOR -> User(
                id = "doctor_user_01",
                name = "Doctor User",
                email = "doctor@careflow.app",
                phone = "",
                role = UserRole.DOCTOR
            )
            UserRole.ADMIN -> User(
                id = "admin_user_01",
                name = "Hospital Administrator",
                email = "admin@careflow.app",
                phone = "",
                role = UserRole.ADMIN
            )
        }
        dataStore.registerUser(target)
        _currentUserFlow.value = target
        dataStore.setCurrentUser(target)
        Resource.Success(target)
    }
}
