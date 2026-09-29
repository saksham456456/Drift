package com.example.anonymouschat.data.repository

import com.example.anonymouschat.data.model.Chat
import com.example.anonymouschat.data.model.Message
import com.example.anonymouschat.data.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatRepository {
    private val database = FirebaseDatabase.getInstance()

    fun getChats(uid: String): Flow<List<Chat>> = callbackFlow {
        val query = database.getReference("chats").orderByChild("participants/$uid").startAt(0.0)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chats = mutableListOf<Chat>()
                for (child in snapshot.children) {
                    val chat = child.getValue(Chat::class.java)
                    if (chat != null) {
                        chats.add(chat.copy(id = child.key ?: ""))
                    }
                }
                chats.sortByDescending { it.lastMessageTime }
                trySend(chats)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val query = database.getReference("messages/$chatId").orderByChild("timestamp").limitToLast(100)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)
                    if (message != null) {
                        messages.add(message.copy(id = child.key ?: ""))
                    }
                }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun sendMessage(chatId: String, message: Message) = withContext(Dispatchers.IO) {
        val messagesRef = database.getReference("messages/$chatId").push()
        val messageId = messagesRef.key ?: return@withContext
        val finalMessage = message.copy(id = messageId, timestamp = System.currentTimeMillis())
        
        messagesRef.setValue(finalMessage).await()

        val chatRef = database.getReference("chats/$chatId")
        val chatSnapshot = chatRef.get().await()
        val chat = chatSnapshot.getValue(Chat::class.java)

        val chatUpdates = mutableMapOf<String, Any>(
            "lastMessage" to finalMessage.text,
            "lastMessageTime" to finalMessage.timestamp
        )

        chat?.participants?.forEach { (uid, count) ->
            if (uid != message.senderUid) {
                val baseCount = if (count < 0L) 0L else count
                chatUpdates["participants/$uid"] = baseCount + 1L
            } else if (count < 0L) {
                chatUpdates["participants/$uid"] = 0L // unhide for sender if they were hidden
            }
        }

        chatRef.updateChildren(chatUpdates).await()
    }

    suspend fun markChatRead(chatId: String, uid: String) = withContext(Dispatchers.IO) {
        val chatRef = database.getReference("chats/$chatId/participants/$uid")
        chatRef.setValue(0L).await()
    }

    suspend fun createDirectChat(currentUid: String, otherUid: String): String = withContext(Dispatchers.IO) {
        val chatsRef = database.getReference("chats")
        
        // This is a naive check. A robust check would involve querying where participants contain both.
        // For simplicity, we query where participants contain currentUid and check locally for otherUid.
        // Using -1.0 to include hidden/deleted chats.
        val snapshot = chatsRef.orderByChild("participants/$currentUid").startAt(-1.0).get().await()
        
        for (child in snapshot.children) {
            val chat = child.getValue(Chat::class.java)
            if (chat != null && chat.type == "direct" && chat.participants.containsKey(otherUid)) {
                if (chat.participants[currentUid] == -1L) {
                    chatsRef.child("${child.key}/participants/$currentUid").setValue(0L).await()
                }
                if (chat.participants[otherUid] == -1L) {
                    chatsRef.child("${child.key}/participants/$otherUid").setValue(0L).await()
                }
                return@withContext child.key ?: ""
            }
        }

        val newChatRef = chatsRef.push()
        val chatId = newChatRef.key ?: throw Exception("Failed to generate chat id")
        
        val chat = Chat(
            id = chatId,
            type = "direct",
            participants = mapOf(currentUid to 0L, otherUid to 0L),
            createdBy = currentUid
        )
        
        newChatRef.setValue(chat).await()
        chatId
    }

    suspend fun createGroupChat(name: String, memberUids: List<String>, createdByUid: String): String = withContext(Dispatchers.IO) {
        val chatsRef = database.getReference("chats").push()
        val chatId = chatsRef.key ?: throw Exception("Failed to generate chat id")
        
        val participants = memberUids.associateWith { 0L }.toMutableMap()
        participants[createdByUid] = 0L

        val chat = Chat(
            id = chatId,
            type = "group",
            name = name,
            participants = participants,
            createdBy = createdByUid
        )
        
        chatsRef.setValue(chat).await()
        chatId
    }

    suspend fun searchUsers(query: String): List<User> = withContext(Dispatchers.IO) {
        val users = mutableListOf<User>()
        // To do a prefix search on usernames in Firebase realtime database
        val snapshot = database.getReference("users")
            .orderByChild("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .await()
            
        for (child in snapshot.children) {
            val user = child.getValue(User::class.java)
            if (user != null) {
                users.add(user)
            }
        }
        users
    }

    fun observeUserPresence(uid: String) {
        val connectedRef = database.getReference(".info/connected")
        val lastSeenRef = database.getReference("users/$uid/lastSeen")

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    lastSeenRef.setValue(-1L)
                    lastSeenRef.onDisconnect().setValue(ServerValue.TIMESTAMP)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun getChat(chatId: String): Flow<Chat?> = callbackFlow {
        val query = database.getReference("chats/$chatId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Chat::class.java)?.copy(id = snapshot.key ?: ""))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    fun getOtherUserStatus(uid: String): Flow<Long?> = callbackFlow {
        val query = database.getReference("users/$uid/lastSeen")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Long::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    fun getUserProfile(uid: String): Flow<User?> = callbackFlow {
        val query = database.getReference("users/$uid")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(User::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun deleteChat(chatId: String) = withContext(Dispatchers.IO) {
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext
        val chatRef = database.getReference("chats/$chatId")
        val chatSnapshot = chatRef.get().await()
        val chat = chatSnapshot.getValue(Chat::class.java) ?: return@withContext

        val updatedParticipants = chat.participants.toMutableMap()
        updatedParticipants[currentUid] = -1L

        if (updatedParticipants.values.all { it == -1L }) {
            chatRef.removeValue().await()
            database.getReference("messages/$chatId").removeValue().await()
        } else {
            chatRef.child("participants").setValue(updatedParticipants).await()
        }
    }
}
