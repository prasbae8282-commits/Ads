package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AdCampaign
import com.example.data.model.FirestoreRewardTransaction
import com.example.data.model.FirestoreWithdrawalRequest
import com.example.data.model.RewardTransaction
import com.example.data.model.UserAccount
import com.example.data.model.UserProfile
import com.example.data.model.WithdrawalRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FirestoreRewardRepository(private val context: Context) {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.getString(R.string.firestore_database_id)
    )

    private val auth = Firebase.auth
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google sebelum mengakses data.")
    }

    fun observeUserProfile(userId: String): Flow<UserAccount?> {
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    val profile = snapshot.toObject(UserProfile::class.java)
                    profile?.let {
                        UserAccount(
                            id = 1,
                            balance = it.balance,
                            totalEarned = it.totalEarned,
                            totalWithdrawn = it.totalWithdrawn,
                            adsWatchedCount = it.adsWatchedCount,
                            adsWatchedToday = it.adsWatchedToday,
                            lastActiveDate = it.lastActiveDate,
                            streakDays = it.streakDays,
                            lastCheckInDate = it.lastCheckInDate
                        )
                    }
                } else {
                    null
                }
            }
    }

    fun observeTransactions(userId: String): Flow<List<RewardTransaction>> {
        return db.collection("users").document(userId)
            .collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { doc ->
                    val tx = doc.toObject(FirestoreRewardTransaction::class.java)
                    tx?.let {
                        RewardTransaction(
                            id = doc.id.hashCode().toLong(),
                            type = it.type,
                            title = it.title,
                            detail = it.detail,
                            amount = it.amount,
                            timestamp = it.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                            status = it.status,
                            transactionCode = it.transactionCode
                        )
                    }
                }
            }
    }

    suspend fun ensureUserProfile(user: FirebaseUser) {
        val uid = user.uid
        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()
        val today = dateFormat.format(Date())

        if (!snapshot.exists()) {
            val initialBalance = 1500L
            val initialPayload = mapOf(
                "userId" to uid,
                "displayName" to (user.displayName ?: "Pengguna Cuan"),
                "email" to (user.email ?: ""),
                "photoUrl" to user.photoUrl?.toString(),
                "balance" to initialBalance,
                "totalEarned" to initialBalance,
                "totalWithdrawn" to 0L,
                "adsWatchedCount" to 0,
                "adsWatchedToday" to 0,
                "lastActiveDate" to today,
                "streakDays" to 1,
                "lastCheckInDate" to today,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(initialPayload).await()

            // Initial welcome transaction
            val txRef = docRef.collection("transactions").document()
            val txCode = generateCode("NEW")
            val txPayload = mapOf(
                "userId" to uid,
                "type" to "BONUS_QUEST",
                "title" to "Bonus Pengguna Baru",
                "detail" to "Selamat datang di CuanNonton! Modal awal kumpulkan saldo.",
                "amount" to initialBalance,
                "status" to "SUCCESS",
                "transactionCode" to txCode,
                "createdAt" to FieldValue.serverTimestamp()
            )
            txRef.set(txPayload).await()
        } else {
            // Check day change
            val lastActive = snapshot.getString("lastActiveDate") ?: ""
            if (lastActive != today) {
                docRef.update(
                    mapOf(
                        "adsWatchedToday" to 0,
                        "lastActiveDate" to today,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        }
    }

    suspend fun claimAdReward(ad: AdCampaign): RewardTransaction {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val reward = ad.rewardAmount // 500L

        docRef.update(
            mapOf(
                "balance" to FieldValue.increment(reward),
                "totalEarned" to FieldValue.increment(reward),
                "adsWatchedCount" to FieldValue.increment(1),
                "adsWatchedToday" to FieldValue.increment(1),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        val txCode = generateCode("ADS")
        val txRef = docRef.collection("transactions").document()
        val txPayload = mapOf(
            "userId" to uid,
            "type" to "AD_REWARD",
            "title" to "Nonton Iklan 15 Detik",
            "detail" to "Sponsor: ${ad.sponsor} - ${ad.title}",
            "amount" to reward,
            "status" to "SUCCESS",
            "transactionCode" to txCode,
            "createdAt" to FieldValue.serverTimestamp()
        )
        txRef.set(txPayload).await()

        return RewardTransaction(
            type = "AD_REWARD",
            title = "Nonton Iklan 15 Detik",
            detail = "Sponsor: ${ad.sponsor} - ${ad.title}",
            amount = reward,
            status = "SUCCESS",
            transactionCode = txCode
        )
    }

    suspend fun claimDailyCheckIn(day: Int, rewardAmount: Long): Boolean {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()
        val today = dateFormat.format(Date())

        val lastCheckIn = snapshot.getString("lastCheckInDate") ?: ""
        val currentStreak = snapshot.getLong("streakDays")?.toInt() ?: 0

        if (lastCheckIn == today && currentStreak > 0) {
            return false // Already claimed today
        }

        val newStreak = if (currentStreak >= 7) 1 else currentStreak + 1
        docRef.update(
            mapOf(
                "balance" to FieldValue.increment(rewardAmount),
                "totalEarned" to FieldValue.increment(rewardAmount),
                "streakDays" to newStreak,
                "lastCheckInDate" to today,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        val txCode = generateCode("CHK")
        val txRef = docRef.collection("transactions").document()
        val txPayload = mapOf(
            "userId" to uid,
            "type" to "CHECK_IN",
            "title" to "Check-in Harian (Hari ke-$day)",
            "detail" to "Bonus login harian beruntun",
            "amount" to rewardAmount,
            "status" to "SUCCESS",
            "transactionCode" to txCode,
            "createdAt" to FieldValue.serverTimestamp()
        )
        txRef.set(txPayload).await()
        return true
    }

    suspend fun requestWithdrawal(
        method: String,
        providerName: String,
        accountNumber: String,
        accountName: String,
        amount: Long
    ): RewardRepository.WithdrawalResult {
        if (amount < 50000L) {
            return RewardRepository.WithdrawalResult.Error("Minimal penarikan adalah Rp 50.000")
        }

        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()
        val currentBalance = snapshot.getLong("balance") ?: 0L

        if (currentBalance < amount) {
            return RewardRepository.WithdrawalResult.Error(
                "Saldo kamu (${RewardRepository.formatRupiah(currentBalance)}) tidak mencukupi untuk menarik ${RewardRepository.formatRupiah(amount)}"
            )
        }

        if (accountNumber.trim().length < 8) {
            return RewardRepository.WithdrawalResult.Error("Nomor akun / e-wallet tidak valid (minimal 8 digit)")
        }

        if (accountName.trim().length < 3) {
            return RewardRepository.WithdrawalResult.Error("Silakan masukkan nama pemilik akun secara lengkap")
        }

        val refCode = generateCode("WD")
        val estimated = if (method in listOf("DANA", "GOPAY", "OVO", "SHOPEEPAY")) {
            "Instan (1 - 15 Menit)"
        } else {
            "1 x 24 Jam Kerja"
        }

        // Deduct from profile
        docRef.update(
            mapOf(
                "balance" to FieldValue.increment(-amount),
                "totalWithdrawn" to FieldValue.increment(amount),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        // Create withdrawal request document
        val wdRef = docRef.collection("withdrawals").document()
        val wdPayload = mapOf(
            "userId" to uid,
            "method" to method,
            "providerName" to providerName,
            "accountNumber" to accountNumber.trim(),
            "accountName" to accountName.trim(),
            "amount" to amount,
            "adminFee" to 0L,
            "totalReceived" to amount,
            "status" to "DIPROSES",
            "referenceCode" to refCode,
            "estimatedArrival" to estimated,
            "createdAt" to FieldValue.serverTimestamp()
        )
        wdRef.set(wdPayload).await()

        // Create transaction ledger document
        val txRef = docRef.collection("transactions").document()
        val txPayload = mapOf(
            "userId" to uid,
            "type" to "WITHDRAWAL",
            "title" to "Penarikan Saldo $providerName",
            "detail" to "No: ${accountNumber.trim()} a/n ${accountName.trim()}",
            "amount" to -amount,
            "status" to "PROCESSING",
            "transactionCode" to refCode,
            "createdAt" to FieldValue.serverTimestamp()
        )
        txRef.set(txPayload).await()

        val withdrawalObj = WithdrawalRequest(
            id = wdRef.id.hashCode().toLong(),
            method = method,
            providerName = providerName,
            accountNumber = accountNumber.trim(),
            accountName = accountName.trim(),
            amount = amount,
            adminFee = 0L,
            totalReceived = amount,
            status = "DIPROSES",
            referenceCode = refCode,
            estimatedArrival = estimated
        )

        return RewardRepository.WithdrawalResult.Success(withdrawalObj)
    }

    suspend fun addDemoBalance(amount: Long = 50000L) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)

        docRef.update(
            mapOf(
                "balance" to FieldValue.increment(amount),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        val txCode = generateCode("TEST")
        val txRef = docRef.collection("transactions").document()
        val txPayload = mapOf(
            "userId" to uid,
            "type" to "BONUS_QUEST",
            "title" to "Saldo Demo Pengujian",
            "detail" to "Top-up saldo cepat untuk verifikasi penarikan",
            "amount" to amount,
            "status" to "SUCCESS",
            "transactionCode" to txCode,
            "createdAt" to FieldValue.serverTimestamp()
        )
        txRef.set(txPayload).await()
    }

    private fun generateCode(prefix: String): String {
        val rand = UUID.randomUUID().toString().substring(0, 6).uppercase(Locale.US)
        return "CWN-$prefix-$rand"
    }
}
