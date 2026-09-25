package com.example.careflow.domain.usecase

import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.repository.DoctorRepository
import kotlinx.coroutines.flow.Flow

class GetDoctorsUseCase(
    private val doctorRepository: DoctorRepository
) {
    operator fun invoke(departmentId: String? = null, query: String? = null): Flow<List<Doctor>> {
        return doctorRepository.observeDoctors(departmentId = departmentId, query = query)
    }
}
