package com.maaz.befit.ui.components

import android.R
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maaz.befit.ui.theme.BeFitTheme
import com.maaz.befit.ui.theme.HealthBlue
import com.maaz.befit.ui.theme.HealthGreen
import com.maaz.befit.ui.theme.HealthOrange
import com.maaz.befit.ui.theme.HealthRed
import com.maaz.befit.ui.theme.HealthTeal


@Composable
fun HealthChart(
    title: String,
    data: List<ChartDataPoint>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Simple bar chart representation
                SimpleBarChart(
                    data = data,
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }
    }
}

@Composable
fun SimpleBarChart(
    data: List<ChartDataPoint>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1f

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { dataPoint ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bar
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height((dataPoint.value / maxValue * 80).dp)
                        .background(
                            color = color,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Label
                Text(
                    text = dataPoint.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WeeklyProgressChart(
    title: String,
    weeklyData: List<WeeklyDataPoint>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (weeklyData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No weekly data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Weekly progress bars
                WeeklyProgressBars(
                    data = weeklyData,
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }
    }
}

@Composable
fun WeeklyProgressBars(
    data: List<WeeklyDataPoint>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { dataPoint ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress bar
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height((dataPoint.value.toFloat() / maxValue * 80).dp)
                        .background(
                            color = color,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Day label
                Text(
                    text = dataPoint.day,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Value label
                Text(
                    text = dataPoint.value.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun HealthScoreGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthTeal.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Health Score",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = HealthTeal
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Circular progress indicator for health score
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = score / 100f,
                    modifier = Modifier.size(120.dp),
                    color = when {
                        score >= 80 -> HealthGreen
                        score >= 60 -> HealthOrange
                        else -> HealthRed
                    },
                    strokeWidth = 8.dp,
                    trackColor = HealthTeal.copy(alpha = 0.3f)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            score >= 80 -> HealthGreen
                            score >= 60 -> HealthOrange
                            else -> HealthRed
                        }
                    )
                    Text(
                        text = "out of 100",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Health score description
            Text(
                text = when {
                    score >= 80 -> "Excellent! You're maintaining great health habits!"
                    score >= 60 -> "Good progress! Keep up the healthy lifestyle!"
                    else -> "Focus on consistent daily habits to improve your score."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Data classes
data class ChartDataPoint(
    val label: String,
    val value: Float
)

data class WeeklyDataPoint(
    val day: String,
    val value: Int
)

@Preview(showBackground = true)
@Composable
fun HealthComponentsFullPreview() {
    BeFitTheme {
        val sampleData = listOf(
            ChartDataPoint("Mon", 2f),
            ChartDataPoint("Tue", 5f),
            ChartDataPoint("Wed", 3f),
            ChartDataPoint("Thu", 7f),
            ChartDataPoint("Fri", 4f),
        )

        val weeklyData = listOf(
            WeeklyDataPoint("Mon", 5000),
            WeeklyDataPoint("Tue", 7000),
            WeeklyDataPoint("Wed", 6500),
            WeeklyDataPoint("Thu", 8000),
            WeeklyDataPoint("Fri", 7000),
            WeeklyDataPoint("Sat", 11000),
            WeeklyDataPoint("Sun", 4000),
        )

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HealthChart(
                title = "Water Intake",
                data = sampleData,
                color = HealthBlue
            )

            WeeklyProgressChart(
                title = "Weekly Steps",
                weeklyData = weeklyData,
                color = HealthGreen
            )

            HealthScoreGauge(score = 75)
        }
    }
}




