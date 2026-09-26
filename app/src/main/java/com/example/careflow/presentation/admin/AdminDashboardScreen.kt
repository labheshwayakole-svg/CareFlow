package com.example.careflow.presentation.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview & Hospital, 1: Doctors, 2: Departments, 3: Live Queues

    var showEditHospitalDialog by remember { mutableStateOf(false) }
    var showAddDoctorDialog by remember { mutableStateOf(false) }
    var showAddDeptDialog by remember { mutableStateOf(false) }

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
                    containerColor = CarePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Doctor")
                }
                2 -> FloatingActionButton(
                    onClick = { showAddDeptDialog = true },
                    containerColor = CarePrimary,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PastelSky),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = CarePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = uiState.hospital?.name ?: "Hospital Admin Portal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CareTextPrimary
                            )
                            Text(
                                text = "OPD: ${uiState.hospital?.opdName?.ifBlank { "Main Outpatient Block" } ?: "Main Outpatient Block"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CarePrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showEditHospitalDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Hospital", tint = CarePrimary)
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

            // Tab Navigation
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = CareSurface,
                contentColor = CarePrimary
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
                        onEditHospitalClick = { showEditHospitalDialog = true }
                    )
                    1 -> DoctorsTab(
                        doctors = uiState.doctors,
                        activeQueues = uiState.activeQueues,
                        onToggleDoctorAvailability = onToggleDoctorAvailability
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

    // 2. Edit Hospital Profile Dialog
    if (showEditHospitalDialog) {
        var hospName by remember { mutableStateOf(uiState.hospital?.name ?: "") }
        var opdName by remember { mutableStateOf(uiState.hospital?.opdName ?: "") }
        var address by remember { mutableStateOf(uiState.hospital?.address ?: "") }
        var city by remember { mutableStateOf(uiState.hospital?.city ?: "") }
        var phone by remember { mutableStateOf(uiState.hospital?.phone ?: "") }

        AlertDialog(
            onDismissRequest = { showEditHospitalDialog = false },
            title = { Text("Hospital & OPD Profile", fontWeight = FontWeight.Bold, color = CareTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = hospName,
                        onValueChange = { hospName = it },
                        label = { Text("Hospital / Clinic Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = opdName,
                        onValueChange = { opdName = it },
                        label = { Text("OPD Name (e.g. Ground Floor Main OPD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Hospital Address / Landmark") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Hospital Emergency / Helpdesk Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (hospName.isNotBlank()) {
                            onSaveHospital(hospName, opdName, address, city, phone)
                            showEditHospitalDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CarePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Details", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditHospitalDialog = false }) {
                    Text("Cancel", color = CareTextSecondary)
                }
            }
        )
    }

    // 3. Add Doctor Dialog
    if (showAddDoctorDialog) {
        var name by remember { mutableStateOf("") }
        var specialization by remember { mutableStateOf("") }
        var customDeptName by remember { mutableStateOf("") }
        var selectedDept by remember { mutableStateOf(uiState.departments.firstOrNull()) }
        var qualification by remember { mutableStateOf("") }
        var experienceYears by remember { mutableStateOf("") }
        var fee by remember { mutableStateOf("") }
        var shiftStart by remember { mutableStateOf("09:00 AM") }
        var shiftEnd by remember { mutableStateOf("02:00 PM") }
        var maxTokens by remember { mutableStateOf("20") }
        var deptDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDoctorDialog = false },
            title = { Text("Add Doctor to OPD", fontWeight = FontWeight.Bold, color = CareTextPrimary) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Doctor Full Name (e.g. Ramesh Patel)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = specialization,
                            onValueChange = { specialization = it },
                            label = { Text("Specialization (e.g. Cardiologist)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    item {
                        if (uiState.departments.isEmpty()) {
                            OutlinedTextField(
                                value = customDeptName,
                                onValueChange = { customDeptName = it },
                                label = { Text("Department (e.g. Cardiology)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        } else {
                            ExposedDropdownMenuBox(
                                expanded = deptDropdownExpanded,
                                onExpandedChange = { deptDropdownExpanded = !deptDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedDept?.name ?: "Select Department",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptDropdownExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    label = { Text("Department") },
                                    shape = RoundedCornerShape(10.dp)
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
                                                deptDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = qualification,
                                onValueChange = { qualification = it },
                                label = { Text("Qualification (MBBS, MD)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = experienceYears,
                                onValueChange = { experienceYears = it },
                                label = { Text("Exp (Yrs)") },
                                modifier = Modifier.weight(0.7f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = fee,
                                onValueChange = { fee = it },
                                label = { Text("Fee (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = maxTokens,
                                onValueChange = { maxTokens = it },
                                label = { Text("Max Slots/Day") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = shiftStart,
                                onValueChange = { shiftStart = it },
                                label = { Text("Shift Start") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = shiftEnd,
                                onValueChange = { shiftEnd = it },
                                label = { Text("Shift End") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deptId = selectedDept?.id ?: ""
                        val deptName = selectedDept?.name ?: customDeptName.ifBlank { "General Medicine" }
                        if (name.isNotBlank()) {
                            onAddDoctor(
                                name,
                                specialization,
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
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Create Doctor", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDoctorDialog = false }) {
                    Text("Cancel", color = CareTextSecondary)
                }
            }
        )
    }

    // 4. Add Department Dialog
    if (showAddDeptDialog) {
        var deptName by remember { mutableStateOf("") }
        var deptDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDeptDialog = false },
            title = { Text("Add Hospital Department", fontWeight = FontWeight.Bold, color = CareTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = deptName,
                        onValueChange = { deptName = it },
                        label = { Text("Department Name (e.g. Cardiology)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = deptDesc,
                        onValueChange = { deptDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deptName.isNotBlank()) {
                            onAddDepartment(deptName, deptDesc, "stethoscope")
                            showAddDeptDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CarePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Add Department", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDeptDialog = false }) {
                    Text("Cancel", color = CareTextSecondary)
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
    onEditHospitalClick: () -> Unit
) {
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
                        Button(
                            onClick = onEditHospitalClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CarePrimary)
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Setup Hospital & OPD", fontWeight = FontWeight.Bold, color = Color.White)
                        }
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
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(doc.name, fontWeight = FontWeight.Bold, color = CareTextPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = CarePrimary, modifier = Modifier.size(14.dp))
                            }
                            Text("${doc.specialization} • ${doc.departmentName}", style = MaterialTheme.typography.bodySmall, color = CareTextSecondary)
                            if (doc.isOnBreak) {
                                Text("☕ On Break", color = QueueWaitingAmber, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = QueueServingCyanContainer
                        ) {
                            Text(
                                text = "Serving #${queue?.currentToken ?: 0}",
                                color = QueueServingCyan,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
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
    onToggleDoctorAvailability: (String, Boolean) -> Unit
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
                    onToggleAvailability = { onToggleDoctorAvailability(doctor.id, doctor.isAvailable) }
                )
            }
        }
    }
}

@Composable
private fun DoctorAdminCard(
    doctor: Doctor,
    queue: DailyQueue?,
    onToggleAvailability: () -> Unit
) {
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
                            color = CareTextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, contentDescription = null, tint = CarePrimary, modifier = Modifier.size(15.dp))
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (doctor.isAvailable) "Accepting" else "Paused",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (doctor.isAvailable) QueueActiveGreen else CareTextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = doctor.isAvailable,
                        onCheckedChange = { onToggleAvailability() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = QueueActiveGreen
                        )
                    )
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                            Column {
                                Text(dept.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = CareTextPrimary)
                                Text(dept.description.ifBlank { "Clinical department" }, style = MaterialTheme.typography.bodySmall, color = CareTextMuted)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = CarePrimaryContainer
                        ) {
                            Text(
                                text = "${deptDocs.size} Doctors",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CarePrimary,
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
                            Column {
                                Text(queue.doctorName.ifBlank { doctor?.name ?: "Doctor" }, fontWeight = FontWeight.Bold, color = CareTextPrimary)
                                Text("Date: ${queue.date}", style = MaterialTheme.typography.labelSmall, color = CareTextMuted)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PastelMint
                            ) {
                                Text(
                                    text = queue.status.name,
                                    color = QueueActiveGreen,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
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
                        Column {
                            Text(apt.patientName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = CareTextPrimary)
                            Text("Token #${apt.tokenNumber} • Dr. ${apt.doctorName}", style = MaterialTheme.typography.bodySmall, color = CarePrimary)
                            Text("${apt.date} at ${apt.timeSlot}", style = MaterialTheme.typography.labelSmall, color = CareTextMuted)
                        }
                        StatusChip(status = apt.status)
                    }
                }
            }
        }
    }
}
