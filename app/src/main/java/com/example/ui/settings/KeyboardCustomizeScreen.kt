package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.KeyboardSettings
import com.example.keyboard.ShiftState
import com.example.theme.KeyboardPalette
import com.example.theme.KeyboardThemes
import com.example.ui.keyboard.QwertyKeyLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardCustomizeScreen(
    settings: KeyboardSettings,
    onUpdateShowEmojiKey: (Boolean) -> Unit,
    onUpdateShowLanguageKey: (Boolean) -> Unit,
    onUpdateShowKeySubLabels: (Boolean) -> Unit,
    onUpdateKeyPopupMode: (String) -> Unit,
    onUpdateShowNumberRow: (Boolean) -> Unit,
    onUpdateHeightRatio: (Float) -> Unit,
    onBack: () -> Unit
) {
    val palette: KeyboardPalette = remember(settings.theme) { KeyboardThemes.getPalette(settings.theme) }
    var previewShift by remember { mutableStateOf(ShiftState.LOWERCASE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keyboard Layout & Keys") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Live Interactive Preview Header
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Preview,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live Layout Preview",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Text(
                            text = "Theme: ${palette.themeName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Mini Keyboard Preview Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.keyboardBackground)
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        QwertyKeyLayout(
                            isAvro = false,
                            shiftState = previewShift,
                            showNumberRow = settings.showNumberRow,
                            keyHeight = (36 * settings.keyboardHeightRatio).dp,
                            palette = palette,
                            enterLabel = "↵",
                            showEmojiKey = settings.showEmojiKey,
                            showLanguageKey = settings.showLanguageKey,
                            showKeySubLabels = settings.showKeySubLabels,
                            popupMode = settings.keyPopupMode,
                            onCharTyped = {},
                            onDelete = {},
                            onSpace = {},
                            onEnter = {},
                            onShift = {
                                previewShift = if (previewShift == ShiftState.LOWERCASE) ShiftState.SHIFT_ONCE else ShiftState.LOWERCASE
                            },
                            onSwitchMode = {},
                            onLanguageCycle = {},
                            onLongPressLanguage = {}
                        )
                    }
                    Text(
                        text = "Real-time preview updates immediately with your changes below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Section 1: Bottom Row Keys (বাটন কাস্টমাইজেশন)
            SectionHeader(title = "BOTTOM ROW KEYS (বাটন কাস্টমাইজেশন)")

            CustomizeSwitchTile(
                icon = Icons.Default.Mood,
                title = "Emoji Button (😊 ইমোজি বাটন)",
                subtitle = "Show dedicated emoji picker button on the bottom row",
                checked = settings.showEmojiKey,
                onCheckedChange = { onUpdateShowEmojiKey(it) },
                tag = "switch_emoji_key"
            )

            CustomizeSwitchTile(
                icon = Icons.Default.Language,
                title = "Language Switcher (🌐 ভাষা পরিবর্তন বাটন)",
                subtitle = "Show globe icon next to spacebar to quickly switch English / বাংলা / Avro",
                checked = settings.showLanguageKey,
                onCheckedChange = { onUpdateShowLanguageKey(it) },
                tag = "switch_language_key"
            )

            CustomizeSwitchTile(
                icon = Icons.Default.ShortText,
                title = "Dedicated Number Row (১২৩৪৫৬৭৮৯০)",
                subtitle = "Show full row of numbers 1-0 permanently at the top of keyboard",
                checked = settings.showNumberRow,
                onCheckedChange = { onUpdateShowNumberRow(it) },
                tag = "switch_number_row"
            )

            // Section 2: Key Hints & Sub-labels (সাব-লেবেল ও হিন্টস)
            SectionHeader(title = "KEY SUB-LABELS & HINTS (কী সাব-লেবেল ও হিন্টস)")

            CustomizeSwitchTile(
                icon = Icons.Default.Visibility,
                title = "Key Hints & Mini Sub-labels",
                subtitle = "Show mini numbers & symbol hints on letter keys (e.g. 1 on Q, 2 on W)",
                checked = settings.showKeySubLabels,
                onCheckedChange = { onUpdateShowKeySubLabels(it) },
                tag = "switch_sublabels"
            )

            // Section 3: Key Press & Long-Press Popups (কী প্রেস / হোল্ড প্রিভিউ)
            SectionHeader(title = "POPUP & LONG-PRESS PREVIEW (কী প্রেস / পপ-আপ প্রিভিউ)")

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "When pressing or holding a key:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )

                    val popupModes = listOf(
                        Triple(
                            "popup",
                            "Floating Bubble Popup (ভাসমান বাবল)",
                            "Magnified bubble pops up above key (with Puppy animation on Puppy Pop theme)"
                        ),
                        Triple(
                            "mini",
                            "Mini Badge on Key (মিনি ইন-কী ব্যাজ)",
                            "Compact mini badge shows directly on top edge of the key without covering screen"
                        ),
                        Triple(
                            "disabled",
                            "Hidden / Off (কোনো পপ-আপ ছাড়া)",
                            "Completely hide preview popups for distraction-free ultra fast typing"
                        )
                    )

                    popupModes.forEach { (modeId, modeTitle, modeDesc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onUpdateKeyPopupMode(modeId) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = settings.keyPopupMode == modeId,
                                onClick = { onUpdateKeyPopupMode(modeId) }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = modeTitle,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = modeDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Keyboard Height
            SectionHeader(title = "KEYBOARD HEIGHT (কিবোর্ড উচ্চতা)")

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Height Scale", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            text = "${(settings.keyboardHeightRatio * 100).toInt()}%",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = settings.keyboardHeightRatio,
                        onValueChange = { onUpdateHeightRatio(it) },
                        valueRange = 0.8f..1.3f,
                        steps = 5,
                        modifier = Modifier.testTag("slider_keyboard_height")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Compact (80%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Standard (100%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tall (130%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
    )
}

@Composable
private fun CustomizeSwitchTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(tag)
            )
        }
    }
}
