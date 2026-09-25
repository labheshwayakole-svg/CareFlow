package com.example.careflow.domain.repository

import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.QueueStatus
import kotlinx.coroutines.flow.Flow

interface QueueRepository {
    fun observeLiveQueue(doctorId: String, date: String): Flow<DailyQueue?>
    fun observeAllActiveQueues(hospitalId: String? = null): Flow<List<DailyQueue>>
    suspend fun getOrCreateQueue(doctorId: String, doctorName: String, hospitalId: String, date: String): Resource<DailyQueue>
    suspend fun advanceQueue(doctorId: String, date: String, nextPatientId: String? = null): Resource<DailyQueue>
    suspend fun callPatient(doctorId: String, date: String, tokenNumber: Int, patientId: String): Resource<DailyQueue>
    suspend fun updateQueueStatus(doctorId: String, date: String, status: QueueStatus, breakMessage: String? = null): Resource<Unit>
    suspend fun markConsultationComplete(doctorId: String, date: String, appointmentId: String, doctorNotes: String = ""): Resource<Unit>
    suspend fun markNoShow(doctorId: String, date: String, appointmentId: String): Resource<Unit>
    suspend fun resetQueue(doctorId: String, date: String): Resource<DailyQueue>
}
