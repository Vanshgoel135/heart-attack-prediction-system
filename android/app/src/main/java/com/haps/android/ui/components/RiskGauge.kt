package com.haps.android.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haps.android.ui.theme.RiskHigh
import com.haps.android.ui.theme.RiskLow
import com.haps.android.ui.theme.RiskMed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RiskGauge(
    probability: Double,
    riskLevel: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(probability) {
        animatedProgress.animateTo(
            targetValue = probability.toFloat().coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    val riskColor = when {
        riskLevel.contains("High", ignoreCase = true) || probability >= 0.70 -> RiskHigh
        riskLevel.contains("Mod", ignoreCase = true) || probability >= 0.35 -> RiskMed
        else -> RiskLow
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val strokeWidth = 22.dp.toPx()
            val diameter = minOf(size.width, size.height * 1.8f) - strokeWidth * 2
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset((size.width - diameter) / 2f, size.height - diameter / 2f - 20.dp.toPx())

            // 1. Background Arc (Semi-circle from 180 to 0 degrees, sweep = 180)
            drawArc(
                color = Color.LightGray.copy(alpha = 0.25f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Colored Gradient Arc (Green -> Amber -> Red)
            val gradientBrush = Brush.sweepGradient(
                0.5f to RiskLow,
                0.75f to RiskMed,
                1.0f to RiskHigh,
                center = Offset(topLeft.x + diameter / 2f, topLeft.y + diameter / 2f)
            )

            val sweepAngle = 180f * animatedProgress.value
            if (sweepAngle > 0f) {
                drawArc(
                    brush = gradientBrush,
                    startAngle = 180f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 3. Pointer Indicator Dot at Current Value
            val angleRad = Math.toRadians((180.0 + sweepAngle).toDouble())
            val radius = diameter / 2f
            val centerX = topLeft.x + radius
            val centerY = topLeft.y + radius
            val pointerX = centerX + radius * cos(angleRad).toFloat()
            val pointerY = centerY + radius * sin(angleRad).toFloat()

            drawCircle(
                color = Color.White,
                radius = strokeWidth * 0.45f,
                center = Offset(pointerX, pointerY)
            )
            drawCircle(
                color = riskColor,
                radius = strokeWidth * 0.30f,
                center = Offset(pointerX, pointerY)
            )
        }

        // Central text readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 20.dp)
        ) {
            val displayPercent = (animatedProgress.value * 100).toInt()
            Text(
                text = "$displayPercent%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = riskColor
            )
            StatusPill(riskLevel = riskLevel)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "CARDIAC RISK PROBABILITY",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
        }
    }
}
