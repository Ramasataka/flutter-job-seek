package com.example.uasjobportal.Models

import com.google.gson.annotations.SerializedName

data class CompanyData(
    @SerializedName("id") val id: Int,
    @SerializedName("company_name") val companyName: String,
    @SerializedName("address") val address: String
)
