package com.example.careflow.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.careflow.presentation.components.RoleBadge
import com.example.careflow.presentation.components.StatCard
import com.example.careflow.presentation.components.StatusChip

@Composable
fun AdminDashboardScreen(
    uiState: AdminUiState,
    onToggleDoctorAvailability: (String, Boolean) -> Unit,
    onAddDoctor: (String, String, String, String, String, Double, String, String) -> Unit,
    onAddDepartment: (String, String) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Doctors, 1: Departments, 2: Bookings
    var showAddDoctorDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDoctorDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Doctor")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Admin Portal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "CareFlow Hospital Control Center",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Stats Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Doctors",
                        value = "${uiState.doctors.size}",
                        icon = Icons.Default.MedicalServices,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Depts",
                        value = "${uiState.departments.size}",
                        icon = Icons.Default.LocalHospital,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Bookings",
                        value = "${uiState.appointments.size}",
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Tabs
            item {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Doctors (${uiState.doctors.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Departments (${uiState.departments.size})") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("All Bookings (${uiState.appointments.size})") }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    items(uiState.doctors, key = { it.id }) { doc ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = doc.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(text = "${doc.specialization} • ${doc.departmentName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Text(text = "Shift: ${doc.shiftStartTime} - ${doc.shiftEndTime}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = doc.isAvailable,
                                    onCheckedChange = { onToggleDoctorAvailability(doc.id, doc.isAvailable) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    items(uiState.departments, key = { it.id }) { dept ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = dept.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(text = dept.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Active Doctors: ${dept.doctorCount}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                2 -> {
                    items(uiState.appointments, key = { it.id }) { apt ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "${apt.patientName} ➔ Dr. ${apt.doctorName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "Date: ${apt.date} • Token #${apt.tokenNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusChip(status = apt.status)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDoctorDialog) {
        var docName by remember { mutableStateOf("") }
        var docSpec by remember { mutableStateOf("") }
        var docQual by remember { mutableStateOf("MBBS, MD") }
        var docFee by remember { mutableStateOf("500") }

        AlertDialog(
            onDismissRequest = { showAddDoctorDialog = false },
            title = { Text("Add New Doctor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = docName,
                        onValueChange = { docName = it },
                        label = { Text("Doctor Name (e.g. Dr. Ramesh)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = docSpec,
                        onValueChange = { docSpec = it },
                        label = { Text("Specialization (e.g. Cardiologist)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = docQual,
                        onValueChange = { docQual = it },
                        label = { Text("Qualifications") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = docFee,
                        onValueChange = { docFee = it },
                        label = { Text("Consultation Fee (₹)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fee = docFee.toDoubleOrNull() ?: 500.0
                        onAddDoctor(
                            docName,
                            docSpec,
                            "dept_gen",
                            "General Medicine",
                            docQual,
                            fee,
                            "09:00 AM",
                            "02:00 PM"
                        )
                        showAddDoctorDialog = false
                    },
                    enabled = docName.isNotBlank() && docSpec.isNotBlank()
                ) {
                    Text("Add Doctor")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDoctorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
