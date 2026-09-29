package com.example.anonymouschat.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anonymouschat.data.model.User
import com.example.anonymouschat.data.repository.AuthRepository
import com.example.anonymouschat.data.repository.ChatRepository
import com.example.anonymouschat.ui.components.AvatarCircle
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onChatStarted: (chatId: String, chatName: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find People") },
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
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by username...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No users found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(searchResults, key = { it.uid }) { user ->
                        UserResultItem(
                            user = user,
                            onClick = {
                                viewModel.startChat(user.uid, user.displayName) { chatId ->
                                    onChatStarted(chatId, user.displayName)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserResultItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarCircle(displayName = user.displayName, size = 48.dp)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = user.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val chatRepository: ChatRepository = ChatRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            @Suppress("OPT_IN_USAGE")
            _searchQuery
                .debounce(300L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _searchResults.value = emptyList()
                        _isLoading.value = false
                    } else {
                        searchUsers(query)
                    }
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private suspend fun searchUsers(query: String) {
        _isLoading.value = true
        try {
            val currentUid = authRepository.getCurrentUid()
            val results = chatRepository.searchUsers(query)
            _searchResults.value = results.filter { it.uid != currentUid }
        } catch (e: Exception) {
            _searchResults.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun startChat(otherUid: String, otherName: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUid = authRepository.getCurrentUid()
                if (currentUid != null) {
                    val client = io.getstream.chat.android.client.ChatClient.instance()
                    val channel = client.channel("messaging", "")
                    
                    // The create API requires passing the members
                    val result = channel.create(
                        memberIds = listOf(currentUid, otherUid),
                        extraData = emptyMap()
                    ).await()
                    
                    if (result.isSuccess) {
                        onComplete(result.getOrNull()?.cid?.replace("messaging:", "") ?: "")
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
