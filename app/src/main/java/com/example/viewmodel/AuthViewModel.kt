package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _isSessionChecking = MutableStateFlow(true)
    val isSessionChecking = _isSessionChecking.asStateFlow()

    val currentUser = authRepository.currentUser

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val hasSession = authRepository.checkSession()
            if (hasSession) {
                _authState.value = AuthState.Authenticated
            }
            _isSessionChecking.value = false
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.signUp(name, email, password)
                // Assuming auto-login or requiring manual login. 
                // Let's assume auto-login if no email confirmation required:
                authRepository.login(email, password)
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                val msg = e.message?.lowercase() ?: ""
                val userMsg = when {
                    msg.contains("email_not_confirmed") || msg.contains("email not confirmed") -> "Signup successful. Please check your email to confirm your account."
                    msg.contains("weak") && msg.contains("password") -> "Password is too weak. Please use a stronger password."
                    msg.contains("already registered") || msg.contains("already exists") -> "This email is already registered. Try logging in."
                    msg.contains("invalid email") || msg.contains("format") -> "Please provide a valid email address."
                    else -> "Signup failed. Please try again."
                }
                _authState.value = AuthState.Error(userMsg)
            }
        }
    }


fun signInWithGoogle(context: android.content.Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val success = authRepository.signInWithGoogle(context)
                if (success) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Google Sign-In failed or was cancelled.")
                }
            } catch (e: Exception) {
                val msg = e.message?.lowercase() ?: ""
                val userMsg = when {
                    msg.contains("cancel") -> "Google Sign-In cancelled."
                    msg.contains("network") -> "Network error during Google Sign-In."
                    msg.contains("no credential") || msg.contains("no_credential") -> "No Google accounts found. Please add a Google account to your device in Settings."
                    else -> "Google Sign-In failed: ${e.message}"
                }
                _authState.value = AuthState.Error(userMsg)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.login(email, password)
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                val msg = e.message?.lowercase() ?: ""
                val userMsg = when {
                    msg.contains("email_not_confirmed") || msg.contains("email not confirmed") -> "Please check your email to confirm your account before logging in."
                    msg.contains("invalid login credentials") || msg.contains("invalid email or password") -> "Incorrect email or password. Please try again."
                    msg.contains("invalid email") || msg.contains("format") -> "Please provide a valid email address."
                    else -> "Login failed. Please check your credentials and try again."
                }
                _authState.value = AuthState.Error(userMsg)
            }
        }
    }

    fun logout() {
        _authState.value = AuthState.Idle
        viewModelScope.launch {
            authRepository.logout()
        }
    }
    
    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                authRepository.resetPassword(email)
                _authState.value = AuthState.Error("Password reset email sent (if registered)") // Using Error state for toast 
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to send reset email")
            }
        }
    }
    
    fun resetState() {
        if (_authState.value !is AuthState.Authenticated) {
            _authState.value = AuthState.Idle
        }
    }
}
