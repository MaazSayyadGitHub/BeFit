package com.maaz.befit.ui.components


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maaz.befit.ui.theme.BeFitTheme
import com.maaz.befit.ui.theme.HealthBlue
import com.maaz.befit.ui.theme.HealthGreen
import com.maaz.befit.ui.theme.HealthPurple

@Composable
fun MultiOptionFAB (
    onWaterClick: () -> Unit,
    onStepsClick: () -> Unit,
    onSleepClick: () -> Unit,
    modifier: Modifier = Modifier
){
    var showMenu by remember { mutableStateOf(true) }

    val rotation by animateFloatAsState(
        targetValue = if (showMenu) 45f else 0f,
        animationSpec = tween(300),
        label = "ratation"
    )

    FloatingActionButton(
        onClick = { showMenu = !showMenu},
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = if (showMenu) "Close Menu" else "Open Menu",
            modifier = Modifier.rotate(rotation)
        )
    }
    if (showMenu) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = {
                Text(
                    text = "Quick Log",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "What would you like to log?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Water option
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onWaterClick()
                                showMenu = false
                            },
                        colors = CardDefaults.cardColors(containerColor = HealthBlue.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocalDrink,
                                contentDescription = "Water",
                                tint = HealthBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Water Intake",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    // Steps option
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onStepsClick()
                                showMenu = false
                            },
                        colors = CardDefaults.cardColors(containerColor = HealthGreen.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DirectionsWalk,
                                contentDescription = "Steps",
                                tint = HealthGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Step Count",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    // Sleep option
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSleepClick()
                                showMenu = false
                            },
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
                                contentDescription = "Sleep",
                                tint = HealthPurple,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sleep Log",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMenu = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MultiOptionFABPreview() {

    BeFitTheme {
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            MultiOptionFAB(
                onWaterClick = {},
                onStepsClick = {},
                onSleepClick = {},
                modifier = Modifier
            )
        }

    }
}

