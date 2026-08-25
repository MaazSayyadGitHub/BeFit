package com.maaz.befit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.maaz.befit.data.service.NotificationService
import com.maaz.befit.ui.screen.AccessibilitySettingsScreen
import com.maaz.befit.ui.screen.ConnectionStatusScreen
import com.maaz.befit.ui.screen.GoalsScreen
import com.maaz.befit.ui.screen.GoogleFitSetupScreen
import com.maaz.befit.ui.screen.NotificationSettingsScreen
import com.maaz.befit.ui.screen.ReportsScreen
import com.maaz.befit.ui.screen.RunningMapScreen
import com.maaz.befit.ui.screen.SettingsScreen
import com.maaz.befit.ui.screen.SleepTrackingScreen
import com.maaz.befit.ui.screen.WearableSetupScreen

import androidx.compose.material.icons.filled.*
import com.maaz.befit.ui.screen.ModernDashboardScreen
import com.maaz.befit.ui.screen.StepTrackingScreen
import com.maaz.befit.ui.screen.WaterTrackingScreen

@Composable
fun HealthNavigation(
    navController: NavHostController,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost( // nav host container for screens
        navController = navController,
        startDestination = "dashboard",
        modifier = modifier
    ) {
        composable("dashboard") {
            ModernDashboardScreen(
                onNavigateToWater = { navController.navigate("water") },
                onNavigateToSteps = { navController.navigate("steps") },
                onNavigateToSleep = { navController.navigate("sleep") },
                onNavigateToGoals = { navController.navigate("goals") },
                onNavigateToReports = { navController.navigate("reports") },
                onNavigateToConnectionStatus = { navController.navigate("connection_status") },
                onNavigateToRunningMap = { navController.navigate("running_map") },
                onSignOut = onSignOut
            )
        }

        composable("water") {
            WaterTrackingScreen()
        }

        composable("steps") {
            StepTrackingScreen()
        }

        composable("running_map") {
            RunningMapScreen(
                onNavigateBack = { navController.popBackStack() } // remove current screen from stack, means runningMapScreen and go back to previous screen.
            )
        }

        composable("sleep") {
            SleepTrackingScreen()
        }

        composable("reports") {
            ReportsScreen()
        }

        composable("settings") {
            SettingsScreen(
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToGoals = { navController.navigate("goals") },
                onNavigateToAccessibility = { navController.navigate("accessibility") }
            )
        }

        composable("goals") {
            GoalsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("accessibility") {
            AccessibilitySettingsScreen(
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("notifications") {
            // We'll need to inject NotificationService here
            // For now, let's create a simple version
            NotificationSettingsScreen(
                notificationService = NotificationService(
                    LocalContext.current
                ),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("connection_status") {
            ConnectionStatusScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGoogleFitSetup = { navController.navigate("google_fit_setup") },
                onNavigateToWearableSetup = { navController.navigate("wearable_setup") }
            )
        }

        composable("google_fit_setup") {
            GoogleFitSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onSetupComplete = { navController.popBackStack() }
            )
        }

        composable("wearable_setup") {
            WearableSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onSetupComplete = { navController.popBackStack() }
            )
        }
    }
}

//@Composable
//fun WaterTrackingScreen() {
//    TODO("Not yet implemented")
//}
//
//@Composable
//fun StepTrackingScreen() {
//    TODO("Not yet implemented")
//}
//
//@Composable
//fun ModernDashboardScreen(
//    onNavigateToWater: () -> Unit,
//    onNavigateToSteps: () -> Unit,
//    onNavigateToSleep: () -> Unit,
//    onNavigateToGoals: () -> Unit,
//    onNavigateToReports: () -> Unit,
//    onNavigateToConnectionStatus: () -> Unit,
//    onNavigateToRunningMap: () -> Unit,
//    onSignOut: () -> Unit
//) {
//    TODO("Not yet implemented")
//}