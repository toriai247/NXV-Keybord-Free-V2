package com.example.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.keyboard.KeyboardMode
import com.example.keyboard.ShiftState
import com.example.language.bangla.BanglaLayouts
import com.example.language.english.EnglishEngine
import com.example.theme.KeyboardPalette

/**
 * The "Original UI" layout for the Reference Minimal keyboard theme.
 * Implements the exact 10-column rectangular minimalist grid shown in the reference image:
 *
 * Row 1: Q  W  E  R  T  Y  U  I  O  P
 * Row 2:  A  S  D  F  G  H  J  K  L
 * Row 3: ♡  Z  X  C  V  B  N  M  ⇧  ⌫
 * Row 4: 🗨  Lang  ,  [--- WAKSIE ---]  .  ↵
 *
 * Features:
 * - Pure white flat keycaps with thin 1dp borders and 2dp subtle rounded corners
 * - Dark high-contrast minimalist typography
 * - ♡ (Heart key) for quick favorite emoji/expressions
 * - 🗨 (Speech bubble key) for quick symbols and numbers
 * - Dynamic language switch key showing active language (EN, BN, AV)
 * - 5-column wide spacebar with centered "WAKSIE" watermark and cursor drag navigation
 * - Full 5-second alphabet-key hold for one-handed mode activation
 */
@Composable
fun ReferenceOriginalKeyboardLayout(
    isAvro: Boolean,
    isBangla: Boolean,
    shiftState: ShiftState,
    keyHeight: Dp,
    palette: KeyboardPalette,
    enterLabel: String,
    currentLanguage: String = "english",
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

    // Language display abbreviation for Row 4 Col 2
    val langLabel = when (currentLanguage.lowercase()) {
        "bangla" -> "BN"
        "avro" -> "AV"
        else -> "EN"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .testTag("reference_original_keyboard_layout")
    ) {
        // =====================================================================
        // ROW 1: 10 Keys (Q W E R T Y U I O P or Bangla Row 1)
        // =====================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val row1Keys = if (isBangla) {
                if (isShifted) BanglaLayouts.SHIFT_ROWS[0] else BanglaLayouts.NORMAL_ROWS[0]
            } else {
                EnglishEngine.QWERTY_ROW_1
            }

            row1Keys.forEachIndexed { index, letter ->
                val char = if (isBangla) letter else if (isShifted) letter.uppercase() else letter.lowercase()
                val subNum = EnglishEngine.NUMBER_ROW.getOrNull(index)

                KeyboardKeyView(
                    label = char,
                    subLabel = subNum,
                    showSubLabel = false,
                    popupMode = popupMode,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    palette = palette,
                    onHoldProgressUpdate = onHoldProgressUpdate,
                    onFiveSecondHoldComplete = onFiveSecondHoldComplete,
                    onHoldCancelled = onHoldCancelled,
                    onTap = { onCharTyped(char) },
                    onLongPress = { subNum?.let { onCharTyped(it) } }
                )
            }
        }

        // =====================================================================
        // ROW 2: 9 Keys (A S D F G H J K L or Bangla Row 2)
        // =====================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            val row2Keys = if (isBangla) {
                if (isShifted) BanglaLayouts.SHIFT_ROWS[1] else BanglaLayouts.NORMAL_ROWS[1]
            } else {
                EnglishEngine.QWERTY_ROW_2
            }

            row2Keys.forEach { letter ->
                val char = if (isBangla) letter else if (isShifted) letter.uppercase() else letter.lowercase()

                KeyboardKeyView(
                    label = char,
                    showSubLabel = false,
                    popupMode = popupMode,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    palette = palette,
                    onHoldProgressUpdate = onHoldProgressUpdate,
                    onFiveSecondHoldComplete = onFiveSecondHoldComplete,
                    onHoldCancelled = onHoldCancelled,
                    onTap = { onCharTyped(char) }
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))
        }

        // =====================================================================
        // ROW 3: 10 Keys: [♡ Heart] [7 Alphabet Keys] [⇧ Shift] [⌫ Backspace]
        // =====================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Col 1: ♡ (Heart / Favorites key)
            KeyboardKeyView(
                label = "♡",
                modifier = Modifier.weight(1f),
                isSpecialAction = true,
                height = keyHeight,
                palette = palette,
                showSubLabel = false,
                popupMode = popupMode,
                onTap = {
                    onCharTyped("❤️")
                },
                onLongPress = {
                    onCharTyped("✨")
                }
            )

            // Cols 2-8: 7 Letters (Z X C V B N M or Bangla Row 3)
            val row3Keys = if (isBangla) {
                if (isShifted) BanglaLayouts.SHIFT_ROWS[2].take(7) else BanglaLayouts.NORMAL_ROWS[2].take(7)
            } else {
                EnglishEngine.QWERTY_ROW_3
            }

            row3Keys.forEach { letter ->
                val char = if (isBangla) letter else if (isShifted) letter.uppercase() else letter.lowercase()

                KeyboardKeyView(
                    label = char,
                    showSubLabel = false,
                    popupMode = popupMode,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    palette = palette,
                    onHoldProgressUpdate = onHoldProgressUpdate,
                    onFiveSecondHoldComplete = onFiveSecondHoldComplete,
                    onHoldCancelled = onHoldCancelled,
                    onTap = { onCharTyped(char) }
                )
            }

            // Col 9: ⇧ (Shift Key)
            KeyboardKeyView(
                label = "⇧",
                modifier = Modifier.weight(1f),
                isSpecialAction = true,
                isCapsLock = shiftState == ShiftState.CAPS_LOCK,
                isShiftActive = shiftState == ShiftState.SHIFT_ONCE,
                height = keyHeight,
                palette = palette,
                showSubLabel = false,
                popupMode = popupMode,
                onTap = { onShift() }
            )

            // Col 10: ⌫ (Backspace Key with continuous deletion on hold)
            KeyboardKeyView(
                label = "⌫",
                modifier = Modifier.weight(1f),
                isSpecialAction = true,
                isRepeatable = true,
                height = keyHeight,
                palette = palette,
                showSubLabel = false,
                popupMode = popupMode,
                onTap = { onDelete() }
            )
        }

        // =====================================================================
        // ROW 4: 10 Columns: [🗨] [Lang] [,] [--- WAKSIE (5f) ---] [.] [↵]
        // =====================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Col 1: 🗨 (Speech bubble / Numbers & Symbols key)
            KeyboardKeyView(
                label = "🗨",
                modifier = Modifier.weight(1f),
                isSpecialAction = true,
                height = keyHeight,
                palette = palette,
                showSubLabel = false,
                popupMode = popupMode,
                onTap = { onSwitchMode(KeyboardMode.NUMBERS) },
                onLongPress = { onSwitchMode(KeyboardMode.EMOJI) }
            )

            // Col 2: Language Switch key (EN / BN / AV)
            KeyboardKeyView(
                label = langLabel,
                modifier = Modifier.weight(1f),
                isSpecialAction = true,
                height = keyHeight,
                palette = palette,
                showSubLabel = false,
                popupMode = popupMode,
                onTap = { onLanguageCycle() },
                onLongPress = { onLongPressLanguage() }
            )

            // Col 3: Comma key [,]
            KeyboardKeyView(
                label = ",",
                subLabel = ";",
                modifier = Modifier.weight(1f),
                isSpecialAction = false,
                height = keyHeight,
                palette = palette,
                showSubLabel = false,
                popupMode = popupMode,
                onTap = { onCharTyped(",") },
                onLongPress = { onCharTyped(";") }
            )

            // Cols 4-8: Spacebar with centered "WAKSIE" watermark (5f weight)
            KeyboardKeyView(
                label = "WAKSIE",
                modifier = Modifier.weight(5f),
                isSpaceBar = true,
                height = keyHeight,
                palette = palette,
                showSubLabel = false,
                popupMode = popupMode,
                onHorizontalDrag = onSpaceDrag,
                onTap = { onSpace() },
                onLongPress = { onLongPressLanguage() }
            )

            // Col 9: Period key [.]
            val dotChar = if (isBangla) "।" else "."
            KeyboardKeyView(
                label = dotChar,
                subLabel = "?",
                modifier = Modifier.weight(1f),
                isSpecialAction = false,
                height = keyHeight,
                palette = palette,
                showSubLabel = false,
                popupMode = popupMode,
                onTap = { onCharTyped(dotChar) },
                onLongPress = { onCharTyped("?") }
            )

            // Col 10: Action / Return / Enter key [↵]
            KeyboardKeyView(
                label = "↵",
                modifier = Modifier.weight(1f),
                isSpecialAction = true,
                isPrimaryAction = true,
                height = keyHeight,
                palette = palette,
                showSubLabel = false,
                popupMode = popupMode,
                onTap = { onEnter() }
            )
        }
    }
}
