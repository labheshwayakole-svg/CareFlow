package com.example.careflow.domain.repository

import android.app.Activity
import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.User
import com.example.careflow.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeCurrentUser(): Flow<User?>
    suspend fun getCurrentUser(): User?
    suspend fun login(email: String, password: String, portalRole: UserRole = UserRole.PATIENT): Resource<User>
    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole
    ): Resource<User>
    suspend fun loginWithGoogle(idToken: String, role: UserRole = UserRole.PATIENT): Resource<User>
    suspend fun loginWithGoogleAccount(email: String, displayName: String, role: UserRole = UserRole.PATIENT): Resource<User>
    suspend fun verifyPhoneNumber(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onAutoVerified: (User) -> Unit,
        onError: (String) -> Unit
    )
    suspend fun signInWithPhoneCode(verificationId: String, code: String, name: String, role: UserRole): Resource<User>
    suspend fun updateSelectedHospital(hospitalId: String): Resource<Unit>
    suspend fun updateHospitalId(hospitalId: String): Resource<Unit>
    suspend fun updateUserEmail(newEmail: String): Resource<Unit>
    suspend fun updateAdminAndHospitalProfile(
        adminName: String,
        adminEmail: String,
        adminPhone: String,
        hospitalName: String,
        opdName: String,
        address: String,
        city: String,
        hospitalPhone: String
    ): Resource<Unit>
    suspend fun logout(): Resource<Unit>
    suspend fun switchDemoUser(role: UserRole): Resource<User>
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
    suspend fun requestDoctorPasswordReset(idOrEmail: String): Resource<com.example.careflow.domain.model.DoctorResetRequest>
    suspend fun verifyDoctorPasswordReset(idOrEmail: String, otp: String, newPassword: String): Resource<Unit>
    fun observeDoctorResetRequests(hospitalId: String? = null): Flow<List<com.example.careflow.domain.model.DoctorResetRequest>>
    suspend fun dismissDoctorResetRequest(requestId: String): Resource<Unit>
    suspend fun requestUniversalPasswordReset(
        idEmailOrPhone: String,
        portalRole: UserRole? = null,
        optionalPhone: String? = null
    ): Resource<com.example.careflow.domain.model.PasswordResetSession>
    suspend fun validateUniversalResetOtp(resetId: String, otp: String): Resource<Unit>
    suspend fun verifyUniversalPasswordReset(resetId: String, otp: String, newPassword: String): Resource<User>
}
