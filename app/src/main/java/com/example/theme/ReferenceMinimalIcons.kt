package com.example.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Minimalist native vector drawings for the "Reference Minimal" theme.
 * All icons are drawn with precise Canvas lines matching the reference keyboard.
 */
object ReferenceMinimalIcons {

    // ==========================================
    // TOP TOOLBAR CONTROLS (5 Controls)
    // ==========================================

    /**
     * 1. Grid/Apps icon: 4 clean squares in a 2x2 grid.
     */
    @Composable
    fun GridAppsIcon(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = Color(0xFF222222)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val boxSize = w * 0.36f
            val gap = w * 0.14f
            val corner = 2.dp.toPx()
            val strokeW = 1.6.dp.toPx()

            // Top-left
            drawRoundRect(
                color = tint,
                topLeft = Offset(0f, 0f),
                size = Size(boxSize, boxSize),
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(width = strokeW)
            )
            // Top-right
            drawRoundRect(
                color = tint,
                topLeft = Offset(boxSize + gap, 0f),
                size = Size(boxSize, boxSize),
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(width = strokeW)
            )
            // Bottom-left
            drawRoundRect(
                color = tint,
                topLeft = Offset(0f, boxSize + gap),
                size = Size(boxSize, boxSize),
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(width = strokeW)
            )
            // Bottom-right
            drawRoundRect(
                color = tint,
                topLeft = Offset(boxSize + gap, boxSize + gap),
                size = Size(boxSize, boxSize),
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(width = strokeW)
            )
        }
    }

    /**
     * 2. Emoji/Smiley icon: Clean minimalist round face with playful expression.
     */
    @Composable
    fun SmileyEmojiIcon(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = Color(0xFF222222)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = 1.6.dp.toPx()
            val radius = (w * 0.44f)

            // Outer circle
            drawCircle(
                color = tint,
                radius = radius,
                center = Offset(w * 0.5f, h * 0.5f),
                style = Stroke(width = strokeW)
            )

            // Left eye (dot)
            drawCircle(
                color = tint,
                radius = 1.8.dp.toPx(),
                center = Offset(w * 0.36f, h * 0.42f)
            )

            // Right eye (wink / angle: <)
            val winkPath = Path().apply {
                moveTo(w * 0.68f, h * 0.36f)
                lineTo(w * 0.60f, h * 0.42f)
                lineTo(w * 0.68f, h * 0.48f)
            }
            drawPath(
                path = winkPath,
                color = tint,
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Smile curve
            val smilePath = Path().apply {
                moveTo(w * 0.34f, h * 0.64f)
                quadraticTo(w * 0.50f, h * 0.76f, w * 0.66f, h * 0.64f)
            }
            drawPath(
                path = smilePath,
                color = tint,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }
    }

    /**
     * 3. Clipboard/Document icon: Clean rectangular document with text lines.
     */
    @Composable
    fun DocumentClipboardIcon(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = Color(0xFF222222)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = 1.6.dp.toPx()
            val corner = 2.5.dp.toPx()

            // Sheet outline
            drawRoundRect(
                color = tint,
                topLeft = Offset(w * 0.16f, h * 0.12f),
                size = Size(w * 0.68f, h * 0.76f),
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(width = strokeW)
            )

            // Line 1
            drawLine(
                color = tint,
                start = Offset(w * 0.30f, h * 0.34f),
                end = Offset(w * 0.70f, h * 0.34f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            // Line 2
            drawLine(
                color = tint,
                start = Offset(w * 0.30f, h * 0.50f),
                end = Offset(w * 0.70f, h * 0.50f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            // Line 3 (shorter)
            drawLine(
                color = tint,
                start = Offset(w * 0.30f, h * 0.66f),
                end = Offset(w * 0.54f, h * 0.66f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
    }

    /**
     * 4. Cursor Editing icon: < I > (text editing mode)
     */
    @Composable
    fun CursorEditIcon(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = Color(0xFF222222)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = 1.6.dp.toPx()

            // Left bracket <
            val leftBracket = Path().apply {
                moveTo(w * 0.28f, h * 0.28f)
                lineTo(w * 0.14f, h * 0.50f)
                lineTo(w * 0.28f, h * 0.72f)
            }
            drawPath(
                path = leftBracket,
                color = tint,
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Center vertical cursor bar |
            drawLine(
                color = tint,
                start = Offset(w * 0.50f, h * 0.20f),
                end = Offset(w * 0.50f, h * 0.80f),
                strokeWidth = 2.0.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Top serif
            drawLine(
                color = tint,
                start = Offset(w * 0.42f, h * 0.20f),
                end = Offset(w * 0.58f, h * 0.20f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            // Bottom serif
            drawLine(
                color = tint,
                start = Offset(w * 0.42f, h * 0.80f),
                end = Offset(w * 0.58f, h * 0.80f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Right bracket >
            val rightBracket = Path().apply {
                moveTo(w * 0.72f, h * 0.28f)
                lineTo(w * 0.86f, h * 0.50f)
                lineTo(w * 0.72f, h * 0.72f)
            }
            drawPath(
                path = rightBracket,
                color = tint,
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }

    /**
     * 5. Search icon: Crisp line magnifying glass.
     */
    @Composable
    fun SearchIcon(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = Color(0xFF222222)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = 1.6.dp.toPx()
            val radius = w * 0.30f
            val cx = w * 0.42f
            val cy = h * 0.42f

            // Circle lens
            drawCircle(
                color = tint,
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = strokeW)
            )

            // Angled handle
            val handleLen = w * 0.28f
            val angle = Math.toRadians(45.0)
            val startX = cx + (radius * Math.cos(angle)).toFloat()
            val startY = cy + (radius * Math.sin(angle)).toFloat()
            val endX = startX + (handleLen * Math.cos(angle)).toFloat()
            val endY = startY + (handleLen * Math.sin(angle)).toFloat()

            drawLine(
                color = tint,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }

    // ==========================================
    // KEYBOARD SPECIAL KEYS
    // ==========================================

    /**
     * Heart outline icon for Row 3 column 1: ♡
     */
    @Composable
    fun HeartOutlineIcon(
        modifier: Modifier = Modifier,
        size: Dp = 22.dp,
        tint: Color = Color(0xFF111111)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = 1.6.dp.toPx()

            val path = Path().apply {
                moveTo(w * 0.50f, h * 0.82f) // Bottom point
                cubicTo(
                    w * 0.16f, h * 0.58f,
                    w * 0.08f, h * 0.32f,
                    w * 0.26f, h * 0.20f
                )
                cubicTo(
                    w * 0.38f, h * 0.12f,
                    w * 0.48f, h * 0.24f,
                    w * 0.50f, h * 0.32f
                )
                cubicTo(
                    w * 0.52f, h * 0.24f,
                    w * 0.62f, h * 0.12f,
                    w * 0.74f, h * 0.20f
                )
                cubicTo(
                    w * 0.92f, h * 0.32f,
                    w * 0.84f, h * 0.58f,
                    w * 0.50f, h * 0.82f
                )
                close()
            }

            drawPath(
                path = path,
                color = tint,
                style = Stroke(width = strokeW, join = StrokeJoin.Round, cap = StrokeCap.Round)
            )
        }
    }

    /**
     * Speech bubble icon with lines for Row 4 column 1: 🗨
     */
    @Composable
    fun SpeechBubbleIcon(
        modifier: Modifier = Modifier,
        size: Dp = 22.dp,
        tint: Color = Color(0xFF111111)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = 1.6.dp.toPx()
            val corner = 3.dp.toPx()

            // Bubble body with small pointer at bottom-left
            val bubblePath = Path().apply {
                // Top-left
                moveTo(w * 0.15f + corner, h * 0.20f)
                lineTo(w * 0.85f - corner, h * 0.20f)
                quadraticTo(w * 0.85f, h * 0.20f, w * 0.85f, h * 0.20f + corner)
                // Right side
                lineTo(w * 0.85f, h * 0.66f - corner)
                quadraticTo(w * 0.85f, h * 0.66f, w * 0.85f - corner, h * 0.66f)
                // Bottom
                lineTo(w * 0.42f, h * 0.66f)
                lineTo(w * 0.24f, h * 0.82f) // Tail tip
                lineTo(w * 0.26f, h * 0.66f)
                lineTo(w * 0.15f + corner, h * 0.66f)
                quadraticTo(w * 0.15f, h * 0.66f, w * 0.15f, h * 0.66f - corner)
                // Left side
                lineTo(w * 0.15f, h * 0.20f + corner)
                quadraticTo(w * 0.15f, h * 0.20f, w * 0.15f + corner, h * 0.20f)
                close()
            }

            drawPath(
                path = bubblePath,
                color = tint,
                style = Stroke(width = strokeW, join = StrokeJoin.Round)
            )

            // Inner horizontal conversation lines
            drawLine(
                color = tint,
                start = Offset(w * 0.30f, h * 0.38f),
                end = Offset(w * 0.70f, h * 0.38f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = Offset(w * 0.30f, h * 0.50f),
                end = Offset(w * 0.58f, h * 0.50f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
    }

    /**
     * Minimalist Shift icon: Upward arrow with clean lines.
     */
    @Composable
    fun MinimalShiftIcon(
        modifier: Modifier = Modifier,
        size: Dp = 22.dp,
        isShifted: Boolean = false,
        isCapsLock: Boolean = false,
        tint: Color = Color(0xFF111111)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = 1.8.dp.toPx()

            val arrowPath = Path().apply {
                moveTo(w * 0.50f, h * 0.18f) // Top tip
                lineTo(w * 0.82f, h * 0.52f) // Right barb
                lineTo(w * 0.64f, h * 0.52f)
                lineTo(w * 0.64f, h * 0.82f)
                lineTo(w * 0.36f, h * 0.82f)
                lineTo(w * 0.36f, h * 0.52f)
                lineTo(w * 0.18f, h * 0.52f) // Left barb
                close()
            }

            if (isShifted || isCapsLock) {
                drawPath(path = arrowPath, color = tint, style = Fill)
                if (isCapsLock) {
                    // Underline bar for Caps Lock
                    drawLine(
                        color = tint,
                        start = Offset(w * 0.24f, h * 0.90f),
                        end = Offset(w * 0.76f, h * 0.90f),
                        strokeWidth = 2.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            } else {
                drawPath(
                    path = arrowPath,
                    color = tint,
                    style = Stroke(width = strokeW, join = StrokeJoin.Round, cap = StrokeCap.Round)
                )
            }
        }
    }

    /**
     * Minimalist Backspace icon: Clean backspace outline with 'X'.
     */
    @Composable
    fun MinimalBackspaceIcon(
        modifier: Modifier = Modifier,
        size: Dp = 22.dp,
        tint: Color = Color(0xFF111111)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = 1.6.dp.toPx()

            val tagPath = Path().apply {
                moveTo(w * 0.14f, h * 0.50f) // Left point
                lineTo(w * 0.40f, h * 0.20f)
                lineTo(w * 0.86f, h * 0.20f)
                quadraticTo(w * 0.90f, h * 0.20f, w * 0.90f, h * 0.24f)
                lineTo(w * 0.90f, h * 0.76f)
                quadraticTo(w * 0.90f, h * 0.80f, w * 0.86f, h * 0.80f)
                lineTo(w * 0.40f, h * 0.80f)
                close()
            }

            drawPath(
                path = tagPath,
                color = tint,
                style = Stroke(width = strokeW, join = StrokeJoin.Round)
            )

            // Inner 'X'
            val xCenter = w * 0.62f
            val yCenter = h * 0.50f
            val xDelta = w * 0.11f
            val yDelta = h * 0.12f

            drawLine(
                color = tint,
                start = Offset(xCenter - xDelta, yCenter - yDelta),
                end = Offset(xCenter + xDelta, yCenter + yDelta),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = Offset(xCenter + xDelta, yCenter - yDelta),
                end = Offset(xCenter - xDelta, yCenter + yDelta),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
    }

    /**
     * Minimalist Return / Enter icon: Return angle arrow ↵
     */
    @Composable
    fun MinimalEnterIcon(
        modifier: Modifier = Modifier,
        size: Dp = 22.dp,
        tint: Color = Color(0xFF111111)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = 1.8.dp.toPx()

            val enterPath = Path().apply {
                moveTo(w * 0.74f, h * 0.28f)
                lineTo(w * 0.74f, h * 0.56f)
                lineTo(w * 0.26f, h * 0.56f)
            }

            drawPath(
                path = enterPath,
                color = tint,
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Arrow head
            val arrowHead = Path().apply {
                moveTo(w * 0.40f, h * 0.42f)
                lineTo(w * 0.24f, h * 0.56f)
                lineTo(w * 0.40f, h * 0.70f)
            }
            drawPath(
                path = arrowHead,
                color = tint,
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}
