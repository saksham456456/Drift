package com.example.anonymouschat.data.repository

import com.example.anonymouschat.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    suspend fun signUp(username: String, password: String, displayName: String): Result<String> {
        return try {
            val email = "${username}@drift.app"
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("User creation failed")

            val user = User(
                uid = uid,
                username = username,
                displayName = displayName,
                createdAt = System.currentTimeMillis()
            )

            // Save user data
            database.getReference("users").child(uid).setValue(user).await()
            
            // Save username mapping
            database.getReference("usernames").child(username).setValue(uid).await()

            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, password: String): Result<String> {
        return try {
            // Alternatively, we could just use username@drift.app as email to sign in directly
            val email = "${username}@drift.app"
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Login failed")
            
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUid(): String? {
        return auth.currentUser?.uid
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun logout() {
        auth.signOut()
    }
}
