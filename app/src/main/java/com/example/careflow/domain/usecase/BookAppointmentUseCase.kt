package com.example.careflow.domain.usecase

import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.firstOrNull

class BookAppointmentUseCase(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(
        patientId: String,
        patientName: String,
        patientPhone: String,
        doctor: Doctor,
        hospitalId: String = doctor.hospitalId,
        date: String,
        timeSlot: String,
        notes: String = "",
        symptoms: String = notes,
        estimatedTime: String = ""
    ): Resource<Appointment> {
        val resolvedName = patientName.trim().ifBlank { "Patient" }
        val resolvedPhone = patientPhone.trim()
        val resolvedDate = date.trim().ifBlank {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        }

        if (timeSlot.isBlank()) {
            return Resource.Error("Please select an available time slot.")
        }
        if (!doctor.isAvailable) {
            return Resource.Error("Dr. ${doctor.name} is currently not accepting bookings.")
        }

        // Verify slot is not already taken
        try {
            val existingAppointments = appointmentRepository.observeAppointmentsForDoctor(doctor.id, date).firstOrNull() ?: emptyList()

            val normalizedSlot = timeSlot.trim()
            val alreadyBooked = existingAppointments.any { apt ->
                apt.status != com.example.careflow.domain.model.AppointmentStatus.CANCELLED &&
                apt.status != com.example.careflow.domain.model.AppointmentStatus.NO_SHOW &&
                (apt.timeSlot.equals(normalizedSlot, ignoreCase = true) ||
                 apt.timeSlot.startsWith(normalizedSlot, ignoreCase = true) ||
                 normalizedSlot.startsWith(apt.timeSlot, ignoreCase = true))
            }
            if (alreadyBooked) {
                return Resource.Error("Slot $timeSlot is already booked for Dr. ${doctor.name} on $date. Please select another slot.")
            }
        } catch (_: Exception) {
            // Proceed to atomic repository booking if pre-check fails
        }

        return appointmentRepository.bookAppointment(
            patientId = patientId,
            patientName = resolvedName,
            patientPhone = resolvedPhone,
            doctor = doctor,
            hospitalId = hospitalId.ifBlank { doctor.hospitalId },
            date = resolvedDate,
            timeSlot = timeSlot,
            symptoms = symptoms.ifBlank { notes },
            estimatedTime = estimatedTime
        )
    }
}
