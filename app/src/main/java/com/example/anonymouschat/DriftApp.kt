package com.example.anonymouschat

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory

class DriftApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Firebase offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        // Stream Offline Plugin
        val offlinePluginFactory = StreamOfflinePluginFactory(appContext = this)

        // Initialize Stream Chat Client
        // Note: Replace with actual Stream API Key in production
        ChatClient.Builder("dummy_api_key", this)
            .logLevel(ChatLogLevel.ALL)
            .build()
    }
}
