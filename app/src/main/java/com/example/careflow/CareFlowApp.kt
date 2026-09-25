package com.example.careflow

import android.app.Application
import com.google.firebase.FirebaseApp

class CareFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
