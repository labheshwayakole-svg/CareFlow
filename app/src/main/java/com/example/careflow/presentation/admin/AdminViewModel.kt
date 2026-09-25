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
import com.example.careflow.domain.model.Hospital
import com.example.careflow.domain.model.User
import com.example.careflow.domain.model.UserRole
import com.example.careflow.domain.repository.AppointmentRepository
import com.example.careflow.domain.repository.AuthRepository
import com.example.careflow.domain.repository.DoctorRepository
import com.example.careflow.domain.repository.HospitalRepository
import com.example.careflow.domain.repository.QueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@Immutable
data class GeneratedDoctorCredentials(
    val doctorName: String,
    val loginEmail: String,
    val temporaryPassword: String
)

@Immutable
data class AdminUiState(
    val currentAdmin: User? = null,
    val hospital: Hospital? = null,
    val doctors: List<Doctor> = emptyList(),
    val departments: List<Department> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    val activeQueues: List<DailyQueue> = emptyList(),
    val generatedCredentials: GeneratedDoctorCredentials? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AdminViewModel(
    private val authRepository: AuthRepository = ServiceLocator.authRepository,
    private val hospitalRepository: HospitalRepository = ServiceLocator.hospitalRepository,
    private val doctorRepository: DoctorRepository = ServiceLocator.doctorRepository,
    private val appointmentRepository: AppointmentRepository = ServiceLocator.appointmentRepository,
    private val queueRepository: QueueRepository = ServiceLocator.queueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadAdminAndHospital()
    }

    private fun loadAdminAndHospital() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                _uiState.update { it.copy(currentAdmin = user) }
                if (user != null) {
                    val hospRes = hospitalRepository.getHospitalByAdminId(user.id)
                    val hosp = if (hospRes is Resource.Success) hospRes.data else null
                    _uiState.update { it.copy(hospital = hosp) }
                    if (hosp != null) {
                        observeHospitalData(hosp.id)
                    }
                }
            }
        }
    }

    private fun observeHospitalData(hospitalId: String) {
        viewModelScope.launch {
            doctorRepository.observeDoctors(hospitalId = hospitalId).collect { docs ->
                _uiState.update { it.copy(doctors = docs) }
            }
        }
        viewModelScope.launch {
            doctorRepository.observeDepartments(hospitalId = hospitalId).collect { depts ->
                _uiState.update { it.copy(departments = depts) }
            }
        }
        viewModelScope.launch {
            appointmentRepository.observeAppointmentsForHospital(hospitalId = hospitalId).collect { apts ->
                _uiState.update { it.copy(appointments = apts) }
            }
        }
        viewModelScope.launch {
            queueRepository.observeAllActiveQueues(hospitalId = hospitalId).collect { queues ->
                _uiState.update { it.copy(activeQueues = queues) }
            }
        }
    }

    fun saveHospitalDetails(
        name: String,
        opdName: String,
        address: String,
        city: String,
        phone: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val admin = _uiState.value.currentAdmin
            val currentHosp = _uiState.value.hospital
            val updatedHospital = (currentHosp ?: Hospital(
                id = admin?.hospitalId ?: "hosp_${UUID.randomUUID().toString().take(6)}",
                name = name,
                adminId = admin?.id ?: ""
            )).copy(
                name = name,
                opdName = opdName,
                address = address,
                city = city,
                phone = phone,
                updatedAt = System.currentTimeMillis()
            )

            when (val res = hospitalRepository.registerHospital(updatedHospital)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            hospital = res.data,
                            isLoading = false,
                            successMessage = "${res.data.name} & OPD details saved successfully!"
                        )
                    }
                    observeHospitalData(res.data.id)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleDoctorAvailability(doctorId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            doctorRepository.updateDoctorAvailability(doctorId, !currentStatus)
            _uiState.update { it.copy(successMessage = "Doctor availability updated.") }
        }
    }

    fun addDoctorWithCredentials(
        name: String,
        specialization: String,
        departmentId: String,
        departmentName: String,
        qualification: String,
        experienceYears: Int,
        fee: Double,
        shiftStart: String,
        shiftEnd: String,
        maxTokensPerDay: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val hospId = _uiState.value.hospital?.id ?: "hosp_main"
            val sanitizedName = name.trim().lowercase().replace("dr.", "").replace("dr ", "").trim().replace(" ", ".")
            val loginEmail = "dr.$sanitizedName@careflow.hospital"
            val tempPassword = "Doctor@${(1000..9999).random()}"

            // 1. Create doctor account in auth repository
            val regRes = authRepository.register(
                name = if (name.startsWith("Dr.")) name else "Dr. $name",
                email = loginEmail,
                password = tempPassword,
                phone = "",
                role = UserRole.DOCTOR
            )

            val docUserId = if (regRes is Resource.Success) regRes.data.id else "user_$sanitizedName"

            val newDoctor = Doctor(
                id = "doc_${UUID.randomUUID().toString().take(6)}",
                userId = docUserId,
                hospitalId = hospId,
                name = if (name.startsWith("Dr.")) name else "Dr. $name",
                specialization = specialization,
                departmentId = departmentId,
                departmentName = departmentName,
                qualification = qualification,
                experienceYears = experienceYears,
                consultationFee = fee,
                shiftStartTime = shiftStart,
                shiftEndTime = shiftEnd,
                maxTokensPerDay = maxTokensPerDay,
                loginEmail = loginEmail,
                isAvailable = true,
                isInOPD = false,
                isOnBreak = false
            )

            when (val res = doctorRepository.addDoctor(newDoctor)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generatedCredentials = GeneratedDoctorCredentials(
                                doctorName = res.data.name,
                                loginEmail = loginEmail,
                                temporaryPassword = tempPassword
                            ),
                            successMessage = "Dr. ${res.data.name} added! Login credentials generated."
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

    fun addDepartment(name: String, description: String, iconName: String = "stethoscope") {
        viewModelScope.launch {
            val hospId = _uiState.value.hospital?.id ?: "hosp_main"
            val newDept = Department(
                id = "dept_${name.lowercase().replace(" ", "_").take(8)}",
                name = name,
                description = description,
                hospitalId = hospId,
                iconName = iconName
            )
            doctorRepository.addDepartment(newDept)
            _uiState.update { it.copy(successMessage = "$name department added successfully.") }
        }
    }

    fun dismissCredentialsDialog() {
        _uiState.update { it.copy(generatedCredentials = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
