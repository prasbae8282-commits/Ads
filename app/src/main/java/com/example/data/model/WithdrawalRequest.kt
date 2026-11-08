package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "withdrawal_requests")
data class WithdrawalRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val method: String, // "DANA", "GOPAY", "OVO", "SHOPEEPAY", "BANK"
    val providerName: String, // "DANA", "GoPay", "Bank BCA", etc.
    val accountNumber: String,
    val accountName: String,
    val amount: Long, // e.g. 50000, 75000, 100000
    val adminFee: Long = 0L,
    val totalReceived: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "DIPROSES", // "DIPROSES", "BERHASIL"
    val referenceCode: String,
    val estimatedArrival: String = "1 x 24 Jam Kerja"
)
