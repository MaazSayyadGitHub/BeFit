package com.maaz.befit.ui.screen

import androidx.compose.animation.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.maaz.befit.data.model.HealthData
import com.maaz.befit.data.model.UserGoals
import com.maaz.befit.ui.theme.*
import com.maaz.befit.viewmodel.HealthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun ModernDashboardScreen(
    onNavigateToWater: () -> Unit = {},
    onNavigateToSteps: () -> Unit = {},
    onNavigateToSleep: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToConnectionStatus: () -> Unit = {},
    onNavigateToRunningMap: () -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: HealthViewModel = hiltViewModel()
) {
    val healthData by viewModel.todayHealthData.collectAsState()
    val userGoals by viewModel.userGoals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val connectedWearables by viewModel.connectedWearables.collectAsState()
    val isWearableSyncInProgress by viewModel.isWearableSyncInProgress.collectAsState()

    // Connection status
    var isGoogleFitConnected by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Animation states
    var showWelcomeAnimation by remember { mutableStateOf(true) }
    val scaleAnim = remember { Animatable(0.8f) }
    val alphaAnim = remember { Animatable(0f) }

    // Check Google Fit connection
    LaunchedEffect(Unit) {
        isGoogleFitConnected = viewModel.isGoogleFitAvailable()
    }

    // Welcome animation
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
                        HealthBlue.copy(alpha = 0.03f),
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

        // Header with greeting and date
        item {
            ModernHeader(
                onSignOut = onSignOut,
                scaleAnim = scaleAnim.value,
                alphaAnim = alphaAnim.value
            )
        }

        // Health Score Card
        item {
            ModernHealthScoreCard(
                healthData = healthData,
                userGoals = userGoals,
                isLoading = isLoading,
                scaleAnim = scaleAnim.value,
                alphaAnim = alphaAnim.value
            )
        }

        // Quick Actions Grid
        item {
            ModernQuickActions(
                onNavigateToWater = onNavigateToWater,
                onNavigateToSteps = onNavigateToSteps,
                onNavigateToSleep = onNavigateToSleep,
                onNavigateToReports = onNavigateToReports,
                onNavigateToRunningMap = onNavigateToRunningMap,
                alphaAnim = alphaAnim.value
            )
        }

        // Today's Progress
        item {
            ModernProgressSection(
                healthData = healthData,
                userGoals = userGoals,
                alphaAnim = alphaAnim.value
            )
        }

        // Connection Status
        item {
            ModernConnectionStatusCard(
                isGoogleFitConnected = isGoogleFitConnected,
                connectedWearables = connectedWearables,
                isWearableSyncInProgress = isWearableSyncInProgress,
                onNavigateToConnectionStatus = onNavigateToConnectionStatus,
                onSyncGoogleFit = {
                    coroutineScope.launch {
                        viewModel.syncWithGoogleFit()
                    }
                },
                onSyncWearables = { viewModel.syncWearableData() },
                alphaAnim = alphaAnim.value
            )
        }

        // Health Insights
        item {
            ModernInsightsCard(
                healthData = healthData,
                userGoals = userGoals,
                alphaAnim = alphaAnim.value
            )
        }

        // Error Message
        if (errorMessage != null) {
            item {
                ModernErrorCard(
                    message = errorMessage!!,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ModernHeader(
    onSignOut: () -> Unit,
    scaleAnim: Float,
    alphaAnim: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim)
            .scale(scaleAnim),
        colors = CardDefaults.cardColors(
            containerColor = HealthBlue.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = getGreeting(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = HealthBlue
                )
                Text(
                    text = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onSignOut,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = HealthBlue.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = HealthBlue
                )
            }
        }
    }
}

@Composable
private fun ModernHealthScoreCard(
    healthData: HealthData?,
    userGoals: UserGoals?,
    isLoading: Boolean,
    scaleAnim: Float,
    alphaAnim: Float
) {
    val healthScore = healthData?.healthScore ?: 0
    val scoreColor = when {
        healthScore >= 80 -> HealthGreen
        healthScore >= 60 -> HealthOrange
        else -> HealthRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim)
            .scale(scaleAnim),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Today's Health Score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Circular Progress Indicator
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = healthScore / 100f,
                    modifier = Modifier.size(120.dp),
                    strokeWidth = 8.dp,
                    color = scoreColor,
                    trackColor = scoreColor.copy(alpha = 0.2f)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = healthScore.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                    Text(
                        text = "/ 100",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = getHealthScoreMessage(healthScore),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModernQuickActions(
    onNavigateToWater: () -> Unit,
    onNavigateToSteps: () -> Unit,
    onNavigateToSleep: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToRunningMap: () -> Unit,
    alphaAnim: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(getQuickActions(onNavigateToWater, onNavigateToSteps, onNavigateToSleep, onNavigateToReports, onNavigateToRunningMap)) { action ->
                    QuickActionButton(
                        title = action.title,
                        icon = action.icon,
                        color = action.color,
                        onClick = action.onClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernProgressSection(
    healthData: HealthData?,
    userGoals: UserGoals?,
    alphaAnim: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Today's Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            val progressItems = getProgressItems(healthData, userGoals)
            progressItems.forEach { item ->
                ProgressItem(
                    title = item.title,
                    current = item.current,
                    target = item.target,
                    unit = item.unit,
                    progress = item.progress,
                    color = item.color,
                    icon = item.icon
                )
                if (item != progressItems.last()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ModernConnectionStatusCard(
    isGoogleFitConnected: Boolean,
    connectedWearables: List<String>,
    isWearableSyncInProgress: Boolean,
    onNavigateToConnectionStatus: () -> Unit,
    onSyncGoogleFit: () -> Unit,
    onSyncWearables: () -> Unit,
    alphaAnim: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Device Connections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = onNavigateToConnectionStatus) {
                    Text("View All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Fit Status
            ConnectionStatusItem(
                title = "Google Fit",
                isConnected = isGoogleFitConnected,
                onSync = onSyncGoogleFit,
                icon = Icons.Default.FitnessCenter,
                color = HealthBlue
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Wearable Devices Status
            ConnectionStatusItem(
                title = "Wearable Devices",
                isConnected = connectedWearables.isNotEmpty(),
                onSync = onSyncWearables,
                isLoading = isWearableSyncInProgress,
                icon = Icons.Default.Watch,
                color = HealthPurple,
                subtitle = if (connectedWearables.isNotEmpty()) "${connectedWearables.size} device(s)" else "No devices"
            )
        }
    }
}

@Composable
private fun ModernInsightsCard(
    healthData: HealthData?,
    userGoals: UserGoals?,
    alphaAnim: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = HealthOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Health Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val insights = getHealthInsights(healthData, userGoals)
            insights.forEach { insight ->
                InsightItem(
                    text = insight,
                    color = HealthOrange
                )
                if (insight != insights.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ModernErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

// Helper composables
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.size(100.dp, 80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProgressItem(
    title: String,
    current: Int,
    target: Int,
    unit: String,
    progress: Float,
    color: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$current / $target $unit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun ConnectionStatusItem(
    title: String,
    isConnected: Boolean,
    onSync: () -> Unit,
    isLoading: Boolean = false,
    icon: ImageVector,
    color: Color,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isConnected) color else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isConnected) {
            IconButton(
                onClick = onSync,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = color
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = color
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightItem(
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(6.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Helper functions
private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        in 18..21 -> "Good Evening"
        else -> "Good Night"
    }
}

private fun getHealthScoreMessage(score: Int): String {
    return when {
        score >= 90 -> "Excellent! You're doing amazing!"
        score >= 80 -> "Great job! Keep up the good work!"
        score >= 70 -> "Good progress! You're on the right track."
        score >= 60 -> "Not bad! There's room for improvement."
        else -> "Let's work together to improve your health!"
    }
}

private data class ModernQuickAction(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

private fun getQuickActions(
    onNavigateToWater: () -> Unit,
    onNavigateToSteps: () -> Unit,
    onNavigateToSleep: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToRunningMap: () -> Unit
): List<ModernQuickAction> {
    return listOf(
        ModernQuickAction("Water", Icons.Default.WaterDrop, HealthBlue, onNavigateToWater),
        ModernQuickAction("Steps", Icons.Default.DirectionsWalk, HealthGreen, onNavigateToSteps),
        ModernQuickAction("Sleep", Icons.Default.Bedtime, HealthPurple, onNavigateToSleep),
        ModernQuickAction("Running", Icons.Default.DirectionsRun, HealthRed, onNavigateToRunningMap),
        ModernQuickAction("Reports", Icons.Default.Assessment, HealthOrange, onNavigateToReports)
    )
}

private data class ProgressItemData(
    val title: String,
    val current: Int,
    val target: Int,
    val unit: String,
    val progress: Float,
    val color: Color,
    val icon: ImageVector
)

private fun getProgressItems(healthData: HealthData?, userGoals: UserGoals?): List<ProgressItemData> {
    val steps = healthData?.steps ?: 0
    val water = healthData?.waterIntake ?: 0
    val sleep = healthData?.sleepHours ?: 0f

    val stepGoal = userGoals?.dailySteps ?: 10000
    val waterGoal = userGoals?.dailyWater ?: 8
    val sleepGoal = userGoals?.dailySleep ?: 8f

    return listOf(
        ProgressItemData(
            "Steps",
            steps,
            stepGoal,
            "steps",
            (steps.toFloat() / stepGoal).coerceAtMost(1f),
            HealthGreen,
            Icons.Default.DirectionsWalk
        ),
        ProgressItemData(
            "Water",
            water,
            waterGoal,
            "glasses",
            (water.toFloat() / waterGoal).coerceAtMost(1f),
            HealthBlue,
            Icons.Default.WaterDrop
        ),
        ProgressItemData(
            "Sleep",
            sleep.toInt(),
            sleepGoal.toInt(),
            "hours",
            (sleep / sleepGoal).coerceAtMost(1f),
            HealthPurple,
            Icons.Default.Bedtime
        )
    )
}

private fun getHealthInsights(healthData: HealthData?, userGoals: UserGoals?): List<String> {
    val insights = mutableListOf<String>()

    healthData?.let { data ->
        userGoals?.let { goals ->
            when {
                data.steps >= goals.dailySteps -> insights.add("🎉 You've reached your daily step goal!")
                data.steps >= goals.dailySteps * 0.8 -> insights.add("💪 You're close to your step goal!")

                data.waterIntake >= goals.dailyWater -> insights.add("💧 Great job staying hydrated!")
                data.waterIntake >= goals.dailyWater * 0.8 -> insights.add("🥤 Almost at your water goal!")

                data.sleepHours >= goals.dailySleep -> insights.add("😴 Perfect sleep duration!")
                data.sleepHours >= goals.dailySleep * 0.8 -> insights.add("🌙 Good sleep, but could be better!")

                else -> insights.add("📈 Keep tracking your health metrics daily!")
            }
        }
    }

    if (insights.isEmpty()) {
        insights.add("📱 Start tracking your health metrics to get personalized insights!")
    }

    return insights
}



