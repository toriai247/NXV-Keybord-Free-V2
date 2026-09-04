package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.theme.KeyboardPalette

@Composable
fun NumberPadKeyboardLayout(
    keyHeight: Dp = 48.dp,
    palette: KeyboardPalette,
    enterLabel: String = "↵",
    onCharTyped: (String) -> Unit,
    onDelete: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isBanglaDigits by remember { mutableStateOf(false) }

    val digitMap = mapOf(
        "1" to "১", "2" to "২", "3" to "৩",
        "4" to "৪", "5" to "৫", "6" to "৬",
        "7" to "৭", "8" to "৮", "9" to "৯",
        "0" to "০"
    )

    fun getDigit(d: String): String {
        return if (isBanglaDigits) digitMap[d] ?: d else d
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.keyboardBackground)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Top Header: Math Operators & Bangla Digit Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bangla / English Digit Toggle Chip
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isBanglaDigits) palette.accentColor else palette.keyActionBackground)
                    .border(
                        width = 0.8.dp,
                        color = if (isBanglaDigits) palette.accentColor else palette.keyBorderColor,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { isBanglaDigits = !isBanglaDigits }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBanglaDigits) "১২৩ (বাংলা)" else "123 (English)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isBanglaDigits) palette.onAccentColor else palette.textColor
                )
            }

            // Quick Math Symbol Chips (+ - * / %)
            listOf("+", "-", "*", "/", "%").forEach { symbol ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.keyActionBackground)
                        .border(
                            width = 0.8.dp,
                            color = palette.keyBorderColor,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onCharTyped(symbol) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = symbol,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textColor
                    )
                }
            }
        }

        // 2. Main Numpad Dialer Grid (4 Columns x 4 Rows)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyHeight * 4 + 18.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Left 3 Columns: Digits Grid
            Column(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: 1, 2, 3
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NumpadDigitKey(getDigit("1"), getSubDigit("1", isBanglaDigits), palette, Modifier.weight(1f)) { onCharTyped(getDigit("1")) }
                    NumpadDigitKey(getDigit("2"), getSubDigit("2", isBanglaDigits), palette, Modifier.weight(1f)) { onCharTyped(getDigit("2")) }
                    NumpadDigitKey(getDigit("3"), getSubDigit("3", isBanglaDigits), palette, Modifier.weight(1f)) { onCharTyped(getDigit("3")) }
                }

                // Row 2: 4, 5, 6
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NumpadDigitKey(getDigit("4"), getSubDigit("4", isBanglaDigits), palette, Modifier.weight(1f)) { onCharTyped(getDigit("4")) }
                    NumpadDigitKey(getDigit("5"), getSubDigit("5", isBanglaDigits), palette, Modifier.weight(1f)) { onCharTyped(getDigit("5")) }
                    NumpadDigitKey(getDigit("6"), getSubDigit("6", isBanglaDigits), palette, Modifier.weight(1f)) { onCharTyped(getDigit("6")) }
                }

                // Row 3: 7, 8, 9
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NumpadDigitKey(getDigit("7"), getSubDigit("7", isBanglaDigits), palette, Modifier.weight(1f)) { onCharTyped(getDigit("7")) }
                    NumpadDigitKey(getDigit("8"), getSubDigit("8", isBanglaDigits), palette, Modifier.weight(1f)) { onCharTyped(getDigit("8")) }
                    NumpadDigitKey(getDigit("9"), getSubDigit("9", isBanglaDigits), palette, Modifier.weight(1f)) { onCharTyped(getDigit("9")) }
                }

                // Row 4: ., 0, ,
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NumpadActionKey(".", palette, Modifier.weight(1f)) { onCharTyped(".") }
                    NumpadDigitKey(getDigit("0"), getSubDigit("0", isBanglaDigits), palette, Modifier.weight(1f)) { onCharTyped(getDigit("0")) }
                    NumpadActionKey(",", palette, Modifier.weight(1f)) { onCharTyped(",") }
                }
            }

            // Rightmost Column: Backspace & Enter / Space Actions
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Backspace (Tall action key)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.keyActionBackground)
                        .border(width = 0.8.dp, color = palette.keyBorderColor, shape = RoundedCornerShape(10.dp))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = palette.textColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Space Bar Key
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.keyBackground)
                        .border(width = 0.8.dp, color = palette.keyBorderColor, shape = RoundedCornerShape(10.dp))
                        .clickable { onSpace() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SpaceBar,
                        contentDescription = "Space",
                        tint = palette.textColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Enter Key (Accent Colored - Spans 2 weight)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.accentColor)
                        .clickable { onEnter() },
                    contentAlignment = Alignment.Center
                ) {
                    if (enterLabel.lowercase().contains("search")) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = palette.onAccentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                            contentDescription = "Enter",
                            tint = palette.onAccentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 3. Bottom Return Bar: ABC Switch Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.keyActionBackground)
                    .border(width = 0.8.dp, color = palette.keyBorderColor, shape = RoundedCornerShape(10.dp))
                    .clickable { onClose() }
                    .testTag("numpad_close_abc"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Return to Keyboard",
                        tint = palette.textColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ABC Keyboard",
                        color = palette.textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun getSubDigit(d: String, isBangla: Boolean): String {
    val enToBn = mapOf(
        "1" to "১", "2" to "২", "3" to "৩",
        "4" to "৪", "5" to "৫", "6" to "৬",
        "7" to "৭", "8" to "৮", "9" to "৯",
        "0" to "০"
    )
    val bnToEn = enToBn.entries.associate { (k, v) -> v to k }
    return if (isBangla) bnToEn[d] ?: "" else enToBn[d] ?: ""
}

@Composable
private fun NumpadDigitKey(
    digit: String,
    subDigit: String,
    palette: KeyboardPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(palette.keyBackground)
            .border(width = 0.8.dp, color = palette.keyBorderColor, shape = RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                color = palette.textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            if (subDigit.isNotEmpty()) {
                Text(
                    text = subDigit,
                    color = palette.secondaryTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun NumpadActionKey(
    symbol: String,
    palette: KeyboardPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(palette.keyActionBackground)
            .border(width = 0.8.dp, color = palette.keyBorderColor, shape = RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = palette.textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
