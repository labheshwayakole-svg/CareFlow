package com.example.careflow.domain.usecase

import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.QueueEstimate
import com.example.careflow.domain.model.QueueStatus

class CalculateWaitTimeUseCase {
    operator fun invoke(
        queue: DailyQueue?,
        patientToken: Int,
        doctorName: String,
        avgConsultationMinutes: Int
    ): QueueEstimate {
        if (queue == null) {
            return QueueEstimate(
                doctorName = doctorName,
                date = "",
                currentServingToken = 0,
                patientToken = patientToken,
                tokensAhead = patientToken - 1,
                estimatedWaitMinutes = (patientToken - 1) * avgConsultationMinutes,
                queueStatus = QueueStatus.NOT_STARTED,
                isYourTurn = false
            )
        }

        val currentToken = queue.currentToken
        val tokensAhead = maxOf(0, patientToken - currentToken - 1)
        val isYourTurn = currentToken == patientToken && queue.status == QueueStatus.ACTIVE
        val estimatedWait = if (currentToken >= patientToken) {
            0
        } else {
            tokensAhead * avgConsultationMinutes
        }

        return QueueEstimate(
            doctorName = doctorName,
            date = queue.date,
            currentServingToken = currentToken,
            patientToken = patientToken,
            tokensAhead = tokensAhead,
            estimatedWaitMinutes = estimatedWait,
            queueStatus = queue.status,
            isYourTurn = isYourTurn
        )
    }
}
