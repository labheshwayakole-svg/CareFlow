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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
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
import com.example.careflow.theme.QueueActiveGreen
import com.example.careflow.theme.QueueCancelledRed
import com.example.careflow.theme.QueueServingCyan
import com.example.careflow.theme.QueueWaitingAmber

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
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            when (selectedTab) {
                1 -> FloatingActionButton(
                    onClick = { showAddDoctorDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Doctor")
                }
                2 -> FloatingActionButton(
                    onClick = { showAddDeptDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
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
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = uiState.hospital?.name ?: "Hospital Admin Portal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "OPD: ${uiState.hospital?.opdName?.ifBlank { "Main Block" } ?: "Main Block"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { showEditHospitalDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Hospital", tint = MaterialTheme.colorScheme.primary)
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
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Overview") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Doctors (${uiState.doctors.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Depts (${uiState.departments.size})") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Live Queues") }
                )
            }

            // Tab Contents
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
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
                    Text("Doctor Account Created", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Auto-generated login credentials for ${creds.doctorName}. Share these with the doctor:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Login Email / ID:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(creds.loginEmail, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Temporary Password:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(creds.temporaryPassword, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Doctor Credentials", "Email: ${creds.loginEmail}\nPassword: ${creds.temporaryPassword}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Credentials copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Credentials")
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismissCredentials) {
                    Text("Done")
                }
            }
        )
    }

    // 2. Edit Hospital & OPD Dialog
    if (showEditHospitalDialog) {
        var hospName by remember { mutableStateOf(uiState.hospital?.name ?: "") }
        var opdName by remember { mutableStateOf(uiState.hospital?.opdName ?: "") }
        var address by remember { mutableStateOf(uiState.hospital?.address ?: "") }
        var city by remember { mutableStateOf(uiState.hospital?.city ?: "") }
        var phone by remember { mutableStateOf(uiState.hospital?.phone ?: "") }

        AlertDialog(
            onDismissRequest = { showEditHospitalDialog = false },
            title = { Text("Hospital & OPD Setup", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = hospName,
                        onValueChange = { hospName = it },
                        label = { Text("Hospital Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = opdName,
                        onValueChange = { opdName = it },
                        label = { Text("OPD Name / Block") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Hospital Emergency / Helpdesk Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
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
                    }
                ) {
                    Text("Save Details")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditHospitalDialog = false }) {
                    Text("Cancel")
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
            title = { Text("Add Doctor to OPD", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Doctor Full Name (e.g. Ramesh Patel)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = specialization,
                            onValueChange = { specialization = it },
                            label = { Text("Specialization (e.g. Cardiologist)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        if (uiState.departments.isEmpty()) {
                            OutlinedTextField(
                                value = customDeptName,
                                onValueChange = { customDeptName = it },
                                label = { Text("Department (e.g. Cardiology)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
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
                                    label = { Text("Department") }
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
                                label = { Text("Qualification (e.g. MBBS, MD)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = experienceYears,
                                onValueChange = { experienceYears = it },
                                label = { Text("Exp (Yrs)") },
                                modifier = Modifier.weight(0.7f),
                                singleLine = true
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
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = maxTokens,
                                onValueChange = { maxTokens = it },
                                label = { Text("Max Slots/Day") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
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
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = shiftEnd,
                                onValueChange = { shiftEnd = it },
                                label = { Text("Shift End") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deptId = selectedDept?.id ?: if (customDeptName.isNotBlank()) "dept_${customDeptName.lowercase().replace(" ", "_").take(8)}" else "dept_general"
                        val deptName = selectedDept?.name ?: if (customDeptName.isNotBlank()) customDeptName else "General"
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
                    }
                ) {
                    Text("Create Doctor")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDoctorDialog = false }) {
                    Text("Cancel")
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
            title = { Text("Add Hospital Department", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = deptName,
                        onValueChange = { deptName = it },
                        label = { Text("Department Name (e.g. Neurology)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = deptDesc,
                        onValueChange = { deptDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
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
                    }
                ) {
                    Text("Add Department")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDeptDialog = false }) {
                    Text("Cancel")
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Hospital Configured",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You are in Admin mode. Please setup your hospital name, OPD details, and contact number to begin managing departments and doctors.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onEditHospitalClick,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Setup Hospital & OPD", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = uiState.hospital.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "OPD: ${uiState.hospital.opdName.ifBlank { "General OPD" }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(onClick = onEditHospitalClick) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "📍 ${uiState.hospital.address.ifBlank { "Address not configured" }}, ${uiState.hospital.city}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "📞 Helpdesk: ${uiState.hospital.phone.ifBlank { "Not provided" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "In OPD Now",
                    value = "$doctorsInOpd / ${uiState.doctors.size}",
                    icon = Icons.Default.MedicalServices,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "On Break",
                    value = "$doctorsOnBreak",
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Waiting Patients",
                    value = "$totalWaiting",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "In Consultation",
                    value = "$totalInConsultation",
                    icon = Icons.Default.Person,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Completed",
                    value = "$totalCompleted",
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Summary of Doctors in OPD
        item {
            Text(
                text = "Doctors Currently Present in OPD",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        val opdDocs = uiState.doctors.filter { it.isInOPD }
        if (opdDocs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "No doctors are currently marked 'In OPD'. Doctors toggle this when they arrive at the hospital.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(opdDocs) { doc ->
                val queue = uiState.activeQueues.find { it.doctorId == doc.id }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(doc.name, fontWeight = FontWeight.Bold)
                            Text("${doc.specialization} • ${doc.departmentName}", style = MaterialTheme.typography.bodySmall)
                            if (doc.isOnBreak) {
                                Text("☕ On Break", color = QueueWaitingAmber, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = QueueServingCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Serving Token #${queue?.currentToken ?: 0}",
                                color = QueueServingCyan,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                Text(
                    text = "No doctors registered yet. Tap '+' to add a doctor and auto-generate their login account.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
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

                // OPD Live Indicator
                Surface(
                    shape = RoundedCornerShape(50),
                    color = when {
                        doctor.isOnBreak -> QueueWaitingAmber.copy(alpha = 0.2f)
                        doctor.isInOPD -> QueueActiveGreen.copy(alpha = 0.2f)
                        else -> Color.Gray.copy(alpha = 0.2f)
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
                                else -> Color.DarkGray
                            }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Shift and Token Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🕒 Shift: ${doctor.shiftStartTime} - ${doctor.shiftEndTime}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "🎟 Max Slots/Day: ${doctor.maxTokensPerDay} | Fee: ₹${doctor.consultationFee.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = doctor.isAvailable,
                        onCheckedChange = { onToggleAvailability() }
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
                Text(
                    text = "No departments added yet. Tap '+' to create your first hospital department (e.g. Cardiology, Orthopedics).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            items(departments) { dept ->
                val deptDocs = doctors.filter { it.departmentId == dept.id }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(dept.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(dept.description.ifBlank { "General clinical specialty" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${deptDocs.size} Doctors",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                fontWeight = FontWeight.Bold
            )
        }

        if (activeQueues.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "No active doctor queues running right now. As doctors start their OPD shifts and advance queues, real-time counters appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(activeQueues) { queue ->
                val doctor = doctors.find { it.id == queue.doctorId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(queue.doctorName.ifBlank { doctor?.name ?: "Doctor" }, fontWeight = FontWeight.Bold)
                                Text("Date: ${queue.date}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = QueueActiveGreen.copy(alpha = 0.15f)
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
                                shape = RoundedCornerShape(8.dp),
                                color = QueueServingCyan.copy(alpha = 0.12f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Serving Now", style = MaterialTheme.typography.labelSmall, color = QueueServingCyan)
                                    Text("#${queue.currentToken}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = QueueServingCyan)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = QueueWaitingAmber.copy(alpha = 0.12f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Total Issued", style = MaterialTheme.typography.labelSmall, color = QueueWaitingAmber)
                                    Text("${queue.totalTokensIssued}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = QueueWaitingAmber)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Waiting", style = MaterialTheme.typography.labelSmall)
                                    Text("${(queue.totalTokensIssued - queue.currentToken).coerceAtLeast(0)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
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
                fontWeight = FontWeight.Bold
            )
        }

        if (appointments.isEmpty()) {
            item {
                Text(
                    text = "No bookings received yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(appointments.take(15)) { apt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(apt.patientName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Token #${apt.tokenNumber} • Dr. ${apt.doctorName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Text("${apt.date} at ${apt.timeSlot}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusChip(status = apt.status)
                    }
                }
            }
        }
    }
}
