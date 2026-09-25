package com.example.careflow

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.example.careflow.presentation.patient.MyAppointmentsScreen
import com.example.careflow.presentation.patient.PatientHomeScreen
import com.example.careflow.presentation.patient.PatientViewModel
import com.example.careflow.presentation.patient.QueueTrackerScreen

sealed interface PatientScreenDestination {
    data object Home : PatientScreenDestination
    data class Booking(val doctor: Doctor) : PatientScreenDestination
    data object QueueTracker : PatientScreenDestination
    data object MyAppointments : PatientScreenDestination
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
    val snackbarHostState = remember { SnackbarHostState() }

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
                    onSwitchDemoRole = { role -> authViewModel.switchDemoRole(role) }
                )
            } else {
                when (user.role) {
                    UserRole.PATIENT -> {
                        AnimatedContent(
                            targetState = patientDestination,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "PatientNav"
                        ) { destination ->
                            when (destination) {
                                is PatientScreenDestination.Home -> {
                                    PatientHomeScreen(
                                        uiState = patientState,
                                        onSelectDepartment = { deptId -> patientViewModel.selectDepartment(deptId) },
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
                                        onLogoutClick = {
                                            authViewModel.logout()
                                            patientDestination = PatientScreenDestination.Home
                                        }
                                    )
                                }
                                is PatientScreenDestination.Booking -> {
                                    DoctorBookingScreen(
                                        doctor = destination.doctor,
                                        uiState = patientState,
                                        onBookClick = { date, slot, notes ->
                                            patientViewModel.bookAppointment(date, slot, notes)
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
                            }
                        }
                    }

                    UserRole.DOCTOR -> {
                        DoctorDashboardScreen(
                            uiState = doctorState,
                            onCallNextPatient = { doctorViewModel.callNextPatient() },
                            onCompleteConsultation = { notes -> doctorViewModel.completeCurrentConsultation(notes) },
                            onMarkNoShow = { doctorViewModel.markCurrentNoShow() },
                            onToggleAvailability = { isAvailable -> doctorViewModel.toggleAvailability(isAvailable) },
                            onResetQueue = { doctorViewModel.resetQueue() },
                            onLogoutClick = { authViewModel.logout() }
                        )
                    }

                    UserRole.ADMIN -> {
                        AdminDashboardScreen(
                            uiState = adminState,
                            onToggleDoctorAvailability = { docId, cur ->
                                adminViewModel.toggleDoctorAvailability(docId, cur)
                            },
                            onAddDoctor = { name, spec, deptId, deptName, qual, fee, start, end ->
                                adminViewModel.addDoctor(name, spec, deptId, deptName, qual, fee, start, end)
                            },
                            onAddDepartment = { name, desc ->
                                adminViewModel.addDepartment(name, desc)
                            },
                            onLogoutClick = { authViewModel.logout() }
                        )
                    }
                }
            }
        }
    }
}
