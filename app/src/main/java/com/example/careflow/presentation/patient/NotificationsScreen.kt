package com.example.careflow.presentation.patient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careflow.domain.model.Notification
import com.example.careflow.domain.model.NotificationType
import com.example.careflow.theme.CarePrimary
import com.example.careflow.theme.CarePrimaryContainer
import com.example.careflow.theme.PulseCrimsonDark
import com.example.careflow.theme.PulseLiveGreen
import com.example.careflow.theme.QueueWaitingAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class NotificationFilter(val label: String) {
    ALL("All"),
    UNREAD("Unread"),
    QUEUE_CALLS("Queue Calls"),
    REMINDERS("Reminders")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    uiState: PatientUiState,
    onBackClick: () -> Unit,
    onMarkNotificationRead: (String) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onDeleteNotification: (String) -> Unit,
    onClearAllNotifications: () -> Unit,
    onNavigateToQueue: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(NotificationFilter.ALL) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val notifications = uiState.notifications
    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.UNREAD -> notifications.filter { !it.isRead }
            NotificationFilter.QUEUE_CALLS -> notifications.filter { it.type == NotificationType.QUEUE_CALLED }
            NotificationFilter.REMINDERS -> notifications.filter {
                it.type == NotificationType.APPOINTMENT_REMINDER_1HR ||
                it.type == NotificationType.APPOINTMENT_REMINDER_30MIN
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Notifications",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = CarePrimary
                            ) {
                                Text(
                                    text = "$unreadCount new",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        IconButton(
                            onClick = onMarkAllNotificationsRead,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all as read",
                                tint = CarePrimary
                            )
                        }
                    }
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear All Notifications",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Chips Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(NotificationFilter.entries) { filter ->
                    val count = when (filter) {
                        NotificationFilter.ALL -> notifications.size
                        NotificationFilter.UNREAD -> unreadCount
                        NotificationFilter.QUEUE_CALLS -> notifications.count { it.type == NotificationType.QUEUE_CALLED }
                        NotificationFilter.REMINDERS -> notifications.count {
                            it.type == NotificationType.APPOINTMENT_REMINDER_1HR ||
                            it.type == NotificationType.APPOINTMENT_REMINDER_30MIN
                        }
                    }

                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = "${filter.label} ($count)",
                                fontSize = 13.sp,
                                fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CarePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Notification List or Empty State
            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedFilter == NotificationFilter.ALL) "All Caught Up!" else "No ${selectedFilter.label} Notifications",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedFilter == NotificationFilter.ALL)
                                "You have no active alerts or reminders at this time."
                            else
                                "There are no notifications matching the selected filter.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { notif ->
                        NotificationItemCard(
                            notification = notif,
                            onCardClick = {
                                if (!notif.isRead) onMarkNotificationRead(notif.id)
                            },
                            onDeleteClick = {
                                onDeleteNotification(notif.id)
                            },
                            onTrackQueue = onNavigateToQueue
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Clearing All Notifications
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Clear All Notifications?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all your notification history. Are you sure you want to proceed?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearAllNotifications()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NotificationItemCard(
    notification: Notification,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onTrackQueue: () -> Unit
) {
    val iconInfo = remember(notification.type) { getNotificationVisuals(notification.type) }
    val timeAgo = remember(notification.createdAt) { formatRelativeTime(notification.createdAt) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) {
                CarePrimaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (!notification.isRead) 1.5.dp else 1.dp,
            color = if (!notification.isRead) CarePrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notification.isRead) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Type Icon Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconInfo.backgroundColor,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconInfo.icon,
                            contentDescription = null,
                            tint = iconInfo.tintColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Body
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notification.title,
                            fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Unread Dot
                        if (!notification.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CarePrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        // Delete Button
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete notification",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = notification.body,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeAgo,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Medium
                        )

                        // Special Action for QUEUE_CALLED
                        if (notification.type == NotificationType.QUEUE_CALLED) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PulseCrimsonDark,
                                modifier = Modifier.clickable(onClick = onTrackQueue)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Track Live Queue",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class NotificationVisualInfo(
    val icon: ImageVector,
    val tintColor: Color,
    val backgroundColor: Color
)

private fun getNotificationVisuals(type: NotificationType): NotificationVisualInfo {
    return when (type) {
        NotificationType.QUEUE_CALLED -> NotificationVisualInfo(
            icon = Icons.Default.NotificationsActive,
            tintColor = Color(0xFFDC2626),
            backgroundColor = Color(0xFFFEE2E2)
        )
        NotificationType.APPOINTMENT_REMINDER_1HR -> NotificationVisualInfo(
            icon = Icons.Default.Schedule,
            tintColor = Color(0xFFD97706),
            backgroundColor = Color(0xFFFEF3C7)
        )
        NotificationType.APPOINTMENT_REMINDER_30MIN -> NotificationVisualInfo(
            icon = Icons.Default.Schedule,
            tintColor = Color(0xFFEA580C),
            backgroundColor = Color(0xFFFFEDD5)
        )
        NotificationType.DOCTOR_ON_BREAK -> NotificationVisualInfo(
            icon = Icons.Default.FreeBreakfast,
            tintColor = QueueWaitingAmber,
            backgroundColor = Color(0xFFFEF9C3)
        )
        NotificationType.DOCTOR_BACK_FROM_BREAK,
        NotificationType.DOCTOR_AVAILABLE -> NotificationVisualInfo(
            icon = Icons.Default.Verified,
            tintColor = PulseLiveGreen,
            backgroundColor = Color(0xFFDCFCE7)
        )
        NotificationType.DOCTOR_LEFT_OPD -> NotificationVisualInfo(
            icon = Icons.Default.MeetingRoom,
            tintColor = Color(0xFF64748B),
            backgroundColor = Color(0xFFF1F5F9)
        )
        NotificationType.QUEUE_POSITION_UPDATE -> NotificationVisualInfo(
            icon = Icons.Default.Notifications,
            tintColor = CarePrimary,
            backgroundColor = CarePrimaryContainer
        )
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diffMs = now - timestamp

    if (diffMs < 60_000L) return "Just now"
    val minutes = diffMs / 60_000L
    if (minutes < 60) return "${minutes}m ago"
    val hours = minutes / 60
    if (hours < 24) return "${hours}h ago"
    val days = hours / 24
    if (days < 7) return "${days}d ago"

    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
