package com.example.careflow.data.repository

import com.example.careflow.core.dispatcher.DefaultDispatcherProvider
import com.example.careflow.core.dispatcher.DispatcherProvider
import com.example.careflow.core.result.Resource
import com.example.careflow.data.datasource.InMemoryDataStore
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.QueueStatus
import com.example.careflow.domain.repository.QueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class QueueRepositoryImpl(
    private val dataStore: InMemoryDataStore,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : QueueRepository {

    override fun observeLiveQueue(doctorId: String, date: String): Flow<DailyQueue?> {
        val key = "${doctorId}_$date"
        return dataStore.queues.map { map ->
            map[key]
        }
    }

    override fun observeAllActiveQueues(): Flow<List<DailyQueue>> {
        return dataStore.queues.map { it.values.toList() }
    }

    override suspend fun getOrCreateQueue(
        doctorId: String,
        doctorName: String,
        date: String
    ): Resource<DailyQueue> = withContext(dispatchers.io) {
        val key = "${doctorId}_$date"
        val existing = dataStore.queues.value[key]
        if (existing != null) {
            Resource.Success(existing)
        } else {
            val newQueue = DailyQueue(
                queueId = key,
                doctorId = doctorId,
                doctorName = doctorName,
                date = date,
                currentToken = 0,
                totalTokensIssued = 0,
                status = QueueStatus.ACTIVE
            )
            // Save to queues map
            val map = dataStore.queues.value.toMutableMap()
            map[key] = newQueue
            Resource.Success(newQueue)
        }
    }

    override suspend fun advanceQueue(doctorId: String, date: String): Resource<DailyQueue> = withContext(dispatchers.io) {
        try {
            val updatedQueue = dataStore.advanceQueueAtomic(doctorId, date)
            Resource.Success(updatedQueue)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to advance queue", e)
        }
    }

    override suspend fun updateQueueStatus(
        doctorId: String,
        date: String,
        status: QueueStatus
    ): Resource<Unit> = withContext(dispatchers.io) {
        dataStore.updateQueueStatus(doctorId, date, status)
        Resource.Success(Unit)
    }

    override suspend fun resetQueue(doctorId: String, date: String): Resource<DailyQueue> = withContext(dispatchers.io) {
        val reset = dataStore.resetQueue(doctorId, date)
        Resource.Success(reset)
    }
}
