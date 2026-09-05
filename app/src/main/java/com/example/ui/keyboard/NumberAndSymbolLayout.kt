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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.keyboard.KeyboardMode
import com.example.language.english.EnglishEngine
import com.example.theme.KeyboardPalette

@Composable
fun NumberAndSymbolLayout(
    isSymbolPage: Boolean,
    keyHeight: Dp,
    palette: KeyboardPalette,
    enterLabel: String,
    showEmojiKey: Boolean = true,
    showLanguageKey: Boolean = true,
    showKeySubLabels: Boolean = true,
    popupMode: String = "popup",
    onCharTyped: (String) -> Unit,
    onDelete: () -> Unit,
    onSpace: () -> Unit,
    onSpaceDrag: ((Float) -> Unit)? = null,
    onEnter: () -> Unit,
    onSwitchMode: (KeyboardMode) -> Unit,
    onTogglePage: () -> Unit,
    onLanguageCycle: () -> Unit = {},
    onLongPressLanguage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val row1 = if (isSymbolPage) EnglishEngine.MORE_SYMBOLS_ROW_1 else EnglishEngine.SYMBOL_ROW_1
    val row2 = if (isSymbolPage) EnglishEngine.MORE_SYMBOLS_ROW_2 else EnglishEngine.SYMBOL_ROW_2
    val row3 = if (isSymbolPage) EnglishEngine.MORE_SYMBOLS_ROW_3 else EnglishEngine.SYMBOL_ROW_3

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            row1.forEachIndexed { index, char ->
                KeyboardKeyView(
                    label = char,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    palette = palette,
                    showSubLabel = showKeySubLabels,
                    popupMode = popupMode,
                    columnIndex = index,
                    totalColumns = row1.size,
                    onTap = { onCharTyped(char) }
                )
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            row2.forEachIndexed { index, char ->
                KeyboardKeyView(
                    label = char,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    palette = palette,
                    showSubLabel = showKeySubLabels,
                    popupMode = popupMode,
                    columnIndex = index,
                    totalColumns = row2.size,
                    onTap = { onCharTyped(char) }
                )
            }
        }

        // Row 3: [=\< or 123] ... [Backspace]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Toggle Symbol Page
            KeyboardKeyView(
                label = if (isSymbolPage) "123" else "#+=",
                modifier = Modifier.weight(1.5f),
                isSpecialAction = true,
                height = keyHeight,
                palette = palette,
                showSubLabel = showKeySubLabels,
                popupMode = popupMode,
                onTap = { onTogglePage() }
            )

            row3.forEach { char ->
                KeyboardKeyView(
                    label = char,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    palette = palette,
                    showSubLabel = showKeySubLabels,
                    popupMode = popupMode,
                    onTap = { onCharTyped(char) }
                )
            }

            // Backspace
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

        // Row 4: [ABC] [😊 (if enabled)] [🌐 (if enabled)] [Space] [.] [↵]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            KeyboardKeyView(
                label = "ABC",
                modifier = Modifier.weight(1.3f),
                isSpecialAction = true,
                height = keyHeight,
                palette = palette,
                showSubLabel = showKeySubLabels,
                popupMode = popupMode,
                onTap = { onSwitchMode(KeyboardMode.ENGLISH) }
            )

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

            if (showLanguageKey) {
                KeyboardKeyView(
                    label = "🌐",
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

            val spaceWeight = when {
                !showEmojiKey && !showLanguageKey -> 5.4f
                !showEmojiKey || !showLanguageKey -> 4.4f
                else -> 3.6f
            }

            KeyboardKeyView(
                label = "SPACE",
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

            KeyboardKeyView(
                label = ".",
                modifier = Modifier.weight(1.0f),
                isSpecialAction = false,
                height = keyHeight,
                palette = palette,
                showSubLabel = showKeySubLabels,
                popupMode = popupMode,
                onTap = { onCharTyped(".") }
            )

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
