package ru.orangesoftware.financisto.feature.account.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.orangesoftware.financisto.feature.account.AccountActionCalloutItem

/**
 * Account action callout dialog that displays a 3x3 grid of actions.
 * 
 * This recreates the functionality of the QuickActionGrid from the legacy app
 * with modern Compose UI.
 */
@Composable
fun AccountActionCallout(
    actions: List<AccountActionCalloutItem>,
    onActionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ActionGrid(
        actions = actions,
        onActionClick = onActionClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
private fun ActionGrid(
    actions: List<AccountActionCalloutItem>,
    onActionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp) // Increased spacing
    ) {
        // Create 3x3 grid from actions list
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp) // Increased spacing
            ) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    if (index < actions.size) {
                        ActionItem(
                            action = actions[index],
                            onClick = { onActionClick(index) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Empty space to maintain grid layout
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionItem(
    action: AccountActionCalloutItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp), // Increased corner radius
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.padding(12.dp), // Increased padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp) // Increased spacing
        ) {
            Icon(
                painter = painterResource(id = action.iconResId),
                contentDescription = action.title,
                modifier = Modifier.size(28.dp), // Slightly larger icon
                tint = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = action.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp, // Slightly larger font
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1, // Force single line
                overflow = TextOverflow.Ellipsis // Handle overflow with ellipsis
            )
        }
    }
}
