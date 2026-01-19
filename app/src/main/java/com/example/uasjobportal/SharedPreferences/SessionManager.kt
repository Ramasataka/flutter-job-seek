package com.example.uasjobportal.SharedPreferences

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
    }

    /**
     * Fungsi untuk menyimpan token
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    /**
     * Fungsi untuk mengambil token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    /**
     * Fungsi untuk menghapus token (Logout)
     */
    fun clearAuthToken() {
        val editor = prefs.edit()
        editor.clear() // Menghapus semua data yang tersimpan
        editor.apply()
    }
}