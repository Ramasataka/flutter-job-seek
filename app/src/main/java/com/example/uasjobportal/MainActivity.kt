package com.example.uasjobportal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uasjobportal.SharedPreferences.SessionManager
import com.example.uasjobportal.accessRetroFit.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var sessionManager: SessionManager
    lateinit var btnLogout: Button
    lateinit var rcDt: RecyclerView
    private lateinit var btnRefresh: ImageButton // Variable tombol refresh
    private lateinit var progressBar: ProgressBar // Variable loading
    private lateinit var tvEmpty: TextView
    private lateinit var jobAdapter: JobSeekerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek Token Dulu sebelum setContent
        sessionManager = SessionManager(this)
        if (sessionManager.fetchAuthToken() == null) {
            // Jika token tidak ada, pindah ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Jika ada token, load layout dashboard
        setContentView(R.layout.activity_main)
        // Note: Saya pakai activity_auth.xml sesuai request anda sebelumnya
        // yang berisi RecylerView jobList

        // Inisialisasi View
        rcDt = findViewById(R.id.jobList)
        btnRefresh = findViewById(R.id.btnRefresh) // Bind tombol refresh
        progressBar = findViewById(R.id.progressBar) // Bind loading
        tvEmpty = findViewById(R.id.tvEmpty)

        // Saya gunakan buttonLogReg sebagai tombol logout
        btnLogout = findViewById(R.id.buttonLogReg)
        btnLogout.text = "LOGOUT" // Ubah teks jadi logout

        btnLogout.setOnClickListener {
            doLogout()
        }

        jobAdapter = JobSeekerAdapter(arrayListOf())
        rcDt.layoutManager = LinearLayoutManager(this)
        rcDt.adapter = jobAdapter

        // 4. Load Data dari Server
        loadJobData()

        btnRefresh.setOnClickListener {
            loadJobData()
        }
    }

    private fun loadJobData() {
        // 1. Tampilkan Loading, Sembunyikan List
        progressBar.visibility = View.VISIBLE
        rcDt.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        btnRefresh.isEnabled = false // Disable tombol biar gak di klik berkali2

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(this@MainActivity).getSeekerJobs()

                // 2. Sembunyikan Loading setelah request selesai
                progressBar.visibility = View.GONE
                btnRefresh.isEnabled = true

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        if (body.data.isNotEmpty()) {
                            // Data Ada
                            rcDt.visibility = View.VISIBLE
                            jobAdapter.setData(body.data)
                        } else {
                            // Data Kosong
                            tvEmpty.visibility = View.VISIBLE
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error Server: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnRefresh.isEnabled = true
                Toast.makeText(this@MainActivity, "Koneksi Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun doLogout() {
        lifecycleScope.launch {
            try {
                // Panggil API Logout ke Server (Optional tapi disarankan agar token di server hangus)
                val response = RetrofitClient.getInstance(this@MainActivity).logoutUser()

                if (response.isSuccessful || response.code() == 401) {
                    // 401 berarti token expired, tetap kita logoutkan di device

                    // 1. Hapus token dari HP
                    sessionManager.clearAuthToken()

                    // 2. Pindah ke Login Activity
                    Toast.makeText(this@MainActivity, "Logout Berhasil", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Gagal Logout: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                // Jika error koneksi, paksa logout lokal saja
                sessionManager.clearAuthToken()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}