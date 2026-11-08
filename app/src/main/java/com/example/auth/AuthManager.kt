package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object AuthManager {
    private const val TAG = "CuanAuth"

    fun getClientId(context: Context): String {
        return context.getString(R.string.default_web_client_id)
    }

    fun attemptSilentSignIn(
        context: Context,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onUnauthenticated: () -> Unit
    ) {
        val current = Firebase.auth.currentUser
        if (current != null) {
            onSuccess(current)
            return
        }

        val clientId = try {
            getClientId(context)
        } catch (e: Exception) {
            Log.e(TAG, "Default web client ID not found in resources", e)
            onUnauthenticated()
            return
        }

        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    authResult.user?.let(onSuccess) ?: onUnauthenticated()
                } else {
                    onUnauthenticated()
                }
            } catch (e: NoCredentialException) {
                // Expected on initial install or when not previously authorized
                Log.d(TAG, "No pre-authorized Google credentials found: ${e.message}")
                onUnauthenticated()
            } catch (e: Exception) {
                Log.d(TAG, "Silent sign-in not available: ${e.message}")
                onUnauthenticated()
            }
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit
    ) {
        val clientId = try {
            getClientId(activity)
        } catch (e: Exception) {
            onError("Konfigurasi Google Sign-In belum siap (default_web_client_id tidak ditemukan)")
            return
        }

        val credentialManager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onError("Gagal mendapatkan akun Google pengguna")
                    }
                } else {
                    onError("Tipe kredensial tidak sesuai: ${credential::class.java.simpleName}")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In dibatalkan oleh pengguna: ${e.message}", e)
                onCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Login Google gagal", e)
                onError(e.localizedMessage ?: "Terjadi kesalahan saat login Google")
            }
        }
    }

    fun signOut(
        context: Context,
        scope: CoroutineScope,
        onComplete: () -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)
        scope.launch {
            withContext(NonCancellable) {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(TAG, "Gagal membersihkan state kredensial: ${e.message}")
                } finally {
                    Firebase.auth.signOut()
                    onComplete()
                }
            }
        }
    }
}
