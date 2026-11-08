package com.example

import com.example.data.repository.RewardRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testRupiahFormatting() {
        val formatted500 = RewardRepository.formatRupiah(500L)
        val formatted50k = RewardRepository.formatRupiah(50000L)
        assertTrue(formatted500.contains("500"))
        assertTrue(formatted50k.contains("50.000"))
    }

    @Test
    fun testMinimumWithdrawalConstant() {
        val minWithdrawal = 50000L
        val adReward = 500L
        val adsNeeded = minWithdrawal / adReward
        assertEquals(100L, adsNeeded)
    }
}
