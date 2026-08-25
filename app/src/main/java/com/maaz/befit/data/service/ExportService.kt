package com.maaz.befit.data.service

import android.content.Context
import android.os.Build
import android.os.Environment
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.maaz.befit.data.model.HealthData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportService(private val context: Context) {

    suspend fun exportToCSV(healthData: List<HealthData>): String = withContext(Dispatchers.IO) {

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "BeFit_HealthTracker_Data_$timestamp.csv"
        val file = getDownloadsFile(fileName)

        FileWriter(file).use { writer ->
            // Write CSV header
            writer.append("Date,Steps,Distance (m),Calories Burned,Water Intake (ml),Sleep Hours,Heart Rate,Health Score,Created At,Updated At\n")

            // write data rows
            healthData.forEach { data ->
                writer.append("${data.date}")
                writer.append("${data.steps}")
                writer.append("${data.distance}")
                writer.append("${data.caloriesBurned}")
                writer.append("${data.waterIntake}")
                writer.append("${data.sleepHours}")
                writer.append("${data.heartRate}")
                writer.append("${data.healthScore}")
                writer.append("${data.createdAt}")
                writer.append("${data.updatedAt}")
            }
        }

        // Return user-friendly path
        "Downloads/BeFitHealthTracker_Data_$timestamp.csv"

    }

    suspend fun exportToPDF(healthData: List<HealthData>) : String = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "BeFit_HealthTracker_Report_$timestamp.pdf"
        val file = getDownloadsFile(fileName)


        // Create PDF using iText
        FileOutputStream(file).use { outputStream ->
            val pdfWriter = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Title
            val title = Paragraph("BE-FIT HEALTH TRACKER - HEALTH REPORT")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18f)
            document.add(title)

            // Report info
            val reportInfo = Paragraph()
                .add("Generated On: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date())}\n")
                .add("Report Period: ${if (healthData.isNotEmpty()) "${healthData.first().date} to ${healthData.last().date}" else "No data available"}\n")
                .add("Total Records: ${healthData.size}")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12f)

            document.add(reportInfo)

            document.add(Paragraph("\n"))

            if (healthData.isNotEmpty()) {
                // Summary Statistics
                val totalSteps = healthData.sumOf { it.steps }
                val totalWater = healthData.sumOf { it.waterIntake }
                val totalDistance = healthData.sumOf { it.distance.toDouble() }
                val totalCalories = healthData.sumOf { it.caloriesBurned }
                val avgSleep = healthData.map { it.sleepHours }.average()
                val avgHealthScore = healthData.map { it.healthScore }.average()
                val avgHeartRate = if (healthData.any { it.heartRate > 0 }) {
                    healthData.filter { it.heartRate > 0 }.map { it.heartRate }.average()
                } else 0.0

                val summaryTitle = Paragraph("SUMMARY STATISTICS")
                    .setFontSize(14f)
                    .setBold()

                document.add(summaryTitle)

                val summaryTable = Table(UnitValue.createPointArray(floatArrayOf(50f, 50f)))
                    .setWidth(UnitValue.createPercentValue(100f))

                summaryTable.addCell(Cell().add(Paragraph("Total Steps")))
                summaryTable.addCell(Cell().add(Paragraph(String.format("%,d", totalSteps))))

                summaryTable.addCell(Cell().add(Paragraph("Total Distance")))
                summaryTable.addCell(Cell().add(Paragraph("${String.format("%.2f", totalDistance / 1000)} km")))

                summaryTable.addCell(Cell().add(Paragraph("Total Calories Burned")))
                summaryTable.addCell(Cell().add(Paragraph(String.format("%,d", totalCalories))))

                summaryTable.addCell(Cell().add(Paragraph("Total Water Intake")))
                summaryTable.addCell(Cell().add(Paragraph("${String.format("%,d", totalWater)} ml")))

                summaryTable.addCell(Cell().add(Paragraph("Average Sleep")))
                summaryTable.addCell(Cell().add(Paragraph("${String.format("%.1f", avgSleep)} hours")))

                summaryTable.addCell(Cell().add(Paragraph("Average Heart Rate")))
                summaryTable.addCell(Cell().add(Paragraph("${String.format("%.0f", avgHeartRate)} bmp")))

                summaryTable.addCell(Cell().add(Paragraph("Average Health Score")))
                summaryTable.addCell(Cell().add(Paragraph("${String.format("%.1f", avgHealthScore)}/100")))

                document.add(summaryTable)
                document.add(Paragraph("\n"))

                // Health insights
                val insightsTitle = Paragraph("HEALTH INSIGHTS")
                    .setFontSize(14f)
                    .setBold()

                document.add(insightsTitle)

                val bestDay = healthData.maxByOrNull { it.healthScore }
                val worstDay = healthData.minByOrNull { it.healthScore }
                val mostActiveDay = healthData.maxByOrNull { it.steps }

                val insights = Paragraph()
                bestDay?.let {
                    insights.add("Best Health Day: ${it.date} (Score: ${it.healthScore}/100)\n")
                }
                worstDay?.let {
                    insights.add("Needs Improvement: ${it.date} (Score: ${it.healthScore}/100)\n")
                }
                mostActiveDay?.let {
                    insights.add("Most Active Day: ${it.date} (${String.format("%,d", it.steps)} steps)\n")
                }

                document.add(insights)
                document.add(Paragraph("\n"))

                // Detailed Data Table
                val dataTitle = Paragraph("DETAILED DAILY DATA")
                    .setFontSize(14f)
                    .setBold()
                document.add(dataTitle)

                val dataTable = Table(UnitValue.createPercentArray(floatArrayOf(20f, 15f, 15f, 15f, 15f, 20f)))
                    .setWidth(UnitValue.createPercentValue(100f))

                // Table Headers

                dataTable.addHeaderCell(Cell().add(Paragraph("Date")))
                dataTable.addHeaderCell(Cell().add(Paragraph("Steps")))
                dataTable.addHeaderCell(Cell().add(Paragraph("Water (ml)")))
                dataTable.addHeaderCell(Cell().add(Paragraph("Sleep (h)")))
                dataTable.addHeaderCell(Cell().add(Paragraph("Health Score")))
                dataTable.addHeaderCell(Cell().add(Paragraph("Created")))

                // table data
                healthData.sortedByDescending { it.date }.forEach { data ->
                    dataTable.addCell(Cell().add(Paragraph(data.date)))
                    dataTable.addCell(Cell().add(Paragraph(String.format("%,d", data.steps))))
                    dataTable.addCell(Cell().add(Paragraph(String.format("%,d", data.waterIntake))))
                    dataTable.addCell(Cell().add(Paragraph(String.format("%.1f", data.sleepHours))))
                    dataTable.addCell(Cell().add(Paragraph(data.healthScore.toString())))
                    dataTable.addCell(Cell().add(Paragraph(data.createdAt.take(10)))) // just the date part
                }

                document.add(dataTable)

            } else {
                val noDataMessage = Paragraph("No Health Data Available for Export.\nStart tracking your Health to Generate Reports!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12f)
                document.add(noDataMessage)
            }

            document.close()
        }

        // return user-friendly path
        "Downloads/BeFitHealthTracker_Report_$timestamp.pdf"
    }

    /**
     * Get a file in Downloads Directory with user-friendly access
     */

    private fun getDownloadsFile(fileName: String) : File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            // For Android 10+ (API 29+), use scoped storage
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(downloadsDir, fileName)
        } else {
            // For older Android versions, use external storage
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(downloadsDir, fileName)
        }
    }

    /**
     * Get user-friendly export directory path
     */
    fun getExportDirectoryPath() : String {
        return "Downloads"
    }

    /**
     * Get Available exports from Downloads folder
     */
    fun getAvailableExports() : List<File> {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return downloadsDir.listFiles { file ->
            file.name.startsWith("BeFitHealthTracker_") && (file.name.endsWith(".csv") || file.name.endsWith(".pdf"))
        }?.toList() ?: emptyList()
    }

    /**
     * Delete On Export File
     */
    fun deleteExport(file: File) : Boolean {
        return try {
            file.delete() // returns true or false
        } catch (e : Exception){
            false
        }
    }

    /**
     * Get user-friendly path for display
     */
    fun getUserFriendlyPath(file: File) : String {
        val fileName = file.name
        return "Downloads/$fileName"
    }
}

// Extension function for string repetition
private operator fun String.times(n : Int): String = this.repeat(n)

