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
import com.maaz.befit.ui.theme.*
import com.maaz.befit.viewmodel.HealthViewModel
import com.maaz.befit.viewmodel.ThemeViewModel
import com.maaz.befit.ui.utils.SoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackingScreen(
    viewModel: HealthViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val healthData by viewModel.todayHealthData.collectAsState()
    val userGoals by viewModel.userGoals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val soundEnabled by themeViewModel.soundEnabled.collectAsState()
    val context = LocalContext.current

    var showAddWaterDialog by remember { mutableStateOf(false) }
    var selectedAmount by remember { mutableStateOf(250) }
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
                text = "Water Tracking",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            // Water Intake Progress Card
            WaterProgressCard(
                currentIntake = healthData?.waterIntake ?: 0,
                goal = userGoals?.dailyWater ?: 2000,
                isLoading = isLoading
            )
        }

        item {
            // Quick Add Buttons
            Text(
                text = "Quick Add",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(getQuickWaterAmounts()) { amount ->
                    QuickWaterButton(
                        amount = amount,
                        onClick = {
                            viewModel.addWaterIntake(amount)
                            if (soundEnabled) soundManager.playWaterSound()
                        }
                    )
                }
            }
        }

        item {
            // Custom Add Button
            Button(
                onClick = { showAddWaterDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HealthBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Water")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Custom Amount")
            }
        }

        item {
            // Water Tips
            WaterTipsCard()
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

    // Add Water Dialog
    if (showAddWaterDialog) {
        AlertDialog(
            onDismissRequest = { showAddWaterDialog = false },
            title = { Text("Add Water Intake") },
            text = {
                Column {
                    Text("Select amount:")
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(getWaterAmountOptions()) { amount ->
                            FilterChip(
                                onClick = { selectedAmount = amount },
                                label = { Text("${amount}ml") },
                                selected = selectedAmount == amount
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addWaterIntake(selectedAmount)
                        if (soundEnabled) soundManager.playWaterSound()
                        showAddWaterDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWaterDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WaterProgressCard(
    currentIntake: Int,
    goal: Int,
    isLoading: Boolean
) {
    val progress = if (goal > 0) (currentIntake.toFloat() / goal).coerceAtMost(1f) else 0f
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthBlue.copy(alpha = 0.1f))
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
                        color = HealthBlue,
                        strokeWidth = 8.dp,
                        trackColor = HealthBlue.copy(alpha = 0.3f)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = HealthBlue
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
                    text = "${currentIntake}ml / ${goal}ml",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Water Intake Today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Motivational message
                Text(
                    text = when {
                        percentage >= 100 -> "🎉 Excellent! You've reached your goal!"
                        percentage >= 80 -> "Almost there! Just ${goal - currentIntake}ml to go!"
                        percentage >= 50 -> "Good progress! Keep hydrating!"
                        else -> "Stay hydrated! You've got this!"
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
fun QuickWaterButton(
    amount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = HealthBlue.copy(alpha = 0.1f)),
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
                Icons.Default.LocalDrink,
                contentDescription = "Water",
                tint = HealthBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${amount}ml",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WaterTipsCard() {
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
                    contentDescription = "Water Tips",
                    tint = HealthTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hydration Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthTeal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val tips = listOf(
                "Drink water first thing in the morning",
                "Keep a water bottle with you at all times",
                "Set hourly reminders to drink water",
                "Eat water-rich foods like fruits and vegetables",
                "Listen to your body's thirst signals"
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
fun getQuickWaterAmounts(): List<Int> {
    return listOf(100, 150, 200, 250, 300, 500)
}

fun getWaterAmountOptions(): List<Int> {
    return listOf(100, 150, 200, 250, 300, 400, 500, 750, 1000)
}