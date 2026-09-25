package com.example.careflow.data.firebase

import android.app.Activity
import com.example.careflow.core.result.Resource
import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
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

    // --- Authentication ---

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
            // If user record is not found in Firebase Auth, automatically create it for seamless sign-in
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
                    // Fall through to original error if password was wrong on existing user
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
                // Instant auto-retrieval / verification on device
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

    suspend fun signInWithPhoneCredential(
        credential: PhoneAuthCredential,
        name: String,
        role: UserRole
    ): Resource<User> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Resource.Error("Phone sign in failed.")
            val existing = getUserProfile(firebaseUser.uid)
            if (existing != null) {
                Resource.Success(existing)
            } else {
                val newUser = User(
                    id = firebaseUser.uid,
                    name = if (name.isNotBlank()) name else "Verified Patient",
                    email = "${firebaseUser.phoneNumber?.filter { it.isDigit() } ?: "user"}@careflow.phone",
                    phone = firebaseUser.phoneNumber ?: "",
                    role = role
                )
                saveUserProfile(newUser)
                Resource.Success(newUser)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Phone auto-verification failed", e)
        }
    }

    suspend fun signInWithPhoneCode(
        verificationId: String,
        smsCode: String,
        name: String,
        role: UserRole
    ): Resource<User> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Resource.Error("Phone authentication failed.")
            val existing = getUserProfile(firebaseUser.uid)
            if (existing != null) {
                Resource.Success(existing)
            } else {
                val newUser = User(
                    id = firebaseUser.uid,
                    name = if (name.isNotBlank()) name else "Phone Patient",
                    email = "${firebaseUser.phoneNumber?.filter { it.isDigit() } ?: "user"}@careflow.phone",
                    phone = firebaseUser.phoneNumber ?: "",
                    role = role
                )
                saveUserProfile(newUser)
                Resource.Success(newUser)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Verification failed", e)
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
                    role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.PATIENT }
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveUserProfile(user: User) {
        val data = hashMapOf(
            "id" to user.id,
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone,
            "role" to user.role.name,
            "createdAt" to user.createdAt
        )
        firestore.collection("users").document(user.id).set(data, SetOptions.merge()).await()
    }

    fun signOut() {
        auth.signOut()
    }

    // --- Firestore Data Collections & Snapshots ---

    fun observeDepartments(): Flow<List<Department>> = callbackFlow {
        val reg: ListenerRegistration = firestore.collection("departments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    Department(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        doctorCount = (doc.getLong("doctorCount") ?: 0).toInt(),
                        iconName = doc.getString("iconName") ?: "stethoscope"
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun observeDoctors(): Flow<List<Doctor>> = callbackFlow {
        val reg = firestore.collection("doctors")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    Doctor(
                        id = doc.id,
                        userId = doc.getString("userId") ?: doc.id,
                        name = doc.getString("name") ?: "",
                        specialization = doc.getString("specialization") ?: "",
                        departmentId = doc.getString("departmentId") ?: "",
                        departmentName = doc.getString("departmentName") ?: "",
                        qualification = doc.getString("qualification") ?: "",
                        experienceYears = (doc.getLong("experienceYears") ?: 5).toInt(),
                        consultationFee = doc.getDouble("consultationFee") ?: 500.0,
                        avgConsultationMinutes = (doc.getLong("avgConsultationMinutes") ?: 15).toInt(),
                        shiftStartTime = doc.getString("shiftStartTime") ?: "09:00 AM",
                        shiftEndTime = doc.getString("shiftEndTime") ?: "02:00 PM",
                        maxTokensPerDay = (doc.getLong("maxTokensPerDay") ?: 30).toInt(),
                        isAvailable = doc.getBoolean("isAvailable") ?: true,
                        rating = doc.getDouble("rating") ?: 4.8,
                        totalReviews = (doc.getLong("totalReviews") ?: 100).toInt()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun observeAppointmentsForPatient(patientId: String): Flow<List<Appointment>> = callbackFlow {
        val reg = firestore.collection("appointments")
            .whereEqualTo("patientId", patientId)
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

    fun observeLiveQueue(doctorId: String, date: String): Flow<DailyQueue?> = callbackFlow {
        val key = "${doctorId}_$date"
        val reg = firestore.collection("daily_queues").document(key)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val data = snapshot.data ?: emptyMap<String, Any>()
                val statusStr = data["status"]?.toString() ?: "ACTIVE"
                val q = DailyQueue(
                    queueId = key,
                    doctorId = doctorId,
                    doctorName = data["doctorName"]?.toString() ?: "",
                    date = date,
                    currentToken = (data["currentToken"] as? Number)?.toInt() ?: 0,
                    totalTokensIssued = (data["totalTokensIssued"] as? Number)?.toInt() ?: 0,
                    status = try { QueueStatus.valueOf(statusStr) } catch (_: Exception) { QueueStatus.ACTIVE },
                    updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
                trySend(q)
            }
        awaitClose { reg.remove() }
    }

    // Atomic Booking via Firestore Transaction
    suspend fun bookAppointmentAtomic(
        patientId: String,
        patientName: String,
        patientPhone: String,
        doctor: Doctor,
        date: String,
        timeSlot: String,
        notes: String
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

            // Update or create queue doc
            val queueData = hashMapOf(
                "queueId" to queueDocId,
                "doctorId" to doctor.id,
                "doctorName" to doctor.name,
                "date" to date,
                "currentToken" to currentToken,
                "totalTokensIssued" to nextToken,
                "status" to "ACTIVE",
                "updatedAt" to System.currentTimeMillis()
            )
            transaction.set(queueRef, queueData, SetOptions.merge())

            // Create appointment doc
            val aptData = hashMapOf(
                "id" to appointmentId,
                "patientId" to patientId,
                "patientName" to patientName,
                "patientPhone" to patientPhone,
                "doctorId" to doctor.id,
                "doctorName" to doctor.name,
                "departmentName" to doctor.departmentName,
                "date" to date,
                "timeSlot" to timeSlot,
                "tokenNumber" to nextToken,
                "status" to "BOOKED",
                "bookedAt" to System.currentTimeMillis(),
                "notes" to notes,
                "doctorNotes" to ""
            )
            transaction.set(appointmentRef, aptData)

            Appointment(
                id = appointmentId,
                patientId = patientId,
                patientName = patientName,
                patientPhone = patientPhone,
                doctorId = doctor.id,
                doctorName = doctor.name,
                departmentName = doctor.departmentName,
                date = date,
                timeSlot = timeSlot,
                tokenNumber = nextToken,
                status = AppointmentStatus.BOOKED,
                notes = notes
            )
        }.await()
    }

    // Advance Queue Atomically via Firestore Transaction
    suspend fun advanceQueueAtomic(doctorId: String, date: String): DailyQueue {
        val queueDocId = "${doctorId}_$date"
        val queueRef = firestore.collection("daily_queues").document(queueDocId)

        return firestore.runTransaction { transaction ->
            val snap = transaction.get(queueRef)
            val currentToken = if (snap.exists()) (snap.getLong("currentToken") ?: 0).toInt() else 0
            val totalIssued = if (snap.exists()) (snap.getLong("totalTokensIssued") ?: 0).toInt() else 0
            val docName = snap.getString("doctorName") ?: "Doctor"

            val nextServing = currentToken + 1
            transaction.update(queueRef, mapOf(
                "currentToken" to nextServing,
                "updatedAt" to System.currentTimeMillis(),
                "status" to "ACTIVE"
            ))

            DailyQueue(
                queueId = queueDocId,
                doctorId = doctorId,
                doctorName = docName,
                date = date,
                currentToken = nextServing,
                totalTokensIssued = totalIssued,
                status = QueueStatus.ACTIVE
            )
        }.await()
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus, doctorNotes: String = "") {
        val map = mutableMapOf<String, Any>("status" to status.name)
        if (doctorNotes.isNotBlank()) {
            map["doctorNotes"] = doctorNotes
        }
        firestore.collection("appointments").document(appointmentId).update(map).await()
    }

    suspend fun updateDoctorAvailability(doctorId: String, isAvailable: Boolean) {
        firestore.collection("doctors").document(doctorId).update("isAvailable", isAvailable).await()
    }

    suspend fun addDoctor(doctor: Doctor) {
        val data = hashMapOf(
            "userId" to doctor.userId,
            "name" to doctor.name,
            "specialization" to doctor.specialization,
            "departmentId" to doctor.departmentId,
            "departmentName" to doctor.departmentName,
            "qualification" to doctor.qualification,
            "experienceYears" to doctor.experienceYears,
            "consultationFee" to doctor.consultationFee,
            "avgConsultationMinutes" to doctor.avgConsultationMinutes,
            "shiftStartTime" to doctor.shiftStartTime,
            "shiftEndTime" to doctor.shiftEndTime,
            "maxTokensPerDay" to doctor.maxTokensPerDay,
            "isAvailable" to doctor.isAvailable,
            "rating" to doctor.rating,
            "totalReviews" to doctor.totalReviews
        )
        firestore.collection("doctors").document(doctor.id).set(data).await()
    }

    suspend fun addDepartment(department: Department) {
        val data = hashMapOf(
            "name" to department.name,
            "description" to department.description,
            "doctorCount" to department.doctorCount,
            "iconName" to department.iconName
        )
        firestore.collection("departments").document(department.id).set(data).await()
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
            departmentName = data["departmentName"]?.toString() ?: "",
            date = data["date"]?.toString() ?: "",
            timeSlot = data["timeSlot"]?.toString() ?: "",
            tokenNumber = (data["tokenNumber"] as? Number)?.toInt() ?: 1,
            status = try { AppointmentStatus.valueOf(statusStr) } catch (_: Exception) { AppointmentStatus.BOOKED },
            bookedAt = (data["bookedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            notes = data["notes"]?.toString() ?: "",
            doctorNotes = data["doctorNotes"]?.toString() ?: ""
        )
    }
}
