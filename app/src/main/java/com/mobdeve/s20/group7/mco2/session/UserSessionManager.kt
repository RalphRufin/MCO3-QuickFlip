package com.mobdeve.s20.group7.mco2.session

import android.content.Context
import com.mobdeve.s20.group7.mco2.models.User

class UserSessionManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    companion object {
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_PROFILE_PIC_URL = "profilePicUrl"
        private const val KEY_AUTH_METHOD = "authMethod"
    }

    fun saveUserSession(user: User) {
        editor.apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, user.username)
            putString(KEY_EMAIL, user.email)
            putString(KEY_PROFILE_PIC_URL, user.profilePicUrl)
            putString(KEY_AUTH_METHOD, user.authMethod)
            apply()
        }
    }

    fun getCurrentUser(): User? {
        if (!sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            return null
        }

        return User(
            username = sharedPreferences.getString(KEY_USERNAME, "") ?: "",
            email = sharedPreferences.getString(KEY_EMAIL, "") ?: "",
            profilePicUrl = sharedPreferences.getString(KEY_PROFILE_PIC_URL, "") ?: "",
            authMethod = sharedPreferences.getString(KEY_AUTH_METHOD, "") ?: "",
            deckItems = listOf()
        )
    }

    fun clearSession() {
        editor.apply {
            clear()
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
}