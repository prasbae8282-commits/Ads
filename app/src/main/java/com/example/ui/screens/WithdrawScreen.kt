package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserAccount
import com.example.data.repository.RewardRepository
import com.example.ui.theme.DanaBlue
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldCoinDark
import com.example.ui.theme.GoldCoinPrimary
import com.example.ui.theme.GopayGreen
import com.example.ui.theme.OvoPurple
import com.example.ui.theme.ShopeeOrange

data class PayoutMethodOption(
    val id: String,
    val name: String,
    val category: String,
    val brandColor: Color,
    val inputHint: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    account: UserAccount?,
    selectedMethod: String,
    selectedNominal: Long,
    accountNumber: String,
    accountHolderName: String,
    errorMessage: String?,
    onMethodSelect: (String) -> Unit,
    onNominalSelect: (Long) -> Unit,
    onAccountNumberChange: (String) -> Unit,
    onAccountHolderNameChange: (String) -> Unit,
    onSubmitWithdrawal: () -> Unit,
    onWatchAdClick: () -> Unit,
    onAddDemoBalance: () -> Unit
) {
    val balance = account?.balance ?: 0L
    val minWithdrawal = 50000L
    val isBalanceSufficient = balance >= selectedNominal
    val canSubmit = isBalanceSufficient &&
            accountNumber.trim().length >= 8 &&
            accountHolderName.trim().length >= 3

    val methods = listOf(
        PayoutMethodOption("DANA", "DANA", "E-Wallet", DanaBlue, "Nomor HP DANA (08xx)"),
        PayoutMethodOption("GOPAY", "GoPay", "E-Wallet", GopayGreen, "Nomor HP GoPay (08xx)"),
        PayoutMethodOption("OVO", "OVO", "E-Wallet", OvoPurple, "Nomor HP OVO (08xx)"),
        PayoutMethodOption("SHOPEEPAY", "ShopeePay", "E-Wallet", ShopeeOrange, "Nomor HP ShopeePay (08xx)"),
        PayoutMethodOption("BANK_BCA", "Bank BCA", "Transfer Bank", Color(0xFF003D79), "Nomor Rekening BCA"),
        PayoutMethodOption("BANK_BRI", "Bank BRI", "Transfer Bank", Color(0xFF00529C), "Nomor Rekening BRI"),
        PayoutMethodOption("BANK_MANDIRI", "Bank Mandiri", "Transfer Bank", Color(0xFF002D62), "Nomor Rekening Mandiri")
    )

    val currentMethod = methods.find { it.id == selectedMethod } ?: methods.first()

    val nominalOptions = listOf(50000L, 75000L, 100000L, 150000L, 250000L)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("withdraw_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top App Bar
        item {
            TopAppBar(
                title = {
                    Text(
                        text = "Tarik Saldo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // Saldo & Syarat Minimal Penarikan Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (balance >= minWithdrawal)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SALDO TERSEDIA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = RewardRepository.formatRupiah(balance),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreenPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (balance >= minWithdrawal) EmeraldGreenPrimary else GoldCoinDark
                        ) {
                            Text(
                                text = "MINIMAL RP 50.000",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (balance < minWithdrawal) {
                        val needed = minWithdrawal - balance
                        val adsNeeded = (needed / 500L).coerceAtLeast(1L)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = GoldCoinDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kurang ${RewardRepository.formatRupiah(needed)} (~$adsNeeded iklan lagi) untuk mencapai batas minimal penarikan.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onWatchAdClick,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tonton Iklan (+500)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onAddDemoBalance,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Demo +Rp 50rb", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Syarat minimal terpenuhi! Saldo kamu siap ditransfer.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldGreenPrimary
                            )
                        }
                    }
                }
            }
        }

        // Section: Pilih Nominal Penarikan
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "1. Pilih Nominal Penarikan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Minimal penarikan adalah Rp 50.000",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(nominalOptions) { nominal ->
                        val isSelected = selectedNominal == nominal
                        val isAvailable = balance >= nominal

                        Card(
                            modifier = Modifier
                                .clickable { onNominalSelect(nominal) }
                                .testTag("nominal_chip_$nominal"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isSelected -> EmeraldGreenPrimary
                                    isAvailable -> MaterialTheme.colorScheme.surface
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            ),
                            border = if (!isSelected && isAvailable) {
                                androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreenPrimary.copy(alpha = 0.4f))
                            } else null,
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = RewardRepository.formatRupiah(nominal),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                if (nominal == 50000L) {
                                    Text(
                                        text = "MINIMAL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else GoldCoinDark
                                    )
                                } else {
                                    Text(
                                        text = if (isAvailable) "Tersedia" else "Kurang",
                                        fontSize = 9.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Pilih Metode Pembayaran
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "2. Pilih E-Wallet / Bank Tujuan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Bebas biaya transfer ke seluruh metode",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(methods) { method ->
                        val isSelected = selectedMethod == method.id

                        Card(
                            modifier = Modifier
                                .clickable { onMethodSelect(method.id) }
                                .testTag("method_chip_${method.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) method.brandColor else MaterialTheme.colorScheme.surface
                            ),
                            border = if (!isSelected) {
                                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                            } else null,
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else method.brandColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = method.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = method.category,
                                        fontSize = 9.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Form Akun Tujuan
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "3. Informasi Rekening / E-Wallet",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = onAccountNumberChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_account_number"),
                        label = { Text(currentMethod.inputHint) },
                        placeholder = { Text("Contoh: 081234567890") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = accountHolderName,
                        onValueChange = onAccountHolderNameChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_account_name"),
                        label = { Text("Nama Lengkap Pemilik Akun") },
                        placeholder = { Text("Nama sesuai akun / e-wallet") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rincian Penarikan
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Ringkasan Penarikan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Nominal Penarikan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(RewardRepository.formatRupiah(selectedNominal), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Biaya Admin", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp 0 (GRATIS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreenPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimasi Waktu", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (currentMethod.category == "E-Wallet") "Instan (1-15 Menit)" else "1x24 Jam Kerja",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Diterima", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    RewardRepository.formatRupiah(selectedNominal),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldGreenPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    Button(
                        onClick = onSubmitWithdrawal,
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_submit_withdrawal"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreenPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBalanceSufficient) "Tarik ${RewardRepository.formatRupiah(selectedNominal)} Sekarang"
                            else "Saldo Kurang (Minimal Rp 50.000)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (!isBalanceSufficient) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Kumpulkan saldo hingga Rp 50.000 dengan menonton iklan 15 detik.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
