package com.example.careflow.presentation.patient

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careflow.core.di.ServiceLocator
import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.model.Hospital
import com.example.careflow.domain.model.Notification
import com.example.careflow.domain.model.NotificationType
import com.example.careflow.domain.model.QueueEstimate
import com.example.careflow.domain.model.User
import com.example.careflow.domain.repository.AppointmentRepository
import com.example.careflow.domain.repository.AuthRepository
import com.example.careflow.domain.repository.DoctorRepository
import com.example.careflow.domain.repository.HospitalRepository
import com.example.careflow.domain.repository.NotificationRepository
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
import java.util.UUID

@Immutable
data class PatientUiState(
    val currentUser: User? = null,
    val hospitals: List<Hospital> = emptyList(),
    val selectedHospital: Hospital? = null,
    val departments: List<Department> = emptyList(),
    val doctors: List<Doctor> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val selectedDepartmentId: String? = null,
    val filterOnlyInOpd: Boolean = false,
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
    private val hospitalRepository: HospitalRepository = ServiceLocator.hospitalRepository,
    private val doctorRepository: DoctorRepository = ServiceLocator.doctorRepository,
    private val appointmentRepository: AppointmentRepository = ServiceLocator.appointmentRepository,
    private val notificationRepository: NotificationRepository = ServiceLocator.notificationRepository,
    private val bookAppointmentUseCase: BookAppointmentUseCase = ServiceLocator.bookAppointmentUseCase,
    private val observeLiveQueueUseCase: ObserveLiveQueueUseCase = ServiceLocator.observeLiveQueueUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientUiState())
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()

    private var queueTrackingJob: Job? = null
    private var doctorsJob: Job? = null
    private var departmentsJob: Job? = null
    private var notificationsJob: Job? = null

    init {
        observeCurrentUser()
        observeHospitals()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (user != null) {
                    observeMyAppointments(user.id)
                    observeNotifications(user.id)
                    // If user has a selected hospital, match it
                    val hosp = _uiState.value.hospitals.find { it.id == user.selectedHospitalId }
                        ?: _uiState.value.hospitals.firstOrNull()
                    if (hosp != null && _uiState.value.selectedHospital == null) {
                        selectHospital(hosp)
                    }
                }
            }
        }
    }

    private fun observeHospitals() {
        viewModelScope.launch {
            hospitalRepository.observeHospitals().collect { hospList ->
                _uiState.update { it.copy(hospitals = hospList) }
                if (_uiState.value.selectedHospital == null && hospList.isNotEmpty()) {
                    val userHospId = _uiState.value.currentUser?.selectedHospitalId
                    val matched = hospList.find { it.id == userHospId } ?: hospList.first()
                    selectHospital(matched)
                }
            }
        }
    }

    fun selectHospital(hospital: Hospital) {
        _uiState.update {
            it.copy(
                selectedHospital = hospital,
                selectedDepartmentId = null
            )
        }
        viewModelScope.launch {
            authRepository.updateSelectedHospital(hospital.id)
        }
        observeHospitalDepartments(hospital.id)
        observeHospitalDoctors(hospital.id)
    }

    private fun observeHospitalDepartments(hospitalId: String) {
        departmentsJob?.cancel()
        departmentsJob = viewModelScope.launch {
            doctorRepository.observeDepartments(hospitalId = hospitalId).collect { depts ->
                _uiState.update { it.copy(departments = depts) }
            }
        }
    }

    private fun observeHospitalDoctors(hospitalId: String) {
        doctorsJob?.cancel()
        doctorsJob = viewModelScope.launch {
            doctorRepository.observeDoctors(
                hospitalId = hospitalId,
                departmentId = _uiState.value.selectedDepartmentId,
                query = _uiState.value.searchQuery
            ).collect { docs ->
                val filtered = if (_uiState.value.filterOnlyInOpd) {
                    docs.filter { it.isInOPD }
                } else {
                    docs
                }
                _uiState.update { it.copy(doctors = filtered) }
            }
        }
    }

    private fun observeMyAppointments(patientId: String) {
        viewModelScope.launch {
            appointmentRepository.observeAppointmentsForPatient(patientId).collect { apts ->
                _uiState.update { it.copy(myAppointments = apts) }

                // Check 1-hour and 30-minute reminder triggers
                checkAppointmentReminders(apts)

                // If currently tracking an appointment, update estimate
                val activeTracking = _uiState.value.activeTrackingAppointment
                if (activeTracking != null) {
                    val refreshed = apts.find { it.id == activeTracking.id }
                    if (refreshed != null) {
                        _uiState.update { it.copy(activeTrackingAppointment = refreshed) }
                    }
                } else {
                    val activeApt = apts.find { it.status.name == "BOOKED" || it.status.name == "WAITING" || it.status.name == "CALLED" || it.status.name == "IN_CONSULTATION" }
                    if (activeApt != null) {
                        trackAppointmentQueue(activeApt)
                    }
                }
            }
        }
    }

    private fun observeNotifications(patientId: String) {
        notificationsJob?.cancel()
        notificationsJob = viewModelScope.launch {
            notificationRepository.observeNotifications(patientId).collect { notifs ->
                _uiState.update { it.copy(notifications = notifs) }
            }
        }
    }

    private fun checkAppointmentReminders(appointments: List<Appointment>) {
        val now = System.currentTimeMillis()
        val user = _uiState.value.currentUser ?: return

        for (apt in appointments) {
            if (apt.status != com.example.careflow.domain.model.AppointmentStatus.BOOKED) continue

            // Parse appointment slot time for today
            try {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                if (apt.date == todayStr) {
                    val timePart = apt.timeSlot.substringBefore(" - ").trim()
                    val format = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                    val slotTime = format.parse("$todayStr $timePart")?.time ?: continue
                    val diffMinutes = (slotTime - now) / (60 * 1000)

                    // 1-hour reminder (30..65 minutes)
                    if (diffMinutes in 31..65 && !apt.reminder1hrSent) {
                        viewModelScope.launch {
                            appointmentRepository.markReminderSent(apt.id, is1Hr = true)
                            notificationRepository.sendNotification(
                                Notification(
                                    id = "rem1hr_${UUID.randomUUID().toString().take(8)}",
                                    userId = user.id,
                                    title = "Appointment in 1 Hour",
                                    body = "Your consultation with Dr. ${apt.doctorName} (Token #${apt.tokenNumber}) is scheduled in 1 hour. Please head toward OPD.",
                                    type = NotificationType.APPOINTMENT_REMINDER_1HR,
                                    appointmentId = apt.id,
                                    doctorId = apt.doctorId
                                )
                            )
                        }
                    }

                    // 30-min reminder (0..30 minutes)
                    if (diffMinutes in 0..30 && !apt.reminder30minSent) {
                        viewModelScope.launch {
                            appointmentRepository.markReminderSent(apt.id, is1Hr = false)
                            notificationRepository.sendNotification(
                                Notification(
                                    id = "rem30m_${UUID.randomUUID().toString().take(8)}",
                                    userId = user.id,
                                    title = "Appointment in 30 Minutes!",
                                    body = "Your consultation with Dr. ${apt.doctorName} (Token #${apt.tokenNumber}) is coming up in 30 minutes. Please report to the waiting corridor.",
                                    type = NotificationType.APPOINTMENT_REMINDER_30MIN,
                                    appointmentId = apt.id,
                                    doctorId = apt.doctorId
                                )
                            )
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun selectDepartment(deptId: String?) {
        val newDeptId = if (_uiState.value.selectedDepartmentId == deptId) null else deptId
        _uiState.update { it.copy(selectedDepartmentId = newDeptId) }
        refreshDoctors()
    }

    fun toggleFilterOnlyInOpd() {
        _uiState.update { it.copy(filterOnlyInOpd = !it.filterOnlyInOpd) }
        refreshDoctors()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshDoctors()
    }

    private fun refreshDoctors() {
        val hospId = _uiState.value.selectedHospital?.id
        doctorsJob?.cancel()
        doctorsJob = viewModelScope.launch {
            doctorRepository.observeDoctors(
                hospitalId = hospId,
                departmentId = _uiState.value.selectedDepartmentId,
                query = _uiState.value.searchQuery
            ).collect { docs ->
                val filtered = if (_uiState.value.filterOnlyInOpd) {
                    docs.filter { it.isInOPD }
                } else {
                    docs
                }
                _uiState.update { it.copy(doctors = filtered) }
            }
        }
    }

    fun selectDoctor(doctor: Doctor) {
        _uiState.update { it.copy(selectedDoctor = doctor, lastBookedAppointment = null) }
    }

    fun bookAppointment(
        date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        timeSlot: String = "10:00 AM",
        notes: String = "",
        symptoms: String = notes
    ) {
        val user = _uiState.value.currentUser ?: return
        val doctor = _uiState.value.selectedDoctor ?: return
        val hospId = _uiState.value.selectedHospital?.id ?: doctor.hospitalId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val res = bookAppointmentUseCase(
                patientId = user.id,
                patientName = user.name,
                patientPhone = user.phone,
                doctor = doctor,
                hospitalId = hospId,
                date = date,
                timeSlot = timeSlot,
                notes = notes,
                symptoms = symptoms
            )
            when (res) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lastBookedAppointment = res.data,
                            successMessage = "Appointment confirmed! Your Live Token is #${res.data.tokenNumber}"
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

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllNotificationsAsRead() {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            notificationRepository.markAllAsRead(user.id)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
