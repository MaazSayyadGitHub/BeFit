package com.maaz.befit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import com.maaz.befit.data.model.HealthData
import com.maaz.befit.data.model.UserGoals
import com.maaz.befit.data.service.ExportService
import com.maaz.befit.ui.theme.*
import com.maaz.befit.viewmodel.HealthViewModel
import com.maaz.befit.ui.utils.StatItem
import com.maaz.befit.ui.utils.formatNumber
import com.maaz.befit.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: HealthViewModel = hiltViewModel()
) {
    val healthData by viewModel.todayHealthData.collectAsState()
    val userGoals by viewModel.userGoals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Create export service instance
    val context = LocalContext.current
    val exportService = remember { ExportService(context) }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Data is now automatically loaded via reactive flow

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Text(
                text = "Health Reports",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            // Health Score Gauge
            HealthScoreGauge(
                score = healthData?.healthScore ?: 0
            )
        }

        item {
            // Today's Summary
            TodaySummaryCard(
                healthData = healthData,
                userGoals = userGoals,
                isLoading = isLoading
            )
        }

        item {
            // Weekly Overview with Charts
            WeeklyOverviewCard(
                healthData = healthData,
                userGoals = userGoals
            )
        }

        item {
            // Health Metrics
            HealthMetricsCard(
                healthData = healthData,
                userGoals = userGoals
            )
        }

        item {
            // Weekly Steps Chart
            HealthChart(
                title = "Weekly Steps Progress",
                data = getWeeklyStepsData(listOf(healthData).filterNotNull()),
                color = HealthGreen
            )
        }

        item {
            // Weekly Water Chart
            HealthChart(
                title = "Weekly Water Intake",
                data = getWeeklyWaterData(listOf(healthData).filterNotNull()),
                color = HealthBlue
            )
        }

        item {
            // Weekly Sleep Chart
            HealthChart(
                title = "Weekly Sleep Hours",
                data = getWeeklySleepData(listOf(healthData).filterNotNull()),
                color = HealthPurple
            )
        }

        item {
            // Export Options
            ExportOptionsCard(
                onExportCSV = {
                    scope.launch {
                        try {
                            val healthDataList = listOfNotNull(healthData)
                            val filePath = exportService.exportToCSV(healthDataList)
                            exportMessage = "✅ CSV file saved to: $filePath"
                        } catch (e: Exception) {
                            exportMessage = "❌ Export failed: ${e.message}"
                        }
                    }
                },
                onExportPDF = {
                    scope.launch {
                        try {
                            val healthDataList = listOfNotNull(healthData)
                            val filePath = exportService.exportToPDF(healthDataList)
                            exportMessage = "✅ Health report saved to: $filePath"
                        } catch (e: Exception) {
                            exportMessage = "❌ Export failed: ${e.message}"
                        }
                    }
                }
            )
        }

        item {
            // Health Insights
            HealthInsightsCard(
                healthData = healthData,
                userGoals = userGoals
            )
        }

        if (errorMessage != null) {
            item {
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
                            text = errorMessage!!,
                            color = HealthRed,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            }
        }

        if (exportMessage != null) {
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
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = HealthGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = exportMessage!!,
                            color = HealthGreen,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { exportMessage = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodaySummaryCard(
    healthData: HealthData?,
    userGoals: UserGoals?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthBlue.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Today,
                    contentDescription = "Today's Summary",
                    tint = HealthBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Today's Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HealthBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(getTodayMetrics(healthData, userGoals)) { metric ->
                        MetricCard(
                            title = metric.title,
                            value = metric.value,
                            unit = metric.unit,
                            progress = metric.progress,
                            color = metric.color,
                            icon = metric.icon
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyOverviewCard(
    healthData: HealthData?,
    userGoals: UserGoals?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthGreen.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = "Weekly Overview",
                    tint = HealthGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Weekly Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HealthGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekly progress summary
            WeeklyProgressChart(
                title = "7-Day Progress",
                weeklyData = getWeeklyProgressData(listOf(healthData).filterNotNull(), userGoals),
                color = HealthGreen
            )
        }
    }
}

@Composable
fun HealthMetricsCard(
    healthData: HealthData?,
    userGoals: UserGoals?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthPurple.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = "Health Metrics",
                    tint = HealthPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Health Metrics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HealthPurple
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Steps",
                    value = "${healthData?.steps?.formatNumber() ?: "0"}",
                    icon = Icons.Default.DirectionsWalk,
                    color = HealthGreen
                )
                StatItem(
                    label = "Distance",
                    value = "${String.format("%.1f", (healthData?.steps ?: 0) * 0.0008)} km",
                    icon = Icons.Default.Straighten,
                    color = HealthGreen
                )
                StatItem(
                    label = "Calories",
                    value = "${(healthData?.steps ?: 0) * 0.04}",
                    icon = Icons.Default.LocalFireDepartment,
                    color = HealthOrange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Water",
                    value = "${healthData?.waterIntake ?: 0}ml",
                    icon = Icons.Default.LocalDrink,
                    color = HealthBlue
                )
                StatItem(
                    label = "Sleep",
                    value = "${String.format("%.1f", healthData?.sleepHours ?: 0f)}h",
                    icon = Icons.Default.Bedtime,
                    color = HealthPurple
                )
                StatItem(
                    label = "Health Score",
                    value = "${healthData?.healthScore ?: 0}",
                    icon = Icons.Default.Psychology,
                    color = HealthTeal
                )
            }
        }
    }
}

@Composable
fun ExportOptionsCard(
    onExportCSV: () -> Unit = {},
    onExportPDF: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthOrange.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Export Options",
                    tint = HealthOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export Options",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HealthOrange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onExportPDF,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthOrange)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PDF")
                }

                Button(
                    onClick = onExportCSV,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthOrange)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = "CSV")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CSV")
                }
            }
        }
    }
}

@Composable
fun HealthInsightsCard(
    healthData: HealthData?,
    userGoals: UserGoals?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HealthTeal.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = "Health Insights",
                    tint = HealthTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Health Insights",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HealthTeal
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val insights = generateHealthInsights(healthData, userGoals)
            insights.forEach { insight ->
                Text(
                    text = "• $insight",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    progress: Float,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = color.copy(alpha = 0.3f)
            )
        }
    }
}


// Data classes
data class MetricData(
    val title: String,
    val value: String,
    val unit: String,
    val progress: Float,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// Helper functions
fun getTodayMetrics(
    healthData: HealthData?,
    userGoals: UserGoals?
): List<MetricData> {
    val steps = healthData?.steps ?: 0
    val water = healthData?.waterIntake ?: 0
    val sleep = healthData?.sleepHours ?: 0f

    val stepGoal = userGoals?.dailySteps ?: 10000
    val waterGoal = userGoals?.dailyWater ?: 2000
    val sleepGoal = userGoals?.dailySleep ?: 8f

    return listOf(
        MetricData(
            title = "Steps",
            value = steps.formatNumber(),
            unit = "steps",
            progress = (steps.toFloat() / stepGoal).coerceAtMost(1f),
            color = HealthGreen,
            icon = Icons.Default.DirectionsWalk
        ),
        MetricData(
            title = "Water",
            value = "${water}ml",
            unit = "intake",
            progress = (water.toFloat() / waterGoal).coerceAtMost(1f),
            color = HealthBlue,
            icon = Icons.Default.LocalDrink
        ),
        MetricData(
            title = "Sleep",
            value = "${String.format("%.1f", sleep)}h",
            unit = "duration",
            progress = (sleep / sleepGoal).coerceAtMost(1f),
            color = HealthPurple,
            icon = Icons.Default.Bedtime
        )
    )
}

fun generateHealthInsights(
    healthData: HealthData?,
    userGoals: UserGoals?
): List<String> {
    val insights = mutableListOf<String>()

    if (healthData == null) {
        insights.add("Start tracking your health data to get personalized insights!")
        return insights
    }

    val stepGoal = userGoals?.dailySteps ?: 10000
    val waterGoal = userGoals?.dailyWater ?: 2000
    val sleepGoal = userGoals?.dailySleep ?: 8f

    // Overall health score insights
    when {
        healthData.healthScore >= 80 -> insights.add("Excellent health score! You're doing great!")
        healthData.healthScore >= 60 -> insights.add("Good health score! Keep up the great work!")
        else -> insights.add("Your health score can improve. Focus on consistent daily habits.")
    }

    // Step insights
    when {
        healthData.steps >= stepGoal -> insights.add("Outstanding step count! You're very active today.")
        healthData.steps >= stepGoal * 0.8 -> insights.add("Great step progress! You're almost at your goal.")
        else -> insights.add("Try to increase your daily steps for better cardiovascular health.")
    }

    // Water insights
    when {
        healthData.waterIntake >= waterGoal -> insights.add("Perfect hydration! Your body will thank you.")
        healthData.waterIntake >= waterGoal * 0.8 -> insights.add("Good hydration! Just a bit more water needed.")
        else -> insights.add("Increase your water intake for better energy and focus.")
    }

    // Sleep insights
    when {
        healthData.sleepHours >= sleepGoal -> insights.add("Excellent sleep duration! You're well-rested.")
        healthData.sleepHours >= sleepGoal * 0.8 -> insights.add("Good sleep! Try to get a bit more rest.")
        else -> insights.add("Prioritize sleep for better recovery and mental clarity.")
    }

    return insights
}

// Chart data helper functions
fun getWeeklyStepsData(weeklyData: List<HealthData>): List<ChartDataPoint> {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return if (weeklyData.isEmpty()) {
        // Return empty data if no records
        days.map { ChartDataPoint(it, 0f) }
    } else {
        weeklyData.mapIndexed { index, data ->
            ChartDataPoint(days.getOrElse(index) { "Day" }, data.steps.toFloat())
        }
    }
}

fun getWeeklyWaterData(weeklyData: List<HealthData>): List<ChartDataPoint> {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return if (weeklyData.isEmpty()) {
        // Return empty data if no records
        days.map { ChartDataPoint(it, 0f) }
    } else {
        weeklyData.mapIndexed { index, data ->
            ChartDataPoint(days.getOrElse(index) { "Day" }, data.waterIntake.toFloat())
        }
    }
}

fun getWeeklySleepData(weeklyData: List<HealthData>): List<ChartDataPoint> {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return if (weeklyData.isEmpty()) {
        // Return empty data if no records
        days.map { ChartDataPoint(it, 0f) }
    } else {
        weeklyData.mapIndexed { index, data ->
            ChartDataPoint(days.getOrElse(index) { "Day" }, data.sleepHours)
        }
    }
}

fun getWeeklyProgressData(
    weeklyData: List<HealthData>,
    userGoals: UserGoals?
): List<WeeklyDataPoint> {
    val stepGoal = userGoals?.dailySteps ?: 10000
    val waterGoal = userGoals?.dailyWater ?: 2000
    val sleepGoal = userGoals?.dailySleep ?: 8f

    val days = listOf("M", "T", "W", "T", "F", "S", "S")

    return if (weeklyData.isEmpty()) {
        // Return empty progress if no records
        days.map { WeeklyDataPoint(it, 0) }
    } else {
        weeklyData.mapIndexed { index, data ->
            val stepProgress = ((data.steps.toFloat() / stepGoal) * 100).toInt().coerceAtMost(100)
            val waterProgress = ((data.waterIntake.toFloat() / waterGoal) * 100).toInt().coerceAtMost(100)
            val sleepProgress = ((data.sleepHours / sleepGoal) * 100).toInt().coerceAtMost(100)

            // Average of all progress metrics
            val avgProgress = (stepProgress + waterProgress + sleepProgress) / 3
            WeeklyDataPoint(days.getOrElse(index) { "D" }, avgProgress)
        }
    }
}