package com.example.careflow.presentation.patient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careflow.domain.model.Hospital
import com.example.careflow.presentation.components.CarePulseButton
import com.example.careflow.presentation.components.LiveStatusPill
import com.example.careflow.theme.CardBorderSubtle
import com.example.careflow.theme.CareBackground
import com.example.careflow.theme.CarePrimary
import com.example.careflow.theme.CarePrimaryContainer
import com.example.careflow.theme.CareTextPrimary
import com.example.careflow.theme.CareTextSecondary
import com.example.careflow.theme.PastelMint
import com.example.careflow.theme.PastelSky
import com.example.careflow.theme.PulseLiveGreen
import com.example.careflow.theme.PulseLiveGreenBg
import com.example.careflow.theme.PulseLiveGreenBorder
import com.example.careflow.theme.PulseRed
import com.example.careflow.theme.PulseRoseBg
import com.example.careflow.theme.PulseRoseBorder
import com.example.careflow.theme.QueueActiveGreen

data class ProblemCategory(
    val title: String,
    val keywords: List<String>,
    val departmentDisplayName: String = ""
)

val commonProblems = listOf(
    ProblemCategory("All Hospitals", emptyList(), ""),
    ProblemCategory("🩺 Fever / General", listOf("general", "fever", "cough", "cold", "medicine", "opd", "physician"), "General Medicine"),
    ProblemCategory("❤️ Cardiology / Heart", listOf("cardio", "heart", "chest", "bp", "cardiology", "cardiologist"), "Cardiology"),
    ProblemCategory("🦴 Orthopedic / Bone", listOf("ortho", "bone", "joint", "fracture", "knee", "orthopedic"), "Orthopedics"),
    ProblemCategory("👶 Pediatrics / Child", listOf("pediatric", "child", "baby", "infant", "pediatrician"), "Pediatrics"),
    ProblemCategory("🧴 Dermatology / Skin", listOf("derma", "skin", "rash", "allergy", "dermatologist"), "Dermatology"),
    ProblemCategory("👁️ Eye / Vision", listOf("eye", "vision", "ophthalmology", "ophthalmologist"), "Ophthalmology"),
    ProblemCategory("🦷 Dental / Teeth", listOf("dental", "teeth", "tooth", "dentist"), "Dental"),
    ProblemCategory("🧠 Neurology / Brain", listOf("neuro", "brain", "nerve", "headache", "neurologist", "neurosurgeon"), "Neurology"),
    ProblemCategory("👂 ENT / Throat", listOf("ent", "ear", "nose", "throat"), "ENT")
)

fun getMatchingDoctorsForHospital(
    hospital: Hospital,
    keywords: List<String>,
    allDoctors: List<com.example.careflow.domain.model.Doctor>
): List<com.example.careflow.domain.model.Doctor> {
    if (keywords.isEmpty()) return emptyList()
    return allDoctors.filter { doc ->
        doc.hospitalId == hospital.id && keywords.any { kw ->
            doc.specialization.lowercase().contains(kw) ||
            doc.departmentName.lowercase().contains(kw)
        }
    }
}

@Composable
fun HospitalSearchScreen(
    uiState: PatientUiState,
    onSelectHospital: (Hospital) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    val chipScrollState = rememberScrollState()

    val selectedCategory = commonProblems.getOrNull(selectedCategoryIndex)
    val categoryKeywords = selectedCategory?.keywords.orEmpty()
    val queryLower = searchQuery.trim().lowercase()

    // Filter and Sort hospitals:
    // When user selects a specialty template (Neurology, ENT, Dental, Dermatology, etc.),
    // hospitals that have doctors for that department occur AT THE VERY TOP!
    val filteredHospitals = remember(uiState.hospitals, uiState.allDoctors, searchQuery, selectedCategoryIndex) {
        uiState.hospitals.filter { hosp ->
            if (queryLower.isBlank()) {
                true
            } else {
                val hospMatches = hosp.name.lowercase().contains(queryLower) ||
                    hosp.city.lowercase().contains(queryLower) ||
                    hosp.address.lowercase().contains(queryLower) ||
                    hosp.opdName.lowercase().contains(queryLower)
                val docMatches = uiState.allDoctors.any { doc ->
                    doc.hospitalId == hosp.id && (
                        doc.name.lowercase().contains(queryLower) ||
                        doc.specialization.lowercase().contains(queryLower) ||
                        doc.departmentName.lowercase().contains(queryLower)
                    )
                }
                hospMatches || docMatches
            }
        }.sortedWith(
            compareByDescending<Hospital> { hosp ->
                if (categoryKeywords.isEmpty()) {
                    0
                } else {
                    val matchingDocs = getMatchingDoctorsForHospital(hosp, categoryKeywords, uiState.allDoctors)
                    if (matchingDocs.isNotEmpty()) 1 else 0
                }
            }.thenByDescending { hosp ->
                if (categoryKeywords.isEmpty()) {
                    0
                } else {
                    getMatchingDoctorsForHospital(hosp, categoryKeywords, uiState.allDoctors).size
                }
            }.thenBy { it.name }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CareBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row: Calming Logo, Patient Greeting, Logout
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.careflow.R.drawable.ic_app_logo),
                        contentDescription = "CareFlow Logo",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hello, ${uiState.currentUser?.name ?: "Patient"} 👋",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Select a hospital for OPD",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

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

        // Calming Hospital Search Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PulseRoseBorder.copy(alpha = 0.6f), RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CarePrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FIND HOSPITAL & OPD CENTER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CarePrimary,
                                letterSpacing = 1.sp
                            )
                        }

                        LiveStatusPill(label = "Realtime Sync", isLive = true)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Search by hospital name, city, or medical symptom to view live doctor queues and book consultation tokens.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search input with clean porcelain container
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search hospital name, city, or medical problem...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = CarePrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = CarePrimary,
                            unfocusedBorderColor = CardBorderSubtle
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Quick Medical Problem Filter Chips
        item {
            Column {
                Text(
                    text = "SELECT MEDICAL PROBLEM OR SPECIALTY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CarePrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(chipScrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commonProblems.forEachIndexed { index, problem ->
                        val isSelected = selectedCategoryIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategoryIndex = if (selectedCategoryIndex == index) 0 else index
                            },
                            label = { Text(problem.title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PulseRoseBg,
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
        }

        // Section Title: Available Hospitals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (selectedCategoryIndex > 0 && selectedCategory != null) {
                            "HOSPITALS WITH ${selectedCategory.departmentDisplayName.uppercase()} SPECIALISTS FIRST (${filteredHospitals.size})"
                        } else {
                            "AVAILABLE HOSPITALS (${filteredHospitals.size})"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    if (selectedCategoryIndex > 0 && selectedCategory != null) {
                        val matchingCount = filteredHospitals.count { hosp ->
                            getMatchingDoctorsForHospital(hosp, categoryKeywords, uiState.allDoctors).isNotEmpty()
                        }
                        Text(
                            text = "✦ $matchingCount hospital${if (matchingCount != 1) "s" else ""} have active ${selectedCategory.departmentDisplayName} doctors",
                            style = MaterialTheme.typography.bodySmall,
                            color = QueueActiveGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Hospital List or Empty State
        if (filteredHospitals.isEmpty()) {
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
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(PulseRoseBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.hospitals.isEmpty()) Icons.Default.LocalHospital else Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = CarePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (uiState.hospitals.isEmpty()) "No Hospitals Registered Yet" else "No matching hospitals found",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (uiState.hospitals.isEmpty()) {
                                "Hospital Administrators can log in through the Admin Portal to add their hospital, OPD departments, and doctors."
                            } else {
                                "Try searching by another hospital name, city, or clearing the problem filter."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        if (searchQuery.isNotEmpty() || selectedCategoryIndex != 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategoryIndex = 0
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Clear Filters", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredHospitals, key = { it.id }) { hospital ->
                val matchingDocs = getMatchingDoctorsForHospital(hospital, categoryKeywords, uiState.allDoctors)
                HospitalCard(
                    hospital = hospital,
                    matchingDoctors = matchingDocs,
                    selectedCategoryName = selectedCategory?.departmentDisplayName?.takeIf { selectedCategoryIndex > 0 },
                    onSelectHospital = { onSelectHospital(hospital) }
                )
            }
        }
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
private fun HospitalCard(
    hospital: Hospital,
    matchingDoctors: List<com.example.careflow.domain.model.Doctor> = emptyList(),
    selectedCategoryName: String? = null,
    onSelectHospital: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectHospital() }
            .border(1.dp, CardBorderSubtle, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            if (matchingDoctors.isNotEmpty() && selectedCategoryName != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PulseLiveGreenBg,
                    border = BorderStroke(1.dp, PulseLiveGreenBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PulseLiveGreenBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = PulseLiveGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "✦ ${matchingDoctors.size} $selectedCategoryName Specialist${if (matchingDoctors.size > 1) "s" else ""} Available",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PulseLiveGreen
                            )
                            Text(
                                text = matchingDoctors.joinToString(", ") { doc ->
                                    val cleanName = doc.name.removePrefix("Dr. ").trim()
                                    "Dr. $cleanName"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = CareTextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PulseRoseBg)
                        .border(1.dp, PulseRoseBorder, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = CarePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = hospital.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Hospital",
                                tint = CarePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        LiveStatusPill(label = "OPD Open", isLive = true)
                    }

                    if (hospital.opdName.isNotBlank()) {
                        Text(
                            text = "OPD Complex: ${hospital.opdName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CarePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location & City
            if (hospital.address.isNotBlank() || hospital.city.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = listOf(hospital.address, hospital.city).filter { it.isNotBlank() }.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Phone
            if (hospital.phone.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = hospital.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary action button - HemoPulse CarePulseButton
            CarePulseButton(
                text = "Select Hospital & Enter OPD",
                onClick = onSelectHospital,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
