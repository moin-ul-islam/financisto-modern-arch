package ru.orangesoftware.financisto.feature.account.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Integrity error banner component that replicates the red error banner
 * from the legacy implementation. Shows at the top of the screen when
 * data integrity issues are detected.
 */
@Composable
fun IntegrityErrorBanner(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Red)
            .clickable { onDismiss() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Integrity Error", // This should be localized string resource
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}
