package com.example.careflow.presentation.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.presentation.components.StatCard
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
import com.example.careflow.theme.PastelMint
import com.example.careflow.theme.PastelPeach
import com.example.careflow.theme.PastelSky
import com.example.careflow.theme.QueueActiveGreen
import com.example.careflow.theme.QueueActiveGreenContainer
import com.example.careflow.theme.QueueCancelledRed
import com.example.careflow.theme.QueueCancelledRedContainer
import com.example.careflow.theme.QueueServingCyan
import com.example.careflow.theme.QueueServingCyanContainer
import com.example.careflow.theme.QueueWaitingAmber
import com.example.careflow.theme.QueueWaitingAmberContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DoctorDashboardScreen(
    uiState: DoctorUiState,
    onCallNextPatient: () -> Unit,
    onCallSpecificPatient: (Appointment) -> Unit,
    onCompleteConsultation: (String) -> Unit,
    onMarkNoShow: () -> Unit,
    onToggleOpdPresence: (Boolean) -> Unit,
    onToggleBreakMode: (Boolean, String) -> Unit,
    onToggleAvailability: (Boolean) -> Unit,
    onResetQueue: () -> Unit,
    onLogoutClick: () -> Unit,
    onClearMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val doctor = uiState.doctor
    val queue = uiState.todayQueue
    val currentPatient = uiState.currentServingAppointment
    var visitNotes by remember { mutableStateOf("") }
    var showBreakDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var breakMessage by remember { mutableStateOf("15-minute tea break") }
    val snackbarHostState = remember { SnackbarHostState() }

    val presetBreakReasons = listOf(
        "15-minute quick tea break",
        "30-minute lunch break",
        "Emergency ward round",
        "Clinical procedure"
    )

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessages()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CareBackground),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CareBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (doctor == null) {
                item {
                    // Empty Doctor Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PastelSky),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = CarePrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Doctor Workstation",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CareTextPrimary
                                )
                                Text(
                                    text = "CareFlow Clinical Portal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CarePrimary
                                )
                            }
                        }

                        IconButton(onClick = onLogoutClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CareSurface),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(PastelSky),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = CarePrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Doctor Profile Found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CareTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "You are currently opened in Doctor mode, but no doctor has been added yet.\n\nPlease open as Admin first to setup your hospital and add doctors, or sign in using generated doctor credentials.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = CareTextMuted
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = onLogoutClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CarePrimary)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Switch Role / Logout", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                // Header: Doctor Profile & Status
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = CareSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(PastelSky),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MedicalServices,
                                        contentDescription = null,
                                        tint = CarePrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
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
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = "${doctor.specialization} • ${doctor.departmentName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CarePrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${doctor.qualification} • ${doctor.hospitalName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CareTextMuted
                                    )
                                }
                            }

                            IconButton(onClick = onLogoutClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Real-Time OPD Presence & Break Toggles
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CareSurface),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // OPD Presence Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("In OPD Room Presence", fontWeight = FontWeight.Bold, color = CareTextPrimary)
                                    Text(
                                        text = if (doctor.isInOPD) "Physically in OPD Room (Accepting Patients)" else "Marked as Left OPD / Offline",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (doctor.isInOPD) QueueActiveGreen else CareTextMuted
                                    )
                                }
                                Switch(
                                    checked = doctor.isInOPD,
                                    onCheckedChange = { onToggleOpdPresence(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = QueueActiveGreen
                                    )
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = CardBorderSubtle
                            )

                            // Break Mode Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Break Mode", fontWeight = FontWeight.Bold, color = CareTextPrimary)
                                    Text(
                                        text = if (doctor.isOnBreak) "On Break: ${doctor.breakMessage.ifBlank { "Queue Paused" }}" else "Active (No Break)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (doctor.isOnBreak) QueueWaitingAmber else CareTextMuted
                                    )
                                }
                                if (doctor.isOnBreak) {
                                    Button(
                                        onClick = { onToggleBreakMode(false, "") },
                                        colors = ButtonDefaults.buttonColors(containerColor = QueueActiveGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Resume OPD", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { showBreakDialog = true },
                                        enabled = doctor.isInOPD,
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
                                    ) {
                                        Icon(Icons.Default.FreeBreakfast, contentDescription = null, modifier = Modifier.size(16.dp), tint = QueueWaitingAmber)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Take Break", color = CareTextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Live Queue Controller Card (Tactile Centerpiece)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = CareSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "LIVE OPD QUEUE CONTROLLER",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CarePrimary,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            TokenBadge(
                                tokenNumber = queue?.currentToken ?: 0,
                                label = "NOW SERVING",
                                isHighlighted = true
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Counters Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Total Issued", style = MaterialTheme.typography.labelSmall, color = CareTextMuted)
                                    Text(
                                        text = "${queue?.totalTokensIssued ?: 0}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = CareTextPrimary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Completed", style = MaterialTheme.typography.labelSmall, color = CareTextMuted)
                                    Text(
                                        text = "${queue?.totalCompleted ?: 0}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = QueueActiveGreen
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Waiting", style = MaterialTheme.typography.labelSmall, color = CareTextMuted)
                                    val waiting = ((queue?.totalTokensIssued ?: 0) - (queue?.currentToken ?: 0)).coerceAtLeast(0)
                                    Text(
                                        text = "$waiting",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = QueueWaitingAmber
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Next Patient Action Button
                            Button(
                                onClick = onCallNextPatient,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CarePrimary),
                                enabled = doctor.isInOPD && !doctor.isOnBreak
                            ) {
                                Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Call Next Patient (#${(queue?.currentToken ?: 0) + 1})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Currently Serving Patient Details & Prescription Entry
                item {
                    Text(
                        text = "Current Consultation Room",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CareTextPrimary
                    )
                }

                item {
                    if (currentPatient != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = CareSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, QueueServingCyan.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(QueueServingCyanContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = QueueServingCyan,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = currentPatient.patientName,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = CareTextPrimary
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Phone,
                                                    contentDescription = null,
                                                    tint = CareTextMuted,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = currentPatient.patientPhone,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = CareTextSecondary
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = QueueServingCyan
                                    ) {
                                        Text(
                                            text = "Token #${currentPatient.tokenNumber}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Patient Symptoms Box
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = CareSurfaceVariant,
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Patient Symptoms / Reason for Visit:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = CarePrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = currentPatient.symptoms.ifBlank { currentPatient.notes.ifBlank { "Routine consultation / Checkup" } },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CareTextPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Doctor's Notes / Prescription Input
                                OutlinedTextField(
                                    value = visitNotes,
                                    onValueChange = { visitNotes = it },
                                    label = { Text("Clinical Notes / Prescription / Advice") },
                                    placeholder = { Text("Enter diagnosis, prescription, or clinical notes...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CarePrimary,
                                        unfocusedBorderColor = CardBorderSubtle,
                                        focusedContainerColor = CareSurface,
                                        unfocusedContainerColor = CareSurface
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Action buttons: Complete Consultation & No-Show
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onMarkNoShow,
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, QueueCancelledRed.copy(alpha = 0.5f))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = QueueCancelledRed)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("No-Show", color = QueueCancelledRed, fontWeight = FontWeight.SemiBold)
                                    }

                                    Button(
                                        onClick = {
                                            onCompleteConsultation(visitNotes)
                                            visitNotes = ""
                                        },
                                        modifier = Modifier.weight(1.5f).height(46.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = QueueActiveGreen)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Complete Consultation", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = CareSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(PastelMint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = QueueActiveGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Consultation room is ready",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CareTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap 'Call Next Patient' above or select any patient from the queue below.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CareTextMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Today's Waiting Patients
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Patient Queue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CareTextPrimary
                        )
                        TextButton(onClick = { showResetDialog = true }) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = CareTextMuted)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Counter", color = CareTextMuted)
                        }
                    }
                }

                val waitingList = uiState.todayAppointments.sortedBy { it.tokenNumber }
                if (waitingList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CareSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
                        ) {
                            Text(
                                text = "No appointments booked for today yet. Patient bookings will appear here in real time.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CareTextMuted,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }
                } else {
                    items(waitingList) { apt ->
                        val isCurrentlyServing = apt.id == currentPatient?.id
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentlyServing) PastelSky else CareSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCurrentlyServing) CarePrimary else CardBorderSubtle
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isCurrentlyServing) CarePrimary else CarePrimaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "#${apt.tokenNumber}",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrentlyServing) Color.White else CarePrimary,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = apt.patientName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CareTextPrimary
                                        )
                                        Text(
                                            text = "${apt.timeSlot} • ${apt.patientPhone}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CareTextMuted
                                        )
                                        if (apt.symptoms.isNotBlank()) {
                                            Text(
                                                text = "Symptom: ${apt.symptoms}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CareTextSecondary
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusChip(status = apt.status)
                                    if (apt.status == AppointmentStatus.BOOKED || apt.status == AppointmentStatus.WAITING) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { onCallSpecificPatient(apt) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CarePrimary)
                                        ) {
                                            Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Break Dialog
    if (showBreakDialog) {
        AlertDialog(
            onDismissRequest = { showBreakDialog = false },
            title = {
                Text(
                    text = "Take an OPD Break",
                    fontWeight = FontWeight.Bold,
                    color = CareTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Patients in queue will be notified that you are temporarily on break with your return estimate:",
                        color = CareTextSecondary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetBreakReasons.forEach { reason ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (breakMessage == reason) QueueWaitingAmberContainer else CareSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (breakMessage == reason) QueueWaitingAmber else CardBorderSubtle
                                ),
                                modifier = Modifier.clickable { breakMessage = reason }
                            ) {
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (breakMessage == reason) QueueWaitingAmber else CareTextSecondary,
                                    fontWeight = if (breakMessage == reason) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = breakMessage,
                        onValueChange = { breakMessage = it },
                        label = { Text("Break Reason / Expected Return") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleBreakMode(true, breakMessage)
                        showBreakDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = QueueWaitingAmber),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Start Break", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBreakDialog = false }) {
                    Text("Cancel", color = CareTextSecondary)
                }
            }
        )
    }

    // Reset Queue Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset Queue Counter?",
                    fontWeight = FontWeight.Bold,
                    color = CareTextPrimary
                )
            },
            text = {
                Text(
                    text = "This will reset today's serving token counter back to 0. Use this only at the end of the shift or before opening the next shift.",
                    color = CareTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetQueue()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reset Counter", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = CareTextSecondary)
                }
            }
        )
    }
}

