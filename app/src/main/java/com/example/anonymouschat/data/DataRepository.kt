package com.example.anonymouschat.data

import com.example.anonymouschat.core.model.Drift
import com.example.anonymouschat.core.model.Message
import com.example.anonymouschat.core.model.Pocket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface DataRepository {
    suspend fun signInAnonymously(): String?
    fun getDrifts(): Flow<List<Drift>>
    suspend fun releaseDrift(drift: Drift)
    fun getPocketMessages(pocketId: String): Flow<List<Message>>
    suspend fun sendMessage(message: Message)
}

@Singleton
class DefaultDataRepository @Inject constructor() : DataRepository {
    
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val driftsRef = database.getReference("drifts")
    private val messagesRef = database.getReference("messages")

    override suspend fun signInAnonymously(): String? {
        return try {
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            auth.currentUser?.uid
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getDrifts(): Flow<List<Drift>> = callbackFlow {
        // Simple listener for Drifts
        val listener = driftsRef.limitToLast(50).addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(Drift::class.java) }.reversed()
                trySend(items)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { driftsRef.removeEventListener(listener) }
    }

    override suspend fun releaseDrift(drift: Drift) {
        val uid = auth.currentUser?.uid ?: return
        val driftWithAuthor = drift.copy(authorId = uid)
        driftsRef.child(driftWithAuthor.id).setValue(driftWithAuthor).await()
    }

    override fun getPocketMessages(pocketId: String): Flow<List<Message>> = callbackFlow {
        val ref = messagesRef.child(pocketId)
        val listener = ref.limitToLast(100).addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                trySend(items)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun sendMessage(message: Message) {
        val uid = auth.currentUser?.uid ?: return
        val msgWithSender = message.copy(senderId = uid)
        messagesRef.child(message.pocketId).child(message.id).setValue(msgWithSender).await()
    }
}
