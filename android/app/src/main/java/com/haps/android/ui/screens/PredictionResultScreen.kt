package com.haps.android.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haps.android.model.PredictionResponse
import com.haps.android.ui.components.FeatureImportanceChart
import com.haps.android.ui.components.ReportPdfGenerator
import com.haps.android.ui.components.RiskGauge
import com.haps.android.ui.viewmodel.HistoryViewModel
import com.haps.android.ui.viewmodel.PredictionViewModel
import com.haps.android.util.Result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionResultScreen(
    predictionId: Long,
    predictionViewModel: PredictionViewModel,
    historyViewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val predictionResult by predictionViewModel.predictionResult.collectAsState()
    val patientName by predictionViewModel.patientName.collectAsState()

    // Fetch the prediction object
    var currentPrediction by remember {
        mutableStateOf(
            if (predictionResult is Result.Success) {
                (predictionResult as Result.Success<PredictionResponse>).data
            } else {
                PredictionResponse(
                    id = predictionId,
                    probability = 0.78,
                    label = 1,
                    riskLevel = "High Risk",
                    topFactors = listOf(
                        mapOf("feature" to "cp", "importance" to 0.32),
                        mapOf("feature" to "oldpeak", "importance" to 0.24),
                        mapOf("feature" to "trestbps", "importance" to 0.18),
                        mapOf("feature" to "chol", "importance" to 0.15)
                    ),
                    modelVersion = "CatBoost-1.0.0"
                )
            }
        )
    }

    LaunchedEffect(predictionId) {
        historyViewModel.loadDetail(predictionId)
    }

    val selectedDetail by historyViewModel.selectedDetail.collectAsState()
    LaunchedEffect(selectedDetail) {
        selectedDetail?.let { currentPrediction = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prediction Assessment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(Icons.Default.Home, contentDescription = "Dashboard")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Risk Gauge Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PATIENT: ${patientName.ifBlank { "Patient #${currentPrediction.id ?: "001"}" }}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RiskGauge(
                        probability = currentPrediction.probability,
                        riskLevel = currentPrediction.riskLevel
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Evaluated via ${currentPrediction.modelVersion ?: "HAPS CatBoost v1.0"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // 2. Action Buttons (PDF Report & Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        ReportPdfGenerator.generateAndShareReport(
                            context = context,
                            prediction = currentPrediction,
                            patientName = patientName.ifBlank { "Patient #${currentPrediction.id}" }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share PDF")
                }

                OutlinedButton(
                    onClick = {
                        val file = ReportPdfGenerator.generatePdf(
                            context = context,
                            prediction = currentPrediction,
                            patientName = patientName.ifBlank { "Patient #${currentPrediction.id}" }
                        )
                        if (file != null) {
                            Toast.makeText(context, "Report saved: ${file.name}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Failed to export PDF", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save PDF")
                }
            }

            // 3. Top Contributing Factors Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Key Contributing Factors",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    FeatureImportanceChart(factors = currentPrediction.topFactors ?: emptyList())
                }
            }

            // 4. Clinical Medical Recommendations
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Clinical Recommendations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val recommendations = ReportPdfGenerator.getRecommendationsForRisk(currentPrediction.riskLevel)
                    recommendations.forEach { rec ->
                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = rec,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
