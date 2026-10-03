package com.example.careflow.presentation.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careflow.domain.model.UserRole
import com.example.careflow.presentation.components.CareOutlinePillButton
import com.example.careflow.presentation.components.CarePulseButton
import com.example.careflow.presentation.components.LivePulseDot
import com.example.careflow.presentation.components.LiveStatusPill
import com.example.careflow.presentation.components.LuminousHeroCard
import com.example.careflow.theme.CardBorderSubtle
import com.example.careflow.theme.CareBackground
import com.example.careflow.theme.CarePrimary
import com.example.careflow.theme.PulseCoral
import com.example.careflow.theme.PulseCrimsonDark
import com.example.careflow.theme.PulseLiveGreen
import com.example.careflow.theme.PulseLiveGreenBg
import com.example.careflow.theme.PulseLiveGreenBorder
import com.example.careflow.theme.PulseRed
import com.example.careflow.theme.PulseRedBright
import com.example.careflow.theme.PulseRoseBg
import com.example.careflow.theme.PulseRoseBorder
import com.example.careflow.theme.PulseRoseText
import com.example.careflow.theme.CareTextPrimary
import com.example.careflow.theme.CareTextSecondary
import com.example.careflow.theme.PastelPeach
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

@OptIn(ExperimentalLayoutApi::class)
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
    onForgotPasswordClick: (String) -> Unit = {},
    onRequestDoctorResetOtp: (String) -> Unit = {},
    onVerifyDoctorResetOtp: (String, String, String) -> Unit = { _, _, _ -> },
    onClearDoctorResetState: () -> Unit = {},
    onRequestUniversalReset: (String, UserRole, String?) -> Unit = { _, _, _ -> },
    onValidateUniversalResetOtp: (String, String) -> Unit = { _, _ -> },
    onVerifyUniversalReset: (String, String, String) -> Unit = { _, _, _ -> },
    onClearUniversalResetState: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentPortal by remember { mutableStateOf(AuthPortal.PATIENT) }

    // Patient Email/Phone mode
    var authMode by remember { mutableIntStateOf(0) } // 0: Email/Pass, 1: Phone OTP
    var emailTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Register
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    // Patient Fields - All initially blank
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Phone OTP fields
    var otpPhone by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var otpName by remember { mutableStateOf("") }

    // Doctor Login Fields - All initially blank
    var doctorId by remember { mutableStateOf("") }
    var doctorPassword by remember { mutableStateOf("") }

    // Admin Fields - All initially blank
    var adminTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Register
    var adminEmail by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var adminName by remember { mutableStateOf("") }
    var adminPhone by remember { mutableStateOf("") }

    // Universal 6-Digit Forgot Password Dialog State (Patient, Doctor, Admin)
    var showUniversalForgotDialog by remember { mutableStateOf(false) }
    var forgotTargetRole by remember { mutableStateOf(UserRole.PATIENT) }
    var forgotInputIdentifier by remember { mutableStateOf("") }
    var forgotOtpInput by remember { mutableStateOf("") }
    var forgotNewPassword by remember { mutableStateOf("") }
    var forgotConfirmPassword by remember { mutableStateOf("") }
    var resendCooldown by remember { mutableIntStateOf(60) }

    androidx.compose.runtime.LaunchedEffect(uiState.isUniversalResetOtpSent) {
        if (uiState.isUniversalResetOtpSent) {
            resendCooldown = 60
            while (resendCooldown > 0) {
                kotlinx.coroutines.delay(1000L)
                resendCooldown--
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(uiState.user) {
        if (uiState.user != null) {
            showUniversalForgotDialog = false
            forgotOtpInput = ""
            forgotNewPassword = ""
            forgotConfirmPassword = ""
        }
    }

    // Sub-portal / Sub-tab back navigation (Industry Standard Back Handler)
    if (currentPortal != AuthPortal.PATIENT) {
        BackHandler {
            currentPortal = AuthPortal.PATIENT
        }
    } else if (authMode == 1) {
        BackHandler {
            authMode = 0
        }
    } else if (emailTab == 1) {
        BackHandler {
            emailTab = 0
        }
    } else if (adminTab == 1) {
        BackHandler {
            adminTab = 0
        }
    }

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
            .background(CareBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ----------------------------------------------------
        // CAREFLOW BRANDING HEADER
        // ----------------------------------------------------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Official CareFlow App Icon
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.careflow.R.drawable.ic_app_logo),
                contentDescription = "CareFlow Logo",
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), spotColor = CarePrimary.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CareFlow",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = CareTextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LiveStatusPill(
                        text = "SMART OPD",
                        isLive = true,
                        accentColor = CarePrimary,
                        backgroundColor = PastelPeach,
                        borderColor = CardBorderSubtle
                    )
                }
                Text(
                    text = "Intelligent Real-Time Hospital OPD & Chamber Engine",
                    style = MaterialTheme.typography.bodySmall,
                    color = CareTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }



        // ----------------------------------------------------
        // HEMOPULSE SEGMENTED PORTAL PILL SWITCHER
        // ----------------------------------------------------
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tab 1: Patient
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(
                            if (currentPortal == AuthPortal.PATIENT)
                                Brush.horizontalGradient(listOf(PulseCrimsonDark, PulseRed, PulseCoral))
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable { currentPortal = AuthPortal.PATIENT }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Patient",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (currentPortal == AuthPortal.PATIENT) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (currentPortal == AuthPortal.PATIENT) Color.White else Color(0xFF64748B)
                    )
                }

                // Tab 2: Doctor
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(
                            if (currentPortal == AuthPortal.DOCTOR)
                                Brush.horizontalGradient(listOf(PulseCrimsonDark, PulseRed, PulseCoral))
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable { currentPortal = AuthPortal.DOCTOR }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Doctor",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (currentPortal == AuthPortal.DOCTOR) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (currentPortal == AuthPortal.DOCTOR) Color.White else Color(0xFF64748B)
                    )
                }

                // Tab 3: Admin
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(
                            if (currentPortal == AuthPortal.ADMIN)
                                Brush.horizontalGradient(listOf(PulseCrimsonDark, PulseRed, PulseCoral))
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable { currentPortal = AuthPortal.ADMIN }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Admin",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (currentPortal == AuthPortal.ADMIN) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (currentPortal == AuthPortal.ADMIN) Color.White else Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ----------------------------------------------------
        // MAIN LUMINOUS AUTH CARD
        // ----------------------------------------------------
        LuminousHeroCard {
            // ----------------------------------------------------
            // PORTAL 1: PATIENT AUTHENTICATION
            // ----------------------------------------------------
            if (currentPortal == AuthPortal.PATIENT) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (emailTab == 0) "Sign In to Patient Portal" else "Create Patient Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )

                    LiveStatusPill(
                        text = "PATIENT ACCESS",
                        isLive = false,
                        accentColor = PulseCrimsonDark,
                        backgroundColor = PulseRoseBg,
                        borderColor = PulseRoseBorder
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Email vs Phone Tabs
                TabRow(
                    selectedTabIndex = authMode,
                    containerColor = Color(0xFFF8FAFC),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CardBorderSubtle, RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = authMode == 0,
                        onClick = { authMode = 0 },
                        text = { Text("Email & Password", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = authMode == 1,
                        onClick = { authMode = 1 },
                        text = { Text("Phone OTP", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (authMode == 0) {
                    // Sign In / Register toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = { emailTab = 0 }) {
                            Text(
                                "Sign In",
                                fontWeight = if (emailTab == 0) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (emailTab == 0) PulseRedBright else Color(0xFF64748B)
                            )
                        }
                        Text("•", modifier = Modifier.align(Alignment.CenterVertically), color = Color(0xFFCBD5E1))
                        TextButton(onClick = { emailTab = 1 }) {
                            Text(
                                "Create Account",
                                fontWeight = if (emailTab == 1) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (emailTab == 1) PulseRedBright else Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (emailTab == 1) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PulseCrimsonDark) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PulseCrimsonDark) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Patient Email") },
                        placeholder = { Text("e.g. name@example.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PulseCrimsonDark) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PulseCrimsonDark) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    if (emailTab == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                forgotTargetRole = UserRole.PATIENT
                                forgotInputIdentifier = email
                                showUniversalForgotDialog = true
                            }) {
                                Text(
                                    text = "Forgot Password?",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PulseRedBright
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    CarePulseButton(
                        text = if (emailTab == 0) "Sign In as Patient" else "Create Account & Continue",
                        onClick = {
                            if (emailTab == 0) {
                                onLoginClick(email, password)
                            } else {
                                onRegisterClick(name, email, password, phone, UserRole.PATIENT)
                            }
                        },
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Phone OTP flow
                    if (!uiState.isOtpSent) {
                        OutlinedTextField(
                            value = otpPhone,
                            onValueChange = { otpPhone = it },
                            label = { Text("Mobile Number (+91...)") },
                            placeholder = { Text("Enter 10-digit mobile number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PulseCrimsonDark) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CarePulseButton(
                            text = "Send Instant OTP",
                            onClick = {
                                val act = activity ?: context.findActivity()
                                if (act != null && otpPhone.isNotBlank()) {
                                    onSendOtpClick(act, otpPhone)
                                }
                            },
                            isLoading = uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "Enter 6-Digit OTP Code sent to $otpPhone",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = otpName,
                            onValueChange = { otpName = it },
                            label = { Text("Your Full Name (Optional)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PulseCrimsonDark) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it },
                            label = { Text("6-Digit OTP Code") },
                            placeholder = { Text("Enter code (e.g. 123456)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CarePulseButton(
                            text = "Verify & Sign In",
                            onClick = { onVerifyOtpClick(otpCode, otpName, UserRole.PATIENT) },
                            enabled = !uiState.isLoading && otpCode.length >= 6,
                            isLoading = uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign-In with HemoPulse Pill button
                CareOutlinePillButton(
                    text = "Continue with Google Account",
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
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ----------------------------------------------------
            // PORTAL 2: DOCTOR AUTHENTICATION
            // ----------------------------------------------------
            if (currentPortal == AuthPortal.DOCTOR) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Doctor Chamber Portal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )

                    LiveStatusPill(
                        text = "CHAMBER CONSOLE",
                        isLive = true,
                        accentColor = PulseLiveGreen,
                        backgroundColor = PulseLiveGreenBg,
                        borderColor = PulseLiveGreenBorder
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = PulseRoseBg.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseRoseBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = PulseCrimsonDark,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "OPD Queue & Consultation Management",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = PulseCrimsonDark
                            )
                            Text(
                                text = "Sign in to advance queue tokens, manage breaks, and consult patients.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = doctorId,
                    onValueChange = { doctorId = it },
                    label = { Text("Doctor Email / ID") },
                    placeholder = { Text("e.g. doctor@hospital.com or Doctor ID") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = PulseCrimsonDark) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = doctorPassword,
                    onValueChange = { doctorPassword = it },
                    label = { Text("Chamber Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PulseCrimsonDark) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        forgotTargetRole = UserRole.DOCTOR
                        forgotInputIdentifier = if (doctorId.contains("@")) doctorId else ""
                        showUniversalForgotDialog = true
                    }) {
                        Text(
                            text = "Forgot Password? (6-Digit OTP Reset)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = PulseRedBright
                        )
                    }
                }

                CarePulseButton(
                    text = "Sign In as Doctor 👨‍⚕️",
                    onClick = { onDoctorLoginClick(doctorId, doctorPassword) },
                    isLoading = uiState.isLoading,
                    enabled = doctorId.isNotBlank() && doctorPassword.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ----------------------------------------------------
            // PORTAL 3: ADMIN AUTHENTICATION
            // ----------------------------------------------------
            if (currentPortal == AuthPortal.ADMIN) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hospital Admin Console",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )

                    LiveStatusPill(
                        text = "ADMINISTRATOR",
                        isLive = false,
                        accentColor = PulseCrimsonDark,
                        backgroundColor = PulseRoseBg,
                        borderColor = PulseRoseBorder
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Admin Sign In / Register Tabs
                TabRow(
                    selectedTabIndex = adminTab,
                    containerColor = Color(0xFFF8FAFC),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CardBorderSubtle, RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = adminTab == 0,
                        onClick = { adminTab = 0 },
                        text = { Text("Admin Sign In", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = adminTab == 1,
                        onClick = { adminTab = 1 },
                        text = { Text("Register Hospital", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (adminTab == 1) {
                    OutlinedTextField(
                        value = adminName,
                        onValueChange = { adminName = it },
                        label = { Text("Administrator Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PulseCrimsonDark) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = adminPhone,
                        onValueChange = { adminPhone = it },
                        label = { Text("Official Phone") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PulseCrimsonDark) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = adminEmail,
                    onValueChange = { adminEmail = it },
                    label = { Text("Hospital Admin Email or Phone") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PulseCrimsonDark) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = adminPassword,
                    onValueChange = { adminPassword = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PulseCrimsonDark) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                if (adminTab == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            forgotTargetRole = UserRole.ADMIN
                            forgotInputIdentifier = adminEmail
                            showUniversalForgotDialog = true
                        }) {
                            Text(
                                text = "Forgot Admin Password?",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = PulseRedBright
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(18.dp))
                }

                CarePulseButton(
                    text = if (adminTab == 0) "Sign In to Hospital Console" else "Register Hospital Administration",
                    onClick = {
                        if (adminTab == 0) {
                            onAdminLoginClick(adminEmail, adminPassword)
                        } else {
                            onAdminRegisterClick(adminName, adminEmail, adminPassword, adminPhone)
                        }
                    },
                    isLoading = uiState.isLoading,
                    enabled = adminEmail.isNotBlank() && adminPassword.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Error message banner
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = Color(0xFF991B1B),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer note
        Text(
            text = "CareFlow • Smart Healthcare Solutions",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
        )
    }

    // ==========================================
    // UNIVERSAL 6-DIGIT FORGOT PASSWORD DIALOG
    // (Industry Standard - Instagram / Snapchat Style)
    // ==========================================
    if (showUniversalForgotDialog) {
        val session = uiState.resetSession
        val roleTitle = when (forgotTargetRole) {
            UserRole.DOCTOR -> "Doctor Chamber"
            UserRole.ADMIN -> "Admin Console"
            UserRole.PATIENT -> "Patient Account"
        }
        val roleIcon = when (forgotTargetRole) {
            UserRole.DOCTOR -> Icons.Default.MedicalServices
            UserRole.ADMIN -> Icons.Default.AdminPanelSettings
            UserRole.PATIENT -> Icons.Default.Lock
        }
        val dialogTitleText = when {
            !uiState.isUniversalResetOtpSent || session == null -> "$roleTitle Password Reset"
            !uiState.isUniversalResetOtpVerified -> "$roleTitle - Confirm Code"
            else -> "$roleTitle - Set New Password"
        }

        AlertDialog(
            onDismissRequest = {
                showUniversalForgotDialog = false
                onClearUniversalResetState()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(roleIcon, contentDescription = null, tint = PulseRedBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dialogTitleText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // IN-DIALOG ERROR NOTIFICATION BANNER: Always rendered right over the reset dialog popup
                    if (!uiState.errorMessage.isNullOrBlank()) {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = PulseRedBright,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.errorMessage ?: "",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF991B1B),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    if (!uiState.isUniversalResetOtpSent || session == null) {
                        // --- STEP 1: Email Address Identifier ---
                        val isDocPortal = forgotTargetRole == UserRole.DOCTOR
                        Text(
                            text = if (isDocPortal) {
                                "Enter your registered Doctor Email Address. CareFlow will send a 6-digit verification code to reset your password."
                            } else {
                                "Enter your registered Email Address. CareFlow will send a 6-digit verification code to your email inbox."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF475569)
                        )

                        val cleanForgot = forgotInputIdentifier.replace("+", "").replace(" ", "").replace("-", "")
                        val isEmailValidFormat = forgotInputIdentifier.isBlank() ||
                                (forgotInputIdentifier.contains("@") && forgotInputIdentifier.contains(".")) ||
                                (cleanForgot.length >= 10 && cleanForgot.all { it.isDigit() }) ||
                                forgotInputIdentifier.equals("sarfaraz", ignoreCase = true) ||
                                forgotInputIdentifier.equals("sarf", ignoreCase = true)

                        OutlinedTextField(
                            value = forgotInputIdentifier,
                            onValueChange = { forgotInputIdentifier = it.trim() },
                            label = { Text("Registered Email Address or Phone") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = PulseCrimsonDark
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            isError = !isEmailValidFormat,
                            supportingText = if (!isEmailValidFormat) {
                                { Text("Please enter a valid email address or registered phone number", color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                            } else null
                        )

                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Security code will be dispatched to your registered email address. Please check your inbox or spam folder.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    } else if (!uiState.isUniversalResetOtpVerified) {
                        // --- STEP 2: Verify 6-Digit OTP Only ---
                        val rawEmail = session.email
                        val maskedEmail = if (rawEmail.contains("@")) {
                            val parts = rawEmail.split("@")
                            val namePart = parts[0]
                            val domain = parts.getOrElse(1) { "" }
                            if (namePart.length > 2) "${namePart.take(2)}••••${namePart.takeLast(1)}@$domain" else "$namePart@$domain"
                        } else rawEmail

                        Text(
                            text = if (maskedEmail.isNotBlank()) {
                                "Please enter the 6-digit verification code sent to $maskedEmail"
                            } else {
                                "Please enter the 6-digit verification code sent to your registered email address."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF475569)
                        )

                        // ONLY the 6-digit OTP code input
                        OutlinedTextField(
                            value = forgotOtpInput,
                            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) forgotOtpInput = it },
                            label = { Text("Enter 6-Digit Code") },
                            placeholder = { Text("••••••") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PulseCrimsonDark) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Resend Countdown Timer Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (resendCooldown > 0) {
                                Text(
                                    text = "Resend code in ${resendCooldown}s",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                TextButton(
                                    onClick = {
                                        onRequestUniversalReset(
                                            forgotInputIdentifier,
                                            forgotTargetRole,
                                            null
                                        )
                                    }
                                ) {
                                    Text(
                                        text = "Didn't receive code? Resend",
                                        fontSize = 12.sp,
                                        color = PulseRedBright,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        // --- STEP 3: OTP Verified -> Enter New Password & Confirm Password ---
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Code Verified Successfully!",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                    Text(
                                        text = "Now choose a strong new password for your account.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF1E3A8A)
                                    )
                                }
                            }
                        }

                        // New Password input
                        OutlinedTextField(
                            value = forgotNewPassword,
                            onValueChange = { forgotNewPassword = it },
                            label = { Text("New Password") },
                            placeholder = { Text("Min. 6 characters") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PulseCrimsonDark) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Confirm Password input
                        OutlinedTextField(
                            value = forgotConfirmPassword,
                            onValueChange = { forgotConfirmPassword = it },
                            label = { Text("Confirm New Password") },
                            placeholder = { Text("Re-enter new password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PulseCrimsonDark) },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = forgotConfirmPassword.isNotBlank() && forgotConfirmPassword != forgotNewPassword,
                            supportingText = {
                                if (forgotConfirmPassword.isNotBlank() && forgotConfirmPassword != forgotNewPassword) {
                                    Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                when {
                    !uiState.isUniversalResetOtpSent || session == null -> {
                        val cleanForgot = forgotInputIdentifier.replace("+", "").replace(" ", "").replace("-", "")
                        val isEmailValid = forgotInputIdentifier.isNotBlank() && (
                                (forgotInputIdentifier.contains("@") && forgotInputIdentifier.contains(".")) ||
                                (cleanForgot.length >= 10 && cleanForgot.all { it.isDigit() }) ||
                                forgotInputIdentifier.equals("sarfaraz", ignoreCase = true) ||
                                forgotInputIdentifier.equals("sarf", ignoreCase = true)
                        )
                        Button(
                            onClick = {
                                if (isEmailValid) {
                                    onRequestUniversalReset(
                                        forgotInputIdentifier,
                                        forgotTargetRole,
                                        null
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PulseRed),
                            shape = RoundedCornerShape(10.dp),
                            enabled = isEmailValid && !uiState.isLoading
                        ) {
                            Text(
                                if (uiState.isLoading) "Sending..." else "Send 6-Digit Code",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    !uiState.isUniversalResetOtpVerified -> {
                        Button(
                            onClick = {
                                if (forgotOtpInput.length == 6) {
                                    onValidateUniversalResetOtp(session.resetId, forgotOtpInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PulseRed),
                            shape = RoundedCornerShape(10.dp),
                            enabled = forgotOtpInput.length == 6 && !uiState.isLoading
                        ) {
                            Text(
                                if (uiState.isLoading) "Verifying..." else "Confirm Code",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        Button(
                            onClick = {
                                if (forgotNewPassword.length >= 6 && forgotNewPassword == forgotConfirmPassword) {
                                    onVerifyUniversalReset(session.resetId, forgotOtpInput, forgotNewPassword)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PulseRed),
                            shape = RoundedCornerShape(10.dp),
                            enabled = forgotNewPassword.length >= 6 &&
                                    forgotNewPassword == forgotConfirmPassword &&
                                    !uiState.isLoading
                        ) {
                            Text(
                                if (uiState.isLoading) "Updating..." else "Update Password",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUniversalForgotDialog = false
                    onClearUniversalResetState()
                }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }
}



