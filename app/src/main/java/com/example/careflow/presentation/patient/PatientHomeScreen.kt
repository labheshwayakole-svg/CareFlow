package com.example.careflow.presentation.patient

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.model.Hospital
import com.example.careflow.domain.model.Notification
import com.example.careflow.presentation.components.RoleBadge
import com.example.careflow.presentation.components.TokenBadge
import com.example.careflow.theme.QueueActiveGreen
import com.example.careflow.theme.QueueServingCyan
import com.example.careflow.theme.QueueWaitingAmber

@Composable
fun PatientHomeScreen(
    uiState: PatientUiState,
    onSelectHospital: (Hospital) -> Unit,
    onChangeHospitalClick: () -> Unit = {},
    onSelectDepartment: (String?) -> Unit,
    onToggleFilterInOpd: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectDoctorForBooking: (Doctor) -> Unit,
    onOpenQueueTracker: () -> Unit,
    onOpenMyAppointments: () -> Unit,
    onMarkNotificationRead: (String) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHospitalDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    val unreadNotifs = uiState.notifications.count { !it.isRead }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Greeting and Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${uiState.currentUser?.name ?: "Patient"}! 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    uiState.currentUser?.let { RoleBadge(role = it.role) }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Notification Bell with Badge
                    IconButton(onClick = { showNotificationsDialog = true }) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifs > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text("$unreadNotifs")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = if (unreadNotifs > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onOpenMyAppointments) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "My Appointments",
                            tint = MaterialTheme.colorScheme.primary
                        )
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

        // Selected Hospital Selector Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChangeHospitalClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Hospital & OPD Center",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = uiState.selectedHospital?.name ?: "No Hospital Registered",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (uiState.selectedHospital != null) {
                                    "${uiState.selectedHospital.opdName.ifBlank { "Main OPD" }} • ${uiState.selectedHospital.city}"
                                } else {
                                    "Tap to select or register hospital in Admin"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Switch", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Live Queue Banner (if patient has an active appointment)
        if (uiState.activeQueueEstimate != null && uiState.activeTrackingAppointment != null) {
            item {
                val estimate = uiState.activeQueueEstimate
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenQueueTracker() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (estimate.isYourTurn) QueueActiveGreen.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (estimate.isYourTurn) QueueActiveGreen else QueueServingCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (estimate.isYourTurn) "YOU'RE NEXT! STEP IN" else "LIVE QUEUE TRACKER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (estimate.isYourTurn) QueueActiveGreen else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Dr. ${estimate.doctorName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${estimate.tokensAhead} patients ahead • ~${estimate.estimatedWaitMinutes} min wait",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TokenBadge(
                            tokenNumber = estimate.patientToken,
                            label = "Your Token",
                            isHighlighted = estimate.isYourTurn
                        )
                    }
                }
            }
        }

        // Search Bar & OPD Live Filter
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search doctor, specialty, or condition...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clinical Departments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    FilterChip(
                        selected = uiState.filterOnlyInOpd,
                        onClick = onToggleFilterInOpd,
                        label = { Text("🟢 In OPD Now") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = QueueActiveGreen.copy(alpha = 0.2f),
                            selectedLabelColor = QueueActiveGreen
                        )
                    )
                }
            }
        }

        // Department Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedDepartmentId == null,
                        onClick = { onSelectDepartment(null) },
                        label = { Text("All Specialties") }
                    )
                }
                items(uiState.departments) { dept ->
                    FilterChip(
                        selected = uiState.selectedDepartmentId == dept.id,
                        onClick = { onSelectDepartment(dept.id) },
                        label = { Text(dept.name) }
                    )
                }
            }
        }

        // Doctors Header
        item {
            Text(
                text = "Available Doctors (${uiState.doctors.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Doctors List
        if (uiState.doctors.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.selectedHospital == null) "No hospital registered yet. Please setup your hospital in the Admin Dashboard."
                            else if (uiState.filterOnlyInOpd) "No doctors currently marked 'In OPD' in this department."
                            else "No doctors found in this hospital yet. Doctors will appear here once added in the Admin Dashboard.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(uiState.doctors) { doctor ->
                DoctorPatientCard(
                    doctor = doctor,
                    onBookClick = { onSelectDoctorForBooking(doctor) }
                )
            }
        }
    }

    // Hospital Switcher Dialog
    if (showHospitalDialog) {
        AlertDialog(
            onDismissRequest = { showHospitalDialog = false },
            title = { Text("Select Hospital", fontWeight = FontWeight.Bold) },
            text = {
                if (uiState.hospitals.isEmpty()) {
                    Text(
                        text = "No hospitals registered yet. Please switch to Admin mode and setup your hospital.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.hospitals) { hosp ->
                            val isSelected = hosp.id == uiState.selectedHospital?.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectHospital(hosp)
                                        showHospitalDialog = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Business,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(hosp.name, fontWeight = FontWeight.Bold)
                                        Text("${hosp.opdName.ifBlank { "Main OPD" }} • ${hosp.city}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHospitalDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Notifications Dialog / Center
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notifications", fontWeight = FontWeight.Bold)
                    if (unreadNotifs > 0) {
                        TextButton(onClick = { onMarkAllNotificationsRead() }) {
                            Text("Mark all read", fontSize = 12.sp)
                        }
                    }
                }
            },
            text = {
                if (uiState.notifications.isEmpty()) {
                    Text(
                        text = "No notifications yet. You will receive 1-hour reminders, 30-min reminders, doctor break updates, and call alerts here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.notifications) { notif ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMarkNotificationRead(notif.id) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!notif.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(notif.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        if (!notif.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(notif.body, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun DoctorPatientCard(
    doctor: Doctor,
    onBookClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = doctor.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${doctor.specialization} • ${doctor.departmentName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${doctor.qualification} • ${doctor.experienceYears} yrs exp",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live OPD Presence Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = when {
                        doctor.isOnBreak -> QueueWaitingAmber.copy(alpha = 0.2f)
                        doctor.isInOPD -> QueueActiveGreen.copy(alpha = 0.2f)
                        else -> Color.Gray.copy(alpha = 0.15f)
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
                                doctor.isInOPD -> "IN OPD NOW"
                                else -> "OFFLINE"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                doctor.isOnBreak -> QueueWaitingAmber
                                doctor.isInOPD -> QueueActiveGreen
                                else -> Color.DarkGray
                            }
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${doctor.shiftStartTime} - ${doctor.shiftEndTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "₹${doctor.consultationFee.toInt()} consultation fee",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onBookClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = doctor.isAvailable
                ) {
                    Text(
                        text = if (doctor.isAvailable) "Book Slot" else "Full / Paused",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
