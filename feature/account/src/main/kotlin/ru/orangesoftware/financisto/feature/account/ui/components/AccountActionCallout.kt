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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import ru.orangesoftware.financisto.feature.account.AccountActionCalloutItem

/**
 * Custom shape that creates a rounded rectangle with an upward-pointing arrow
 */
private class CalloutWithArrowShape(
    private val cornerRadius: androidx.compose.ui.unit.Dp,
    private val arrowWidth: androidx.compose.ui.unit.Dp,
    private val arrowHeight: androidx.compose.ui.unit.Dp,
    private val arrowOffset: androidx.compose.ui.unit.Dp
) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): androidx.compose.ui.graphics.Outline {
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }
        val arrowWidthPx = with(density) { arrowWidth.toPx() }
        val arrowHeightPx = with(density) { arrowHeight.toPx() }
        val arrowOffsetPx = with(density) { arrowOffset.toPx() }

        val path = Path().apply {
            // Start from top-left corner (accounting for arrow)
            moveTo(cornerRadiusPx, arrowHeightPx)
            
            // Top-left rounded corner
            quadraticBezierTo(0f, arrowHeightPx, 0f, arrowHeightPx + cornerRadiusPx)
            
            // Left edge
            lineTo(0f, size.height - cornerRadiusPx)
            
            // Bottom-left rounded corner
            quadraticBezierTo(0f, size.height, cornerRadiusPx, size.height)
            
            // Bottom edge
            lineTo(size.width - cornerRadiusPx, size.height)
            
            // Bottom-right rounded corner
            quadraticBezierTo(size.width, size.height, size.width, size.height - cornerRadiusPx)
            
            // Right edge
            lineTo(size.width, arrowHeightPx + cornerRadiusPx)
            
            // Top-right rounded corner
            quadraticBezierTo(size.width, arrowHeightPx, size.width - cornerRadiusPx, arrowHeightPx)
            
            // Top edge to arrow start
            lineTo(arrowOffsetPx + arrowWidthPx / 2f, arrowHeightPx)
            
            // Arrow point
            lineTo(arrowOffsetPx, 0f)
            
            // Arrow back to top edge
            lineTo(arrowOffsetPx - arrowWidthPx / 2f, arrowHeightPx)
            
            // Complete top edge
            lineTo(cornerRadiusPx, arrowHeightPx)
            
            close()
        }
        
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

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
    
    // Calculate better positioning with padding from edges
    val screenPadding = with(LocalDensity.current) { 16.dp.toPx().toInt() }
    
    // Adjust x position to keep callout on screen with padding to the left
    val adjustedX = (anchorBounds.x - 80).coerceAtLeast(screenPadding)
    
    Popup(
        offset = IntOffset(
            x = adjustedX,
            y = anchorBounds.y + 80 // Position callout below the account item
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
    // Create a unified callout with integrated arrow
    val calloutShape = CalloutWithArrowShape(
        cornerRadius = 16.dp,
        arrowWidth = 24.dp,
        arrowHeight = 12.dp,
        arrowOffset = 120.dp // Position of arrow from left edge
    )
    
    Surface(
        modifier = modifier
            .width(360.dp)
            .wrapContentHeight()
            .shadow(
                elevation = 12.dp,
                shape = calloutShape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .border(
                width = 0.5.dp,
                color = Color(0xFFE7E0EC), // Material3 outline variant
                shape = calloutShape
            ),
        shape = calloutShape,
        color = Color(0xFFFFFBFE), // Material3 surface color - same as arrow
        shadowElevation = 12.dp
    ) {
        ActionGrid(
            actions = actions,
            onActionClick = onActionClick,
            modifier = Modifier.padding(
                top = 28.dp, // Extra top padding for arrow
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
        )
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

@Composable
private fun CalloutArrowUp() {
    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(24.dp, 12.dp) // Slightly larger arrow
    ) {
        val path = Path().apply {
            moveTo(size.width / 2f, 0f) // Top center (point)
            lineTo(0f, size.height) // Bottom left
            lineTo(size.width, size.height) // Bottom right
            close()
        }
        
        // First draw the arrow shadow to match card elevation
        val shadowPath = Path().apply {
            moveTo(size.width / 2f + 1f, 1f) // Slightly offset for shadow effect
            lineTo(1f, size.height + 1f)
            lineTo(size.width + 1f, size.height + 1f)
            close()
        }
        drawPath(
            path = shadowPath,
            color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.06f)
        )
        
        // Draw the main arrow with Material surface color
        drawPath(
            path = path,
            color = androidx.compose.ui.graphics.Color(0xFFFFFBFE) // Material3 surface color
        )
        
        // Add a subtle border that matches the card's outline
        drawPath(
            path = path,
            color = androidx.compose.ui.graphics.Color(0xFFE7E0EC), // Material3 outline variant
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.5.dp.toPx())
        )
    }
}
