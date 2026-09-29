package com.example.anonymouschat.ui.screens.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class CreateGroupViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private val authRepository = AuthRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _selectedUsers = MutableStateFlow<List<User>>(emptyList())
    val selectedUsers: StateFlow<List<User>> = _selectedUsers.asStateFlow()
    
    private val _groupName = MutableStateFlow("")
    val groupName = _groupName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            _searchQuery.debounce(300).collectLatest { query ->
                if (query.length >= 2) {
                    val currentUid = authRepository.getCurrentUid()
                    val results = chatRepository.searchUsers(query).filter { it.uid != currentUid }
                    _searchResults.value = results
                } else {
                    _searchResults.value = emptyList()
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateGroupName(name: String) {
        _groupName.value = name
    }

    fun toggleUserSelection(user: User) {
        val currentList = _selectedUsers.value.toMutableList()
        if (currentList.any { it.uid == user.uid }) {
            currentList.removeAll { it.uid == user.uid }
        } else {
            currentList.add(user)
        }
        _selectedUsers.value = currentList
    }

    fun createGroup(onSuccess: (String) -> Unit) {
        val name = _groupName.value.trim()
        val members = _selectedUsers.value
        val currentUid = authRepository.getCurrentUid() ?: return
        
        if (name.isEmpty() || members.isEmpty()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val memberUids = members.map { it.uid }.toMutableList()
                memberUids.add(currentUid)
                
                val client = io.getstream.chat.android.client.ChatClient.instance()
                val channel = client.channel("messaging", "")
                
                val result = channel.create(
                    memberIds = memberUids,
                    extraData = mapOf("name" to name)
                ).await()
                
                if (result.isSuccess) {
                    onSuccess(result.getOrNull()?.cid?.replace("messaging:", "") ?: "")
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit,
    onGroupCreated: (chatId: String, chatName: String) -> Unit,
    viewModel: CreateGroupViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedUsers by viewModel.selectedUsers.collectAsState()
    val groupName by viewModel.groupName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val canCreate = groupName.isNotBlank() && selectedUsers.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Group") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { 
                            viewModel.createGroup { chatId ->
                                onGroupCreated(chatId, groupName)
                            }
                        },
                        enabled = canCreate && !isLoading
                    ) {
                        Text("Create")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Group Name Input
            OutlinedTextField(
                value = groupName,
                onValueChange = { viewModel.updateGroupName(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Group Subject") },
                singleLine = true
            )
            
            // Selected Users
            if (selectedUsers.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(selectedUsers, key = { it.uid }) { user ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { viewModel.toggleUserSelection(user) }
                        ) {
                            Box {
                                AvatarCircle(displayName = user.displayName, size = 48.dp)
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.displayName.split(" ").firstOrNull() ?: "",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search users...") },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = "Search")
                },
                singleLine = true
            )
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults, key = { it.uid }) { user ->
                        val isSelected = selectedUsers.any { it.uid == user.uid }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleUserSelection(user) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarCircle(displayName = user.displayName, size = 40.dp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "@${user.username}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
