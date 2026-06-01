package com.example.melofy.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.melofy.ui.components.GlassmorphicCard
import com.example.melofy.ui.theme.Accent
import com.example.melofy.ui.theme.Background
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Secondary
import com.example.melofy.ui.theme.TextPrimary
import com.example.melofy.ui.theme.TextSecondary
import com.example.melofy.ui.viewmodel.AuthViewModel
import com.example.melofy.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val state by viewModel.userState.collectAsState()
    val otpSent by viewModel.otpSent.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    var loginType by remember { mutableStateOf("Email") } // "Email" or "Phone"
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("+91") }
    var otpCode by remember { mutableStateOf("") }

    LaunchedEffect(key1 = state) {
        if (state is AuthViewModel.UserState.Authenticated) {
            onAuthSuccess()
        }
        if (state is AuthViewModel.UserState.Error) {
            Toast.makeText(context, (state as AuthViewModel.UserState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetErrorState()
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.validationEvent.collect { validationMsg ->
            Toast.makeText(context, validationMsg, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        // Soft backdrop elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Secondary.copy(0.06f), Color.Transparent),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glowing Application Logo/Header
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Melofy Logo",
                modifier = Modifier
                    .size(90.dp)
                    .padding(bottom = 12.dp)
            )

            Text(
                text = "Melofy",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp,
                    brush = Brush.linearGradient(listOf(Primary, Secondary))
                )
            )

            Text(
                text = "Feel Every Beat",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Accent,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Premium Glassmorphic input container
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (loginType == "Email") {
                            if (isLoginMode) "Welcome Back" else "Create Account"
                        } else {
                            if (!otpSent) "Verify Phone" else "Enter OTP Code"
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 24.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sliding Premium Glass Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color.White.copy(0.04f), RoundedCornerShape(24.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Email", "Phone").forEach { type ->
                            val isSelected = loginType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(
                                        if (isSelected) Primary else Color.Transparent,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        loginType = type
                                        viewModel.resetOtpState()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedContent(
                        targetState = Triple(loginType, isLoginMode, otpSent),
                        label = "form_fields"
                    ) { (type, isLogin, isOtpSent) ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (type == "Email") {
                                if (!isLogin) {
                                    // Full name for registration
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Full Name") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Primary
                                            )
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Primary,
                                            unfocusedBorderColor = Color.White.copy(0.12f),
                                            focusedLabelColor = Primary,
                                            unfocusedLabelColor = TextSecondary,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // Email Address
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Email Address") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = Primary
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primary,
                                        unfocusedBorderColor = Color.White.copy(0.12f),
                                        focusedLabelColor = Primary,
                                        unfocusedLabelColor = TextSecondary,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Password Input
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Primary
                                        )
                                    },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primary,
                                        unfocusedBorderColor = Color.White.copy(0.12f),
                                        focusedLabelColor = Primary,
                                        unfocusedLabelColor = TextSecondary,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            } else {
                                // Phone Verification flow
                                if (!isOtpSent) {
                                    // Step 1: Input Phone Number
                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { phoneNumber = it },
                                        label = { Text("Phone Number") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = null,
                                                tint = Primary
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Primary,
                                            unfocusedBorderColor = Color.White.copy(0.12f),
                                            focusedLabelColor = Primary,
                                            unfocusedLabelColor = TextSecondary,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                } else {
                                    // Step 2: Verification Code Input
                                    Text(
                                        text = "Verification code sent to $phoneNumber",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    OutlinedTextField(
                                        value = otpCode,
                                        onValueChange = { otpCode = it },
                                        label = { Text("6-Digit OTP Code") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Primary
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Primary,
                                            unfocusedBorderColor = Color.White.copy(0.12f),
                                            focusedLabelColor = Primary,
                                            unfocusedLabelColor = TextSecondary,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Change Number / Resend OTP",
                                        color = Primary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .clickable {
                                                viewModel.resetOtpState()
                                                otpCode = ""
                                            }
                                            .padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Primary login / register / OTP trigger button
                    Button(
                        onClick = {
                            if (loginType == "Email") {
                                if (isLoginMode) {
                                    viewModel.login(email, password)
                                } else {
                                    viewModel.signUp(name, email, password)
                                }
                            } else {
                                if (!otpSent) {
                                    if (activity != null) {
                                        viewModel.sendPhoneOtp(phoneNumber, activity)
                                    } else {
                                        Toast.makeText(context, "Error: Android activity context unavailable", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    viewModel.verifyPhoneOtp(otpCode)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = state !is AuthViewModel.UserState.Loading
                    ) {
                        if (state is AuthViewModel.UserState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (loginType == "Email") {
                                    if (isLoginMode) "Sign In" else "Sign Up"
                                } else {
                                    if (!otpSent) "Send Verification Code" else "Verify & Sign In"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }

                    if (loginType == "Email") {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Toggle text action
                        Text(
                            text = if (isLoginMode) "New to Melofy? Create Account" else "Already have an account? Sign In",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Accent,
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable {
                                    isLoginMode = !isLoginMode
                                }
                                .padding(4.dp)
                        )

                        // Divider "or continue with"
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(0.08f)))
                            Text(
                                text = "or",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(0.08f)))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Google Login Button (Glassmorphic Outline Button with colorful 'G')
                        val coroutineScope = rememberCoroutineScope()
                        
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val credentialManager = CredentialManager.create(context)
                                        val googleIdOption = GetGoogleIdOption.Builder()
                                            .setFilterByAuthorizedAccounts(false)
                                            .setServerClientId("87510348335-j0e2jkmtjd3r3gj0j6cvnab89ab316is.apps.googleusercontent.com")
                                            .setAutoSelectEnabled(false)
                                            .build()

                                        val getCredRequest = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()

                                        val result = credentialManager.getCredential(
                                            context = context,
                                            request = getCredRequest
                                        )
                                        val credential = result.credential
                                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                                            viewModel.loginWithGoogle(googleIdToken.idToken)
                                        } else if (credential is GoogleIdTokenCredential) {
                                            viewModel.loginWithGoogle(credential.idToken)
                                        } else {
                                            Toast.makeText(context, "Google Sign-In returned invalid credentials type: ${credential.type}", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        if (e !is androidx.credentials.exceptions.GetCredentialCancellationException) {
                                            Toast.makeText(context, "Google Sign-In failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(0.12f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.04f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            enabled = state !is AuthViewModel.UserState.Loading
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Colorful Styled 'G' icon matching our premium brand vibe
                                Text(
                                    text = "G",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF4285F4), // Blue
                                                Color(0xFF34A853), // Green
                                                Color(0xFFFBBC05), // Yellow
                                                Color(0xFFEA4335)  // Red
                                            )
                                        )
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continue with Google",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Skip Login
                    Text(
                        text = "Skip Login (Guest Mode)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable {
                                onAuthSuccess()
                            }
                            .padding(6.dp)
                    )
                }
            }
        }
    }
}
