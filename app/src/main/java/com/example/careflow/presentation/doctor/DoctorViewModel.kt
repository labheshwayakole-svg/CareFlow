package com.example.careflow.presentation.doctor

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careflow.core.di.ServiceLocator
import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.model.QueueStatus
import com.example.careflow.domain.repository.AppointmentRepository
import com.example.careflow.domain.repository.AuthRepository
import com.example.careflow.domain.repository.DoctorRepository
import com.example.careflow.domain.repository.QueueRepository
import com.example.careflow.domain.usecase.AdvanceQueueUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Immutable
data class DoctorUiState(
    val doctor: Doctor? = null,
    val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val todayQueue: DailyQueue? = null,
    val todayAppointments: List<Appointment> = emptyList(),
    val currentServingAppointment: Appointment? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class DoctorViewModel(
    private val authRepository: AuthRepository = ServiceLocator.authRepository,
    private val doctorRepository: DoctorRepository = ServiceLocator.doctorRepository,
    private val appointmentRepository: AppointmentRepository = ServiceLocator.appointmentRepository,
    private val queueRepository: QueueRepository = ServiceLocator.queueRepository,
    private val advanceQueueUseCase: AdvanceQueueUseCase = ServiceLocator.advanceQueueUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorUiState())
    val uiState: StateFlow<DoctorUiState> = _uiState.asStateFlow()

    init {
        loadDoctorProfile()
    }

    private fun loadDoctorProfile() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                if (user != null) {
                    when (val res = doctorRepository.getDoctorByUserId(user.id)) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(doctor = res.data) }
                            observeDoctorQueue(res.data.id)
                            observeDoctorAppointments(res.data.id)
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(errorMessage = res.message) }
                        }
                        is Resource.Loading -> {}
                    }
                }
            }
        }
    }

    private fun observeDoctorQueue(doctorId: String) {
        viewModelScope.launch {
            queueRepository.observeLiveQueue(doctorId, _uiState.value.todayDate).collect { queue ->
                _uiState.update { state ->
                    val currentServing = state.todayAppointments.find {
                        it.tokenNumber == (queue?.currentToken ?: 0) && it.status == AppointmentStatus.IN_CONSULTATION
                    }
                    state.copy(todayQueue = queue, currentServingAppointment = currentServing)
                }
            }
        }
    }

    private fun observeDoctorAppointments(doctorId: String) {
        viewModelScope.launch {
            appointmentRepository.observeAppointmentsForDoctor(doctorId, _uiState.value.todayDate).collect { apts ->
                _uiState.update { state ->
                    val currentServing = apts.find {
                        it.tokenNumber == (state.todayQueue?.currentToken ?: 0) && it.status == AppointmentStatus.IN_CONSULTATION
                    }
                    state.copy(todayAppointments = apts, currentServingAppointment = currentServing)
                }
            }
        }
    }

    fun callNextPatient() {
        val doc = _uiState.value.doctor ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = advanceQueueUseCase(doc.id, _uiState.value.todayDate)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            todayQueue = res.data,
                            successMessage = "Calling Token #${res.data.currentToken}"
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

    fun completeCurrentConsultation(notes: String = "") {
        val current = _uiState.value.currentServingAppointment ?: return
        viewModelScope.launch {
            appointmentRepository.updateAppointmentStatus(
                appointmentId = current.id,
                status = AppointmentStatus.COMPLETED,
                doctorNotes = notes
            )
            _uiState.update { it.copy(successMessage = "Consultation for Token #${current.tokenNumber} marked as completed.") }
        }
    }

    fun markCurrentNoShow() {
        val current = _uiState.value.currentServingAppointment ?: return
        viewModelScope.launch {
            appointmentRepository.updateAppointmentStatus(
                appointmentId = current.id,
                status = AppointmentStatus.NO_SHOW
            )
            _uiState.update { it.copy(successMessage = "Patient #${current.tokenNumber} marked as Absent/No-Show.") }
        }
    }

    fun toggleAvailability(isAvailable: Boolean) {
        val doc = _uiState.value.doctor ?: return
        viewModelScope.launch {
            doctorRepository.updateDoctorAvailability(doc.id, isAvailable)
            _uiState.update { it.copy(doctor = doc.copy(isAvailable = isAvailable)) }
        }
    }

    fun updateQueueStatus(status: QueueStatus) {
        val doc = _uiState.value.doctor ?: return
        viewModelScope.launch {
            queueRepository.updateQueueStatus(doc.id, _uiState.value.todayDate, status)
            _uiState.update { it.copy(successMessage = "Queue status updated to ${status.name}") }
        }
    }

    fun resetQueue() {
        val doc = _uiState.value.doctor ?: return
        viewModelScope.launch {
            queueRepository.resetQueue(doc.id, _uiState.value.todayDate)
            _uiState.update { it.copy(successMessage = "Queue reset for today.") }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
