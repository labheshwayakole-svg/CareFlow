package com.example.careflow.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.QueueStatus
import com.example.careflow.domain.model.UserRole
import com.example.careflow.theme.QueueActiveGreen
import com.example.careflow.theme.QueueCancelledRed
import com.example.careflow.theme.QueueServingCyan
import com.example.careflow.theme.QueueWaitingAmber

@Composable
fun TokenBadge(
    tokenNumber: Int,
    isHighlighted: Boolean = false,
    label: String = "TOKEN",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isHighlighted) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) QueueActiveGreen else MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "#$tokenNumber",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun StatusChip(
    status: AppointmentStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when (status) {
        AppointmentStatus.BOOKED -> Triple(QueueWaitingAmber.copy(alpha = 0.2f), QueueWaitingAmber, "BOOKED")
        AppointmentStatus.WAITING -> Triple(QueueWaitingAmber.copy(alpha = 0.2f), QueueWaitingAmber, "WAITING")
        AppointmentStatus.CALLED -> Triple(QueueServingCyan.copy(alpha = 0.25f), QueueServingCyan, "CALLED")
        AppointmentStatus.IN_CONSULTATION -> Triple(QueueServingCyan.copy(alpha = 0.2f), QueueServingCyan, "IN CONSULTATION")
        AppointmentStatus.COMPLETED -> Triple(QueueActiveGreen.copy(alpha = 0.2f), QueueActiveGreen, "COMPLETED")
        AppointmentStatus.CANCELLED -> Triple(QueueCancelledRed.copy(alpha = 0.2f), QueueCancelledRed, "CANCELLED")
        AppointmentStatus.NO_SHOW -> Triple(Color.Gray.copy(alpha = 0.2f), Color.DarkGray, "NO-SHOW")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val (bgColor, label) = when (role) {
        UserRole.PATIENT -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) to "PATIENT"
        UserRole.DOCTOR -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) to "DOCTOR"
        UserRole.ADMIN -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f) to "ADMIN"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
