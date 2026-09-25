package com.example.careflow.domain.usecase

import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.repository.AppointmentRepository

class BookAppointmentUseCase(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(
        patientId: String,
        patientName: String,
        patientPhone: String,
        doctor: Doctor,
        date: String,
        timeSlot: String,
        notes: String
    ): Resource<Appointment> {
        if (patientName.isBlank()) {
            return Resource.Error("Patient name is required.")
        }
        if (patientPhone.isBlank() || patientPhone.length < 10) {
            return Resource.Error("Please enter a valid 10-digit phone number.")
        }
        if (date.isBlank()) {
            return Resource.Error("Please select a date for your appointment.")
        }
        if (timeSlot.isBlank()) {
            return Resource.Error("Please select an available time slot.")
        }
        if (!doctor.isAvailable) {
            return Resource.Error("Dr. ${doctor.name} is currently not accepting bookings.")
        }

        return appointmentRepository.bookAppointment(
            patientId = patientId,
            patientName = patientName,
            patientPhone = patientPhone,
            doctor = doctor,
            date = date,
            timeSlot = timeSlot,
            notes = notes
        )
    }
}
