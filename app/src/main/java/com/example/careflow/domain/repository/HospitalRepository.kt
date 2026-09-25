package com.example.careflow.domain.repository

import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.Hospital
import kotlinx.coroutines.flow.Flow

interface HospitalRepository {
    fun observeHospitals(): Flow<List<Hospital>>
    fun observeHospitalById(hospitalId: String): Flow<Hospital?>
    suspend fun getHospitalById(hospitalId: String): Resource<Hospital>
    suspend fun getHospitalByAdminId(adminId: String): Resource<Hospital?>
    suspend fun registerHospital(hospital: Hospital): Resource<Hospital>
    suspend fun updateHospital(hospital: Hospital): Resource<Unit>
}
