package com.example.careflow.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.careflow.domain.model.User
import com.example.careflow.domain.model.UserRole

class UserSessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        prefs.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_NAME, user.name)
            putString(KEY_EMAIL, user.email)
            putString(KEY_PHONE, user.phone)
            putString(KEY_ROLE, user.role.name)
            putString(KEY_HOSPITAL_ID, user.hospitalId)
            putString(KEY_SELECTED_HOSPITAL_ID, user.selectedHospitalId)
            putString(KEY_PROFILE_IMAGE, user.profileImageUrl)
            putString(KEY_FCM_TOKEN, user.fcmToken)
            putLong(KEY_CREATED_AT, user.createdAt)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUser(): User? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val id = prefs.getString(KEY_USER_ID, null)
        if (!isLoggedIn || id.isNullOrBlank()) return null

        val roleStr = prefs.getString(KEY_ROLE, UserRole.PATIENT.name) ?: UserRole.PATIENT.name
        val role = try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            UserRole.PATIENT
        }
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val phone = prefs.getString(KEY_PHONE, "") ?: ""
        val storedHospId = prefs.getString(KEY_HOSPITAL_ID, null)
        val storedSelectedHospId = prefs.getString(KEY_SELECTED_HOSPITAL_ID, null) ?: storedHospId

        val storedName = prefs.getString(KEY_NAME, "") ?: ""
        val resolvedName = when {
            storedName.isNotBlank() -> storedName
            role == UserRole.ADMIN -> "Hospital Administrator"
            email.isNotBlank() -> email.substringBefore("@").replaceFirstChar { it.uppercase() }
            else -> "User"
        }

        return User(
            id = id,
            name = resolvedName,
            email = email,
            phone = phone,
            role = role,
            hospitalId = storedHospId,
            selectedHospitalId = storedSelectedHospId,
            profileImageUrl = prefs.getString(KEY_PROFILE_IMAGE, "") ?: "",
            fcmToken = prefs.getString(KEY_FCM_TOKEN, "") ?: "",
            createdAt = prefs.getLong(KEY_CREATED_AT, System.currentTimeMillis())
        )
    }

    fun updateSelectedHospital(hospitalId: String?) {
        prefs.edit().putString(KEY_SELECTED_HOSPITAL_ID, hospitalId).apply()
    }

    fun updateHospitalId(hospitalId: String?) {
        prefs.edit().putString(KEY_HOSPITAL_ID, hospitalId).apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "careflow_user_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_ROLE = "role"
        private const val KEY_HOSPITAL_ID = "hospital_id"
        private const val KEY_SELECTED_HOSPITAL_ID = "selected_hospital_id"
        private const val KEY_PROFILE_IMAGE = "profile_image"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_CREATED_AT = "created_at"
    }
}
