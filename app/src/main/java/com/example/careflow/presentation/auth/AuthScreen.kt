package com.example.careflow.presentation.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careflow.domain.model.UserRole

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: (String, String, String, String, UserRole) -> Unit,
    onSendOtpClick: (Activity, String) -> Unit,
    onVerifyOtpClick: (String, String, UserRole) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onSwitchDemoRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableIntStateOf(0) } // 0: Email/Pass, 1: Phone OTP
    var emailTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Register
    val context = LocalContext.current
    val activity = context as? Activity

    // Email fields
    var email by remember { mutableStateOf("patient@careflow.com") }
    var password by remember { mutableStateOf("pass123") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.PATIENT) }

    // Phone OTP fields
    var otpPhone by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var otpName by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Branding
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = "CareFlow Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "CareFlow",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Firebase Powered Smart Queue Platform",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Card Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Top Selector: Email / Password vs Phone OTP
                TabRow(
                    selectedTabIndex = authMode,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = authMode == 0,
                        onClick = { authMode = 0 },
                        text = { Text("Email / Pass", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                    Tab(
                        selected = authMode == 1,
                        onClick = { authMode = 1 },
                        text = { Text("Phone OTP", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (authMode == 0) {
                    // Email Authentication Sub-Tabs (Sign In / Register)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = { emailTab = 0 }) {
                            Text(
                                "Sign In",
                                fontWeight = if (emailTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (emailTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text("•", modifier = Modifier.align(Alignment.CenterVertically), color = MaterialTheme.colorScheme.outline)
                        TextButton(onClick = { emailTab = 1 }) {
                            Text(
                                "Create Account",
                                fontWeight = if (emailTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (emailTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (emailTab == 1) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (emailTab == 0) {
                                onLoginClick(email, password)
                            } else {
                                onRegisterClick(name, email, password, phone, selectedRole)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        } else {
                            Text(
                                text = if (emailTab == 0) "Sign In via Firebase" else "Create Firebase Account",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Phone Authentication
                    if (!uiState.isOtpSent) {
                        OutlinedTextField(
                            value = otpPhone,
                            onValueChange = { otpPhone = it },
                            label = { Text("Phone Number (+91...)") },
                            placeholder = { Text("9876543210") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (activity != null && otpPhone.isNotBlank()) {
                                    onSendOtpClick(activity, otpPhone)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isLoading && otpPhone.isNotBlank()
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            } else {
                                Text("Send Firebase OTP 📲", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Enter OTP
                        Text(
                            text = "Enter 6-digit OTP code sent to your phone",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = otpName,
                            onValueChange = { otpName = it },
                            label = { Text("Your Name (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it },
                            label = { Text("6-Digit OTP Code") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onVerifyOtpClick(otpCode, otpName, selectedRole) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isLoading && otpCode.length >= 6
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            } else {
                                Text("Verify & Sign In", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(14.dp))

                // Google Sign-In Button
                OutlinedButton(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🔍 Continue with Google", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Demo Quick Access
        Text(
            text = "⚡ INSTANT ROLE DEMO SWITCHER",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onSwitchDemoRole(UserRole.PATIENT) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Patient", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { onSwitchDemoRole(UserRole.DOCTOR) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Doctor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { onSwitchDemoRole(UserRole.ADMIN) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Admin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
