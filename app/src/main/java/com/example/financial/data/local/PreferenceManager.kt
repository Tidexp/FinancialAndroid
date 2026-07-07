package com.example.financial.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferenceManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_LAST_USER_ID = "last_user_id"
    }

    var authToken: String?
        get() = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        set(value) = sharedPreferences.edit().putString(KEY_AUTH_TOKEN, value).apply()

    var lastUserId: String?
        get() = sharedPreferences.getString(KEY_LAST_USER_ID, null)
        set(value) = sharedPreferences.edit().putString(KEY_LAST_USER_ID, value).apply()

    fun clearAuth() {
        sharedPreferences.edit().remove(KEY_AUTH_TOKEN).apply()
    }
}
