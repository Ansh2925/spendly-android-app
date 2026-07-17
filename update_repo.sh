cat << 'INNER_EOF' > app/src/main/java/com/example/data/repository/AuthRepository.kt
package com.example.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class Profile(
    val id: String,
    val full_name: String,
    val email: String
)

@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val auth = supabaseClient.auth

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

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
        }
        val user = result?.id ?: return false
        
        try {
            auth.updateUser {
                data = buildJsonObject {
                    put("full_name", name)
                }
            }
        } catch (e: Exception) {
            // Might fail if not auto-logged in (e.g. requires email verification)
        }
        
        try {
            val profile = Profile(
                id = user,
                full_name = name,
                email = email
            )
            supabaseClient.postgrest["profiles"].insert(profile)
        } catch (e: Exception) {
            // Might fail if RLS not perfectly set up or network issue,
            // but auth might have succeeded.
        }
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

    suspend fun logout() {
        auth.signOut()
        _currentUser.value = null
    }
    
    suspend fun resetPassword(email: String) {
        auth.resetPasswordForEmail(email)
    }
}
INNER_EOF
