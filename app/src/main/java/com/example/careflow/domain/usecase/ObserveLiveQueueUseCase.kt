package com.example.careflow.domain.usecase

import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.QueueEstimate
import com.example.careflow.domain.repository.QueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveLiveQueueUseCase(
    private val queueRepository: QueueRepository,
    private val calculateWaitTimeUseCase: CalculateWaitTimeUseCase = CalculateWaitTimeUseCase()
) {
    operator fun invoke(
        doctorId: String,
        doctorName: String,
        date: String,
        patientToken: Int,
        avgConsultationMinutes: Int
    ): Flow<QueueEstimate> {
        return queueRepository.observeLiveQueue(doctorId, date).map { queue: DailyQueue? ->
            calculateWaitTimeUseCase(
                queue = queue,
                patientToken = patientToken,
                doctorName = doctorName,
                avgConsultationMinutes = avgConsultationMinutes
            )
        }
    }
}
