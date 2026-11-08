package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldCoinPrimary

@Composable
fun AppBottomNavBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // 1. Beranda (Home)
        NavigationBarItem(
            modifier = Modifier.testTag("nav_home"),
            selected = currentScreen == AppScreen.HOME,
            onClick = { onNavigate(AppScreen.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Beranda"
                )
            },
            label = { Text("Beranda", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldGreenPrimary,
                selectedTextColor = EmeraldGreenPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        // 2. Nonton (+Rp 500) - Special highlight item
        NavigationBarItem(
            modifier = Modifier.testTag("nav_watch_ad"),
            selected = currentScreen == AppScreen.AD_PLAYER,
            onClick = { onNavigate(AppScreen.AD_PLAYER) },
            icon = {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = GoldCoinPrimary,
                            contentColor = Color.Black
                        ) {
                            Text("+500", fontSize = 9.sp)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircleFilled,
                        contentDescription = "Nonton Iklan",
                        tint = if (currentScreen == AppScreen.AD_PLAYER) EmeraldGreenPrimary else GoldCoinPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            label = { Text("Nonton", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldGreenPrimary,
                selectedTextColor = EmeraldGreenPrimary,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
            )
        )

        // 3. Tarik Saldo (Withdraw)
        NavigationBarItem(
            modifier = Modifier.testTag("nav_withdraw"),
            selected = currentScreen == AppScreen.WITHDRAW,
            onClick = { onNavigate(AppScreen.WITHDRAW) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.WITHDRAW) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                    contentDescription = "Tarik Dana"
                )
            },
            label = { Text("Tarik", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldGreenPrimary,
                selectedTextColor = EmeraldGreenPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        // 4. Riwayat (History)
        NavigationBarItem(
            modifier = Modifier.testTag("nav_history"),
            selected = currentScreen == AppScreen.HISTORY,
            onClick = { onNavigate(AppScreen.HISTORY) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.HISTORY) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                    contentDescription = "Riwayat"
                )
            },
            label = { Text("Riwayat", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldGreenPrimary,
                selectedTextColor = EmeraldGreenPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        // 5. Bantuan (FAQ)
        NavigationBarItem(
            modifier = Modifier.testTag("nav_faq"),
            selected = currentScreen == AppScreen.FAQ,
            onClick = { onNavigate(AppScreen.FAQ) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.HelpOutline,
                    contentDescription = "Bantuan"
                )
            },
            label = { Text("Bantuan", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldGreenPrimary,
                selectedTextColor = EmeraldGreenPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
