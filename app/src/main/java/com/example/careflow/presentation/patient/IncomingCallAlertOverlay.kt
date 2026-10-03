package com.example.careflow.presentation.patient

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import com.example.careflow.core.notification.CallNotificationService
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.careflow.domain.model.Notification
import com.example.careflow.presentation.components.CareOutlinePillButton
import com.example.careflow.presentation.components.CarePulseButton
import com.example.careflow.theme.CardBorderSubtle
import com.example.careflow.theme.CareBackground
import com.example.careflow.theme.CarePrimary
import com.example.careflow.theme.CareSecondary
import com.example.careflow.theme.CareSurfaceVariant
import com.example.careflow.theme.CareTextMuted
import com.example.careflow.theme.CareTextPrimary
import com.example.careflow.theme.CareTextSecondary
import com.example.careflow.theme.CoralBright
import com.example.careflow.theme.CoralWarm
import com.example.careflow.theme.PastelPeach
import com.example.careflow.theme.PulseRoseBorder
import kotlinx.coroutines.delay

/**
 * Full-screen alert dialog displayed when the doctor calls the patient's token.
 * Redesigned in the CareFlow Warm Terracotta Coral & Sunset Peach theme.
 * Features animated pulsing ring, token number badge, and direct call-to-actions.
 */
@Composable
fun IncomingCallAlertOverlay(
    notification: Notification,
    onDismiss: () -> Unit,
    onNavigateToQueue: () -> Unit
) {
    val context = LocalContext.current
    var secondsRemaining by remember { mutableIntStateOf(30) }

    // Guarantee all ringtone audio & vibration stops when dialog leaves composition
    DisposableEffect(Unit) {
        onDispose {
            CallNotificationService.stopAlert(context)
            CallNotificationService.cancelNotification(context)
        }
    }

    // Auto-dismiss countdown timer (30 seconds)
    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining -= 1
        }
        CallNotificationService.stopAlert(context)
        CallNotificationService.cancelNotification(context)
        onDismiss()
    }

    // Infinite pulsing scale and glow animations
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

    // Parse Token number and Doctor Name from title/body
    val tokenNumberStr = remember(notification.title) {
        val extracted = notification.title.substringAfter("Token #", "")
            .substringBefore(")").trim()
        if (extracted.isNotEmpty()) "#$extracted"
        else {
            val digits = notification.title.filter { it.isDigit() }
            if (digits.isNotEmpty()) "#$digits" else "#—"
        }
    }

    val doctorName = remember(notification.body) {
        val raw = if (notification.body.contains("Dr. ")) {
            notification.body.substringAfter("Dr. ").substringBefore(" is calling").trim()
        } else {
            "Your Doctor"
        }
        val clean = raw.removePrefix("Dr.").removePrefix("Dr. ").trim()
        if (clean.isNotBlank() && clean != "Your Doctor") "Dr. $clean" else raw
    }

    Dialog(
        onDismissRequest = { /* Require deliberate button interaction */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CareBackground
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                PastelPeach.copy(alpha = 0.85f),
                                CareBackground,
                                CareBackground
                            )
                        )
                    )
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Urgent Header Badge: Warm Terracotta Coral Pill
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = CarePrimary,
                        shadowElevation = 3.dp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NOW CALLING",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    // Pulsing Bell/Speaker Icon with Rings in Warm Coral
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(110.dp)
                            .padding(bottom = 4.dp)
                    ) {
                        // Expanding Ring
                        Box(
                            modifier = Modifier
                                .size(105.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(CarePrimary.copy(alpha = ringAlpha))
                        )
                        // Inner Solid Circle with Sunset Coral Gradient
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(CoralBright, CoralWarm)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Active Call Alert",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "It's Your Turn!",
                        color = CareTextPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Your consultation is ready. Please proceed to the OPD room.",
                        color = CareTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Token Number Display Card: Warm Porcelain & Apricot Container
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = CardBorderSubtle,
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp, horizontal = 18.dp)
                        ) {
                            // Apricot Token Number Box
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = PastelPeach,
                                border = BorderStroke(1.dp, PulseRoseBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = "YOUR OPD TOKEN",
                                        color = CareTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = tokenNumberStr,
                                        color = CarePrimary,
                                        fontSize = 54.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Doctor info chip
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = CareSurfaceVariant,
                                border = BorderStroke(1.dp, CardBorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(1.dp, PulseRoseBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = CarePrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = doctorName,
                                            color = CareTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Consultation Room is waiting",
                                            color = CareTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MeetingRoom,
                                    contentDescription = null,
                                    tint = CareSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Step inside now — Doctor is ready for you",
                                    color = CareSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action 1: "I'm Coming!" Button (Signature CarePulseButton in Warm Terracotta Coral)
                    CarePulseButton(
                        text = "I'm Coming!",
                        onClick = {
                            CallNotificationService.stopAlert(context)
                            CallNotificationService.cancelNotification(context)
                            onDismiss()
                        },
                        icon = Icons.Default.Check,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action 2: "View Live Queue Tracker" Button (Clean Warm Outline Pill)
                    CareOutlinePillButton(
                        text = "View Live Queue Tracker",
                        onClick = {
                            CallNotificationService.stopAlert(context)
                            CallNotificationService.cancelNotification(context)
                            onNavigateToQueue()
                        },
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        textColor = CarePrimary,
                        borderColor = CardBorderSubtle,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Auto-dismiss countdown badge (Warm pill)
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = CareSurfaceVariant,
                        border = BorderStroke(1.dp, CardBorderSubtle)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = CareTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto-dismissing in ${secondsRemaining}s",
                                color = CareTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
