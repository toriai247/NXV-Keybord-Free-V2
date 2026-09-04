package com.example.ui.keyboard

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.KeyboardMode
import com.example.keyboard.OneHandedMode
import com.example.keyboard.ShiftState
import com.example.suggestion.SuggestionItem
import com.example.theme.KeyboardPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Custom Radial One-Handed Arc Keyboard Layout with:
 * - Issue 1 Fix: Truncated predictive text inside centered pill bubbles.
 * - Issue 2 Fix: Strictly clipped bounds (Modifier.clipToBounds) with clean crisp background (no blurry overlays).
 * - Issue 3 Fix: Smooth distance/angle alpha transparency fading for outer text and keys blending with background.
 */
data class RadialKey(
    val id: String,
    val displayLabel: String = id,
    val isAlphabetic: Boolean = id.length == 1 && (id[0].isLetter() || id[0].code > 128),
    val isSpecial: Boolean = false
)

@Composable
fun CurvedArcKeyboardLayout(
    mode: OneHandedMode,
    currentLanguage: String,
    shiftState: ShiftState,
    suggestions: List<SuggestionItem>,
    palette: KeyboardPalette,
    enterLabel: String,
    oneHandedHeightDp: Int = 330,
    oneHandedTheme: String = "theme_match",
    oneHandedRotateText: Boolean = true,
    oneHandedArcScale: Float = 1.0f,
    oneHandedShowSuggestions: Boolean = true,
    oneHandedKeyStyle: String = "clean_arc",
    onCharTyped: (String) -> Unit,
    onDelete: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onShiftToggle: () -> Unit,
    onSwitchMode: (KeyboardMode) -> Unit,
    onLanguageCycle: () -> Unit,
    onSuggestionClicked: (SuggestionItem) -> Unit,
    onHoldProgressUpdate: (Float) -> Unit,
    onFiveSecondHoldComplete: () -> Unit,
    onHoldCancelled: () -> Unit,
    onSwitchSide: () -> Unit,
    onExpandNormal: () -> Unit,
    onUpdateOneHandedHeightDp: ((Int) -> Unit)? = null,
    onUpdateOneHandedTheme: ((String) -> Unit)? = null,
    onUpdateOneHandedRotateText: ((Boolean) -> Unit)? = null,
    onUpdateOneHandedArcScale: ((Float) -> Unit)? = null,
    onUpdateOneHandedShowSuggestions: ((Boolean) -> Unit)? = null,
    onUpdateOneHandedKeyStyle: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isRight = mode == OneHandedMode.RIGHT
    var activePressedKey by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showQuickSettings by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Determine color theme colors (No blur, no shadow, clean crisp contrast)
    val (bgColor, keyFillColor, textColor, specialTextColor, accentColor, strokeColor) = remember(oneHandedTheme, palette) {
        when (oneHandedTheme) {
            "light_crisp" -> HexColorSet(
                bg = Color(0xFFF8FAFC),
                keyFill = Color(0xFFE2E8F0),
                text = Color(0xFF0F172A),
                specialText = Color(0xFF1E293B),
                accent = Color(0xFF0284C7),
                stroke = Color(0xFFCBD5E1)
            )
            "amoled_dark" -> HexColorSet(
                bg = Color(0xFF000000),
                keyFill = Color(0xFF18181B),
                text = Color(0xFFFFFFFF),
                specialText = Color(0xFFE4E4E7),
                accent = Color(0xFF06B6D4),
                stroke = Color(0xFF27272A)
            )
            "rose_pastel" -> HexColorSet(
                bg = Color(0xFFFFF1F2),
                keyFill = Color(0xFFFFE4E6),
                text = Color(0xFF881337),
                specialText = Color(0xFF9F1239),
                accent = Color(0xFFE11D48),
                stroke = Color(0xFFFECDD3)
            )
            "sky_cyan" -> HexColorSet(
                bg = Color(0xFFECFEFF),
                keyFill = Color(0xFFCFFAFE),
                text = Color(0xFF0C4A6E),
                specialText = Color(0xFF0369A1),
                accent = Color(0xFF0D9488),
                stroke = Color(0xFFA5F3FC)
            )
            else -> HexColorSet(
                bg = palette.keyboardBackground,
                keyFill = palette.keyBackground,
                text = palette.textColor,
                specialText = palette.textColor.copy(alpha = 0.85f),
                accent = palette.accentColor,
                stroke = palette.dividerColor.copy(alpha = 0.4f)
            )
        }
    }

    // Row 1: QWERTY + Backspace (Left to Right)
    val row1Keys = remember(currentLanguage, shiftState) {
        when (currentLanguage) {
            "bangla" -> listOf("ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ", "⌫")
            else -> listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "⌫")
        }.map { label ->
            val finalLabel = if (label.length == 1 && label[0].isLetter() && shiftState.isUppercase) label.uppercase() else label
            RadialKey(id = finalLabel, displayLabel = if (label == "⌫") "←" else finalLabel, isSpecial = label == "⌫")
        }
    }

    // Row 2: ASDF + Enter (Left to Right)
    val row2Keys = remember(currentLanguage, shiftState) {
        when (currentLanguage) {
            "bangla" -> listOf("ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", enterLabel)
            else -> listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "↵")
        }.map { label ->
            val finalLabel = if (label.length == 1 && label[0].isLetter() && shiftState.isUppercase) label.uppercase() else label
            RadialKey(id = finalLabel, displayLabel = if (label == "↵" || label == enterLabel) "↵" else finalLabel, isSpecial = label == "↵" || label == enterLabel)
        }
    }

    // Row 3: Shift + ZXCV + Symbols (Left to Right)
    val row3Keys = remember(currentLanguage, shiftState) {
        when (currentLanguage) {
            "bangla" -> listOf("⇧", "ন", "প", "ফ", "ব", "ভ", "ম", "য", "র", "ল")
            else -> listOf("⇧", "z", "x", "c", "v", "b", "n", "m", "?!,", ".")
        }.map { label ->
            val finalLabel = if (label.length == 1 && label[0].isLetter() && shiftState.isUppercase) label.uppercase() else label
            RadialKey(id = finalLabel, displayLabel = if (label == "⇧") "↑" else finalLabel, isSpecial = label == "⇧" || label == "?!,")
        }
    }

    // Row 4: Inner Arc Controls (Emoji, Symbols, Space, Language)
    val row4Keys = listOf(
        RadialKey(id = "😃", displayLabel = "😃", isAlphabetic = false, isSpecial = true),
        RadialKey(id = "?123", displayLabel = "?123", isAlphabetic = false, isSpecial = true),
        RadialKey(id = "space", displayLabel = "space", isAlphabetic = false, isSpecial = true),
        RadialKey(id = "🌐", displayLabel = "🌐", isAlphabetic = false, isSpecial = true)
    )

    // Truncate suggestions to 3-4 letters max ("How", "Tha", "Hel")
    val displaySuggestions = remember(suggestions, oneHandedShowSuggestions) {
        if (!oneHandedShowSuggestions) emptyList()
        else {
            val rawList = if (suggestions.isNotEmpty()) suggestions.take(4) else listOf(
                SuggestionItem(displayText = "His", replacementText = "His"),
                SuggestionItem(displayText = "How", replacementText = "How"),
                SuggestionItem(displayText = "Thanks", replacementText = "Thanks"),
                SuggestionItem(displayText = "Hello", replacementText = "Hello")
            )
            rawList.map { item ->
                val truncatedText = if (item.displayText.length > 4) item.displayText.take(3) + "…" else item.displayText
                item.copy(displayText = truncatedText)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(oneHandedHeightDp.dp)
            .clipToBounds()
            .background(bgColor)
    ) {
        // Floating Dock Controls (⇆ Switch Side, ⚙️ One-Handed Settings, and ⛶ Expand)
        val dockAlignment = if (isRight) Alignment.TopStart else Alignment.TopEnd
        Row(
            modifier = Modifier
                .align(dockAlignment)
                .padding(10.dp)
                .clip(CircleShape)
                .background(keyFillColor)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgColor)
                    .clickable { onSwitchSide() },
                contentAlignment = Alignment.Center
            ) {
                Text("⇆", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgColor)
                    .clickable { showQuickSettings = true },
                contentAlignment = Alignment.Center
            ) {
                Text("⚙️", color = textColor, fontSize = 16.sp)
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgColor)
                    .clickable { onExpandNormal() },
                contentAlignment = Alignment.Center
            ) {
                Text("⛶", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Curved Canvas Layout with Issue 2 Fix (clipToBounds)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(row1Keys, row2Keys, row3Keys, displaySuggestions, mode, shiftState, oneHandedArcScale, oneHandedShowSuggestions) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()

                        // Pivot coordinates
                        val px = if (isRight) w * 1.06f else -w * 0.06f
                        val py = h * 1.12f

                        val dx = down.position.x - px
                        val dy = py - down.position.y
                        val dist = sqrt(dx * dx + dy * dy)
                        var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        if (angleDeg < 0) angleDeg += 360f

                        val maxR = max(w, h) * 1.18f * oneHandedArcScale

                        // Radial Band Boundaries
                        val rSuggIn = maxR * 0.83f
                        val rSuggOut = maxR * 0.98f

                        val r1In = maxR * 0.67f
                        val r1Out = maxR * 0.82f

                        val r2In = maxR * 0.51f
                        val r2Out = maxR * 0.66f

                        val r3In = maxR * 0.35f
                        val r3Out = maxR * 0.50f

                        val r4In = maxR * 0.17f
                        val r4Out = maxR * 0.33f

                        val minAngle = if (isRight) 95f else 5f
                        val maxAngle = if (isRight) 175f else 85f
                        val sweepTotal = maxAngle - minAngle

                        var hitRow = -1
                        var hitCol = -1
                        var hitKey: RadialKey? = null
                        var hitSugg: SuggestionItem? = null

                        if (angleDeg in minAngle..maxAngle) {
                            when {
                                displaySuggestions.isNotEmpty() && dist in rSuggIn..rSuggOut -> {
                                    hitRow = 0
                                    val norm = if (isRight) (maxAngle - angleDeg) / sweepTotal else (angleDeg - minAngle) / sweepTotal
                                    hitCol = (norm * displaySuggestions.size).toInt().coerceIn(0, displaySuggestions.size - 1)
                                    hitSugg = displaySuggestions[hitCol]
                                }
                                dist in r1In..r1Out -> {
                                    hitRow = 1
                                    val norm = if (isRight) (maxAngle - angleDeg) / sweepTotal else (angleDeg - minAngle) / sweepTotal
                                    hitCol = (norm * row1Keys.size).toInt().coerceIn(0, row1Keys.size - 1)
                                    hitKey = row1Keys[hitCol]
                                }
                                dist in r2In..r2Out -> {
                                    hitRow = 2
                                    val norm = if (isRight) (maxAngle - angleDeg) / sweepTotal else (angleDeg - minAngle) / sweepTotal
                                    hitCol = (norm * row2Keys.size).toInt().coerceIn(0, row2Keys.size - 1)
                                    hitKey = row2Keys[hitCol]
                                }
                                dist in r3In..r3Out -> {
                                    hitRow = 3
                                    val norm = if (isRight) (maxAngle - angleDeg) / sweepTotal else (angleDeg - minAngle) / sweepTotal
                                    hitCol = (norm * row3Keys.size).toInt().coerceIn(0, row3Keys.size - 1)
                                    hitKey = row3Keys[hitCol]
                                }
                                dist in r4In..r4Out -> {
                                    hitRow = 4
                                    val norm = if (isRight) (maxAngle - angleDeg) / sweepTotal else (angleDeg - minAngle) / sweepTotal
                                    hitCol = (norm * row4Keys.size).toInt().coerceIn(0, row4Keys.size - 1)
                                    hitKey = row4Keys[hitCol]
                                }
                            }
                        }

                        if (hitRow >= 0 && hitCol >= 0) {
                            activePressedKey = Pair(hitRow, hitCol)
                        }

                        val total5sMs = 5000L
                        var is5sCompleted = false

                        val timerJob = coroutineScope.launch {
                            if (hitKey?.isAlphabetic == true) {
                                val updateInterval = 50L
                                var elapsed = 0L
                                while (isActive && elapsed < total5sMs) {
                                    delay(updateInterval)
                                    elapsed += updateInterval
                                    if (elapsed >= 300L) {
                                        val progress = ((elapsed - 300L).toFloat() / (total5sMs - 300L)).coerceIn(0f, 1f)
                                        onHoldProgressUpdate(progress)
                                    }
                                    if (elapsed >= total5sMs) {
                                        is5sCompleted = true
                                        onFiveSecondHoldComplete()
                                        onHoldCancelled()
                                        break
                                    }
                                }
                            }
                        }

                        // Wait for UP event
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                timerJob.cancel()
                                onHoldCancelled()
                                activePressedKey = null

                                if (!is5sCompleted) {
                                    if (hitSugg != null) {
                                        onSuggestionClicked(hitSugg)
                                    } else if (hitKey != null) {
                                        when (hitKey.id) {
                                            "⌫", "←" -> onDelete()
                                            "⇧", "↑" -> onShiftToggle()
                                            "↵", enterLabel -> onEnter()
                                            "space" -> onSpace()
                                            "?123" -> onSwitchMode(KeyboardMode.NUMBERS)
                                            "😃" -> onSwitchMode(KeyboardMode.EMOJI)
                                            "🌐" -> onLanguageCycle()
                                            else -> onCharTyped(hitKey.id)
                                        }
                                    }
                                }
                                break
                            }
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            val px = if (isRight) w * 1.06f else -w * 0.06f
            val py = h * 1.12f

            val maxR = max(w, h) * 1.18f * oneHandedArcScale

            // Radial Band Boundaries
            val rSuggIn = maxR * 0.83f
            val rSuggOut = maxR * 0.98f

            val r1In = maxR * 0.67f
            val r1Out = maxR * 0.82f

            val r2In = maxR * 0.51f
            val r2Out = maxR * 0.66f

            val r3In = maxR * 0.35f
            val r3Out = maxR * 0.50f

            val r4In = maxR * 0.17f
            val r4Out = maxR * 0.33f

            val minAngle = if (isRight) 95f else 5f
            val maxAngle = if (isRight) 175f else 85f
            val sweepTotal = maxAngle - minAngle

            // Clean, non-blurry, crisp background fill
            drawCircle(
                color = keyFillColor.copy(alpha = 0.5f),
                radius = maxR * 0.98f,
                center = Offset(px, py)
            )

            val textPaint = Paint().apply {
                color = textColor.toArgb()
                textSize = 19.dp.toPx()
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val specialPaint = Paint().apply {
                color = specialTextColor.toArgb()
                textSize = 16.dp.toPx()
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val suggPaint = Paint().apply {
                color = textColor.toArgb()
                textSize = 14.dp.toPx()
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            // Function to draw arc separator line
            fun drawArcSeparator(r: Float) {
                drawArc(
                    color = strokeColor,
                    startAngle = -maxAngle,
                    sweepAngle = sweepTotal,
                    useCenter = false,
                    topLeft = Offset(px - r, py - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            // Draw arc separators
            if (displaySuggestions.isNotEmpty()) {
                drawArcSeparator(rSuggOut)
                drawArcSeparator(rSuggIn)
            }
            drawArcSeparator(r1In)
            drawArcSeparator(r2In)
            drawArcSeparator(r3In)
            drawArcSeparator(r4In)

            // Helper function for Issue 3: Distance and angle-based alpha transparency fading
            fun calculateAlphaFade(aMid: Float): Float {
                val angleFromCenter = if (isRight) (maxAngle - aMid) / sweepTotal else (aMid - minAngle) / sweepTotal
                return (1.0f - (angleFromCenter * 0.70f)).coerceIn(0.15f, 1.0f)
            }

            // Function to draw ring keys
            fun drawRingKeys(
                keys: List<RadialKey>,
                rIn: Float,
                rOut: Float,
                rowIndex: Int
            ) {
                val stepAngle = sweepTotal / keys.size
                val rMid = (rIn + rOut) / 2f

                for (i in keys.indices) {
                    val a1 = if (isRight) maxAngle - i * stepAngle else minAngle + i * stepAngle
                    val a2 = if (isRight) a1 - stepAngle else a1 + stepAngle
                    val aMid = (a1 + a2) / 2f

                    val isPressed = activePressedKey == Pair(rowIndex, i)

                    // Issue 3: Alpha Transparency Fading based on angle distance
                    val alphaFade = calculateAlphaFade(aMid)

                    // Draw touch press highlight
                    if (isPressed) {
                        val path = Path().apply {
                            val startA = if (isRight) -a1 else -a2
                            val sweepA = if (isRight) (a1 - a2) else (a2 - a1)
                            arcTo(
                                rect = Rect(px - rOut, py - rOut, px + rOut, py + rOut),
                                startAngleDegrees = startA,
                                sweepAngleDegrees = sweepA,
                                forceMoveTo = true
                            )
                            arcTo(
                                rect = Rect(px - rIn, py - rIn, px + rIn, py + rIn),
                                startAngleDegrees = startA + sweepA,
                                sweepAngleDegrees = -sweepA,
                                forceMoveTo = false
                            )
                            close()
                        }
                        drawPath(path, color = accentColor.copy(alpha = 0.40f * alphaFade))
                    }

                    // Key Label Position
                    val kx = px + rMid * cos(Math.toRadians(aMid.toDouble())).toFloat()
                    val ky = py - rMid * sin(Math.toRadians(aMid.toDouble())).toFloat()

                    drawContext.canvas.nativeCanvas.save()
                    drawContext.canvas.nativeCanvas.translate(kx, ky)

                    // Tangent rotation along the arc (if enabled)
                    val rot = if (oneHandedRotateText) {
                        if (isRight) (135f - aMid) * 0.45f else (aMid - 45f) * 0.45f
                    } else 0f

                    drawContext.canvas.nativeCanvas.rotate(rot)

                    val key = keys[i]
                    val activePaint = if (key.isSpecial) specialPaint else textPaint
                    activePaint.textSize = if (key.displayLabel.length > 2) 15.dp.toPx() else 19.dp.toPx()
                    
                    val baseColor = if (key.isSpecial) specialTextColor else textColor
                    activePaint.color = baseColor.copy(alpha = alphaFade).toArgb()

                    drawContext.canvas.nativeCanvas.drawText(key.displayLabel, 0f, 6.dp.toPx(), activePaint)

                    drawContext.canvas.nativeCanvas.restore()
                }
            }

            // Draw Suggestion Arc Ring with Circular Bubble Pills & Transparency Fading
            if (displaySuggestions.isNotEmpty()) {
                val suggStep = sweepTotal / displaySuggestions.size
                val rSuggMid = (rSuggIn + rSuggOut) / 2f
                for (i in displaySuggestions.indices) {
                    val a1 = if (isRight) maxAngle - i * suggStep else minAngle + i * suggStep
                    val a2 = if (isRight) a1 - suggStep else a1 + suggStep
                    val aMid = (a1 + a2) / 2f
                    val isPressed = activePressedKey == Pair(0, i)

                    val alphaFade = calculateAlphaFade(aMid)

                    val kx = px + rSuggMid * cos(Math.toRadians(aMid.toDouble())).toFloat()
                    val ky = py - rSuggMid * sin(Math.toRadians(aMid.toDouble())).toFloat()

                    drawContext.canvas.nativeCanvas.save()
                    drawContext.canvas.nativeCanvas.translate(kx, ky)

                    val rot = if (oneHandedRotateText) {
                        if (isRight) (135f - aMid) * 0.45f else (aMid - 45f) * 0.45f
                    } else 0f

                    drawContext.canvas.nativeCanvas.rotate(rot)

                    val bubbleW = 54.dp.toPx()
                    val bubbleH = 26.dp.toPx()

                    val bubbleBgColor = if (isPressed) {
                        accentColor.copy(alpha = 0.65f * alphaFade)
                    } else {
                        keyFillColor.copy(alpha = 0.90f * alphaFade)
                    }

                    // Bubble fill
                    drawRoundRect(
                        color = bubbleBgColor,
                        topLeft = Offset(-bubbleW / 2f, -bubbleH / 2f),
                        size = Size(bubbleW, bubbleH),
                        cornerRadius = CornerRadius(13.dp.toPx(), 13.dp.toPx())
                    )

                    // Bubble subtle outline
                    drawRoundRect(
                        color = strokeColor.copy(alpha = 0.40f * alphaFade),
                        topLeft = Offset(-bubbleW / 2f, -bubbleH / 2f),
                        size = Size(bubbleW, bubbleH),
                        cornerRadius = CornerRadius(13.dp.toPx(), 13.dp.toPx()),
                        style = Stroke(width = 1.2.dp.toPx())
                    )

                    suggPaint.color = textColor.copy(alpha = alphaFade).toArgb()
                    drawContext.canvas.nativeCanvas.drawText(
                        displaySuggestions[i].displayText,
                        0f,
                        5.dp.toPx(),
                        suggPaint
                    )

                    drawContext.canvas.nativeCanvas.restore()
                }
            }

            // Draw Keyboard Rings 1 to 4
            drawRingKeys(row1Keys, r1In, r1Out, 1)
            drawRingKeys(row2Keys, r2In, r2Out, 2)
            drawRingKeys(row3Keys, r3In, r3Out, 3)
            drawRingKeys(row4Keys, r4In, r4Out, 4)
        }

        // Quick One-Handed Settings Floating Overlay Modal
        if (showQuickSettings) {
            OneHandedQuickSettingsSheet(
                oneHandedHeightDp = oneHandedHeightDp,
                oneHandedTheme = oneHandedTheme,
                oneHandedRotateText = oneHandedRotateText,
                oneHandedArcScale = oneHandedArcScale,
                oneHandedShowSuggestions = oneHandedShowSuggestions,
                oneHandedKeyStyle = oneHandedKeyStyle,
                onDismiss = { showQuickSettings = false },
                onUpdateHeightDp = { onUpdateOneHandedHeightDp?.invoke(it) },
                onUpdateTheme = { onUpdateOneHandedTheme?.invoke(it) },
                onUpdateRotateText = { onUpdateOneHandedRotateText?.invoke(it) },
                onUpdateArcScale = { onUpdateOneHandedArcScale?.invoke(it) },
                onUpdateShowSuggestions = { onUpdateOneHandedShowSuggestions?.invoke(it) },
                onUpdateKeyStyle = { onUpdateOneHandedKeyStyle?.invoke(it) }
            )
        }
    }
}

private data class HexColorSet(
    val bg: Color,
    val keyFill: Color,
    val text: Color,
    val specialText: Color,
    val accent: Color,
    val stroke: Color
)

@Composable
private fun OneHandedQuickSettingsSheet(
    oneHandedHeightDp: Int,
    oneHandedTheme: String,
    oneHandedRotateText: Boolean,
    oneHandedArcScale: Float,
    oneHandedShowSuggestions: Boolean,
    oneHandedKeyStyle: String,
    onDismiss: () -> Unit,
    onUpdateHeightDp: (Int) -> Unit,
    onUpdateTheme: (String) -> Unit,
    onUpdateRotateText: (Boolean) -> Unit,
    onUpdateArcScale: (Float) -> Unit,
    onUpdateShowSuggestions: (Boolean) -> Unit,
    onUpdateKeyStyle: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                .clickable(enabled = false) {}
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️ One-Handed Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Height Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Keyboard Size (Height)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("${oneHandedHeightDp}dp", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                    }
                    androidx.compose.material3.Slider(
                        value = oneHandedHeightDp.toFloat(),
                        onValueChange = { onUpdateHeightDp(it.toInt()) },
                        valueRange = 260f..380f,
                        steps = 5
                    )
                }

                // Arc Scale Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Thumb Reach (Arc Scale)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("${(oneHandedArcScale * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                    }
                    androidx.compose.material3.Slider(
                        value = oneHandedArcScale,
                        onValueChange = { onUpdateArcScale(it) },
                        valueRange = 0.80f..1.25f,
                        steps = 8
                    )
                }

                // Color Theme Chips
                Text("Color Theme", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val themes = listOf(
                        "theme_match" to "Match",
                        "light_crisp" to "Light",
                        "amoled_dark" to "AMOLED",
                        "rose_pastel" to "Rose",
                        "sky_cyan" to "Sky"
                    )
                    themes.forEach { (id, label) ->
                        val isSelected = oneHandedTheme == id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                .background(if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onUpdateTheme(id) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.onPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Rotate Text Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rotate Letters on Arc", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(if (oneHandedRotateText) "Rotated along curve" else "Straight upright text", fontSize = 11.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    androidx.compose.material3.Switch(
                        checked = oneHandedRotateText,
                        onCheckedChange = { onUpdateRotateText(it) }
                    )
                }

                // Show Suggestions Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Predictive Word Bubbles", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    androidx.compose.material3.Switch(
                        checked = oneHandedShowSuggestions,
                        onCheckedChange = { onUpdateShowSuggestions(it) }
                    )
                }
            }
        }
    }
}
