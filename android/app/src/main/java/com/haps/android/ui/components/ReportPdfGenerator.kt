package com.haps.android.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.haps.android.model.PredictionResponse
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReportPdfGenerator {

    fun generateAndShareReport(
        context: Context,
        prediction: PredictionResponse,
        patientName: String = "Patient"
    ) {
        val pdfFile = generatePdf(context, prediction, patientName) ?: return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "HAPS Cardiovascular Risk Assessment Report - $patientName")
            putExtra(Intent.EXTRA_TEXT, "Attached is the HAPS Heart Attack Risk Assessment Report for $patientName.\nRisk Level: ${prediction.riskLevel} (${(prediction.probability * 100).toInt()}%).")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Clinical Report"))
    }

    fun generatePdf(
        context: Context,
        prediction: PredictionResponse,
        patientName: String
    ): File? {
        return try {
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(10, 37, 64)
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(80, 90, 105)
                textSize = 12f
            }

            val sectionPaint = Paint().apply {
                color = Color.rgb(10, 37, 64)
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val bodyPaint = Paint().apply {
                color = Color.rgb(30, 40, 50)
                textSize = 11f
            }

            val boldBodyPaint = Paint().apply {
                color = Color.rgb(30, 40, 50)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            var y = 50f

            // Header Banner
            val bannerPaint = Paint().apply { color = Color.rgb(10, 37, 64) }
            canvas.drawRect(0f, 0f, 595f, 75f, bannerPaint)

            val headerTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("HAPS — HEART ATTACK PREDICTION SYSTEM", 40f, 45f, headerTextPaint)

            val subHeaderPaint = Paint().apply {
                color = Color.rgb(200, 220, 240)
                textSize = 10f
            }
            canvas.drawText("CLINICAL CARDIAC RISK ASSESSMENT REPORT", 40f, 62f, subHeaderPaint)

            y = 105f
            canvas.drawText("Patient Identification & Timestamp", 40f, y, sectionPaint)
            y += 20f
            canvas.drawText("Patient Name: $patientName", 40f, y, boldBodyPaint)
            val dateStr = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.US).format(Date())
            canvas.drawText("Assessment Date: $dateStr", 320f, y, bodyPaint)
            y += 18f
            canvas.drawText("Prediction ID: #${prediction.id ?: "NEW"}", 40f, y, bodyPaint)
            canvas.drawText("AI Model Version: ${prediction.modelVersion ?: "1.0.0"}", 320f, y, bodyPaint)

            y += 35f
            // Risk Summary Card
            val cardPaint = Paint().apply {
                color = when {
                    prediction.riskLevel.contains("High", ignoreCase = true) -> Color.rgb(255, 235, 235)
                    prediction.riskLevel.contains("Mod", ignoreCase = true) -> Color.rgb(255, 245, 230)
                    else -> Color.rgb(235, 250, 240)
                }
            }
            canvas.drawRoundRect(40f, y, 555f, y + 80f, 8f, 8f, cardPaint)

            val riskTitlePaint = Paint().apply {
                color = when {
                    prediction.riskLevel.contains("High", ignoreCase = true) -> Color.rgb(186, 26, 26)
                    prediction.riskLevel.contains("Mod", ignoreCase = true) -> Color.rgb(230, 81, 0)
                    else -> Color.rgb(46, 125, 50)
                }
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("ASSESSMENT: ${prediction.riskLevel.uppercase()}", 60f, y + 38f, riskTitlePaint)

            val probPaint = Paint().apply {
                color = Color.rgb(50, 60, 70)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val probPercent = (prediction.probability * 100).toInt()
            canvas.drawText("Calculated Cardiovascular Risk Probability: $probPercent%", 60f, y + 62f, probPaint)

            y += 115f
            canvas.drawText("Clinical Feature Inputs", 40f, y, sectionPaint)
            y += 18f

            val features = prediction.inputFeatures ?: emptyMap()
            var col = 0
            features.entries.take(12).forEach { (k, v) ->
                val xPos = if (col % 2 == 0) 40f else 300f
                canvas.drawText("• $k: $v", xPos, y, bodyPaint)
                if (col % 2 != 0) {
                    y += 16f
                }
                col++
            }
            if (col % 2 != 0) y += 16f

            y += 25f
            canvas.drawText("Key Contributing Risk Factors", 40f, y, sectionPaint)
            y += 18f

            val topFactors = prediction.topFactors ?: emptyList()
            if (topFactors.isEmpty()) {
                canvas.drawText("No abnormal contributing factor spikes detected.", 40f, y, bodyPaint)
                y += 16f
            } else {
                topFactors.take(4).forEach { factor ->
                    val fName = factor["feature"]?.toString() ?: "Factor"
                    val imp = (factor["importance"] as? Number)?.toDouble() ?: 0.0
                    canvas.drawText("• $fName: ${(imp * 100).toInt()}% relative contribution", 40f, y, bodyPaint)
                    y += 16f
                }
            }

            y += 25f
            canvas.drawText("Physician Recommendations", 40f, y, sectionPaint)
            y += 18f

            val recommendations = getRecommendationsForRisk(prediction.riskLevel)
            recommendations.forEach { rec ->
                canvas.drawText("• $rec", 40f, y, bodyPaint)
                y += 16f
            }

            y += 35f
            // Disclaimer
            val disclaimerPaint = Paint().apply {
                color = Color.rgb(120, 130, 140)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
            canvas.drawText("DISCLAIMER: This report is generated by the Heart Attack Prediction System (HAPS) AI model.", 40f, y, disclaimerPaint)
            canvas.drawText("It is designed for clinical decision support and should be evaluated by a licensed cardiologist.", 40f, y + 14f, disclaimerPaint)

            doc.finishPage(page)

            val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val file = File(outputDir, "HAPS_Report_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            doc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            doc.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getRecommendationsForRisk(riskLevel: String): List<String> {
        return when {
            riskLevel.contains("High", ignoreCase = true) -> listOf(
                "Immediate cardiologist consultation recommended.",
                "Schedule Coronary Angiography and ECG monitoring.",
                "Review lipid-lowering (statin) therapy and anti-platelet treatment.",
                "Strict lifestyle monitoring: low-sodium DASH diet, smoking cessation."
            )
            riskLevel.contains("Mod", ignoreCase = true) -> listOf(
                "Follow-up with primary care physician within 2-4 weeks.",
                "Perform Exercise Stress Test (TMT) and 2D Echocardiogram.",
                "Optimize blood pressure and target LDL cholesterol below 100 mg/dL.",
                "Engage in 150 minutes of moderate aerobic activity weekly."
            )
            else -> listOf(
                "Maintain healthy balanced Mediterranean or heart-healthy diet.",
                "Regular annual cardiovascular check-ups and lipid profiling.",
                "Continue moderate daily physical exercise (30 mins/day).",
                "Maintain healthy body weight and manage stress levels."
            )
        }
    }
}
