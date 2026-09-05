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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
 * Microsoft Word Flow-inspired One-Handed Arc Keyboard Layout:
 * - Concentric arc tracks for Suggestions, Row 1 (QWERTY), Row 2 (ASDF), Row 3 (ZXCV), and Row 4 (Space/Controls).
 * - Exact Tangent/Normal letter rotation so all characters face perpendicular to thumb reach line-of-sight.
 * - Pristine curved divider lines and soft radiating fan gradient.
 * - Accurate polar coordinate hit detection (Radius & Angle).
 * - Multi-language support (English, Bangla, Avro).
 */
data class RadialKey(
    val id: String,
    val displayLabel: String = id,
    val isAlphabetic: Boolean = id.length == 1 && (id[0].isLetter() || id[0].code > 128),
    val isSpecial: Boolean = false,
    val weight: Float = 1.0f
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

    // Determine color theme colors with crystal clear contrast & soft fan gradients
    val (bgColor, keyFillColor, textColor, specialTextColor, accentColor, strokeColor) = remember(oneHandedTheme, palette) {
        when (oneHandedTheme) {
            "light_crisp" -> HexColorSet(
                bg = Color(0xFFF1F5F9),
                keyFill = Color(0xFFE2E8F0),
                text = Color(0xFF0F172A),
                specialText = Color(0xFF334155),
                accent = Color(0xFF0284C7),
                stroke = Color(0xFF94A3B8)
            )
            "amoled_dark" -> HexColorSet(
                bg = Color(0xFF050505),
                keyFill = Color(0xFF18181B),
                text = Color(0xFFFAFAFA),
                specialText = Color(0xFFA1A1AA),
                accent = Color(0xFF06B6D4),
                stroke = Color(0xFF3F3F46)
            )
            "rose_pastel" -> HexColorSet(
                bg = Color(0xFFFFF1F2),
                keyFill = Color(0xFFFFE4E6),
                text = Color(0xFF881337),
                specialText = Color(0xFF9F1239),
                accent = Color(0xFFE11D48),
                stroke = Color(0xFFFDA4AF)
            )
            "sky_cyan" -> HexColorSet(
                bg = Color(0xFFECFEFF),
                keyFill = Color(0xFFCFFAFE),
                text = Color(0xFF0C4A6E),
                specialText = Color(0xFF0369A1),
                accent = Color(0xFF0D9488),
                stroke = Color(0xFF7DD3FC)
            )
            else -> HexColorSet(
                bg = palette.keyboardBackground,
                keyFill = palette.keyBackground,
                text = palette.textColor,
                specialText = palette.secondaryTextColor,
                accent = palette.accentColor,
                stroke = palette.dividerColor.copy(alpha = 0.6f)
            )
        }
    }

    // Row 1: QWERTY + Backspace (Left to Right along arc)
    val row1Keys = remember(currentLanguage, shiftState) {
        when (currentLanguage) {
            "bangla" -> listOf("ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ", "⌫")
            else -> listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "⌫")
        }.map { label ->
            val finalLabel = if (label.length == 1 && label[0].isLetter() && shiftState.isUppercase) label.uppercase() else label
            RadialKey(
                id = finalLabel,
                displayLabel = if (label == "⌫") "←" else finalLabel,
                isSpecial = label == "⌫",
                weight = if (label == "⌫") 1.15f else 1.0f
            )
        }
    }

    // Row 2: ASDF + Enter (Left to Right along arc)
    val row2Keys = remember(currentLanguage, shiftState) {
        when (currentLanguage) {
            "bangla" -> listOf("ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", enterLabel)
            else -> listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "↵")
        }.map { label ->
            val finalLabel = if (label.length == 1 && label[0].isLetter() && shiftState.isUppercase) label.uppercase() else label
            RadialKey(
                id = finalLabel,
                displayLabel = if (label == "↵" || label == enterLabel) "↵" else finalLabel,
                isSpecial = label == "↵" || label == enterLabel,
                weight = if (label == "↵" || label == enterLabel) 1.15f else 1.0f
            )
        }
    }

    // Row 3: Shift + ZXCV + Symbols (Left to Right along arc)
    val row3Keys = remember(currentLanguage, shiftState) {
        when (currentLanguage) {
            "bangla" -> listOf("⇧", "ন", "প", "ফ", "ব", "ভ", "ম", "য", "র", "ল")
            else -> listOf("⇧", "z", "x", "c", "v", "b", "n", "m", "?!,", ".")
        }.map { label ->
            val finalLabel = if (label.length == 1 && label[0].isLetter() && shiftState.isUppercase) label.uppercase() else label
            val isShift = label == "⇧"
            val display = if (isShift) (if (shiftState.isCapsLock) "⇪" else "↑") else finalLabel
            RadialKey(
                id = finalLabel,
                displayLabel = display,
                isSpecial = isShift || label == "?!,",
                weight = if (isShift) 1.2f else if (label == "?!,") 1.1f else 1.0f
            )
        }
    }

    // Row 4: Inner Arc Controls (Emoji, Symbols, Space, Language)
    val row4Keys = listOf(
        RadialKey(id = "😃", displayLabel = "😃", isAlphabetic = false, isSpecial = true, weight = 1.0f),
        RadialKey(id = "?123", displayLabel = "?123", isAlphabetic = false, isSpecial = true, weight = 1.1f),
        RadialKey(id = "space", displayLabel = "space", isAlphabetic = false, isSpecial = true, weight = 2.8f),
        RadialKey(id = "🌐", displayLabel = "🌐", isAlphabetic = false, isSpecial = true, weight = 1.0f)
    )

    // Suggestion words along the top-most fan arc
    val displaySuggestions = remember(suggestions, oneHandedShowSuggestions) {
        if (!oneHandedShowSuggestions) emptyList()
        else {
            if (suggestions.isNotEmpty()) suggestions.take(4) else listOf(
                SuggestionItem(displayText = "His", replacementText = "His"),
                SuggestionItem(displayText = "How", replacementText = "How"),
                SuggestionItem(displayText = "Thanks", replacementText = "Thanks"),
                SuggestionItem(displayText = "Hello", replacementText = "Hello")
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(oneHandedHeightDp.dp)
            .clipToBounds()
            .background(bgColor)
    ) {
        // Floating Dock Controls on opposite corner (⇆ Switch Side, ⚙️ Settings, ⛶ Expand)
        val dockAlignment = if (isRight) Alignment.TopStart else Alignment.TopEnd
        Row(
            modifier = Modifier
                .align(dockAlignment)
                .padding(8.dp)
                .clip(CircleShape)
                .background(keyFillColor.copy(alpha = 0.9f))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(bgColor.copy(alpha = 0.8f))
                    .clickable { onSwitchSide() },
                contentAlignment = Alignment.Center
            ) {
                Text("⇆", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(bgColor.copy(alpha = 0.8f))
                    .clickable { showQuickSettings = true },
                contentAlignment = Alignment.Center
            ) {
                Text("⚙️", color = textColor, fontSize = 14.sp)
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(bgColor.copy(alpha = 0.8f))
                    .clickable { onExpandNormal() },
                contentAlignment = Alignment.Center
            ) {
                Text("⛶", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Custom Curved Canvas Layout
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(row1Keys, row2Keys, row3Keys, row4Keys, displaySuggestions, mode, shiftState, oneHandedArcScale, oneHandedShowSuggestions) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()

                        // Pivot coordinates (anchored right or left corner)
                        val px = if (isRight) w * 0.96f else w * 0.04f
                        val py = h * 1.04f

                        val dx = down.position.x - px
                        val dy = py - down.position.y
                        val dist = sqrt(dx * dx + dy * dy)
                        var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        if (angleDeg < 0) angleDeg += 360f

                        val baseR = max(w, h) * 1.08f * oneHandedArcScale

                        // Concentric Arc Radii
                        val rSuggIn = baseR * 0.86f
                        val rSuggOut = baseR * 0.99f

                        val r1In = baseR * 0.69f
                        val r1Out = baseR * 0.85f

                        val r2In = baseR * 0.52f
                        val r2Out = baseR * 0.68f

                        val r3In = baseR * 0.35f
                        val r3Out = baseR * 0.51f

                        val r4In = baseR * 0.16f
                        val r4Out = baseR * 0.34f

                        val minAngle = if (isRight) 98f else 4f
                        val maxAngle = if (isRight) 176f else 82f
                        val sweepTotal = maxAngle - minAngle

                        var hitRow = -1
                        var hitCol = -1
                        var hitKey: RadialKey? = null
                        var hitSugg: SuggestionItem? = null

                        if (angleDeg in minAngle..maxAngle) {
                            fun findWeightedIndex(keys: List<RadialKey>, normalizedAngle: Float): Int {
                                val totalWeight = keys.sumOf { it.weight.toDouble() }.toFloat()
                                val targetWeight = normalizedAngle * totalWeight
                                var currentWeight = 0f
                                for (i in keys.indices) {
                                    currentWeight += keys[i].weight
                                    if (targetWeight <= currentWeight || i == keys.lastIndex) {
                                        return i
                                    }
                                }
                                return keys.lastIndex
                            }

                            val norm = if (isRight) (maxAngle - angleDeg) / sweepTotal else (angleDeg - minAngle) / sweepTotal
                            val clampedNorm = norm.coerceIn(0f, 1f)

                            when {
                                displaySuggestions.isNotEmpty() && dist in rSuggIn..rSuggOut -> {
                                    hitRow = 0
                                    hitCol = (clampedNorm * displaySuggestions.size).toInt().coerceIn(0, displaySuggestions.size - 1)
                                    hitSugg = displaySuggestions[hitCol]
                                }
                                dist in r1In..r1Out -> {
                                    hitRow = 1
                                    hitCol = findWeightedIndex(row1Keys, clampedNorm)
                                    hitKey = row1Keys[hitCol]
                                }
                                dist in r2In..r2Out -> {
                                    hitRow = 2
                                    hitCol = findWeightedIndex(row2Keys, clampedNorm)
                                    hitKey = row2Keys[hitCol]
                                }
                                dist in r3In..r3Out -> {
                                    hitRow = 3
                                    hitCol = findWeightedIndex(row3Keys, clampedNorm)
                                    hitKey = row3Keys[hitCol]
                                }
                                dist in r4In..r4Out -> {
                                    hitRow = 4
                                    hitCol = findWeightedIndex(row4Keys, clampedNorm)
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

                        // Wait for UP gesture event
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
                                            "⇧", "↑", "⇪" -> onShiftToggle()
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

            // Pivot coordinates
            val px = if (isRight) w * 0.96f else w * 0.04f
            val py = h * 1.04f

            val baseR = max(w, h) * 1.08f * oneHandedArcScale

            // Concentric Band Boundaries
            val rSuggIn = baseR * 0.86f
            val rSuggOut = baseR * 0.99f

            val r1In = baseR * 0.69f
            val r1Out = baseR * 0.85f

            val r2In = baseR * 0.52f
            val r2Out = baseR * 0.68f

            val r3In = baseR * 0.35f
            val r3Out = baseR * 0.51f

            val r4In = baseR * 0.16f
            val r4Out = baseR * 0.34f

            val minAngle = if (isRight) 98f else 4f
            val maxAngle = if (isRight) 176f else 82f
            val sweepTotal = maxAngle - minAngle

            // Draw smooth radiating fan gradient background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        keyFillColor.copy(alpha = 0.85f),
                        keyFillColor.copy(alpha = 0.45f),
                        bgColor.copy(alpha = 0.15f)
                    ),
                    center = Offset(px, py),
                    radius = rSuggOut
                ),
                radius = rSuggOut,
                center = Offset(px, py)
            )

            val textPaint = Paint().apply {
                color = textColor.toArgb()
                textSize = 21.dp.toPx()
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }

            val specialPaint = Paint().apply {
                color = specialTextColor.toArgb()
                textSize = 17.dp.toPx()
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val suggPaint = Paint().apply {
                color = textColor.toArgb()
                textSize = 15.dp.toPx()
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }

            // Function to draw smooth concentric arc divider lines
            fun drawConcentricDivider(r: Float, alpha: Float = 0.5f, strokeWidthDp: Float = 1.0f) {
                drawArc(
                    color = strokeColor.copy(alpha = alpha),
                    startAngle = -maxAngle,
                    sweepAngle = sweepTotal,
                    useCenter = false,
                    topLeft = Offset(px - r, py - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = strokeWidthDp.dp.toPx())
                )
            }

            // Draw concentric divider lines exactly like the reference design
            if (displaySuggestions.isNotEmpty()) {
                drawConcentricDivider(rSuggOut, alpha = 0.4f, strokeWidthDp = 1.2f)
                drawConcentricDivider(rSuggIn, alpha = 0.5f, strokeWidthDp = 1.0f)
            }
            drawConcentricDivider(r1In, alpha = 0.5f, strokeWidthDp = 1.0f)
            drawConcentricDivider(r2In, alpha = 0.5f, strokeWidthDp = 1.0f)
            drawConcentricDivider(r3In, alpha = 0.5f, strokeWidthDp = 1.0f)
            drawConcentricDivider(r4In, alpha = 0.35f, strokeWidthDp = 1.0f)

            // Function to draw curved ring keys with accurate tangent rotation
            fun drawRingKeys(
                keys: List<RadialKey>,
                rIn: Float,
                rOut: Float,
                rowIndex: Int
            ) {
                val totalWeight = keys.sumOf { it.weight.toDouble() }.toFloat()
                val rMid = (rIn + rOut) / 2f
                var accumulatedWeight = 0f

                for (i in keys.indices) {
                    val key = keys[i]
                    val startFraction = accumulatedWeight / totalWeight
                    accumulatedWeight += key.weight
                    val endFraction = accumulatedWeight / totalWeight

                    val a1 = if (isRight) maxAngle - startFraction * sweepTotal else minAngle + startFraction * sweepTotal
                    val a2 = if (isRight) maxAngle - endFraction * sweepTotal else minAngle + endFraction * sweepTotal
                    val aMid = (a1 + a2) / 2f

                    val isPressed = activePressedKey == Pair(rowIndex, i)

                    // Draw key sector highlight on press
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
                        drawPath(path, color = accentColor.copy(alpha = 0.45f))
                    }

                    // Key Center Position
                    val kx = px + rMid * cos(Math.toRadians(aMid.toDouble())).toFloat()
                    val ky = py - rMid * sin(Math.toRadians(aMid.toDouble())).toFloat()

                    drawContext.canvas.nativeCanvas.save()
                    drawContext.canvas.nativeCanvas.translate(kx, ky)

                    // Precise Tangent Rotation: perpendicular to radial ray to thumb!
                    if (oneHandedRotateText) {
                        val tangentRot = if (isRight) {
                            -(aMid - 90f) + 42f
                        } else {
                            -(aMid - 90f) - 42f
                        }
                        drawContext.canvas.nativeCanvas.rotate(tangentRot)
                    }

                    val activePaint = if (key.isSpecial) specialPaint else textPaint
                    activePaint.textSize = when {
                        key.id == "space" -> 14.dp.toPx()
                        key.displayLabel.length > 2 -> 13.dp.toPx()
                        key.isSpecial -> 18.dp.toPx()
                        else -> 21.dp.toPx()
                    }
                    activePaint.color = if (key.isSpecial) specialTextColor.toArgb() else textColor.toArgb()

                    drawContext.canvas.nativeCanvas.drawText(
                        key.displayLabel,
                        0f,
                        7.dp.toPx(),
                        activePaint
                    )

                    drawContext.canvas.nativeCanvas.restore()
                }
            }

            // Draw Top Suggestion Arc Ring
            if (displaySuggestions.isNotEmpty()) {
                val suggStep = sweepTotal / displaySuggestions.size
                val rSuggMid = (rSuggIn + rSuggOut) / 2f

                for (i in displaySuggestions.indices) {
                    val a1 = if (isRight) maxAngle - i * suggStep else minAngle + i * suggStep
                    val a2 = if (isRight) a1 - suggStep else a1 + suggStep
                    val aMid = (a1 + a2) / 2f
                    val isPressed = activePressedKey == Pair(0, i)

                    val kx = px + rSuggMid * cos(Math.toRadians(aMid.toDouble())).toFloat()
                    val ky = py - rSuggMid * sin(Math.toRadians(aMid.toDouble())).toFloat()

                    drawContext.canvas.nativeCanvas.save()
                    drawContext.canvas.nativeCanvas.translate(kx, ky)

                    if (oneHandedRotateText) {
                        val tangentRot = if (isRight) {
                            -(aMid - 90f) + 42f
                        } else {
                            -(aMid - 90f) - 42f
                        }
                        drawContext.canvas.nativeCanvas.rotate(tangentRot)
                    }

                    if (isPressed) {
                        drawCircle(
                            color = accentColor.copy(alpha = 0.35f),
                            radius = 24.dp.toPx()
                        )
                    }

                    suggPaint.color = (if (isPressed) accentColor else textColor).toArgb()
                    val textToDraw = displaySuggestions[i].displayText
                    drawContext.canvas.nativeCanvas.drawText(
                        textToDraw,
                        0f,
                        5.dp.toPx(),
                        suggPaint
                    )

                    drawContext.canvas.nativeCanvas.restore()
                }
            }

            // Draw the 4 keyboard rings
            drawRingKeys(row1Keys, r1In, r1Out, 1)
            drawRingKeys(row2Keys, r2In, r2Out, 2)
            drawRingKeys(row3Keys, r3In, r3Out, 3)
            drawRingKeys(row4Keys, r4In, r4Out, 4)
        }

        // Quick Settings Sheet Modal
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
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
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
                        text = "⚙️ One-Handed Arc Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                        Text("Keyboard Height", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("${oneHandedHeightDp}dp", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
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
                        Text("${(oneHandedArcScale * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onUpdateTheme(id) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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
                        Text(if (oneHandedRotateText) "Rotated along curve" else "Straight upright text", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
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
                    Text("Predictive Word Suggestions", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = oneHandedShowSuggestions,
                        onCheckedChange = { onUpdateShowSuggestions(it) }
                    )
                }
            }
        }
    }
}
