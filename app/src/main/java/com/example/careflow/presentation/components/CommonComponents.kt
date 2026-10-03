package com.example.careflow.presentation.components

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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.UserRole
import com.example.careflow.theme.CardBorderSubtle
import com.example.careflow.theme.CardShadowSubtle
import com.example.careflow.theme.CareOnPrimaryContainer
import com.example.careflow.theme.CarePrimary
import com.example.careflow.theme.CarePrimaryContainer
import com.example.careflow.theme.PastelAmber
import com.example.careflow.theme.PastelMint
import com.example.careflow.theme.PastelSky
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import com.example.careflow.theme.PulseCrimsonDark
import com.example.careflow.theme.PulseRed
import com.example.careflow.theme.PulseRedBright
import com.example.careflow.theme.PulseCoral
import com.example.careflow.theme.PulseRoseBg
import com.example.careflow.theme.PulseRoseBorder
import com.example.careflow.theme.PulseRoseText
import com.example.careflow.theme.PulseShadow
import com.example.careflow.theme.PulseLiveGreen
import com.example.careflow.theme.PulseLiveGreenBg
import com.example.careflow.theme.PulseLiveGreenBorder
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
        targetValue = if (isHighlighted) 1.05f else 1f,
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
            containerColor = if (isHighlighted) QueueActiveGreen else CarePrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isHighlighted) Color.White else CareOnPrimaryContainer,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "#$tokenNumber",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isHighlighted) Color.White else CareOnPrimaryContainer,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * Premium Hospital OPD Digital Token Pass
 * Designed like a clinical boarding slip with live queue progress
 */
@Composable
fun DigitalTokenPass(
    hospitalName: String,
    opdRoom: String,
    doctorName: String,
    tokenNumber: Int,
    currentCallingToken: Int,
    estimatedWaitMinutes: Int,
    status: AppointmentStatus,
    modifier: Modifier = Modifier
) {
    val isTurnPassed = (currentCallingToken > tokenNumber && tokenNumber > 0) || status == AppointmentStatus.COMPLETED
    val isTurnOver = isTurnPassed || status == AppointmentStatus.CANCELLED || status == AppointmentStatus.NO_SHOW
    val isMyTurn = !isTurnOver && (currentCallingToken == tokenNumber || ((status == AppointmentStatus.CALLED || status == AppointmentStatus.IN_CONSULTATION) && (currentCallingToken == 0 || currentCallingToken == tokenNumber)))
    val effectiveStatus = if (isTurnPassed) AppointmentStatus.COMPLETED else status
    val tokensAhead = if (isTurnPassed) 0 else maxOf(0, tokenNumber - currentCallingToken)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, if (isMyTurn) QueueActiveGreen else CardBorderSubtle, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isMyTurn) 6.dp else 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Hospital & Chamber Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        when {
                            status == AppointmentStatus.COMPLETED -> QueueActiveGreen.copy(alpha = 0.10f)
                            status == AppointmentStatus.CANCELLED || status == AppointmentStatus.NO_SHOW -> QueueCancelledRed.copy(alpha = 0.08f)
                            isMyTurn -> QueueActiveGreen.copy(alpha = 0.12f)
                            else -> CarePrimaryContainer.copy(alpha = 0.5f)
                        }
                    )
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = when {
                                status == AppointmentStatus.COMPLETED || isMyTurn -> QueueActiveGreen
                                status == AppointmentStatus.CANCELLED || status == AppointmentStatus.NO_SHOW -> QueueCancelledRed
                                else -> CarePrimary
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = hospitalName.ifBlank { "CareFlow Hospital" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CarePrimary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = CarePrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = opdRoom.ifBlank { "Room 102" },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CarePrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Body: Token Number & Live Turn State
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "YOUR TOKEN NUMBER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "#$tokenNumber",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                status == AppointmentStatus.COMPLETED || isMyTurn -> QueueActiveGreen
                                status == AppointmentStatus.CANCELLED || status == AppointmentStatus.NO_SHOW -> QueueCancelledRed
                                else -> CarePrimary
                            }
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        StatusChip(status = effectiveStatus)
                        Spacer(modifier = Modifier.height(4.dp))
                        val cleanDocName = doctorName.removePrefix("Dr.").removePrefix("Dr. ").trim().let { if (it.isNotBlank()) "Dr. $it" else "Doctor" }
                        Text(
                            text = "Consulting with $cleanDocName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Perforated Divider
                HorizontalDivider(color = CardBorderSubtle, thickness = 1.dp)

                Spacer(modifier = Modifier.height(14.dp))

                // Queue Progress Bar & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        status == AppointmentStatus.COMPLETED || isMyTurn -> QueueActiveGreen
                                        status == AppointmentStatus.CANCELLED || status == AppointmentStatus.NO_SHOW -> QueueCancelledRed
                                        else -> QueueServingCyan
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Now Calling: Token #$currentCallingToken",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (waitIcon, waitColor, waitText) = when {
                            status == AppointmentStatus.COMPLETED -> Triple(Icons.Default.CheckCircle, QueueActiveGreen, "Consultation Complete")
                            status == AppointmentStatus.CANCELLED -> Triple(Icons.Default.Cancel, QueueCancelledRed, "Cancelled")
                            status == AppointmentStatus.NO_SHOW -> Triple(Icons.Default.Warning, QueueWaitingAmber, "No Show")
                            isMyTurn -> Triple(Icons.Default.AccessTime, QueueActiveGreen, "Proceed to Chamber!")
                            else -> Triple(Icons.Default.AccessTime, QueueWaitingAmber, "~$estimatedWaitMinutes mins wait")
                        }
                        Icon(
                            imageVector = waitIcon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = waitColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = waitText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = waitColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val progress = when {
                    status == AppointmentStatus.COMPLETED -> 1f
                    status == AppointmentStatus.CANCELLED || status == AppointmentStatus.NO_SHOW -> 0.05f
                    tokenNumber > 0 && currentCallingToken > 0 -> {
                        (currentCallingToken.toFloat() / tokenNumber.toFloat()).coerceIn(0.1f, 1f)
                    }
                    else -> 0.2f
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        status == AppointmentStatus.COMPLETED || isMyTurn -> QueueActiveGreen
                        status == AppointmentStatus.CANCELLED || status == AppointmentStatus.NO_SHOW -> QueueCancelledRed
                        else -> CarePrimary
                    },
                    trackColor = CarePrimaryContainer.copy(alpha = 0.5f),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when {
                        status == AppointmentStatus.COMPLETED || isTurnPassed -> "✅ Your turn was called earlier / Consultation finished."
                        status == AppointmentStatus.CANCELLED -> "❌ This token was cancelled."
                        status == AppointmentStatus.NO_SHOW -> "⚠️ Marked as No-Show. Please visit OPD reception to reschedule."
                        isMyTurn -> "🟢 Your turn is active! Please enter the doctor's chamber."
                        tokensAhead == 1 -> "⚡ You are next in queue! Please wait near the door."
                        else -> "⏳ $tokensAhead patients ahead of you in the queue."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        status == AppointmentStatus.COMPLETED || isTurnPassed || isMyTurn -> QueueActiveGreen
                        status == AppointmentStatus.CANCELLED || status == AppointmentStatus.NO_SHOW -> QueueCancelledRed
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (isMyTurn || status == AppointmentStatus.COMPLETED || isTurnPassed) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun DigitalTokenPass(
    appointment: Appointment,
    currentServing: Int,
    estimatedWaitMinutes: Int = 15,
    modifier: Modifier = Modifier
) {
    DigitalTokenPass(
        hospitalName = appointment.hospitalName.ifBlank { "CareFlow Hospital OPD" },
        opdRoom = "Chamber 102",
        doctorName = "Dr. ${appointment.doctorName.removePrefix("Dr.").removePrefix("Dr. ").trim()}",
        tokenNumber = appointment.tokenNumber,
        currentCallingToken = currentServing,
        estimatedWaitMinutes = estimatedWaitMinutes,
        status = appointment.status,
        modifier = modifier
    )
}

@Composable
fun StatusChip(
    status: AppointmentStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when (status) {
        AppointmentStatus.BOOKED -> Triple(PastelAmber, QueueWaitingAmber, "BOOKED")
        AppointmentStatus.WAITING -> Triple(PastelAmber, QueueWaitingAmber, "IN QUEUE")
        AppointmentStatus.CALLED -> Triple(PastelMint, QueueActiveGreen, "CALLING NOW")
        AppointmentStatus.IN_CONSULTATION -> Triple(PastelSky, QueueServingCyan, "IN CONSULTATION")
        AppointmentStatus.COMPLETED -> Triple(PastelMint, QueueActiveGreen, "COMPLETED")
        AppointmentStatus.CANCELLED -> Triple(PastelAmber.copy(alpha = 0.5f), QueueCancelledRed, "CANCELLED")
        AppointmentStatus.NO_SHOW -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), "NO-SHOW")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (role) {
        UserRole.PATIENT -> Triple(CarePrimaryContainer.copy(alpha = 0.5f), CarePrimary, "PATIENT")
        UserRole.DOCTOR -> Triple(PastelMint, QueueActiveGreen, "DOCTOR")
        UserRole.ADMIN -> Triple(PastelAmber, QueueWaitingAmber, "HOSPITAL ADMIN")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            color = textColor,
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
    iconTint: Color = CarePrimary,
    containerColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, CardBorderSubtle, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =========================================================================
// HEMOPULSE SIGNATURE COMPONENTS (from hemopulseapp.web.app)
// =========================================================================

/**
 * Animated Live Radar Pulse Indicator
 * Matches .pill-live-dot and .emergency-pulse-indicator from HemoPulse
 */
@Composable
fun LivePulseDot(
    modifier: Modifier = Modifier,
    color: Color = PulseRedBright,
    size: androidx.compose.ui.unit.Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseDot")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier.size(size * 2),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/**
 * Signature Live Status Pill
 * Matches .pill-live-tag and .brand-badge from HemoPulse
 */
@Composable
fun LiveStatusPill(
    text: String = "",
    label: String = text,
    modifier: Modifier = Modifier,
    isLive: Boolean = true,
    accentColor: Color = PulseRedBright,
    backgroundColor: Color = PulseRoseBg,
    borderColor: Color = PulseRoseBorder
) {
    val displayLabel = if (label.isNotBlank()) label else text
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isLive) {
                LivePulseDot(color = accentColor, size = 6.dp)
            }
            Text(
                text = displayLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Signature HemoPulse Gradient Pill Button
 * Matches .btn-nav-filled, .btn-hero-red, and .primary from HemoPulse
 */
@Composable
fun CarePulseButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val gradientBrush = Brush.horizontalGradient(
        listOf(PulseCrimsonDark, PulseRed, PulseCoral)
    )

    Surface(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .height(48.dp)
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = CircleShape,
                ambientColor = PulseShadow,
                spotColor = PulseShadow
            ),
        shape = CircleShape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(if (enabled) gradientBrush else Brush.linearGradient(listOf(Color.LightGray, Color.Gray)))
                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.3.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

/**
 * Signature HemoPulse Outline Pill Button
 * Matches .btn-nav-outline and .outline from HemoPulse
 */
@Composable
fun CareOutlinePillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = CardBorderSubtle,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        shape = CircleShape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/**
 * Luminous Glassmorphic Hero Card Container
 * Matches .hero-card styling from HemoPulse
 */
@Composable
fun LuminousHeroCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(26.dp))
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x18F04843)),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            content = content
        )
    }
}

