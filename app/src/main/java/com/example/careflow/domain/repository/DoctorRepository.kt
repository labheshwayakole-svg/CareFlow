package com.example.careflow.domain.repository

import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
import kotlinx.coroutines.flow.Flow

interface DoctorRepository {
    fun observeDepartments(hospitalId: String? = null): Flow<List<Department>>
    fun observeDoctors(hospitalId: String? = null, departmentId: String? = null, query: String? = null): Flow<List<Doctor>>
    suspend fun getDoctorById(doctorId: String): Resource<Doctor>
    suspend fun getDoctorByUserId(userId: String): Resource<Doctor>
    suspend fun addDoctor(doctor: Doctor): Resource<Doctor>
    suspend fun deleteDoctor(doctorId: String): Resource<Unit>
    suspend fun updateDoctorAvailability(doctorId: String, isAvailable: Boolean): Resource<Unit>
    suspend fun updateDoctorOpdStatus(doctorId: String, isInOPD: Boolean): Resource<Unit>
    suspend fun updateDoctorBreakStatus(doctorId: String, isOnBreak: Boolean): Resource<Unit>
    suspend fun addDepartment(department: Department): Resource<Department>
}
