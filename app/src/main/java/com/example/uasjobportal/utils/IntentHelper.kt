package com.example.uasjobportal.utils

import android.app.Activity
import android.content.Context
import android.content.Intent

// Extension function agar pindah activity lebih mudah
inline fun <reified T : Activity> Context.goingTo(
    shouldFinish: Boolean = false,
    clearStack: Boolean = false
): Intent {
    val intent = Intent(this, T::class.java)
    if (clearStack) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    // Jika dipanggil dari activity, kita bisa start langsung
    // Tapi karena return type adalah Intent, user tetap harus startActivity(intent)
    return intent
}