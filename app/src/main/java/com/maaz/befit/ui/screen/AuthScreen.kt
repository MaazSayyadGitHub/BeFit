package com.maaz.befit.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.maaz.befit.data.dao.HealthDataDao
import com.maaz.befit.data.dao.SleepLogDao
import com.maaz.befit.data.dao.StepLogDao
import com.maaz.befit.data.dao.UserGoalsDao
import com.maaz.befit.data.dao.WaterLogDao
import com.maaz.befit.data.repository.HealthRepository
import com.maaz.befit.data.service.FirebaseDataService
import com.maaz.befit.ui.theme.BeFitTheme
import com.maaz.befit.ui.theme.HealthBlue
import com.maaz.befit.ui.theme.HealthGreen
import com.maaz.befit.ui.theme.HealthRed
import com.maaz.befit.ui.theme.HealthTeal
import com.maaz.befit.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,  // Callback when authentication succeeds
    viewModel: AuthViewModel = hiltViewModel()  // ViewModel for authentication logic
) {
    // ===== STATE MANAGEMENT =====
    // Collect state from ViewModel (reactive data)
    val user by viewModel.user.collectAsState()  // Current user state
    val isLoading by viewModel.isLoading.collectAsState()  // Loading indicator
    val errorMessage by viewModel.errorMessage.collectAsState()  // Error messages

    // ===== LOCAL UI STATE =====
    // Form input fields
    var isSignUp by remember { mutableStateOf(false) }  // Toggle between login/signup
    var email by remember { mutableStateOf("") }  // User email input
    var password by remember { mutableStateOf("") }  // User password input
    var confirmPassword by remember { mutableStateOf("") }  // Password confirmation for signup
    var passwordVisible by remember { mutableStateOf(false) }  // Show/hide password
    var confirmPasswordVisible by remember { mutableStateOf(false) }  // Show/hide confirm password

    // ===== ANIMATION STATES =====
    // Logo animation - scales down when loading
    val logoScale by animateFloatAsState(
        targetValue = if (isLoading) 0.9f else 1f,
        animationSpec = tween(300),
        label = "logoScale"
    )

    // Form animation - fades when loading
    val formAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0.7f else 1f,
        animationSpec = tween(300),
        label = "formAlpha"
    )

    // Navigate to main app when user is authenticated
    LaunchedEffect(user) {
        if (user != null) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        HealthBlue.copy(alpha = 0.15f),
                        HealthTeal.copy(alpha = 0.1f),
                        HealthGreen.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                )
            )
    ) {
        // Health-themed background elements
        HealthBackgroundElements()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo Section with health theme
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(800, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Health-themed logo with multiple icons
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(logoScale)
                            .clip(RoundedCornerShape(25.dp))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        HealthBlue.copy(alpha = 0.3f),
                                        HealthTeal.copy(alpha = 0.2f),
                                        HealthGreen.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Animated health icons
                        HealthLogoIcons()
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Smart Health Tracker",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = HealthBlue,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Track • Monitor • Achieve",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HealthTeal,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your journey to better health starts here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Auth Form with animations
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(800, delayMillis = 200, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(formAlpha),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Health-themed Auth Mode Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(25.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row {
                                    TextButton(
                                        onClick = {
                                            isSignUp = false
                                            viewModel.clearError()
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = if (!isSignUp) Color.White else HealthBlue
                                        ),
                                        modifier = Modifier
                                            .background(
                                                if (!isSignUp) HealthBlue else Color.Transparent,
                                                RoundedCornerShape(25.dp)
                                            )
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Login,
                                                contentDescription = "Sign In",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                "Sign In",
                                                fontWeight = if (!isSignUp) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            isSignUp = true
                                            viewModel.clearError()
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = if (isSignUp) Color.White else HealthBlue
                                        ),
                                        modifier = Modifier
                                            .background(
                                                if (isSignUp) HealthTeal else Color.Transparent,
                                                RoundedCornerShape(25.dp)
                                            )
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.PersonAdd,
                                                contentDescription = "Sign Up",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                "Sign Up",
                                                fontWeight = if (isSignUp) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Health-themed Email Field
                        HealthTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email Address",
                            icon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Health-themed Password Field
                        HealthTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Password",
                            icon = Icons.Default.Lock,
                            keyboardType = KeyboardType.Password,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Confirm Password Field (animated)
                        AnimatedVisibility(
                            visible = isSignUp,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            HealthTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = "Confirm Password",
                                icon = Icons.Default.Lock,
                                keyboardType = KeyboardType.Password,
                                isPassword = true,
                                passwordVisible = confirmPasswordVisible,
                                onPasswordVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Error Message (animated)
                        AnimatedVisibility(
                            visible = errorMessage != null,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HealthRed.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = HealthRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = errorMessage!!,
                                        color = HealthRed,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        // Health-themed Auth Button
                        HealthAuthButton(
                            onClick = {
                                if (isSignUp) {
                                    if (password == confirmPassword) {
                                        viewModel.signUp(email, password)
                                    } else {
                                        // Handle password mismatch
                                    }
                                } else {
                                    viewModel.signIn(email, password)
                                }
                            },
                            isLoading = isLoading,
                            isSignUp = isSignUp,
                            enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && (!isSignUp || confirmPassword.isNotBlank()),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * HEALTH BACKGROUND ELEMENTS
 *
 * Creates floating health-themed icons in the background for visual appeal
 */
@Composable
private fun HealthBackgroundElements() {
    // Floating health icons
    val healthIcons = listOf(
        Icons.Default.Favorite to HealthRed.copy(alpha = 0.1f),
        Icons.Default.LocalDrink to HealthBlue.copy(alpha = 0.1f),
        Icons.Default.DirectionsWalk to HealthGreen.copy(alpha = 0.1f),
        Icons.Default.Bedtime to HealthTeal.copy(alpha = 0.1f)
    )

    healthIcons.forEachIndexed { index, (icon, color) ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = (100 + index * 150).dp,
                    start = (50 + index * 80).dp
                )
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = color
            )
        }
    }
}

/**
 * HEALTH LOGO ICONS
 *
 * Animated health icons in the logo area
 */
@Composable
private fun HealthLogoIcons() {
    val infiniteTransition = rememberInfiniteTransition(label = "logoAnimation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box {
        // Main heart icon
        Icon(
            Icons.Default.Favorite,
            contentDescription = "Health Tracker",
            modifier = Modifier.size(50.dp),
            tint = HealthRed
        )

        // Rotating health icons around the main icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer {
                    rotationZ = rotation
                }
        ) {
            // Water drop
            Icon(
                Icons.Default.LocalDrink,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = 30.dp, y = 0.dp),
                tint = HealthBlue
            )

            // Steps
            Icon(
                Icons.Default.DirectionsWalk,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = -30.dp, y = 0.dp),
                tint = HealthGreen
            )

            // Sleep
            Icon(
                Icons.Default.Bedtime,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = 0.dp, y = 30.dp),
                tint = HealthTeal
            )
        }
    }
}

/**
 * HEALTH TEXT FIELD
 *
 * Custom text field with health-themed styling
 */
@Composable
private fun HealthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = label,
                tint = HealthBlue
            )
        },
        trailingIcon = if (isPassword && onPasswordVisibilityToggle != null) {
            {
                IconButton(onClick = onPasswordVisibilityToggle) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = HealthBlue
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HealthBlue,
            focusedLabelColor = HealthBlue,
            unfocusedBorderColor = HealthBlue.copy(alpha = 0.5f)
        )
    )
}

/**
 * HEALTH AUTH BUTTON
 *
 * Custom authentication button with health-themed styling
 */
@Composable
private fun HealthAuthButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    isSignUp: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSignUp) HealthTeal else HealthBlue,
            disabledContainerColor = (if (isSignUp) HealthTeal else HealthBlue).copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (isSignUp) Icons.Default.PersonAdd else Icons.Default.Login,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isSignUp) "Create Account" else "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun AuthScreenPreview() {
//
//
//    BeFitTheme {
//        Box(
//            modifier = Modifier.fillMaxSize()
//                .padding(16.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            AuthScreen(
//                onAuthSuccess = {},
////                AuthViewModel(firebaseAuth = null, healthRepository = null)
//            )
//        }
//
//    }
//}
