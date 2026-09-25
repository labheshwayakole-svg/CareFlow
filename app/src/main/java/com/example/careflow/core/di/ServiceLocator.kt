package com.example.careflow.core.di

import com.example.careflow.core.dispatcher.DefaultDispatcherProvider
import com.example.careflow.core.dispatcher.DispatcherProvider
import com.example.careflow.data.datasource.InMemoryDataStore
import com.example.careflow.data.firebase.FirebaseDataSource
import com.example.careflow.data.repository.AppointmentRepositoryImpl
import com.example.careflow.data.repository.AuthRepositoryImpl
import com.example.careflow.data.repository.DoctorRepositoryImpl
import com.example.careflow.data.repository.HospitalRepositoryImpl
import com.example.careflow.data.repository.NotificationRepositoryImpl
import com.example.careflow.data.repository.QueueRepositoryImpl
import com.example.careflow.domain.repository.AppointmentRepository
import com.example.careflow.domain.repository.AuthRepository
import com.example.careflow.domain.repository.DoctorRepository
import com.example.careflow.domain.repository.HospitalRepository
import com.example.careflow.domain.repository.NotificationRepository
import com.example.careflow.domain.repository.QueueRepository
import com.example.careflow.domain.usecase.AdvanceQueueUseCase
import com.example.careflow.domain.usecase.BookAppointmentUseCase
import com.example.careflow.domain.usecase.CalculateWaitTimeUseCase
import com.example.careflow.domain.usecase.GetDoctorsUseCase
import com.example.careflow.domain.usecase.ObserveLiveQueueUseCase

object ServiceLocator {
    val dispatcherProvider: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
    }

    val inMemoryDataStore: InMemoryDataStore by lazy {
        InMemoryDataStore()
    }

    val firebaseDataSource: FirebaseDataSource by lazy {
        FirebaseDataSource()
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            dataStore = inMemoryDataStore,
            firebaseDataSource = firebaseDataSource,
            dispatchers = dispatcherProvider
        )
    }

    val hospitalRepository: HospitalRepository by lazy {
        HospitalRepositoryImpl(
            firebaseDataSource = firebaseDataSource,
            dispatchers = dispatcherProvider
        )
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl(
            firebaseDataSource = firebaseDataSource,
            dispatchers = dispatcherProvider
        )
    }

    val doctorRepository: DoctorRepository by lazy {
        DoctorRepositoryImpl(
            dataStore = inMemoryDataStore,
            firebaseDataSource = firebaseDataSource,
            dispatchers = dispatcherProvider
        )
    }

    val appointmentRepository: AppointmentRepository by lazy {
        AppointmentRepositoryImpl(
            dataStore = inMemoryDataStore,
            firebaseDataSource = firebaseDataSource,
            dispatchers = dispatcherProvider
        )
    }

    val queueRepository: QueueRepository by lazy {
        QueueRepositoryImpl(
            dataStore = inMemoryDataStore,
            firebaseDataSource = firebaseDataSource,
            dispatchers = dispatcherProvider
        )
    }

    // Use Cases
    val calculateWaitTimeUseCase: CalculateWaitTimeUseCase by lazy {
        CalculateWaitTimeUseCase()
    }

    val getDoctorsUseCase: GetDoctorsUseCase by lazy {
        GetDoctorsUseCase(doctorRepository)
    }

    val bookAppointmentUseCase: BookAppointmentUseCase by lazy {
        BookAppointmentUseCase(appointmentRepository)
    }

    val observeLiveQueueUseCase: ObserveLiveQueueUseCase by lazy {
        ObserveLiveQueueUseCase(queueRepository, calculateWaitTimeUseCase)
    }

    val advanceQueueUseCase: AdvanceQueueUseCase by lazy {
        AdvanceQueueUseCase(queueRepository, appointmentRepository)
    }
}
