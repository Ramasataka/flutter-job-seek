package com.example.uasjobportal.Models
import com.google.gson.annotations.SerializedName
data class LoginResponse(
    @SerializedName("message") val message: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("user") val user: User
)
