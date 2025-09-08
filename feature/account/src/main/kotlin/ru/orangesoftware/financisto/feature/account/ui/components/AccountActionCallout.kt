package ru.orangesoftware.financisto.feature.account.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import ru.orangesoftware.financisto.feature.account.AccountActionCalloutItem
import kotlin.math.roundToInt

/**
 * Account action callout dialog that displays a 3x3 grid of actions
 * with an arrow pointing to the selected account item.
 * 
 * This recreates the functionality of the QuickActionGrid from the legacy app
 * with modern Compose UI and callout-style design.
 */
@Composable
fun AccountActionCallout(
    actions: List<AccountActionCalloutItem>,
    isVisible: Boolean,
    anchorBounds: IntOffset,
    onActionClick: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    android.util.Log.d("AccountActionCallout", "Callout composable called - visible: $isVisible, actions: ${actions.size}")
    if (!isVisible) return
    
    Popup(
        offset = IntOffset(
            x = anchorBounds.x - 100, // Center callout horizontally on account
            y = anchorBounds.y - 50   // Position callout above the account item
        ),
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        CalloutContent(
            actions = actions,
            onActionClick = onActionClick,
            modifier = modifier
        )
    }
}

@Composable
private fun CalloutContent(
    actions: List<AccountActionCalloutItem>,
    onActionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.wrapContentSize()
    ) {
        // Main callout card
        Card(
            modifier = Modifier
                .width(240.dp)
                .wrapContentHeight()
                .shadow(8.dp, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            ActionGrid(
                actions = actions,
                onActionClick = onActionClick,
                modifier = Modifier.padding(12.dp)
            )
        }
        
        // Arrow pointing down to the selected account
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 108.dp), // Center the arrow horizontally
            contentAlignment = Alignment.TopCenter
        ) {
            CalloutArrow()
        }
    }
}

@Composable
private fun ActionGrid(
    actions: List<AccountActionCalloutItem>,
    onActionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Create 3x3 grid from actions list
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            .clickable { onClick() }
            .padding(4.dp),
        shape = RoundedCornerShape(6.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(id = action.iconResId),
                contentDescription = action.title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = action.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun CalloutArrow() {
    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(20.dp, 10.dp)
    ) {
        val path = Path().apply {
            moveTo(size.width / 2f, 0f) // Top center
            lineTo(0f, size.height) // Bottom left
            lineTo(size.width, size.height) // Bottom right
            close()
        }
        
        drawPath(
            path = path,
            color = androidx.compose.ui.graphics.Color.White
        )
        
        // Add a subtle border to the arrow
        drawPath(
            path = path,
            color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.3f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )
    }
}
