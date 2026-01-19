package com.example.uasjobportal.Models

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    // Endpoint Login (Public)
    @POST("login")
    suspend fun loginUser(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // Endpoint Logout (Protected - Butuh Header Token)
    // Token akan diurus otomatis oleh RetrofitClient (Interceptor)
    @POST("register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("logout")
    suspend fun logoutUser(): Response<LogoutResponse>

    @GET("me")
    suspend fun getCurrentUser(): Response<User>

    @GET("seeker/jobs")
    suspend fun getSeekerJobs(): Response<JobSeekerResponse>
}