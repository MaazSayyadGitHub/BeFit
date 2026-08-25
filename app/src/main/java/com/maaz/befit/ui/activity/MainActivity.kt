package com.maaz.befit.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maaz.befit.ui.components.MultiOptionFAB
import com.maaz.befit.ui.navigation.BottomNavigationBar
import com.maaz.befit.ui.navigation.HealthNavigation
import com.maaz.befit.ui.screen.AuthScreen
import com.maaz.befit.ui.screen.SplashScreen
import com.maaz.befit.ui.theme.BeFitTheme
import com.maaz.befit.viewmodel.AuthViewModel
import com.maaz.befit.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            val isHighContrast by themeViewModel.isHighContrast.collectAsState()

            BeFitTheme (
                darkTheme = isDarkTheme,
                highContrast = isHighContrast
            ) {
                val authViewModel: AuthViewModel = hiltViewModel()
                val user by authViewModel.user.collectAsState()

                var showSplash by remember { mutableStateOf(true) }

                when {
                    showSplash -> {
                        SplashScreen(
                            onSplashFinished = {
                                showSplash = false
                            }
                        )
                    }

                    user != null -> {
                        // User is authenticated, show main app
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding(),
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            bottomBar = {
                                BottomNavigationBar(
                                    currentRoute = currentRoute,
                                    onNavigate = { route ->
                                        if (currentRoute != route) {
                                            if (route == "dashboard") {
                                                // For home navigation, clear back stack and go to dashboard
                                                navController.navigate(route) {
                                                    popUpTo(0) {
                                                        inclusive = false
                                                    }
                                                    launchSingleTop = true
                                                }
                                            } else {
                                                // For other screens, use normal navigation
                                                navController.navigate(route) {
                                                    popUpTo("dashboard") {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    }
                                )
                            },
                            floatingActionButton = {
                                MultiOptionFAB(
                                    onWaterClick = {
                                        if (currentRoute != "water") {
                                            navController.navigate("water") {
                                                popUpTo("dashboard") {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    onStepsClick = {
                                        if (currentRoute != "steps") {
                                            navController.navigate("steps") {
                                                popUpTo("dashboard") {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    onSleepClick = {
                                        if (currentRoute != "sleep") {
                                            navController.navigate("sleep") {
                                                popUpTo("dashboard") {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        ) { innerPadding ->
                            val layoutDirection = LocalLayoutDirection.current
                            HealthNavigation(
                                navController = navController,
                                onSignOut = { authViewModel.signOut() },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                    }

                    else -> {
                        // User is not authenticated, show auth screen
                        AuthScreen(
                            onAuthSuccess = {
                                // Auth success is handled by the LaunchedEffect in AuthScreen
                            }
                        )
                    }
                }
            }
        }
    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name Studio!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun GreetingPreview() {
//    BeFitTheme {
//        Greeting("Android")
//    }
//}