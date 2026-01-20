package com.example.uasjobportal.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyHelper {
    fun formatRupiah(nominal: String?): String {
        if (nominal.isNullOrEmpty()) return "Rp 0"
        return try {
            // Bersihkan string dari karakter non-angka jika ada
            val cleanString = nominal.replace("[^\\d]".toRegex(), "")
            val number = cleanString.toDouble()
            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            numberFormat.maximumFractionDigits = 0 // Hilangkan ,00 di belakang
            numberFormat.format(number)
        } catch (e: Exception) {
            "Rp $nominal" // Fallback jika gagal format
        }
    }
}