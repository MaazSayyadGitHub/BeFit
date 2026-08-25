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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maaz.befit.ui.theme.*
import com.maaz.befit.viewmodel.ThemeViewModel
import com.maaz.befit.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToAccessibility: () -> Unit = {},
    themeViewModel: ThemeViewModel = hiltViewModel(),
    healthViewModel: HealthViewModel = hiltViewModel()
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val isHighContrast by themeViewModel.isHighContrast.collectAsState()
    val isLargeText by themeViewModel.isLargeText.collectAsState()
    val soundEnabled by themeViewModel.soundEnabled.collectAsState()
    val notificationsEnabled by themeViewModel.notificationsEnabled.collectAsState()

    var showResetDataDialog by remember { mutableStateOf(false) }
    var showExportDataDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text(
                text = "Customize your app experience and manage your data",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Appearance Section
        item {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = HealthBlue
            )
        }

        item {
            // Dark Mode Toggle
            SettingsCard(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                description = "Use dark theme for better eye comfort",
                iconColor = HealthPurple
            ) {
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { themeViewModel.setDarkTheme(it) }
                )
            }
        }

        item {
            // High Contrast Toggle
            SettingsCard(
                icon = Icons.Default.Contrast,
                title = "High Contrast",
                description = "Increase contrast for better visibility",
                iconColor = HealthBlue
            ) {
                Switch(
                    checked = isHighContrast,
                    onCheckedChange = { themeViewModel.setHighContrast(it) }
                )
            }
        }

        item {
            // Large Text Toggle
            SettingsCard(
                icon = Icons.Default.TextFields,
                title = "Large Text",
                description = "Increase text size for better readability",
                iconColor = HealthGreen
            ) {
                Switch(
                    checked = isLargeText,
                    onCheckedChange = { themeViewModel.setLargeText(it) }
                )
            }
        }

        // Sound & Notifications Section
        item {
            Text(
                text = "Sound & Notifications",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = HealthOrange
            )
        }

        item {
            // Sound Effects Toggle
            SettingsCard(
                icon = Icons.Default.VolumeUp,
                title = "Sound Effects",
                description = "Play sounds for water, steps, and sleep tracking",
                iconColor = HealthOrange
            ) {
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { themeViewModel.setSoundEnabled(it) }
                )
            }
        }

        item {
            // Notifications Toggle
            SettingsCard(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                description = "Receive health reminders and updates",
                iconColor = HealthTeal
            ) {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { themeViewModel.setNotificationsEnabled(it) }
                )
            }
        }

        item {
            // Notification Settings Button
            Button(
                onClick = onNavigateToNotifications,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HealthTeal)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notification Settings")
            }
        }

        // Health Goals Section
        item {
            Text(
                text = "Health Goals",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = HealthGreen
            )
        }

        item {
            // Goals Settings Button
            Button(
                onClick = onNavigateToGoals,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)
            ) {
                Icon(Icons.Default.Flag, contentDescription = "Goals")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage Health Goals")
            }
        }

        // Data Management Section
        item {
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = HealthRed
            )
        }

        item {
            // Export Data Button
            Button(
                onClick = { showExportDataDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HealthBlue)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Export")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Health Data")
            }
        }

        item {
            // Reset Data Button
            Button(
                onClick = { showResetDataDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HealthRed)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Reset")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Data")
            }
        }

        // Accessibility Section
        item {
            Text(
                text = "Accessibility",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = HealthTeal
            )
        }

        item {
            // Accessibility Settings Button
            Button(
                onClick = onNavigateToAccessibility,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HealthTeal)
            ) {
                Icon(Icons.Default.Accessibility, contentDescription = "Accessibility")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Accessibility Settings")
            }
        }

        // App Info Section
        item {
            Text(
                text = "App Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Smart Health Tracker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Track your daily health activities with AI-driven insights",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Reset Data Confirmation Dialog
    if (showResetDataDialog) {
        AlertDialog(
            onDismissRequest = { showResetDataDialog = false },
            title = { Text("Reset All Data") },
            text = {
                Text("Are you sure you want to reset all your health data? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        healthViewModel.resetAllData()
                        showResetDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthRed)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Data Confirmation Dialog
    if (showExportDataDialog) {
        AlertDialog(
            onDismissRequest = { showExportDataDialog = false },
            title = { Text("Export Health Data") },
            text = {
                Text("Your health data will be exported to your device's Downloads folder. You can choose between CSV (spreadsheet) or PDF (report) format.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Implement export functionality with format selection
                        showExportDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthBlue)
                ) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
@Composable
fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = iconColor.copy(alpha = 0.1f))
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
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
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            content()
        }
    }
}
