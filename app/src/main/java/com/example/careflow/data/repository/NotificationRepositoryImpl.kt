package com.example.careflow.data.repository

import com.example.careflow.core.dispatcher.DispatcherProvider
import com.example.careflow.core.result.Resource
import com.example.careflow.data.firebase.FirebaseDataSource
import com.example.careflow.domain.model.Notification
import com.example.careflow.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class NotificationRepositoryImpl(
    private val firebaseDataSource: FirebaseDataSource,
    private val dispatchers: DispatcherProvider
) : NotificationRepository {

    override fun observeNotifications(userId: String): Flow<List<Notification>> =
        firebaseDataSource.observeNotifications(userId).flowOn(dispatchers.io)

    override suspend fun sendNotification(notification: Notification): Resource<Unit> =
        withContext(dispatchers.io) {
            try {
                firebaseDataSource.sendNotification(notification)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to send notification", e)
            }
        }

    override suspend fun markAsRead(notificationId: String): Resource<Unit> =
        withContext(dispatchers.io) {
            try {
                firebaseDataSource.markNotificationAsRead(notificationId)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to mark notification as read", e)
            }
        }

    override suspend fun markAllAsRead(userId: String): Resource<Unit> =
        withContext(dispatchers.io) {
            try {
                firebaseDataSource.markAllNotificationsAsRead(userId)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to mark all as read", e)
            }
        }
}
