package com.example.melofy.data.repository

import android.app.Activity
import com.example.melofy.di.IoDispatcher
import com.example.melofy.domain.model.User
import com.example.melofy.domain.repository.AuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    override suspend fun loginWithEmail(email: String, password: String): Result<User> = withContext(ioDispatcher) {
        runCatching {
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user ?: throw Exception("Auth failed: Null user returned")
                
                // Fetch details from Firestore
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                if (userDoc.exists()) {
                    User(
                        uid = firebaseUser.uid,
                        name = userDoc.getString("name") ?: "Melofy Listener",
                        email = firebaseUser.email ?: email,
                        avatar = userDoc.getString("avatar") ?: "",
                        createdAt = userDoc.getLong("createdAt") ?: System.currentTimeMillis(),
                        country = userDoc.getString("country") ?: "",
                        favoriteArtists = userDoc.getString("favoriteArtists") ?: "",
                        favoriteGenres = userDoc.getString("favoriteGenres") ?: ""
                    )
                } else {
                    val newUser = User(uid = firebaseUser.uid, name = "Melofy Listener", email = firebaseUser.email ?: email)
                    firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
                    newUser
                }
            } catch (e: Exception) {
                throw mapAuthException(e)
            }
        }
    }

    override suspend fun signUpWithEmail(name: String, email: String, password: String): Result<User> = withContext(ioDispatcher) {
        runCatching {
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user ?: throw Exception("Signup failed: Null user returned")
                
                val newUser = User(
                    uid = firebaseUser.uid,
                    name = name,
                    email = email,
                    avatar = "",
                    createdAt = System.currentTimeMillis()
                )
                
                firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
                newUser
            } catch (e: Exception) {
                throw mapAuthException(e)
            }
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            try {
                firebaseAuth.sendPasswordResetEmail(email).await()
                Unit
            } catch (e: Exception) {
                throw mapAuthException(e)
            }
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> = withContext(ioDispatcher) {
        runCatching {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user ?: throw Exception("Google Authentication returned null user")
                
                // Fetch or create user in Firestore
                val userDocRef = firestore.collection("users").document(firebaseUser.uid)
                val userDocSnapshot = userDocRef.get().await()
                
                val user = if (userDocSnapshot.exists()) {
                    val existingAvatar = userDocSnapshot.getString("avatar") ?: ""
                    User(
                        uid = firebaseUser.uid,
                        name = userDocSnapshot.getString("name") ?: firebaseUser.displayName ?: "Melofy Listener",
                        email = firebaseUser.email ?: "",
                        avatar = existingAvatar.ifBlank { firebaseUser.photoUrl?.toString() ?: "" },
                        createdAt = userDocSnapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                        country = userDocSnapshot.getString("country") ?: "",
                        favoriteArtists = userDocSnapshot.getString("favoriteArtists") ?: "",
                        favoriteGenres = userDocSnapshot.getString("favoriteGenres") ?: ""
                    )
                } else {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "Melofy Listener",
                        email = firebaseUser.email ?: "",
                        avatar = firebaseUser.photoUrl?.toString() ?: "",
                        createdAt = System.currentTimeMillis()
                    )
                    userDocRef.set(newUser).await()
                    newUser
                }
                user
            } catch (e: Exception) {
                throw mapAuthException(e)
            }
        }
    }

    override suspend fun getCurrentUser(): User? = withContext(ioDispatcher) {
        try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val avatar = userDoc.getString("avatar") ?: ""
                User(
                    uid = firebaseUser.uid,
                    name = userDoc.getString("name") ?: firebaseUser.displayName ?: "Melofy Listener",
                    email = firebaseUser.email ?: "",
                    avatar = avatar.ifBlank { firebaseUser.photoUrl?.toString() ?: "" },
                    createdAt = userDoc.getLong("createdAt") ?: System.currentTimeMillis(),
                    country = userDoc.getString("country") ?: "",
                    favoriteArtists = userDoc.getString("favoriteArtists") ?: "",
                    favoriteGenres = userDoc.getString("favoriteGenres") ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            try {
                firebaseAuth.signOut()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    override suspend fun sendPhoneVerificationCode(phoneNumber: String, activity: Activity): Result<String> = withContext(ioDispatcher) {
        runCatching {
            suspendCancellableCoroutine<String> { continuation ->
                val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // Instant verification (or test verification)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        if (continuation.isActive) {
                            continuation.resumeWith(Result.failure(mapAuthException(e)))
                        }
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        if (continuation.isActive) {
                            continuation.resume(verificationId)
                        }
                    }
                }

                val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }
    }

    override suspend fun verifyPhoneCode(verificationId: String, code: String): Result<User> = withContext(ioDispatcher) {
        runCatching {
            try {
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                val result = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = result.user ?: throw Exception("Verification failed: Null user returned")
                
                // Sync with Firestore
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val user = if (userDoc.exists()) {
                    User(
                        uid = firebaseUser.uid,
                        name = userDoc.getString("name") ?: "Melofy Listener",
                        email = firebaseUser.email ?: "",
                        avatar = userDoc.getString("avatar") ?: "",
                        createdAt = userDoc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } else {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        name = "Melofy Listener",
                        email = "",
                        avatar = "",
                        createdAt = System.currentTimeMillis()
                    )
                    firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
                    newUser
                }
                user
            } catch (e: Exception) {
                throw mapAuthException(e)
            }
        }
    }

    override suspend fun updateProfile(name: String, avatar: String): Result<User> = withContext(ioDispatcher) {
        runCatching {
            try {
                val firebaseUser = firebaseAuth.currentUser ?: throw Exception("User not logged in")
                val userDocRef = firestore.collection("users").document(firebaseUser.uid)
                val userDoc = userDocRef.get().await()
                val createdAt = userDoc.getLong("createdAt") ?: System.currentTimeMillis()
                val country = userDoc.getString("country") ?: ""
                val favoriteArtists = userDoc.getString("favoriteArtists") ?: ""
                val favoriteGenres = userDoc.getString("favoriteGenres") ?: ""
                
                val updatedUser = User(
                    uid = firebaseUser.uid,
                    name = name,
                    email = firebaseUser.email ?: "",
                    avatar = avatar,
                    createdAt = createdAt,
                    country = country,
                    favoriteArtists = favoriteArtists,
                    favoriteGenres = favoriteGenres
                )
                userDocRef.set(updatedUser).await()
                
                try {
                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                        displayName = name
                    }
                    firebaseUser.updateProfile(profileUpdates).await()
                } catch (e: Exception) {
                    // Non-blocking if firebase auth update profile fails
                }
                
                updatedUser
            } catch (e: Exception) {
                throw mapAuthException(e)
            }
        }
    }

    override suspend fun updateUserPreferences(
        country: String,
        favoriteArtists: String,
        favoriteGenres: String
    ): Result<User> = withContext(ioDispatcher) {
        runCatching {
            try {
                val firebaseUser = firebaseAuth.currentUser ?: throw Exception("User not logged in")
                val userDocRef = firestore.collection("users").document(firebaseUser.uid)
                val userDoc = userDocRef.get().await()
                val createdAt = userDoc.getLong("createdAt") ?: System.currentTimeMillis()
                val name = userDoc.getString("name") ?: firebaseUser.displayName ?: "Melofy Listener"
                val avatar = userDoc.getString("avatar") ?: firebaseUser.photoUrl?.toString() ?: ""
                
                val updatedUser = User(
                    uid = firebaseUser.uid,
                    name = name,
                    email = firebaseUser.email ?: "",
                    avatar = avatar,
                    createdAt = createdAt,
                    country = country,
                    favoriteArtists = favoriteArtists,
                    favoriteGenres = favoriteGenres
                )
                userDocRef.set(updatedUser).await()
                updatedUser
            } catch (e: Exception) {
                throw mapAuthException(e)
            }
        }
    }

    private fun mapAuthException(e: Exception): Exception {
        return when (e) {
            is FirebaseAuthInvalidUserException -> Exception("Account not found. Please sign up.")
            is FirebaseAuthInvalidCredentialsException -> {
                val errorCode = (e as? FirebaseAuthException)?.errorCode
                when (errorCode) {
                    "ERROR_INVALID_EMAIL" -> Exception("Invalid email address format.")
                    "ERROR_WRONG_PASSWORD" -> Exception("Incorrect password.")
                    "ERROR_USER_DISABLED" -> Exception("This account has been disabled.")
                    else -> Exception("Authentication failed: ${e.localizedMessage}")
                }
            }
            is FirebaseAuthUserCollisionException -> Exception("An account already exists with this email.")
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_OPERATION_NOT_ALLOWED" -> Exception("This login method is disabled in Firebase Console.")
                    "ERROR_TOO_MANY_REQUESTS" -> Exception("Too many attempts. Please try again later.")
                    "ERROR_NETWORK_REQUEST_FAILED" -> Exception("Network error. Please check your connection.")
                    else -> Exception("Firebase Error: ${e.localizedMessage}")
                }
            }
            else -> e
        }
    }
}

