package com.example.uasjobportal.Models

import com.google.gson.annotations.SerializedName

data class JobSeekerResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<JobData>
)
