package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.AuthManager
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.RewardCelebrationDialog
import com.example.ui.components.WithdrawalReceiptDialog
import com.example.ui.screens.AdPlayerScreen
import com.example.ui.screens.HelpFaqScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.WithdrawScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAuthGate()
            }
        }
    }
}

@Composable
fun MainAuthGate() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }

    // Realtime auth state listener
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Silent auto sign-in attempt on startup
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            AuthManager.attemptSilentSignIn(
                context = context,
                scope = scope,
                onSuccess = { user ->
                    currentUser = user
                },
                onUnauthenticated = {
                    // Stay on SignInScreen
                }
            )
        }
    }

    if (currentUser == null) {
        SignInScreen(
            onSignInSuccess = { user ->
                currentUser = user
            }
        )
    } else {
        val user = currentUser!!
        key(user.uid) {
            val viewModel: MainViewModel = viewModel(key = user.uid)
            LaunchedEffect(user.uid) {
                viewModel.onUserAuthenticated(user)
            }
            CuanNontonApp(
                viewModel = viewModel,
                currentUser = user,
                onSignOut = {
                    viewModel.signOut(context)
                }
            )
        }
    }
}

@Composable
fun CuanNontonApp(
    viewModel: MainViewModel,
    currentUser: FirebaseUser,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userAccount by viewModel.userAccount.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val adState by viewModel.adState.collectAsStateWithLifecycle()
    val selectedMethod by viewModel.selectedMethod.collectAsStateWithLifecycle()
    val selectedNominal by viewModel.selectedNominal.collectAsStateWithLifecycle()
    val accountNumber by viewModel.accountNumber.collectAsStateWithLifecycle()
    val accountHolderName by viewModel.accountHolderName.collectAsStateWithLifecycle()
    val withdrawalSuccess by viewModel.withdrawalSuccess.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle toast messages
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Android back button handling
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        viewModel.navigateTo(AppScreen.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentScreen != AppScreen.AD_PLAYER) {
                AppBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { target ->
                        if (target == AppScreen.AD_PLAYER) {
                            viewModel.startWatchingAd()
                        } else {
                            viewModel.navigateTo(target)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            AppScreen.HOME -> {
                HomeScreen(
                    account = userAccount,
                    currentUser = currentUser,
                    onSignOut = onSignOut,
                    onWatchAdClick = { viewModel.startWatchingAd() },
                    onWithdrawClick = { viewModel.navigateTo(AppScreen.WITHDRAW) },
                    onNavigate = { viewModel.navigateTo(it) },
                    onClaimCheckIn = { day, reward -> viewModel.claimDailyCheckIn(day, reward) },
                    onAddDemoBalance = { viewModel.addDemoBalance50k() },
                    onResetData = { /* Reset if needed */ }
                )
            }
            AppScreen.AD_PLAYER -> {
                AdPlayerScreen(
                    adState = adState,
                    onClaimReward = { viewModel.claimAdReward() },
                    onToggleMute = { viewModel.toggleMute() },
                    onCloseClick = { viewModel.navigateTo(AppScreen.HOME) },
                    onActionClick = {
                        val sponsorUrl = "https://demper.org/4/6705407964029137324caf2320288946"
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(sponsorUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Membuka penawaran sponsor: ${adState.ad.title}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            AppScreen.WITHDRAW -> {
                WithdrawScreen(
                    account = userAccount,
                    selectedMethod = selectedMethod,
                    selectedNominal = selectedNominal,
                    accountNumber = accountNumber,
                    accountHolderName = accountHolderName,
                    errorMessage = errorMessage,
                    onMethodSelect = { viewModel.setWithdrawalMethod(it) },
                    onNominalSelect = { viewModel.setWithdrawalNominal(it) },
                    onAccountNumberChange = { viewModel.setAccountNumber(it) },
                    onAccountHolderNameChange = { viewModel.setAccountHolderName(it) },
                    onSubmitWithdrawal = { viewModel.submitWithdrawal() },
                    onWatchAdClick = { viewModel.startWatchingAd() },
                    onAddDemoBalance = { viewModel.addDemoBalance50k() }
                )
            }
            AppScreen.HISTORY -> {
                HistoryScreen(
                    transactions = transactions
                )
            }
            AppScreen.FAQ, AppScreen.QUESTS -> {
                HelpFaqScreen(
                    onAddDemoBalance = { viewModel.addDemoBalance50k() },
                    onResetData = { onSignOut() }
                )
            }
        }

        // Reward Celebration Dialog (Triggered after 15s ad completion)
        if (adState.showCelebration) {
            RewardCelebrationDialog(
                amount = adState.earnedReward,
                adTitle = adState.ad.title,
                onWatchNext = { viewModel.dismissCelebration(watchNext = true) },
                onDismiss = { viewModel.dismissCelebration(watchNext = false) }
            )
        }

        // Withdrawal Receipt Dialog (Triggered after successful withdrawal request)
        withdrawalSuccess?.let { request ->
            WithdrawalReceiptDialog(
                withdrawal = request,
                onDismiss = { viewModel.dismissWithdrawalReceipt() }
            )
        }
    }
}
