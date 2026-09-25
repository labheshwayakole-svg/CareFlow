package com.example.careflow.data.repository

import com.example.careflow.core.dispatcher.DispatcherProvider
import com.example.careflow.core.result.Resource
import com.example.careflow.data.firebase.FirebaseDataSource
import com.example.careflow.domain.model.Hospital
import com.example.careflow.domain.repository.HospitalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class HospitalRepositoryImpl(
    private val firebaseDataSource: FirebaseDataSource,
    private val dispatchers: DispatcherProvider
) : HospitalRepository {

    override fun observeHospitals(): Flow<List<Hospital>> =
        firebaseDataSource.observeHospitals().flowOn(dispatchers.io)

    override fun observeHospitalById(hospitalId: String): Flow<Hospital?> =
        firebaseDataSource.observeHospitalById(hospitalId).flowOn(dispatchers.io)

    override suspend fun getHospitalById(hospitalId: String): Resource<Hospital> =
        withContext(dispatchers.io) {
            try {
                val hospital = firebaseDataSource.getHospitalById(hospitalId)
                if (hospital != null) {
                    Resource.Success(hospital)
                } else {
                    Resource.Error("Hospital not found")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to get hospital", e)
            }
        }

    override suspend fun getHospitalByAdminId(adminId: String): Resource<Hospital?> =
        withContext(dispatchers.io) {
            try {
                val hospital = firebaseDataSource.getHospitalByAdminId(adminId)
                Resource.Success(hospital)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to get hospital by admin", e)
            }
        }

    override suspend fun registerHospital(hospital: Hospital): Resource<Hospital> =
        withContext(dispatchers.io) {
            try {
                firebaseDataSource.saveHospital(hospital)
                Resource.Success(hospital)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to register hospital", e)
            }
        }

    override suspend fun updateHospital(hospital: Hospital): Resource<Unit> =
        withContext(dispatchers.io) {
            try {
                firebaseDataSource.saveHospital(hospital)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to update hospital", e)
            }
        }
}
