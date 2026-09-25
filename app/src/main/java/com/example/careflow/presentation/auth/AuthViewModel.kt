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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = authRepository.login(email, password)) {
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = authRepository.register(name, email, password, phone, role)) {
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

    fun sendPhoneOtp(activity: Activity, phone: String) {
        val formatted = if (!phone.startsWith("+")) "+91$phone" else phone
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
                onError = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err) }
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
