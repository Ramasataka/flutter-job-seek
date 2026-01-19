package com.example.uasjobportal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.uasjobportal.SharedPreferences.SessionManager
import com.example.uasjobportal.accessRetroFit.RetrofitClient
import com.example.uasjobportal.utils.goingTo
import kotlinx.coroutines.launch

class SeekerProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnHome: Button
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seeker_profile)

        sessionManager = SessionManager(this)
        tvName = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileEmail)
        btnLogout = findViewById(R.id.btnLogout)
        btnHome = findViewById(R.id.btnHome)

        loadProfile()

        // Logika Logout (Dipindah dari MainActivity)
        btnLogout.setOnClickListener {
            doLogout()
        }

        // Logika Tombol Home -> Kembali ke MainActivity
        btnHome.setOnClickListener {
            val intent = goingTo<MainActivity>()
            // Flag ini agar saat di Home, kalau back tidak balik ke profile
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@SeekerProfileActivity).getCurrentUser()
                if (response.isSuccessful) {
                    val user = response.body()
                    tvName.text = user?.name
                    tvEmail.text = user?.email
                }
            } catch (e: Exception) {
                Toast.makeText(this@SeekerProfileActivity, "Gagal load profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun doLogout() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@SeekerProfileActivity).logoutUser()
                // Logout sukses atau token expired tetap hapus sesi
                sessionManager.clearAuthToken()

                val intent = goingTo<LoginActivity>(clearStack = true)
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                sessionManager.clearAuthToken()
                val intent = goingTo<LoginActivity>(clearStack = true)
                startActivity(intent)
                finish()
            }
        }
    }
}