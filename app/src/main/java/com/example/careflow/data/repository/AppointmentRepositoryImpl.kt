package com.example.careflow.data.repository

import com.example.careflow.core.dispatcher.DefaultDispatcherProvider
import com.example.careflow.core.dispatcher.DispatcherProvider
import com.example.careflow.core.result.Resource
import com.example.careflow.data.datasource.InMemoryDataStore
import com.example.careflow.data.firebase.FirebaseDataSource
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppointmentRepositoryImpl(
    private val dataStore: InMemoryDataStore,
    private val firebaseDataSource: FirebaseDataSource = FirebaseDataSource(),
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AppointmentRepository {

    override suspend fun bookAppointment(
        patientId: String,
        patientName: String,
        patientPhone: String,
        doctor: Doctor,
        date: String,
        timeSlot: String,
        notes: String
    ): Resource<Appointment> = withContext(dispatchers.io) {
        try {
            // Attempt Firestore transaction first
            try {
                val apt = firebaseDataSource.bookAppointmentAtomic(
                    patientId = patientId,
                    patientName = patientName,
                    patientPhone = patientPhone,
                    doctor = doctor,
                    date = date,
                    timeSlot = timeSlot,
                    notes = notes
                )
                // Mirror to local data store
                dataStore.bookAppointmentAtomic(
                    patientId, patientName, patientPhone, doctor, date, timeSlot, notes
                )
                return@withContext Resource.Success(apt)
            } catch (_: Exception) {
                // Fallback to local atomic memory transaction
                val apt = dataStore.bookAppointmentAtomic(
                    patientId = patientId,
                    patientName = patientName,
                    patientPhone = patientPhone,
                    doctor = doctor,
                    date = date,
                    timeSlot = timeSlot,
                    notes = notes
                )
                Resource.Success(apt)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to book appointment", e)
        }
    }

    override fun observeAppointmentsForPatient(patientId: String): Flow<List<Appointment>> {
        return dataStore.appointments.map { list ->
            list.filter { it.patientId == patientId }
        }
    }

    override fun observeAppointmentsForDoctor(doctorId: String, date: String): Flow<List<Appointment>> {
        return dataStore.appointments.map { list ->
            list.filter { it.doctorId == doctorId && (date.isBlank() || it.date == date) }
        }
    }

    override fun observeAllAppointments(): Flow<List<Appointment>> = dataStore.appointments

    override suspend fun cancelAppointment(appointmentId: String): Resource<Unit> = withContext(dispatchers.io) {
        try {
            firebaseDataSource.updateAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED)
        } catch (_: Exception) {}
        dataStore.cancelAppointment(appointmentId)
        Resource.Success(Unit)
    }

    override suspend fun updateAppointmentStatus(
        appointmentId: String,
        status: AppointmentStatus,
        doctorNotes: String
    ): Resource<Unit> = withContext(dispatchers.io) {
        try {
            firebaseDataSource.updateAppointmentStatus(appointmentId, status, doctorNotes)
        } catch (_: Exception) {}
        dataStore.updateAppointmentStatus(appointmentId, status, doctorNotes)
        Resource.Success(Unit)
    }
}
