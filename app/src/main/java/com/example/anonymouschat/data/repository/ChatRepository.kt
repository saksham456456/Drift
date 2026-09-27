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

class ChatRepository(
    private val database: FirebaseDatabase
) {

    fun getChats(uid: String): Flow<List<Chat>> = callbackFlow {
        val query = database.getReference("chats").orderByChild("participants/$uid").equalTo(true)
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

        val chatUpdates = mapOf(
            "lastMessage" to finalMessage.text,
            "lastMessageTime" to finalMessage.timestamp
        )
        database.getReference("chats/$chatId").updateChildren(chatUpdates).await()
    }

    suspend fun createDirectChat(currentUid: String, otherUid: String): String = withContext(Dispatchers.IO) {
        val chatsRef = database.getReference("chats")
        
        // This is a naive check. A robust check would involve querying where participants contain both.
        // For simplicity, we query where participants contain currentUid and check locally for otherUid.
        val snapshot = chatsRef.orderByChild("participants/$currentUid").equalTo(true).get().await()
        
        for (child in snapshot.children) {
            val chat = child.getValue(Chat::class.java)
            if (chat != null && chat.type == "direct" && chat.participants.containsKey(otherUid)) {
                return@withContext child.key ?: ""
            }
        }

        val newChatRef = chatsRef.push()
        val chatId = newChatRef.key ?: throw Exception("Failed to generate chat id")
        
        val chat = Chat(
            id = chatId,
            type = "direct",
            participants = mapOf(currentUid to true, otherUid to true),
            createdBy = currentUid
        )
        
        newChatRef.setValue(chat).await()
        chatId
    }

    suspend fun createGroupChat(name: String, memberUids: List<String>, createdByUid: String): String = withContext(Dispatchers.IO) {
        val chatsRef = database.getReference("chats").push()
        val chatId = chatsRef.key ?: throw Exception("Failed to generate chat id")
        
        val participants = memberUids.associateWith { true }.toMutableMap()
        participants[createdByUid] = true

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
                    lastSeenRef.onDisconnect().setValue(ServerValue.TIMESTAMP)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
