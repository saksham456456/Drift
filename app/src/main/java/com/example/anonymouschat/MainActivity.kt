package com.example.anonymouschat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.anonymouschat.theme.DriftTheme

import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.anonymouschat.data.repository.SettingsRepository

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        enableEdgeToEdge()
        setContent {
            val settingsRepository = remember { SettingsRepository(applicationContext) }
            val isDarkMode by settingsRepository.isDarkMode.collectAsState()

            // Automatically connect to Stream if user is logged in to Firebase
            androidx.compose.runtime.LaunchedEffect(Unit) {
                val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val client = io.getstream.chat.android.client.ChatClient.instance()
                    val streamUser = io.getstream.chat.android.models.User(
                        id = user.uid,
                        name = user.displayName ?: "User"
                    )
                    // Using devToken for local testing without the Vercel bouncer
                    client.connectUser(streamUser, client.devToken(user.uid)).enqueue()
                }
            }

            DriftTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DriftNavigation()
                }
            }
        }
    }
}
