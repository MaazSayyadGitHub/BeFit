package com.maaz.befit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maaz.befit.data.model.HealthData
import com.maaz.befit.ui.theme.*
import com.maaz.befit.viewmodel.HealthViewModel
import com.maaz.befit.ui.utils.StatItem
import com.maaz.befit.ui.utils.formatNumber
import com.maaz.befit.ui.utils.SoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepTrackingScreen(
    viewModel: HealthViewModel = hiltViewModel()
) {
    val healthData by viewModel.todayHealthData.collectAsState()
    val userGoals by viewModel.userGoals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    var showAddStepsDialog by remember { mutableStateOf(false) }
    var selectedSteps by remember { mutableStateOf(1000) }
    val soundManager = remember { SoundManager(context) }

    // Data is now automatically loaded via reactive flow

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Text(
                text = "Step Tracking",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            // Steps Progress Card
            StepsProgressCard(
                currentSteps = healthData?.steps ?: 0,
                goal = userGoals?.dailySteps ?: 10000,
                isLoading = isLoading
            )
        }

        item {
            // Quick Add Buttons
            Text(
                text = "Quick Add Steps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(getQuickStepAmounts()) { steps ->
                    QuickStepButton(
                        steps = steps,
                        onClick = {
                            viewModel.addSteps(steps)
                            soundManager.playStepSound()
                        }
                    )
                }
            }
        }

        item {
            // Custom Add Button
            Button(
                onClick = { showAddStepsDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Steps")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Custom Steps")
            }
        }

        item {
            // Activity Stats
            ActivityStatsCard(healthData = healthData)
        }

        item {
            // Walking Tips
            WalkingTipsCard()
        }

        if (errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HealthRed.copy(alpha = 0.1f))
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
                            tint = HealthRed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = HealthRed,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            }
        }
    }

    // Add Steps Dialog
    if (showAddStepsDialog) {
        AlertDialog(
            onDismissRequest = { showAddStepsDialog = false },
            title = { Text("Add Steps") },
            text = {
                Column {
                    Text("Select number of steps:")
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(getStepAmountOptions()) { steps ->
                            FilterChip(
                                onClick = { selectedSteps = steps },
                                label = { Text("${steps}") },
                                selected = selectedSteps == steps
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addSteps(selectedSteps)
                        soundManager.playStepSound()
                        showAddStepsDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStepsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StepsProgressCard(
    currentSteps: Int,
    goal: Int,
    isLoading: Boolean
) {
    val progress = if (goal > 0) (currentSteps.toFloat() / goal).coerceAtMost(1f) else 0f
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthGreen.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                // Circular Progress Indicator
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.size(120.dp),
                        color = HealthGreen,
                        strokeWidth = 8.dp,
                        trackColor = HealthGreen.copy(alpha = 0.3f)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = HealthGreen
                        )
                        Text(
                            text = "Goal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${currentSteps.formatNumber()} / ${goal.formatNumber()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Steps Today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Motivational message
                Text(
                    text = when {
                        percentage >= 100 -> "🎉 Amazing! You've reached your goal!"
                        percentage >= 80 -> "Almost there! Just ${goal - currentSteps} more steps!"
                        percentage >= 50 -> "Great progress! Keep moving!"
                        else -> "Let's get moving! Every step counts!"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickStepButton(
    steps: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = HealthGreen.copy(alpha = 0.1f)),
        shape = CircleShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.DirectionsWalk,
                contentDescription = "Steps",
                tint = HealthGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${steps}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActivityStatsCard(
    healthData: HealthData?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthOrange.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = "Activity Stats",
                    tint = HealthOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Activity Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthOrange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Distance",
                    value = "${String.format("%.1f", (healthData?.steps ?: 0) * 0.0008)} km",
                    icon = Icons.Default.Straighten,
                    color = HealthOrange
                )
                StatItem(
                    label = "Calories",
                    value = "${(healthData?.steps ?: 0) * 0.04}",
                    icon = Icons.Default.LocalFireDepartment,
                    color = HealthOrange
                )
            }
        }
    }
}

@Composable
fun WalkingTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthTeal.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = "Walking Tips",
                    tint = HealthTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Walking Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthTeal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val tips = listOf(
                "Take the stairs instead of elevators",
                "Park farther away from your destination",
                "Take walking breaks during work",
                "Walk while talking on the phone",
                "Use a pedometer or fitness tracker",
                "Set hourly movement reminders"
            )

            tips.forEach { tip ->
                Text(
                    text = "• $tip",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

// Helper functions
fun getQuickStepAmounts(): List<Int> {
    return listOf(500, 1000, 1500, 2000, 2500, 3000)
}

fun getStepAmountOptions(): List<Int> {
    return listOf(500, 1000, 1500, 2000, 2500, 3000, 4000, 5000, 7500, 10000)
}