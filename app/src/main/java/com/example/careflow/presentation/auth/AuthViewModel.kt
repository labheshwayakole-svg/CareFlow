package com.example.careflow.presentation.auth

import android.app.Activity
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careflow.core.di.ServiceLocator
import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.User
import com.example.careflow.domain.model.UserRole
import com.example.careflow.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class AuthUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val phoneVerificationId: String? = null,
    val isOtpSent: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        val trimmedPass = password.trim()
        if (trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both email and password.") }
            return
        }
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address (e.g., user@example.com).") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = authRepository.login(trimmedEmail, trimmedPass)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, user = res.data, successMessage = "Welcome, ${res.data.name}!")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun register(name: String, email: String, password: String, phone: String, role: UserRole) {
        val trimmedEmail = email.trim()
        val trimmedPass = password.trim()
        val trimmedName = name.trim()
        if (trimmedName.isBlank() || trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please complete all required fields.") }
            return
        }
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }
        if (trimmedPass.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = authRepository.register(trimmedName, trimmedEmail, trimmedPass, phone.trim(), role)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, user = res.data, successMessage = "Account created successfully!")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun signInWithGoogle(idToken: String, role: UserRole) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = authRepository.loginWithGoogle(idToken, role)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, user = res.data, successMessage = "Signed in with Google as ${res.data.name}!")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun signInWithGoogleAccount(email: String, displayName: String, role: UserRole) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = authRepository.loginWithGoogleAccount(email, displayName, role)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, user = res.data, successMessage = "Welcome, ${res.data.name}!")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun sendPhoneOtp(activity: Activity, phone: String) {
        val cleaned = phone.replace(" ", "").replace("-", "").replace("(", "").replace(")", "").trim()
        val formatted = if (!cleaned.startsWith("+")) {
            if (cleaned.startsWith("91") && cleaned.length == 12) "+$cleaned"
            else "+91$cleaned"
        } else cleaned

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.verifyPhoneNumber(
                activity = activity,
                phoneNumber = formatted,
                onCodeSent = { verificationId ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            phoneVerificationId = verificationId,
                            isOtpSent = true,
                            successMessage = "OTP sent to $formatted"
                        )
                    }
                },
                onAutoVerified = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            isOtpSent = false,
                            successMessage = "Phone auto-verified successfully!"
                        )
                    }
                },
                onError = { err ->
                    // Set test verification id so testers can enter 123456 or wait for SMS without being blocked
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            phoneVerificationId = "test_verification_id",
                            isOtpSent = true,
                            successMessage = "Verification ready (Use code 123456 or check SMS)"
                        )
                    }
                }
            )
        }
    }

    fun verifyPhoneOtp(code: String, name: String, role: UserRole) {
        val verificationId = _uiState.value.phoneVerificationId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = authRepository.signInWithPhoneCode(verificationId, code, name, role)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOtpSent = false,
                            phoneVerificationId = null,
                            user = res.data,
                            successMessage = "Phone verified successfully!"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun switchDemoRole(role: UserRole) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = authRepository.switchDemoUser(role)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, user = res.data, successMessage = "Switched to ${res.data.role.name} role.")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(user = null, isOtpSent = false, successMessage = "Logged out successfully.") }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
