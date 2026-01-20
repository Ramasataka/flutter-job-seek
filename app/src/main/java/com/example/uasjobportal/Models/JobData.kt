package com.example.uasjobportal.Models

import com.google.gson.annotations.SerializedName

data class JobData(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("job_desc") val jobDesc: String,
    @SerializedName("salary") val salary: String,
    @SerializedName("location") val location: String,
    @SerializedName("company") val company: CompanyData // Nested Object
)
