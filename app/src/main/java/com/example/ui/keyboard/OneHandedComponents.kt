package com.example.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.OneHandedMode
import com.example.theme.KeyboardPalette

/**
 * Visual feedback banner shown when holding any alphabetic key for 5 seconds
 * to activate or deactivate One-Handed Mode.
 */
@Composable
fun OneHandedHoldProgressBar(
    progress: Float,
    isOneHandedActive: Boolean,
    palette: KeyboardPalette,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 60),
        label = "one_handed_progress"
    )

    AnimatedVisibility(
        visible = progress > 0f,
        enter = fadeIn(tween(100)),
        exit = fadeOut(tween(150)),
        modifier = modifier
    ) {
        val targetModeText = if (isOneHandedActive) "Exiting One-Handed Mode" else "Activating One-Handed Mode"
        val totalDots = 5
        val filledDots = (animatedProgress * totalDots).toInt().coerceIn(0, totalDots)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(palette.keyActionBackground)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✋",
                        fontSize = 14.sp
                    )
                    Text(
                        text = targetModeText,
                        color = palette.textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Dot progress indicator: • • • • •
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..totalDots) {
                        val isFilled = i <= filledDots
                        Box(
                            modifier = Modifier
                                .size(if (isFilled) 9.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) palette.accentColor
                                    else palette.textColor.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }

            // Bottom progress line
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(animatedProgress)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(palette.accentColor)
            )
        }
    }
}

/**
 * Side dock control bar for One-Handed Mode (Left/Right)
 */
@Composable
fun OneHandedSideDock(
    mode: OneHandedMode,
    palette: KeyboardPalette,
    onSwitchSide: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(46.dp)
            .fillMaxHeight()
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(palette.keyActionBackground.copy(alpha = 0.85f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Switch Side Button (Left <-> Right)
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(palette.keyBackground)
                .clickable { onSwitchSide() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⇆",
                color = palette.textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Expand / Normal Mode Button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(palette.keyBackground)
                .clickable { onExpand() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⛶",
                color = palette.textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Calculates ergonomic thumb curvature offsets for arc-like one-handed layout
 */
object OneHandedArcCalculator {
    fun getArcYOffset(
        rowIndex: Int,
        colIndex: Int,
        totalCols: Int,
        mode: OneHandedMode
    ): Dp {
        if (!mode.isOneHanded || totalCols <= 1) return 0.dp

        // Normalize position from 0.0 to 1.0 across the row
        val normalizedPos = colIndex.toFloat() / (totalCols - 1)

        // Arc curve formula: curve keys higher/lower depending on thumb pivot
        val curveIntensity = 3.5f // dp depth
        return when (mode) {
            OneHandedMode.RIGHT -> {
                // Right thumb pivot at bottom right: keys further left (col 0) curve downwards
                val factor = (1f - normalizedPos) * (1f - normalizedPos)
                (factor * curveIntensity).dp
            }
            OneHandedMode.LEFT -> {
                // Left thumb pivot at bottom left: keys further right curve downwards
                val factor = normalizedPos * normalizedPos
                (factor * curveIntensity).dp
            }
            OneHandedMode.NORMAL -> 0.dp
        }
    }
}
