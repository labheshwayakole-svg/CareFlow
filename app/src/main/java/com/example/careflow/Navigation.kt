package com.example.careflow

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.model.UserRole
import com.example.careflow.presentation.admin.AdminDashboardScreen
import com.example.careflow.presentation.admin.AdminViewModel
import com.example.careflow.presentation.auth.AuthScreen
import com.example.careflow.presentation.auth.AuthViewModel
import com.example.careflow.presentation.doctor.DoctorDashboardScreen
import com.example.careflow.presentation.doctor.DoctorViewModel
import com.example.careflow.presentation.patient.DoctorBookingScreen
import com.example.careflow.presentation.patient.HospitalSearchScreen
import com.example.careflow.presentation.patient.IncomingCallAlertOverlay
import com.example.careflow.presentation.patient.MyAppointmentsScreen
import com.example.careflow.presentation.patient.NotificationsScreen
import com.example.careflow.presentation.patient.PatientHomeScreen
import com.example.careflow.presentation.patient.PatientViewModel
import com.example.careflow.presentation.patient.QueueTrackerScreen

sealed interface PatientScreenDestination {
    data object Home : PatientScreenDestination
    data class Booking(val doctor: Doctor) : PatientScreenDestination
    data object QueueTracker : PatientScreenDestination
    data object MyAppointments : PatientScreenDestination
    data object Notifications : PatientScreenDestination
}

@Composable
fun MainNavigation(
    authViewModel: AuthViewModel = viewModel(),
    patientViewModel: PatientViewModel = viewModel(),
    doctorViewModel: DoctorViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val patientState by patientViewModel.uiState.collectAsStateWithLifecycle()
    val doctorState by doctorViewModel.uiState.collectAsStateWithLifecycle()
    val adminState by adminViewModel.uiState.collectAsStateWithLifecycle()

    var patientDestination by remember { mutableStateOf<PatientScreenDestination>(PatientScreenDestination.Home) }
    var showRootBackDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    // Handle system notification tap: navigate directly to QueueTracker
    val activity = context as? android.app.Activity
    LaunchedEffect(activity?.intent) {
        val navExtra = activity?.intent?.getStringExtra("EXTRA_NAVIGATE_TO")
        if (navExtra == "QUEUE_TRACKER") {
            patientDestination = PatientScreenDestination.QueueTracker
            activity.intent?.removeExtra("EXTRA_NAVIGATE_TO")
        }
    }

    fun handleRootBackExit() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000L) {
            (context as? android.app.Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
    }

    // User-friendly Snackbar error & success messaging (Rule #5)
    val messageToShow = authState.errorMessage
        ?: authState.successMessage
        ?: patientState.errorMessage
        ?: patientState.successMessage
        ?: doctorState.errorMessage
        ?: doctorState.successMessage
        ?: adminState.errorMessage
        ?: adminState.successMessage

    LaunchedEffect(messageToShow) {
        if (messageToShow != null) {
            snackbarHostState.showSnackbar(messageToShow)
            authViewModel.clearMessages()
            patientViewModel.clearMessages()
            doctorViewModel.clearMessages()
            adminViewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize().safeDrawingPadding()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val user = authState.user

            if (user == null) {
                BackHandler {
                    handleRootBackExit()
                }

                AuthScreen(
                    uiState = authState,
                    onLoginClick = { email, pass -> authViewModel.login(email, pass) },
                    onRegisterClick = { name, email, pass, phone, role ->
                        authViewModel.register(name, email, pass, phone, role)
                    },
                    onSendOtpClick = { activity, phone ->
                        authViewModel.sendPhoneOtp(activity, phone)
                    },
                    onVerifyOtpClick = { code, name, role ->
                        authViewModel.verifyPhoneOtp(code, name, role)
                    },
                    onGoogleSignInSuccess = { idToken, role ->
                        authViewModel.signInWithGoogle(idToken, role)
                    },
                    onGoogleSignInAccountFallback = { email, name, role ->
                        authViewModel.signInWithGoogleAccount(email, name, role)
                    },
                    onDoctorLoginClick = { idOrEmail, pass ->
                        authViewModel.loginDoctor(idOrEmail, pass)
                    },
                    onAdminLoginClick = { email, pass ->
                        authViewModel.loginAdmin(email, pass)
                    },
                    onAdminRegisterClick = { name, email, pass, phone ->
                        authViewModel.registerAdmin(name, email, pass, phone)
                    },
                    onForgotPasswordClick = { email ->
                        authViewModel.sendPasswordResetEmail(email)
                    },
                    onRequestDoctorResetOtp = { idOrEmail ->
                        authViewModel.requestDoctorPasswordReset(idOrEmail)
                    },
                    onVerifyDoctorResetOtp = { idOrEmail, otp, newPass ->
                        authViewModel.verifyDoctorPasswordReset(idOrEmail, otp, newPass)
                    },
                    onClearDoctorResetState = {
                        authViewModel.clearDoctorResetState()
                    },
                    onRequestUniversalReset = { idEmailOrPhone, role, optionalPhone ->
                        authViewModel.requestUniversalPasswordReset(idEmailOrPhone, role, optionalPhone)
                    },
                    onValidateUniversalResetOtp = { resetId, otp ->
                        authViewModel.validateUniversalResetOtp(resetId, otp)
                    },
                    onVerifyUniversalReset = { resetId, otp, newPass ->
                        authViewModel.verifyUniversalPasswordReset(resetId, otp, newPass)
                    },
                    onClearUniversalResetState = {
                        authViewModel.clearUniversalResetState()
                    }
                )
            } else {
                when (user.role) {
                    UserRole.PATIENT -> {
                        if (patientState.selectedHospital == null) {
                            BackHandler {
                                showRootBackDialog = true
                            }

                            HospitalSearchScreen(
                                uiState = patientState,
                                onSelectHospital = { hosp -> patientViewModel.selectHospital(hosp) },
                                onLogoutClick = {
                                    authViewModel.logout()
                                    patientViewModel.clearSelectedHospital()
                                    patientDestination = PatientScreenDestination.Home
                                }
                            )
                        } else {
                            if (patientDestination != PatientScreenDestination.Home) {
                                BackHandler {
                                    patientDestination = PatientScreenDestination.Home
                                }
                            } else {
                                BackHandler {
                                    patientViewModel.clearSelectedHospital()
                                }
                            }

                            AnimatedContent(
                                targetState = patientDestination,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "PatientNav"
                            ) { destination ->
                                when (destination) {
                                    is PatientScreenDestination.Home -> {
                                        PatientHomeScreen(
                                            uiState = patientState,
                                            onSelectHospital = { hosp -> patientViewModel.selectHospital(hosp) },
                                            onChangeHospitalClick = {
                                                patientViewModel.clearSelectedHospital()
                                                patientDestination = PatientScreenDestination.Home
                                            },
                                            onSelectDepartment = { deptId -> patientViewModel.selectDepartment(deptId) },
                                            onToggleFilterInOpd = { patientViewModel.toggleFilterOnlyInOpd() },
                                            onSearchQueryChange = { query -> patientViewModel.setSearchQuery(query) },
                                            onSelectDoctorForBooking = { doc ->
                                                patientViewModel.selectDoctor(doc)
                                                patientDestination = PatientScreenDestination.Booking(doc)
                                            },
                                            onOpenQueueTracker = {
                                                patientDestination = PatientScreenDestination.QueueTracker
                                            },
                                            onOpenMyAppointments = {
                                                patientDestination = PatientScreenDestination.MyAppointments
                                            },
                                            onOpenNotifications = {
                                                patientDestination = PatientScreenDestination.Notifications
                                            },
                                            onMarkNotificationRead = { notifId ->
                                                patientViewModel.markNotificationAsRead(notifId)
                                            },
                                            onMarkAllNotificationsRead = {
                                                patientViewModel.markAllNotificationsAsRead()
                                            },
                                            onLogoutClick = {
                                                authViewModel.logout()
                                                patientViewModel.clearSelectedHospital()
                                                patientDestination = PatientScreenDestination.Home
                                            }
                                        )
                                    }
                                is PatientScreenDestination.Booking -> {
                                    DoctorBookingScreen(
                                        doctor = destination.doctor,
                                        uiState = patientState,
                                        onSelectDate = { date ->
                                            patientViewModel.selectBookingDate(date)
                                        },
                                        onBookClick = { date, slot, notes, symptoms ->
                                            patientViewModel.bookAppointment(date, slot, notes, symptoms)
                                        },
                                        onTrackQueueClick = {
                                            patientDestination = PatientScreenDestination.QueueTracker
                                        },
                                        onBackClick = {
                                            patientDestination = PatientScreenDestination.Home
                                        }
                                    )
                                }
                                is PatientScreenDestination.QueueTracker -> {
                                    QueueTrackerScreen(
                                        uiState = patientState,
                                        onBackClick = {
                                            patientDestination = PatientScreenDestination.Home
                                        }
                                    )
                                }
                                is PatientScreenDestination.MyAppointments -> {
                                    MyAppointmentsScreen(
                                        uiState = patientState,
                                        onTrackAppointmentQueue = { apt ->
                                            patientViewModel.trackAppointmentQueue(apt)
                                            patientDestination = PatientScreenDestination.QueueTracker
                                        },
                                        onCancelAppointment = { aptId ->
                                            patientViewModel.cancelAppointment(aptId)
                                        },
                                        onBackClick = {
                                            patientDestination = PatientScreenDestination.Home
                                        }
                                    )
                                }
                                is PatientScreenDestination.Notifications -> {
                                    NotificationsScreen(
                                        uiState = patientState,
                                        onBackClick = {
                                            patientDestination = PatientScreenDestination.Home
                                        },
                                        onMarkNotificationRead = { notifId ->
                                            patientViewModel.markNotificationAsRead(notifId)
                                        },
                                        onMarkAllNotificationsRead = {
                                            patientViewModel.markAllNotificationsAsRead()
                                        },
                                        onDeleteNotification = { notifId ->
                                            patientViewModel.deleteNotification(notifId)
                                        },
                                        onClearAllNotifications = {
                                            patientViewModel.clearAllNotifications()
                                        },
                                        onNavigateToQueue = {
                                            patientDestination = PatientScreenDestination.QueueTracker
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                UserRole.DOCTOR -> {
                    BackHandler {
                        showRootBackDialog = true
                    }

                    DoctorDashboardScreen(
                        uiState = doctorState,
                        onCallNextPatient = { doctorViewModel.callNextPatient() },
                        onCallPreviousPatient = { doctorViewModel.callPreviousPatient() },
                        onCallSpecificPatient = { apt -> doctorViewModel.callSpecificPatient(apt) },
                        onCompleteConsultation = { notes -> doctorViewModel.completeCurrentConsultation(notes) },
                        onMarkNoShow = { doctorViewModel.markCurrentNoShow() },
                        onToggleOpdPresence = { isInOpd -> doctorViewModel.toggleOpdPresence(isInOpd) },
                        onToggleBreakMode = { onBreak, msg -> doctorViewModel.toggleBreakMode(onBreak, msg) },
                        onToggleAvailability = { isAvailable -> doctorViewModel.toggleAvailability(isAvailable) },
                        onResetQueue = { doctorViewModel.resetQueue() },
                        onLogoutClick = { authViewModel.logout() },
                        onClearMessages = { doctorViewModel.clearMessages() }
                    )
                }

                UserRole.ADMIN -> {
                    BackHandler {
                        showRootBackDialog = true
                    }

                    AdminDashboardScreen(
                        uiState = adminState,
                        onSaveHospital = { name, opd, addr, city, phone ->
                            adminViewModel.saveHospitalDetails(name, opd, addr, city, phone)
                        },
                        onToggleDoctorAvailability = { docId, cur ->
                            adminViewModel.toggleDoctorAvailability(docId, cur)
                        },
                        onAddDoctor = { name, spec, deptId, deptName, qual, exp, fee, start, end, tokens ->
                            adminViewModel.addDoctorWithCredentials(
                                name, spec, deptId, deptName, qual, exp, fee, start, end, tokens
                            )
                        },
                        onAddDepartment = { name, desc, icon ->
                            adminViewModel.addDepartment(name, desc, icon)
                        },
                        onDismissCredentials = { adminViewModel.dismissCredentialsDialog() },
                        onLogoutClick = { authViewModel.logout() },
                        onClearMessages = { adminViewModel.clearMessages() },
                        onDismissDoctorResetRequest = { reqId ->
                            adminViewModel.dismissDoctorResetRequest(reqId)
                        },
                        onRemoveDoctor = { docId ->
                            adminViewModel.removeDoctor(docId)
                        },
                        onUpdateAdminEmail = { newEmail ->
                            adminViewModel.updateAdminEmail(newEmail)
                        },
                        onUpdateAdminAndHospitalProfile = { adminName, adminEmail, adminPhone, hospName, opd, addr, city, phone ->
                            adminViewModel.updateAdminAndHospitalProfile(
                                adminName, adminEmail, adminPhone, hospName, opd, addr, city, phone
                            )
                        }
                    )
                }
            }
        }

        // Universal High-urgency in-app call overlay for patient (displayed over ANY screen)
        patientState.incomingCallAlert?.let { alert ->
            IncomingCallAlertOverlay(
                notification = alert,
                onDismiss = { patientViewModel.dismissCallAlert() },
                onNavigateToQueue = {
                    patientViewModel.dismissCallAlert()
                    val targetHospitalId = alert.hospitalId.orEmpty().ifBlank {
                        patientState.myAppointments.find { it.id == alert.appointmentId }?.hospitalId.orEmpty()
                    }
                    if (patientState.selectedHospital == null && targetHospitalId.isNotBlank()) {
                        val matchHosp = patientState.hospitals.find { it.id == targetHospitalId }
                        if (matchHosp != null) {
                            patientViewModel.selectHospital(matchHosp)
                        }
                    }
                    val targetApt = patientState.myAppointments.find { it.id == alert.appointmentId }
                    if (targetApt != null) {
                        patientViewModel.trackAppointmentQueue(targetApt)
                    }
                    patientDestination = PatientScreenDestination.QueueTracker
                }
            )
        }
    }
}

    if (showRootBackDialog) {
        val roleName = when (authState.user?.role) {
            UserRole.ADMIN -> "Admin"
            UserRole.DOCTOR -> "Doctor"
            UserRole.PATIENT -> "Patient"
            null -> "User"
        }
        AlertDialog(
            onDismissRequest = { showRootBackDialog = false },
            title = {
                Text(
                    text = "Exit $roleName Portal?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Would you like to log out of your $roleName account to switch portals, or exit the application?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRootBackDialog = false
                        authViewModel.logout()
                        patientViewModel.clearSelectedHospital()
                        patientDestination = PatientScreenDestination.Home
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            showRootBackDialog = false
                            (context as? android.app.Activity)?.finish()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Exit App")
                    }
                    TextButton(onClick = { showRootBackDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}
