package com.example.careflow.domain.repository

import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.QueueStatus
import kotlinx.coroutines.flow.Flow

interface QueueRepository {
    fun observeLiveQueue(doctorId: String, date: String): Flow<DailyQueue?>
    fun observeAllActiveQueues(): Flow<List<DailyQueue>>
    suspend fun getOrCreateQueue(doctorId: String, doctorName: String, date: String): Resource<DailyQueue>
    suspend fun advanceQueue(doctorId: String, date: String): Resource<DailyQueue>
    suspend fun updateQueueStatus(doctorId: String, date: String, status: QueueStatus): Resource<Unit>
    suspend fun resetQueue(doctorId: String, date: String): Resource<DailyQueue>
}
