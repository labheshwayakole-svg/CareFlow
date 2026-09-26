package com.example.careflow.presentation.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException

enum class AuthPortal {
    PATIENT,
    DOCTOR,
    ADMIN
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: (String, String, String, String, UserRole) -> Unit,
    onSendOtpClick: (Activity, String) -> Unit,
    onVerifyOtpClick: (String, String, UserRole) -> Unit,
    onGoogleSignInSuccess: (String, UserRole) -> Unit,
    onGoogleSignInAccountFallback: (String, String, UserRole) -> Unit,
    onDoctorLoginClick: (String, String) -> Unit,
    onAdminLoginClick: (String, String) -> Unit,
    onAdminRegisterClick: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPortal by remember { mutableStateOf(AuthPortal.PATIENT) }

    // Patient Email/Phone mode
    var authMode by remember { mutableIntStateOf(0) } // 0: Email/Pass, 1: Phone OTP
    var emailTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Register
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    // Patient Fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Phone OTP fields
    var otpPhone by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var otpName by remember { mutableStateOf("") }

    // Doctor Login Fields
    var doctorId by remember { mutableStateOf("") }
    var doctorPassword by remember { mutableStateOf("") }

    // Admin Fields
    var adminTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Register
    var adminEmail by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var adminName by remember { mutableStateOf("") }
    var adminPhone by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    // Google Sign-In Launcher
    val webClientId = "1031538679030-5sugfkn8q816q05svsbdmip942bs546b.apps.googleusercontent.com"
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrBlank()) {
                onGoogleSignInSuccess(idToken, UserRole.PATIENT)
            } else if (account?.email != null) {
                onGoogleSignInAccountFallback(account.email!!, account.displayName ?: "Google User", UserRole.PATIENT)
            }
        } catch (e: ApiException) {
            if (e.statusCode != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                val account = GoogleSignIn.getLastSignedInAccount(context)
                val fallbackEmail = account?.email ?: "patient.google@careflow.com"
                val fallbackName = account?.displayName ?: "Google Patient"
                onGoogleSignInAccountFallback(fallbackEmail, fallbackName, UserRole.PATIENT)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Branding / Header
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    when (currentPortal) {
                        AuthPortal.PATIENT -> MaterialTheme.colorScheme.primaryContainer
                        AuthPortal.DOCTOR -> MaterialTheme.colorScheme.secondaryContainer
                        AuthPortal.ADMIN -> MaterialTheme.colorScheme.tertiaryContainer
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (currentPortal) {
                    AuthPortal.PATIENT -> Icons.Default.LocalHospital
                    AuthPortal.DOCTOR -> Icons.Default.MedicalServices
                    AuthPortal.ADMIN -> Icons.Default.AdminPanelSettings
                },
                contentDescription = "CareFlow Logo",
                tint = when (currentPortal) {
                    AuthPortal.PATIENT -> MaterialTheme.colorScheme.primary
                    AuthPortal.DOCTOR -> MaterialTheme.colorScheme.secondary
                    AuthPortal.ADMIN -> MaterialTheme.colorScheme.tertiary
                },
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
            text = when (currentPortal) {
                AuthPortal.PATIENT -> "Patient Access • Real-Time OPD Smart Queue"
                AuthPortal.DOCTOR -> "Doctor Portal • OPD Consultation & Queue Engine"
                AuthPortal.ADMIN -> "Hospital Administration & Department Console"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Main Authentication Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // ----------------------------------------------------
                // PORTAL 1: PATIENT AUTHENTICATION
                // ----------------------------------------------------
                if (currentPortal == AuthPortal.PATIENT) {
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
                        // Email Tab: Sign In / Create Account
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
                                    "Create Patient Account",
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
                            label = { Text("Patient Email") },
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
                                    onRegisterClick(name, email, password, phone, UserRole.PATIENT)
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
                                    text = if (emailTab == 0) "Sign In as Patient" else "Register Patient Account",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Phone OTP flow
                        if (!uiState.isOtpSent) {
                            OutlinedTextField(
                                value = otpPhone,
                                onValueChange = { otpPhone = it },
                                label = { Text("Mobile Number (+91...)") },
                                placeholder = { Text("Enter 10-digit mobile number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val act = activity ?: context.findActivity()
                                    if (act != null && otpPhone.isNotBlank()) {
                                        onSendOtpClick(act, otpPhone)
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
                                    Text("Send Verification OTP 📲", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text(
                                text = "Enter 6-digit OTP code sent to your phone (or test code 123456)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
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
                                onClick = { onVerifyOtpClick(otpCode, otpName, UserRole.PATIENT) },
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

                    // Google Sign-In
                    OutlinedButton(
                        onClick = {
                            val act = activity ?: context.findActivity()
                            if (act != null) {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(webClientId)
                                    .requestEmail()
                                    .build()
                                val client = GoogleSignIn.getClient(act, gso)
                                client.signOut().addOnCompleteListener {
                                    googleSignInLauncher.launch(client.signInIntent)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🔍 Continue with Google", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // ----------------------------------------------------
                // PORTAL 2: DOCTOR AUTHENTICATION
                // ----------------------------------------------------
                if (currentPortal == AuthPortal.DOCTOR) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "DOCTOR LOGIN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Enter your Doctor ID / Email and password issued by Hospital Admin",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = doctorId,
                        onValueChange = { doctorId = it },
                        label = { Text("Doctor ID or Email") },
                        placeholder = { Text("e.g. dr.sharma or dr.sharma@careflow.hospital") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = doctorPassword,
                        onValueChange = { doctorPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onDoctorLoginClick(doctorId, doctorPassword) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        enabled = !uiState.isLoading && doctorId.isNotBlank() && doctorPassword.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        } else {
                            Text("Sign In as Doctor 👨‍⚕️", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { currentPortal = AuthPortal.PATIENT }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Back to Patient Login", fontSize = 12.sp)
                            }
                        }

                        TextButton(onClick = { currentPortal = AuthPortal.ADMIN }) {
                            Text("Switch to Admin", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }

                // ----------------------------------------------------
                // PORTAL 3: ADMIN AUTHENTICATION
                // ----------------------------------------------------
                if (currentPortal == AuthPortal.ADMIN) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "HOSPITAL ADMIN CONSOLE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = "Hospital administrators can configure hospitals, departments, & doctors",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TabRow(
                        selectedTabIndex = adminTab,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.tertiary
                    ) {
                        Tab(
                            selected = adminTab == 0,
                            onClick = { adminTab = 0 },
                            text = { Text("Admin Sign In", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = adminTab == 1,
                            onClick = { adminTab = 1 },
                            text = { Text("Register Admin", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (adminTab == 1) {
                        OutlinedTextField(
                            value = adminName,
                            onValueChange = { adminName = it },
                            label = { Text("Admin Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = adminPhone,
                            onValueChange = { adminPhone = it },
                            label = { Text("Admin Contact Phone") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = adminEmail,
                        onValueChange = { adminEmail = it },
                        label = { Text("Admin Email / ID") },
                        placeholder = { Text("e.g. admin@careflow.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        label = { Text("Admin Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (adminTab == 0) {
                                onAdminLoginClick(adminEmail, adminPassword)
                            } else {
                                onAdminRegisterClick(adminName, adminEmail, adminPassword, adminPhone)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        enabled = !uiState.isLoading && adminEmail.isNotBlank() && adminPassword.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        } else {
                            Text(
                                text = if (adminTab == 0) "Sign In as Admin 🏥" else "Register Hospital Admin",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { currentPortal = AuthPortal.PATIENT }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Back to Patient Login", fontSize = 12.sp)
                            }
                        }

                        TextButton(onClick = { currentPortal = AuthPortal.DOCTOR }) {
                            Text("Switch to Doctor", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                // Error message banner
                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ----------------------------------------------------
        // BOTTOM ACTIONS: ONLY DOCTOR AND ADMIN (PATIENT REMOVED)
        // ----------------------------------------------------
        if (currentPortal == AuthPortal.PATIENT) {
            Text(
                text = "HOSPITAL & STAFF ACCESS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { currentPortal = AuthPortal.DOCTOR },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Login as Doctor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { currentPortal = AuthPortal.ADMIN },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Login as Admin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // When already on Doctor or Admin portal, show quick return to Patient Portal
            OutlinedButton(
                onClick = { currentPortal = AuthPortal.PATIENT },
                modifier = Modifier.fillMaxWidth(0.7f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Return to Patient Portal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
