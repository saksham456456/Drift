package com.example.anonymouschat

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class DriftApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable offline capabilities for Firebase Realtime Database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
