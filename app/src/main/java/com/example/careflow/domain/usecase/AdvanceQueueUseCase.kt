package com.example.careflow.domain.usecase

import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.repository.AppointmentRepository
import com.example.careflow.domain.repository.QueueRepository
import kotlinx.coroutines.flow.firstOrNull

class AdvanceQueueUseCase(
    private val queueRepository: QueueRepository,
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(
        doctorId: String,
        date: String
    ): Resource<DailyQueue> {
        val result = queueRepository.advanceQueue(doctorId, date)
        if (result is Resource.Success) {
            val updatedQueue = result.data
            // Sync appointment statuses for doctor
            val appointments = appointmentRepository.observeAppointmentsForDoctor(doctorId, date).firstOrNull() ?: emptyList()
            for (apt in appointments) {
                when {
                    apt.tokenNumber < updatedQueue.currentToken && apt.status == AppointmentStatus.IN_CONSULTATION -> {
                        appointmentRepository.updateAppointmentStatus(apt.id, AppointmentStatus.COMPLETED)
                    }
                    apt.tokenNumber == updatedQueue.currentToken && apt.status == AppointmentStatus.BOOKED -> {
                        appointmentRepository.updateAppointmentStatus(apt.id, AppointmentStatus.IN_CONSULTATION)
                    }
                }
            }
        }
        return result
    }
}
