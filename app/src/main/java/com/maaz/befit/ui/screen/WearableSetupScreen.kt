package com.maaz.befit.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.maaz.befit.data.service.WearableDeviceService
import com.maaz.befit.ui.components.HealthBackButtonTopAppBar
import com.maaz.befit.ui.theme.*
import com.maaz.befit.viewmodel.HealthViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearableSetupScreen(
    onNavigateBack: () -> Unit = {},
    onSetupComplete: () -> Unit = {},
    viewModel: HealthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val wearableService = remember { WearableDeviceService(context) }
    val coroutineScope = rememberCoroutineScope()

    var connectedDevices by remember { mutableStateOf<List<String>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastSyncTime by remember { mutableStateOf<String?>(null) }

    // Animation states
    val alphaAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800),
        label = "alpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    suspend fun scanForDevicesSuspend() {
        isScanning = true
        isLoading = true
        try {
            // Simulate scanning delay for better UX
            delay(2000)
            connectedDevices = wearableService.getConnectedDevices()
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = "Error scanning for devices: ${e.message}"
        } finally {
            isScanning = false
            isLoading = false
        }
    }

    // Scan for devices
    LaunchedEffect(Unit) {
        scanForDevicesSuspend()
    }

    suspend fun syncWearableDataSuspend() {
        isLoading = true
        try {
            val success = wearableService.syncWearableData()
            if (success) {
                lastSyncTime = "Just now"
                errorMessage = null
            } else {
                errorMessage = "Sync failed. Please check your device connections."
            }
        } catch (e: Exception) {
            errorMessage = "Error syncing data: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        HealthPurple.copy(alpha = 0.05f),
                        Color.White
                    )
                )
            )
    ) {
        HealthBackButtonTopAppBar(
            title = "Wearable Devices",
            onBackClick = onNavigateBack,
            healthColor = HealthPurple
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alphaAnim)
                        .scale(scaleAnim),
                    colors = CardDefaults.cardColors(
                        containerColor = HealthPurple.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Watch,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = HealthPurple
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Connect Your Devices",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = HealthPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sync your fitness trackers, smartwatches, and health apps for comprehensive health tracking.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Device Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
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
                                text = "Device Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HealthPurple
                            )

                            if (isScanning) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = HealthPurple
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Scanning...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                TextButton(
                                    onClick = {
                                        // Trigger rescan
                                        coroutineScope.launch {
                                            scanForDevicesSuspend()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Rescan",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (connectedDevices.isEmpty() && !isScanning) {
                            // No devices found
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeviceUnknown,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No devices detected",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Make sure your devices are connected and apps are installed",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Show connected devices
                            connectedDevices.forEach { device ->
                                DeviceItem(
                                    deviceName = device,
                                    isConnected = true,
                                    onSync = {
                                        coroutineScope.launch {
                                            syncWearableDataSuspend()
                                        }
                                    },
                                    isLoading = isLoading
                                )
                            }
                        }
                    }
                }
            }

            // Supported Devices
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Supported Devices & Apps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HealthPurple
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val supportedDevices = listOf(
                            DeviceInfo(
                                name = "Samsung Health",
                                description = "Samsung Galaxy Watch, Galaxy Fit, and Samsung Health app",
                                icon = Icons.Default.PhoneAndroid,
                                setupRequired = "Install Samsung Health app"
                            ),
                            DeviceInfo(
                                name = "Wear OS",
                                description = "Google Pixel Watch, Fossil, TicWatch, and other Wear OS devices",
                                icon = Icons.Default.Watch,
                                setupRequired = "Install Wear OS app"
                            ),
                            DeviceInfo(
                                name = "Bluetooth Fitness Trackers",
                                description = "Fitbit, Garmin, and other Bluetooth-enabled fitness devices",
                                icon = Icons.Default.Bluetooth,
                                setupRequired = "Pair device via Bluetooth"
                            )
                        )

                        supportedDevices.forEach { device ->
                            SupportedDeviceItem(device = device)
                            if (device != supportedDevices.last()) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }

            // Sync Status
            if (lastSyncTime != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = HealthGreen.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = HealthGreen
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Last sync: $lastSyncTime",
                                style = MaterialTheme.typography.bodyMedium,
                                color = HealthGreen
                            )
                        }
                    }
                }
            }

            // Error Message
            if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (connectedDevices.isNotEmpty()) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    syncWearableDataSuspend()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HealthPurple
                            ),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLoading) "Syncing..." else "Sync All Devices",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = HealthPurple
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Continue to Dashboard",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun DeviceItem(
    deviceName: String,
    isConnected: Boolean,
    onSync: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) HealthGreen.copy(alpha = 0.1f) else Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    deviceName.contains("Samsung") -> Icons.Default.PhoneAndroid
                    deviceName.contains("Wear OS") -> Icons.Default.Watch
                    deviceName.contains("Bluetooth") -> Icons.Default.Bluetooth
                    else -> Icons.Default.DeviceHub
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isConnected) HealthGreen else MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isConnected) HealthGreen else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isConnected) "Connected" else "Not connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                            color = HealthGreen
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = HealthGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportedDeviceItem(device: DeviceInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = device.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = HealthPurple
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = device.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Setup: ${device.setupRequired}",
                style = MaterialTheme.typography.bodySmall,
                color = HealthPurple,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class DeviceInfo(
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val setupRequired: String
)