package ru.orangesoftware.financisto.feature.account.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Bottom toolbar component that replicates the legacy bottom bar layout.
 * Contains add button, menu button (optional), and total balance text.
 */
@Composable
fun BottomToolbar(
    totalText: String,
    showMenuButton: Boolean,
    onAddClick: () -> Unit,
    onMenuClick: () -> Unit,
    onTotalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Add button (replicating bAdd from legacy layout)
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_input_add), // Using system add icon as placeholder
                    contentDescription = "Add Account",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Menu button (optional, replicating bMenu from legacy layout)
            if (showMenuButton) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_more), // Using system menu icon as placeholder
                        contentDescription = "Menu",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Spacer to push total text to the right
            Spacer(modifier = Modifier.weight(1f))
            
            // Total balance text (replicating total_text.xml layout)
            if (totalText.isNotEmpty()) {
                Text(
                    text = totalText,
                    modifier = Modifier
                        .clickable { onTotalClick() }
                        .padding(10.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
