package com.maaz.befit.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.maaz.befit.ui.theme.*
import kotlinx.coroutines.*

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.3f) }
    val rotationAnim = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        startAnimation = true

        // Parallel animations
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = EaseInOutCubic
                )
            )
        }

        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        launch {
            rotationAnim.animateTo(
                targetValue = 360f,
                animationSpec = tween(
                    durationMillis = 1500,
                    easing = EaseInOutCubic
                )
            )
        }

        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        HealthBlue.copy(alpha = 0.1f),
                        HealthTeal.copy(alpha = 0.05f),
                        Color.White
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value),
                contentAlignment = Alignment.Center
            ) {
                // Health icon with gradient background
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(HealthGreen, HealthTeal)
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_myplaces), // Using system icon as placeholder
                        contentDescription = "Health Tracker Logo",
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Name with Animation
            Text(
                text = "Smart Health Tracker",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = HealthBlue,
                modifier = Modifier.alpha(alphaAnim.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Your Personal Health Companion",
                fontSize = 16.sp,
                color = HealthTeal,
                modifier = Modifier.alpha(alphaAnim.value * 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading Indicator
            CircularProgressIndicator(
                modifier = Modifier
                    .size(32.dp)
                    .alpha(alphaAnim.value),
                color = HealthGreen,
                strokeWidth = 3.dp
            )
        }

        // Floating particles animation
        if (startAnimation) {
            repeat(5) { index ->
                FloatingParticle(
                    delay = index * 200,
                    alpha = alphaAnim.value
                )
            }
        }
    }
}

@Composable
private fun FloatingParticle(
    delay: Int,
    alpha: Float
){
    val infiniteTransition = rememberInfiniteTransition(label = "particle")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -100f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )

    Box(
        modifier = Modifier
            .offset(
                x = (100 + offsetX).dp,
                y = (400 + offsetY).dp
            )
            .size(8.dp)
            .background(
                color = HealthTeal.copy(alpha = alpha * 0.3f),
                shape = androidx.compose.foundation.shape.CircleShape
            )
    )
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    BeFitTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            SplashScreen(
                onSplashFinished = {}
            )
        }
    }
}
