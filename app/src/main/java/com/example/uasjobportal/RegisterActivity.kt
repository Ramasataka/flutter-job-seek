package com.example.uasjobportal

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.uasjobportal.Models.RegisterRequest
import com.example.uasjobportal.Models.User
import com.example.uasjobportal.SharedPreferences.SessionManager
import com.example.uasjobportal.accessRetroFit.RetrofitClient
import kotlinx.coroutines.launch
import com.example.uasjobportal.utils.goingTo

class RegisterActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var btnRegister: Button
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        etUsername = findViewById(R.id.editTextUsername)
        etEmail = findViewById(R.id.editTextRegEmail)
        etPass = findViewById(R.id.editTextRegPass)
        spinnerRole = findViewById(R.id.spinnerRoleUser)
        btnRegister = findViewById(R.id.buttonRegister)

        // Setup Spinner Role
        val roles = arrayOf("user", "company")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spinnerRole.adapter = adapter

        btnRegister.setOnClickListener {
            doRegister()
        }
    }

    private fun doRegister() {
        val name = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPass.text.toString().trim()
        val role = spinnerRole.selectedItem.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val req = RegisterRequest(name, email, password, role)
                val response = RetrofitClient.getInstance(this@RegisterActivity).registerUser(req)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // Simpan token
                        sessionManager.saveAuthToken(body.accessToken)

                        Toast.makeText(this@RegisterActivity, "Register Berhasil!", Toast.LENGTH_SHORT).show()

                        // Pindah halaman sesuai Role
                        checkRoleAndNavigate(body.user.role)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@RegisterActivity, "Gagal: $errorBody", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun checkRoleAndNavigate(role: String) {
        if (role == "company") {
            // Pindah ke ProfileActivity (Dashboard Company)
            val intent = goingTo<MainActivity>()
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            // Pindah ke MainActivity (Job Seeker)
            val intent = goingTo<MainActivity>()
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        finish()
    }
}