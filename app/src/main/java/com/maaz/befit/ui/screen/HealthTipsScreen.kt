package com.maaz.befit.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.maaz.befit.data.service.HealthTip
import com.maaz.befit.data.service.HealthTipsService
import com.maaz.befit.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTipsScreen(
    healthTipsService: HealthTipsService = hiltViewModel<HealthTipsViewModel>().healthTipsService
) {
    var selectedCategory by remember { mutableStateOf("general") }
    var healthTips by remember { mutableStateOf<List<HealthTip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var randomTip by remember { mutableStateOf<HealthTip?>(null) }

    val scope = rememberCoroutineScope()

    val categories = listOf(
        "general" to "General",
        "hydration" to "Hydration",
        "exercise" to "Exercise",
        "sleep" to "Sleep",
        "nutrition" to "Nutrition"
    )

    // Load tips when category changes
    LaunchedEffect(selectedCategory) {
        isLoading = true
        healthTips = healthTipsService.getHealthTips(selectedCategory)
        isLoading = false
    }

    // Load random tip
    LaunchedEffect(Unit) {
        randomTip = healthTipsService.getRandomTip()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Health Tips",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Random Tip Card
        randomTip?.let { tip ->
            item {
                RandomTipCard(tip = tip)
            }
        }

        // Category Filter
        item {
            Text(
                text = "Browse by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { (category, displayName) ->
                    FilterChip(
                        onClick = { selectedCategory = category },
                        label = { Text(displayName) },
                        selected = selectedCategory == category
                    )
                }
            }
        }

        // Tips List
        item {
            Text(
                text = "Health Tips",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(healthTips) { tip ->
                HealthTipCard(tip = tip)
            }
        }
    }
}

@Composable
fun RandomTipCard(tip: HealthTip) {
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
                    Icons.Default.Lightbulb,
                    contentDescription = "Random Tip",
                    tint = HealthTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tip of the Day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthTeal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = tip.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tip.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HealthTipCard(tip: HealthTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (tip.category) {
                "hydration" -> HealthBlue.copy(alpha = 0.1f)
                "exercise" -> HealthGreen.copy(alpha = 0.1f)
                "sleep" -> HealthPurple.copy(alpha = 0.1f)
                "nutrition" -> HealthOrange.copy(alpha = 0.1f)
                else -> HealthTeal.copy(alpha = 0.1f)
            }
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
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = when (tip.category) {
                        "hydration" -> HealthBlue
                        "exercise" -> HealthGreen
                        "sleep" -> HealthPurple
                        "nutrition" -> HealthOrange
                        else -> HealthTeal
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = tip.category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tip.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

class HealthTipsViewModel @javax.inject.Inject constructor(
    val healthTipsService: HealthTipsService
) : androidx.lifecycle.ViewModel()