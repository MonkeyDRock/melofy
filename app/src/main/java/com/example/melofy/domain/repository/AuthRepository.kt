package com.example.melofy.domain.repository

import android.app.Activity
import com.example.melofy.domain.model.User

interface AuthRepository {
    suspend fun loginWithEmail(email: String, password: String): Result<User>
    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<User>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun loginWithGoogle(idToken: String): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun logout(): Result<Unit>
    
    suspend fun sendPhoneVerificationCode(phoneNumber: String, activity: Activity): Result<String>
    suspend fun verifyPhoneCode(verificationId: String, code: String): Result<User>
    suspend fun updateProfile(name: String, avatar: String): Result<User>
    suspend fun updateUserPreferences(country: String, favoriteArtists: String, favoriteGenres: String): Result<User>
}
