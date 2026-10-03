# 🏥 CareFlow — Smart Hospital OPD & Live Queue Management System

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(Material%203)-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase%20Auth%20%7C%20Firestore-FFCA28.svg?style=flat&logo=firebase)](https://firebase.google.com/)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture%20%2B%20MVVM-blue.svg)](https://developer.android.com/topic/architecture)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36%20(Android%2014%2B)-009688.svg)](https://developer.android.com/)

**CareFlow** is an enterprise-grade hospital outpatient department (OPD) queue orchestration, doctor consultation management, and multi-specialty healthcare governance application built natively for Android. 

It eliminates chaotic hospital waiting room queues by replacing physical lines with real-time digital token passes, live countdown telemetry, high-urgency audio-visual consultation calls, and hospital administration consoles.

---

## 📑 Table of Contents
- [✨ Key Highlights](#-key-highlights)
- [📱 Portal Architecture & Features](#-portal-architecture--features)
  - [1. Patient Portal (Smart Turn Pass & OPD Discovery)](#1-patient-portal-smart-turn-pass--opd-discovery)
  - [2. Doctor Portal (Live Chamber & Queue Director)](#2-doctor-portal-live-chamber--queue-director)
  - [3. Admin Portal (Hospital Command Center)](#3-admin-portal-hospital-command-center)
  - [4. Multi-Role Account Isolation Under One Email](#4-multi-role-account-isolation-under-one-email)
- [🏗️ Architectural Design & System Flow](#️-architectural-design--system-flow)
- [📦 Technology Stack](#-technology-stack)
- [🚀 Getting Started & Setup](#-getting-started--setup)
- [🔐 Preloaded Demo & Testing Credentials](#-preloaded-demo--testing-credentials)
- [📂 Project Directory Structure](#-project-directory-structure)
- [🛡️ Security, Privacy & Invalidation Policies](#️-security-privacy--invalidation-policies)

---

## ✨ Key Highlights

* **100% Jetpack Compose Material 3 UI**: Modern medical design system featuring vibrant visual indicators, glassmorphic cards, dynamic queues, and accessible contrast palettes.
* **Real-Time Queue Telemetry**: Live queue tracking powered by Cloud Firestore listeners and reactive Kotlin `StateFlow`, updating wait estimates and token progress synchronously across patients and doctors.
* **Emergency & Consultation Call System**: High-urgency audio-visual call alerts overlaying the patient app immediately when the doctor summons their token into the chamber.
* **Multi-Role Profile Isolation**: A single email address can hold distinct, non-interfering profiles as a **Patient**, **Doctor**, and **Admin** across dedicated portal entries.
* **Strict Single-Admin Invalidation**: When an administrator updates their email address in the hospital settings, previous emails are instantly stripped of administrative status and unauthorized access is rejected.
* **Zero-Friction Booking**: Instant OPD token pass generation requiring only patient name and slot, eliminating booking friction.
* **Dual-Engine Architecture (Cloud + Offline Fallback)**: Full operational support using Firebase Cloud Firestore with automatic offline fallback to rich in-memory seed data.

---

## 📱 Portal Architecture & Features

### 1. Patient Portal (Smart Turn Pass & OPD Discovery)
Designed for patients seeking streamlined hospital consultations without crowded waiting rooms:
* **Multi-Hospital Network Selection**: Discover top hospitals (e.g., *CareFlow Multi-Specialty Hospital*, *Apollo Health City*, *Max Super Speciality*, *Fortis FMRI*, *Manipal Hospital*).
* **Specialized Department Navigation**: Explore departments including Cardiology, Neurology, Pediatrics, Orthopedics, Dermatology, ENT, and Dental Sciences.
* **Doctor Discovery & OPD Status**: View doctor profiles, verified qualifications, experience, consultation fees, ratings, active OPD shift timings, and current token load.
* **Instant Token Generation**: 1-Tap appointment booking providing a live digital token pass.
* **Live Queue Tracker**: Dynamic queue progress bar, estimated wait time, doctors serving token, and current distance from turn.
* **Full-Screen Audio-Visual Call Overlay**: Plays audio chime, device vibration, and a full-screen pop-up alert when called by the doctor.
* **Digital Appointment Vault**: Search, view, reschedule, or cancel upcoming and past appointments with digital token passes.

### 2. Doctor Portal (Live Chamber & Queue Director)
Built for physicians to manage patient throughput and chamber operations:
* **Chamber Queue Console**: View today's live patient queue ordered by token number.
* **Patient Status Lifecycle**: Advance patients through `PENDING` ➔ `CALLED` ➔ `IN_CONSULTATION` ➔ `COMPLETED` or `SKIPPED / ABSENT`.
* **One-Tap Patient Summoning**: Triggers high-priority audio-visual alerts on the patient's device and updates the hospital digital signage board.
* **Digital Consultation & Prescriptions**: Record diagnoses, clinical observations, and medications tied to patient appointment records.
* **OPD Presence & Break Mode**: Toggle availability (`In OPD Chamber` vs. `Out of Chamber`) and activate Break Mode with custom status messages displayed to waiting patients.
* **Doctor Password Reset Flow**: Secure self-service reset request sent directly to the Hospital Admin console for OTP verification.

### 3. Admin Portal (Hospital Command Center)
Equipped for hospital administrators to oversee hospital operations and staffing:
* **Hospital Profile & Facility Management**: Configure hospital names, outpatient concourses, emergency helplines, addresses, and administrator identity.
* **Doctor Onboarding & Credential Generation**: Register new doctors with auto-generated secure credentials (`Doctor ID`, `login email`, `temporary password`), specialty, department, shift timings, and daily token quotas.
* **Department Architecture**: Create, update, or remove medical departments with specialized descriptions and medical icons.
* **Doctor Reset Request Approvals**: Approve or dismiss doctor password reset requests and securely dispatch OTP codes.
* **Hospital Analytics & Live OPD Telemetry**: Real-time overview of active doctors, tokens issued, completed consultations, and patient flow rates.
* **Strict Admin Email Invalidation**: Updating admin email immediately reassigns administrative authority, invalidating previous credentials from admin portal access.

### 4. Multi-Role Account Isolation Under One Email
CareFlow introduces segregated role scoping:
```
                      User: example@hospital.com
                                   │
         ┌─────────────────────────┼─────────────────────────┐
         ▼                         ▼                         ▼
   Patient Portal            Doctor Portal             Admin Portal
   - Patient ID: pat_123     - Doctor ID: DOC-CF-100   - Admin ID: adm-main
   - Role: PATIENT           - Role: DOCTOR            - Role: ADMIN
   - Personal Appointments   - Consultation Queue      - Hospital Governance
   - Token Passes & History  - Chamber Shift & Notes   - Staff & OPD Config
```
* Logging in through the **Patient Tab** accesses the patient experience and personal health records.
* Logging in through the **Doctor Tab** verifies physician directory status and opens the consultation queue.
* Logging in through the **Admin Tab** validates active hospital administrative rights.

---

## 🏗️ Architectural Design & System Flow

CareFlow adheres to **Clean Architecture** and **MVVM** principles with unidirectional data flow (UDF):

```
┌───────────────────────────────────────────────────────────────────┐
│                     Presentation Layer (UI)                      │
│   Jetpack Compose Screens  •  StateFlow Observation  •  Material 3 │
│  (PatientHomeScreen, DoctorDashboardScreen, AdminDashboardScreen) │
└─────────────────────────────────┬─────────────────────────────────┘
                                  │ Observes State & Dispatches Events
┌─────────────────────────────────▼─────────────────────────────────┐
│                        ViewModel Layer                           │
│  PatientViewModel  •  DoctorViewModel  •  AdminViewModel  •  Auth  │
└─────────────────────────────────┬─────────────────────────────────┘
                                  │ Executes Domain Logic
┌─────────────────────────────────▼─────────────────────────────────┐
│                         Domain Layer                              │
│   Use Cases: BookAppointmentUseCase, AdvanceQueueUseCase, etc.     │
│   Domain Models: Hospital, Doctor, Appointment, DailyQueue, User  │
│   Repository Interfaces: AuthRepository, AppointmentRepository... │
└─────────────────────────────────┬─────────────────────────────────┘
                                  │ Implements Interfaces
┌─────────────────────────────────▼─────────────────────────────────┐
│                          Data Layer                               │
│   Repositories: AuthRepositoryImpl, AppointmentRepositoryImpl...  │
│   Remote Source: FirebaseDataSource (Auth + Cloud Firestore)      │
│   Local Source: UserSessionManager (SharedPreferences)            │
│   In-Memory Fallback: InMemoryDataStore + CareFlowSeedData        │
└───────────────────────────────────────────────────────────────────┘
```

---

## 📦 Technology Stack

| Layer | Component | Description |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.0+ | Exclusive modern Kotlin with Coroutines & Serialization |
| **UI Toolkit** | Jetpack Compose (BOM) | 100% declarative UI with Material 3 components |
| **Architecture** | MVVM + Clean Architecture | Strict separation of concerns (Presentation, Domain, Data) |
| **Asynchrony** | Coroutines & Flow | `StateFlow`, `SharedFlow`, `callbackFlow` for real-time streams |
| **Dependency Wiring** | ServiceLocator | Centralized, lifecycle-safe dependency provider |
| **Backend & Database**| Firebase Auth & Cloud Firestore | Real-time collection snapshots and cloud authentication |
| **Session Persistence**| UserSessionManager | Role-aware local encrypted session caching |
| **Target SDK** | SDK 36 (Android 14+) | Min SDK 24 (compatible with Android 7.0 Nougat and above) |

---

## 🚀 Getting Started & Setup

### Prerequisites
* **Android Studio**: Ladybug / Hedgehog or newer (2024.1+)
* **JDK**: Java Development Kit 17 (JVM Toolchain 17 configured)
* **Android SDK**: API Level 36 installed via SDK Manager
* **Firebase Project**: Configured with `google-services.json` in the `/app` directory

### Build & Run
1. **Clone the repository**:
   ```bash
   git clone https://github.com/shaikhsarfarazss/CareFlow.git
   cd CareFlow
   ```
2. **Verify Gradle & Dependencies**:
   ```bash
   ./gradlew compileDebugKotlin
   ```
3. **Assemble the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   *The generated APK will be available under: `app/build/outputs/apk/debug/app-debug.apk`*
4. **Deploy to Device or Emulator**:
   ```bash
   ./gradlew installDebug
   ```

---

## 🔐 Preloaded Demo & Testing Credentials

For demonstration, academic review, and local offline testing, CareFlow includes comprehensive seed accounts:

### 1. Hospital Administrator Accounts
| Hospital | Admin Email | Default Password | Primary Contact |
| :--- | :--- | :--- | :--- |
| **CareFlow Multi-Specialty Hospital (Mumbai)** | `sarf0203araz@gmail.com` | `Admin@1234` | `7219889504` |
| **Apollo Health City (Bengaluru)** | `admin@apollo.com` | `Admin@1234` | `+91 80 2630 4050` |
| **Max Super Speciality (New Delhi)** | `admin@maxhealth.com` | `Admin@1234` | `+91 11 2651 5050` |
| **Fortis Memorial (Gurugram)** | `admin@fortis.com` | `Admin@1234` | `+91 124 492 1021` |
| **Manipal Hospital (Pune)** | `admin@manipal.com` | `Admin@1234` | `+91 20 6723 8888` |

### 2. Lead Doctor Accounts
| Doctor Name | Department | Doctor ID / Email | Password |
| :--- | :--- | :--- | :--- |
| **Dr. Sarfaraz Shaikh** | Cardiology & Heart Sciences | `DOC-CF-100` / `dr.sarfaraz@careflow.hospital` | `Doctor@1234` |
| **Dr. Priya Sharma** | General Medicine & OPD | `DOC-CF-101` / `dr.priya@careflow.hospital` | `Doctor@1234` |
| **Dr. Rajesh Kulkarni** | Orthopedics & Joint Care | `DOC-CF-102` / `dr.rajesh@careflow.hospital` | `Doctor@1234` |
| **Dr. Ananya Deshmukh** | Pediatrics & Neonatology | `DOC-CF-103` / `dr.ananya@careflow.hospital` | `Doctor@1234` |
| **Dr. Vikram Patil** | Dermatology & Skin Surgery | `DOC-CF-104` / `dr.vikram@careflow.hospital` | `Doctor@1234` |

### 3. Patient Test Accounts
| Patient Name | Email | Phone Number | Default Password |
| :--- | :--- | :--- | :--- |
| **Sarfaraz Shaikh (Patient Profile)** | `sarf0203araz@gmail.com` | `7219889504` | `Patient@1234` |
| **Rahul Sharma** | `rahul.sharma@gmail.com` | `+91 9876543210` | `Patient@1234` |
| **Pooja Verma** | `pooja.verma@gmail.com` | `+91 9812345678` | `Patient@1234` |

*(Note: In offline testing mode, generic fallback codes such as `123456` or `12341234` can also be used for verification).*

---

## 📂 Project Directory Structure

```
CareFlow/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/careflow/
│   │   │   │   ├── CareFlowApp.kt            # Application Class & Global Init
│   │   │   │   ├── MainActivity.kt           # Single-Activity Compose Host
│   │   │   │   ├── Navigation.kt             # Navigation 3 Host & Role Routing
│   │   │   │   ├── core/                     # Core cross-cutting utilities
│   │   │   │   │   ├── di/ServiceLocator.kt  # Dependency Provider
│   │   │   │   │   ├── dispatcher/           # Coroutine Dispatcher abstractions
│   │   │   │   │   ├── notification/         # CallNotificationService & Overlays
│   │   │   │   │   └── result/Resource.kt    # Generic Resource<T> wrapper
│   │   │   │   ├── domain/                   # Business rules & abstractions
│   │   │   │   │   ├── model/                # Hospital, Doctor, Appointment, User...
│   │   │   │   │   ├── repository/           # Repository interfaces
│   │   │   │   │   └── usecase/              # BookAppointment, AdvanceQueue...
│   │   │   │   ├── data/                     # Data access & implementations
│   │   │   │   │   ├── datasource/           # InMemoryDataStore & SeedData
│   │   │   │   │   ├── firebase/             # FirebaseDataSource (Auth & Firestore)
│   │   │   │   │   ├── local/                # UserSessionManager (SharedPrefs)
│   │   │   │   │   └── repository/           # Repository implementations
│   │   │   │   ├── presentation/             # UI Layer (M3 Composables & ViewModels)
│   │   │   │   │   ├── admin/                # AdminDashboard & AdminViewModel
│   │   │   │   │   ├── auth/                 # Multi-role AuthScreen & AuthViewModel
│   │   │   │   │   ├── doctor/               # DoctorDashboard & DoctorViewModel
│   │   │   │   │   ├── patient/              # Booking, QueueTracker, Home, Overlays
│   │   │   │   │   └── components/           # Reusable UI widgets & Badges
│   │   │   │   └── theme/                    # Theme, Color palettes & Typography
│   │   │   └── res/                          # Drawables, mipmaps, values
│   │   └── test/                             # Unit tests
│   └── build.gradle.kts
├── firestore.rules                           # Cloud Firestore Security Rules
├── gradle/libs.versions.toml                 # Version Catalog
└── README.md
```

---

## 🛡️ Security, Privacy & Invalidation Policies

1. **Strict Admin Previous Email Invalidation**:
   * Changing the Administrator email updates `hospitals/{id}.adminEmail`.
   * All previous Administrator user records are downgraded in Firestore to `PATIENT`.
   * Any subsequent login attempt with the previous email into the Admin Portal is blocked with an explicit unauthorized response.
2. **Multi-Role Scoped Data Access**:
   * Patient appointments, doctor queues, and hospital settings are strictly decoupled.
   * A single user authenticated across different portals has separate, role-scoped identity identifiers (`patient_<hash>`, `DOC-CF-100`, `admin_<hosp>`).
3. **Emergency Call Broadcast Guard**:
   * Consultation alerts utilize background-aware broadcast receivers and high-priority notification channels so patient alerts trigger reliably across device states.
4. **Coroutine Threading**:
   * Network, Firestore queries, and local data mutations are executed strictly within `Dispatchers.IO`, keeping the Main Compose thread fluid at 60/120 FPS.

---

## 📄 License
This project is proprietary and developed as part of the **CareFlow Healthcare Suite**. All rights reserved.
