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
import kotlinx.coroutines.flow.flowOn
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
        hospitalId: String,
        date: String,
        timeSlot: String,
        symptoms: String,
        estimatedTime: String
    ): Resource<Appointment> = withContext(dispatchers.io) {
        try {
            // Attempt Firestore transaction first
            try {
                val apt = firebaseDataSource.bookAppointmentAtomic(
                    patientId = patientId,
                    patientName = patientName,
                    patientPhone = patientPhone,
                    doctor = doctor,
                    hospitalId = hospitalId,
                    date = date,
                    timeSlot = timeSlot,
                    symptoms = symptoms,
                    estimatedTime = estimatedTime
                )
                // Mirror to local data store
                dataStore.bookAppointmentAtomic(
                    patientId, patientName, patientPhone, doctor, date, timeSlot, symptoms
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
                    notes = symptoms
                )
                Resource.Success(apt)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to book appointment", e)
        }
    }

    override fun observeAppointmentsForPatient(patientId: String): Flow<List<Appointment>> =
        firebaseDataSource.observeAppointmentsForPatient(patientId).flowOn(dispatchers.io)

    override fun observeAppointmentsForDoctor(doctorId: String, date: String): Flow<List<Appointment>> =
        firebaseDataSource.observeAppointmentsForDoctor(doctorId, date).flowOn(dispatchers.io)

    override fun observeAppointmentsForHospital(hospitalId: String, date: String?): Flow<List<Appointment>> =
        firebaseDataSource.observeAppointmentsForHospital(hospitalId, date).flowOn(dispatchers.io)

    override fun observeAllAppointments(): Flow<List<Appointment>> =
        firebaseDataSource.observeAllAppointments().flowOn(dispatchers.io)

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

    override suspend fun markReminderSent(appointmentId: String, is1Hr: Boolean): Resource<Unit> = withContext(dispatchers.io) {
        try {
            firebaseDataSource.markReminderSent(appointmentId, is1Hr)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark reminder sent", e)
        }
    }
}
