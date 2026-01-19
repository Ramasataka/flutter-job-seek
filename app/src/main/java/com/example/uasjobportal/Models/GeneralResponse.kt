package com.example.uasjobportal.Models

import com.google.gson.annotations.SerializedName

data class GeneralResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
