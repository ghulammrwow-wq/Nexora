package com.example.tokenroomdatabase.utils

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TokenManager(context: Context) {

    private val preferences =
        context.getSharedPreferences(
            "auth_prefs",
            Context.MODE_PRIVATE
        )

    // Date and Time Format
    private val dateFormat =
        SimpleDateFormat(
            "dd MMM yyyy, h:mm:ss a",
            Locale.getDefault()
        )


    // SAVE LOGIN
    fun saveLogin(
        token: String,
        email: String
    ) {

        val currentTime =
            System.currentTimeMillis()

        preferences.edit()
            .putString(
                "token",
                token
            )
            .putString(
                "user_email",
                email
            )
            .putBoolean(
                "isLoggedIn",
                true
            )
            .putLong(
                "last_active_time",
                currentTime
            )
            .apply()

        val loginTime =
            dateFormat.format(
                Date(currentTime)
            )

        val expiryTime =
            currentTime + (10 * 60 * 1000L)

        val expiryDate =
            dateFormat.format(
                Date(expiryTime)
            )

        Log.d(
            "SESSION_CHECK",
            "========================================"
        )

        Log.d(
            "SESSION_CHECK",
            "LOGIN SAVED"
        )

        Log.d(
            "SESSION_CHECK",
            "Token: $token"
        )

        Log.d(
            "SESSION_CHECK",
            "Email: $email"
        )

        Log.d(
            "SESSION_CHECK",
            "Login Time: $loginTime"
        )

        Log.d(
            "SESSION_CHECK",
            "Session Expiry: $expiryDate"
        )

        Log.d(
            "SESSION_CHECK",
            "Session Duration: 10 minutes"
        )

        Log.d(
            "SESSION_CHECK",
            "========================================"
        )
    }

    // GET TOKEN
    fun getToken(): String? {

        return preferences.getString(
            "token",
            null
        )
    }

    // GET USER EMAIL
    fun getUserEmail(): String? {

        return preferences.getString(
            "user_email",
            null
        )
    }

    // CHECK LOGIN
    fun isLoggedIn(): Boolean {

        return preferences.getBoolean(
            "isLoggedIn",
            false
        )
    }

    // UPDATE LAST ACTIVE TIME
    fun updateLastActiveTime() {

        val currentTime =
            System.currentTimeMillis()

        preferences.edit()
            .putLong(
                "last_active_time",
                currentTime
            )
            .apply()

        val activeTime =
            dateFormat.format(
                Date(currentTime)
            )

        Log.d(
            "SESSION_CHECK",
            "Last Active Time Updated: $activeTime"
        )
    }

    // CHECK SESSION
    fun isSessionValid(): Boolean {

        val token =
            getToken()

        if (token == null) {

            Log.d(
                "SESSION_CHECK",
                "Token is NULL"
            )

            return false
        }

        if (!isLoggedIn()) {

            Log.d(
                "SESSION_CHECK",
                "User is NOT logged in"
            )

            return false
        }

        val lastActiveTime =
            preferences.getLong(
                "last_active_time",
                0
            )

        if (lastActiveTime == 0L) {

            Log.d(
                "SESSION_CHECK",
                "Last Active Time is missing"
            )

            return false
        }

        // 10 Minutes
        val sessionDuration =
            10 * 60 * 1000L

        val currentTime =
            System.currentTimeMillis()

        val elapsedTime =
            currentTime - lastActiveTime

        val remainingTime =
            sessionDuration - elapsedTime

        val valid =
            elapsedTime < sessionDuration

        // Proper formatted times
        val lastActiveDate =
            dateFormat.format(
                Date(lastActiveTime)
            )

        val currentDate =
            dateFormat.format(
                Date(currentTime)
            )

        val expiryTime =
            lastActiveTime + sessionDuration

        val expiryDate =
            dateFormat.format(
                Date(expiryTime)
            )

        // Remaining time
        val remainingMinutes =
            if (remainingTime > 0) {
                remainingTime / 60000
            } else {
                0
            }

        val remainingSeconds =
            if (remainingTime > 0) {
                (remainingTime % 60000) / 1000
            } else {
                0
            }

        Log.d(
            "SESSION_CHECK",
            "========================================"
        )

        Log.d(
            "SESSION_CHECK",
            "SESSION CHECK"
        )

        Log.d(
            "SESSION_CHECK",
            "Token: $token"
        )

        Log.d(
            "SESSION_CHECK",
            "Last Active: $lastActiveDate"
        )

        Log.d(
            "SESSION_CHECK",
            "Current Time: $currentDate"
        )

        Log.d(
            "SESSION_CHECK",
            "Expiry Time: $expiryDate"
        )

        Log.d(
            "SESSION_CHECK",
            "Elapsed Time: ${elapsedTime / 60000} minutes ${(elapsedTime % 60000) / 1000} seconds"
        )

        Log.d(
            "SESSION_CHECK",
            "Remaining Time: %02d minutes %02d seconds"
                .format(
                    remainingMinutes,
                    remainingSeconds
                )
        )

        Log.d(
            "SESSION_CHECK",
            "Session Valid: $valid"
        )

        Log.d(
            "SESSION_CHECK",
            "========================================"
        )

        return valid
    }


    // CLEAR LOGIN / LOGOUT
    fun clearToken() {

        preferences.edit()
            .clear()
            .apply()

        Log.d(
            "SESSION_CHECK",
            "========================================"
        )

        Log.d(
            "SESSION_CHECK",
            "SESSION CLEARED"
        )

        Log.d(
            "SESSION_CHECK",
            "User logged out"
        )

        Log.d(
            "SESSION_CHECK",
            "========================================"
        )
    }

    fun clearSession() {
        clearToken()
    }
}