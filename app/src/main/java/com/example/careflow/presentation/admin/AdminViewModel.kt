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
import com.example.careflow.domain.model.DoctorResetRequest
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
    val doctorResetRequests: List<DoctorResetRequest> = emptyList(),
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
                    val hospRes = if (!user.hospitalId.isNullOrBlank()) {
                        hospitalRepository.getHospitalById(user.hospitalId)
                    } else {
                        val byAdmin = hospitalRepository.getHospitalByAdminId(user.id)
                        if (byAdmin is Resource.Success && byAdmin.data != null) byAdmin
                        else hospitalRepository.getHospitalById("hosp_main")
                    }
                    val hosp = if (hospRes is Resource.Success && hospRes.data != null) hospRes.data else null
                    if (hosp != null) {
                        _uiState.update { it.copy(hospital = hosp) }
                        observeHospitalData(hosp.id)
                    }
                }
            }
        }

        // Continually observe hospitals in real-time from Firestore
        viewModelScope.launch {
            hospitalRepository.observeHospitals().collect { hospList ->
                val currentAdmin = _uiState.value.currentAdmin
                val currentHosp = _uiState.value.hospital
                val adminEmail = currentAdmin?.email?.trim()?.lowercase() ?: ""
                val matching = hospList.find { it.adminEmail.equals(adminEmail, ignoreCase = true) }
                    ?: hospList.find { it.email.equals(adminEmail, ignoreCase = true) }
                    ?: hospList.find { it.adminId == currentAdmin?.id }
                    ?: hospList.find { it.id == currentAdmin?.hospitalId }
                    ?: hospList.find { it.id == "hosp_main" }
                    ?: hospList.firstOrNull()

                if (matching != null && (currentHosp == null || currentHosp.id != matching.id || currentHosp.name != matching.name)) {
                    _uiState.update { it.copy(hospital = matching) }
                    observeHospitalData(matching.id)
                }
            }
        }

        // Continually observe pending doctor reset requests
        viewModelScope.launch {
            authRepository.observeDoctorResetRequests(null).collect { requests ->
                _uiState.update { it.copy(doctorResetRequests = requests) }
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
            val hospId = currentHosp?.id
                ?: admin?.hospitalId?.ifBlank { null }
                ?: "hosp_${admin?.id?.take(6) ?: UUID.randomUUID().toString().take(6)}"

            val updatedHospital = (currentHosp ?: Hospital(
                id = hospId,
                name = name,
                adminId = admin?.id ?: ""
            )).copy(
                name = name,
                opdName = opdName,
                address = address,
                city = city,
                phone = phone,
                adminId = admin?.id ?: currentHosp?.adminId ?: "",
                updatedAt = System.currentTimeMillis()
            )

            // Link admin user to this hospital
            if (admin != null) {
                authRepository.updateHospitalId(updatedHospital.id)
                authRepository.updateSelectedHospital(updatedHospital.id)
            }

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

    fun updateAdminEmail(newEmail: String) {
        val cleanEmail = newEmail.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address (e.g. admin@hospital.com)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val admin = _uiState.value.currentAdmin
            if (admin != null) {
                when (val res = authRepository.updateUserEmail(cleanEmail)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                currentAdmin = it.currentAdmin?.copy(email = cleanEmail),
                                isLoading = false,
                                successMessage = "Admin email updated successfully to $cleanEmail"
                            )
                        }
                        val currentHosp = _uiState.value.hospital
                        if (currentHosp != null) {
                            try {
                                val updatedHosp = currentHosp.copy(
                                    email = cleanEmail,
                                    adminEmail = cleanEmail,
                                    updatedAt = System.currentTimeMillis()
                                )
                                hospitalRepository.registerHospital(updatedHosp)
                                _uiState.update { it.copy(hospital = updatedHosp) }
                            } catch (_: Exception) {}
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = res.message ?: "Failed to update admin email"
                            )
                        }
                    }
                    is Resource.Loading -> {}
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Admin session not found") }
            }
        }
    }

    fun updateAdminAndHospitalProfile(
        adminName: String,
        adminEmail: String,
        adminPhone: String,
        hospitalName: String,
        opdName: String,
        address: String,
        city: String,
        hospitalPhone: String
    ) {
        val cleanName = adminName.trim()
        val cleanEmail = adminEmail.trim().lowercase()
        val cleanPhone = adminPhone.trim()

        if (cleanName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Administrator name cannot be empty.") }
            return
        }
        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address (e.g. admin@careflow.com).") }
            return
        }
        if (hospitalName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Hospital name cannot be empty.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = authRepository.updateAdminAndHospitalProfile(
                adminName = cleanName,
                adminEmail = cleanEmail,
                adminPhone = cleanPhone,
                hospitalName = hospitalName.trim(),
                opdName = opdName.trim(),
                address = address.trim(),
                city = city.trim(),
                hospitalPhone = hospitalPhone.trim()
            )
            when (res) {
                is Resource.Success -> {
                    val updatedAdmin = _uiState.value.currentAdmin?.copy(
                        name = cleanName,
                        email = cleanEmail,
                        phone = cleanPhone
                    )
                    val updatedHosp = _uiState.value.hospital?.copy(
                        name = hospitalName.trim(),
                        opdName = opdName.trim(),
                        address = address.trim(),
                        city = city.trim(),
                        phone = hospitalPhone.trim(),
                        email = cleanEmail,
                        adminEmail = cleanEmail,
                        adminName = cleanName,
                        adminPhone = cleanPhone
                    )
                    _uiState.update {
                        it.copy(
                            currentAdmin = updatedAdmin,
                            hospital = updatedHosp,
                            isLoading = false,
                            successMessage = "Profile & Hospital details updated successfully! Future logins and password resets will use $cleanEmail."
                        )
                    }
                    if (updatedHosp != null) {
                        observeHospitalData(updatedHosp.id)
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = res.message ?: "Failed to update profile."
                        )
                    }
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

    fun removeDoctor(doctorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val res = doctorRepository.deleteDoctor(doctorId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Doctor removed successfully from hospital."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = res.message ?: "Failed to remove doctor"
                        )
                    }
                }
                is Resource.Loading -> {}
            }
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

            val hospId = _uiState.value.hospital?.id ?: _uiState.value.currentAdmin?.hospitalId ?: "hosp_main"
            val hospName = _uiState.value.hospital?.name?.ifBlank { "CareFlow Multi-Specialty Hospital" } ?: "CareFlow Multi-Specialty Hospital"

            if (_uiState.value.hospital == null) {
                val admin = _uiState.value.currentAdmin
                val defaultHosp = Hospital(
                    id = hospId,
                    name = hospName,
                    opdName = "Main Outpatient Block",
                    address = "Hospital Campus",
                    city = "City",
                    phone = "",
                    adminId = admin?.id ?: ""
                )
                hospitalRepository.registerHospital(defaultHosp)
                _uiState.update { it.copy(hospital = defaultHosp) }
            }

            val sanitizedName = name.trim().lowercase().replace("dr.", "").replace("dr ", "").trim().replace(" ", ".")
            val loginEmail = "dr.$sanitizedName@careflow.hospital"
            val tempPassword = "Doctor@${(1000..9999).random()}"
            val docUserId = "user_doc_${UUID.randomUUID().toString().take(6)}"

            // Safe user registration: DO NOT replace admin's FirebaseAuth session
            val doctorUser = User(
                id = docUserId,
                name = if (name.startsWith("Dr.")) name else "Dr. $name",
                email = loginEmail,
                phone = "",
                role = UserRole.DOCTOR,
                hospitalId = hospId
            )
            ServiceLocator.inMemoryDataStore.registerUser(doctorUser)
            try {
                com.example.careflow.data.firebase.FirebaseDataSource().saveUserProfile(doctorUser)
            } catch (_: Exception) {}

            val newDoctor = Doctor(
                id = "doc_${UUID.randomUUID().toString().take(6)}",
                userId = docUserId,
                hospitalId = hospId,
                hospitalName = hospName,
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
                password = tempPassword,
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

    fun dismissDoctorResetRequest(requestId: String) {
        viewModelScope.launch {
            authRepository.dismissDoctorResetRequest(requestId)
        }
    }

    fun updateDoctorShift(doctorId: String, doctorName: String, shiftStartTime: String, shiftEndTime: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val updatedDocs = state.doctors.map { d ->
                    if (d.id == doctorId) d.copy(shiftStartTime = shiftStartTime, shiftEndTime = shiftEndTime) else d
                }
                state.copy(isLoading = true, doctors = updatedDocs)
            }
            when (val res = doctorRepository.updateDoctorShift(doctorId, shiftStartTime, shiftEndTime)) {
                is Resource.Success -> {
                    val formatted = com.example.careflow.domain.util.BookingTimeUtils.formatDisplayShift(shiftStartTime, shiftEndTime)
                    _uiState.update { state ->
                        val updatedDocs = state.doctors.map { d ->
                            if (d.id == doctorId) d.copy(shiftStartTime = shiftStartTime, shiftEndTime = shiftEndTime) else d
                        }
                        state.copy(
                            isLoading = false,
                            doctors = updatedDocs,
                            successMessage = "Shift updated for $doctorName: $formatted"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message ?: "Failed to update shift") }
                }
                is Resource.Loading -> {}
            }
        }
    }
}
