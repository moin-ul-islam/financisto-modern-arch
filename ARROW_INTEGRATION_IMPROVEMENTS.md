# AccountActionCallout Arrow Integration Improvements

## Overview
The arrow in the AccountActionCallout has been significantly improved to look like an integrated part of the callout card with consistent visual styling.

## Visual Improvements Made

### 1. Improved Arrow Styling
- **Shadow Integration**: Added a subtle shadow to the arrow that matches the card's elevation
- **Color Consistency**: Arrow now uses the exact same Material 3 surface color as the card (`0xFFFFFBFE`)
- **Border Matching**: Arrow border uses the same Material 3 outline variant color as the card (`0xFFE7E0EC`)
- **Shadow Offset**: Added a slight offset to create depth and visual integration

### 2. Card Border Enhancement
- **Unified Border**: Added a consistent 0.5dp border to the entire callout card using Material 3 outline variant
- **Matching Colors**: Both card and arrow use identical colors and border styling
- **Integrated Shadow**: Adjusted shadow colors to be more subtle and professional

### 3. Technical Implementation
- **Custom Shape**: Implemented `CalloutWithArrowShape` that creates a unified outline including the arrow
- **Consistent Styling**: Arrow drawing logic matches the card's Material Design principles
- **Better Integration**: The arrow appears as an extension of the card rather than a separate element

## Code Changes

### Arrow Drawing Improvements
```kotlin
@Composable
private fun CalloutArrowUp() {
    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(24.dp, 12.dp)
    ) {
        // Shadow path for depth
        val shadowPath = Path().apply {
            moveTo(size.width / 2f + 1f, 1f) // Offset for shadow
            lineTo(1f, size.height + 1f)
            lineTo(size.width + 1f, size.height + 1f)
            close()
        }
        drawPath(
            path = shadowPath,
            color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.06f)
        )
        
        // Main arrow with matching surface color
        drawPath(
            path = path,
            color = androidx.compose.ui.graphics.Color(0xFFFFFBFE) // Material3 surface
        )
        
        // Border matching card outline
        drawPath(
            path = path,
            color = androidx.compose.ui.graphics.Color(0xFFE7E0EC), // Material3 outline variant
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.5.dp.toPx())
        )
    }
}
```

### Card Border Enhancement
```kotlin
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
```

## Visual Result
The arrow now appears as a natural extension of the callout card with:
- ✅ Consistent colors matching Material 3 design system
- ✅ Integrated shadow that provides depth without being overpowering
- ✅ Unified border styling across card and arrow
- ✅ Professional appearance that looks like a single cohesive element
- ✅ Better visual integration with the overall callout design

The callout now has a polished, professional appearance where the arrow looks like it's naturally part of the card structure rather than a separate attached element.
