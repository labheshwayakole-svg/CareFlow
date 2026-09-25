package com.example.careflow.domain.repository

import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(userId: String): Flow<List<Notification>>
    suspend fun sendNotification(notification: Notification): Resource<Unit>
    suspend fun markAsRead(notificationId: String): Resource<Unit>
    suspend fun markAllAsRead(userId: String): Resource<Unit>
}
