package com.example.careflow.domain.usecase

import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.QueueStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateWaitTimeUseCaseTest {

    private val calculateWaitTimeUseCase = CalculateWaitTimeUseCase()

    @Test
    fun calculateWaitTime_withTokensAhead_returnsAccurateMinutes() {
        // Given current token 4, patient token 7, 15 min avg consultation
        val queue = DailyQueue(
            queueId = "doc_01_2026-09-25",
            doctorId = "doc_01",
            doctorName = "Dr. Sharma",
            date = "2026-09-25",
            currentToken = 4,
            totalTokensIssued = 10,
            status = QueueStatus.ACTIVE
        )

        // When
        val estimate = calculateWaitTimeUseCase(
            queue = queue,
            patientToken = 7,
            doctorName = "Dr. Sharma",
            avgConsultationMinutes = 15
        )

        // Then: Tokens ahead should be 7 - 4 - 1 = 2; Wait time = 2 * 15 = 30 mins
        assertEquals(2, estimate.tokensAhead)
        assertEquals(30, estimate.estimatedWaitMinutes)
        assertFalse(estimate.isYourTurn)
    }

    @Test
    fun calculateWaitTime_whenCurrentTokenEqualsPatientToken_flagsIsYourTurn() {
        val queue = DailyQueue(
            queueId = "doc_01_2026-09-25",
            doctorId = "doc_01",
            doctorName = "Dr. Sharma",
            date = "2026-09-25",
            currentToken = 7,
            totalTokensIssued = 10,
            status = QueueStatus.ACTIVE
        )

        val estimate = calculateWaitTimeUseCase(
            queue = queue,
            patientToken = 7,
            doctorName = "Dr. Sharma",
            avgConsultationMinutes = 15
        )

        assertEquals(0, estimate.tokensAhead)
        assertEquals(0, estimate.estimatedWaitMinutes)
        assertTrue(estimate.isYourTurn)
    }

    @Test
    fun calculateWaitTime_whenCurrentTokenExceedsPatientToken_returnsZeroWait() {
        val queue = DailyQueue(
            queueId = "doc_01_2026-09-25",
            doctorId = "doc_01",
            doctorName = "Dr. Sharma",
            date = "2026-09-25",
            currentToken = 8,
            totalTokensIssued = 10,
            status = QueueStatus.ACTIVE
        )

        val estimate = calculateWaitTimeUseCase(
            queue = queue,
            patientToken = 5,
            doctorName = "Dr. Sharma",
            avgConsultationMinutes = 15
        )

        assertEquals(0, estimate.tokensAhead)
        assertEquals(0, estimate.estimatedWaitMinutes)
        assertFalse(estimate.isYourTurn)
    }
}
