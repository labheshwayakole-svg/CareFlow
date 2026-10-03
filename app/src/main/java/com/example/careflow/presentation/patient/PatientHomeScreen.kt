package com.example.careflow.presentation.patient

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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.model.Hospital
import com.example.careflow.presentation.components.CareOutlinePillButton
import com.example.careflow.presentation.components.CarePulseButton
import com.example.careflow.presentation.components.DigitalTokenPass
import com.example.careflow.presentation.components.LivePulseDot
import com.example.careflow.presentation.components.LiveStatusPill
import com.example.careflow.presentation.components.LuminousHeroCard
import com.example.careflow.presentation.components.RoleBadge
import com.example.careflow.presentation.components.StatusChip
import com.example.careflow.theme.CardBorderSubtle
import com.example.careflow.theme.CareBackground
import com.example.careflow.theme.CarePrimary
import com.example.careflow.theme.CarePrimaryContainer
import com.example.careflow.theme.PastelAmber
import com.example.careflow.theme.PastelMint
import com.example.careflow.theme.PulseCoral
import com.example.careflow.theme.QueueActiveGreen
import com.example.careflow.theme.QueueCancelledRed
import com.example.careflow.theme.PulseCrimsonDark
import com.example.careflow.theme.PulseLiveGreen
import com.example.careflow.theme.PulseLiveGreenBg
import com.example.careflow.theme.PulseLiveGreenBorder
import com.example.careflow.theme.PulseRed
import com.example.careflow.theme.PulseRedBright
import com.example.careflow.theme.PulseRoseBg
import com.example.careflow.theme.PulseRoseBorder
import com.example.careflow.theme.PulseShadow
import com.example.careflow.theme.QueueActiveGreen
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
    onOpenNotifications: () -> Unit,
    onMarkNotificationRead: (String) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    val unreadNotifs = uiState.notifications.count { !it.isRead }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CareBackground)
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.careflow.R.drawable.ic_app_logo),
                    contentDescription = "CareFlow Logo",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hello, ${uiState.currentUser?.name ?: "Patient"} 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    uiState.currentUser?.let { RoleBadge(role = it.role) }
                }
            }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenNotifications) {
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
                                tint = if (unreadNotifs > 0) CarePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onOpenMyAppointments) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "My Appointments",
                            tint = CarePrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEE2E2),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.clickable { showLogoutConfirmDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Logout",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // Active Hospital Hero Banner (HemoPulse Luminous Card)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChangeHospitalClick() }
                    .border(1.dp, CardBorderSubtle, RoundedCornerShape(24.dp))
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp), spotColor = PulseShadow),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                            listOf(PulseCrimsonDark, PulseRed, PulseCoral)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "SELECTED OPD CAMPUS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PulseCrimsonDark,
                                        letterSpacing = 0.8.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = PulseCrimsonDark, modifier = Modifier.size(14.dp))
                                }
                                Text(
                                    text = uiState.selectedHospital?.name ?: "No Hospital Selected",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }

                        LiveStatusPill(
                            text = "OPD ACTIVE",
                            isLive = true,
                            accentColor = PulseLiveGreen,
                            backgroundColor = PulseLiveGreenBg,
                            borderColor = PulseLiveGreenBorder
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.selectedHospital != null) {
                                "${uiState.selectedHospital.opdName.ifBlank { "Main OPD Complex" }} • ${uiState.selectedHospital.city}"
                            } else {
                                "Tap to select a hospital"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        CareOutlinePillButton(
                            text = "Switch Hospital",
                            onClick = { onChangeHospitalClick() },
                            textColor = PulseCrimsonDark,
                            borderColor = PulseRoseBorder,
                            modifier = Modifier.height(36.dp)
                        )
                    }
                }
            }
        }

        // Live Queue Boarding Pass (ONLY if patient has an active appointment for THIS hospital)
        val currentHospId = uiState.selectedHospital?.id
        val apt = uiState.activeTrackingAppointment
        val estimate = uiState.activeQueueEstimate
        if (estimate != null && apt != null && currentHospId != null && apt.hospitalId == currentHospId) {
            item {
                val isTurnOver = apt.status == AppointmentStatus.COMPLETED ||
                    apt.status == AppointmentStatus.CANCELLED ||
                    apt.status == AppointmentStatus.NO_SHOW ||
                    (estimate.currentCallingToken > estimate.patientToken && estimate.currentCallingToken > 0 && apt.status != AppointmentStatus.WAITING && apt.status != AppointmentStatus.CALLED && apt.status != AppointmentStatus.IN_CONSULTATION)

                var showFullTrackDetails by rememberSaveable(apt.id, apt.status) { mutableStateOf(!isTurnOver) }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE TOKEN STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CarePrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (!isTurnOver) {
                                "Tap to view full tracker →"
                            } else if (showFullTrackDetails) {
                                "Hide details ▲"
                            } else {
                                "Tap to view full tracker →"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = CarePrimary,
                            modifier = Modifier.clickable {
                                if (isTurnOver) {
                                    showFullTrackDetails = !showFullTrackDetails
                                } else {
                                    onOpenQueueTracker()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isTurnOver) {
                        // Patient turn is actively in queue / being called: show full pass directly
                        DigitalTokenPass(
                            hospitalName = apt.hospitalName.ifBlank { uiState.selectedHospital?.name ?: "Hospital OPD" },
                            opdRoom = "Chamber ${estimate.roomNumber.ifBlank { "102" }}",
                            doctorName = estimate.doctorName,
                            tokenNumber = estimate.patientToken,
                            currentCallingToken = estimate.currentCallingToken,
                            estimatedWaitMinutes = estimate.estimatedWaitMinutes,
                            status = apt.status,
                            modifier = Modifier.clickable { onOpenQueueTracker() }
                        )
                    } else {
                        // Patient turn is over: Automatically converts into a sleek button/card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showFullTrackDetails = !showFullTrackDetails
                                },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (apt.status == AppointmentStatus.COMPLETED) {
                                    Color(0xFFF0FDF4)
                                } else {
                                    Color(0xFFFEF2F2)
                                }
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (apt.status == AppointmentStatus.COMPLETED) {
                                    QueueActiveGreen.copy(alpha = 0.35f)
                                } else {
                                    QueueCancelledRed.copy(alpha = 0.35f)
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(
                                                    color = if (apt.status == AppointmentStatus.COMPLETED) {
                                                        QueueActiveGreen.copy(alpha = 0.15f)
                                                    } else {
                                                        QueueCancelledRed.copy(alpha = 0.15f)
                                                    },
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (apt.status == AppointmentStatus.COMPLETED) {
                                                    Icons.Default.CheckCircle
                                                } else {
                                                    Icons.Default.Cancel
                                                },
                                                contentDescription = null,
                                                tint = if (apt.status == AppointmentStatus.COMPLETED) {
                                                    QueueActiveGreen
                                                } else {
                                                    QueueCancelledRed
                                                },
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Token #${estimate.patientToken}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                StatusChip(status = apt.status)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (apt.status == AppointmentStatus.COMPLETED) {
                                                    "Turn Finished • Dr. ${estimate.doctorName}"
                                                } else {
                                                    "Dr. ${estimate.doctorName} • Chamber ${estimate.roomNumber.ifBlank { "102" }}"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = if (showFullTrackDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = if (apt.status == AppointmentStatus.COMPLETED) QueueActiveGreen else CarePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Button 1: Tap to reveal full track details inline
                                    Surface(
                                        onClick = { showFullTrackDetails = !showFullTrackDetails },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (apt.status == AppointmentStatus.COMPLETED) {
                                            QueueActiveGreen.copy(alpha = 0.12f)
                                        } else {
                                            QueueCancelledRed.copy(alpha = 0.12f)
                                        },
                                        border = BorderStroke(
                                            1.dp,
                                            if (apt.status == AppointmentStatus.COMPLETED) QueueActiveGreen.copy(alpha = 0.35f) else QueueCancelledRed.copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier.weight(1.1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (showFullTrackDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = if (apt.status == AppointmentStatus.COMPLETED) QueueActiveGreen else QueueCancelledRed,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (showFullTrackDetails) "Hide Track Details" else "Tap for Track Details",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (apt.status == AppointmentStatus.COMPLETED) QueueActiveGreen else QueueCancelledRed,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    // Button 2: Navigate directly to Full Live Tracker Screen
                                    Surface(
                                        onClick = { onOpenQueueTracker() },
                                        shape = RoundedCornerShape(10.dp),
                                        color = CarePrimary,
                                        modifier = Modifier.weight(0.9f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Full Tracker",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Shows full track details ONLY when user taps the button
                        AnimatedVisibility(
                            visible = showFullTrackDetails,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                DigitalTokenPass(
                                    hospitalName = apt.hospitalName.ifBlank { uiState.selectedHospital?.name ?: "Hospital OPD" },
                                    opdRoom = "Chamber ${estimate.roomNumber.ifBlank { "102" }}",
                                    doctorName = estimate.doctorName,
                                    tokenNumber = estimate.patientToken,
                                    currentCallingToken = estimate.currentCallingToken,
                                    estimatedWaitMinutes = estimate.estimatedWaitMinutes,
                                    status = apt.status,
                                    modifier = Modifier.clickable { onOpenQueueTracker() }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Bar & OPD Live Filter
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search doctor, specialty, or problem...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CarePrimary) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = CarePrimary,
                        unfocusedBorderColor = CardBorderSubtle
                    ),
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FilterChip(
                        selected = uiState.filterOnlyInOpd,
                        onClick = onToggleFilterInOpd,
                        label = { Text("🟢 In OPD Chamber", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PastelMint,
                            selectedLabelColor = QueueActiveGreen,
                            containerColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.filterOnlyInOpd,
                            borderColor = if (uiState.filterOnlyInOpd) QueueActiveGreen else CardBorderSubtle
                        )
                    )
                }
            }
        }

        // Department Chips Carousel
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                item {
                    val isAllSelected = uiState.selectedDepartmentId == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { onSelectDepartment(null) },
                        label = { Text("All Specialties", fontSize = 12.sp, fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CarePrimaryContainer,
                            selectedLabelColor = CarePrimary,
                            containerColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isAllSelected,
                            borderColor = if (isAllSelected) CarePrimary else CardBorderSubtle
                        )
                    )
                }
                items(uiState.departments, key = { it.id }) { dept ->
                    val isSelected = uiState.selectedDepartmentId == dept.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectDepartment(dept.id) },
                        label = { Text(dept.name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CarePrimaryContainer,
                            selectedLabelColor = CarePrimary,
                            containerColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) CarePrimary else CardBorderSubtle
                        )
                    )
                }
            }
        }

        // Doctors Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AVAILABLE DOCTORS (${uiState.doctors.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
        }

        // Doctors List
        if (uiState.doctors.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorderSubtle, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(CarePrimaryContainer.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = CarePrimary, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (uiState.selectedHospital == null) "No hospital selected."
                            else if (uiState.filterOnlyInOpd) "No doctors currently in OPD chamber."
                            else "No doctors listed in this hospital yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.selectedHospital == null) "Please select a hospital to view doctors."
                            else "Doctors will appear here once added in the Admin Dashboard.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(uiState.doctors, key = { it.id }) { doctor ->
                DoctorPatientCard(
                    doctor = doctor,
                    onBookClick = { onSelectDoctorForBooking(doctor) }
                )
            }
        }
    }

    // Notifications Dialog
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
                            Text("Mark all read", fontSize = 12.sp, color = CarePrimary)
                        }
                    }
                }
            },
            text = {
                if (uiState.notifications.isEmpty()) {
                    Text(
                        text = "No notifications yet. You will receive 1-hour reminders, 30-min reminders, doctor break updates, and call alerts here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.notifications, key = { it.id }) { notif ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMarkNotificationRead(notif.id) }
                                    .border(1.dp, CardBorderSubtle, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!notif.isRead) CarePrimaryContainer.copy(alpha = 0.35f) else Color.White
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
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
                                                    .background(CarePrimary)
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
                    Text("Close", color = CarePrimary)
                }
            }
        )
    }

    // Patient Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "Log Out?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of your Patient account?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderSubtle, RoundedCornerShape(22.dp))
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp), spotColor = Color(0x140F172A)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Doctor avatar with soft rose background & crimson initial
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PulseRoseBg)
                            .border(1.dp, PulseRoseBorder, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = doctor.name.replace("Dr.", "").trim().take(2).uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            color = PulseCrimsonDark,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = doctor.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = null, tint = PulseLiveGreen, modifier = Modifier.size(15.dp))
                        }
                        Text(
                            text = "${doctor.specialization} • ${doctor.departmentName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PulseCrimsonDark,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${doctor.qualification} • ${doctor.experienceYears} yrs exp",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PastelAmber
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = QueueWaitingAmber, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = String.format(java.util.Locale.getDefault(), "%.1f", if (doctor.rating > 0.0) doctor.rating else 4.9),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = QueueWaitingAmber,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Live OPD Presence Pill with animated pulse
                LiveStatusPill(
                    text = when {
                        doctor.isOnBreak -> "ON BREAK"
                        doctor.isInOPD -> "IN CHAMBER"
                        else -> "OFFLINE"
                    },
                    isLive = doctor.isInOPD,
                    accentColor = when {
                        doctor.isOnBreak -> QueueWaitingAmber
                        doctor.isInOPD -> PulseLiveGreen
                        else -> Color.Gray
                    },
                    backgroundColor = when {
                        doctor.isOnBreak -> PastelAmber
                        doctor.isInOPD -> PulseLiveGreenBg
                        else -> Color(0xFFF1F5F9)
                    },
                    borderColor = when {
                        doctor.isOnBreak -> QueueWaitingAmber.copy(alpha = 0.4f)
                        doctor.isInOPD -> PulseLiveGreenBorder
                        else -> CardBorderSubtle
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chamber Room & Consultation Timing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFD), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(15.dp), tint = PulseCrimsonDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Chamber Room ${doctor.roomNumber.ifBlank { "102" }}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = PulseCrimsonDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF64748B))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = com.example.careflow.domain.util.BookingTimeUtils.formatDisplayShift(doctor.shiftStartTime, doctor.shiftEndTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fee and HemoPulse Gradient Pill Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CONSULTATION FEE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "₹${doctor.consultationFee.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }

                CarePulseButton(
                    text = if (doctor.isAvailable) "Book OPD Token" else "Chamber Full",
                    onClick = onBookClick,
                    enabled = doctor.isAvailable,
                    modifier = Modifier.height(42.dp)
                )
            }
        }
    }
}
