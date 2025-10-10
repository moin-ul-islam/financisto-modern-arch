package ru.orangesoftware.financisto.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.orangesoftware.financisto.core.ui.theme.*

/**
 * Budget progress bar with color coding based on utilization
 */
@Composable
fun BudgetProgressBar(
    current: Float,
    total: Float,
    showLabels: Boolean = true,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) (current / total).coerceIn(0f, 1f) else 0f
    val progressColor = getProgressColor(progress)
    
    Column(modifier = modifier) {
        if (showLabels) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%.2f / %.2f", current, total),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = progressColor
                )
            }
            Spacer(modifier = Modifier.height(Spacing.XSmall))
        }
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Credit card utilization progress bar
 */
@Composable
fun CreditUtilizationBar(
    used: Float,
    limit: Float,
    modifier: Modifier = Modifier
) {
    val utilization = if (limit > 0) (used / limit).coerceIn(0f, 1f) else 0f
    val progressColor = getProgressColor(utilization)
    
    LinearProgressIndicator(
        progress = utilization,
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp),
        color = progressColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round
    )
}

/**
 * Determines progress bar color based on utilization percentage
 */
@Composable
private fun getProgressColor(progress: Float): Color {
    return when {
        progress < 0.7f -> ProgressLow
        progress < 0.9f -> ProgressMedium
        else -> ProgressHigh
    }
}
