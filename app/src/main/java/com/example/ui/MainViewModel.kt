package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.data.model.AdCampaign
import com.example.data.model.RewardTransaction
import com.example.data.model.SampleAdCampaigns
import com.example.data.model.UserAccount
import com.example.data.model.WithdrawalRequest
import com.example.data.repository.FirestoreRewardRepository
import com.example.data.repository.RewardRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    AD_PLAYER,
    WITHDRAW,
    HISTORY,
    QUESTS,
    FAQ
}

data class AdPlayerState(
    val ad: AdCampaign = SampleAdCampaigns.all.first(),
    val remainingSeconds: Int = 15,
    val totalSeconds: Int = 15,
    val isPlaying: Boolean = false,
    val isCompleted: Boolean = false,
    val isMuted: Boolean = false,
    val showCelebration: Boolean = false,
    val earnedReward: Long = 500L
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreRepository = FirestoreRewardRepository(application)

    // Current user state
    private val _currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // Navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // User Account & Data StateFlows
    private val _userAccount = MutableStateFlow<UserAccount?>(null)
    val userAccount: StateFlow<UserAccount?> = _userAccount.asStateFlow()

    private val _transactions = MutableStateFlow<List<RewardTransaction>>(emptyList())
    val transactions: StateFlow<List<RewardTransaction>> = _transactions.asStateFlow()

    // Ad Player State
    private val _adState = MutableStateFlow(AdPlayerState())
    val adState: StateFlow<AdPlayerState> = _adState.asStateFlow()

    private var timerJob: Job? = null
    private var observeJobs = mutableListOf<Job>()

    // Withdrawal Form State
    private val _selectedMethod = MutableStateFlow("DANA")
    val selectedMethod: StateFlow<String> = _selectedMethod.asStateFlow()

    private val _selectedNominal = MutableStateFlow(50000L)
    val selectedNominal: StateFlow<Long> = _selectedNominal.asStateFlow()

    private val _accountNumber = MutableStateFlow("")
    val accountNumber: StateFlow<String> = _accountNumber.asStateFlow()

    private val _accountHolderName = MutableStateFlow("")
    val accountHolderName: StateFlow<String> = _accountHolderName.asStateFlow()

    private val _withdrawalSuccess = MutableStateFlow<WithdrawalRequest?>(null)
    val withdrawalSuccess: StateFlow<WithdrawalRequest?> = _withdrawalSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        Firebase.auth.currentUser?.let { user ->
            onUserAuthenticated(user)
        }
    }

    fun onUserAuthenticated(user: FirebaseUser) {
        _currentUser.value = user
        observeJobs.forEach { it.cancel() }
        observeJobs.clear()

        viewModelScope.launch {
            try {
                firestoreRepository.ensureUserProfile(user)
            } catch (e: Exception) {
                // Ignore or log
            }
        }

        val accountJob = viewModelScope.launch {
            firestoreRepository.observeUserProfile(user.uid).collect { account ->
                _userAccount.value = account
            }
        }
        observeJobs.add(accountJob)

        val txJob = viewModelScope.launch {
            firestoreRepository.observeTransactions(user.uid).collect { txs ->
                _transactions.value = txs
            }
        }
        observeJobs.add(txJob)
    }

    fun signOut(context: Context) {
        AuthManager.signOut(context, viewModelScope) {
            observeJobs.forEach { it.cancel() }
            observeJobs.clear()
            _currentUser.value = null
            _userAccount.value = null
            _transactions.value = emptyList()
            _currentScreen.value = AppScreen.HOME
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun startWatchingAd(specificAd: AdCampaign? = null) {
        timerJob?.cancel()
        val chosenAd = specificAd ?: SampleAdCampaigns.getRandom(_adState.value.ad.id)
        _adState.value = AdPlayerState(
            ad = chosenAd,
            remainingSeconds = 15,
            totalSeconds = 15,
            isPlaying = true,
            isCompleted = false,
            showCelebration = false,
            earnedReward = chosenAd.rewardAmount
        )
        _currentScreen.value = AppScreen.AD_PLAYER

        timerJob = viewModelScope.launch {
            for (sec in 15 downTo 1) {
                _adState.value = _adState.value.copy(
                    remainingSeconds = sec,
                    isPlaying = true
                )
                delay(1000)
            }
            // 15 seconds completed!
            _adState.value = _adState.value.copy(
                remainingSeconds = 0,
                isPlaying = false,
                isCompleted = true
            )
            triggerHaptic()
        }
    }

    fun claimAdReward() {
        val currentAd = _adState.value.ad
        viewModelScope.launch {
            try {
                firestoreRepository.claimAdReward(currentAd)
                triggerCelebrationHaptic()
                _adState.value = _adState.value.copy(
                    showCelebration = true
                )
            } catch (e: Exception) {
                _toastMessage.value = "Gagal mengklaim reward: ${e.message}"
            }
        }
    }

    fun dismissCelebration(watchNext: Boolean) {
        _adState.value = _adState.value.copy(showCelebration = false)
        if (watchNext) {
            startWatchingAd()
        } else {
            _currentScreen.value = AppScreen.HOME
        }
    }

    fun toggleMute() {
        _adState.value = _adState.value.copy(isMuted = !_adState.value.isMuted)
    }

    fun setWithdrawalMethod(method: String) {
        _selectedMethod.value = method
    }

    fun setWithdrawalNominal(nominal: Long) {
        _selectedNominal.value = nominal
    }

    fun setAccountNumber(number: String) {
        _accountNumber.value = number
    }

    fun setAccountHolderName(name: String) {
        _accountHolderName.value = name
    }

    fun submitWithdrawal() {
        val method = _selectedMethod.value
        val providerName = when (method) {
            "DANA" -> "DANA"
            "GOPAY" -> "GoPay"
            "OVO" -> "OVO"
            "SHOPEEPAY" -> "ShopeePay"
            "BANK_BCA" -> "Bank BCA"
            "BANK_BRI" -> "Bank BRI"
            "BANK_MANDIRI" -> "Bank Mandiri"
            else -> method
        }
        val number = _accountNumber.value
        val name = _accountHolderName.value
        val nominal = _selectedNominal.value

        viewModelScope.launch {
            when (val result = firestoreRepository.requestWithdrawal(method, providerName, number, name, nominal)) {
                is RewardRepository.WithdrawalResult.Success -> {
                    triggerCelebrationHaptic()
                    _withdrawalSuccess.value = result.request
                    _errorMessage.value = null
                }
                is RewardRepository.WithdrawalResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun dismissWithdrawalReceipt() {
        _withdrawalSuccess.value = null
        _currentScreen.value = AppScreen.HISTORY
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun claimDailyCheckIn(day: Int, reward: Long) {
        viewModelScope.launch {
            try {
                val success = firestoreRepository.claimDailyCheckIn(day, reward)
                if (success) {
                    triggerCelebrationHaptic()
                    _toastMessage.value = "Berhasil klaim bonus harian: +${RewardRepository.formatRupiah(reward)}"
                } else {
                    _toastMessage.value = "Kamu sudah melakukan check-in hari ini!"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Gagal klaim: ${e.message}"
            }
        }
    }

    fun addDemoBalance50k() {
        viewModelScope.launch {
            try {
                firestoreRepository.addDemoBalance(50000L)
                triggerCelebrationHaptic()
                _toastMessage.value = "Mode Sandbox: +Rp 50.000 berhasil ditambahkan ke saldo!"
            } catch (e: Exception) {
                _toastMessage.value = "Gagal menambah saldo demo: ${e.message}"
            }
        }
    }

    private fun triggerHaptic() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(80)
            }
        } catch (_: Exception) {
            // Ignore
        }
    }

    private fun triggerCelebrationHaptic() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                val timings = longArrayOf(0, 100, 80, 150)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            }
        } catch (_: Exception) {
            // Ignore
        }
    }
}
