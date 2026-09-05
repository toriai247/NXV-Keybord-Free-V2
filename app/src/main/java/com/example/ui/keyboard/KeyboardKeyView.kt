package com.example.ui.keyboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.theme.Cat3dPopupCharacter
import com.example.theme.CatThemeIcons
import com.example.theme.KeyPopupStyle
import com.example.theme.KeyboardPalette
import com.example.theme.KittyThemeIcons
import com.example.theme.PuppyPopupCharacter
import com.example.theme.ReferenceMinimalIcons
import com.example.theme.RgbSpectrumUtils
import com.example.theme.SpacebarStyle
import com.example.theme.StrawberryThemeIcons
import com.example.theme.ThemeSpecialIconStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun KeyboardKeyView(
    label: String,
    modifier: Modifier = Modifier,
    subLabel: String? = null,
    showSubLabel: Boolean = true,
    popupMode: String = "popup",
    isSpecialAction: Boolean = false,
    isPrimaryAction: Boolean = false,
    isSpaceBar: Boolean = false,
    isRepeatable: Boolean = false,
    isShiftActive: Boolean = false,
    isCapsLock: Boolean = false,
    height: Dp = 46.dp,
    palette: KeyboardPalette,
    showPreview: Boolean = true,
    isAlphabeticKey: Boolean = label.length == 1 && (label[0].isLetter() || label[0].code > 128),
    columnIndex: Int = -1,
    totalColumns: Int = 10,
    onHoldProgressUpdate: ((Float) -> Unit)? = null,
    onFiveSecondHoldComplete: (() -> Unit)? = null,
    onHoldCancelled: (() -> Unit)? = null,
    onHorizontalDrag: ((Float) -> Unit)? = null,
    onTap: () -> Unit,
    onLongPress: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 60),
        label = "key_scale"
    )

    val isKittenSpecialKey = palette.specialIconStyle == ThemeSpecialIconStyle.KAWAII_KITTEN &&
            (label.lowercase() == "s" || label.lowercase() == "i")

    val isRgbNeon = palette.specialIconStyle == ThemeSpecialIconStyle.RGB_NEON
    val rgbNeonColor = remember(label, columnIndex, totalColumns) {
        RgbSpectrumUtils.getColorForKey(label, columnIndex, totalColumns)
    }

    // Determine Key Background Color based on Theme & State
    val bgColor = when {
        isRgbNeon -> {
            if (isPressed) rgbNeonColor.copy(alpha = 0.28f)
            else palette.keyBackground
        }
        isPressed -> when {
            palette.popupStyle == KeyPopupStyle.PUPPY_CHARACTER && label.length == 1 && label[0].isLetter() -> palette.keyPressedBackground // Vibrant blue for Puppy Pop!
            isCapsLock -> palette.accentColor.copy(alpha = 0.85f)
            isPrimaryAction -> palette.accentColor
            isSpecialAction || isShiftActive -> palette.keyPressedBackground
            else -> palette.keyPressedBackground
        }
        isCapsLock -> palette.accentColor
        isShiftActive -> palette.keyPressedBackground
        isKittenSpecialKey -> if (isPressed) palette.keyPressedBackground else palette.keyActionBackground
        isPrimaryAction -> if (palette.specialIconStyle == ThemeSpecialIconStyle.PUPPY_MINIMAL) palette.keyActionBackground else palette.accentColor
        isSpecialAction -> palette.keyActionBackground
        else -> palette.keyBackground
    }

    val isRetroMech = palette.specialIconStyle == ThemeSpecialIconStyle.RETRO_MECH
    val isCat3d = palette.specialIconStyle == ThemeSpecialIconStyle.CAT_3D_SLATE
    val isCat3dCreamKey = isCat3d && (
        (label.length == 1 && "qwertyuiopQWERTYUIOP".contains(label[0])) ||
        isPrimaryAction || label == "↵" || label == "return" || label == "Go" || label == "Done" || label == "Search" || label == "Enter"
    )
    val isKeyOrange = label == "Space" || (isSpaceBar && palette.spacebarStyle == SpacebarStyle.RETRO_MECH_SPACE)
    val isKeyWhite = isAlphabeticKey || label == "😊" || label == "🌐" || label == "." || label == "," || label == " Smiley" || label.contains("smiley") || label == "Emoji"

    // Determine Label Text Color based on Theme & State
    val labelColor = when {
        isRgbNeon -> {
            if (isPressed) Color.White else rgbNeonColor
        }
        isCat3d -> {
            if (isCat3dCreamKey) Color(0xFF282E37) else Color.White
        }
        isRetroMech -> {
            when {
                isKeyWhite -> {
                    if (isAlphabeticKey) {
                        Color(0xFF0F448C) // Bold cobalt blue for letters
                    } else {
                        Color(0xFFE5523D) // Orange-red for Smiley, Globe, and dot/comma
                    }
                }
                isKeyOrange -> {
                    Color.White
                }
                else -> {
                    // Dark blue-grey modifiers have vivid orange-red icons/labels!
                    Color(0xFFE5523D)
                }
            }
        }
        isPressed && palette.popupStyle == KeyPopupStyle.PUPPY_CHARACTER && label.length == 1 && label[0].isLetter() -> Color.White
        isCapsLock || (isPrimaryAction && palette.specialIconStyle != ThemeSpecialIconStyle.PUPPY_MINIMAL) -> palette.onAccentColor
        isShiftActive && palette.specialIconStyle != ThemeSpecialIconStyle.STRAWBERRY_DESSERT -> palette.accentColor
        isSpaceBar -> palette.secondaryTextColor
        isSpecialAction -> palette.textColor
        else -> palette.textColor
    }

    // Key shape & elevation from theme
    val cornerShape = RoundedCornerShape(palette.keyCornerRadius)
    val elevation = if (isPressed) palette.pressedElevation else palette.keyElevation
    val borderColor = when {
        isRgbNeon -> if (isPressed) Color.White else rgbNeonColor
        isPrimaryAction && palette.specialIconStyle != ThemeSpecialIconStyle.STRAWBERRY_DESSERT -> Color.Transparent
        else -> palette.keyBorderColor
    }
    val effectiveBorderWidth = if (isRgbNeon) 1.8.dp else palette.keyBorderWidth

    val gestureModifier = if (isSpaceBar && (onHorizontalDrag != null || onLongPress != null)) {
        Modifier.pointerInput(onHorizontalDrag, onLongPress) {
            awaitEachGesture {
                val down = awaitFirstDown()
                isPressed = true
                var isDragging = false
                var isLongPressed = false
                val touchSlop = viewConfiguration.touchSlop
                val longPressTimeout = viewConfiguration.longPressTimeoutMillis

                val longPressJob = coroutineScope.launch {
                    delay(longPressTimeout)
                    if (!isDragging) {
                        isLongPressed = true
                        onLongPress?.invoke()
                    }
                }

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (change.isConsumed) break
                    if (!change.pressed) {
                        longPressJob.cancel()
                        if (!isDragging && !isLongPressed) {
                            onTap()
                        }
                        break
                    }
                    val totalDist = kotlin.math.abs(change.position.x - down.position.x)
                    if (!isDragging && totalDist > touchSlop) {
                        isDragging = true
                        longPressJob.cancel()
                    }
                    if (isDragging) {
                        val dx = change.positionChange().x
                        if (dx != 0f) {
                            change.consume()
                            onHorizontalDrag?.invoke(dx)
                        }
                    }
                }
                isPressed = false
            }
        }
    } else if (onFiveSecondHoldComplete != null && isAlphabeticKey) {
        Modifier.pointerInput(label, isAlphabeticKey) {
            awaitEachGesture {
                val down = awaitFirstDown()
                isPressed = true
                var is5sCompleted = false
                val startTime = System.currentTimeMillis()
                val total5sMs = 5000L

                val timerJob = coroutineScope.launch {
                    val updateInterval = 50L
                    var elapsed = 0L
                    while (isActive && elapsed < total5sMs) {
                        delay(updateInterval)
                        elapsed += updateInterval
                        if (elapsed >= 300L) {
                            val progress = ((elapsed - 300L).toFloat() / (total5sMs - 300L)).coerceIn(0f, 1f)
                            onHoldProgressUpdate?.invoke(progress)
                        }
                        if (elapsed >= total5sMs) {
                            is5sCompleted = true
                            onFiveSecondHoldComplete.invoke()
                            onHoldCancelled?.invoke()
                            break
                        }
                    }
                }

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) {
                        timerJob.cancel()
                        onHoldCancelled?.invoke()
                        val pressDuration = System.currentTimeMillis() - startTime

                        if (!is5sCompleted) {
                            if (pressDuration < 450L) {
                                onTap()
                            } else {
                                onLongPress?.invoke() ?: onTap()
                            }
                        }
                        break
                    }
                }
                isPressed = false
            }
        }
    } else {
        Modifier.pointerInput(isRepeatable) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    if (isRepeatable) {
                        onTap()
                        val repeatJob = coroutineScope.launch {
                            delay(380L)
                            var currentDelay = 65L
                            var holdDuration = 0L
                            while (isActive) {
                                onTap()
                                delay(currentDelay)
                                holdDuration += currentDelay
                                if (holdDuration > 1400L) {
                                    currentDelay = 35L
                                } else if (holdDuration > 700L) {
                                    currentDelay = 50L
                                }
                            }
                        }
                        try {
                            tryAwaitRelease()
                        } finally {
                            repeatJob.cancel()
                            isPressed = false
                        }
                    } else {
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    }
                },
                onTap = {
                    if (!isRepeatable) {
                        onTap()
                    }
                },
                onLongPress = {
                    if (!isRepeatable) {
                        onLongPress?.invoke() ?: onTap()
                    }
                }
            )
        }
    }

    val density = LocalDensity.current
    val bevelHeight = with(density) { 4.dp.toPx() }
    val pressedOffset = with(density) { if (isPressed) 2.5.dp.toPx() else 0f }

    val keyModifier = if (isRetroMech) {
        modifier
            .padding(horizontal = 2.dp, vertical = 2.5.dp)
            .height(height)
            .scale(scale)
            .drawBehind {
                val radius = palette.keyCornerRadius.toPx()
                val shadowColor = when {
                    isKeyOrange -> Color(0xFFAC2E1E) // Dark orange-red bevel base
                    isKeyWhite -> Color(0xFFB5C1C9) // Bevel base for white keycaps
                    else -> Color(0xFF142936) // Dark blue-teal bevel base
                }
                val faceColor = when {
                    isKeyOrange -> if (isPressed) Color(0xFFC7412E) else palette.accentColor
                    isKeyWhite -> if (isPressed) palette.keyPressedBackground else palette.keyBackground
                    else -> if (isPressed) Color(0xFF193242) else palette.keyActionBackground
                }
                val highlightColor = when {
                    isKeyOrange -> Color(0xFFFF8B7A) // Soft orange highlight line
                    isKeyWhite -> Color(0xFFFFFFFF) // Crisp white highlight line
                    else -> Color(0xFF3B6785) // Soft blue highlight line
                }

                // 1. Draw bottom 3D bevel base shadow
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(0f, bevelHeight),
                    size = Size(size.width, size.height - bevelHeight),
                    cornerRadius = CornerRadius(radius, radius)
                )

                // 2. Draw top face (offset downwards when pressed)
                drawRoundRect(
                    color = faceColor,
                    topLeft = Offset(0f, pressedOffset),
                    size = Size(size.width, size.height - bevelHeight),
                    cornerRadius = CornerRadius(radius, radius)
                )

                // 3. Draw fine highlight stroke on the top face
                drawRoundRect(
                    color = highlightColor,
                    topLeft = Offset(0f, pressedOffset),
                    size = Size(size.width, size.height - bevelHeight),
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .then(gestureModifier)
            .testTag("key_$label")
    } else if (isCat3d) {
        modifier
            .padding(horizontal = 2.dp, vertical = 2.5.dp)
            .height(height)
            .scale(scale)
            .drawBehind {
                val radius = palette.keyCornerRadius.toPx()
                val catBevel = 3.8.dp.toPx()
                val catPressed = if (isPressed) 2.5.dp.toPx() else 0f

                val shadowColor = if (isCat3dCreamKey) Color(0xFF9EA5B0) else Color(0xFF1E232B)
                val faceColor = if (isCat3dCreamKey) {
                    if (isPressed) Color(0xFFDCE0E6) else Color(0xFFF4F5F7)
                } else {
                    if (isPressed) Color(0xFF2E353E) else Color(0xFF3E4652)
                }
                val highlightColor = if (isCat3dCreamKey) Color(0xFFFFFFFF) else Color(0xFF525C6B)

                // 1. Draw bottom 3D bevel base shadow
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(0f, catBevel),
                    size = Size(size.width, size.height - catBevel),
                    cornerRadius = CornerRadius(radius, radius)
                )

                // 2. Draw top face (offset downwards when pressed)
                drawRoundRect(
                    color = faceColor,
                    topLeft = Offset(0f, catPressed),
                    size = Size(size.width, size.height - catBevel),
                    cornerRadius = CornerRadius(radius, radius)
                )

                // 3. Draw fine top highlight stroke
                drawRoundRect(
                    color = highlightColor,
                    topLeft = Offset(0f, catPressed),
                    size = Size(size.width, size.height - catBevel),
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .then(gestureModifier)
            .testTag("key_$label")
    } else {
        modifier
            .padding(horizontal = 2.dp, vertical = 2.5.dp)
            .height(height)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = cornerShape,
                spotColor = if (isRgbNeon && isPressed) rgbNeonColor else Color(0x35000000)
            )
            .clip(cornerShape)
            .background(bgColor)
            .border(
                width = effectiveBorderWidth,
                color = borderColor,
                shape = cornerShape
            )
            .then(gestureModifier)
            .testTag("key_$label")
    }

    val contentOffsetY = if (isRetroMech || isCat3d) {
        if (isPressed) 2.5.dp else 0.dp
    } else {
        0.dp
    }

    Box(
        modifier = keyModifier,
        contentAlignment = Alignment.Center
    ) {
        // Neon Press Glow Burst / Typing Ripple Effect for RGB Theme
        if (isRgbNeon && isPressed) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Soft radial glow aura behind character
                drawCircle(
                    color = rgbNeonColor.copy(alpha = 0.35f),
                    radius = kotlin.math.max(w, h) * 0.7f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
            }
        }

        // Main key surface content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = contentOffsetY),
            contentAlignment = Alignment.Center
        ) {
            // Check for theme-specific special icon drawings
            when {
                // CAT 3D SLATE SPECIAL DRAWINGS
                palette.specialIconStyle == ThemeSpecialIconStyle.CAT_3D_SLATE && (label == "⇧" || label == "⬆" || label == "⇪") -> {
                    CatThemeIcons.CatShiftIcon(
                        size = (height * 0.58f).coerceIn(20.dp, 28.dp),
                        isShifted = isShiftActive || isCapsLock
                    )
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.CAT_3D_SLATE && label == "⌫" -> {
                    CatThemeIcons.CatBackspaceIcon(
                        size = (height * 0.72f).coerceIn(26.dp, 36.dp)
                    )
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.CAT_3D_SLATE && (isPrimaryAction || label == "↵" || label == "return" || label == "Go" || label == "Done" || label == "Search" || label == "Enter") -> {
                    Text(
                        text = if (label == "↵" || label == "return") "Enter" else label,
                        color = Color(0xFF282E37),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // RGB CHROMA NEON SPECIAL DRAWINGS (Reference image match!)
                isRgbNeon && (label == "⇧" || label == "⬆" || label == "⇪") -> {
                    RgbSpectrumUtils.RgbShiftIcon(
                        size = (height * 0.52f).coerceIn(18.dp, 24.dp),
                        color = if (isPressed) Color.White else rgbNeonColor,
                        isShifted = isShiftActive,
                        isCapsLock = isCapsLock
                    )
                }
                isRgbNeon && label == "⌫" -> {
                    RgbSpectrumUtils.RgbBackspaceIcon(
                        size = (height * 0.52f).coerceIn(18.dp, 24.dp),
                        color = if (isPressed) Color.White else rgbNeonColor
                    )
                }
                isRgbNeon && label == "🌐" -> {
                    RgbSpectrumUtils.RgbGlobeIcon(
                        size = (height * 0.50f).coerceIn(18.dp, 22.dp),
                        color = if (isPressed) Color.White else rgbNeonColor
                    )
                }
                isRgbNeon && label == "😊" -> {
                    RgbSpectrumUtils.RgbEmojiIcon(
                        size = (height * 0.50f).coerceIn(18.dp, 22.dp),
                        color = if (isPressed) Color.White else rgbNeonColor
                    )
                }
                isRgbNeon && (isPrimaryAction || label == "↵" || label == "return" || label == "Go" || label == "Done" || label == "Search") -> {
                    RgbSpectrumUtils.RgbReturnKeyContent(
                        label = label,
                        color = if (isPressed) Color.White else rgbNeonColor
                    )
                }

                // KAWAII KITTEN SPECIAL DRAWINGS
                palette.specialIconStyle == ThemeSpecialIconStyle.KAWAII_KITTEN && (label == "⇧" || label == "⬆" || label == "⇪") -> {
                    KittyThemeIcons.KittyShiftIcon(
                        size = (height * 0.58f).coerceIn(20.dp, 28.dp),
                        isShifted = isShiftActive || isCapsLock
                    )
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.KAWAII_KITTEN && label == "⌫" -> {
                    KittyThemeIcons.KittyBackspaceIcon(
                        size = (height * 0.58f).coerceIn(20.dp, 28.dp)
                    )
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.KAWAII_KITTEN && label.lowercase() == "s" -> {
                    KittyThemeIcons.CutePawIcon(
                        size = (height * 0.65f).coerceIn(22.dp, 32.dp)
                    )
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.KAWAII_KITTEN && label.lowercase() == "i" -> {
                    KittyThemeIcons.KittenFaceIcon(
                        size = (height * 0.72f).coerceIn(24.dp, 36.dp)
                    )
                }

                // 1. STRAWBERRY DESSERT THEME SPECIAL ICONS
                palette.specialIconStyle == ThemeSpecialIconStyle.STRAWBERRY_DESSERT && (label == "⇧" || label == "⬆" || label == "⇪") -> {
                    StrawberryThemeIcons.StrawberryShiftIcon(
                        size = (height * 0.58f).coerceIn(20.dp, 28.dp),
                        isShifted = isShiftActive || isCapsLock
                    )
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.STRAWBERRY_DESSERT && label == "⌫" -> {
                    StrawberryThemeIcons.FrappeBackspaceIcon(
                        size = (height * 0.58f).coerceIn(20.dp, 28.dp)
                    )
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.STRAWBERRY_DESSERT && (label == "😊" || label == "🌐" || label == "?123" || label == "#+=") -> {
                    if (label == "😊") {
                        StrawberryThemeIcons.IceCreamIcon(size = (height * 0.58f).coerceIn(20.dp, 28.dp))
                    } else {
                        Text(
                            text = if (label == "🌐") "lolo" else label,
                            color = palette.textColor,
                            fontSize = if (label == "🌐") 11.sp else 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.STRAWBERRY_DESSERT && (isPrimaryAction || label == "↵" || label == "Go" || label == "Search" || label == "Done") -> {
                    StrawberryThemeIcons.ShortcakeEnterIcon(
                        size = (height * 0.62f).coerceIn(22.dp, 30.dp)
                    )
                }

                // 2. PUPPY POP MINIMAL ICONS (Reference Image 1)
                palette.specialIconStyle == ThemeSpecialIconStyle.PUPPY_MINIMAL && (label == "⇧" || label == "⬆" || label == "⇪") -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        // Minimalist thin arrow with dot (as seen in Image 1)
                        Text(
                            text = "↑",
                            color = palette.textColor,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal
                        )
                        // Top-left dot indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 4.dp, start = 6.dp)
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isShiftActive || isCapsLock) palette.accentColor
                                    else palette.secondaryTextColor.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.PUPPY_MINIMAL && label == "⌫" -> {
                    // Minimalist backspace symbol
                    Canvas(modifier = Modifier.size(24.dp)) {
                        val w = size.width
                        val h = size.height
                        val tagPath = Path().apply {
                            moveTo(w * 0.25f, h * 0.5f)
                            lineTo(w * 0.45f, h * 0.22f)
                            lineTo(w * 0.85f, h * 0.22f)
                            lineTo(w * 0.85f, h * 0.78f)
                            lineTo(w * 0.45f, h * 0.78f)
                            close()
                        }
                        drawPath(path = tagPath, color = palette.textColor, style = Stroke(width = 1.8f))
                        // 'X' inside
                        drawLine(
                            color = palette.textColor,
                            start = Offset(w * 0.54f, h * 0.38f),
                            end = Offset(w * 0.74f, h * 0.62f),
                            strokeWidth = 1.6f
                        )
                        drawLine(
                            color = palette.textColor,
                            start = Offset(w * 0.74f, h * 0.38f),
                            end = Offset(w * 0.54f, h * 0.62f),
                            strokeWidth = 1.6f
                        )
                    }
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.PUPPY_MINIMAL && (isPrimaryAction || label == "↵" || label == "Go" || label == "Done") -> {
                    // Minimalist angled enter return arrow
                    Canvas(modifier = Modifier.size(26.dp)) {
                        val w = size.width
                        val h = size.height
                        val enterPath = Path().apply {
                            moveTo(w * 0.80f, h * 0.35f)
                            lineTo(w * 0.80f, h * 0.65f)
                            lineTo(w * 0.30f, h * 0.65f)
                            lineTo(w * 0.42f, h * 0.50f)
                            moveTo(w * 0.30f, h * 0.65f)
                            lineTo(w * 0.42f, h * 0.80f)
                        }
                        drawPath(path = enterPath, color = palette.textColor, style = Stroke(width = 2.2f))
                    }
                }

                // 3. REFERENCE MINIMAL THEME SPECIAL ICONS (Original and NXV Mode)
                palette.specialIconStyle == ThemeSpecialIconStyle.REFERENCE_MINIMAL && (label == "⇧" || label == "⬆" || label == "⇪") -> {
                    ReferenceMinimalIcons.MinimalShiftIcon(
                        size = (height * 0.52f).coerceIn(18.dp, 24.dp),
                        isShifted = isShiftActive,
                        isCapsLock = isCapsLock,
                        tint = palette.textColor
                    )
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.REFERENCE_MINIMAL && label == "⌫" -> {
                    ReferenceMinimalIcons.MinimalBackspaceIcon(
                        size = (height * 0.52f).coerceIn(18.dp, 24.dp),
                        tint = palette.textColor
                    )
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.REFERENCE_MINIMAL && (isPrimaryAction || label == "↵" || label == "Go" || label == "Done" || label == "Search") -> {
                    ReferenceMinimalIcons.MinimalEnterIcon(
                        size = (height * 0.52f).coerceIn(18.dp, 24.dp),
                        tint = palette.textColor
                    )
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.REFERENCE_MINIMAL && label == "♡" -> {
                    ReferenceMinimalIcons.HeartOutlineIcon(
                        size = (height * 0.52f).coerceIn(18.dp, 24.dp),
                        tint = palette.textColor
                    )
                }
                palette.specialIconStyle == ThemeSpecialIconStyle.REFERENCE_MINIMAL && label == "🗨" -> {
                    ReferenceMinimalIcons.SpeechBubbleIcon(
                        size = (height * 0.52f).coerceIn(18.dp, 24.dp),
                        tint = palette.textColor
                    )
                }

                // 4. STANDARD TYPOGRAPHY KEYS (All themes)
                else -> {
                    if (isSpaceBar) {
                        when (palette.spacebarStyle) {
                            SpacebarStyle.RGB_NEON_BAR -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "space",
                                        color = rgbNeonColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                            SpacebarStyle.REFERENCE_MINIMAL_SPACE -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = palette.spacebarWatermark ?: "WAKSIE",
                                        color = palette.textColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                            SpacebarStyle.RETRO_MECH_SPACE -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    // Empty box to keep it completely clean and elegant, matching the reference image perfectly!
                                }
                            }
                            SpacebarStyle.CAT_3D_SLATE_BAR -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(1.5.dp))
                                            .background(Color.White)
                                    )
                                }
                            }
                            SpacebarStyle.KITTY_PAW_BAR -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        KittyThemeIcons.CutePawIcon(size = 18.dp)
                                        Text(
                                            text = "Space",
                                            color = palette.textColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        KittyThemeIcons.CutePawIcon(size = 18.dp)
                                    }
                                }
                            }
                            SpacebarStyle.PUPPY_BRACKET -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Minimalist center bracket line: └─┘
                                    Canvas(modifier = Modifier.size(width = 38.dp, height = 12.dp)) {
                                        val w = size.width
                                        val h = size.height
                                        val bracketPath = Path().apply {
                                            moveTo(w * 0.1f, h * 0.2f)
                                            lineTo(w * 0.1f, h * 0.8f)
                                            lineTo(w * 0.9f, h * 0.8f)
                                            lineTo(w * 0.9f, h * 0.2f)
                                        }
                                        drawPath(path = bracketPath, color = palette.textColor.copy(alpha = 0.85f), style = Stroke(width = 2.0f))
                                    }

                                    // Watermark "nxv" or label
                                    Text(
                                        text = palette.spacebarWatermark ?: label,
                                        color = palette.secondaryTextColor.copy(alpha = 0.65f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(bottom = 3.dp, end = 8.dp)
                                    )
                                }
                            }
                            SpacebarStyle.STRAWBERRY_PILL -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (label != "SPACE") {
                                        Text(
                                            text = label,
                                            color = palette.secondaryTextColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            SpacebarStyle.STANDARD_BAR -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = labelColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    // Subtle indicator bar
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 4.dp)
                                            .width(36.dp)
                                            .height(2.dp)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(palette.dividerColor)
                                    )
                                }
                            }
                        }
                    } else {
                        when (label) {
                            "😊" -> {
                                Icon(
                                    imageVector = Icons.Default.SentimentSatisfiedAlt,
                                    contentDescription = "Emoji",
                                    tint = labelColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            "🌐" -> {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Language",
                                    tint = labelColor,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            "⌫" -> {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = labelColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            "↵" -> {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                                    contentDescription = "Enter",
                                    tint = labelColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            "⇧", "⬆", "⇪" -> {
                                Icon(
                                    imageVector = Icons.Default.North,
                                    contentDescription = "Shift",
                                    tint = labelColor,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            else -> {
                                Text(
                                    text = label,
                                    color = labelColor,
                                    fontSize = if (label.length > 2) 13.sp else 18.sp,
                                    fontWeight = when {
                                        isPrimaryAction || isCapsLock -> FontWeight.Bold
                                        isSpecialAction || isShiftActive -> FontWeight.SemiBold
                                        else -> FontWeight.Medium
                                    },
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // CapsLock active indicator bar
            if (isCapsLock && palette.specialIconStyle != ThemeSpecialIconStyle.STRAWBERRY_DESSERT) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 3.5.dp)
                        .width(14.dp)
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(palette.onAccentColor)
                )
            } else if (isShiftActive && palette.specialIconStyle == ThemeSpecialIconStyle.STANDARD) {
                // One-time Shift active indicator dot
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                        .width(5.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(palette.accentColor)
                )
            }

            // SubLabel (e.g. number hint or top symbol)
            if (showSubLabel && subLabel != null) {
                Text(
                    text = subLabel,
                    color = palette.topRowHintColor ?: palette.secondaryTextColor.copy(alpha = 0.6f),
                    fontSize = if (palette.specialIconStyle == ThemeSpecialIconStyle.STRAWBERRY_DESSERT) 9.sp else 9.sp,
                    fontWeight = if (palette.specialIconStyle == ThemeSpecialIconStyle.STRAWBERRY_DESSERT) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .align(
                            if (palette.specialIconStyle == ThemeSpecialIconStyle.STRAWBERRY_DESSERT) Alignment.TopCenter
                            else Alignment.TopEnd
                        )
                        .padding(
                            top = if (palette.specialIconStyle == ThemeSpecialIconStyle.STRAWBERRY_DESSERT) 1.5.dp else 2.dp,
                            end = if (palette.specialIconStyle == ThemeSpecialIconStyle.STRAWBERRY_DESSERT) 0.dp else 4.dp
                        )
                )
            }
        }

        // Popup Preview when pressed based on popupMode
        if (isPressed && showPreview && popupMode != "disabled" && label.length == 1) {
            when {
                popupMode == "popup" && palette.popupStyle == KeyPopupStyle.RGB_NEON_POPUP -> {
                    // Sleek AMOLED black popup with vivid neon glowing border and letter
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-52).dp)
                            .size(48.dp)
                            .zIndex(99f)
                            .shadow(12.dp, RoundedCornerShape(12.dp), spotColor = rgbNeonColor)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF060606))
                            .border(2.dp, rgbNeonColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label.uppercase(),
                            color = rgbNeonColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                popupMode == "popup" && palette.popupStyle == KeyPopupStyle.CAT_3D_SLATE_POPUP -> {
                    Cat3dPopupCharacter(
                        char = label.uppercase(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-56).dp)
                            .zIndex(99f)
                    )
                }
                popupMode == "popup" && palette.popupStyle == KeyPopupStyle.PUPPY_CHARACTER && label[0].isLetter() -> {
                    PuppyPopupCharacter(
                        char = label.uppercase(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-56).dp)
                            .zIndex(99f)
                    )
                }
                popupMode == "popup" -> {
                    // Standard / Themed floating bubble
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-48).dp)
                            .size(46.dp)
                            .zIndex(99f)
                            .shadow(8.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label.uppercase(),
                            color = palette.onAccentColor,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                popupMode == "mini" -> {
                    // Mini on-key indicator badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(palette.accentColor.copy(alpha = 0.85f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                            .zIndex(50f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label.uppercase(),
                            color = palette.onAccentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
