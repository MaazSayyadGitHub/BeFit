package com.maaz.befit.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maaz.befit.data.service.NotificationService
import com.maaz.befit.ui.components.HealthBackButtonTopAppBar
import com.maaz.befit.ui.theme.*
import com.maaz.befit.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen (
    notificationService: NotificationService,
    onNavigateBack: () -> Unit = {},
    themeViewModel: ThemeViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val notificationsEnabled by themeViewModel.notificationsEnabled.collectAsState()
    var waterReminders by remember { mutableStateOf(true) }
    var exerciseReminders by remember { mutableStateOf(true) }
    var sleepReminders by remember { mutableStateOf(true) }
    var goalReminders by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize()
    ){
        // Top App Bar with Back Button
        HealthBackButtonTopAppBar(
            title = "Notification Settings",
            onBackClick = onNavigateBack,
            healthColor = HealthTeal
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){
            item {
                Text(
                    text = "Customize your health reminders and notifications",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                // Water Reminders
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HealthBlue.copy(alpha = 0.1f))
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            Icons.Default.LocalDrink,
                            contentDescription = "Water Reminders",
                            tint = HealthBlue,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ){
                            Text(
                                text = "Water Reminders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Get reminded to drink water every 2 hours",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = waterReminders,
                            onCheckedChange = {
                                waterReminders = it
                                if (it) {
                                    notificationService.scheduleWaterReminders()
                                } else {
                                    notificationService.cancelAllReminders()
                                }
                            }
                        )
                    }
                }
            }
            item {
                // Exercise Reminders
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HealthGreen.copy(alpha = 0.1f))
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            Icons.Default.DirectionsWalk,
                            contentDescription = "Exercise Reminders",
                            tint = HealthGreen,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Exercise Reminders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Get reminded to move every 3 hours",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = exerciseReminders,
                            onCheckedChange = {
                                exerciseReminders = it
                                if (it) {
                                    notificationService.scheduleStepReminders()
                                } else {
                                    notificationService.cancelAllReminders()
                                }
                            }
                        )
                    }
                }
            }
            item {
                // Sleep Reminders
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HealthPurple.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Bedtime,
                            contentDescription = "Sleep Reminders",
                            tint = HealthPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Sleep Reminders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Get reminded to go to bed at 10 PM",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = sleepReminders,
                            onCheckedChange = {
                                sleepReminders = it
                                if (it) {
                                    notificationService.scheduleSleepReminder()
                                } else {
                                    notificationService.cancelAllReminders()
                                }
                            }
                        )
                    }
                }
            }

            item {
                // Goal Reminders
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HealthOrange.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = "Goal Reminders",
                            tint = HealthOrange,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Goal Reminders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Get reminded about your daily goals progress",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = goalReminders,
                            onCheckedChange = {
                                goalReminders = it
                            }
                        )
                    }
                }
            }
            item {
                // Notification Tips
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HealthTeal.copy(alpha = 0.1f))
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notification Tips",
                                tint = HealthTeal,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Notification Tips",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = HealthTeal
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val tips = listOf(
                            "Enable notifications to stay on track with your health goals",
                            "Customize reminder times based on your daily routine",
                            "Turn off notifications during sleep hours for better rest",
                            "Use smart reminders to build healthy habits",
                            "Check your notification settings regularly"
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
        }
    }
}

// ViewModel for Notification Settings
class NotificationSettingsViewModel @javax.inject.Inject constructor(
    val notificationService: NotificationService
) : androidx.lifecycle.ViewModel()