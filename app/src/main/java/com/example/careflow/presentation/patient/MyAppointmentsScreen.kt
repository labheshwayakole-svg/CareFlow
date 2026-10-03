package com.example.careflow.presentation.patient

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.presentation.components.CarePulseButton
import com.example.careflow.presentation.components.StatusChip
import com.example.careflow.presentation.components.TokenBadge
import com.example.careflow.theme.CardBorderSubtle
import com.example.careflow.theme.CareBackground
import com.example.careflow.theme.CarePrimary
import com.example.careflow.theme.CarePrimaryContainer
import com.example.careflow.theme.CareSurface
import com.example.careflow.theme.CareSurfaceVariant
import com.example.careflow.theme.CareTextMuted
import com.example.careflow.theme.CareTextPrimary
import com.example.careflow.theme.CareTextSecondary
import com.example.careflow.theme.PastelSky
import com.example.careflow.theme.PulseRed
import com.example.careflow.theme.PulseRoseBg
import com.example.careflow.theme.PulseRoseBorder

@Composable
fun MyAppointmentsScreen(
    uiState: PatientUiState,
    onTrackAppointmentQueue: (Appointment) -> Unit,
    onCancelAppointment: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Active, 2: Past
    var appointmentToCancel by remember { mutableStateOf<Appointment?>(null) }

    val filteredAppointments = remember(uiState.myAppointments, selectedFilterTab) {
        when (selectedFilterTab) {
            1 -> uiState.myAppointments.filter { it.status == AppointmentStatus.BOOKED || it.status == AppointmentStatus.WAITING || it.status == AppointmentStatus.IN_CONSULTATION }
            2 -> uiState.myAppointments.filter { it.status == AppointmentStatus.COMPLETED || it.status == AppointmentStatus.CANCELLED || it.status == AppointmentStatus.NO_SHOW }
            else -> uiState.myAppointments
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CareBackground)
            .padding(16.dp)
    ) {
        // Navigation Header
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
                    text = "My Appointments",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CareTextPrimary
                )
                Text(
                    text = "${uiState.myAppointments.size} Total Bookings",
                    style = MaterialTheme.typography.bodySmall,
                    color = CareTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Tabs
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
            color = CareSurface
        ) {
            TabRow(
                selectedTabIndex = selectedFilterTab,
                containerColor = CareSurface,
                contentColor = CarePrimary,
                divider = {}
            ) {
                Tab(
                    selected = selectedFilterTab == 0,
                    onClick = { selectedFilterTab = 0 },
                    text = {
                        Text(
                            text = "All (${uiState.myAppointments.size})",
                            fontWeight = if (selectedFilterTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedFilterTab == 1,
                    onClick = { selectedFilterTab = 1 },
                    text = {
                        val activeCount = uiState.myAppointments.count { it.status == AppointmentStatus.BOOKED || it.status == AppointmentStatus.IN_CONSULTATION }
                        Text(
                            text = "Active ($activeCount)",
                            fontWeight = if (selectedFilterTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedFilterTab == 2,
                    onClick = { selectedFilterTab = 2 },
                    text = {
                        val pastCount = uiState.myAppointments.count { it.status == AppointmentStatus.COMPLETED || it.status == AppointmentStatus.CANCELLED }
                        Text(
                            text = "History ($pastCount)",
                            fontWeight = if (selectedFilterTab == 2) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredAppointments.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PastelSky),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = null,
                        tint = CarePrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (selectedFilterTab == 1) "No active appointments" else "No appointments found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CareTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Book an OPD slot with any doctor to receive your live digital queue token.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredAppointments, key = { it.id }) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onTrackClick = { onTrackAppointmentQueue(appointment) },
                        onCancelClick = { appointmentToCancel = appointment }
                    )
                }
            }
        }
    }

    // Cancel Confirmation Dialog
    appointmentToCancel?.let { apt ->
        AlertDialog(
            onDismissRequest = { appointmentToCancel = null },
            title = {
                Text(
                    text = "Cancel Appointment?",
                    fontWeight = FontWeight.Bold,
                    color = CareTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to cancel your appointment with Dr. ${apt.doctorName} (Token #${apt.tokenNumber}) on ${apt.date} at ${apt.timeSlot}?",
                    color = CareTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelAppointment(apt.id)
                        appointmentToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Cancel", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { appointmentToCancel = null }) {
                    Text("Keep Appointment", color = CareTextSecondary)
                }
            }
        )
    }
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onTrackClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CareSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Doctor and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PulseRoseBg)
                            .border(1.dp, PulseRoseBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = CarePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Dr. ${appointment.doctorName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CareTextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = CarePrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = appointment.departmentName,
                            style = MaterialTheme.typography.bodySmall,
                            color = CarePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (appointment.hospitalName.isNotBlank()) {
                            Text(
                                text = appointment.hospitalName,
                                style = MaterialTheme.typography.labelSmall,
                                color = CareTextMuted
                            )
                        }
                    }
                }

                StatusChip(status = appointment.status)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = CardBorderSubtle
            )

            // Date, Time, and Token Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = CareTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = appointment.date,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CareTextPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = CareTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Slot: ${appointment.timeSlot}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CareTextSecondary
                        )
                    }
                    if (appointment.symptoms.isNotBlank() || appointment.notes.isNotBlank()) {
                        Text(
                            text = "Reason: ${appointment.symptoms.ifBlank { appointment.notes }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CareTextMuted
                        )
                    }
                }

                TokenBadge(
                    tokenNumber = appointment.tokenNumber,
                    label = "TOKEN",
                    isHighlighted = appointment.status == AppointmentStatus.BOOKED || appointment.status == AppointmentStatus.IN_CONSULTATION
                )
            }

            // Action Buttons for Active Appointments
            if (appointment.status == AppointmentStatus.BOOKED || appointment.status == AppointmentStatus.IN_CONSULTATION) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CarePulseButton(
                        text = "Track Live Queue",
                        onClick = onTrackClick,
                        modifier = Modifier.weight(1f).height(44.dp)
                    )

                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

