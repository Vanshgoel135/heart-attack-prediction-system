package com.haps.android.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haps.android.ui.components.FeatureImportanceChart
import com.haps.android.ui.components.ReportPdfGenerator
import com.haps.android.ui.components.StatusPill
import com.haps.android.ui.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionDetailScreen(
    predictionId: Long,
    historyViewModel: HistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val selectedDetail by historyViewModel.selectedDetail.collectAsState()
    val isLoading by historyViewModel.isLoading.collectAsState()

    LaunchedEffect(predictionId) {
        historyViewModel.loadDetail(predictionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assessment #$predictionId Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    selectedDetail?.let { detail ->
                        IconButton(onClick = {
                            ReportPdfGenerator.generateAndShareReport(context, detail, "Patient #$predictionId")
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share PDF")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading && selectedDetail == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            selectedDetail?.let { item ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Overview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Risk Level:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                StatusPill(riskLevel = item.riskLevel)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Cardiac Risk Probability: ${(item.probability * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            val dateStr = item.createdAt?.take(19)?.replace("T", " ") ?: "Recorded"
                            Text(
                                text = "Recorded: $dateStr • Evaluated via ${item.modelVersion ?: "CatBoost-1.0.0"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Share & Save Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                ReportPdfGenerator.generateAndShareReport(context, item, "Patient #$predictionId")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PDF")
                        }

                        OutlinedButton(
                            onClick = {
                                val file = ReportPdfGenerator.generatePdf(context, item, "Patient #$predictionId")
                                if (file != null) {
                                    Toast.makeText(context, "Saved to cache: ${file.name}", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF")
                        }
                    }

                    // Clinical Factors Inputs Grid
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FactCheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recorded Clinical Parameters",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val features = item.inputFeatures ?: emptyMap()
                            if (features.isEmpty()) {
                                Text("No parameter breakdown recorded.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    features.entries.chunked(2).forEach { rowPairs ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowPairs.forEach { (key, value) ->
                                                Card(
                                                    modifier = Modifier.weight(1f),
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                        Text(text = key, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text(text = "$value", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                            if (rowPairs.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Contributing Factors Chart
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Top Contributing Factors",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FeatureImportanceChart(factors = item.topFactors ?: emptyList())
                        }
                    }

                    // Recommendations
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Clinical Recommendations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val recs = ReportPdfGenerator.getRecommendationsForRisk(item.riskLevel)
                            recs.forEach { rec ->
                                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = rec, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            } ?: Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Assessment record not found.")
            }
        }
    }
}
