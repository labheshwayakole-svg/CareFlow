package com.example.careflow.presentation.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
import com.example.careflow.presentation.components.CareOutlinePillButton
import com.example.careflow.presentation.components.CarePulseButton
import com.example.careflow.presentation.components.LiveStatusPill
import com.example.careflow.presentation.components.StatCard
import com.example.careflow.presentation.components.StatusChip
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
import com.example.careflow.theme.PulseCrimsonDark
import com.example.careflow.theme.PulseRed
import com.example.careflow.theme.PulseRoseBg
import com.example.careflow.theme.PulseRoseBorder
import com.example.careflow.theme.QueueActiveGreen
import com.example.careflow.theme.QueueServingCyan
import com.example.careflow.theme.QueueServingCyanContainer
import com.example.careflow.theme.QueueWaitingAmber
import com.example.careflow.theme.QueueWaitingAmberContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    uiState: AdminUiState,
    onSaveHospital: (String, String, String, String, String) -> Unit,
    onToggleDoctorAvailability: (String, Boolean) -> Unit,
    onAddDoctor: (String, String, String, String, String, Int, Double, String, String, Int) -> Unit,
    onAddDepartment: (String, String, String) -> Unit,
    onDismissCredentials: () -> Unit,
    onLogoutClick: () -> Unit,
    onClearMessages: () -> Unit,
    onDismissDoctorResetRequest: (String) -> Unit = {},
    onRemoveDoctor: (String) -> Unit = {},
    onUpdateAdminEmail: (String) -> Unit = {},
    onUpdateAdminAndHospitalProfile: (String, String, String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview & Hospital, 1: Doctors, 2: Departments, 3: Live Queues

    var showEditHospitalDialog by remember { mutableStateOf(false) }
    var showAdminEmailDialog by remember { mutableStateOf(false) }
    var showAddDoctorDialog by remember { mutableStateOf(false) }
    var showAddDeptDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

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
        containerColor = CareBackground,
        floatingActionButton = {
            when (selectedTab) {
                1 -> FloatingActionButton(
                    onClick = { showAddDoctorDialog = true },
                    containerColor = PulseRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Doctor")
                }
                2 -> FloatingActionButton(
                    onClick = { showAddDeptDialog = true },
                    containerColor = PulseRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Department")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp,
                color = CareSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PulseRoseBg)
                                .border(1.dp, PulseRoseBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = PulseRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.hospital?.name ?: "CareFlow Multi-Specialty Hospital",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CareTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val adminNameDisplay = uiState.currentAdmin?.name?.ifBlank { null }
                                ?: uiState.hospital?.adminName?.ifBlank { null }
                                ?: "Administrator"
                            val opdNameDisplay = uiState.hospital?.opdName?.ifBlank { "Main OPD" } ?: "Main OPD"
                            Text(
                                text = "OPD: $opdNameDisplay • Admin: $adminNameDisplay",
                                style = MaterialTheme.typography.bodySmall,
                                color = PulseRed,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showEditHospitalDialog = true }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Admin & Hospital Settings", tint = PulseRed)
                        }
                        IconButton(onClick = { showEditHospitalDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile & Hospital", tint = PulseRed)
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

            // Tab Navigation
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = CareSurface,
                contentColor = PulseRed
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Overview",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Doctors (${uiState.doctors.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "Depts (${uiState.departments.size})",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            text = "Live Queues",
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }

            // Tab Contents
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> OverviewTab(
                        uiState = uiState,
                        onEditHospitalClick = { showEditHospitalDialog = true },
                        onUpdateEmailClick = { showAdminEmailDialog = true },
                        onDismissDoctorResetRequest = onDismissDoctorResetRequest
                    )
                    1 -> DoctorsTab(
                        doctors = uiState.doctors,
                        activeQueues = uiState.activeQueues,
                        onToggleDoctorAvailability = onToggleDoctorAvailability,
                        onRemoveDoctor = onRemoveDoctor
                    )
                    2 -> DepartmentsTab(
                        departments = uiState.departments,
                        doctors = uiState.doctors
                    )
                    3 -> LiveQueuesTab(
                        activeQueues = uiState.activeQueues,
                        appointments = uiState.appointments,
                        doctors = uiState.doctors
                    )
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. Credentials Dialog (After Doctor Creation)
    uiState.generatedCredentials?.let { creds ->
        AlertDialog(
            onDismissRequest = onDismissCredentials,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = QueueActiveGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Doctor Account Created!", fontWeight = FontWeight.Bold, color = CareTextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Share these login credentials with Dr. ${creds.doctorName} so they can sign in to the Doctor portal:",
                        color = CareTextSecondary
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CareSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Email:", style = MaterialTheme.typography.labelSmall, color = CareTextMuted)
                            Text(text = creds.loginEmail, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = CareTextPrimary)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = "Temporary Password:", style = MaterialTheme.typography.labelSmall, color = CareTextMuted)
                            Text(text = creds.temporaryPassword, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = CarePrimary)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Doctor Credentials", "Email: ${creds.loginEmail}\nPassword: ${creds.temporaryPassword}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Credentials copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CarePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Credentials to Clipboard", color = CarePrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissCredentials,
                    colors = ButtonDefaults.buttonColors(containerColor = CarePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Done", color = Color.White)
                }
            }
        )
    }

    // 2. Edit Admin Profile & Hospital Settings Dialog
    if (showEditHospitalDialog) {
        var adminName by remember { mutableStateOf(uiState.currentAdmin?.name ?: uiState.hospital?.adminName ?: "") }
        var adminEmail by remember { mutableStateOf(uiState.currentAdmin?.email ?: uiState.hospital?.email ?: "") }
        var adminPhone by remember { mutableStateOf(uiState.currentAdmin?.phone ?: uiState.hospital?.adminPhone ?: "") }
        var hospName by remember { mutableStateOf(uiState.hospital?.name ?: "") }
        var opdName by remember { mutableStateOf(uiState.hospital?.opdName ?: "") }
        var address by remember { mutableStateOf(uiState.hospital?.address ?: "") }
        var city by remember { mutableStateOf(uiState.hospital?.city ?: "") }
        var phone by remember { mutableStateOf(uiState.hospital?.phone ?: "") }
        val editHospitalScrollState = rememberScrollState()

        Dialog(
            onDismissRequest = { showEditHospitalDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .fillMaxHeight(0.90f),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    tonalElevation = 6.dp,
                    shadowElevation = 16.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PulseRoseBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = PulseRed, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Admin & Hospital Settings",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CareTextPrimary
                                    )
                                    Text(
                                        text = "Manage your admin profile & hospital information",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CareTextSecondary
                                    )
                                }
                            }
                            IconButton(onClick = { showEditHospitalDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = CareTextSecondary)
                            }
                        }

                        HorizontalDivider(color = CardBorderSubtle)

                        // Scrollable content area
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(editHospitalScrollState)
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Section 1: Administrator Profile
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = CareSurfaceVariant,
                                border = BorderStroke(1.dp, CardBorderSubtle)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = CarePrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Administrator Profile",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = CareTextPrimary
                                        )
                                    }

                                    OutlinedTextField(
                                        value = adminName,
                                        onValueChange = { adminName = it },
                                        label = { Text("Administrator Full Name") },
                                        placeholder = { Text("e.g. Dr. Sarfaraz Shaikh") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CarePrimary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = adminEmail,
                                        onValueChange = { adminEmail = it },
                                        label = { Text("Admin Login Email") },
                                        placeholder = { Text("e.g. admin@careflow.com") },
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CarePrimary) },
                                        supportingText = {
                                            Text(
                                                "⚠️ Future logins and password resets will use this email.",
                                                color = CarePrimary,
                                                fontSize = 11.sp
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = adminPhone,
                                        onValueChange = { adminPhone = it },
                                        label = { Text("Admin Personal Phone") },
                                        placeholder = { Text("e.g. +91 7219889504") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = CarePrimary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }

                            // Section 2: Hospital Facility Details
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = CareSurfaceVariant,
                                border = BorderStroke(1.dp, CardBorderSubtle)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = PulseRed, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Hospital & OPD Facility Details",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = CareTextPrimary
                                        )
                                    }

                                    OutlinedTextField(
                                        value = hospName,
                                        onValueChange = { hospName = it },
                                        label = { Text("Hospital / Clinic Name") },
                                        placeholder = { Text("e.g. CareFlow Multi-Specialty Hospital") },
                                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = PulseRed) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = opdName,
                                        onValueChange = { opdName = it },
                                        label = { Text("OPD Name (e.g. Smart OPD Block A)") },
                                        placeholder = { Text("e.g. Smart OPD Block A") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = address,
                                        onValueChange = { address = it },
                                        label = { Text("Hospital Address / Landmark") },
                                        placeholder = { Text("e.g. MG Road, Near Metro Station") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = city,
                                        onValueChange = { city = it },
                                        label = { Text("City") },
                                        placeholder = { Text("e.g. Mumbai") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { phone = it },
                                        label = { Text("Hospital Emergency / Helpdesk Phone") },
                                        placeholder = { Text("e.g. +91 98765 43210") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PulseRed) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = CardBorderSubtle)

                        // Action buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showEditHospitalDialog = false }) {
                                Text("Cancel", color = CareTextSecondary, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (hospName.isNotBlank() && adminName.isNotBlank() && adminEmail.isNotBlank()) {
                                        onUpdateAdminAndHospitalProfile(
                                            adminName, adminEmail, adminPhone, hospName, opdName, address, city, phone
                                        )
                                        showEditHospitalDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CarePrimary),
                                shape = RoundedCornerShape(12.dp),
                                enabled = hospName.isNotBlank() && adminName.isNotBlank() && adminEmail.isNotBlank()
                            ) {
                                Text("Save All Changes", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dedicated Update Admin Email Dialog
    if (showAdminEmailDialog) {
        var newEmail by remember { mutableStateOf(uiState.currentAdmin?.email ?: "") }
        var emailError by remember { mutableStateOf<String?>(null) }

        Dialog(
            onDismissRequest = { showAdminEmailDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    tonalElevation = 6.dp,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PastelPeach),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = CarePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Admin Email Address",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CareTextPrimary
                                    )
                                    Text(
                                        text = "Add or update administrator email",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CareTextSecondary
                                    )
                                }
                            }
                            IconButton(onClick = { showAdminEmailDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = CareTextSecondary)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = CardBorderSubtle)

                        // Current email pill
                        if (!uiState.currentAdmin?.email.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CareSurfaceVariant,
                                border = BorderStroke(1.dp, CardBorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "CURRENT REGISTERED EMAIL",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CareTextMuted,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = uiState.currentAdmin?.email ?: "",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CareTextPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Input field
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = {
                                newEmail = it
                                emailError = null
                            },
                            label = { Text("New Admin Email Address") },
                            placeholder = { Text("e.g. admin@careflow.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = CarePrimary)
                            },
                            isError = emailError != null,
                            supportingText = {
                                if (emailError != null) {
                                    Text(emailError!!, color = PulseRed)
                                } else {
                                    Text("This email will be used for hospital admin access, notifications, and emergency alerts.", color = CareTextMuted)
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAdminEmailDialog = false }) {
                                Text("Cancel", color = CareTextSecondary, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val clean = newEmail.trim()
                                    if (clean.isBlank() || !clean.contains("@") || !clean.contains(".")) {
                                        emailError = "Please enter a valid email address (e.g. admin@hospital.com)"
                                    } else {
                                        onUpdateAdminEmail(clean)
                                        showAdminEmailDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CarePrimary),
                                shape = RoundedCornerShape(12.dp),
                                enabled = newEmail.isNotBlank()
                            ) {
                                Text("Save New Email", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. Add Doctor Dialog (With Templates for Specialization, Department, and Qualification, plus fixed viewport scroll)
    if (showAddDoctorDialog) {
        var name by remember { mutableStateOf("") }
        var specialization by remember { mutableStateOf("") }
        var customDeptName by remember { mutableStateOf("") }
        var selectedDept by remember { mutableStateOf(uiState.departments.firstOrNull()) }
        var qualification by remember { mutableStateOf("") }
        var experienceYears by remember { mutableStateOf("5") }
        var fee by remember { mutableStateOf("500") }
        var shiftStart by remember { mutableStateOf("09:00 AM") }
        var shiftEnd by remember { mutableStateOf("02:00 PM") }
        var maxTokens by remember { mutableStateOf("25") }
        var deptDropdownExpanded by remember { mutableStateOf(false) }
        val addDoctorScrollState = rememberScrollState()

        val specializationTemplates = listOf(
            "General Physician",
            "Cardiologist",
            "Orthopedic Surgeon",
            "Pediatrician",
            "Dermatologist",
            "Neurologist",
            "Gynecologist & Obstetrician",
            "ENT Specialist",
            "Ophthalmologist",
            "General Surgeon",
            "Psychiatrist",
            "Dentist",
            "Pulmonologist",
            "Urologist",
            "Radiologist",
            "Endocrinologist",
            "Gastroenterologist"
        )

        val qualificationTemplates = listOf(
            "MBBS, MD",
            "MBBS, MS",
            "MBBS, DNB",
            "MBBS",
            "DM (Specialist)",
            "MCh (Surgeon)",
            "BDS, MDS",
            "MBBS, DCH",
            "MBBS, DGO",
            "MRCP (UK)",
            "FRCS"
        )

        val defaultDeptTemplates = listOf(
            "General Medicine OPD",
            "Cardiology OPD",
            "Pediatrics OPD",
            "Orthopedics OPD",
            "Dermatology Clinic",
            "Neurology OPD",
            "Gynecology & Obstetrics",
            "ENT Clinic",
            "Dental OPD",
            "Ophthalmology OPD",
            "Emergency Care",
            "Psychiatry OPD"
        )

        val allDepartmentChoices = remember(uiState.departments) {
            val fromExisting = uiState.departments.map { it.name }
            val combined = (fromExisting + defaultDeptTemplates).distinct()
            combined
        }

        Dialog(
            onDismissRequest = { showAddDoctorDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .fillMaxHeight(0.90f),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    tonalElevation = 6.dp,
                    shadowElevation = 16.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Add Doctor to OPD",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CareTextPrimary
                            )
                            Text(
                                text = "Fill doctor credentials, shift & OPD parameters",
                                style = MaterialTheme.typography.bodySmall,
                                color = CareTextSecondary
                            )
                        }
                        IconButton(onClick = { showAddDoctorDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = CareTextSecondary)
                        }
                    }

                    HorizontalDivider(color = CardBorderSubtle)

                    // Scrollable Form Content (Smooth standard vertical scroll, no jitter or bouncing)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(addDoctorScrollState)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Doctor Full Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Doctor Full Name") },
                            placeholder = { Text("e.g. Dr. Ramesh Patel") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // 2. Specialization + Templates
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = specialization,
                                onValueChange = { specialization = it },
                                label = { Text("Specialization") },
                                placeholder = { Text("e.g. Cardiologist or select template below") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Text(
                                text = "Specialization Templates (Tap to select):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CareTextSecondary
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(vertical = 2.dp)
                            ) {
                                items(specializationTemplates) { template ->
                                    val isSelected = specialization.equals(template, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isSelected) CarePrimary.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, if (isSelected) CarePrimary else Color(0xFFCBD5E1)),
                                        modifier = Modifier.clickable { specialization = template }
                                    ) {
                                        Text(
                                            text = template,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) CarePrimary else Color(0xFF334155),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Department + Templates
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (uiState.departments.isEmpty()) {
                                OutlinedTextField(
                                    value = customDeptName,
                                    onValueChange = { customDeptName = it },
                                    label = { Text("Department") },
                                    placeholder = { Text("e.g. Cardiology OPD or select template") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else {
                                ExposedDropdownMenuBox(
                                    expanded = deptDropdownExpanded,
                                    onExpandedChange = { deptDropdownExpanded = !deptDropdownExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = selectedDept?.name ?: customDeptName.ifBlank { "Select Department" },
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptDropdownExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        label = { Text("Department") },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = deptDropdownExpanded,
                                        onDismissRequest = { deptDropdownExpanded = false }
                                    ) {
                                        uiState.departments.forEach { dept ->
                                            DropdownMenuItem(
                                                text = { Text(dept.name) },
                                                onClick = {
                                                    selectedDept = dept
                                                    customDeptName = dept.name
                                                    deptDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Department Templates (Tap to select):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CareTextSecondary
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(vertical = 2.dp)
                            ) {
                                items(allDepartmentChoices) { deptTitle ->
                                    val currentName = selectedDept?.name ?: customDeptName
                                    val isSelected = currentName.equals(deptTitle, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isSelected) CarePrimary.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, if (isSelected) CarePrimary else Color(0xFFCBD5E1)),
                                        modifier = Modifier.clickable {
                                            val matching = uiState.departments.find { it.name.equals(deptTitle, ignoreCase = true) }
                                            if (matching != null) {
                                                selectedDept = matching
                                                customDeptName = matching.name
                                            } else {
                                                customDeptName = deptTitle
                                                selectedDept = null
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = deptTitle,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) CarePrimary else Color(0xFF334155),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Qualification + Templates
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = qualification,
                                    onValueChange = { qualification = it },
                                    label = { Text("Qualification") },
                                    placeholder = { Text("e.g. MBBS, MD") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = experienceYears,
                                    onValueChange = { experienceYears = it },
                                    label = { Text("Exp (Yrs)") },
                                    placeholder = { Text("5") },
                                    modifier = Modifier.weight(0.6f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Text(
                                text = "Qualification Templates (Tap to select):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CareTextSecondary
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(vertical = 2.dp)
                            ) {
                                items(qualificationTemplates) { template ->
                                    val isSelected = qualification.equals(template, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isSelected) CarePrimary.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, if (isSelected) CarePrimary else Color(0xFFCBD5E1)),
                                        modifier = Modifier.clickable { qualification = template }
                                    ) {
                                        Text(
                                            text = template,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) CarePrimary else Color(0xFF334155),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 5. Fee & Max Slots per Day
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = fee,
                                onValueChange = { fee = it },
                                label = { Text("Consultation Fee (₹)") },
                                placeholder = { Text("500") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = maxTokens,
                                onValueChange = { maxTokens = it },
                                label = { Text("Max Slots/Day") },
                                placeholder = { Text("25") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // 6. Shift Times
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = shiftStart,
                                onValueChange = { shiftStart = it },
                                label = { Text("Shift Start") },
                                placeholder = { Text("09:00 AM") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = shiftEnd,
                                onValueChange = { shiftEnd = it },
                                label = { Text("Shift End") },
                                placeholder = { Text("02:00 PM") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = CardBorderSubtle)

                    // Footer Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDoctorDialog = false }) {
                            Text("Cancel", color = CareTextSecondary, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val deptId = selectedDept?.id ?: ""
                                val deptName = selectedDept?.name ?: customDeptName.ifBlank { "General Medicine" }
                                if (name.isNotBlank()) {
                                    onAddDoctor(
                                        name,
                                        specialization.ifBlank { "General Physician" },
                                        deptId,
                                        deptName,
                                        qualification.ifBlank { "MBBS, MD" },
                                        experienceYears.toIntOrNull() ?: 1,
                                        fee.toDoubleOrNull() ?: 500.0,
                                        shiftStart,
                                        shiftEnd,
                                        maxTokens.toIntOrNull() ?: 20
                                    )
                                    showAddDoctorDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CarePrimary),
                            shape = RoundedCornerShape(12.dp),
                            enabled = name.isNotBlank()
                        ) {
                            Text("Create Doctor", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            }
        }
    }

    // 4. Add Department Dialog
    if (showAddDeptDialog) {
        var deptName by remember { mutableStateOf("") }
        var deptDesc by remember { mutableStateOf("") }
        val addDeptScrollState = rememberScrollState()

        val departmentPresets = listOf(
            "Cardiology" to "Heart, vascular health & specialized cardiology OPD",
            "General Medicine" to "Primary adult medicine, fever, diagnostics & consultations",
            "Pediatrics" to "Child healthcare, neonatal monitoring & vaccination",
            "Orthopedics" to "Bone, joint, fractures, spine & trauma clinic",
            "Dermatology" to "Skin, hair, allergy & cosmetic dermatology",
            "Neurology" to "Brain, spine, nerves & neurological disorders",
            "Gynecology & Obstetrics" to "Women's wellness, pregnancy & maternal care",
            "ENT Clinic" to "Ear, nose, throat & sinus treatment",
            "Ophthalmology" to "Eye examinations, cataract & vision care",
            "Dental Care" to "Oral surgery, orthodontics & dentistry",
            "Emergency Care" to "24/7 emergency, trauma & acute critical care",
            "Psychiatry" to "Mental wellness, therapy & psychological care"
        )

        Dialog(
            onDismissRequest = { showAddDeptDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.75f),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    tonalElevation = 6.dp,
                    shadowElevation = 16.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Add Hospital Department",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CareTextPrimary
                                )
                                Text(
                                    text = "Select template or create custom OPD department",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CareTextSecondary
                                )
                            }
                            IconButton(onClick = { showAddDeptDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = CareTextSecondary)
                            }
                        }

                        HorizontalDivider(color = CardBorderSubtle)

                        // Scrollable content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(addDeptScrollState)
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            OutlinedTextField(
                                value = deptName,
                                onValueChange = { deptName = it },
                                label = { Text("Department Name") },
                                placeholder = { Text("e.g. Cardiology OPD") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Text(
                                text = "Department Templates (Tap to fill):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CareTextSecondary
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(vertical = 2.dp)
                            ) {
                                items(departmentPresets) { (name, desc) ->
                                    val isSelected = deptName.equals(name, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isSelected) CarePrimary.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, if (isSelected) CarePrimary else Color(0xFFCBD5E1)),
                                        modifier = Modifier.clickable {
                                            deptName = name
                                            deptDesc = desc
                                        }
                                    ) {
                                        Text(
                                            text = name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) CarePrimary else Color(0xFF334155),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = deptDesc,
                                onValueChange = { deptDesc = it },
                                label = { Text("Description") },
                                placeholder = { Text("Brief description of OPD clinic facilities") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        HorizontalDivider(color = CardBorderSubtle)

                        // Footer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddDeptDialog = false }) {
                                Text("Cancel", color = CareTextSecondary, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (deptName.isNotBlank()) {
                                        onAddDepartment(deptName, deptDesc, "stethoscope")
                                        showAddDeptDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CarePrimary),
                                shape = RoundedCornerShape(12.dp),
                                enabled = deptName.isNotBlank()
                            ) {
                                Text("Add Department", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "Exit Admin Portal",
                    fontWeight = FontWeight.Bold,
                    color = CareTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of the Admin Portal? Your hospital details and doctor profiles remain securely saved in the database.",
                    color = CareTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = CareTextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

// ==========================================
// --- TAB 1: OVERVIEW & HOSPITAL ---
// ==========================================

@Composable
private fun OverviewTab(
    uiState: AdminUiState,
    onEditHospitalClick: () -> Unit,
    onUpdateEmailClick: () -> Unit = {},
    onDismissDoctorResetRequest: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val doctorsInOpd = uiState.doctors.count { it.isInOPD }
    val doctorsOnBreak = uiState.doctors.count { it.isOnBreak }
    val totalWaiting = uiState.appointments.count { it.status == AppointmentStatus.BOOKED || it.status == AppointmentStatus.WAITING }
    val totalInConsultation = uiState.appointments.count { it.status == AppointmentStatus.IN_CONSULTATION }
    val totalCompleted = uiState.appointments.count { it.status == AppointmentStatus.COMPLETED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Doctor Password Reset Requests (Admin-Assisted Security OTP)
        if (uiState.doctorResetRequests.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFF87171)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEF4444)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Doctor Password Reset Request",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = "${uiState.doctorResetRequests.size} Pending Request(s)",
                                        fontSize = 12.sp,
                                        color = Color(0xFFB91C1C)
                                    )
                                }
                            }
                            LiveStatusPill(
                                text = "ACTION REQUIRED",
                                isLive = true,
                                accentColor = Color(0xFFEF4444),
                                backgroundColor = Color(0xFFFEE2E2),
                                borderColor = Color(0xFFFCA5A5)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        uiState.doctorResetRequests.forEach { request ->
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = request.doctorName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = "Doctor ID: ${request.doctorId} • ${request.doctorEmail}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDismissDoctorResetRequest(request.id) }
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Dismiss",
                                                tint = Color(0xFF94A3B8)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Surface(
                                        color = Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "SECURITY OTP FOR DOCTOR",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF64748B),
                                                    letterSpacing = 0.5.sp
                                                )
                                                Text(
                                                    text = request.otp,
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFFDC2626),
                                                    letterSpacing = 4.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = ClipData.newPlainText("Doctor Reset OTP", request.otp)
                                                    clipboard.setPrimaryClip(clip)
                                                    Toast.makeText(context, "OTP copied to clipboard", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = PulseRed),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Copy OTP", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Provide this 6-digit OTP to the doctor so they can reset their password on their device.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Hospital Details Card
        if (uiState.hospital == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CareSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PastelSky),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = CarePrimary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Hospital Configured",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CareTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You are in Admin mode. Please setup your hospital name, OPD details, and contact number to begin managing departments and doctors.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = CareTextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CarePulseButton(
                            text = "Setup Hospital & OPD",
                            onClick = onEditHospitalClick,
                            icon = Icons.Default.Business
                        )
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CareSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = uiState.hospital.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CareTextPrimary
                                )
                                Text(
                                    text = "OPD: ${uiState.hospital.opdName.ifBlank { "General OPD Block" }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CarePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(onClick = onEditHospitalClick) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CarePrimary)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CardBorderSubtle)

                        Text(
                            text = "📍 ${uiState.hospital.address.ifBlank { "Address not configured" }}, ${uiState.hospital.city}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CareTextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "📞 Helpdesk: ${uiState.hospital.phone.ifBlank { "Not provided" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CareTextMuted
                        )
                    }
                }
            }
        }

        // Administrator Account & Email Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CareSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PastelPeach),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = CarePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Hospital Administrator",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CareTextPrimary
                                )
                                Text(
                                    text = uiState.currentAdmin?.name ?: "Administrator",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CareTextSecondary
                                )
                            }
                        }

                        IconButton(onClick = onUpdateEmailClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Admin Email", tint = CarePrimary)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CardBorderSubtle)

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CareBackground,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ADMIN LOGIN & ALERT EMAIL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CareTextMuted,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = uiState.currentAdmin?.email?.ifBlank { "No email configured" } ?: "No email configured",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (!uiState.currentAdmin?.email.isNullOrBlank()) CareTextPrimary else PulseRed
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!uiState.currentAdmin?.email.isNullOrBlank()) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    text = if (!uiState.currentAdmin?.email.isNullOrBlank()) "Active" else "Missing",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!uiState.currentAdmin?.email.isNullOrBlank()) Color(0xFF15803D) else Color(0xFFB91C1C),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CareOutlinePillButton(
                        text = if (uiState.currentAdmin?.email.isNullOrBlank()) "Add Admin Email" else "Change / Add New Email",
                        onClick = onUpdateEmailClick,
                        icon = Icons.Default.Email,
                        textColor = CarePrimary,
                        borderColor = CardBorderSubtle,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Live Real-Time Operations Grid
        item {
            Text(
                text = "Live OPD Real-Time Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CareTextPrimary
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "In OPD Now",
                    value = "$doctorsInOpd / ${uiState.doctors.size}",
                    icon = Icons.Default.MedicalServices,
                    iconTint = QueueActiveGreen,
                    containerColor = PastelMint,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "On Break",
                    value = "$doctorsOnBreak",
                    icon = Icons.Default.Schedule,
                    iconTint = QueueWaitingAmber,
                    containerColor = PastelPeach,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Waiting",
                    value = "$totalWaiting",
                    icon = Icons.Default.People,
                    iconTint = QueueWaitingAmber,
                    containerColor = PastelPeach,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Consulting",
                    value = "$totalInConsultation",
                    icon = Icons.Default.Person,
                    iconTint = QueueServingCyan,
                    containerColor = PastelSky,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Completed",
                    value = "$totalCompleted",
                    icon = Icons.Default.CheckCircle,
                    iconTint = QueueActiveGreen,
                    containerColor = PastelMint,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Summary of Doctors in OPD
        item {
            Text(
                text = "Doctors Currently Present in OPD",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CareTextPrimary
            )
        }

        val opdDocs = uiState.doctors.filter { it.isInOPD }
        if (opdDocs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CareSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
                ) {
                    Text(
                        text = "No doctors are currently marked 'In OPD'. Doctors toggle this when they arrive in their consultation rooms.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CareTextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(opdDocs) { doc ->
                val queue = uiState.activeQueues.find { it.doctorId == doc.id }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CareSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = doc.name,
                                    fontWeight = FontWeight.Bold,
                                    color = CareTextPrimary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = CarePrimary, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = "${doc.specialization} • ${doc.departmentName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CareTextSecondary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            if (doc.isOnBreak) {
                                Text("☕ On Break", color = QueueWaitingAmber, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = QueueServingCyanContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(QueueServingCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Serving #${queue?.currentToken ?: 0}",
                                    color = QueueServingCyan,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// --- TAB 2: DOCTORS MANAGEMENT ---
// ==========================================

@Composable
private fun DoctorsTab(
    doctors: List<Doctor>,
    activeQueues: List<DailyQueue>,
    onToggleDoctorAvailability: (String, Boolean) -> Unit,
    onRemoveDoctor: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (doctors.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CareSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = CarePrimary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No doctors registered yet", fontWeight = FontWeight.Bold, color = CareTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+' below to add your first doctor. An OPD account with login credentials will be automatically generated.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CareTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(doctors) { doctor ->
                val queue = activeQueues.find { it.doctorId == doctor.id }
                DoctorAdminCard(
                    doctor = doctor,
                    queue = queue,
                    onToggleAvailability = { onToggleDoctorAvailability(doctor.id, doctor.isAvailable) },
                    onRemoveDoctor = { onRemoveDoctor(doctor.id) }
                )
            }
        }
    }
}

@Composable
private fun DoctorAdminCard(
    doctor: Doctor,
    queue: DailyQueue?,
    onToggleAvailability: () -> Unit,
    onRemoveDoctor: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Remove Doctor?",
                    fontWeight = FontWeight.Bold,
                    color = CareTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove Dr. ${doctor.name} (${doctor.specialization}) from the hospital roster? Their account will be deactivated and removed from the OPD.",
                    color = CareTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onRemoveDoctor()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Remove Doctor", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = CareTextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CareSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = doctor.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CareTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, contentDescription = null, tint = CarePrimary, modifier = Modifier.size(15.dp))
                    }
                    Text(
                        text = "${doctor.specialization} • ${doctor.departmentName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CarePrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${doctor.qualification} • ${doctor.experienceYears} yrs experience",
                        style = MaterialTheme.typography.labelSmall,
                        color = CareTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // OPD Live Indicator
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
                            maxLines = 1,
                            softWrap = false,
                            color = when {
                                doctor.isOnBreak -> QueueWaitingAmber
                                doctor.isInOPD -> QueueActiveGreen
                                else -> CareTextMuted
                            }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CardBorderSubtle)

            // Shift and Token Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🕒 Shift: ${doctor.shiftStartTime} - ${doctor.shiftEndTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CareTextSecondary
                    )
                    Text(
                        text = "🎟 Max: ${doctor.maxTokensPerDay} slots | Fee: ₹${doctor.consultationFee.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CareTextMuted
                    )
                    if (queue != null) {
                        Text(
                            text = "Serving: #${queue.currentToken} / Total Issued: ${queue.totalTokensIssued}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = QueueServingCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Availability Switch on left, Remove Doctor on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = doctor.isAvailable,
                        onCheckedChange = { onToggleAvailability() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = QueueActiveGreen
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (doctor.isAvailable) "Accepting" else "Paused",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (doctor.isAvailable) QueueActiveGreen else CareTextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.clickable { showDeleteConfirmDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Doctor",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Remove Doctor",
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
}

// ==========================================
// --- TAB 3: DEPARTMENTS MANAGEMENT ---
// ==========================================

@Composable
private fun DepartmentsTab(
    departments: List<Department>,
    doctors: List<Doctor>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (departments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CareSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = CarePrimary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No departments added yet", fontWeight = FontWeight.Bold, color = CareTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+' to create your first hospital department (e.g. Cardiology, Orthopedics, Pediatrics).",
                            style = MaterialTheme.typography.bodySmall,
                            color = CareTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(departments) { dept ->
                val deptDocs = doctors.filter { it.departmentId == dept.id }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CareSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PastelSky),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = CarePrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    dept.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CareTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    dept.description.ifBlank { "Clinical department" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CareTextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = CarePrimaryContainer
                        ) {
                            Text(
                                text = "${deptDocs.size} Doctors",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CarePrimary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// --- TAB 4: LIVE QUEUES & BOOKINGS ---
// ==========================================

@Composable
private fun LiveQueuesTab(
    activeQueues: List<DailyQueue>,
    appointments: List<Appointment>,
    doctors: List<Doctor>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Live Doctor Queues (Today)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CareTextPrimary
            )
        }

        if (activeQueues.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CareSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
                ) {
                    Text(
                        text = "No active doctor queues running right now. As doctors advance queues, live counters appear here in real time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CareTextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(activeQueues) { queue ->
                val doctor = doctors.find { it.id == queue.doctorId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CareSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    queue.doctorName.ifBlank { doctor?.name ?: "Doctor" },
                                    fontWeight = FontWeight.Bold,
                                    color = CareTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "Date: ${queue.date}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CareTextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PastelMint
                            ) {
                                Text(
                                    text = queue.status.name,
                                    color = QueueActiveGreen,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = QueueServingCyanContainer
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Serving Now", style = MaterialTheme.typography.labelSmall, color = QueueServingCyan)
                                    Text("#${queue.currentToken}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = QueueServingCyan)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = QueueWaitingAmberContainer
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Total Issued", style = MaterialTheme.typography.labelSmall, color = QueueWaitingAmber)
                                    Text("${queue.totalTokensIssued}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = QueueWaitingAmber)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = CareSurfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Waiting", style = MaterialTheme.typography.labelSmall, color = CareTextMuted)
                                    Text("${(queue.totalTokensIssued - queue.currentToken).coerceAtLeast(0)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CareTextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Recent Hospital Bookings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CareTextPrimary
            )
        }

        if (appointments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CareSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
                ) {
                    Text(
                        text = "No bookings received yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CareTextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(appointments.take(15)) { apt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CareSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                apt.patientName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = CareTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Token #${apt.tokenNumber} • Dr. ${apt.doctorName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CarePrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${apt.date} at ${apt.timeSlot}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CareTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        StatusChip(status = apt.status)
                    }
                }
            }
        }
    }
}
