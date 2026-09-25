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
import com.example.careflow.domain.model.Notification
import com.example.careflow.domain.model.NotificationType
import com.example.careflow.domain.model.QueueStatus
import com.example.careflow.domain.repository.AppointmentRepository
import com.example.careflow.domain.repository.AuthRepository
import com.example.careflow.domain.repository.DoctorRepository
import com.example.careflow.domain.repository.NotificationRepository
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
import java.util.UUID

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
    private val notificationRepository: NotificationRepository = ServiceLocator.notificationRepository,
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
                        it.tokenNumber == (queue?.currentToken ?: 0) &&
                        (it.status == AppointmentStatus.IN_CONSULTATION || it.status == AppointmentStatus.CALLED)
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
                        it.tokenNumber == (state.todayQueue?.currentToken ?: 0) &&
                        (it.status == AppointmentStatus.IN_CONSULTATION || it.status == AppointmentStatus.CALLED)
                    }
                    state.copy(todayAppointments = apts, currentServingAppointment = currentServing)
                }
            }
        }
    }

    fun toggleOpdPresence(isInOPD: Boolean) {
        val doc = _uiState.value.doctor ?: return
        viewModelScope.launch {
            doctorRepository.updateDoctorOpdStatus(doc.id, isInOPD)
            _uiState.update { it.copy(doctor = doc.copy(isInOPD = isInOPD, isAvailable = isInOPD)) }

            // Notify all today's booked patients
            val title = if (isInOPD) "Dr. ${doc.name} is now in OPD" else "Dr. ${doc.name} has left OPD"
            val body = if (isInOPD) "Consultations have started. Please check your queue position." else "Doctor has concluded their OPD shift for now."
            notifyTodayPatients(doc, title, body, if (isInOPD) NotificationType.DOCTOR_AVAILABLE else NotificationType.DOCTOR_LEFT_OPD)

            _uiState.update {
                it.copy(successMessage = if (isInOPD) "Marked as In OPD. Consultations active." else "Marked as Left OPD.")
            }
        }
    }

    fun toggleBreakMode(isOnBreak: Boolean, message: String = "Doctor is taking a short 15-minute break.") {
        val doc = _uiState.value.doctor ?: return
        viewModelScope.launch {
            doctorRepository.updateDoctorBreakStatus(doc.id, isOnBreak)
            queueRepository.updateQueueStatus(
                doctorId = doc.id,
                date = _uiState.value.todayDate,
                status = if (isOnBreak) QueueStatus.BREAK else QueueStatus.ACTIVE,
                breakMessage = if (isOnBreak) message else null
            )
            _uiState.update { it.copy(doctor = doc.copy(isOnBreak = isOnBreak)) }

            // Notify patients
            val title = if (isOnBreak) "Doctor on Break" else "Doctor Resumed Consultations"
            val body = if (isOnBreak) "Dr. ${doc.name} is on break: $message" else "Dr. ${doc.name} has resumed consultations. Queue is moving."
            notifyTodayPatients(doc, title, body, if (isOnBreak) NotificationType.DOCTOR_ON_BREAK else NotificationType.DOCTOR_BACK_FROM_BREAK)

            _uiState.update {
                it.copy(successMessage = if (isOnBreak) "Break mode ON. Patients notified." else "Break ended. Queue resumed.")
            }
        }
    }

    fun callNextPatient() {
        val doc = _uiState.value.doctor ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Find next waiting appointment
            val currentToken = _uiState.value.todayQueue?.currentToken ?: 0
            val nextApt = _uiState.value.todayAppointments
                .filter { it.tokenNumber > currentToken && (it.status == AppointmentStatus.BOOKED || it.status == AppointmentStatus.WAITING) }
                .minByOrNull { it.tokenNumber }

            when (val res = advanceQueueUseCase(doc.id, _uiState.value.todayDate)) {
                is Resource.Success -> {
                    val updatedQueue = res.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            todayQueue = updatedQueue,
                            successMessage = "Calling Token #${updatedQueue.currentToken}"
                        )
                    }

                    // Send Real-time notification to the called patient
                    if (nextApt != null) {
                        notificationRepository.sendNotification(
                            Notification(
                                id = "notif_${UUID.randomUUID().toString().take(8)}",
                                userId = nextApt.patientId,
                                title = "It's Your Turn! (Token #${nextApt.tokenNumber})",
                                body = "Dr. ${doc.name} is calling Token #${nextApt.tokenNumber}. Please proceed into the OPD consultation room.",
                                type = NotificationType.QUEUE_CALLED,
                                appointmentId = nextApt.id,
                                doctorId = doc.id,
                                hospitalId = doc.hospitalId
                            )
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

    fun callSpecificPatient(appointment: Appointment) {
        val doc = _uiState.value.doctor ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = queueRepository.callPatient(
                doctorId = doc.id,
                date = _uiState.value.todayDate,
                tokenNumber = appointment.tokenNumber,
                patientId = appointment.patientId
            )

            if (res is Resource.Success) {
                appointmentRepository.updateAppointmentStatus(appointment.id, AppointmentStatus.CALLED)
                notificationRepository.sendNotification(
                    Notification(
                        id = "notif_${UUID.randomUUID().toString().take(8)}",
                        userId = appointment.patientId,
                        title = "It's Your Turn! (Token #${appointment.tokenNumber})",
                        body = "Dr. ${doc.name} is calling Token #${appointment.tokenNumber}. Please step into the OPD room.",
                        type = NotificationType.QUEUE_CALLED,
                        appointmentId = appointment.id,
                        doctorId = doc.id,
                        hospitalId = doc.hospitalId
                    )
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todayQueue = res.data,
                        successMessage = "Called Token #${appointment.tokenNumber} (${appointment.patientName})"
                    )
                }
            } else if (res is Resource.Error) {
                _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
            }
        }
    }

    fun completeCurrentConsultation(notes: String = "") {
        val current = _uiState.value.currentServingAppointment ?: return
        val doc = _uiState.value.doctor ?: return
        viewModelScope.launch {
            queueRepository.markConsultationComplete(
                doctorId = doc.id,
                date = _uiState.value.todayDate,
                appointmentId = current.id,
                doctorNotes = notes
            )
            _uiState.update {
                it.copy(
                    currentServingAppointment = null,
                    successMessage = "Consultation for Token #${current.tokenNumber} completed!"
                )
            }
        }
    }

    fun markCurrentNoShow() {
        val current = _uiState.value.currentServingAppointment ?: return
        val doc = _uiState.value.doctor ?: return
        viewModelScope.launch {
            queueRepository.markNoShow(
                doctorId = doc.id,
                date = _uiState.value.todayDate,
                appointmentId = current.id
            )
            _uiState.update {
                it.copy(
                    currentServingAppointment = null,
                    successMessage = "Patient Token #${current.tokenNumber} marked as Absent / No-Show."
                )
            }
        }
    }

    fun toggleAvailability(isAvailable: Boolean) {
        val doc = _uiState.value.doctor ?: return
        viewModelScope.launch {
            doctorRepository.updateDoctorAvailability(doc.id, isAvailable)
            _uiState.update { it.copy(doctor = doc.copy(isAvailable = isAvailable)) }
        }
    }

    fun resetQueue() {
        val doc = _uiState.value.doctor ?: return
        viewModelScope.launch {
            queueRepository.resetQueue(doc.id, _uiState.value.todayDate)
            _uiState.update { it.copy(successMessage = "Queue reset for today.") }
        }
    }

    private fun notifyTodayPatients(doctor: Doctor, title: String, body: String, type: NotificationType) {
        viewModelScope.launch {
            val patientIds = _uiState.value.todayAppointments.map { it.patientId }.distinct()
            for (pid in patientIds) {
                notificationRepository.sendNotification(
                    Notification(
                        id = "notif_${UUID.randomUUID().toString().take(8)}",
                        userId = pid,
                        title = title,
                        body = body,
                        type = type,
                        doctorId = doctor.id,
                        hospitalId = doctor.hospitalId
                    )
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
