package com.example.careflow.data.repository

import com.example.careflow.core.dispatcher.DefaultDispatcherProvider
import com.example.careflow.core.dispatcher.DispatcherProvider
import com.example.careflow.core.result.Resource
import com.example.careflow.data.datasource.InMemoryDataStore
import com.example.careflow.data.firebase.FirebaseDataSource
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.repository.DoctorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class DoctorRepositoryImpl(
    private val dataStore: InMemoryDataStore,
    private val firebaseDataSource: FirebaseDataSource = FirebaseDataSource(),
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : DoctorRepository {

    override fun observeDepartments(hospitalId: String?): Flow<List<Department>> =
        firebaseDataSource.observeDepartments(hospitalId).flowOn(dispatchers.io)

    override fun observeDoctors(
        hospitalId: String?,
        departmentId: String?,
        query: String?
    ): Flow<List<Doctor>> =
        firebaseDataSource.observeDoctors(hospitalId, departmentId, query).flowOn(dispatchers.io)

    override suspend fun getDoctorById(doctorId: String): Resource<Doctor> = withContext(dispatchers.io) {
        try {
            val doc = firebaseDataSource.getDoctorById(doctorId)
            if (doc != null) {
                Resource.Success(doc)
            } else {
                val local = dataStore.doctors.value.find { it.id == doctorId }
                if (local != null) Resource.Success(local) else Resource.Error("Doctor not found.")
            }
        } catch (e: Exception) {
            val local = dataStore.doctors.value.find { it.id == doctorId }
            if (local != null) Resource.Success(local) else Resource.Error(e.message ?: "Failed to get doctor", e)
        }
    }

    override suspend fun getDoctorByUserId(userId: String): Resource<Doctor> = withContext(dispatchers.io) {
        try {
            val doc = firebaseDataSource.getDoctorByUserId(userId)
            if (doc != null) {
                Resource.Success(doc)
            } else {
                val local = dataStore.doctors.value.find { it.userId == userId } ?: dataStore.doctors.value.firstOrNull()
                if (local != null) Resource.Success(local) else Resource.Error("Doctor profile not found.")
            }
        } catch (e: Exception) {
            val local = dataStore.doctors.value.find { it.userId == userId } ?: dataStore.doctors.value.firstOrNull()
            if (local != null) Resource.Success(local) else Resource.Error(e.message ?: "Failed to get doctor", e)
        }
    }

    override suspend fun addDoctor(doctor: Doctor): Resource<Doctor> = withContext(dispatchers.io) {
        try {
            firebaseDataSource.addDoctor(doctor)
            dataStore.addDoctor(doctor)
            Resource.Success(doctor)
        } catch (e: Exception) {
            dataStore.addDoctor(doctor)
            Resource.Success(doctor)
        }
    }

    override suspend fun updateDoctorAvailability(doctorId: String, isAvailable: Boolean): Resource<Unit> = withContext(dispatchers.io) {
        try {
            firebaseDataSource.updateDoctorAvailability(doctorId, isAvailable)
            dataStore.updateDoctorAvailability(doctorId, isAvailable)
            Resource.Success(Unit)
        } catch (e: Exception) {
            dataStore.updateDoctorAvailability(doctorId, isAvailable)
            Resource.Success(Unit)
        }
    }

    override suspend fun updateDoctorOpdStatus(doctorId: String, isInOPD: Boolean): Resource<Unit> = withContext(dispatchers.io) {
        try {
            firebaseDataSource.updateDoctorOpdStatus(doctorId, isInOPD)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update OPD status", e)
        }
    }

    override suspend fun updateDoctorBreakStatus(doctorId: String, isOnBreak: Boolean): Resource<Unit> = withContext(dispatchers.io) {
        try {
            firebaseDataSource.updateDoctorBreakStatus(doctorId, isOnBreak)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update break status", e)
        }
    }

    override suspend fun addDepartment(department: Department): Resource<Department> = withContext(dispatchers.io) {
        try {
            firebaseDataSource.addDepartment(department)
            dataStore.addDepartment(department)
            Resource.Success(department)
        } catch (e: Exception) {
            dataStore.addDepartment(department)
            Resource.Success(department)
        }
    }
}
