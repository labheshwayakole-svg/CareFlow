package com.example.careflow.data.repository

import com.example.careflow.core.dispatcher.DefaultDispatcherProvider
import com.example.careflow.core.dispatcher.DispatcherProvider
import com.example.careflow.core.result.Resource
import com.example.careflow.data.datasource.InMemoryDataStore
import com.example.careflow.data.firebase.FirebaseDataSource
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.repository.DoctorRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoctorRepositoryImpl(
    private val dataStore: InMemoryDataStore,
    private val firebaseDataSource: FirebaseDataSource = FirebaseDataSource(),
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : DoctorRepository {

    override fun observeDepartments(hospitalId: String?): Flow<List<Department>> = kotlinx.coroutines.flow.flow {
        val local = if (hospitalId.isNullOrBlank()) {
            dataStore.departments.value
        } else {
            dataStore.departments.value.filter { it.hospitalId == hospitalId }
        }
        if (local.isNotEmpty()) emit(local)
        try {
            firebaseDataSource.observeDepartments(hospitalId).collect { fbList ->
                if (fbList.isNotEmpty()) {
                    fbList.forEach { dataStore.addDepartment(it) }
                    emit(fbList)
                } else if (local.isNotEmpty()) {
                    emit(local)
                } else {
                    emit(emptyList())
                }
            }
        } catch (_: Exception) {
            if (local.isNotEmpty()) emit(local)
        }
    }.flowOn(dispatchers.io)

    override fun observeDoctors(
        hospitalId: String?,
        departmentId: String?,
        query: String?
    ): Flow<List<Doctor>> = kotlinx.coroutines.flow.callbackFlow {
        fun filter(list: List<Doctor>): List<Doctor> {
            return list.filter { doc ->
                val matchesHospital = hospitalId.isNullOrBlank() || doc.hospitalId == hospitalId
                val matchesDept = departmentId.isNullOrBlank() || doc.departmentId == departmentId
                val matchesQuery = query.isNullOrBlank() ||
                        doc.name.contains(query, ignoreCase = true) ||
                        doc.specialization.contains(query, ignoreCase = true) ||
                        doc.departmentName.contains(query, ignoreCase = true)
                matchesHospital && matchesDept && matchesQuery
            }
        }

        trySend(filter(dataStore.doctors.value))

        val localJob = launch {
            dataStore.doctors.collect { docs ->
                trySend(filter(docs))
            }
        }

        val fbJob = launch {
            try {
                firebaseDataSource.observeDoctors(hospitalId, departmentId, query).collect { fbList ->
                    if (fbList.isNotEmpty()) {
                        fbList.forEach { dataStore.addDoctor(it) }
                    }
                }
            } catch (_: Exception) {}
        }

        awaitClose {
            localJob.cancel()
            fbJob.cancel()
        }
    }.flowOn(dispatchers.io)

    override fun observeDoctorById(doctorId: String): Flow<Doctor?> = callbackFlow {
        val initialDoc = dataStore.doctors.value.find { it.id == doctorId }
        trySend(initialDoc)

        val localJob = launch {
            dataStore.doctors.collect { list ->
                val match = list.find { it.id == doctorId }
                if (match != null) trySend(match)
            }
        }

        val fbJob = launch {
            try {
                firebaseDataSource.observeDoctorById(doctorId).collect { doc ->
                    if (doc != null) {
                        dataStore.addDoctor(doc)
                    }
                }
            } catch (_: Exception) {}
        }

        awaitClose {
            localJob.cancel()
            fbJob.cancel()
        }
    }.flowOn(dispatchers.io)

    override fun observeDoctorByUserId(userId: String): Flow<Doctor?> = callbackFlow {
        fun findInLocal(list: List<Doctor>): Doctor? {
            return list.find { it.userId == userId || it.id == userId }
                ?: dataStore.currentUser.value?.email?.let { curEmail ->
                    if (curEmail.isNotBlank()) list.find { it.loginEmail.equals(curEmail, ignoreCase = true) } else null
                }
        }

        val initialDoc = findInLocal(dataStore.doctors.value)
        trySend(initialDoc)

        val localJob = launch {
            dataStore.doctors.collect { list ->
                val match = findInLocal(list)
                if (match != null) trySend(match)
            }
        }

        val fbJob = launch {
            try {
                firebaseDataSource.observeDoctorByUserId(userId).collect { doc ->
                    if (doc != null) {
                        dataStore.addDoctor(doc)
                    }
                }
            } catch (_: Exception) {}
        }

        awaitClose {
            localJob.cancel()
            fbJob.cancel()
        }
    }.flowOn(dispatchers.io)

    override suspend fun getDoctorById(doctorId: String): Resource<Doctor> = withContext(dispatchers.io) {
        try {
            val doc = firebaseDataSource.getDoctorById(doctorId)
            if (doc != null) {
                dataStore.addDoctor(doc)
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
                dataStore.addDoctor(doc)
                return@withContext Resource.Success(doc)
            }
        } catch (_: Exception) {}

        // Check local store by userId
        val local = dataStore.doctors.value.find { it.userId == userId }
        if (local != null) return@withContext Resource.Success(local)

        // Check local store by loginEmail matching the current user's email
        val curEmail = dataStore.currentUser.value?.email
        if (!curEmail.isNullOrBlank()) {
            val matchedEmail = dataStore.doctors.value.find { it.loginEmail.equals(curEmail, ignoreCase = true) }
            if (matchedEmail != null) return@withContext Resource.Success(matchedEmail)

            // Also check Firestore for doctors matching loginEmail
            val allFbDocs = firebaseDataSource.getDoctors()
            val fbMatch = allFbDocs.find { it.loginEmail.equals(curEmail, ignoreCase = true) }
            if (fbMatch != null) {
                dataStore.addDoctor(fbMatch)
                return@withContext Resource.Success(fbMatch)
            }
        }

        // If the user used doctor role button and doctors have been created in the hospital
        if (userId == "doctor_user_01" || userId.startsWith("doctor_")) {
            val createdDoctor = dataStore.doctors.value.firstOrNull() ?: firebaseDataSource.getDoctors().firstOrNull()
            if (createdDoctor != null) {
                dataStore.addDoctor(createdDoctor)
                return@withContext Resource.Success(createdDoctor)
            }
        }

        Resource.Error("Doctor profile not found.")
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

    override suspend fun deleteDoctor(doctorId: String): Resource<Unit> = withContext(dispatchers.io) {
        try {
            firebaseDataSource.deleteDoctor(doctorId)
            dataStore.deleteDoctor(doctorId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            dataStore.deleteDoctor(doctorId)
            Resource.Success(Unit)
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

    override suspend fun updateDoctorShift(
        doctorId: String,
        shiftStartTime: String,
        shiftEndTime: String
    ): Resource<Unit> = withContext(dispatchers.io) {
        try {
            firebaseDataSource.updateDoctorShift(doctorId, shiftStartTime, shiftEndTime)
            dataStore.updateDoctorShift(doctorId, shiftStartTime, shiftEndTime)
            Resource.Success(Unit)
        } catch (e: Exception) {
            dataStore.updateDoctorShift(doctorId, shiftStartTime, shiftEndTime)
            Resource.Success(Unit)
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
