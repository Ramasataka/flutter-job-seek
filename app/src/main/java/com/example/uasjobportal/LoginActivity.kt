package com.example.uasjobportal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.uasjobportal.Models.LoginRequest
import com.example.uasjobportal.SharedPreferences.SessionManager
import com.example.uasjobportal.accessRetroFit.RetrofitClient
import com.example.uasjobportal.utils.goingTo
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegis : TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        // Cek jika user sudah login, langsung lempar ke Main
        if (sessionManager.fetchAuthToken() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        etEmail = findViewById(R.id.etEmailLogin)
        etPass = findViewById(R.id.etPasswordLogin)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegis = findViewById(R.id.textViewCreateAcc)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Email dan Password harus diisi", Toast.LENGTH_SHORT).show()
            } else {
                doLogin(email, pass)
            }
        }

        btnRegis.setOnClickListener{
            startActivity(
                goingTo<RegisterActivity>()
            )
        }
    }

    private fun doLogin(email: String, pass: String) {
        // Gunakan lifecycleScope untuk menjalankan proses background (Coroutines)
        lifecycleScope.launch {
            try {
                // Siapkan Data
                val request = LoginRequest(email, pass)

                // Panggil API
                val response = RetrofitClient.getInstance(this@LoginActivity).loginUser(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // 1. Simpan Token ke Session Manager
                        sessionManager.saveAuthToken(body.accessToken)

                        // 2. Tampilkan pesan
                        Toast.makeText(this@LoginActivity, "Welcome ${body.user.name}", Toast.LENGTH_LONG).show()

                        // 3. Pindah ke MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        // Clear task agar user tidak bisa back ke halaman login
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login Gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}