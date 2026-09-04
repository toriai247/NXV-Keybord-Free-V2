package com.example.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.keyboard.KeyboardMode
import com.example.keyboard.ShiftState
import com.example.language.english.EnglishEngine
import com.example.theme.KeyboardPalette
import com.example.theme.ThemeSpecialIconStyle

private val STRAWBERRY_ROW1_HINTS = listOf("+", "×", "+", "=", "/", "-", "<", ">", "[", "]")
private val STRAWBERRY_ROW2_HINTS = listOf("!", "@", "#", "%", "^", "&", "*", "(", ")")
private val STRAWBERRY_ROW3_HINTS = listOf("-", "'", "\"", ":", ";", ",", "?")

@Composable
fun QwertyKeyLayout(
    isAvro: Boolean,
    shiftState: ShiftState,
    showNumberRow: Boolean,
    keyHeight: Dp,
    palette: KeyboardPalette,
    enterLabel: String,
    showEmojiKey: Boolean = true,
    showLanguageKey: Boolean = true,
    showKeySubLabels: Boolean = true,
    popupMode: String = "popup",
    onHoldProgressUpdate: ((Float) -> Unit)? = null,
    onFiveSecondHoldComplete: (() -> Unit)? = null,
    onHoldCancelled: (() -> Unit)? = null,
    onCharTyped: (String) -> Unit,
    onDelete: () -> Unit,
    onSpace: () -> Unit,
    onSpaceDrag: ((Float) -> Unit)? = null,
    onEnter: () -> Unit,
    onShift: () -> Unit,
    onSwitchMode: (KeyboardMode) -> Unit,
    onLanguageCycle: () -> Unit,
    onLongPressLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isShifted = shiftState.isUppercase
    val isStrawberry = palette.specialIconStyle == ThemeSpecialIconStyle.STRAWBERRY_DESSERT
    val isPuppy = palette.specialIconStyle == ThemeSpecialIconStyle.PUPPY_MINIMAL

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        // Optional Number Row
        if (showNumberRow) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                EnglishEngine.NUMBER_ROW.forEach { num ->
                    KeyboardKeyView(
                        label = num,
                        modifier = Modifier.weight(1f),
                        height = keyHeight * 0.85f,
                        palette = palette,
                        showSubLabel = showKeySubLabels,
                        popupMode = popupMode,
                        onTap = { onCharTyped(num) }
                    )
                }
            }
        }

        // Row 1: Q W E R T Y U I O P
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            EnglishEngine.QWERTY_ROW_1.forEachIndexed { index, letter ->
                val char = if (isShifted) letter.uppercase() else letter.lowercase()
                val alt = if (isStrawberry) {
                    STRAWBERRY_ROW1_HINTS.getOrNull(index)
                } else if (!showNumberRow) {
                    EnglishEngine.NUMBER_ROW.getOrNull(index)
                } else {
                    null
                }

                KeyboardKeyView(
                    label = char,
                    subLabel = alt,
                    showSubLabel = showKeySubLabels,
                    popupMode = popupMode,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    palette = palette,
                    onHoldProgressUpdate = onHoldProgressUpdate,
                    onFiveSecondHoldComplete = onFiveSecondHoldComplete,
                    onHoldCancelled = onHoldCancelled,
                    onTap = { onCharTyped(char) },
                    onLongPress = { alt?.let { onCharTyped(it) } }
                )
            }
        }

        // Row 2: A S D F G H J K L (indented)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            EnglishEngine.QWERTY_ROW_2.forEachIndexed { index, letter ->
                val char = if (isShifted) letter.uppercase() else letter.lowercase()
                val alt = if (isStrawberry) STRAWBERRY_ROW2_HINTS.getOrNull(index) else null

                KeyboardKeyView(
                    label = char,
                    subLabel = alt,
                    showSubLabel = showKeySubLabels,
                    popupMode = popupMode,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    palette = palette,
                    onHoldProgressUpdate = onHoldProgressUpdate,
                    onFiveSecondHoldComplete = onFiveSecondHoldComplete,
                    onHoldCancelled = onHoldCancelled,
                    onTap = { onCharTyped(char) },
                    onLongPress = { alt?.let { onCharTyped(it) } }
                )
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }

        // Row 3: [Shift] Z X C V B N M [Backspace]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shift Key with clear visual states
            val shiftIcon = when (shiftState) {
                ShiftState.LOWERCASE -> "⇧"
                ShiftState.SHIFT_ONCE -> "⬆"
                ShiftState.CAPS_LOCK, ShiftState.MANUAL_UPPERCASE -> "⇪"
            }
            KeyboardKeyView(
                label = shiftIcon,
                modifier = Modifier.weight(1.5f),
                isSpecialAction = true,
                isCapsLock = shiftState == ShiftState.CAPS_LOCK,
                isShiftActive = shiftState == ShiftState.SHIFT_ONCE,
                height = keyHeight,
                palette = palette,
                showSubLabel = showKeySubLabels,
                popupMode = popupMode,
                onTap = { onShift() }
            )

            EnglishEngine.QWERTY_ROW_3.forEachIndexed { index, letter ->
                val char = if (isShifted) letter.uppercase() else letter.lowercase()
                val alt = when {
                    isStrawberry -> STRAWBERRY_ROW3_HINTS.getOrNull(index)
                    isPuppy && index == 0 -> "*"
                    else -> null
                }

                KeyboardKeyView(
                    label = char,
                    subLabel = alt,
                    showSubLabel = showKeySubLabels,
                    popupMode = popupMode,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    palette = palette,
                    onHoldProgressUpdate = onHoldProgressUpdate,
                    onFiveSecondHoldComplete = onFiveSecondHoldComplete,
                    onHoldCancelled = onHoldCancelled,
                    onTap = { onCharTyped(char) },
                    onLongPress = { alt?.let { onCharTyped(it) } }
                )
            }

            // Backspace Key with continuous repeating on hold
            KeyboardKeyView(
                label = "⌫",
                modifier = Modifier.weight(1.5f),
                isSpecialAction = true,
                isRepeatable = true,
                height = keyHeight,
                palette = palette,
                showSubLabel = showKeySubLabels,
                popupMode = popupMode,
                onTap = { onDelete() }
            )
        }

        // Row 4: [?123] [😊 (if enabled)] [🌐 (if enabled)] [    Space    ] [.] [↵]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numbers & Symbols switch (?123) / (123)
            val numSwitchLabel = if (isPuppy) "123" else "?123"
            KeyboardKeyView(
                label = numSwitchLabel,
                modifier = Modifier.weight(1.3f),
                isSpecialAction = true,
                height = keyHeight,
                palette = palette,
                showSubLabel = showKeySubLabels,
                popupMode = popupMode,
                onTap = { onSwitchMode(KeyboardMode.NUMBERS) }
            )

            // Emoji button 😊 (Always working and customizable!)
            if (showEmojiKey) {
                KeyboardKeyView(
                    label = "😊",
                    modifier = Modifier.weight(1.0f),
                    isSpecialAction = true,
                    height = keyHeight,
                    palette = palette,
                    showSubLabel = showKeySubLabels,
                    popupMode = popupMode,
                    onTap = { onSwitchMode(KeyboardMode.EMOJI) }
                )
            }

            // Language Switch 🌐 (Customizable)
            if (showLanguageKey) {
                KeyboardKeyView(
                    label = "🌐",
                    subLabel = if (isStrawberry) "..." else null,
                    modifier = Modifier.weight(1.0f),
                    isSpecialAction = true,
                    height = keyHeight,
                    palette = palette,
                    showSubLabel = showKeySubLabels,
                    popupMode = popupMode,
                    onTap = { onLanguageCycle() },
                    onLongPress = { onLongPressLanguage() }
                )
            }

            // Space Bar with language badge and geometric indicator
            val spaceWeight = when {
                !showEmojiKey && !showLanguageKey -> 5.4f
                !showEmojiKey || !showLanguageKey -> 4.4f
                else -> 3.6f
            }
            val spaceLabel = if (isAvro) "AVRO" else "SPACE"
            KeyboardKeyView(
                label = spaceLabel,
                modifier = Modifier.weight(spaceWeight),
                isSpaceBar = true,
                height = keyHeight,
                palette = palette,
                showSubLabel = showKeySubLabels,
                popupMode = popupMode,
                onHorizontalDrag = onSpaceDrag,
                onTap = { onSpace() },
                onLongPress = { onLongPressLanguage() }
            )

            // Punctuation (comma/period/dari)
            val punctChar = if (isAvro) "।" else if (isPuppy) "," else "."
            val punctAlt = if (isPuppy) ";" else if (isStrawberry) "..." else null
            KeyboardKeyView(
                label = punctChar,
                subLabel = punctAlt,
                modifier = Modifier.weight(1.0f),
                isSpecialAction = false,
                height = keyHeight,
                palette = palette,
                showSubLabel = showKeySubLabels,
                popupMode = popupMode,
                onTap = { onCharTyped(punctChar) },
                onLongPress = { punctAlt?.let { onCharTyped(it) } }
            )

            // Enter Key (Theme focal accent)
            KeyboardKeyView(
                label = enterLabel,
                modifier = Modifier.weight(1.4f),
                isSpecialAction = true,
                isPrimaryAction = true,
                height = keyHeight,
                palette = palette,
                showSubLabel = showKeySubLabels,
                popupMode = popupMode,
                onTap = { onEnter() }
            )
        }
    }
}
