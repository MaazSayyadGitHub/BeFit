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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.maaz.befit.ui.utils.SoundManager
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTrackingScreen(
    viewModel: HealthViewModel = hiltViewModel()
) {
    val healthData by viewModel.todayHealthData.collectAsState()
    val userGoals by viewModel.userGoals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    var showSleepLogDialog by remember { mutableStateOf(false) }
    var sleepStart by remember { mutableStateOf("22:00") }
    var sleepEnd by remember { mutableStateOf("06:00") }
    var sleepQuality by remember { mutableStateOf(3) }
    val soundManager = remember { SoundManager(context) }

    // Meditation state
    var isMeditationPlaying by remember { mutableStateOf(false) }
    var selectedMeditation by remember { mutableStateOf("") }
    var meditationTimer by remember { mutableStateOf(0) }

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
                text = "Sleep Tracking",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            // Sleep Progress Card
            SleepProgressCard(
                currentSleep = healthData?.sleepHours ?: 0f,
                goal = userGoals?.dailySleep ?: 8f,
                isLoading = isLoading
            )
        }

        item {
            // Log Sleep Button
            Button(
                onClick = { showSleepLogDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HealthPurple)
            ) {
                Icon(Icons.Default.Bedtime, contentDescription = "Log Sleep")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Sleep Session")
            }
        }

        item {
            // Sleep Quality Tips
            SleepQualityTipsCard()
        }

        item {
            // Sleep Tips
            SleepTipsCard()
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

        // ===== MEDITATION & SLEEP SOUNDS SECTION =====
        // This section provides calming sounds to help users relax and fall asleep
        item {
            MeditationSection(
                isPlaying = isMeditationPlaying,  // Whether any meditation sound is currently playing
                selectedMeditation = selectedMeditation,  // Which meditation sound is selected
                timer = meditationTimer,  // How long the sound has been playing
                onPlayPause = { meditation ->
                    // Handle play/pause logic for meditation sounds
                    if (isMeditationPlaying && selectedMeditation == meditation) {
                        // If same meditation is playing, stop it
                        isMeditationPlaying = false
                        selectedMeditation = ""
                        meditationTimer = 0
                    } else {
                        // Start new meditation sound
                        isMeditationPlaying = true
                        selectedMeditation = meditation
                        meditationTimer = 0
                        // Play the corresponding meditation sound
                        when (meditation) {
                            "Rain" -> soundManager.playRainSound()  // Gentle rain sounds
                            "Ocean" -> soundManager.playOceanSound()  // Ocean wave sounds
                            "Forest" -> soundManager.playForestSound()  // Forest ambience
                            "White Noise" -> soundManager.playWhiteNoiseSound()  // Calming white noise
                        }
                    }
                }
            )
        }
    }

    // Sleep Log Dialog
    if (showSleepLogDialog) {
        SleepLogDialog(
            sleepStart = sleepStart,
            sleepEnd = sleepEnd,
            sleepQuality = sleepQuality,
            onSleepStartChange = { sleepStart = it },
            onSleepEndChange = { sleepEnd = it },
            onSleepQualityChange = { sleepQuality = it },
            onConfirm = {
                val duration = calculateSleepDuration(sleepStart, sleepEnd)
                val timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                viewModel.addSleepLog(sleepStart, sleepEnd, duration, sleepQuality)
                soundManager.playSleepSound()
                showSleepLogDialog = false
            },
            onDismiss = { showSleepLogDialog = false }
        )
    }
}

@Composable
fun SleepProgressCard(
    currentSleep: Float,
    goal: Float,
    isLoading: Boolean
) {
    val progress = if (goal > 0) (currentSleep / goal).coerceAtMost(1f) else 0f
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthPurple.copy(alpha = 0.1f))
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
                        color = HealthPurple,
                        strokeWidth = 8.dp,
                        trackColor = HealthPurple.copy(alpha = 0.3f)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = HealthPurple
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
                    text = "${String.format("%.1f", currentSleep)}h / ${String.format("%.1f", goal)}h",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Sleep Duration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Motivational message
                Text(
                    text = when {
                        percentage >= 100 -> "😴 Perfect! You've reached your sleep goal!"
                        percentage >= 80 -> "Almost there! Just ${String.format("%.1f", goal - currentSleep)}h more!"
                        percentage >= 50 -> "Good progress! Keep prioritizing sleep!"
                        else -> "Quality sleep is essential for health!"
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
fun SleepLogDialog(
    sleepStart: String,
    sleepEnd: String,
    sleepQuality: Int,
    onSleepStartChange: (String) -> Unit,
    onSleepEndChange: (String) -> Unit,
    onSleepQualityChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Sleep Session") },
        text = {
            Column {
                // Sleep Start Time
                Text("Sleep Start Time:")
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(getSleepTimes()) { time ->
                        Button(
                            onClick = { onSleepStartChange(time) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sleepStart == time)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(time)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sleep End Time
                Text("Wake Up Time:")
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(getWakeUpTimes()) { time ->
                        Button(
                            onClick = { onSleepEndChange(time) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sleepEnd == time)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(time)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sleep Quality
                Text("Sleep Quality:")
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf(1, 2, 3, 4, 5)) { quality ->
                        Button(
                            onClick = { onSleepQualityChange(quality) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sleepQuality == quality)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("$quality ⭐")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Log Sleep")
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
fun SleepQualityTipsCard() {
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
                    Icons.Default.Star,
                    contentDescription = "Sleep Quality",
                    tint = HealthTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sleep Quality Guide",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthTeal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val qualityGuide = listOf(
                "1 ⭐ - Very poor sleep, frequent awakenings",
                "2 ⭐ - Poor sleep, some restlessness",
                "3 ⭐ - Average sleep, occasional disturbances",
                "4 ⭐ - Good sleep, mostly restful",
                "5 ⭐ - Excellent sleep, deep and restful"
            )

            qualityGuide.forEach { guide ->
                Text(
                    text = guide,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SleepTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthBlue.copy(alpha = 0.1f))
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
                    contentDescription = "Sleep Tips",
                    tint = HealthBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sleep Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthBlue
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val tips = listOf(
                "Maintain a consistent sleep schedule",
                "Create a relaxing bedtime routine",
                "Keep your bedroom cool and dark",
                "Avoid screens 1 hour before bed",
                "Limit caffeine in the afternoon",
                "Exercise regularly but not before bed"
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
fun getSleepTimes(): List<String> {
    return listOf("20:00", "20:30", "21:00", "21:30", "22:00", "22:30", "23:00", "23:30", "00:00")
}

fun getWakeUpTimes(): List<String> {
    return listOf("05:00", "05:30", "06:00", "06:30", "07:00", "07:30", "08:00", "08:30", "09:00")
}

fun calculateSleepDuration(startTime: String, endTime: String): Float {
    val start = startTime.split(":")
    val end = endTime.split(":")

    val startMinutes = start[0].toInt() * 60 + start[1].toInt()
    val endMinutes = end[0].toInt() * 60 + end[1].toInt()

    val durationMinutes = if (endMinutes > startMinutes) {
        endMinutes - startMinutes
    } else {
        (24 * 60) - startMinutes + endMinutes
    }

    return durationMinutes / 60f
}

@Composable
fun MeditationSection(
    isPlaying: Boolean,
    selectedMeditation: String,
    timer: Int,
    onPlayPause: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthPurple.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Spa,
                    contentDescription = "Meditation",
                    tint = HealthPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Meditation & Sleep Sounds",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthPurple
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Choose a calming sound to help you relax and fall asleep",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Meditation options
            val meditationOptions = listOf(
                MeditationOption("Rain", "Gentle rain sounds", Icons.Default.WaterDrop),
                MeditationOption("Ocean", "Ocean waves", Icons.Default.Waves),
                MeditationOption("Forest", "Forest ambience", Icons.Default.Park),
                MeditationOption("White Noise", "Calming white noise", Icons.Default.GraphicEq)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(meditationOptions) { option ->
                    MeditationCard(
                        option = option,
                        isSelected = selectedMeditation == option.name,
                        isPlaying = isPlaying && selectedMeditation == option.name,
                        onClick = { onPlayPause(option.name) }
                    )
                }
            }

            // Timer display
            if (isPlaying) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = HealthPurple.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Now Playing",
                                style = MaterialTheme.typography.bodySmall,
                                color = HealthPurple
                            )
                            Text(
                                text = selectedMeditation,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = HealthPurple
                            )
                        }

                        Text(
                            text = formatTime(timer),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = HealthPurple
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationCard(
    option: MeditationOption,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HealthPurple.copy(alpha = 0.2f) else HealthPurple.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                option.icon,
                contentDescription = option.name,
                tint = if (isSelected) HealthPurple else HealthPurple.copy(alpha = 0.7f),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = option.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) HealthPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (isPlaying) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Playing",
                    tint = HealthPurple,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

data class MeditationOption(
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}