package com.example.careflow

import android.app.Application
import com.example.careflow.core.di.ServiceLocator
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CareFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        try {
            val firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            firestore.firestoreSettings = settings
        } catch (_: Exception) {}

        ServiceLocator.init(this)
        com.example.careflow.core.notification.CallNotificationService.createNotificationChannel(this)
        com.example.careflow.core.notification.GlobalCallAlertListener.start(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                ServiceLocator.firebaseDataSource.seedInitialCloudDataIfEmpty()
            } catch (_: Exception) {}
        }
    }
}
