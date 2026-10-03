package com.example.careflow.presentation.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.model.DoctorTimeSlot
import com.example.careflow.domain.util.BookingTimeUtils
import com.example.careflow.presentation.components.CarePulseButton
import com.example.careflow.presentation.components.DigitalTokenPass
import com.example.careflow.theme.CardBorderSubtle
import com.example.careflow.theme.CareBackground
import com.example.careflow.theme.CarePrimary
import com.example.careflow.theme.CarePrimaryContainer
import com.example.careflow.theme.CareSurface
import com.example.careflow.theme.CareSurfaceVariant
import com.example.careflow.theme.CareTextMuted
import com.example.careflow.theme.CareTextPrimary
import com.example.careflow.theme.CareTextSecondary
import com.example.careflow.theme.PastelMint
import com.example.careflow.theme.PastelPeach
import com.example.careflow.theme.PastelSky
import com.example.careflow.theme.PulseCoral
import com.example.careflow.theme.QueueActiveGreen
import com.example.careflow.theme.QueueWaitingAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DoctorBookingScreen(
    doctor: Doctor,
    uiState: PatientUiState,
    onSelectDate: (String) -> Unit = {},
    onBookClick: (String, String, String, String) -> Unit,
    onTrackQueueClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveSelectedDate = uiState.selectedBookingDate
    val days = if (uiState.availableBookingDates.isNotEmpty()) {
        uiState.availableBookingDates
    } else {
        remember { BookingTimeUtils.generateOneWeekDays() }
    }

    val slots = if (uiState.availableTimeSlots.isNotEmpty()) {
        uiState.availableTimeSlots
    } else {
        remember(doctor, effectiveSelectedDate) {
            BookingTimeUtils.generateDoctorSlots(doctor, uiState.selectedDoctorAppointments, effectiveSelectedDate)
        }
    }

    var selectedSlot by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }

    // Auto-select first available valid slot when slots or date changes
    LaunchedEffect(slots, effectiveSelectedDate) {
        val currentSlotValid = slots.any { it.timeSlot == selectedSlot && !it.isBooked && !it.isPassed }
        if (!currentSlotValid) {
            val firstAvailable = slots.firstOrNull { !it.isBooked && !it.isPassed }?.timeSlot
            if (firstAvailable != null) {
                selectedSlot = firstAvailable
            }
        }
    }

    val quickSymptoms = listOf(
        "Fever & Chills",
        "Chest Pain / Discomfort",
        "Persistent Cough",
        "High Blood Pressure",
        "Headache / Migraine",
        "Routine Health Checkup",
        "Stomach Pain",
        "Joint Pain"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CareBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onBackClick() },
                color = CareSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CareTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Book OPD Appointment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CareTextPrimary
                )
                Text(
                    text = "${doctor.departmentName} • ${doctor.hospitalName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CareTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Doctor Clinical Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CareSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PastelSky),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CarePrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = doctor.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CareTextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = CarePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${doctor.specialization} • ${doctor.departmentName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CarePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${doctor.qualification} • ${doctor.experienceYears} yrs experience",
                            style = MaterialTheme.typography.labelSmall,
                            color = CareTextMuted
                        )
                    }

                    // Live Presence Pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = when {
                            doctor.isOnBreak -> PastelPeach
                            doctor.isInOPD -> PastelMint
                            else -> CareSurfaceVariant
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            doctor.isOnBreak -> QueueWaitingAmber
                                            doctor.isInOPD -> QueueActiveGreen
                                            else -> Color.Gray
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when {
                                    doctor.isOnBreak -> "ON BREAK"
                                    doctor.isInOPD -> "IN OPD"
                                    else -> "OFFLINE"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    doctor.isOnBreak -> QueueWaitingAmber
                                    doctor.isInOPD -> QueueActiveGreen
                                    else -> CareTextMuted
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = CardBorderSubtle
                )

                // Meta Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = CareTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = BookingTimeUtils.formatDisplayShift(doctor.shiftStartTime, doctor.shiftEndTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = CareTextSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PastelSky
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = CarePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "₹${doctor.consultationFee.toInt()} OPD Fee",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = CarePrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.lastBookedAppointment != null) {
            // Success Card with Digital Perforated Token Pass
            val apt = uiState.lastBookedAppointment
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PastelMint,
                    border = androidx.compose.foundation.BorderStroke(1.dp, QueueActiveGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = QueueActiveGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Slot Confirmed & Digital Token Generated!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = QueueActiveGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Perforated Digital Token Pass
                DigitalTokenPass(
                    appointment = apt,
                    currentServing = uiState.activeQueueEstimate?.currentServingToken ?: 0,
                    estimatedWaitMinutes = 15,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onTrackQueueClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarePrimary)
                ) {
                    Text(
                        text = "Track Live Queue Position ⚡",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CareSurfaceVariant)
                ) {
                    Text(
                        text = "Back to Doctors List",
                        fontWeight = FontWeight.SemiBold,
                        color = CareTextSecondary
                    )
                }
            }
        } else {
            // ==========================================
            // 1. 1-Week Horizon Date Carousel
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1-Week Appointment Horizon",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CareTextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PastelMint
                ) {
                    Text(
                        text = "Advance Booking",
                        color = QueueActiveGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select any date from today up to next 7 days:",
                style = MaterialTheme.typography.bodySmall,
                color = CareTextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal scrolling row of 1-week dates
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { day ->
                    val isSelected = day.dateString == effectiveSelectedDate
                    Surface(
                        modifier = Modifier
                            .width(84.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectDate(day.dateString) }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) CarePrimary else CardBorderSubtle,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) CarePrimaryContainer else CareSurface,
                        shadowElevation = if (isSelected) 2.dp else 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day.dayName.uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) CarePrimary else CareTextMuted,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = day.displayDate,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CarePrimary else CareTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = when {
                                    day.isFull -> Color(0xFFF1F5F9)
                                    isSelected -> CarePrimary.copy(alpha = 0.15f)
                                    else -> PastelMint
                                }
                            ) {
                                Text(
                                    text = if (day.isFull) "Full" else "Open",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (day.isFull) Color(0xFF64748B) else QueueActiveGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // 2. Doctor Shift Timings & Real-Time Capacity Banner
            // ==========================================
            val totalSlotsCount = slots.size
            val bookedSlotsCount = slots.count { it.isBooked }
            val availableSlotsCount = slots.count { !it.isBooked && !it.isPassed }
            val occupancyRatio = if (totalSlotsCount > 0) {
                (bookedSlotsCount.toFloat() / totalSlotsCount).coerceIn(0f, 1f)
            } else 0f

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CareSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = CarePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OPD Timings: ${BookingTimeUtils.formatDisplayShift(doctor.shiftStartTime, doctor.shiftEndTime)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CareTextPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (availableSlotsCount > 0) CarePrimaryContainer else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = if (availableSlotsCount > 0) "$availableSlotsCount Slots Free" else "Shift Full",
                                color = if (availableSlotsCount > 0) CarePrimary else Color(0xFF64748B),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4-box seat metrics counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = CareSurfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$totalSlotsCount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CareTextPrimary
                                )
                                Text(
                                    text = "Total Seats",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CareTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Booked
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (bookedSlotsCount > 0) Color(0xFFF1F5F9) else CareSurfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$bookedSlotsCount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (bookedSlotsCount > 0) Color(0xFF475569) else CareTextPrimary
                                )
                                Text(
                                    text = "Booked",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CareTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Available
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = PastelMint
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$availableSlotsCount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = QueueActiveGreen
                                )
                                Text(
                                    text = "Available",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CareTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Next Token
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = CarePrimaryContainer
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "#${uiState.nextPredictedToken}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CarePrimary
                                )
                                Text(
                                    text = "Your Token",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CarePrimary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Occupancy Progress Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Shift Occupancy: ${(occupancyRatio * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = CareTextMuted
                        )
                        Text(
                            text = if (occupancyRatio >= 1f) "Capacity Reached" else "Live Booking Active",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (occupancyRatio >= 1f) Color(0xFFE11D48) else QueueActiveGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { occupancyRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            occupancyRatio >= 0.85f -> Color(0xFFE11D48)
                            occupancyRatio >= 0.5f -> QueueWaitingAmber
                            else -> CarePrimary
                        },
                        trackColor = CareSurfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // 3. Real-Time Consultation Slots Grid (Booked vs Available)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Select Consultation Time Slot",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CareTextPrimary
                    )
                    Text(
                        text = "Booked slots are locked • 30m / patient",
                        style = MaterialTheme.typography.labelSmall,
                        color = CareTextMuted
                    )
                }

                if (selectedSlot.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CarePrimaryContainer
                    ) {
                        Text(
                            text = "Selected: $selectedSlot",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CarePrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (availableSlotsCount == 0 && totalSlotsCount > 0) {
                // All slots booked notice
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "All Slots Booked for This Date",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Dr. ${doctor.name} has reached maximum capacity on $effectiveSelectedDate. Please tap another date in the 1-week horizon above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    slots.chunked(3).forEach { rowSlots ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSlots.forEach { slot ->
                                val isSelected = slot.timeSlot == selectedSlot && !slot.isBooked && !slot.isPassed
                                val isBooked = slot.isBooked
                                val isPassed = slot.isPassed

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = !isBooked && !isPassed) {
                                            selectedSlot = slot.timeSlot
                                        }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = when {
                                                isSelected -> CarePrimary
                                                isBooked -> Color(0xFFE2E8F0)
                                                isPassed -> CardBorderSubtle
                                                else -> CardBorderSubtle
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isSelected -> CarePrimaryContainer
                                        isBooked -> Color(0xFFF8FAFC)
                                        isPassed -> CareSurfaceVariant.copy(alpha = 0.5f)
                                        else -> CareSurface
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isBooked) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Booked",
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                            } else if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = CarePrimary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                            }
                                            Text(
                                                text = slot.timeSlot,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected || isBooked) FontWeight.Bold else FontWeight.Medium,
                                                color = when {
                                                    isSelected -> CarePrimary
                                                    isBooked -> Color(0xFF64748B)
                                                    isPassed -> CareTextMuted
                                                    else -> CareTextPrimary
                                                },
                                                textDecoration = if (isBooked || isPassed) TextDecoration.LineThrough else TextDecoration.None,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        Text(
                                            text = when {
                                                isBooked -> "Booked #${slot.bookedTokenNumber ?: ""}"
                                                isPassed -> "Passed"
                                                isSelected -> "Selected"
                                                else -> "Available"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected || isBooked) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> CarePrimary
                                                isBooked -> Color(0xFF94A3B8)
                                                isPassed -> CareTextMuted
                                                else -> QueueActiveGreen
                                            }
                                        )
                                    }
                                }
                            }
                            // Fill remaining columns if last row has fewer than 3 items
                            if (rowSlots.size < 3) {
                                repeat(3 - rowSlots.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Symptom Chips
            Text(
                text = "Symptoms or Reason for Visit",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CareTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap quick tags or enter details below for your doctor:",
                style = MaterialTheme.typography.bodySmall,
                color = CareTextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickSymptoms.forEach { chipText ->
                    val isChipSelected = symptoms.contains(chipText)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isChipSelected) CarePrimaryContainer else CareSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isChipSelected) CarePrimary else CardBorderSubtle
                        ),
                        modifier = Modifier.clickable {
                            symptoms = if (symptoms.isBlank()) {
                                chipText
                            } else if (isChipSelected) {
                                symptoms.replace(chipText, "").replace(", ,", ",").trim(',', ' ')
                            } else {
                                "$symptoms, $chipText"
                            }
                        }
                    ) {
                        Text(
                            text = chipText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isChipSelected) CarePrimary else CareTextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = symptoms,
                onValueChange = { symptoms = it },
                placeholder = { Text("e.g. Fever since yesterday, chest congestion, back pain...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CarePrimary,
                    unfocusedBorderColor = CardBorderSubtle,
                    focusedContainerColor = CareSurface,
                    unfocusedContainerColor = CareSurface
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Confirm Booking Button
            val canBook = !uiState.isLoading && doctor.isAvailable && availableSlotsCount > 0 && selectedSlot.isNotBlank()
            CarePulseButton(
                text = if (availableSlotsCount == 0 && totalSlotsCount > 0) {
                    "All Seats Booked for this Date"
                } else if (selectedSlot.isNotBlank()) {
                    "Confirm $selectedSlot • Get Token #${uiState.nextPredictedToken}"
                } else {
                    "Select a Time Slot to Proceed"
                },
                onClick = {
                    onBookClick(effectiveSelectedDate, selectedSlot, symptoms, symptoms)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                icon = Icons.Default.MedicalServices,
                isLoading = uiState.isLoading,
                enabled = canBook
            )
        }
    }
}

