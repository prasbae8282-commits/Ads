package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val balance: Long = 0L,
    val totalEarned: Long = 0L,
    val totalWithdrawn: Long = 0L,
    val adsWatchedCount: Int = 0,
    val adsWatchedToday: Int = 0,
    val lastActiveDate: String = "",
    val streakDays: Int = 1,
    val lastCheckInDate: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreRewardTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val type: String = "",
    val title: String = "",
    val detail: String = "",
    val amount: Long = 0L,
    val status: String = "SUCCESS",
    val transactionCode: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreWithdrawalRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val method: String = "",
    val providerName: String = "",
    val accountNumber: String = "",
    val accountName: String = "",
    val amount: Long = 0L,
    val adminFee: Long = 0L,
    val totalReceived: Long = 0L,
    val status: String = "DIPROSES",
    val referenceCode: String = "",
    val estimatedArrival: String = "1 x 24 Jam Kerja",
    @ServerTimestamp val createdAt: Timestamp? = null
)
