package ru.orangesoftware.financisto.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.orangesoftware.financisto.core.ui.theme.GradientUtils
import ru.orangesoftware.financisto.core.ui.theme.IconSize

/**
 * Icon with a colored circular background
 * Used for account icons, category icons, etc.
 */
@Composable
fun IconWithBackground(
    painter: Painter,
    backgroundColor: Color,
    iconTint: Color = Color.White,
    size: Dp = IconSize.Large,
    iconSize: Dp = size * 0.6f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = GradientUtils.iconBackgroundGradient(backgroundColor)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = iconTint
        )
    }
}

/**
 * Icon with background using resource ID
 */
@Composable
fun IconWithBackground(
    iconRes: Int,
    backgroundColor: Color,
    iconTint: Color = Color.White,
    size: Dp = IconSize.Large,
    iconSize: Dp = size * 0.6f,
    modifier: Modifier = Modifier
) {
    IconWithBackground(
        painter = painterResource(id = iconRes),
        backgroundColor = backgroundColor,
        iconTint = iconTint,
        size = size,
        iconSize = iconSize,
        modifier = modifier
    )
}

/**
 * Simple circular icon background without icon (for custom content)
 */
@Composable
fun CircularBackground(
    backgroundColor: Color,
    size: Dp = IconSize.Large,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = GradientUtils.iconBackgroundGradient(backgroundColor)
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}
