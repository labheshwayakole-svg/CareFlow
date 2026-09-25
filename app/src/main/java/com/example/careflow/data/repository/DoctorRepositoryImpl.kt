package com.example.careflow.data.repository

import com.example.careflow.core.dispatcher.DefaultDispatcherProvider
import com.example.careflow.core.dispatcher.DispatcherProvider
import com.example.careflow.core.result.Resource
import com.example.careflow.data.datasource.InMemoryDataStore
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.repository.DoctorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DoctorRepositoryImpl(
    private val dataStore: InMemoryDataStore,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : DoctorRepository {

    override fun observeDepartments(): Flow<List<Department>> = dataStore.departments

    override fun observeDoctors(departmentId: String?, query: String?): Flow<List<Doctor>> {
        return dataStore.doctors.map { list ->
            list.filter { doc ->
                val matchesDept = departmentId.isNullOrBlank() || doc.departmentId == departmentId
                val matchesQuery = query.isNullOrBlank() ||
                        doc.name.contains(query, ignoreCase = true) ||
                        doc.specialization.contains(query, ignoreCase = true) ||
                        doc.departmentName.contains(query, ignoreCase = true)
                matchesDept && matchesQuery
            }
        }
    }

    override suspend fun getDoctorById(doctorId: String): Resource<Doctor> = withContext(dispatchers.io) {
        val doctor = dataStore.doctors.value.find { it.id == doctorId }
        if (doctor != null) {
            Resource.Success(doctor)
        } else {
            Resource.Error("Doctor not found.")
        }
    }

    override suspend fun getDoctorByUserId(userId: String): Resource<Doctor> = withContext(dispatchers.io) {
        val doctor = dataStore.doctors.value.find { it.userId == userId }
        if (doctor != null) {
            Resource.Success(doctor)
        } else {
            // Fallback for demo testing
            val first = dataStore.doctors.value.firstOrNull()
            if (first != null) Resource.Success(first) else Resource.Error("Doctor profile not found.")
        }
    }

    override suspend fun addDoctor(doctor: Doctor): Resource<Doctor> = withContext(dispatchers.io) {
        dataStore.addDoctor(doctor)
        Resource.Success(doctor)
    }

    override suspend fun updateDoctorAvailability(doctorId: String, isAvailable: Boolean): Resource<Unit> = withContext(dispatchers.io) {
        dataStore.updateDoctorAvailability(doctorId, isAvailable)
        Resource.Success(Unit)
    }

    override suspend fun addDepartment(department: Department): Resource<Department> = withContext(dispatchers.io) {
        dataStore.addDepartment(department)
        Resource.Success(department)
    }
}
