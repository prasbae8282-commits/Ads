package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_account WHERE id = 1 LIMIT 1")
    fun getUserAccount(): Flow<UserAccount?>

    @Query("SELECT * FROM user_account WHERE id = 1 LIMIT 1")
    suspend fun getUserAccountDirect(): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: UserAccount)

    @Update
    suspend fun update(account: UserAccount)

    @Query("UPDATE user_account SET balance = balance + :amount, totalEarned = totalEarned + :amount, adsWatchedCount = adsWatchedCount + 1, adsWatchedToday = adsWatchedToday + 1 WHERE id = 1")
    suspend fun addAdReward(amount: Long = 500L)

    @Query("UPDATE user_account SET balance = balance + :amount WHERE id = 1")
    suspend fun addBonusBalance(amount: Long)

    @Query("UPDATE user_account SET balance = balance - :amount, totalWithdrawn = totalWithdrawn + :amount WHERE id = 1")
    suspend fun deductBalanceForWithdrawal(amount: Long)

    @Query("UPDATE user_account SET adsWatchedToday = 0, lastActiveDate = :todayDate WHERE id = 1")
    suspend fun resetDailyCount(todayDate: String)
}
