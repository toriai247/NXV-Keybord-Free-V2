package com.example.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * RGB Chroma Neon Spectrum Palette and Vector Icons:
 * Inspired by the RGB Neon Rainbow Cyber Keyboard reference design.
 * Features 10 vivid neon spectrum stops matching the 10 horizontal key columns.
 */
object RgbSpectrumUtils {

    // 10 Vibrant Rainbow Neon Colors (Columns 0 to 9)
    val SPECTRUM_COLORS = listOf(
        Color(0xFF39FF14), // Col 0: Neon Lime Green (q, a, shift, 123)
        Color(0xFF2DD4BF), // Col 1: Mint Aqua (w, s, z)
        Color(0xFF00F0FF), // Col 2: Electric Cyan (e, d, x, globe)
        Color(0xFF38BDF8), // Col 3: Sky / Electric Blue (r, f, c, emoji)
        Color(0xFF818CF8), // Col 4: Indigo / Purple (t, g, v)
        Color(0xFFA855F7), // Col 5: Violet / Purple (y, h, b)
        Color(0xFFEC4899), // Col 6: Magenta / Hot Pink (u, j, n)
        Color(0xFFEF4444), // Col 7: Coral / Neon Red (i, k, m, space)
        Color(0xFFF97316), // Col 8: Vivid Orange / Amber (o, l)
        Color(0xFFA3E635)  // Col 9: Neon Chartreuse / Lime (p, backspace, return)
    )

    private val KEY_COLUMN_MAP: Map<String, Int> = mapOf(
        // Row 1
        "q" to 0, "w" to 1, "e" to 2, "r" to 3, "t" to 4, "y" to 5, "u" to 6, "i" to 7, "o" to 8, "p" to 9,
        "1" to 0, "2" to 1, "3" to 2, "4" to 3, "5" to 4, "6" to 5, "7" to 6, "8" to 7, "9" to 8, "0" to 9,

        // Row 2
        "a" to 0, "s" to 1, "d" to 2, "f" to 3, "g" to 4, "h" to 5, "j" to 6, "k" to 7, "l" to 8,

        // Row 3
        "shift" to 0, "⇧" to 0, "↑" to 0, "⇪" to 0,
        "z" to 1, "x" to 2, "c" to 3, "v" to 4, "b" to 5, "n" to 6, "m" to 7,
        "⌫" to 9, "backspace" to 9, "delete" to 9,

        // Row 4
        "123" to 0, "?123" to 0, "#+=" to 0,
        "🌐" to 2, "lang" to 2,
        "😊" to 3, "emoji" to 3, "smiley" to 3,
        "space" to 7, "SPACE" to 7, " " to 7,
        "return" to 9, "enter" to 9, "↵" to 9, "go" to 9, "search" to 9, "done" to 9
    )

    fun getColorForKey(label: String, indexInRow: Int = -1, totalInRow: Int = 10): Color {
        val cleanLabel = label.lowercase().trim()
        val mappedCol = KEY_COLUMN_MAP[cleanLabel]
        if (mappedCol != null) {
            return SPECTRUM_COLORS[mappedCol.coerceIn(0, SPECTRUM_COLORS.lastIndex)]
        }

        if (indexInRow >= 0 && totalInRow > 1) {
            val normalizedFraction = indexInRow.toFloat() / (totalInRow - 1).toFloat()
            val colorIndex = (normalizedFraction * (SPECTRUM_COLORS.size - 1)).toInt().coerceIn(0, SPECTRUM_COLORS.lastIndex)
            return SPECTRUM_COLORS[colorIndex]
        }

        // Fallback: hash based deterministic spectrum mapping
        val hash = kotlin.math.abs(cleanLabel.hashCode())
        return SPECTRUM_COLORS[hash % SPECTRUM_COLORS.size]
    }

    /**
     * Outlined Neon Shift Arrow matching the reference design.
     */
    @Composable
    fun RgbShiftIcon(
        modifier: Modifier = Modifier,
        size: Dp = 22.dp,
        color: Color = Color(0xFF39FF14),
        isShifted: Boolean = false,
        isCapsLock: Boolean = false
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            val arrowPath = Path().apply {
                // Top arrowhead tip
                moveTo(w * 0.5f, h * 0.15f)
                lineTo(w * 0.85f, h * 0.52f)
                lineTo(w * 0.65f, h * 0.52f)
                lineTo(w * 0.65f, h * 0.85f)
                lineTo(w * 0.35f, h * 0.85f)
                lineTo(w * 0.35f, h * 0.52f)
                lineTo(w * 0.15f, h * 0.52f)
                close()
            }

            if (isShifted || isCapsLock) {
                drawPath(path = arrowPath, color = color)
                if (isCapsLock) {
                    // Underline bar for CapsLock
                    drawLine(
                        color = color,
                        start = Offset(w * 0.25f, h * 0.95f),
                        end = Offset(w * 0.75f, h * 0.95f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            } else {
                drawPath(
                    path = arrowPath,
                    color = color,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }

    /**
     * Outlined Neon Backspace Tag with 'X' matching the reference design.
     */
    @Composable
    fun RgbBackspaceIcon(
        modifier: Modifier = Modifier,
        size: Dp = 22.dp,
        color: Color = Color(0xFFA3E635)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            val tagPath = Path().apply {
                moveTo(w * 0.18f, h * 0.50f) // Left point
                lineTo(w * 0.44f, h * 0.20f) // Top slope
                lineTo(w * 0.86f, h * 0.20f) // Top flat
                lineTo(w * 0.86f, h * 0.80f) // Right flat
                lineTo(w * 0.44f, h * 0.80f) // Bottom slope
                close()
            }

            drawPath(
                path = tagPath,
                color = color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 'X' inside
            val xCenter = w * 0.62f
            val yCenter = h * 0.50f
            val arm = w * 0.11f

            drawLine(
                color = color,
                start = Offset(xCenter - arm, yCenter - arm),
                end = Offset(xCenter + arm, yCenter + arm),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(xCenter + arm, yCenter - arm),
                end = Offset(xCenter - arm, yCenter + arm),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }

    /**
     * Outlined Neon Globe wireframe matching reference design.
     */
    @Composable
    fun RgbGlobeIcon(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        color: Color = Color(0xFF00F0FF)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeWidth = 1.8.dp.toPx()

            // Outer circle
            drawCircle(
                color = color,
                radius = w * 0.44f,
                center = Offset(w * 0.5f, h * 0.5f),
                style = Stroke(width = strokeWidth)
            )

            // Horizontal equator
            drawLine(
                color = color,
                start = Offset(w * 0.08f, h * 0.5f),
                end = Offset(w * 0.92f, h * 0.5f),
                strokeWidth = strokeWidth * 0.85f
            )

            // Vertical oval (meridian)
            drawOval(
                color = color,
                topLeft = Offset(w * 0.28f, h * 0.08f),
                size = Size(w * 0.44f, h * 0.84f),
                style = Stroke(width = strokeWidth * 0.85f)
            )
        }
    }

    /**
     * Outlined Neon Emoji Smiley face matching reference design.
     */
    @Composable
    fun RgbEmojiIcon(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        color: Color = Color(0xFFA855F7)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeWidth = 1.8.dp.toPx()

            // Outer head circle
            drawCircle(
                color = color,
                radius = w * 0.44f,
                center = Offset(w * 0.5f, h * 0.5f),
                style = Stroke(width = strokeWidth)
            )

            // Eyes
            drawCircle(
                color = color,
                radius = 1.5.dp.toPx(),
                center = Offset(w * 0.35f, h * 0.40f)
            )
            drawCircle(
                color = color,
                radius = 1.5.dp.toPx(),
                center = Offset(w * 0.65f, h * 0.40f)
            )

            // Smiling arc
            drawArc(
                color = color,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * 0.30f, h * 0.42f),
                size = Size(w * 0.40f, h * 0.35f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }

    /**
     * Outlined Neon Return / Enter matching reference design.
     */
    @Composable
    fun RgbReturnKeyContent(
        label: String = "return",
        color: Color = Color(0xFFA3E635),
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (label.lowercase() in listOf("↵", "enter", "return", "go", "done", "search")) "return" else label,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}
