package com.maaz.befit.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maaz.befit.data.model.HealthData
import com.maaz.befit.data.model.UserGoals
import com.maaz.befit.ui.theme.*
import com.maaz.befit.viewmodel.HealthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumHomeScreen(
    onNavigateToWater: () -> Unit = {},
    onNavigateToSteps: () -> Unit = {},
    onNavigateToSleep: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToConnectionStatus: () -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: HealthViewModel = hiltViewModel()
) {
    val healthData by viewModel.todayHealthData.collectAsState()
    val userGoals by viewModel.userGoals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showWelcomeAnimation by remember { mutableStateOf(true) }
    val scaleAnim = remember { Animatable(0.8f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(300)
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = EaseOutCubic)
        )
        showWelcomeAnimation = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        HealthBlue.copy(alpha = 0.05f),
                        Color.White
                    )
                )
            )
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Header Section
        item {
            PremiumHeader(
                onSignOut = onSignOut,
                scaleAnim = scaleAnim.value,
                alphaAnim = alphaAnim.value
            )
        }

        // Health Score Card
        item {
            PremiumHealthScoreCard(
                healthData = healthData,
                userGoals = userGoals,
                isLoading = isLoading,
                scaleAnim = scaleAnim.value,
                alphaAnim = alphaAnim.value
            )
        }

        // Quick Actions
        item {
            PremiumQuickActions(
                onNavigateToWater = onNavigateToWater,
                onNavigateToSteps = onNavigateToSteps,
                onNavigateToSleep = onNavigateToSleep,
                onNavigateToReports = onNavigateToReports,
                alphaAnim = alphaAnim.value
            )
        }

        // Today's Progress
        item {
            PremiumProgressSection(
                healthData = healthData,
                userGoals = userGoals,
                alphaAnim = alphaAnim.value
            )
        }

        // Health Insights
        item {
            PremiumInsightsCard(
                healthData = healthData,
                userGoals = userGoals,
                alphaAnim = alphaAnim.value
            )
        }

        // Device Connectivity
        item {
            PremiumDeviceConnectivityCard(
                viewModel = viewModel,
                onNavigateToConnectionStatus = onNavigateToConnectionStatus,
                alphaAnim = alphaAnim.value
            )
        }

        // Recent Activity
        item {
            PremiumRecentActivity(
                alphaAnim = alphaAnim.value
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PremiumHeader(
    onSignOut: () -> Unit,
    scaleAnim: Float,
    alphaAnim: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scaleAnim)
            .alpha(alphaAnim),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good Morning!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Let's make today healthy",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Notification Icon
            IconButton(
                onClick = { /* Handle notifications */ }
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = HealthTeal
                )
            }

            // Profile/Sign Out
            IconButton(onClick = onSignOut) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = HealthTeal
                )
            }
        }
    }
}

@Composable
private fun PremiumHealthScoreCard(
    healthData: HealthData?,
    userGoals: UserGoals?,
    isLoading: Boolean,
    scaleAnim: Float,
    alphaAnim: Float
) {
    val healthScore = calculateHealthScore(healthData, userGoals)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scaleAnim)
            .alpha(alphaAnim),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            HealthGreen.copy(alpha = 0.1f),
                            HealthTeal.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Today's Health Score",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isLoading) "Calculating..." else "$healthScore/100",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getHealthScoreMessage(healthScore),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // Circular Progress
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = healthScore / 100f,
                        modifier = Modifier.size(80.dp),
                        color = HealthGreen,
                        strokeWidth = 6.dp,
                        trackColor = HealthGreen.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "$healthScore",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumQuickActions(
    onNavigateToWater: () -> Unit,
    onNavigateToSteps: () -> Unit,
    onNavigateToSleep: () -> Unit,
    onNavigateToReports: () -> Unit,
    alphaAnim: Float
) {
    Column(
        modifier = Modifier.alpha(alphaAnim)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Actions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Additional Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Goals Button
                Button(
                    onClick = { /* Navigate to goals - can be added later */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HealthPurple.copy(alpha = 0.1f),
                        contentColor = HealthPurple
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        Icons.Default.Flag,
                        contentDescription = "Goals",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Goals", fontSize = 12.sp)
                }

                // Tips Button
                Button(
                    onClick = { /* Navigate to tips - can be added later */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HealthOrange.copy(alpha = 0.1f),
                        contentColor = HealthOrange
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = "Tips",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tips", fontSize = 12.sp)
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(getQuickActions(
                onNavigateToWater = onNavigateToWater,
                onNavigateToSteps = onNavigateToSteps,
                onNavigateToSleep = onNavigateToSleep,
                onNavigateToReports = onNavigateToReports
            )) { action ->
                PremiumActionCard(
                    title = action.title,
                    icon = action.icon,
                    color = action.color,
                    onClick = action.onClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(120.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = color.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PremiumProgressSection(
    healthData: HealthData?,
    userGoals: UserGoals?,
    alphaAnim: Float
) {
    Column(
        modifier = Modifier.alpha(alphaAnim)
    ) {
        Text(
            text = "Today's Progress",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PremiumProgressItem(
                title = "Steps",
                current = healthData?.steps ?: 0,
                goal = userGoals?.dailySteps ?: 10000,
                icon = Icons.Default.DirectionsWalk,
                color = HealthBlue
            )
            PremiumProgressItem(
                title = "Water",
                current = healthData?.waterIntake ?: 0,
                goal = userGoals?.dailyWater ?: 2000,
                icon = Icons.Default.LocalDrink,
                color = HealthTeal
            )
            PremiumProgressItem(
                title = "Sleep",
                current = (healthData?.sleepHours ?: 0f).toInt(),
                goal = (userGoals?.dailySleep ?: 8f).toInt(),
                icon = Icons.Default.Bedtime,
                color = HealthPurple
            )
        }
    }
}

@Composable
private fun PremiumProgressItem(
    title: String,
    current: Int,
    goal: Int,
    icon: ImageVector,
    color: Color
) {
    val progress = if (goal > 0) (current.toFloat() / goal.toFloat()).coerceAtMost(1f) else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = "$current / $goal",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun PremiumInsightsCard(
    healthData: HealthData?,
    userGoals: UserGoals?,
    alphaAnim: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HealthTeal.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = "Insights",
                    tint = HealthTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Health Insights",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = generatePremiumInsights(healthData, userGoals).firstOrNull() ?: "Keep up the great work! Your health journey is looking positive.",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PremiumRecentActivity(
    alphaAnim: Float
) {
    Column(
        modifier = Modifier.alpha(alphaAnim)
    ) {
        Text(
            text = "Recent Activity",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                PremiumActivityItem(
                    title = "Logged ${listOf("water", "steps", "sleep")[index]}",
                    time = "${index + 1}h ago",
                    icon = listOf(Icons.Default.LocalDrink, Icons.Default.DirectionsWalk, Icons.Default.Bedtime)[index],
                    color = listOf(HealthTeal, HealthBlue, HealthPurple)[index]
                )
            }
        }
    }
}

@Composable
private fun PremiumActivityItem(
    title: String,
    time: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = time,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

// Data classes and helper functions
data class PremiumQuickAction(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

private fun getQuickActions(
    onNavigateToWater: () -> Unit,
    onNavigateToSteps: () -> Unit,
    onNavigateToSleep: () -> Unit,
    onNavigateToReports: () -> Unit
): List<PremiumQuickAction> = listOf(
    PremiumQuickAction("Water", Icons.Default.LocalDrink, HealthTeal, onNavigateToWater),
    PremiumQuickAction("Steps", Icons.Default.DirectionsWalk, HealthBlue, onNavigateToSteps),
    PremiumQuickAction("Sleep", Icons.Default.Bedtime, HealthPurple, onNavigateToSleep),
    PremiumQuickAction("Reports", Icons.Default.Analytics, HealthGreen, onNavigateToReports)
)

private fun calculateHealthScore(healthData: HealthData?, userGoals: UserGoals?): Int {
    if (healthData == null || userGoals == null) return 0

    val stepsScore = ((healthData.steps.toFloat() / userGoals.dailySteps) * 30).coerceAtMost(30f)
    val waterScore = ((healthData.waterIntake.toFloat() / userGoals.dailyWater) * 30).coerceAtMost(30f)
    val sleepScore = ((healthData.sleepHours / userGoals.dailySleep) * 40).coerceAtMost(40f)

    return (stepsScore + waterScore + sleepScore).toInt()
}

private fun getHealthScoreMessage(score: Int): String = when {
    score >= 90 -> "Excellent! You're doing amazing!"
    score >= 75 -> "Great job! Keep it up!"
    score >= 60 -> "Good progress! You're on track!"
    score >= 40 -> "Not bad! Room for improvement"
    else -> "Let's work on building healthy habits!"
}

private fun generatePremiumInsights(healthData: HealthData?, userGoals: UserGoals?): List<String> {
    val insights = mutableListOf<String>()

    if (healthData != null && userGoals != null) {
        if (healthData.steps >= userGoals.dailySteps) {
            insights.add("🎉 Great job hitting your step goal today!")
        }
        if (healthData.waterIntake >= userGoals.dailyWater) {
            insights.add("💧 Excellent hydration! You're well on your way to optimal health.")
        }
        if (healthData.sleepHours >= userGoals.dailySleep) {
            insights.add("😴 Perfect sleep duration! Quality rest is key to recovery.")
        }
    }

    if (insights.isEmpty()) {
        insights.add("Keep up the great work! Your health journey is looking positive.")
    }

    return insights
}

@Composable
fun PremiumDeviceConnectivityCard(
    viewModel: HealthViewModel,
    onNavigateToConnectionStatus: () -> Unit,
    alphaAnim: Float
) {
    val connectedWearables by viewModel.connectedWearables.collectAsState()
    val isWearableSyncInProgress by viewModel.isWearableSyncInProgress.collectAsState()

    // Google Fit connection status
    var isGoogleFitConnected by remember { mutableStateOf(false) }
    var googleFitStatus by remember { mutableStateOf("Checking...") }

    val coroutineScope = rememberCoroutineScope()

    // Check Google Fit connection status
    LaunchedEffect(Unit) {
        isGoogleFitConnected = viewModel.isGoogleFitAvailable()
        googleFitStatus = if (isGoogleFitConnected) "Connected" else "Not Connected"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim),
        colors = CardDefaults.cardColors(containerColor = HealthBlue.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Devices,
                        contentDescription = "Device Connectivity",
                        tint = HealthBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Device Connectivity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = HealthBlue
                    )
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            isGoogleFitConnected = viewModel.isGoogleFitAvailable()
                            googleFitStatus = if (isGoogleFitConnected) "Connected" else "Not Connected"
                            viewModel.refreshWearableDevices()
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh Connections",
                        tint = HealthBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connection Status Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Google Fit Status
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = "Google Fit",
                        tint = if (isGoogleFitConnected) HealthGreen else HealthRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google Fit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = googleFitStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isGoogleFitConnected) HealthGreen else HealthRed,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Wearable Devices Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Watch,
                        contentDescription = "Wearable Devices",
                        tint = if (connectedWearables.isNotEmpty()) HealthGreen else HealthRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wearables",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = if (connectedWearables.isNotEmpty()) "${connectedWearables.size} Connected" else "None",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (connectedWearables.isNotEmpty()) HealthGreen else HealthRed,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.syncWearableData() },
                    enabled = !isWearableSyncInProgress,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isWearableSyncInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Sync Wearables",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.syncWithGoogleFit()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = "Sync Google Fit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Google Fit", fontSize = 12.sp)
                }

                Button(
                    onClick = onNavigateToConnectionStatus,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "View Details",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Details", fontSize = 12.sp)
                }
            }
        }
    }
}