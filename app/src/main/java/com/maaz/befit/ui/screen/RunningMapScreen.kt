package com.maaz.befit.ui.screen

import androidx.compose.runtime.Composable
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.maaz.befit.data.service.LocationService
import com.maaz.befit.data.service.WorkoutSession
import com.maaz.befit.data.service.WorkoutType
import com.maaz.befit.ui.theme.*
import com.maaz.befit.viewmodel.HealthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningMapScreen(
    onNavigateBack: () -> Unit,
    viewModel: HealthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }

    var hasLocationPermission by remember {
        mutableStateOf(locationService.hasLocationPermission())
    }
    var isTracking by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var workoutSession by remember { mutableStateOf<WorkoutSession?>(null) }
    var routePolyline by remember { mutableStateOf<Polyline?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var isMapReady by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Get current location when permissions are granted
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && currentLocation == null) {
            try {
                currentLocation = locationService.getCurrentLocation()
            } catch (e: Exception) {
                Log.e("RunningMapScreen", "Error getting current location", e)
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Request permissions if not granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Start/Stop tracking
    fun startTracking() {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        if (!isMapReady) {
            Log.w("RunningMapScreen", "Map not ready yet, cannot start tracking")
            return
        }

        isTracking = true
        workoutSession = locationService.startWorkoutSession(WorkoutType.RUNNING)

        coroutineScope.launch {
            try {
                locationService.startLocationUpdates().collect { location ->
                    currentLocation = location
                    locationService.addLocationToWorkout(location)
                    workoutSession = locationService.getCurrentWorkoutSession()

                    // Update map camera to follow user (only if map is ready)
                    googleMap?.let { map ->
                        try {
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(location.latitude, location.longitude),
                                    18f
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("RunningMapScreen", "Error updating camera position", e)
                        }
                    }

                    // Update route polyline
                    workoutSession?.let { session ->
                        if (session.locations.size >= 2) { // Need at least 2 points for a line
                            googleMap?.let { map ->
                                try {
                                    // Remove old polyline
                                    routePolyline?.remove()

                                    // Create new polyline with all locations
                                    routePolyline = map.addPolyline(
                                        PolylineOptions()
                                            .addAll(session.locations)
                                            .color(android.graphics.Color.RED)
                                            .width(8f)
                                            .pattern(listOf(Dash(20f), Gap(10f)))
                                            .geodesic(true) // Follow earth's curvature
                                    )

                                    Log.d("RunningMapScreen", "Updated polyline with ${session.locations.size} points")
                                } catch (e: Exception) {
                                    Log.e("RunningMapScreen", "Error updating polyline", e)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RunningMapScreen", "Error in location updates", e)
                isTracking = false
            }
        }
    }

    fun stopTracking() {
        isTracking = false
        workoutSession = locationService.endWorkoutSession()
        routePolyline?.remove()
        routePolyline = null
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (isTracking) "Running - Active" else "Running Tracker",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = HealthGreen,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        if (!hasLocationPermission) {
            // Permission request UI
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthRed.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocationOff,
                            contentDescription = "Location Permission",
                            tint = HealthRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Location Permission Required",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "We need location access to track your running route and show it on the map.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthRed)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        } else {
            // Map View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            onCreate(null)
                            onResume()
                            getMapAsync { map ->
                                googleMap = map
                                isMapReady = true

                                // Configure map
                                map.uiSettings.apply {
                                    isZoomControlsEnabled = true
                                    isMyLocationButtonEnabled = true
                                    isCompassEnabled = true
                                    isMapToolbarEnabled = false
                                }

                                // Enable my location
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    map.isMyLocationEnabled = true
                                }

                                // Set initial camera position
                                coroutineScope.launch {
                                    try {
                                        val location = currentLocation ?: locationService.getCurrentLocation()
                                        location?.let { loc ->
                                            currentLocation = loc
                                            map.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(loc.latitude, loc.longitude),
                                                    18f
                                                )
                                            )
                                        } ?: run {
                                            // Default to a reasonable location if no current location
                                            map.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(37.7749, -122.4194), // San Francisco
                                                    15f
                                                )
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.e("RunningMapScreen", "Error setting initial camera position", e)
                                        // Fallback to default location
                                        map.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(37.7749, -122.4194), // San Francisco
                                                15f
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Tracking Controls Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    // Workout Stats Card
                    if (workoutSession != null) {
                        WorkoutStatsCard(
                            workoutSession = workoutSession!!,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Start/Stop Button
                    Button(
                        onClick = {
                            if (isTracking) {
                                stopTracking()
                            } else {
                                startTracking()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTracking) HealthRed else HealthGreen
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(
                            if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isTracking) "Stop" else "Start",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTracking) "Stop Run" else "Start Run",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutStatsCard(
    workoutSession: WorkoutSession,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Workout Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Distance",
                    value = locationService.formatDistance(workoutSession.totalDistance),
                    icon = Icons.Default.Straighten,
                    color = HealthBlue
                )

                StatItem(
                    label = "Duration",
                    value = locationService.formatDuration(
                        System.currentTimeMillis() - workoutSession.startTime
                    ),
                    icon = Icons.Default.Timer,
                    color = HealthOrange
                )

                StatItem(
                    label = "Pace",
                    value = locationService.formatPace(workoutSession.averagePace),
                    icon = Icons.Default.Speed,
                    color = HealthGreen
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}