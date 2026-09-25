package com.example.careflow.presentation.admin

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careflow.core.di.ServiceLocator
import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.repository.AppointmentRepository
import com.example.careflow.domain.repository.DoctorRepository
import com.example.careflow.domain.repository.QueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@Immutable
data class AdminUiState(
    val doctors: List<Doctor> = emptyList(),
    val departments: List<Department> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    val activeQueues: List<DailyQueue> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AdminViewModel(
    private val doctorRepository: DoctorRepository = ServiceLocator.doctorRepository,
    private val appointmentRepository: AppointmentRepository = ServiceLocator.appointmentRepository,
    private val queueRepository: QueueRepository = ServiceLocator.queueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        observeAll()
    }

    private fun observeAll() {
        viewModelScope.launch {
            doctorRepository.observeDoctors().collect { docs ->
                _uiState.update { it.copy(doctors = docs) }
            }
        }
        viewModelScope.launch {
            doctorRepository.observeDepartments().collect { depts ->
                _uiState.update { it.copy(departments = depts) }
            }
        }
        viewModelScope.launch {
            appointmentRepository.observeAllAppointments().collect { apts ->
                _uiState.update { it.copy(appointments = apts) }
            }
        }
        viewModelScope.launch {
            queueRepository.observeAllActiveQueues().collect { queues ->
                _uiState.update { it.copy(activeQueues = queues) }
            }
        }
    }

    fun toggleDoctorAvailability(doctorId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            doctorRepository.updateDoctorAvailability(doctorId, !currentStatus)
            _uiState.update { it.copy(successMessage = "Doctor status updated.") }
        }
    }

    fun addDoctor(
        name: String,
        specialization: String,
        departmentId: String,
        departmentName: String,
        qualification: String,
        fee: Double,
        shiftStart: String,
        shiftEnd: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val newDoctor = Doctor(
                id = "doc_${UUID.randomUUID().toString().take(6)}",
                userId = "user_doc_${UUID.randomUUID().toString().take(6)}",
                name = name,
                specialization = specialization,
                departmentId = departmentId,
                departmentName = departmentName,
                qualification = qualification,
                experienceYears = 5,
                consultationFee = fee,
                shiftStartTime = shiftStart,
                shiftEndTime = shiftEnd
            )
            when (val res = doctorRepository.addDoctor(newDoctor)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "Dr. ${res.data.name} added successfully!")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun addDepartment(name: String, description: String) {
        viewModelScope.launch {
            val newDept = Department(
                id = "dept_${name.lowercase().replace(" ", "_").take(8)}",
                name = name,
                description = description
            )
            doctorRepository.addDepartment(newDept)
            _uiState.update { it.copy(successMessage = "$name department added.") }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
