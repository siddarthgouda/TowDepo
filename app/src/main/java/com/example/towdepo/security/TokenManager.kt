package com.example.towdepo.security

// security/TokenManager.kt
import android.content.Context
import android.content.SharedPreferences
import com.example.towdepo.data.AuthResponse
import com.example.towdepo.data.User
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.edit

class TokenManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ACCESS_TOKEN_EXPIRY = "access_token_expiry"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_EMAIL_VERIFIED = "is_email_verified"
    }

    fun saveAuthData(authResponse: AuthResponse) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, authResponse.tokens.access.token)
            putString(KEY_REFRESH_TOKEN, authResponse.tokens.refresh.token)
            putString(KEY_ACCESS_TOKEN_EXPIRY, authResponse.tokens.access.expires)
            putString(KEY_USER_ID, authResponse.user.id)
            putString(KEY_USER_EMAIL, authResponse.user.email)
            putString(KEY_USER_NAME, authResponse.user.name)
            putBoolean(KEY_IS_EMAIL_VERIFIED, authResponse.user.isEmailVerified)
        }.apply()
    }

    fun getAccessToken(): String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)

    fun isAccessTokenExpired(): Boolean {
        val expiryString = sharedPreferences.getString(KEY_ACCESS_TOKEN_EXPIRY, null)
        return expiryString?.let {
            try {
                val expiryDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(it)
                expiryDate?.before(Date()) ?: true
            } catch (e: Exception) {
                true
            }
        } ?: true
    }

    fun getUser(): User? {
        val id = sharedPreferences.getString(KEY_USER_ID, null) ?: return null
        return User(
            id = id,
            name = sharedPreferences.getString(KEY_USER_NAME, "") ?: "",
            email = sharedPreferences.getString(KEY_USER_EMAIL, "") ?: "",
            isEmailVerified = sharedPreferences.getBoolean(KEY_IS_EMAIL_VERIFIED, false)
        )


    }

    fun clearAuthData() {
        sharedPreferences.edit { clear() }
    }

    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrEmpty() && !isAccessTokenExpired()
    }
}