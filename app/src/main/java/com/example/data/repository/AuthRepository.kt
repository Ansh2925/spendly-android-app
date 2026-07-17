package com.example.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.spendly.BuildConfig
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class Profile(
    val id: String,
    val full_name: String,
    val email: String,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val credentialManager: CredentialManager
) {
    private val auth = supabaseClient.auth
    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()
    
    suspend fun observeSession() {
        checkSession()
    }

    suspend fun checkSession(): Boolean {
        return try {
            auth.awaitInitialization()
            val session = auth.currentSessionOrNull()
            _currentUser.value = auth.currentUserOrNull()
            session != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signUp(name: String, email: String, password: String): Boolean {
        val result = auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = buildJsonObject {
                put("full_name", name)
            }
        }
        val user = result?.id ?: return false
        return true
    }

    suspend fun login(email: String, password: String): Boolean {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        _currentUser.value = auth.currentUserOrNull()
        return true
    }
    
    suspend fun signInWithGoogle(context: Context): Boolean {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.joinToString("") { "%02x".format(it) }
        
        val googleIdOption = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setNonce(hashedNonce)
            .build()
            
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
            
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            
            auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google
                this.nonce = rawNonce
            }
            
            _currentUser.value = auth.currentUserOrNull()
            
            // Upsert profile
            val user = _currentUser.value
            if (user != null) {
                try {
                    val name = user.userMetadata?.get("full_name")?.toString()?.removeSurrounding("\"") ?: "User"
                    val email = user.email ?: ""
                    
                    val profile = buildJsonObject {
                        put("id", user.id)
                        put("full_name", name)
                        put("email", email)
                    }
                    supabaseClient.postgrest["profiles"].upsert(profile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return true
        }
        return false
    }

    suspend fun logout() {
        try {
            credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
        } catch (e: Exception) {
            // Ignore
        }
        auth.signOut()
        _currentUser.value = null
    }
    
    suspend fun resetPassword(email: String) {
        auth.resetPasswordForEmail(email)
    }
}
