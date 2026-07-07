package com.example.financial.data.repository

import com.example.financial.data.local.AppDatabase
import com.example.financial.data.local.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import java.util.*

/**
 * Custom User data class for Local-First Auth
 */
data class AuthUser(
    val id: String,
    val username: String,
    val isAnonymous: Boolean = false
)

class AuthRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val database: AppDatabase,
    private val preferenceManager: PreferenceManager
) {
    // Current user state (Local-First)
    var currentUser: AuthUser? = null
        private set

    init {
        // Re-hydrate session from PreferenceManager
        val lastId = preferenceManager.lastUserId
        val token = preferenceManager.authToken
        if (lastId != null && token != null) {
            currentUser = AuthUser(lastId, "User_$lastId")
        }
    }

    suspend fun signUp(username: String, password: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            // 1. Hash password with BCrypt
            val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

            // 2. Mock API call or Firestore check (using Firestore as "Custom Server" for demo)
            val userId = UUID.randomUUID().toString()
            val userData = hashMapOf(
                "userId" to userId,
                "username" to username,
                "password" to hashedPassword,
                "createdAt" to System.currentTimeMillis()
            )

            // Check if username exists
            val existing = firestore.collection("custom_users")
                .whereEqualTo("username", username)
                .get().await()

            if (!existing.isEmpty) return@withContext Result.failure(Exception("Tên đăng nhập đã tồn tại"))

            firestore.collection("custom_users").document(userId).set(userData).await()

            val user = AuthUser(userId, username)
            handleLoginSuccess(user, "mock_token_$userId")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(username: String, password: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            val result = firestore.collection("custom_users")
                .whereEqualTo("username", username)
                .get().await()

            if (result.isEmpty) return@withContext Result.failure(Exception("Người dùng không tồn tại"))

            val doc = result.documents[0]
            val hashed = doc.getString("password") ?: ""
            val userId = doc.id

            // 3. Verify password
            if (BCrypt.checkpw(password, hashed)) {
                val user = AuthUser(userId, username)
                handleLoginSuccess(user, "mock_token_$userId")
                Result.success(user)
            } else {
                Result.failure(Exception("Sai mật khẩu"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<AuthUser> = withContext(Dispatchers.IO) {
        val guestId = "guest_" + UUID.randomUUID().toString().take(8)
        val user = AuthUser(guestId, "Guest", isAnonymous = true)
        handleLoginSuccess(user, "guest_token")
        Result.success(user)
    }

    private suspend fun handleLoginSuccess(user: AuthUser, token: String) {
        val lastUserId = preferenceManager.lastUserId

        if (lastUserId != null && lastUserId != user.id) {
            // Security: Clear data if switching accounts
            database.clearDatabase()
        }

        preferenceManager.lastUserId = user.id
        preferenceManager.authToken = token
        currentUser = user
    }

    fun signOut() {
        preferenceManager.clearAuth()
        currentUser = null
    }
}
