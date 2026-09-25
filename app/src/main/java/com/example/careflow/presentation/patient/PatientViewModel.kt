package com.example.careflow.presentation.patient

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careflow.core.di.ServiceLocator
import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.model.QueueEstimate
import com.example.careflow.domain.model.User
import com.example.careflow.domain.repository.AppointmentRepository
import com.example.careflow.domain.repository.AuthRepository
import com.example.careflow.domain.repository.DoctorRepository
import com.example.careflow.domain.usecase.BookAppointmentUseCase
import com.example.careflow.domain.usecase.ObserveLiveQueueUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Immutable
data class PatientUiState(
    val currentUser: User? = null,
    val departments: List<Department> = emptyList(),
    val doctors: List<Doctor> = emptyList(),
    val selectedDepartmentId: String? = null,
    val searchQuery: String = "",
    val myAppointments: List<Appointment> = emptyList(),
    val selectedDoctor: Doctor? = null,
    val activeQueueEstimate: QueueEstimate? = null,
    val activeTrackingAppointment: Appointment? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val lastBookedAppointment: Appointment? = null
)

class PatientViewModel(
    private val authRepository: AuthRepository = ServiceLocator.authRepository,
    private val doctorRepository: DoctorRepository = ServiceLocator.doctorRepository,
    private val appointmentRepository: AppointmentRepository = ServiceLocator.appointmentRepository,
    private val bookAppointmentUseCase: BookAppointmentUseCase = ServiceLocator.bookAppointmentUseCase,
    private val observeLiveQueueUseCase: ObserveLiveQueueUseCase = ServiceLocator.observeLiveQueueUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientUiState())
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()

    private var queueTrackingJob: Job? = null

    init {
        observeCurrentUser()
        observeDepartments()
        observeDoctors()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (user != null) {
                    observeMyAppointments(user.id)
                }
            }
        }
    }

    private fun observeDepartments() {
        viewModelScope.launch {
            doctorRepository.observeDepartments().collect { depts ->
                _uiState.update { it.copy(departments = depts) }
            }
        }
    }

    private fun observeDoctors() {
        viewModelScope.launch {
            doctorRepository.observeDoctors(
                departmentId = _uiState.value.selectedDepartmentId,
                query = _uiState.value.searchQuery
            ).collect { docs ->
                _uiState.update { it.copy(doctors = docs) }
            }
        }
    }

    private fun observeMyAppointments(patientId: String) {
        viewModelScope.launch {
            appointmentRepository.observeAppointmentsForPatient(patientId).collect { apts ->
                _uiState.update { it.copy(myAppointments = apts) }
                // If there's an active appointment, automatically track its queue if none selected
                if (_uiState.value.activeTrackingAppointment == null) {
                    val activeApt = apts.find { it.status.name == "BOOKED" || it.status.name == "IN_CONSULTATION" }
                    if (activeApt != null) {
                        trackAppointmentQueue(activeApt)
                    }
                }
            }
        }
    }

    fun selectDepartment(deptId: String?) {
        val newDeptId = if (_uiState.value.selectedDepartmentId == deptId) null else deptId
        _uiState.update { it.copy(selectedDepartmentId = newDeptId) }
        refreshDoctors()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshDoctors()
    }

    private fun refreshDoctors() {
        viewModelScope.launch {
            doctorRepository.observeDoctors(
                departmentId = _uiState.value.selectedDepartmentId,
                query = _uiState.value.searchQuery
            ).collect { docs ->
                _uiState.update { it.copy(doctors = docs) }
            }
        }
    }

    fun selectDoctor(doctor: Doctor) {
        _uiState.update { it.copy(selectedDoctor = doctor, lastBookedAppointment = null) }
    }

    fun bookAppointment(
        date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        timeSlot: String = "10:00 AM",
        notes: String = ""
    ) {
        val user = _uiState.value.currentUser ?: return
        val doctor = _uiState.value.selectedDoctor ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val res = bookAppointmentUseCase(
                patientId = user.id,
                patientName = user.name,
                patientPhone = user.phone,
                doctor = doctor,
                date = date,
                timeSlot = timeSlot,
                notes = notes
            )
            when (res) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lastBookedAppointment = res.data,
                            successMessage = "Appointment confirmed! Token #${res.data.tokenNumber}"
                        )
                    }
                    trackAppointmentQueue(res.data)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            when (val res = appointmentRepository.cancelAppointment(appointmentId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(successMessage = "Appointment cancelled successfully.") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun trackAppointmentQueue(appointment: Appointment) {
        queueTrackingJob?.cancel()
        _uiState.update { it.copy(activeTrackingAppointment = appointment) }

        queueTrackingJob = viewModelScope.launch {
            observeLiveQueueUseCase(
                doctorId = appointment.doctorId,
                doctorName = appointment.doctorName,
                date = appointment.date,
                patientToken = appointment.tokenNumber,
                avgConsultationMinutes = 15
            ).collect { estimate ->
                _uiState.update { it.copy(activeQueueEstimate = estimate) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
