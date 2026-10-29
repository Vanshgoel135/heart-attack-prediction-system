package com.haps.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haps.android.ui.theme.RiskHigh
import com.haps.android.ui.theme.RiskLow
import com.haps.android.ui.theme.RiskMed

@Composable
fun StatusPill(
    riskLevel: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor) = when {
        riskLevel.contains("High", ignoreCase = true) -> Triple(
            RiskHigh.copy(alpha = 0.15f),
            RiskHigh,
            RiskHigh
        )
        riskLevel.contains("Mod", ignoreCase = true) -> Triple(
            RiskMed.copy(alpha = 0.15f),
            RiskMed,
            RiskMed
        )
        else -> Triple(
            RiskLow.copy(alpha = 0.15f),
            RiskLow,
            RiskLow
        )
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = dotColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = riskLevel.uppercase(),
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
