package com.haps.android.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haps.android.ui.theme.RiskHigh
import com.haps.android.ui.theme.RiskLow
import com.haps.android.ui.theme.RiskMed

@Composable
fun FeatureImportanceChart(
    factors: List<Map<String, Any>>,
    modifier: Modifier = Modifier
) {
    if (factors.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No dominant clinical risk factors detected.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Find maximum importance for normalization
        val maxImportance = factors.maxOfOrNull {
            (it["importance"] as? Number)?.toDouble() ?: 0.1
        } ?: 1.0

        factors.take(5).forEach { factor ->
            val rawName = factor["feature"]?.toString() ?: "Factor"
            val displayName = formatFeatureName(rawName)
            val importance = (factor["importance"] as? Number)?.toDouble() ?: 0.0
            val normalizedRatio = if (maxImportance > 0) (importance / maxImportance).toFloat().coerceIn(0.05f, 1f) else 0.5f

            var startAnimation by remember { mutableStateOf(false) }
            val animatedWidth by animateFloatAsState(
                targetValue = if (startAnimation) normalizedRatio else 0f,
                animationSpec = tween(durationMillis = 800)
            )

            LaunchedEffect(Unit) {
                startAnimation = true
            }

            val barColor = when {
                importance >= 0.25 -> RiskHigh
                importance >= 0.12 -> RiskMed
                else -> RiskLow
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.1f", importance * 100)}% impact",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = barColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = animatedWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(barColor)
                    )
                }
            }
        }
    }
}

private fun formatFeatureName(raw: String): String {
    return when (raw.lowercase()) {
        "cp" -> "Chest Pain Type"
        "trestbps" -> "Resting Blood Pressure"
        "chol" -> "Serum Cholesterol"
        "thalach" -> "Maximum Heart Rate"
        "oldpeak" -> "ST Depression (Oldpeak)"
        "exang" -> "Exercise Induced Angina"
        "ca" -> "Major Coronary Vessels"
        "thal" -> "Thalassemia Defect"
        "slope" -> "Peak Exercise ST Slope"
        "age" -> "Patient Age"
        "sex" -> "Gender Factor"
        "fbs" -> "Fasting Blood Sugar"
        "restecg" -> "Resting ECG"
        "smoking" -> "Smoking History"
        "obesity" -> "BMI / Obesity"
        "diabetes" -> "Diabetes Status"
        "hypertension" -> "Hypertension History"
        "family_history" -> "Cardiovascular Family History"
        "air_pollution_exposure" -> "Air Pollution Exposure"
        "chest_pain_severity" -> "Chest Pain Severity"
        else -> raw.replace("_", " ").replaceFirstChar { it.uppercase() }
    }
}
