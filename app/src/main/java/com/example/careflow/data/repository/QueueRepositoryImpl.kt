package com.example.careflow.data.repository

import com.example.careflow.core.dispatcher.DefaultDispatcherProvider
import com.example.careflow.core.dispatcher.DispatcherProvider
import com.example.careflow.core.result.Resource
import com.example.careflow.data.datasource.InMemoryDataStore
import com.example.careflow.data.firebase.FirebaseDataSource
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.QueueStatus
import com.example.careflow.domain.repository.QueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class QueueRepositoryImpl(
    private val dataStore: InMemoryDataStore,
    private val firebaseDataSource: FirebaseDataSource = FirebaseDataSource(),
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : QueueRepository {

    override fun observeLiveQueue(doctorId: String, date: String): Flow<DailyQueue?> = kotlinx.coroutines.flow.flow {
        val key = "${doctorId}_$date"
        val local = dataStore.queues.value[key]
        if (local != null) emit(local)
        try {
            firebaseDataSource.observeLiveQueue(doctorId, date).collect { fbQueue ->
                if (fbQueue != null) {
                    emit(fbQueue)
                } else if (local != null) {
                    emit(local)
                } else {
                    emit(null)
                }
            }
        } catch (_: Exception) {
            if (local != null) emit(local)
        }
    }.flowOn(dispatchers.io)

    override fun observeAllActiveQueues(hospitalId: String?): Flow<List<DailyQueue>> = kotlinx.coroutines.flow.flow {
        val localList = if (hospitalId.isNullOrBlank()) {
            dataStore.queues.value.values.toList()
        } else {
            dataStore.queues.value.values.filter { it.hospitalId == hospitalId }
        }
        if (localList.isNotEmpty()) emit(localList)
        try {
            firebaseDataSource.observeAllActiveQueues(hospitalId).collect { fbList ->
                if (fbList.isNotEmpty()) {
                    emit(fbList)
                } else if (localList.isNotEmpty()) {
                    emit(localList)
                } else {
                    emit(emptyList())
                }
            }
        } catch (_: Exception) {
            if (localList.isNotEmpty()) emit(localList)
        }
    }.flowOn(dispatchers.io)

    override suspend fun getOrCreateQueue(
        doctorId: String,
        doctorName: String,
        hospitalId: String,
        date: String
    ): Resource<DailyQueue> = withContext(dispatchers.io) {
        try {
            val queue = firebaseDataSource.getOrCreateQueue(doctorId, doctorName, hospitalId, date)
            Resource.Success(queue)
        } catch (e: Exception) {
            val key = "${doctorId}_$date"
            val existing = dataStore.queues.value[key]
            if (existing != null) {
                Resource.Success(existing)
            } else {
                val newQueue = DailyQueue(
                    queueId = key,
                    doctorId = doctorId,
                    doctorName = doctorName,
                    hospitalId = hospitalId,
                    date = date,
                    currentToken = 0,
                    totalTokensIssued = 0,
                    status = QueueStatus.ACTIVE
                )
                val map = dataStore.queues.value.toMutableMap()
                map[key] = newQueue
                Resource.Success(newQueue)
            }
        }
    }

    override suspend fun advanceQueue(
        doctorId: String,
        date: String,
        nextPatientId: String?
    ): Resource<DailyQueue> = withContext(dispatchers.io) {
        try {
            try {
                val updated = firebaseDataSource.advanceQueueAtomic(doctorId, date, nextPatientId)
                dataStore.advanceQueueAtomic(doctorId, date)
                return@withContext Resource.Success(updated)
            } catch (_: Exception) {
                val updatedQueue = dataStore.advanceQueueAtomic(doctorId, date)
                Resource.Success(updatedQueue)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to advance queue", e)
        }
    }

    override suspend fun callPatient(
        doctorId: String,
        date: String,
        tokenNumber: Int,
        patientId: String
    ): Resource<DailyQueue> = withContext(dispatchers.io) {
        try {
            try {
                val updated = firebaseDataSource.callPatientAtomic(doctorId, date, tokenNumber, patientId)
                dataStore.callPatientAtomic(doctorId, date, tokenNumber, patientId)
                Resource.Success(updated)
            } catch (_: Exception) {
                val localQueue = dataStore.callPatientAtomic(doctorId, date, tokenNumber, patientId)
                Resource.Success(localQueue)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to call patient", e)
        }
    }

    override suspend fun updateQueueStatus(
        doctorId: String,
        date: String,
        status: QueueStatus,
        breakMessage: String?
    ): Resource<Unit> = withContext(dispatchers.io) {
        try {
            firebaseDataSource.updateQueueStatus(doctorId, date, status, breakMessage)
            dataStore.updateQueueStatus(doctorId, date, status)
            Resource.Success(Unit)
        } catch (e: Exception) {
            dataStore.updateQueueStatus(doctorId, date, status)
            Resource.Success(Unit)
        }
    }

    override suspend fun markConsultationComplete(
        doctorId: String,
        date: String,
        appointmentId: String,
        doctorNotes: String
    ): Resource<Unit> = withContext(dispatchers.io) {
        try {
            firebaseDataSource.markConsultationComplete(doctorId, date, appointmentId, doctorNotes)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to complete consultation", e)
        }
    }

    override suspend fun markNoShow(
        doctorId: String,
        date: String,
        appointmentId: String
    ): Resource<Unit> = withContext(dispatchers.io) {
        try {
            firebaseDataSource.markNoShow(doctorId, date, appointmentId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark no-show", e)
        }
    }

    override suspend fun resetQueue(doctorId: String, date: String): Resource<DailyQueue> = withContext(dispatchers.io) {
        try {
            val queue = firebaseDataSource.resetQueue(doctorId, date)
            Resource.Success(queue)
        } catch (e: Exception) {
            val reset = dataStore.resetQueue(doctorId, date)
            Resource.Success(reset)
        }
    }
}
