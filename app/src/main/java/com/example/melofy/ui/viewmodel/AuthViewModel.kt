package com.example.melofy.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melofy.domain.model.User
import com.example.melofy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    private val _validationEvent = MutableSharedFlow<String>()
    val validationEvent: SharedFlow<String> = _validationEvent.asSharedFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()

    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _userState.value = UserState.Authenticated(user)
            } else {
                _userState.value = UserState.Unauthenticated
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            viewModelScope.launch { _validationEvent.emit("Email and password cannot be empty") }
            return
        }
        _userState.value = UserState.Loading
        viewModelScope.launch {
            authRepository.loginWithEmail(email, password)
                .onSuccess { user ->
                    _userState.value = UserState.Authenticated(user)
                }
                .onFailure { error ->
                    _userState.value = UserState.Error(error.localizedMessage ?: "Login failed")
                }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            viewModelScope.launch { _validationEvent.emit("All fields are required") }
            return
        }
        if (password.length < 6) {
            viewModelScope.launch { _validationEvent.emit("Password must be at least 6 characters") }
            return
        }
        _userState.value = UserState.Loading
        viewModelScope.launch {
            authRepository.signUpWithEmail(name, email, password)
                .onSuccess { user ->
                    _userState.value = UserState.Authenticated(user)
                }
                .onFailure { error ->
                    _userState.value = UserState.Error(error.localizedMessage ?: "Sign up failed")
                }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _userState.value = UserState.Loading
        viewModelScope.launch {
            authRepository.loginWithGoogle(idToken)
                .onSuccess { user ->
                    _userState.value = UserState.Authenticated(user)
                }
                .onFailure { error ->
                    _userState.value = UserState.Error(error.localizedMessage ?: "Google sign-in failed")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _userState.value = UserState.Unauthenticated
        }
    }

    fun sendPhoneOtp(phoneNumber: String, activity: Activity) {
        if (phoneNumber.isBlank()) {
            viewModelScope.launch { _validationEvent.emit("Phone number cannot be empty") }
            return
        }
        _userState.value = UserState.Loading
        viewModelScope.launch {
            authRepository.sendPhoneVerificationCode(phoneNumber, activity)
                .onSuccess { verificationId ->
                    _verificationId.value = verificationId
                    _otpSent.value = true
                    _userState.value = UserState.Unauthenticated
                }
                .onFailure { error ->
                    _userState.value = UserState.Error(error.localizedMessage ?: "Failed to send code")
                }
        }
    }

    fun verifyPhoneOtp(code: String) {
        val verificationId = _verificationId.value
        if (verificationId.isNullOrBlank()) {
            viewModelScope.launch { _validationEvent.emit("No verification session found. Please request OTP again.") }
            return
        }
        if (code.isBlank() || code.length < 6) {
            viewModelScope.launch { _validationEvent.emit("Please enter a valid 6-digit OTP code") }
            return
        }
        _userState.value = UserState.Loading
        viewModelScope.launch {
            authRepository.verifyPhoneCode(verificationId, code)
                .onSuccess { user ->
                    _userState.value = UserState.Authenticated(user)
                }
                .onFailure { error ->
                    _userState.value = UserState.Error(error.localizedMessage ?: "Verification failed")
                }
        }
    }

    fun resetOtpState() {
        _verificationId.value = null
        _otpSent.value = false
        if (_userState.value is UserState.Error) {
            _userState.value = UserState.Unauthenticated
        }
    }

    fun resetErrorState() {
        if (_userState.value is UserState.Error) {
            _userState.value = UserState.Unauthenticated
        }
    }

    fun updateProfile(name: String, avatar: String) {
        _userState.value = UserState.Loading
        viewModelScope.launch {
            authRepository.updateProfile(name, avatar)
                .onSuccess { updatedUser ->
                    _userState.value = UserState.Authenticated(updatedUser)
                }
                .onFailure { error ->
                    _userState.value = UserState.Error(error.localizedMessage ?: "Failed to update profile")
                }
        }
    }

    fun updateUserPreferences(country: String, favoriteArtists: String, favoriteGenres: String, onSuccess: () -> Unit) {
        _userState.value = UserState.Loading
        viewModelScope.launch {
            authRepository.updateUserPreferences(country, favoriteArtists, favoriteGenres)
                .onSuccess { updatedUser ->
                    _userState.value = UserState.Authenticated(updatedUser)
                    onSuccess()
                }
                .onFailure { error ->
                    _userState.value = UserState.Error(error.localizedMessage ?: "Failed to save preferences")
                }
        }
    }

    sealed interface UserState {
        object Idle : UserState
        object Loading : UserState
        data class Authenticated(val user: User) : UserState
        object Unauthenticated : UserState
        data class Error(val message: String) : UserState
    }
}
