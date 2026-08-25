package com.maaz.befit.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.maaz.befit.viewmodel.HealthViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maaz.befit.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionStatusScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGoogleFitSetup: () -> Unit = {},
    onNavigateToWearableSetup: () -> Unit = {},
    viewModel: HealthViewModel = hiltViewModel()
) {
    val connectedWearables by viewModel.connectedWearables.collectAsState()
    val isWearableSyncInProgress by viewModel.isWearableSyncInProgress.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Connection status states
    var isGoogleFitConnected by remember { mutableStateOf(false) }
    var googleFitDataSources by remember { mutableStateOf<List<String>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var lastRefreshTime by remember { mutableStateOf(0L) }

    val coroutineScope = rememberCoroutineScope()

    // Check connections on screen load
    LaunchedEffect(Unit) {
        isRefreshing = true
        try {
            isGoogleFitConnected = viewModel.isGoogleFitAvailable()
            googleFitDataSources = viewModel.getWeeklyStepsFromGoogleFit().take(1).map { "Google Fit" }
            lastRefreshTime = System.currentTimeMillis()
        } catch (e: Exception) {
            // Handle error
        } finally {
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connection Status") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isRefreshing = true
                                try {
                                    isGoogleFitConnected = viewModel.isGoogleFitAvailable()
                                    googleFitDataSources = viewModel.getWeeklyStepsFromGoogleFit().take(1).map { "Google Fit" }
                                    viewModel.refreshWearableDevices()
                                    lastRefreshTime = System.currentTimeMillis()
                                } finally {
                                    isRefreshing = false
                                }
                            }
                        },
                        enabled = !isRefreshing
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ConnectionStatusHeader(
                    lastRefreshTime = lastRefreshTime,
                    isRefreshing = isRefreshing
                )
            }

            item {
                GoogleFitStatusCard(
                    isConnected = isGoogleFitConnected,
                    dataSources = googleFitDataSources,
                    onSync = {
                        coroutineScope.launch {
                            viewModel.syncWithGoogleFit()
                        }
                    },
                    onSetup = onNavigateToGoogleFitSetup
                )
            }

            item {
                WearableDevicesStatusCard(
                    connectedDevices = connectedWearables,
                    isSyncInProgress = isWearableSyncInProgress,
                    onSync = { viewModel.syncWearableData() },
                    onSetup = onNavigateToWearableSetup
                )
            }

            item {
                TroubleshootingCard()
            }

            if (errorMessage != null) {
                item {
                    ErrorCard(
                        message = errorMessage!!,
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusHeader(
    lastRefreshTime: Long,
    isRefreshing: Boolean
) {
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
                    Icons.Default.Info,
                    contentDescription = "Info",
                    tint = HealthBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Connection Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthBlue
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This screen shows the status of all connected health services and devices. " +
                        "Use the refresh button to check for new connections.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (lastRefreshTime > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last updated: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(lastRefreshTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GoogleFitStatusCard(
    isConnected: Boolean,
    dataSources: List<String>,
    onSync: () -> Unit,
    onSetup: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) HealthGreen.copy(alpha = 0.1f) else HealthRed.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        Icons.Default.FitnessCenter,
                        contentDescription = "Google Fit",
                        tint = if (isConnected) HealthGreen else HealthRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google Fit API",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isConnected) HealthGreen else HealthRed
                    )
                }

                Text(
                    text = if (isConnected) "Connected" else "Not Connected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isConnected) HealthGreen else HealthRed,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isConnected) {
                Text(
                    text = "Google Fit is connected and ready to sync data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (dataSources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Available Data Sources:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    dataSources.forEach { source ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Available",
                                tint = HealthGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = source,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSync,
                    colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)
                ) {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = "Sync",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync Google Fit Data")
                }
            } else {
                Text(
                    text = "Google Fit is not connected. Please sign in to your Google account and grant fitness permissions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSetup,
                    colors = ButtonDefaults.buttonColors(containerColor = HealthBlue)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Setup",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Setup Google Fit")
                }
            }
        }
    }
}

@Composable
fun WearableDevicesStatusCard(
    connectedDevices: List<String>,
    isSyncInProgress: Boolean,
    onSync: () -> Unit,
    onSetup: () -> Unit = {}
) {
    val hasDevices = connectedDevices.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasDevices) HealthGreen.copy(alpha = 0.1f) else HealthRed.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        Icons.Default.Watch,
                        contentDescription = "Wearable Devices",
                        tint = if (hasDevices) HealthGreen else HealthRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wearable Devices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasDevices) HealthGreen else HealthRed
                    )
                }

                Text(
                    text = if (hasDevices) "${connectedDevices.size} Connected" else "None",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasDevices) HealthGreen else HealthRed,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (hasDevices) {
                Text(
                    text = "The following wearable devices are connected:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))
                connectedDevices.forEach { device ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Connected",
                            tint = HealthGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = device,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSync,
                    enabled = !isSyncInProgress,
                    colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)
                ) {
                    if (isSyncInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Sync",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isSyncInProgress) "Syncing..." else "Sync Wearable Data")
                }
            } else {
                Text(
                    text = "No wearable devices detected. Make sure your devices are connected via Bluetooth and have the appropriate apps installed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSetup,
                    colors = ButtonDefaults.buttonColors(containerColor = HealthPurple)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Setup",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Setup Wearable Devices")
                }
            }
        }
    }
}

@Composable
fun TroubleshootingCard() {
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
                    Icons.Default.Help,
                    contentDescription = "Help",
                    tint = HealthOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Troubleshooting",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthOrange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val troubleshootingTips = listOf(
                "Make sure you're signed in to your Google account",
                "Grant fitness permissions when prompted",
                "Enable Bluetooth for wearable device connections",
                "Install Samsung Health app for Samsung device support",
                "Install Wear OS companion app for smartwatch support",
                "Check that your devices are within Bluetooth range",
                "Restart the app if connections seem stuck"
            )

            troubleshootingTips.forEach { tip ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "• ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HealthOrange
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
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
                text = message,
                color = HealthRed,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}
