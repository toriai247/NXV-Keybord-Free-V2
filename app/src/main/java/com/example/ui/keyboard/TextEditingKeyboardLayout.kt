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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SelectAll
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
fun TextEditingKeyboardLayout(
    keyHeight: Dp = 48.dp,
    palette: KeyboardPalette,
    onMoveUp: (isSelecting: Boolean) -> Unit,
    onMoveDown: (isSelecting: Boolean) -> Unit,
    onMoveLeft: (isSelecting: Boolean) -> Unit,
    onMoveRight: (isSelecting: Boolean) -> Unit,
    onMoveToStart: (isSelecting: Boolean) -> Unit,
    onMoveToEnd: (isSelecting: Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSelecting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.keyboardBackground)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Top Header Bar: Selection Mode Switch & Quick Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection Toggle Chip
            Box(
                modifier = Modifier
                    .weight(1.8f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelecting) palette.accentColor else palette.keyActionBackground)
                    .border(
                        width = 0.8.dp,
                        color = if (isSelecting) palette.accentColor else palette.keyBorderColor,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { isSelecting = !isSelecting }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSelecting) "✓ Select Mode: ON" else "Select Text",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelecting) palette.onAccentColor else palette.textColor
                )
            }

            // Quick Actions: Select All, Copy, Paste
            TextEditActionChip("Select All", Icons.Default.SelectAll, palette, Modifier.weight(1.2f)) { onSelectAll() }
            TextEditActionChip("Copy", Icons.Default.ContentCopy, palette, Modifier.weight(1f)) { onCopy() }
            TextEditActionChip("Paste", Icons.Default.ContentPaste, palette, Modifier.weight(1f)) { onPaste() }
        }

        // 2. Central Section: Left Navigation DPAD + Right Action Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyHeight * 3 + 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left DPAD (Cross Layout)
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.keyBackground)
                    .border(width = 0.8.dp, color = palette.keyBorderColor, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // UP
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.keyActionBackground)
                            .clickable { onMoveUp(isSelecting) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Up",
                            tint = palette.textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Middle Row: LEFT, SELECT, RIGHT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.keyActionBackground)
                                .clickable { onMoveLeft(isSelecting) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Left",
                                tint = palette.textColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // CENTER (Select Mode Toggle Button)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelecting) palette.accentColor else palette.keyboardBackground)
                                .border(width = 1.dp, color = palette.accentColor, shape = CircleShape)
                                .clickable { isSelecting = !isSelecting },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isSelecting) "SEL" else "MOV",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelecting) palette.onAccentColor else palette.accentColor
                            )
                        }

                        // RIGHT
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.keyActionBackground)
                                .clickable { onMoveRight(isSelecting) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Right",
                                tint = palette.textColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // DOWN
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.keyActionBackground)
                            .clickable { onMoveDown(isSelecting) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Down",
                            tint = palette.textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Right Action Panel (Home, End, Backspace, Copy, Paste)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: Home (|<) & End (>|)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextEditActionCard(
                        modifier = Modifier.weight(1f),
                        palette = palette,
                        onClick = { onMoveToStart(isSelecting) }
                    ) {
                        Text("|< Start", color = palette.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    TextEditActionCard(
                        modifier = Modifier.weight(1f),
                        palette = palette,
                        onClick = { onMoveToEnd(isSelecting) }
                    ) {
                        Text("End >|", color = palette.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Row 2: Copy & Paste
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextEditActionCard(
                        modifier = Modifier.weight(1f),
                        palette = palette,
                        onClick = onCopy
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = palette.textColor, modifier = Modifier.size(15.dp))
                            Text("Copy", color = palette.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    TextEditActionCard(
                        modifier = Modifier.weight(1f),
                        palette = palette,
                        onClick = onPaste
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = palette.textColor, modifier = Modifier.size(15.dp))
                            Text("Paste", color = palette.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Row 3: Backspace
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextEditActionCard(
                        modifier = Modifier.weight(1f),
                        palette = palette,
                        onClick = onDelete
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Delete",
                                tint = palette.textColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Delete", color = palette.textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Bottom Return Bar
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
                    .testTag("textedit_close_abc"),
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

@Composable
private fun TextEditActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = palette.textColor, modifier = Modifier.size(14.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = palette.textColor)
        }
    }
}

@Composable
private fun TextEditActionCard(
    modifier: Modifier = Modifier,
    palette: KeyboardPalette,
    isActive: Boolean = false,
    activeColor: Color = Color.Unspecified,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val bg = when {
        isActive -> if (activeColor != Color.Unspecified) activeColor else palette.accentColor
        else -> palette.keyActionBackground
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(width = 0.8.dp, color = palette.keyBorderColor, shape = RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
