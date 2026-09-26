package com.example.careflow.presentation.patient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.careflow.domain.model.Hospital

data class ProblemCategory(
    val title: String,
    val keywords: List<String>
)

val commonProblems = listOf(
    ProblemCategory("All Hospitals", emptyList()),
    ProblemCategory("🩺 Fever / General", listOf("general", "fever", "cough", "cold", "medicine", "opd")),
    ProblemCategory("❤️ Cardiology / Heart", listOf("cardio", "heart", "chest", "bp", "cardiology")),
    ProblemCategory("🦴 Orthopedic / Bone", listOf("ortho", "bone", "joint", "fracture", "knee")),
    ProblemCategory("👶 Pediatrics / Child", listOf("pediatric", "child", "baby", "infant")),
    ProblemCategory("🧴 Dermatology / Skin", listOf("derma", "skin", "rash", "allergy")),
    ProblemCategory("👁️ Eye / Vision", listOf("eye", "vision", "ophthalmology")),
    ProblemCategory("🦷 Dental / Teeth", listOf("dental", "teeth", "tooth")),
    ProblemCategory("🧠 Neurology / Brain", listOf("neuro", "brain", "nerve", "headache")),
    ProblemCategory("👂 ENT / Throat", listOf("ent", "ear", "nose", "throat"))
)

@Composable
fun HospitalSearchScreen(
    uiState: PatientUiState,
    onSelectHospital: (Hospital) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    val chipScrollState = rememberScrollState()

    // Filter hospitals based on search query and category keywords
    val filteredHospitals = uiState.hospitals.filter { hosp ->
        val queryLower = searchQuery.trim().lowercase()
        val categoryKeywords = commonProblems.getOrNull(selectedCategoryIndex)?.keywords.orEmpty()

        val matchesTextQuery = if (queryLower.isBlank()) {
            true
        } else {
            hosp.name.lowercase().contains(queryLower) ||
            hosp.city.lowercase().contains(queryLower) ||
            hosp.address.lowercase().contains(queryLower) ||
            hosp.opdName.lowercase().contains(queryLower)
        }

        val matchesCategory = if (categoryKeywords.isEmpty()) {
            true
        } else {
            categoryKeywords.any { kw ->
                hosp.name.lowercase().contains(kw) ||
                hosp.opdName.lowercase().contains(kw) ||
                hosp.address.lowercase().contains(kw)
            } || matchesTextQuery // If the hospital doesn't have department names in its root object, still show if user searched
        }

        matchesTextQuery && matchesCategory
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row: Logo, Greeting, Logout
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = "CareFlow",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Hello, ${uiState.currentUser?.name ?: "Patient"} 👋",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select a hospital to access OPD",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

        // Welcome / Search Banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏥 Find Hospital & OPD Center",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Search by hospital name, city, or medical problem/diagnosis to view live token queues and book doctor consultations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search hospital, city, or problem...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Quick Medical Problem Filter Chips
        item {
            Column {
                Text(
                    text = "COMMON PROBLEMS & SPECIALTIES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
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
                                selectedCategoryIndex = index
                                if (index != 0 && problem.keywords.isNotEmpty()) {
                                    // When a symptom is clicked, also update search query to guide user
                                    searchQuery = problem.keywords.first()
                                } else if (index == 0) {
                                    searchQuery = ""
                                }
                            },
                            label = { Text(problem.title, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
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
                Text(
                    text = "AVAILABLE HOSPITALS (${filteredHospitals.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
        }

        // Hospital List or Empty State
        if (filteredHospitals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (uiState.hospitals.isEmpty()) Icons.Default.LocalHospital else Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (uiState.hospitals.isEmpty()) "No Hospitals Registered Yet" else "No matching hospitals found",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
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
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategoryIndex = 0
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Clear Filters", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredHospitals, key = { it.id }) { hospital ->
                HospitalCard(
                    hospital = hospital,
                    onSelectHospital = { onSelectHospital(hospital) }
                )
            }
        }
    }
}

@Composable
private fun HospitalCard(
    hospital: Hospital,
    onSelectHospital: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectHospital() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hospital.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (hospital.opdName.isNotBlank()) {
                        Text(
                            text = "OPD: ${hospital.opdName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
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

            // Primary action button
            Button(
                onClick = onSelectHospital,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Select Hospital & Enter OPD", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
