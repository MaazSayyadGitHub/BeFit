package com.maaz.befit.ui.screen

import androidx.compose.runtime.Composable

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.maaz.befit.data.service.GoogleFitService
import com.maaz.befit.ui.components.HealthBackButtonTopAppBar
import com.maaz.befit.ui.theme.*
import com.maaz.befit.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleFitSetupScreen(
    onNavigateBack: () -> Unit = {},
    onSetupComplete: () -> Unit = {},
    viewModel: HealthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val googleFitService = remember { GoogleFitService(context) }
    val coroutineScope = rememberCoroutineScope()

    var isGoogleSignedIn by remember { mutableStateOf(false) }
    var hasPermissions by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var setupStep by remember { mutableStateOf(1) }

    // Animation states
    val alphaAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800),
        label = "alpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Check initial status
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            isGoogleSignedIn = !googleFitService.needsGoogleSignIn()
            hasPermissions = googleFitService.isGoogleFitAvailable()

            if (isGoogleSignedIn && hasPermissions) {
                setupStep = 3 // Complete
            } else if (isGoogleSignedIn) {
                setupStep = 2 // Need permissions
            }
        } catch (e: Exception) {
            errorMessage = "Error checking Google Fit status: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            isLoading = true
            try {
                // Handle sign-in result with improved error handling
                val signInSuccessful = googleFitService.handleSignInResult(result.data)
                if (signInSuccessful) {
                    isGoogleSignedIn = true
                    setupStep = 2
                    errorMessage = null
                } else {
                    errorMessage = "Google Sign-In failed. Please check your internet connection and try again."
                }
            } catch (e: Exception) {
                errorMessage = "Error during Google Sign-In: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Google Fit permissions launcher
    val googleFitPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            isLoading = true
            try {
                hasPermissions = googleFitService.isGoogleFitAvailable()
                if (hasPermissions) {
                    setupStep = 3
                    errorMessage = null
                    // Sync data after successful setup
                    viewModel.syncWithGoogleFit()
                } else {
                    errorMessage = "Google Fit permissions not granted. Please try again."
                }
            } catch (e: Exception) {
                errorMessage = "Error requesting Google Fit permissions: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        HealthBlue.copy(alpha = 0.05f),
                        Color.White
                    )
                )
            )
    ) {
        HealthBackButtonTopAppBar(
            title = "Google Fit Setup",
            onBackClick = onNavigateBack,
            healthColor = HealthBlue
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alphaAnim)
                        .scale(scaleAnim),
                    colors = CardDefaults.cardColors(
                        containerColor = HealthBlue.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = HealthBlue
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Connect Google Fit",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = HealthBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sync your health data automatically with Google Fit for a complete health tracking experience.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Benefits
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Benefits of Google Fit Integration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HealthBlue
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val benefits = listOf(
                            "🔄 Automatic data sync from your devices",
                            "📊 Comprehensive health insights",
                            "🎯 Better goal tracking and progress",
                            "📱 Works with all your fitness apps",
                            "☁️ Cloud backup of your health data"
                        )

                        benefits.forEach { benefit ->
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Setup Steps
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Setup Steps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HealthBlue
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Step 1: Google Sign-In
                        SetupStep(
                            stepNumber = 1,
                            title = "Sign in to Google",
                            description = "Connect your Google account to access Google Fit",
                            isCompleted = isGoogleSignedIn,
                            isCurrent = setupStep == 1,
                            isLoading = isLoading && setupStep == 1,
                            onAction = {
                                if (!isGoogleSignedIn) {
                                    googleSignInLauncher.launch(googleFitService.getGoogleSignInIntent())
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Step 2: Permissions
                        SetupStep(
                            stepNumber = 2,
                            title = "Grant Health Permissions",
                            description = "Allow access to your health and fitness data",
                            isCompleted = hasPermissions,
                            isCurrent = setupStep == 2,
                            isLoading = isLoading && setupStep == 2,
                            onAction = {
                                if (isGoogleSignedIn && !hasPermissions) {
                                    googleFitPermissionsLauncher.launch(googleFitService.getGoogleFitPermissionsIntent())
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Step 3: Complete
                        SetupStep(
                            stepNumber = 3,
                            title = "Setup Complete",
                            description = "Your Google Fit integration is ready",
                            isCompleted = setupStep == 3,
                            isCurrent = setupStep == 3,
                            isLoading = false,
                            onAction = {
                                if (setupStep == 3) {
                                    onSetupComplete()
                                }
                            }
                        )
                    }
                }
            }

            // Error Message
            if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (setupStep == 3) {
                        Button(
                            onClick = onSetupComplete,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HealthGreen
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continue to Dashboard",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = HealthBlue
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Skip for Now",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SetupStep(
    stepNumber: Int,
    title: String,
    description: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLoading: Boolean,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step indicator
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = when {
                        isCompleted -> HealthGreen
                        isCurrent -> HealthBlue
                        else -> MaterialTheme.colorScheme.outline
                    },
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Step content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCurrent) HealthBlue else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Action button
        if (isCurrent && !isCompleted && !isLoading) {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HealthBlue
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Start",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}