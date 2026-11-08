package com.example.data.repository

import com.example.data.local.TransactionDao
import com.example.data.local.UserDao
import com.example.data.local.WithdrawalDao
import com.example.data.model.AdCampaign
import com.example.data.model.RewardTransaction
import com.example.data.model.UserAccount
import com.example.data.model.WithdrawalRequest
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RewardRepository(
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val withdrawalDao: WithdrawalDao
) {
    val userAccount: Flow<UserAccount?> = userDao.getUserAccount()
    val allTransactions: Flow<List<RewardTransaction>> = transactionDao.getAllTransactions()
    val allWithdrawals: Flow<List<WithdrawalRequest>> = withdrawalDao.getAllWithdrawals()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    suspend fun ensureUserAccount(): UserAccount {
        val existing = userDao.getUserAccountDirect()
        val today = dateFormat.format(Date())
        if (existing == null) {
            val initial = UserAccount(
                id = 1,
                balance = 1500L, // Starter bonus Rp 1.500 to welcome user
                totalEarned = 1500L,
                totalWithdrawn = 0L,
                adsWatchedCount = 0,
                adsWatchedToday = 0,
                lastActiveDate = today,
                streakDays = 1,
                lastCheckInDate = today
            )
            userDao.insertOrUpdate(initial)
            transactionDao.insertTransaction(
                RewardTransaction(
                    type = "BONUS_QUEST",
                    title = "Bonus Pengguna Baru",
                    detail = "Selamat datang di CuanNonton! Modal awal kumpulkan saldo.",
                    amount = 1500L,
                    status = "SUCCESS",
                    transactionCode = generateCode("NEW")
                )
            )
            return initial
        } else {
            // Check if day changed to reset daily ad counter
            if (existing.lastActiveDate != today) {
                userDao.resetDailyCount(today)
            }
            return existing
        }
    }

    suspend fun claimAdReward(ad: AdCampaign): RewardTransaction {
        val reward = ad.rewardAmount // 500L
        userDao.addAdReward(reward)

        val tx = RewardTransaction(
            type = "AD_REWARD",
            title = "Nonton Iklan 15 Detik",
            detail = "Sponsor: ${ad.sponsor} - ${ad.title}",
            amount = reward,
            status = "SUCCESS",
            transactionCode = generateCode("ADS")
        )
        transactionDao.insertTransaction(tx)
        return tx
    }

    suspend fun claimDailyCheckIn(day: Int, rewardAmount: Long): Boolean {
        val current = userDao.getUserAccountDirect() ?: return false
        val today = dateFormat.format(Date())
        if (current.lastCheckInDate == today && current.streakDays > 0) {
            return false // Already claimed today
        }

        val newStreak = if (current.streakDays >= 7) 1 else current.streakDays + 1
        val updated = current.copy(
            balance = current.balance + rewardAmount,
            totalEarned = current.totalEarned + rewardAmount,
            streakDays = newStreak,
            lastCheckInDate = today
        )
        userDao.update(updated)

        transactionDao.insertTransaction(
            RewardTransaction(
                type = "CHECK_IN",
                title = "Check-in Harian (Hari ke-$day)",
                detail = "Bonus login harian beruntun",
                amount = rewardAmount,
                status = "SUCCESS",
                transactionCode = generateCode("CHK")
            )
        )
        return true
    }

    sealed class WithdrawalResult {
        data class Success(val request: WithdrawalRequest) : WithdrawalResult()
        data class Error(val message: String) : WithdrawalResult()
    }

    suspend fun requestWithdrawal(
        method: String,
        providerName: String,
        accountNumber: String,
        accountName: String,
        amount: Long
    ): WithdrawalResult {
        if (amount < 50000L) {
            return WithdrawalResult.Error("Minimal penarikan adalah Rp 50.000")
        }

        val account = userDao.getUserAccountDirect()
            ?: return WithdrawalResult.Error("Data pengguna tidak ditemukan")

        if (account.balance < amount) {
            return WithdrawalResult.Error("Saldo kamu (${formatRupiah(account.balance)}) tidak mencukupi untuk menarik ${formatRupiah(amount)}")
        }

        if (accountNumber.trim().length < 8) {
            return WithdrawalResult.Error("Nomor akun / e-wallet tidak valid (minimal 8 digit)")
        }

        if (accountName.trim().length < 3) {
            return WithdrawalResult.Error("Silakan masukkan nama pemilik akun secara lengkap")
        }

        // Deduct balance
        userDao.deductBalanceForWithdrawal(amount)

        val refCode = generateCode("WD")
        val withdrawal = WithdrawalRequest(
            method = method,
            providerName = providerName,
            accountNumber = accountNumber.trim(),
            accountName = accountName.trim(),
            amount = amount,
            adminFee = 0L,
            totalReceived = amount,
            status = "DIPROSES",
            referenceCode = refCode,
            estimatedArrival = if (method in listOf("DANA", "GOPAY", "OVO", "SHOPEEPAY")) {
                "Instan (1 - 15 Menit)"
            } else {
                "1 x 24 Jam Kerja"
            }
        )

        val id = withdrawalDao.insertWithdrawal(withdrawal)

        // Add to transactions
        transactionDao.insertTransaction(
            RewardTransaction(
                type = "WITHDRAWAL",
                title = "Penarikan Saldo $providerName",
                detail = "No: ${accountNumber.trim()} a/n ${accountName.trim()}",
                amount = -amount,
                status = "PROCESSING",
                transactionCode = refCode
            )
        )

        return WithdrawalResult.Success(withdrawal.copy(id = id))
    }

    // Tester / Demo Sandbox method to instantly add balance for easy evaluation
    suspend fun addDemoBalance(amount: Long = 50000L) {
        userDao.addBonusBalance(amount)
        transactionDao.insertTransaction(
            RewardTransaction(
                type = "BONUS_QUEST",
                title = "Saldo Demo Pengujian",
                detail = "Top-up saldo cepat untuk verifikasi penarikan",
                amount = amount,
                status = "SUCCESS",
                transactionCode = generateCode("TEST")
            )
        )
    }

    suspend fun resetAllData() {
        val today = dateFormat.format(Date())
        val freshAccount = UserAccount(
            id = 1,
            balance = 1500L,
            totalEarned = 1500L,
            totalWithdrawn = 0L,
            adsWatchedCount = 0,
            adsWatchedToday = 0,
            lastActiveDate = today,
            streakDays = 1,
            lastCheckInDate = today
        )
        userDao.insertOrUpdate(freshAccount)
        transactionDao.clearAll()
        transactionDao.insertTransaction(
            RewardTransaction(
                type = "BONUS_QUEST",
                title = "Bonus Pengguna Baru",
                detail = "Selamat datang di CuanNonton! Modal awal kumpulkan saldo.",
                amount = 1500L,
                status = "SUCCESS",
                transactionCode = generateCode("NEW")
            )
        )
    }

    private fun generateCode(prefix: String): String {
        val rand = UUID.randomUUID().toString().substring(0, 6).uppercase(Locale.US)
        return "CWN-$prefix-$rand"
    }

    companion object {
        fun formatRupiah(amount: Long): String {
            val formatted = java.text.NumberFormat.getIntegerInstance(Locale("id", "ID")).format(amount)
            return "Rp $formatted"
        }
    }
}
