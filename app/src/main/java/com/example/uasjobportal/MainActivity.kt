package com.example.uasjobportal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uasjobportal.Models.GeneralResponse
import com.example.uasjobportal.SharedPreferences.SessionManager
import com.example.uasjobportal.accessRetroFit.RetrofitClient
import com.example.uasjobportal.utils.goingTo
import com.google.gson.Gson
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var btnProfile: Button
    private lateinit var rcDt: RecyclerView
    private lateinit var searchView: SearchView // Variable Search
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

        setContentView(R.layout.activity_main)

        // Init Views
        rcDt = findViewById(R.id.jobList)
        btnProfile = findViewById(R.id.buttonToProfile) // ID Baru
        btnRefresh = findViewById(R.id.btnRefresh)
        searchView = findViewById(R.id.search_bar) // Bind Search View
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)

        // Setup Adapter
        jobAdapter = JobSeekerAdapter(arrayListOf()) { jobId ->
            applyJob(jobId)
        }
        rcDt.layoutManager = LinearLayoutManager(this)
        rcDt.adapter = jobAdapter

        // Load Data Awal (Tanpa Search)
        loadJobData(null)

        // --- LISTENER SEARCH ---
        setupSearchView()

        // --- TOMBOL PROFILE ---
        btnProfile.setOnClickListener {
            // Pindah ke Profile Activity
            startActivity(goingTo<SeekerProfileActivity>())
        }

        // Listener Refresh
        btnRefresh.setOnClickListener {
            // Kosongkan search bar saat refresh
            searchView.setQuery("", false)
            searchView.clearFocus()
            loadJobData(null)
        }
    }
    private fun applyJob(jobId: Int) {
        // Tampilkan loading kecil atau toast progress (opsional)
        Toast.makeText(this, "Mengirim lamaran...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                // Panggil API Apply
                val response = RetrofitClient.getInstance(this@MainActivity).applyJob(jobId)

                if (response.isSuccessful) {
                    // Code 200: Sukses
                    val message = response.body()?.message ?: "Lamaran berhasil dikirim!"
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                } else {
                    // Code 400 (Sudah Apply) atau 404 (Job Hilang)
                    // Kita harus membaca manual errorBody karena Retrofit menganggap ini error
                    val errorJson = response.errorBody()?.string()

                    if (errorJson != null) {
                        try {
                            // Convert JSON error ke Object GeneralResponse agar bisa ambil messagenya
                            val errorResponse = Gson().fromJson(errorJson, GeneralResponse::class.java)
                            Toast.makeText(this@MainActivity, errorResponse.message, Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Terjadi kesalahan: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Gagal mengirim lamaran", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Koneksi Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Panggil API saat tombol search di keyboard ditekan
                loadJobData(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Opsional: Jika ingin search real-time saat ngetik
                // loadJobData(newText)

                // Jika teks dihapus semua, load data awal
                if (newText.isNullOrEmpty()) {
                    loadJobData(null)
                }
                return false
            }
        })
    }

    // Update parameter function untuk menerima search query
    private fun loadJobData(keyword: String?) {
        progressBar.visibility = View.VISIBLE
        rcDt.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        btnRefresh.isEnabled = false

        lifecycleScope.launch {
            try {
                // Kirim parameter keyword ke API
                val response = RetrofitClient.getInstance(this@MainActivity).getSeekerJobs(keyword)

                progressBar.visibility = View.GONE
                btnRefresh.isEnabled = true

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        if (body.data.isNotEmpty()) {
                            rcDt.visibility = View.VISIBLE
                            jobAdapter.setData(body.data)
                        } else {
                            tvEmpty.text = "Tidak ada pekerjaan ditemukan"
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