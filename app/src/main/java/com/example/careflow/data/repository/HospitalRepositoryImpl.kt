package com.example.careflow.data.repository

import com.example.careflow.core.dispatcher.DefaultDispatcherProvider
import com.example.careflow.core.dispatcher.DispatcherProvider
import com.example.careflow.core.result.Resource
import com.example.careflow.data.datasource.InMemoryDataStore
import com.example.careflow.data.firebase.FirebaseDataSource
import com.example.careflow.domain.model.Hospital
import com.example.careflow.domain.repository.HospitalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class HospitalRepositoryImpl(
    private val dataStore: InMemoryDataStore? = null,
    private val firebaseDataSource: FirebaseDataSource = FirebaseDataSource(),
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : HospitalRepository {

    override fun observeHospitals(): Flow<List<Hospital>> = flow {
        val local = dataStore?.hospitals?.value ?: emptyList()
        if (local.isNotEmpty()) {
            emit(local)
        }
        try {
            firebaseDataSource.observeHospitals().collect { fbList ->
                if (fbList.isNotEmpty()) {
                    fbList.forEach { dataStore?.addHospital(it) }
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

    override fun observeHospitalById(hospitalId: String): Flow<Hospital?> = flow {
        val local = dataStore?.hospitals?.value?.find { it.id == hospitalId }
        if (local != null) emit(local)
        try {
            firebaseDataSource.observeHospitalById(hospitalId).collect { fbHosp ->
                if (fbHosp != null) {
                    dataStore?.addHospital(fbHosp)
                    emit(fbHosp)
                } else if (local != null) {
                    emit(local)
                } else {
                    emit(null)
                }
            }
        } catch (_: Exception) {
            if (local != null) emit(local)
        }
    }.flowOn(dispatchers.io)

    override suspend fun getHospitalById(hospitalId: String): Resource<Hospital> =
        withContext(dispatchers.io) {
            try {
                val hospital = firebaseDataSource.getHospitalById(hospitalId)
                if (hospital != null) {
                    dataStore?.addHospital(hospital)
                    Resource.Success(hospital)
                } else {
                    val local = dataStore?.hospitals?.value?.find { it.id == hospitalId }
                    if (local != null) Resource.Success(local) else Resource.Error("Hospital not found")
                }
            } catch (e: Exception) {
                val local = dataStore?.hospitals?.value?.find { it.id == hospitalId }
                if (local != null) Resource.Success(local) else Resource.Error(e.message ?: "Failed to get hospital", e)
            }
        }

    override suspend fun getHospitalByAdminId(adminId: String): Resource<Hospital?> =
        withContext(dispatchers.io) {
            try {
                var hospital = if (adminId.isNotBlank()) firebaseDataSource.getHospitalByAdminId(adminId) else null
                if (hospital == null) {
                    val all = firebaseDataSource.getHospitals()
                    hospital = all.find { it.adminId == adminId }
                        ?: all.find { it.id == "hosp_main" || it.name.contains("CareFlow", ignoreCase = true) }
                        ?: all.firstOrNull()
                }
                if (hospital != null) {
                    dataStore?.addHospital(hospital)
                    Resource.Success(hospital)
                } else {
                    val local = dataStore?.hospitals?.value?.find { it.adminId == adminId }
                        ?: dataStore?.hospitals?.value?.find { it.id == "hosp_main" || it.name.contains("CareFlow", ignoreCase = true) }
                        ?: dataStore?.hospitals?.value?.firstOrNull()
                    Resource.Success(local)
                }
            } catch (e: Exception) {
                val local = dataStore?.hospitals?.value?.find { it.adminId == adminId }
                    ?: dataStore?.hospitals?.value?.find { it.id == "hosp_main" || it.name.contains("CareFlow", ignoreCase = true) }
                    ?: dataStore?.hospitals?.value?.firstOrNull()
                Resource.Success(local)
            }
        }

    override suspend fun registerHospital(hospital: Hospital): Resource<Hospital> =
        withContext(dispatchers.io) {
            try {
                firebaseDataSource.saveHospital(hospital)
                dataStore?.addHospital(hospital)
                Resource.Success(hospital)
            } catch (e: Exception) {
                dataStore?.addHospital(hospital)
                Resource.Success(hospital)
            }
        }

    override suspend fun updateHospital(hospital: Hospital): Resource<Unit> =
        withContext(dispatchers.io) {
            try {
                firebaseDataSource.saveHospital(hospital)
                dataStore?.addHospital(hospital)
                Resource.Success(Unit)
            } catch (e: Exception) {
                dataStore?.addHospital(hospital)
                Resource.Success(Unit)
            }
        }
}
