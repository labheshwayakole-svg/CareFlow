package com.example.careflow.data.datasource

import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.model.Hospital
import com.example.careflow.domain.model.QueueStatus
import com.example.careflow.domain.model.User
import com.example.careflow.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class InMemoryDataStore {
    private val mutex = Mutex()

    // Preloaded with realistic CareFlow hospital, Dr. Sarfaraz, departments, and active patient queue
    private val _hospitals = MutableStateFlow<List<Hospital>>(CareFlowSeedData.hospitals)
    val hospitals: StateFlow<List<Hospital>> = _hospitals.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(CareFlowSeedData.allUsers)
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _departments = MutableStateFlow<List<Department>>(CareFlowSeedData.departments)
    val departments: StateFlow<List<Department>> = _departments.asStateFlow()

    private val _doctors = MutableStateFlow<List<Doctor>>(CareFlowSeedData.doctors)
    val doctors: StateFlow<List<Doctor>> = _doctors.asStateFlow()

    private val _queues = MutableStateFlow<Map<String, DailyQueue>>(CareFlowSeedData.getSeedQueuesForToday())
    val queues: StateFlow<Map<String, DailyQueue>> = _queues.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(CareFlowSeedData.getSeedAppointmentsForToday())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    suspend fun addHospital(hospital: Hospital) {
        mutex.withLock {
            val list = _hospitals.value.toMutableList()
            list.removeAll { it.id == hospital.id }
            list.add(hospital)
            _hospitals.value = list
        }
    }

    // Thread-safe operations
    suspend fun setCurrentUser(user: User?) {
        mutex.withLock {
            _currentUser.value = user
        }
    }

    suspend fun registerUser(user: User) {
        mutex.withLock {
            val list = _users.value.toMutableList()
            // Scope replacement by role so same email can have distinct Patient, Doctor, and Admin profiles
            list.removeAll { it.id == user.id || (it.email.isNotBlank() && it.email.equals(user.email, ignoreCase = true) && it.role == user.role) }
            list.add(user)
            _users.value = list
            _currentUser.value = user
        }
    }

    suspend fun updateAdminProfile(
        adminName: String,
        adminEmail: String,
        adminPhone: String,
        hospitalId: String
    ) {
        mutex.withLock {
            val userList = _users.value.toMutableList()
            // Invalidate ALL previous admin users so the old email is completely stripped of admin privileges
            userList.removeAll { it.role == UserRole.ADMIN || it.id == "admin_default" || it.id == "adiPY1P5MiepIpTLJM7ZqvBgDM53" }

            val updatedAdmin = User(
                id = "admin_${hospitalId}",
                name = adminName,
                email = adminEmail,
                phone = adminPhone,
                role = UserRole.ADMIN,
                hospitalId = hospitalId,
                selectedHospitalId = hospitalId
            )
            userList.add(updatedAdmin)
            _users.value = userList
            _currentUser.value = updatedAdmin

            val hospList = _hospitals.value.toMutableList()
            val targetHosp = hospList.find { it.id == hospitalId } ?: hospList.find { it.id == "hosp_main" }
            if (targetHosp != null) {
                hospList.remove(targetHosp)
                hospList.add(targetHosp.copy(
                    email = adminEmail,
                    adminEmail = adminEmail,
                    adminName = adminName,
                    adminPhone = adminPhone
                ))
                _hospitals.value = hospList
            }
        }
    }

    fun findUserByEmailAndRole(email: String, role: UserRole): User? {
        val clean = email.trim().lowercase()
        return _users.value.find { it.email.trim().lowercase() == clean && it.role == role }
    }

    suspend fun addDoctor(doctor: Doctor) {
        mutex.withLock {
            val list = _doctors.value.toMutableList()
            list.removeAll { it.id == doctor.id }
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

    suspend fun deleteDoctor(doctorId: String) {
        mutex.withLock {
            val list = _doctors.value.toMutableList()
            val removed = list.find { it.id == doctorId }
            list.removeAll { it.id == doctorId }
            _doctors.value = list

            if (removed != null) {
                val deptList = _departments.value.map { dept ->
                    if (dept.id == removed.departmentId) {
                        dept.copy(doctorCount = maxOf(0, dept.doctorCount - 1))
                    } else dept
                }
                _departments.value = deptList

                val userList = _users.value.toMutableList()
                userList.removeAll { it.id == removed.userId }
                _users.value = userList
            }
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
            list.removeAll { it.id == department.id }
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

    suspend fun callPatientAtomic(doctorId: String, date: String, tokenNumber: Int, patientId: String): DailyQueue = mutex.withLock {
        val queueKey = "${doctorId}_$date"
        val existingQueue = _queues.value[queueKey] ?: DailyQueue(
            queueId = queueKey,
            doctorId = doctorId,
            doctorName = _doctors.value.find { it.id == doctorId }?.name ?: "Doctor",
            date = date,
            currentToken = 0,
            totalTokensIssued = tokenNumber,
            status = QueueStatus.ACTIVE
        )

        val updatedQueue = existingQueue.copy(
            currentToken = tokenNumber,
            currentPatientId = patientId,
            status = QueueStatus.ACTIVE,
            lastAdvancedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newQueuesMap = _queues.value.toMutableMap()
        newQueuesMap[queueKey] = updatedQueue
        _queues.value = newQueuesMap

        _appointments.value = _appointments.value.map { apt ->
            if (apt.doctorId == doctorId && apt.date == date) {
                when {
                    apt.tokenNumber == tokenNumber -> apt.copy(status = AppointmentStatus.CALLED)
                    apt.status == AppointmentStatus.CALLED -> apt.copy(status = AppointmentStatus.WAITING)
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

    suspend fun syncAppointments(list: List<Appointment>) {
        mutex.withLock {
            val currentMap = _appointments.value.associateBy { it.id }.toMutableMap()
            list.forEach { currentMap[it.id] = it }
            _appointments.value = currentMap.values.sortedByDescending { it.bookedAt }
        }
    }
}
