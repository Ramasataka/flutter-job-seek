package com.example.uasjobportal.accessRetroFit

import android.content.Context
import com.example.uasjobportal.Models.ApiService
import com.example.uasjobportal.SharedPreferences.SessionManager
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient {

    companion object {
        // Pastikan URL Ngrok ini masih AKTIF (Ngrok ganti ID setiap direstart kecuali bayar)
        private const val BASE_URL = "https://9aec1a454178.ngrok-free.app/api/"

        fun getInstance(context: Context): ApiService {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val sessionManager = SessionManager(context)

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()

                    // --- WAJIB: Agar Laravel tau ini permintaan API, bukan Browser ---
                    requestBuilder.addHeader("Accept", "application/json")
                    requestBuilder.addHeader("Content-Type", "application/json")

                    // --- WAJIB: Bypass Peringatan Ngrok ---
                    requestBuilder.addHeader("ngrok-skip-browser-warning", "true")

                    // Ambil Token jika ada
                    val token = sessionManager.fetchAuthToken()
                    if (!token.isNullOrEmpty()) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }

                    chain.proceed(requestBuilder.build())
                }
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            val gson = GsonBuilder()
                .setLenient() // Agar lebih toleran jika ada sedikit kesalahan format
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}