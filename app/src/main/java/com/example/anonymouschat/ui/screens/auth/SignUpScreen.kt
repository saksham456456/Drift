package com.example.anonymouschat.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anonymouschat.data.repository.AuthRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val database = FirebaseDatabase.getInstance()

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _usernameAvailability = MutableStateFlow<UsernameAvailability>(UsernameAvailability.Idle)
    val usernameAvailability: StateFlow<UsernameAvailability> = _usernameAvailability.asStateFlow()

    private var checkUsernameJob: Job? = null

    fun checkUsername(username: String) {
        if (!username.matches("^[a-zA-Z0-9_]{3,15}$".toRegex())) {
            _usernameAvailability.value = UsernameAvailability.Idle
            return
        }
        
        checkUsernameJob?.cancel()
        _usernameAvailability.value = UsernameAvailability.Checking
        
        checkUsernameJob = viewModelScope.launch {
            delay(500) // debounce
            try {
                val snapshot = database.reference.child("usernames").child(username).get().await()
                if (snapshot.exists()) {
                    _usernameAvailability.value = UsernameAvailability.Unavailable
                } else {
                    _usernameAvailability.value = UsernameAvailability.Available
                }
            } catch (e: Exception) {
                _usernameAvailability.value = UsernameAvailability.Idle
            }
        }
    }

    fun signUp(username: String, password: String, displayName: String) {
        if (username.isBlank() || password.isBlank() || displayName.isBlank()) {
            _uiState.value = SignUpUiState.Error("All fields are required")
            return
        }

        if (_usernameAvailability.value != UsernameAvailability.Available) {
            _uiState.value = SignUpUiState.Error("Username is not available")
            return
        }

        _uiState.value = SignUpUiState.Loading
        viewModelScope.launch {
            val result = authRepository.signUp(username, password, displayName)
            if (result.isSuccess) {
                _uiState.value = SignUpUiState.Success
            } else {
                _uiState.value = SignUpUiState.Error(result.exceptionOrNull()?.message ?: "Sign up failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = SignUpUiState.Idle
    }
}

sealed class SignUpUiState {
    object Idle : SignUpUiState()
    object Loading : SignUpUiState()
    object Success : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}

sealed class UsernameAvailability {
    object Idle : UsernameAvailability()
    object Checking : UsernameAvailability()
    object Available : UsernameAvailability()
    object Unavailable : UsernameAvailability()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SignUpViewModel = viewModel()
) {
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val isUsernameValid by remember(username) { derivedStateOf { username.matches("^[a-zA-Z0-9_]{3,15}$".toRegex()) } }
    val isPasswordValid by remember(password) { derivedStateOf { password.length >= 6 } }
    val isDisplayNameValid by remember(displayName) { derivedStateOf { displayName.isNotBlank() } }
    
    val uiState by viewModel.uiState.collectAsState()
    val usernameAvailability by viewModel.usernameAvailability.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is SignUpUiState.Success -> {
                onSignUpSuccess()
                viewModel.resetState()
            }
            is SignUpUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as SignUpUiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Join Drift",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = displayName.isNotEmpty() && !isDisplayNameValid,
                        supportingText = {
                            if (displayName.isNotEmpty() && !isDisplayNameValid) {
                                Text("Display name cannot be blank")
                            }
                        }
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { 
                            val lowerUser = it.lowercase()
                            username = lowerUser
                            viewModel.checkUsername(lowerUser)
                        },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = (username.isNotEmpty() && !isUsernameValid) || usernameAvailability == UsernameAvailability.Unavailable,
                        supportingText = {
                            if (username.isNotEmpty() && !isUsernameValid) {
                                Text("Username must be 3-15 characters, alphanumeric/underscores")
                            } else if (usernameAvailability == UsernameAvailability.Unavailable) {
                                Text("Username is not available")
                            }
                        },
                        trailingIcon = {
                            when (usernameAvailability) {
                                is UsernameAvailability.Checking -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                                is UsernameAvailability.Available -> {
                                    Icon(Icons.Default.Check, contentDescription = "Available", tint = Color.Green)
                                }
                                is UsernameAvailability.Unavailable -> {
                                    Icon(Icons.Default.Clear, contentDescription = "Unavailable", tint = Color.Red)
                                }
                                else -> {}
                            }
                        }
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = password.isNotEmpty() && !isPasswordValid,
                        supportingText = {
                            if (password.isNotEmpty() && !isPasswordValid) {
                                Text("Password must be at least 6 characters")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.signUp(username, password, displayName) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is SignUpUiState.Loading && 
                                  isUsernameValid && 
                                  isPasswordValid && 
                                  isDisplayNameValid && 
                                  usernameAvailability == UsernameAvailability.Available,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState is SignUpUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create Account")
                        }
                    }
                }
            }
        }
    }
}
