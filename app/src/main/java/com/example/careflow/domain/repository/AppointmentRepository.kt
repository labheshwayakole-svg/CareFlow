package com.example.careflow.domain.repository

import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.Doctor
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    suspend fun bookAppointment(
        patientId: String,
        patientName: String,
        patientPhone: String,
        doctor: Doctor,
        date: String,
        timeSlot: String,
        notes: String
    ): Resource<Appointment>

    fun observeAppointmentsForPatient(patientId: String): Flow<List<Appointment>>
    fun observeAppointmentsForDoctor(doctorId: String, date: String): Flow<List<Appointment>>
    fun observeAllAppointments(): Flow<List<Appointment>>
    suspend fun cancelAppointment(appointmentId: String): Resource<Unit>
    suspend fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus, doctorNotes: String = ""): Resource<Unit>
}
