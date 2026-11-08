package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RewardTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM reward_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<RewardTransaction>>

    @Query("SELECT * FROM reward_transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<RewardTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: RewardTransaction): Long

    @Query("DELETE FROM reward_transactions")
    suspend fun clearAll()
}
