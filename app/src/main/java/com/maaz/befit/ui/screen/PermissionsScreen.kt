package com.maaz.befit.ui.screen

import android.Manifest
import android.graphics.drawable.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.components.Description
import com.maaz.befit.ui.theme.*
import com.maaz.befit.ui.utils.rememberPermissionsManager
import com.maaz.befit.ui.utils.rememberPermissionLauncher

@Composable
fun PermissionsScreen(
    onPermissionsGranted: () -> Unit = {}
) {
    val permissionsManager = rememberPermissionsManager()
    val permissionLauncher = rememberPermissionLauncher { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            onPermissionsGranted()
        }
    }

    var showPermissionDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Permissions Required",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text(
                text = "Smart Health Tracker needs certain permissions to provide you with the best health tracking experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            // Activity Recognition Permission
            PermissionCard(
                icon = Icons.Default.DirectionsWalk,
                title = "Activity Recognition",
                description = "Track your steps, walking, running, and other physical activities automatically",
                isGranted = permissionsManager.hasActivityRecognitionPermission(),
                iconColor = HealthGreen,
                onRequestPermission = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION))
                }
            )
        }

        item {
            // Location Permission
            PermissionCard(
                icon = Icons.Default.LocationOn,
                title = "Location Access",
                description = "Track your outdoor activities and provide accurate distance measurements",
                isGranted = permissionsManager.hasLocationPermission(),
                iconColor = HealthBlue,
                onRequestPermission = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                }
            )
        }

        item {
            // Notification Permission
            PermissionCard(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                description = "Send you helpful reminders for water intake, exercise, and sleep",
                isGranted = permissionsManager.hasNotificationPermission(),
                iconColor = HealthOrange,
                onRequestPermission = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
            )
        }

        item {
            // Request All Permissions Button
            if (!permissionsManager.hasAllRequiredPermissions()) {
                Button(
                    onClick = {
                        val missingPermissions = permissionsManager.getMissingPermissions().toTypedArray()
                        permissionLauncher.launch(missingPermissions)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthTeal)
                ) {
                    Icon(Icons.Default.Security, contentDescription = "Request All")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request All Permissions")
                }
            } else {
                Button(
                    onClick = onPermissionsGranted,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Continue")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue to App")
                }
            }
        }

        item {
            // Privacy Information
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
                            Icons.Default.PrivacyTip,
                            contentDescription = "Privacy",
                            tint = HealthTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacy & Data Protection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = HealthTeal
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val privacyPoints = listOf(
                        "All your health data is stored locally on your device",
                        "No personal data is sent to external servers",
                        "You have full control over your data",
                        "Data can be exported or deleted at any time",
                        "Permissions are only used for health tracking features"
                    )

                    privacyPoints.forEach { point ->
                        Text(
                            text = "• $point",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        item {
            // Skip Permissions Option
            TextButton(
                onClick = { onPermissionsGranted() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for now (Limited functionality)")
            }
        }
    }
}

@Composable
fun PermissionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    iconColor: Color,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted)
                HealthGreen.copy(alpha = 0.1f)
            else
                iconColor.copy(alpha = 0.1f)
        )
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
                tint = if (isGranted) HealthGreen else iconColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (isGranted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Granted",
                            tint = HealthGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isGranted) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = iconColor)
                ) {
                    Text("Grant")
                }
            }
        }
    }
}