package com.example.careflow.data.firebase

import android.app.Activity
import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.model.Hospital
import com.example.careflow.domain.model.Notification
import com.example.careflow.domain.model.NotificationType
import com.example.careflow.domain.model.QueueStatus
import com.example.careflow.domain.model.User
import com.example.careflow.domain.model.UserRole
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.TimeUnit

class FirebaseDataSource(
    val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ==========================================
    // --- Authentication & User Profile ---
    // ==========================================

    fun observeAuthState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithEmail(email: String, pass: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: return Resource.Error("User ID not returned by Firebase.")
            val user = getUserProfile(uid)
            if (user != null) {
                Resource.Success(user)
            } else {
                val defaultUser = User(
                    id = uid,
                    name = result.user?.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    email = email,
                    phone = "",
                    role = if (email.contains("doc", ignoreCase = true)) UserRole.DOCTOR else if (email.contains("admin", ignoreCase = true)) UserRole.ADMIN else UserRole.PATIENT
                )
                saveUserProfile(defaultUser)
                Resource.Success(defaultUser)
            }
        } catch (e: Exception) {
            val msg = e.message.orEmpty()
            if (msg.contains("no user record", ignoreCase = true) ||
                msg.contains("user-not-found", ignoreCase = true) ||
                msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true)
            ) {
                try {
                    val createRes = auth.createUserWithEmailAndPassword(email, pass).await()
                    val uid = createRes.user?.uid ?: return Resource.Error("Failed to auto-create user.")
                    val defaultUser = User(
                        id = uid,
                        name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = email,
                        phone = "",
                        role = if (email.contains("doc", ignoreCase = true)) UserRole.DOCTOR else if (email.contains("admin", ignoreCase = true)) UserRole.ADMIN else UserRole.PATIENT
                    )
                    saveUserProfile(defaultUser)
                    return Resource.Success(defaultUser)
                } catch (_: Exception) {
                    // Fall through
                }
            }
            Resource.Error(e.message ?: "Authentication failed", e)
        }
    }

    suspend fun registerWithEmail(
        name: String,
        email: String,
        pass: String,
        phone: String,
        role: UserRole
    ): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: return Resource.Error("Failed to create user.")
            val user = User(
                id = uid,
                name = name,
                email = email,
                phone = phone,
                role = role
            )
            saveUserProfile(user)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed", e)
        }
    }

    suspend fun signInWithGoogleIdToken(idToken: String, role: UserRole): Resource<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Resource.Error("Google sign in failed.")
            val existing = getUserProfile(firebaseUser.uid)
            if (existing != null) {
                Resource.Success(existing)
            } else {
                val newUser = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Google User",
                    email = firebaseUser.email ?: "",
                    phone = firebaseUser.phoneNumber ?: "",
                    role = role
                )
                saveUserProfile(newUser)
                Resource.Success(newUser)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Google sign in error", e)
        }
    }

    fun startPhoneVerification(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onAutoVerified: (PhoneAuthCredential) -> Unit,
        onError: (String) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                onAutoVerified(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError(e.message ?: "Phone verification failed")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential, name: String = "", role: UserRole = UserRole.PATIENT): Resource<User> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Resource.Error("Phone sign-in failed.")
            val existing = getUserProfile(firebaseUser.uid)
            if (existing != null) {
                Resource.Success(existing)
            } else {
                val newUser = User(
                    id = firebaseUser.uid,
                    name = if (name.isNotBlank()) name else (firebaseUser.phoneNumber ?: "Phone User"),
                    email = "",
                    phone = firebaseUser.phoneNumber ?: "",
                    role = role
                )
                saveUserProfile(newUser)
                Resource.Success(newUser)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Phone sign in error", e)
        }
    }

    suspend fun signInWithPhoneCode(verificationId: String, code: String, name: String = "", role: UserRole = UserRole.PATIENT): Resource<User> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneCredential(credential, name, role)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Invalid verification code", e)
        }
    }

    suspend fun getUserProfile(uid: String): User? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val roleStr = doc.getString("role") ?: "PATIENT"
                User(
                    id = uid,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.PATIENT },
                    hospitalId = doc.getString("hospitalId"),
                    selectedHospitalId = doc.getString("selectedHospitalId"),
                    fcmToken = doc.getString("fcmToken") ?: "",
                    profileImageUrl = doc.getString("profileImageUrl") ?: "",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveUserProfile(user: User) {
        try {
            val data = hashMapOf<String, Any>(
                "id" to user.id,
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "role" to user.role.name,
                "profileImageUrl" to user.profileImageUrl,
                "createdAt" to user.createdAt
            )
            user.hospitalId?.let { data["hospitalId"] = it }
            user.selectedHospitalId?.let { data["selectedHospitalId"] = it }
            if (user.fcmToken.isNotBlank()) data["fcmToken"] = user.fcmToken

            firestore.collection("users").document(user.id).set(data, SetOptions.merge()).await()
        } catch (_: Exception) {
        }
    }

    suspend fun updateUserSelectedHospital(userId: String, hospitalId: String) {
        firestore.collection("users").document(userId).update("selectedHospitalId", hospitalId).await()
    }

    fun signOut() {
        auth.signOut()
    }

    // ==========================================
    // --- Hospitals Collection ---
    // ==========================================

    fun observeHospitals(): Flow<List<Hospital>> = callbackFlow {
        val reg = firestore.collection("hospitals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc -> mapHospital(doc.id, doc.data) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun observeHospitalById(hospitalId: String): Flow<Hospital?> = callbackFlow {
        val reg = firestore.collection("hospitals").document(hospitalId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(mapHospital(snapshot.id, snapshot.data))
            }
        awaitClose { reg.remove() }
    }

    suspend fun getHospitalById(hospitalId: String): Hospital? {
        return try {
            val doc = firestore.collection("hospitals").document(hospitalId).get().await()
            if (doc.exists()) mapHospital(doc.id, doc.data) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getHospitalByAdminId(adminId: String): Hospital? {
        return try {
            val snapshot = firestore.collection("hospitals")
                .whereEqualTo("adminId", adminId)
                .limit(1)
                .get()
                .await()
            val doc = snapshot.documents.firstOrNull()
            if (doc != null && doc.exists()) mapHospital(doc.id, doc.data) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveHospital(hospital: Hospital) {
        val data = hashMapOf(
            "name" to hospital.name,
            "opdName" to hospital.opdName,
            "address" to hospital.address,
            "city" to hospital.city,
            "phone" to hospital.phone,
            "adminId" to hospital.adminId,
            "departmentIds" to hospital.departmentIds,
            "createdAt" to hospital.createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
        firestore.collection("hospitals").document(hospital.id).set(data, SetOptions.merge()).await()
    }

    private fun mapHospital(id: String, data: Map<String, Any>?): Hospital? {
        if (data == null) return null
        @Suppress("UNCHECKED_CAST")
        val deptIds = data["departmentIds"] as? List<String> ?: emptyList()
        return Hospital(
            id = id,
            name = data["name"]?.toString() ?: "",
            opdName = data["opdName"]?.toString() ?: "",
            address = data["address"]?.toString() ?: "",
            city = data["city"]?.toString() ?: "",
            phone = data["phone"]?.toString() ?: "",
            adminId = data["adminId"]?.toString() ?: "",
            departmentIds = deptIds,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    // ==========================================
    // --- Departments Collection ---
    // ==========================================

    fun observeDepartments(hospitalId: String? = null): Flow<List<Department>> = callbackFlow {
        val query = if (!hospitalId.isNullOrBlank()) {
            firestore.collection("departments").whereEqualTo("hospitalId", hospitalId)
        } else {
            firestore.collection("departments")
        }

        val reg = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                Department(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    hospitalId = doc.getString("hospitalId") ?: "",
                    doctorCount = (doc.getLong("doctorCount") ?: 0).toInt(),
                    iconName = doc.getString("iconName") ?: "stethoscope",
                    isActive = doc.getBoolean("isActive") ?: true,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    suspend fun addDepartment(department: Department) {
        val data = hashMapOf(
            "name" to department.name,
            "description" to department.description,
            "hospitalId" to department.hospitalId,
            "doctorCount" to department.doctorCount,
            "iconName" to department.iconName,
            "isActive" to department.isActive,
            "createdAt" to department.createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
        firestore.collection("departments").document(department.id).set(data).await()
    }

    // ==========================================
    // --- Doctors Collection ---
    // ==========================================

    fun observeDoctors(
        hospitalId: String? = null,
        departmentId: String? = null,
        queryText: String? = null
    ): Flow<List<Doctor>> = callbackFlow {
        var baseQuery: com.google.firebase.firestore.Query = firestore.collection("doctors")

        if (!hospitalId.isNullOrBlank()) {
            baseQuery = baseQuery.whereEqualTo("hospitalId", hospitalId)
        }
        if (!departmentId.isNullOrBlank()) {
            baseQuery = baseQuery.whereEqualTo("departmentId", departmentId)
        }

        val reg = baseQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            var list = snapshot?.documents?.mapNotNull { doc -> mapDoctor(doc.id, doc.data) } ?: emptyList()
            if (!queryText.isNullOrBlank()) {
                val q = queryText.lowercase()
                list = list.filter {
                    it.name.lowercase().contains(q) ||
                    it.specialization.lowercase().contains(q) ||
                    it.departmentName.lowercase().contains(q)
                }
            }
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    suspend fun getDoctorById(doctorId: String): Doctor? {
        return try {
            val doc = firestore.collection("doctors").document(doctorId).get().await()
            if (doc.exists()) mapDoctor(doc.id, doc.data) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getDoctorByUserId(userId: String): Doctor? {
        return try {
            val snapshot = firestore.collection("doctors")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()
            val doc = snapshot.documents.firstOrNull()
            if (doc != null && doc.exists()) mapDoctor(doc.id, doc.data) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun addDoctor(doctor: Doctor) {
        val data = hashMapOf(
            "userId" to doctor.userId,
            "hospitalId" to doctor.hospitalId,
            "name" to doctor.name,
            "specialization" to doctor.specialization,
            "departmentId" to doctor.departmentId,
            "departmentName" to doctor.departmentName,
            "qualification" to doctor.qualification,
            "experienceYears" to doctor.experienceYears,
            "consultationFee" to doctor.consultationFee,
            "avgConsultationMinutes" to doctor.avgConsultationMinutes,
            "availableDays" to doctor.availableDays,
            "shiftStartTime" to doctor.shiftStartTime,
            "shiftEndTime" to doctor.shiftEndTime,
            "maxTokensPerDay" to doctor.maxTokensPerDay,
            "isAvailable" to doctor.isAvailable,
            "isInOPD" to doctor.isInOPD,
            "isOnBreak" to doctor.isOnBreak,
            "breakStartedAt" to doctor.breakStartedAt,
            "rating" to doctor.rating,
            "totalReviews" to doctor.totalReviews,
            "photoUrl" to doctor.photoUrl,
            "loginEmail" to doctor.loginEmail,
            "passwordChanged" to doctor.passwordChanged,
            "fcmToken" to doctor.fcmToken,
            "createdAt" to doctor.createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
        firestore.collection("doctors").document(doctor.id).set(data).await()
    }

    suspend fun updateDoctorAvailability(doctorId: String, isAvailable: Boolean) {
        firestore.collection("doctors").document(doctorId).update(
            mapOf("isAvailable" to isAvailable, "updatedAt" to System.currentTimeMillis())
        ).await()
    }

    suspend fun updateDoctorOpdStatus(doctorId: String, isInOPD: Boolean) {
        firestore.collection("doctors").document(doctorId).update(
            mapOf(
                "isInOPD" to isInOPD,
                "isAvailable" to isInOPD,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun updateDoctorBreakStatus(doctorId: String, isOnBreak: Boolean) {
        val updateMap = mutableMapOf<String, Any>(
            "isOnBreak" to isOnBreak,
            "updatedAt" to System.currentTimeMillis()
        )
        if (isOnBreak) {
            updateMap["breakStartedAt"] = System.currentTimeMillis()
        }
        firestore.collection("doctors").document(doctorId).update(updateMap).await()
    }

    private fun mapDoctor(id: String, data: Map<String, Any>?): Doctor? {
        if (data == null) return null
        @Suppress("UNCHECKED_CAST")
        val days = data["availableDays"] as? List<String> ?: listOf("Mon", "Tue", "Wed", "Thu", "Fri")
        return Doctor(
            id = id,
            userId = data["userId"]?.toString() ?: id,
            hospitalId = data["hospitalId"]?.toString() ?: "",
            name = data["name"]?.toString() ?: "",
            specialization = data["specialization"]?.toString() ?: "",
            departmentId = data["departmentId"]?.toString() ?: "",
            departmentName = data["departmentName"]?.toString() ?: "",
            qualification = data["qualification"]?.toString() ?: "",
            experienceYears = (data["experienceYears"] as? Number)?.toInt() ?: 5,
            consultationFee = (data["consultationFee"] as? Number)?.toDouble() ?: 500.0,
            avgConsultationMinutes = (data["avgConsultationMinutes"] as? Number)?.toInt() ?: 15,
            availableDays = days,
            shiftStartTime = data["shiftStartTime"]?.toString() ?: "09:00 AM",
            shiftEndTime = data["shiftEndTime"]?.toString() ?: "02:00 PM",
            maxTokensPerDay = (data["maxTokensPerDay"] as? Number)?.toInt() ?: 25,
            isAvailable = data["isAvailable"] as? Boolean ?: true,
            isInOPD = data["isInOPD"] as? Boolean ?: false,
            isOnBreak = data["isOnBreak"] as? Boolean ?: false,
            breakStartedAt = (data["breakStartedAt"] as? Number)?.toLong(),
            rating = (data["rating"] as? Number)?.toDouble() ?: 4.8,
            totalReviews = (data["totalReviews"] as? Number)?.toInt() ?: 120,
            photoUrl = data["photoUrl"]?.toString() ?: "",
            loginEmail = data["loginEmail"]?.toString() ?: "",
            passwordChanged = data["passwordChanged"] as? Boolean ?: false,
            fcmToken = data["fcmToken"]?.toString() ?: "",
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    // ==========================================
    // --- Appointments Collection ---
    // ==========================================

    fun observeAppointmentsForPatient(patientId: String): Flow<List<Appointment>> = callbackFlow {
        val reg = firestore.collection("appointments")
            .whereEqualTo("patientId", patientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc -> mapAppointment(doc.id, doc.data) } ?: emptyList()
                trySend(list.sortedByDescending { it.bookedAt })
            }
        awaitClose { reg.remove() }
    }

    fun observeAppointmentsForDoctor(doctorId: String, date: String): Flow<List<Appointment>> = callbackFlow {
        val query = if (date.isNotBlank()) {
            firestore.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", date)
        } else {
            firestore.collection("appointments").whereEqualTo("doctorId", doctorId)
        }

        val reg = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc -> mapAppointment(doc.id, doc.data) } ?: emptyList()
            trySend(list.sortedBy { it.tokenNumber })
        }
        awaitClose { reg.remove() }
    }

    fun observeAppointmentsForHospital(hospitalId: String, date: String? = null): Flow<List<Appointment>> = callbackFlow {
        var baseQuery = firestore.collection("appointments").whereEqualTo("hospitalId", hospitalId)
        if (!date.isNullOrBlank()) {
            baseQuery = baseQuery.whereEqualTo("date", date)
        }

        val reg = baseQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc -> mapAppointment(doc.id, doc.data) } ?: emptyList()
            trySend(list.sortedBy { it.tokenNumber })
        }
        awaitClose { reg.remove() }
    }

    fun observeAllAppointments(): Flow<List<Appointment>> = callbackFlow {
        val reg = firestore.collection("appointments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc -> mapAppointment(doc.id, doc.data) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    // Atomic Booking via Firestore Transaction
    suspend fun bookAppointmentAtomic(
        patientId: String,
        patientName: String,
        patientPhone: String,
        doctor: Doctor,
        hospitalId: String,
        date: String,
        timeSlot: String,
        symptoms: String = "",
        estimatedTime: String = ""
    ): Appointment {
        val queueDocId = "${doctor.id}_$date"
        val queueRef = firestore.collection("daily_queues").document(queueDocId)
        val appointmentId = "apt_${UUID.randomUUID().toString().take(8)}"
        val appointmentRef = firestore.collection("appointments").document(appointmentId)

        return firestore.runTransaction { transaction ->
            val queueSnap = transaction.get(queueRef)
            val currentIssued = if (queueSnap.exists()) {
                (queueSnap.getLong("totalTokensIssued") ?: 0).toInt()
            } else 0

            val currentToken = if (queueSnap.exists()) {
                (queueSnap.getLong("currentToken") ?: 0).toInt()
            } else 0

            val nextToken = currentIssued + 1

            // Upsert queue doc
            val queueData = hashMapOf<String, Any>(
                "queueId" to queueDocId,
                "doctorId" to doctor.id,
                "doctorName" to doctor.name,
                "hospitalId" to hospitalId,
                "date" to date,
                "currentToken" to currentToken,
                "totalTokensIssued" to nextToken,
                "status" to "ACTIVE",
                "updatedAt" to System.currentTimeMillis()
            )
            transaction.set(queueRef, queueData, SetOptions.merge())

            // Create appointment doc
            val aptData = hashMapOf<String, Any>(
                "id" to appointmentId,
                "patientId" to patientId,
                "patientName" to patientName,
                "patientPhone" to patientPhone,
                "doctorId" to doctor.id,
                "doctorName" to doctor.name,
                "hospitalId" to hospitalId,
                "departmentId" to doctor.departmentId,
                "departmentName" to doctor.departmentName,
                "date" to date,
                "timeSlot" to timeSlot,
                "estimatedTime" to estimatedTime,
                "tokenNumber" to nextToken,
                "status" to AppointmentStatus.BOOKED.name,
                "symptoms" to symptoms,
                "doctorNotes" to "",
                "bookedAt" to System.currentTimeMillis(),
                "reminder1hrSent" to false,
                "reminder30minSent" to false,
                "updatedAt" to System.currentTimeMillis()
            )
            transaction.set(appointmentRef, aptData)

            Appointment(
                id = appointmentId,
                patientId = patientId,
                patientName = patientName,
                patientPhone = patientPhone,
                doctorId = doctor.id,
                doctorName = doctor.name,
                hospitalId = hospitalId,
                departmentId = doctor.departmentId,
                departmentName = doctor.departmentName,
                date = date,
                timeSlot = timeSlot,
                estimatedTime = estimatedTime,
                tokenNumber = nextToken,
                status = AppointmentStatus.BOOKED,
                symptoms = symptoms
            )
        }.await()
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus, doctorNotes: String = "") {
        val map = mutableMapOf<String, Any>(
            "status" to status.name,
            "updatedAt" to System.currentTimeMillis()
        )
        if (doctorNotes.isNotBlank()) {
            map["doctorNotes"] = doctorNotes
        }
        if (status == AppointmentStatus.CALLED) {
            map["calledAt"] = System.currentTimeMillis()
        } else if (status == AppointmentStatus.COMPLETED) {
            map["completedAt"] = System.currentTimeMillis()
        }
        firestore.collection("appointments").document(appointmentId).update(map).await()
    }

    suspend fun markReminderSent(appointmentId: String, is1Hr: Boolean) {
        val field = if (is1Hr) "reminder1hrSent" else "reminder30minSent"
        firestore.collection("appointments").document(appointmentId).update(
            mapOf(field to true, "updatedAt" to System.currentTimeMillis())
        ).await()
    }

    private fun mapAppointment(id: String, data: Map<String, Any>?): Appointment? {
        if (data == null) return null
        val statusStr = data["status"]?.toString() ?: "BOOKED"
        return Appointment(
            id = id,
            patientId = data["patientId"]?.toString() ?: "",
            patientName = data["patientName"]?.toString() ?: "",
            patientPhone = data["patientPhone"]?.toString() ?: "",
            doctorId = data["doctorId"]?.toString() ?: "",
            doctorName = data["doctorName"]?.toString() ?: "",
            hospitalId = data["hospitalId"]?.toString() ?: "",
            departmentId = data["departmentId"]?.toString() ?: "",
            departmentName = data["departmentName"]?.toString() ?: "",
            date = data["date"]?.toString() ?: "",
            timeSlot = data["timeSlot"]?.toString() ?: "",
            estimatedTime = data["estimatedTime"]?.toString() ?: "",
            tokenNumber = (data["tokenNumber"] as? Number)?.toInt() ?: 1,
            status = try { AppointmentStatus.valueOf(statusStr) } catch (_: Exception) { AppointmentStatus.BOOKED },
            symptoms = data["symptoms"]?.toString() ?: "",
            doctorNotes = data["doctorNotes"]?.toString() ?: "",
            calledAt = (data["calledAt"] as? Number)?.toLong(),
            completedAt = (data["completedAt"] as? Number)?.toLong(),
            bookedAt = (data["bookedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            reminder1hrSent = data["reminder1hrSent"] as? Boolean ?: false,
            reminder30minSent = data["reminder30minSent"] as? Boolean ?: false,
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    // ==========================================
    // --- Daily Queues Collection ---
    // ==========================================

    fun observeLiveQueue(doctorId: String, date: String): Flow<DailyQueue?> = callbackFlow {
        val key = "${doctorId}_$date"
        val reg = firestore.collection("daily_queues").document(key)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(mapDailyQueue(snapshot.id, snapshot.data))
            }
        awaitClose { reg.remove() }
    }

    fun observeAllActiveQueues(hospitalId: String? = null): Flow<List<DailyQueue>> = callbackFlow {
        val query = if (!hospitalId.isNullOrBlank()) {
            firestore.collection("daily_queues").whereEqualTo("hospitalId", hospitalId)
        } else {
            firestore.collection("daily_queues")
        }

        val reg = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc -> mapDailyQueue(doc.id, doc.data) } ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    suspend fun getOrCreateQueue(
        doctorId: String,
        doctorName: String,
        hospitalId: String,
        date: String
    ): DailyQueue {
        val queueDocId = "${doctorId}_$date"
        val docRef = firestore.collection("daily_queues").document(queueDocId)
        val snap = docRef.get().await()
        if (snap.exists()) {
            return mapDailyQueue(snap.id, snap.data)!!
        }

        val newQueue = DailyQueue(
            queueId = queueDocId,
            doctorId = doctorId,
            doctorName = doctorName,
            hospitalId = hospitalId,
            date = date,
            status = QueueStatus.ACTIVE,
            queueStartedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val data = hashMapOf(
            "queueId" to newQueue.queueId,
            "doctorId" to newQueue.doctorId,
            "doctorName" to newQueue.doctorName,
            "hospitalId" to newQueue.hospitalId,
            "date" to newQueue.date,
            "currentToken" to newQueue.currentToken,
            "currentPatientId" to "",
            "totalTokensIssued" to newQueue.totalTokensIssued,
            "totalCompleted" to 0,
            "totalNoShow" to 0,
            "totalCancelled" to 0,
            "status" to newQueue.status.name,
            "queueStartedAt" to newQueue.queueStartedAt,
            "updatedAt" to newQueue.updatedAt
        )
        docRef.set(data).await()
        return newQueue
    }

    suspend fun advanceQueueAtomic(doctorId: String, date: String, nextPatientId: String? = null): DailyQueue {
        val queueDocId = "${doctorId}_$date"
        val queueRef = firestore.collection("daily_queues").document(queueDocId)

        return firestore.runTransaction { transaction ->
            val snap = transaction.get(queueRef)
            val currentToken = if (snap.exists()) (snap.getLong("currentToken") ?: 0).toInt() else 0
            val totalIssued = if (snap.exists()) (snap.getLong("totalTokensIssued") ?: 0).toInt() else 0
            val docName = snap.getString("doctorName") ?: "Doctor"
            val hospId = snap.getString("hospitalId") ?: ""

            val nextServing = currentToken + 1
            val updateMap = hashMapOf<String, Any>(
                "currentToken" to nextServing,
                "status" to QueueStatus.ACTIVE.name,
                "lastAdvancedAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            nextPatientId?.let { updateMap["currentPatientId"] = it }

            transaction.set(queueRef, updateMap, SetOptions.merge())

            DailyQueue(
                queueId = queueDocId,
                doctorId = doctorId,
                doctorName = docName,
                hospitalId = hospId,
                date = date,
                currentToken = nextServing,
                currentPatientId = nextPatientId,
                totalTokensIssued = totalIssued,
                status = QueueStatus.ACTIVE,
                lastAdvancedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }.await()
    }

    suspend fun callPatientAtomic(doctorId: String, date: String, tokenNumber: Int, patientId: String): DailyQueue {
        val queueDocId = "${doctorId}_$date"
        val queueRef = firestore.collection("daily_queues").document(queueDocId)

        return firestore.runTransaction { transaction ->
            val snap = transaction.get(queueRef)
            val totalIssued = if (snap.exists()) (snap.getLong("totalTokensIssued") ?: 0).toInt() else 0
            val docName = snap.getString("doctorName") ?: "Doctor"
            val hospId = snap.getString("hospitalId") ?: ""

            val updateMap = hashMapOf<String, Any>(
                "currentToken" to tokenNumber,
                "currentPatientId" to patientId,
                "status" to QueueStatus.ACTIVE.name,
                "lastAdvancedAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            transaction.set(queueRef, updateMap, SetOptions.merge())

            DailyQueue(
                queueId = queueDocId,
                doctorId = doctorId,
                doctorName = docName,
                hospitalId = hospId,
                date = date,
                currentToken = tokenNumber,
                currentPatientId = patientId,
                totalTokensIssued = totalIssued,
                status = QueueStatus.ACTIVE,
                lastAdvancedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }.await()
    }

    suspend fun updateQueueStatus(doctorId: String, date: String, status: QueueStatus, breakMessage: String? = null) {
        val queueDocId = "${doctorId}_$date"
        val map = mutableMapOf<String, Any>(
            "status" to status.name,
            "updatedAt" to System.currentTimeMillis()
        )
        if (breakMessage != null) {
            map["breakMessage"] = breakMessage
        }
        firestore.collection("daily_queues").document(queueDocId).set(map, SetOptions.merge()).await()
    }

    suspend fun markConsultationComplete(doctorId: String, date: String, appointmentId: String, doctorNotes: String = "") {
        updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED, doctorNotes)
        val queueDocId = "${doctorId}_$date"
        val queueRef = firestore.collection("daily_queues").document(queueDocId)
        firestore.runTransaction { transaction ->
            val snap = transaction.get(queueRef)
            if (snap.exists()) {
                val completed = (snap.getLong("totalCompleted") ?: 0).toInt() + 1
                transaction.update(queueRef, "totalCompleted", completed, "updatedAt", System.currentTimeMillis())
            }
        }.await()
    }

    suspend fun markNoShow(doctorId: String, date: String, appointmentId: String) {
        updateAppointmentStatus(appointmentId, AppointmentStatus.NO_SHOW)
        val queueDocId = "${doctorId}_$date"
        val queueRef = firestore.collection("daily_queues").document(queueDocId)
        firestore.runTransaction { transaction ->
            val snap = transaction.get(queueRef)
            if (snap.exists()) {
                val noShow = (snap.getLong("totalNoShow") ?: 0).toInt() + 1
                transaction.update(queueRef, "totalNoShow", noShow, "updatedAt", System.currentTimeMillis())
            }
        }.await()
    }

    suspend fun resetQueue(doctorId: String, date: String): DailyQueue {
        val queueDocId = "${doctorId}_$date"
        val docRef = firestore.collection("daily_queues").document(queueDocId)
        val resetData = hashMapOf<String, Any>(
            "currentToken" to 0,
            "currentPatientId" to "",
            "status" to QueueStatus.NOT_STARTED.name,
            "updatedAt" to System.currentTimeMillis()
        )
        docRef.set(resetData, SetOptions.merge()).await()
        val snap = docRef.get().await()
        return mapDailyQueue(snap.id, snap.data)!!
    }

    private fun mapDailyQueue(id: String, data: Map<String, Any>?): DailyQueue? {
        if (data == null) return null
        val statusStr = data["status"]?.toString() ?: "ACTIVE"
        return DailyQueue(
            queueId = id,
            doctorId = data["doctorId"]?.toString() ?: "",
            doctorName = data["doctorName"]?.toString() ?: "",
            hospitalId = data["hospitalId"]?.toString() ?: "",
            date = data["date"]?.toString() ?: "",
            currentToken = (data["currentToken"] as? Number)?.toInt() ?: 0,
            currentPatientId = data["currentPatientId"]?.toString(),
            totalTokensIssued = (data["totalTokensIssued"] as? Number)?.toInt() ?: 0,
            totalCompleted = (data["totalCompleted"] as? Number)?.toInt() ?: 0,
            totalNoShow = (data["totalNoShow"] as? Number)?.toInt() ?: 0,
            totalCancelled = (data["totalCancelled"] as? Number)?.toInt() ?: 0,
            status = try { QueueStatus.valueOf(statusStr) } catch (_: Exception) { QueueStatus.ACTIVE },
            breakMessage = data["breakMessage"]?.toString(),
            queueStartedAt = (data["queueStartedAt"] as? Number)?.toLong(),
            lastAdvancedAt = (data["lastAdvancedAt"] as? Number)?.toLong(),
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    // ==========================================
    // --- Notifications Collection ---
    // ==========================================

    fun observeNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val reg = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc -> mapNotification(doc.id, doc.data) } ?: emptyList()
                trySend(list.sortedByDescending { it.createdAt })
            }
        awaitClose { reg.remove() }
    }

    suspend fun sendNotification(notification: Notification) {
        val data = hashMapOf<String, Any>(
            "userId" to notification.userId,
            "title" to notification.title,
            "body" to notification.body,
            "type" to notification.type.name,
            "isRead" to notification.isRead,
            "createdAt" to notification.createdAt
        )
        notification.appointmentId?.let { data["appointmentId"] = it }
        notification.doctorId?.let { data["doctorId"] = it }
        notification.hospitalId?.let { data["hospitalId"] = it }

        firestore.collection("notifications").document(notification.id).set(data).await()
    }

    suspend fun markNotificationAsRead(notificationId: String) {
        firestore.collection("notifications").document(notificationId).update("isRead", true).await()
    }

    suspend fun markAllNotificationsAsRead(userId: String) {
        val snap = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .await()
        for (doc in snap.documents) {
            doc.reference.update("isRead", true).await()
        }
    }

    private fun mapNotification(id: String, data: Map<String, Any>?): Notification? {
        if (data == null) return null
        val typeStr = data["type"]?.toString() ?: NotificationType.QUEUE_POSITION_UPDATE.name
        return Notification(
            id = id,
            userId = data["userId"]?.toString() ?: "",
            title = data["title"]?.toString() ?: "",
            body = data["body"]?.toString() ?: "",
            type = try { NotificationType.valueOf(typeStr) } catch (_: Exception) { NotificationType.QUEUE_POSITION_UPDATE },
            appointmentId = data["appointmentId"]?.toString(),
            doctorId = data["doctorId"]?.toString(),
            hospitalId = data["hospitalId"]?.toString(),
            isRead = data["isRead"] as? Boolean ?: false,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
