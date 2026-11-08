package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reward_transactions")
data class RewardTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String, // "AD_REWARD", "CHECK_IN", "BONUS_QUEST", "WITHDRAWAL"
    val title: String,
    val detail: String,
    val amount: Long, // +500, -50000, etc.
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS", // "SUCCESS", "PROCESSING"
    val transactionCode: String
)
