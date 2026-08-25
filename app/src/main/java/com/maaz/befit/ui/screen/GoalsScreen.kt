package com.maaz.befit.ui.screen

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maaz.befit.data.model.UserGoals
import com.maaz.befit.ui.components.HealthBackButtonTopAppBar
import com.maaz.befit.ui.theme.*
import com.maaz.befit.viewmodel.HealthViewModel

@Composable
fun GoalsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: HealthViewModel = hiltViewModel()
){
    val userGoals by viewModel.userGoals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showEditGoalsDialog by remember { mutableStateOf(false) }
    var tempGoals by remember { mutableStateOf(userGoals ?: UserGoals()) }

    LaunchedEffect(userGoals) {
        userGoals?.let { tempGoals = it }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar with Back Button
        HealthBackButtonTopAppBar(
            title = "Health Goals",
            onBackClick = onNavigateBack,
            healthColor = HealthGreen
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Current Goals Overview
                GoalsOverviewCard(
                    goals = userGoals ?: UserGoals(),
                    isLoading = isLoading
                )
            }

            item {
                // Edit Goals Button
                Button(
                    onClick = { showEditGoalsDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthOrange)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Goals")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Goals")
                }
            }

            item {
                // Goal Categories
                GoalCategoryCard(
                    title = "Daily Steps",
                    currentValue = userGoals?.dailySteps ?: 10000,
                    icon = Icons.Default.DirectionsWalk,
                    color = HealthGreen,
                    unit = "steps",
                    description = "Recommended: 10,000 steps per day for optimal health"
                )
            }
            item {
                GoalCategoryCard(
                    title = "Daily Water Intake",
                    currentValue = userGoals?.dailyWater ?: 2000,
                    icon = Icons.Default.LocalDrink,
                    color = HealthBlue,
                    unit = "ml",
                    description = "Recommended: 2-3 liters of water per day"
                )
            }

            item {
                GoalCategoryCard(
                    title = "Daily Sleep",
                    currentValue = (userGoals?.dailySleep ?: 8f).toInt(),
                    icon = Icons.Default.Bedtime,
                    color = HealthPurple,
                    unit = "hours",
                    description = "Recommended: 7-9 hours of quality sleep per night"
                )
            }

            item {
                GoalCategoryCard(
                    title = "Weekly Exercise",
                    currentValue = userGoals?.weeklyExercise ?: 150,
                    icon = Icons.Default.FitnessCenter,
                    color = HealthOrange,
                    unit = "minutes",
                    description = "Recommended: 150 minutes of moderate exercise per week"
                )
            }
            item {
                // Goal Tips
                GoalTipsCard()
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
        // Edit Goals Dialog
        if (showEditGoalsDialog) {
            EditGoalsDialog(
                goals = tempGoals,
                onGoalsChange = { tempGoals = it },
                onConfirm = {
                    viewModel.updateUserGoals(tempGoals)
                    showEditGoalsDialog = false
                },
                onDismiss = { showEditGoalsDialog = false }
            )
        }
    }
}

@Composable
fun GoalsOverviewCard(
    goals: UserGoals,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthTeal.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = "Goals",
                    tint = HealthTeal,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your Health Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HealthTeal
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Set personalized goals to track your health journey",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GoalCategoryCard(
    title: String,
    currentValue: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    unit: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
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
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "$currentValue $unit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EditGoalsDialog(
    goals: UserGoals,
    onGoalsChange: (UserGoals) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var dailySteps by remember { mutableStateOf(goals.dailySteps.toString()) }
    var dailyWater by remember { mutableStateOf(goals.dailyWater.toString()) }
    var dailySleep by remember { mutableStateOf(goals.dailySleep.toString()) }
    var weeklyExercise by remember { mutableStateOf(goals.weeklyExercise.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Health Goals") },
        text = {
            Column {
                // Daily Steps
                OutlinedTextField(
                    value = dailySteps,
                    onValueChange = { dailySteps = it },
                    label = { Text("Daily Steps") },
                    suffix = { Text("steps") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Daily Water
                OutlinedTextField(
                    value = dailyWater,
                    onValueChange = { dailyWater = it },
                    label = { Text("Daily Water Intake") },
                    suffix = { Text("ml") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Daily Sleep
                OutlinedTextField(
                    value = dailySleep,
                    onValueChange = { dailySleep = it },
                    label = { Text("Daily Sleep") },
                    suffix = { Text("hours") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Weekly Exercise
                OutlinedTextField(
                    value = weeklyExercise,
                    onValueChange = { weeklyExercise = it },
                    label = { Text("Weekly Exercise") },
                    suffix = { Text("minutes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newGoals = goals.copy(
                        dailySteps = dailySteps.toIntOrNull() ?: goals.dailySteps,
                        dailyWater = dailyWater.toIntOrNull() ?: goals.dailyWater,
                        dailySleep = dailySleep.toFloatOrNull() ?: goals.dailySleep,
                        weeklyExercise = weeklyExercise.toIntOrNull() ?: goals.weeklyExercise
                    )
                    onGoalsChange(newGoals)
                    onConfirm()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun GoalTipsCard() {
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
                    contentDescription = "Goal Tips",
                    tint = HealthTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Goal Setting Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthTeal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val tips = listOf(
                "Start with achievable goals and gradually increase them",
                "Make goals specific and measurable",
                "Set both short-term and long-term goals",
                "Track your progress regularly",
                "Celebrate small victories along the way",
                "Adjust goals based on your lifestyle and needs"
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

