package com.example.careflow.data.datasource

import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.model.QueueStatus
import com.example.careflow.domain.model.User
import com.example.careflow.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class InMemoryDataStore {
    private val mutex = Mutex()

    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Demo Users
    val demoPatient = User(
        id = "user_patient_01",
        name = "Sarfraj Ansari",
        email = "patient@careflow.com",
        phone = "9876543210",
        role = UserRole.PATIENT
    )

    val demoDoctor = User(
        id = "user_doc_cardio_01",
        name = "Dr. Rajesh Sharma",
        email = "doctor@careflow.com",
        phone = "9823456789",
        role = UserRole.DOCTOR
    )

    val demoAdmin = User(
        id = "user_admin_01",
        name = "Hospital Administrator",
        email = "admin@careflow.com",
        phone = "9811122233",
        role = UserRole.ADMIN
    )

    private val _users = MutableStateFlow(
        listOf(demoPatient, demoDoctor, demoAdmin)
    )
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(demoPatient)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Departments
    private val initialDepartments = listOf(
        Department("dept_cardio", "Cardiology", "Heart & Cardiovascular Care", doctorCount = 2, iconName = "cardiology"),
        Department("dept_ortho", "Orthopedics", "Bone, Joint & Spine Health", doctorCount = 2, iconName = "orthopedics"),
        Department("dept_pedia", "Pediatrics", "Infant, Child & Adolescent Medicine", doctorCount = 1, iconName = "pediatrics"),
        Department("dept_gen", "General Medicine", "Primary Care & Chronic Disease Management", doctorCount = 2, iconName = "medicine"),
        Department("dept_derma", "Dermatology", "Skin, Hair & Cosmetic Treatment", doctorCount = 1, iconName = "dermatology"),
        Department("dept_neuro", "Neurology", "Brain, Nerve & Stroke Specialists", doctorCount = 1, iconName = "neurology")
    )
    private val _departments = MutableStateFlow(initialDepartments)
    val departments: StateFlow<List<Department>> = _departments.asStateFlow()

    // Doctors
    private val initialDoctors = listOf(
        Doctor(
            id = "doc_cardio_01",
            userId = "user_doc_cardio_01",
            name = "Dr. Rajesh Sharma",
            specialization = "Senior Interventional Cardiologist",
            departmentId = "dept_cardio",
            departmentName = "Cardiology",
            qualification = "MBBS, MD, DM (Cardiology)",
            experienceYears = 16,
            consultationFee = 600.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "09:00 AM",
            shiftEndTime = "02:00 PM",
            maxTokensPerDay = 30,
            isAvailable = true,
            rating = 4.9,
            totalReviews = 210
        ),
        Doctor(
            id = "doc_cardio_02",
            userId = "user_doc_cardio_02",
            name = "Dr. Sunita Patel",
            specialization = "Cardiac Electrophysiologist",
            departmentId = "dept_cardio",
            departmentName = "Cardiology",
            qualification = "MBBS, MD (Medicine), DNB (Cardiology)",
            experienceYears = 11,
            consultationFee = 550.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Wed", "Fri"),
            shiftStartTime = "10:00 AM",
            shiftEndTime = "03:00 PM",
            maxTokensPerDay = 20,
            isAvailable = true,
            rating = 4.7,
            totalReviews = 145
        ),
        Doctor(
            id = "doc_ortho_01",
            userId = "user_doc_ortho_01",
            name = "Dr. Amit Varma",
            specialization = "Joint Replacement & Spine Surgeon",
            departmentId = "dept_ortho",
            departmentName = "Orthopedics",
            qualification = "MBBS, MS (Ortho), M.Ch (UK)",
            experienceYears = 14,
            consultationFee = 700.0,
            avgConsultationMinutes = 20,
            availableDays = listOf("Tue", "Thu", "Sat"),
            shiftStartTime = "09:30 AM",
            shiftEndTime = "01:30 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            rating = 4.8,
            totalReviews = 180
        ),
        Doctor(
            id = "doc_pedia_01",
            userId = "user_doc_pedia_01",
            name = "Dr. Neha Kulkarni",
            specialization = "Consultant Pediatrician",
            departmentId = "dept_pedia",
            departmentName = "Pediatrics",
            qualification = "MBBS, DCH, DNB (Pediatrics)",
            experienceYears = 9,
            consultationFee = 500.0,
            avgConsultationMinutes = 12,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            shiftStartTime = "09:00 AM",
            shiftEndTime = "01:00 PM",
            maxTokensPerDay = 35,
            isAvailable = true,
            rating = 4.9,
            totalReviews = 310
        ),
        Doctor(
            id = "doc_gen_01",
            userId = "user_doc_gen_01",
            name = "Dr. Vikram Joshi",
            specialization = "General Physician & Diabetologist",
            departmentId = "dept_gen",
            departmentName = "General Medicine",
            qualification = "MBBS, MD (Gen Med)",
            experienceYears = 18,
            consultationFee = 400.0,
            avgConsultationMinutes = 10,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "08:30 AM",
            shiftEndTime = "01:30 PM",
            maxTokensPerDay = 40,
            isAvailable = true,
            rating = 4.8,
            totalReviews = 450
        )
    )
    private val _doctors = MutableStateFlow(initialDoctors)
    val doctors: StateFlow<List<Doctor>> = _doctors.asStateFlow()

    // Queues map: key = "{doctorId}_{date}"
    private val _queues = MutableStateFlow<Map<String, DailyQueue>>(
        mapOf(
            "doc_cardio_01_$todayDateString" to DailyQueue(
                queueId = "doc_cardio_01_$todayDateString",
                doctorId = "doc_cardio_01",
                doctorName = "Dr. Rajesh Sharma",
                date = todayDateString,
                currentToken = 4,
                totalTokensIssued = 8,
                status = QueueStatus.ACTIVE
            )
        )
    )
    val queues: StateFlow<Map<String, DailyQueue>> = _queues.asStateFlow()

    // Appointments
    private val initialAppointments = listOf(
        Appointment(
            id = "apt_demo_01",
            patientId = "user_patient_01",
            patientName = "Sarfraj Ansari",
            patientPhone = "9876543210",
            doctorId = "doc_cardio_01",
            doctorName = "Dr. Rajesh Sharma",
            departmentName = "Cardiology",
            date = todayDateString,
            timeSlot = "10:30 AM",
            tokenNumber = 7,
            status = AppointmentStatus.BOOKED,
            notes = "Routine blood pressure check & follow-up"
        ),
        Appointment(
            id = "apt_demo_02",
            patientId = "user_patient_02",
            patientName = "Pooja Verma",
            patientPhone = "9876500001",
            doctorId = "doc_cardio_01",
            doctorName = "Dr. Rajesh Sharma",
            departmentName = "Cardiology",
            date = todayDateString,
            timeSlot = "09:45 AM",
            tokenNumber = 4,
            status = AppointmentStatus.IN_CONSULTATION,
            notes = "Chest discomfort"
        ),
        Appointment(
            id = "apt_demo_03",
            patientId = "user_patient_03",
            patientName = "Kunal Singh",
            patientPhone = "9876500002",
            doctorId = "doc_cardio_01",
            doctorName = "Dr. Rajesh Sharma",
            departmentName = "Cardiology",
            date = todayDateString,
            timeSlot = "10:00 AM",
            tokenNumber = 5,
            status = AppointmentStatus.BOOKED,
            notes = "ECG review"
        )
    )
    private val _appointments = MutableStateFlow(initialAppointments)
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    // Thread-safe operations
    suspend fun setCurrentUser(user: User?) {
        mutex.withLock {
            _currentUser.value = user
        }
    }

    suspend fun registerUser(user: User) {
        mutex.withLock {
            val list = _users.value.toMutableList()
            list.add(user)
            _users.value = list
            _currentUser.value = user
        }
    }

    suspend fun addDoctor(doctor: Doctor) {
        mutex.withLock {
            val list = _doctors.value.toMutableList()
            list.add(doctor)
            _doctors.value = list

            // Update department doctor count
            val deptList = _departments.value.map { dept ->
                if (dept.id == doctor.departmentId) {
                    dept.copy(doctorCount = dept.doctorCount + 1)
                } else dept
            }
            _departments.value = deptList
        }
    }

    suspend fun updateDoctorAvailability(doctorId: String, isAvailable: Boolean) {
        mutex.withLock {
            _doctors.value = _doctors.value.map { doc ->
                if (doc.id == doctorId) doc.copy(isAvailable = isAvailable) else doc
            }
        }
    }

    suspend fun addDepartment(department: Department) {
        mutex.withLock {
            val list = _departments.value.toMutableList()
            list.add(department)
            _departments.value = list
        }
    }

    // Atomic Booking & Token Generation (mirrors Firestore transaction)
    suspend fun bookAppointmentAtomic(
        patientId: String,
        patientName: String,
        patientPhone: String,
        doctor: Doctor,
        date: String,
        timeSlot: String,
        notes: String
    ): Appointment = mutex.withLock {
        val queueKey = "${doctor.id}_$date"
        val currentQueue = _queues.value[queueKey] ?: DailyQueue(
            queueId = queueKey,
            doctorId = doctor.id,
            doctorName = doctor.name,
            date = date,
            currentToken = 0,
            totalTokensIssued = 0,
            status = QueueStatus.ACTIVE
        )

        val nextToken = currentQueue.totalTokensIssued + 1
        val updatedQueue = currentQueue.copy(
            totalTokensIssued = nextToken,
            status = if (currentQueue.status == QueueStatus.NOT_STARTED) QueueStatus.ACTIVE else currentQueue.status,
            updatedAt = System.currentTimeMillis()
        )

        val newQueuesMap = _queues.value.toMutableMap()
        newQueuesMap[queueKey] = updatedQueue
        _queues.value = newQueuesMap

        val newAppointment = Appointment(
            id = "apt_${UUID.randomUUID().toString().take(8)}",
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
            bookedAt = System.currentTimeMillis(),
            notes = notes
        )

        val aptList = _appointments.value.toMutableList()
        aptList.add(0, newAppointment)
        _appointments.value = aptList

        newAppointment
    }

    // Atomic Queue Advancement (Doctor calling next patient)
    suspend fun advanceQueueAtomic(doctorId: String, date: String): DailyQueue = mutex.withLock {
        val queueKey = "${doctorId}_$date"
        val existingQueue = _queues.value[queueKey] ?: DailyQueue(
            queueId = queueKey,
            doctorId = doctorId,
            doctorName = _doctors.value.find { it.id == doctorId }?.name ?: "Doctor",
            date = date,
            currentToken = 0,
            totalTokensIssued = 0,
            status = QueueStatus.ACTIVE
        )

        val nextToken = existingQueue.currentToken + 1
        val updatedQueue = existingQueue.copy(
            currentToken = nextToken,
            status = QueueStatus.ACTIVE,
            updatedAt = System.currentTimeMillis()
        )

        val newQueuesMap = _queues.value.toMutableMap()
        newQueuesMap[queueKey] = updatedQueue
        _queues.value = newQueuesMap

        // Update corresponding appointment status
        _appointments.value = _appointments.value.map { apt ->
            if (apt.doctorId == doctorId && apt.date == date) {
                when {
                    apt.tokenNumber == nextToken -> apt.copy(status = AppointmentStatus.IN_CONSULTATION)
                    apt.tokenNumber < nextToken && apt.status == AppointmentStatus.IN_CONSULTATION -> apt.copy(status = AppointmentStatus.COMPLETED)
                    else -> apt
                }
            } else apt
        }

        updatedQueue
    }

    suspend fun updateQueueStatus(doctorId: String, date: String, status: QueueStatus) {
        mutex.withLock {
            val queueKey = "${doctorId}_$date"
            val existing = _queues.value[queueKey] ?: return
            val updated = existing.copy(status = status, updatedAt = System.currentTimeMillis())
            val map = _queues.value.toMutableMap()
            map[queueKey] = updated
            _queues.value = map
        }
    }

    suspend fun resetQueue(doctorId: String, date: String): DailyQueue = mutex.withLock {
        val queueKey = "${doctorId}_$date"
        val existing = _queues.value[queueKey] ?: return DailyQueue(
            queueId = queueKey,
            doctorId = doctorId,
            doctorName = "",
            date = date
        )
        val reset = existing.copy(currentToken = 0, status = QueueStatus.ACTIVE, updatedAt = System.currentTimeMillis())
        val map = _queues.value.toMutableMap()
        map[queueKey] = reset
        _queues.value = map
        reset
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus, doctorNotes: String = "") {
        mutex.withLock {
            _appointments.value = _appointments.value.map { apt ->
                if (apt.id == appointmentId) {
                    apt.copy(
                        status = status,
                        doctorNotes = if (doctorNotes.isNotBlank()) doctorNotes else apt.doctorNotes
                    )
                } else apt
            }
        }
    }

    suspend fun cancelAppointment(appointmentId: String) {
        mutex.withLock {
            _appointments.value = _appointments.value.map { apt ->
                if (apt.id == appointmentId) {
                    apt.copy(status = AppointmentStatus.CANCELLED)
                } else apt
            }
        }
    }
}
