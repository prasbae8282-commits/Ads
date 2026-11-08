package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_account")
data class UserAccount(
    @PrimaryKey
    val id: Int = 1,
    val balance: Long = 0L,
    val totalEarned: Long = 0L,
    val totalWithdrawn: Long = 0L,
    val adsWatchedCount: Int = 0,
    val adsWatchedToday: Int = 0,
    val lastActiveDate: String = "",
    val streakDays: Int = 0,
    val lastCheckInDate: String = ""
)
