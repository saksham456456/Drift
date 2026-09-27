package com.example.anonymouschat.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anonymouschat.data.model.User
import com.example.anonymouschat.data.repository.AuthRepository
import com.example.anonymouschat.ui.components.AvatarCircle
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import com.example.anonymouschat.data.repository.SettingsRepository
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.OpenInNew

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var showDisplayNameDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val settingsRepository = remember { SettingsRepository(context) }
    val isDarkMode by settingsRepository.isDarkMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            AvatarCircle(
                displayName = currentUser?.displayName ?: "User",
                size = 100.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = currentUser?.displayName ?: "User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "@${currentUser?.username ?: "username"}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            
            SettingRow(
                title = "Display Name",
                subtitle = currentUser?.displayName,
                icon = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
                onClick = { showDisplayNameDialog = true }
            )
            
            Divider()

            SettingRow(
                title = "Dark Mode",
                subtitle = "Toggle dark theme",
                icon = { 
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { settingsRepository.setDarkMode(it) }
                    ) 
                },
                onClick = { settingsRepository.setDarkMode(!isDarkMode) }
            )

            Divider()
            
            SettingRow(
                title = "About Drift",
                onClick = { showAboutDialog = true },
                icon = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Open") }
            )
            
            Divider()

            SettingRow(
                title = "Terms of Service",
                onClick = { uriHandler.openUri("https://drift.app/terms") },
                icon = { Icon(Icons.Outlined.OpenInNew, contentDescription = "Link") }
            )

            Divider()

            SettingRow(
                title = "Privacy Policy",
                onClick = { uriHandler.openUri("https://drift.app/privacy") },
                icon = { Icon(Icons.Outlined.OpenInNew, contentDescription = "Link") }
            )

            Divider()
            
            SettingRow(
                title = "Log Out",
                titleColor = MaterialTheme.colorScheme.error,
                onClick = {
                    viewModel.logout()
                    onLogout()
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "v2.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }

    if (showDisplayNameDialog) {
        var newName by remember { mutableStateOf(currentUser?.displayName ?: "") }
        AlertDialog(
            onDismissRequest = { showDisplayNameDialog = false },
            title = { Text("Change Display Name") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.updateDisplayName(newName)
                        }
                        showDisplayNameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisplayNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Drift") },
            text = { Text("Drift — distraction-free messaging. No stories. No feeds. Just chat.") },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SettingRow(
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color.Unspecified,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (icon != null) {
            icon()
        }
    }
}

class SettingsViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUid()
            if (uid != null) {
                try {
                    val snapshot = FirebaseDatabase.getInstance().getReference("users").child(uid).get().await()
                    val user = snapshot.getValue(User::class.java)
                    _currentUser.value = user
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUid()
            if (uid != null) {
                try {
                    FirebaseDatabase.getInstance().getReference("users")
                        .child(uid)
                        .child("displayName")
                        .setValue(newName)
                        .await()
                    
                    _currentUser.value = _currentUser.value?.copy(displayName = newName)
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
