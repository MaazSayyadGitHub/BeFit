package com.maaz.befit.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maaz.befit.ui.components.HealthBackButtonTopAppBar
import com.maaz.befit.ui.theme.BeFitTheme
import com.maaz.befit.ui.theme.HealthBlue
import com.maaz.befit.ui.theme.HealthGreen
import com.maaz.befit.ui.theme.HealthOrange
import com.maaz.befit.ui.theme.HealthPurple
import com.maaz.befit.ui.theme.HealthTeal
import com.maaz.befit.viewmodel.ThemeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(
    onNavigateToNotifications: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val isHighContrast by themeViewModel.isHighContrast.collectAsState()
    val isLargeText by themeViewModel.isLargeText.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HealthBackButtonTopAppBar(
            title = "Accessibility Setting",
            onBackClick = onNavigateBack,
            healthColor = HealthPurple
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text(
                    text = "Customize the app to better suit your needs and preferences",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = HealthBlue.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Contrast,
                            contentDescription = "High Contrast",
                            tint = HealthBlue,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "High Contrast Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Increase contrast for better visibility",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isHighContrast,
                            onCheckedChange = {
                                themeViewModel.setHighContrast(it)
                            }
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HealthGreen.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TextFields,
                            contentDescription = "Large Text",
                            tint = HealthGreen,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Large Text",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Increase text size for better readability",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isLargeText,
                            onCheckedChange = {
                                themeViewModel.setHighContrast(it)
                            }
                        )
                    }
                }
            }

            item {
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
                            Icons.Default.DarkMode,
                            contentDescription = "Dark Mode",
                            tint = HealthPurple,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Dark Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Use dark theme for better eye comfort",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = {
                                themeViewModel.setHighContrast(it)
                            }
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HealthTeal.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Accessibility,
                            contentDescription = "Accessibility Features",
                            tint = HealthTeal,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Accessibility Features",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = HealthTeal
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val features = listOf(
                            "TalkBack support for screen readers",
                            "High contrast mode for better visibility",
                            "Large text options for readability",
                            "Semantic labels for all interactive elements",
                            "Keyboard navigation support",
                            "Color-blind friendly color schemes"
                        )

                        features.forEach { feature ->
                            Text(
                                text = "• $feature",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            item {
                // Notification Settings Button
                Button(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthBlue)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Notification Settings")
                }
            }

            item {
                // Tips for Better Accessibility
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
                                Icons.Default.Lightbulb,
                                contentDescription = "Accessibility Tips",
                                tint = HealthOrange,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Accessibility Tips",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = HealthOrange
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val tips = listOf(
                            "Enable TalkBack in your device settings for screen reader support",
                            "Use high contrast mode in low light conditions",
                            "Adjust text size in your device settings for better readability",
                            "Use voice commands with Google Assistant for hands-free operation",
                            "Enable haptic feedback for better interaction feedback"
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


//@Preview(showBackground = true)
//@Composable
//fun AccessibilitySettingsScreenPreview() {
//    BeFitTheme {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            AccessibilitySettingsScreen(
//                onNavigateToNotifications = {},
//                onNavigateBack = {}
//            )
//        }
//    }
//}


