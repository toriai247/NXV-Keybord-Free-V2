package com.example.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ClipboardItem
import com.example.downloader.tiktok.TikTokDownloadState
import com.example.suggestion.SuggestionItem
import com.example.theme.KeyboardPalette

data class AutoSavePromptData(
    val serviceName: String,
    val username: String,
    val password: String,
    val onConfirmSave: () -> Unit,
    val onDismiss: () -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SuggestionStrip(
    suggestions: List<SuggestionItem>,
    palette: KeyboardPalette,
    currentLanguage: String = "english",
    autoSavePrompt: AutoSavePromptData? = null,
    recentClip: ClipboardItem? = null,
    clipboardCount: Int = 0,
    isToolbarExpanded: Boolean = false,
    keyVibrationEnabled: Boolean = true,
    keySoundEnabled: Boolean = false,
    oneHandedMode: String = "none",
    isVoiceListening: Boolean = false,
    voiceLiveText: String = "",
    voiceStatusText: String = "",
    voiceRmsLevel: Float = 0f,
    voiceErrorMessage: String? = null,
    onStopVoice: () -> Unit = {},
    onRequestVoicePermission: (() -> Unit)? = null,
    onToggleToolbar: () -> Unit = {},
    onSuggestionClick: (SuggestionItem) -> Unit,
    onClipboardClick: () -> Unit,
    onQuickPaste: ((String) -> Unit)? = null,
    onVaultClick: () -> Unit = {},
    onTextEditClick: () -> Unit = {},
    onNumberPadClick: () -> Unit = {},
    onVoiceClick: () -> Unit = {},
    onThemesClick: () -> Unit = {},
    onThemesLongClick: () -> Unit = {},
    onToggleVibration: () -> Unit = {},
    onToggleSound: () -> Unit = {},
    onToggleOneHanded: () -> Unit = {},
    onSettingsClick: () -> Unit,
    onLanguageCycle: () -> Unit = {},
    onLanguageLongPress: () -> Unit = {},
    tikTokState: TikTokDownloadState = TikTokDownloadState.Idle,
    onTikTokDownloadClicked: () -> Unit = {},
    onTikTokFormatChosen: (isAudio: Boolean) -> Unit = {},
    onTikTokQualityChosen: (quality: String) -> Unit = {},
    onTikTokCancel: () -> Unit = {},
    onTikTokDismiss: () -> Unit = {},
    onTikTokRetry: () -> Unit = {},
    onTikTokOpenFile: (filePath: String, isAudio: Boolean) -> Unit = { _, _ -> },
    onTikTokShareFile: (filePath: String, isAudio: Boolean) -> Unit = { _, _ -> },
    onTikTokPlayInKeyboard: (filePath: String, isAudio: Boolean, title: String) -> Unit = { _, _, _ -> },
    onTikTokToolbarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.suggestionBarBackground)
    ) {
        if (autoSavePrompt != null) {
            // Interactive Auto-Save Banner for Passwords
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password AutoSave",
                        tint = palette.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save password for ${autoSavePrompt.serviceName}?",
                        color = palette.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF4CAF50))
                            .clickable { autoSavePrompt.onConfirmSave() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("autosave_confirm_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Save",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { autoSavePrompt.onDismiss() }
                            .padding(horizontal = 6.dp, vertical = 5.dp)
                            .testTag("autosave_dismiss_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = palette.secondaryTextColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        } else {
            // 1. Main Top Row: Chevron (^) Expander on the left + Spacious 100% Suggestions Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand / Collapse Chevron Button
                Box(
                    modifier = Modifier
                        .padding(start = 2.dp, end = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isToolbarExpanded) palette.accentColor.copy(alpha = 0.20f)
                            else palette.keyActionBackground.copy(alpha = 0.65f)
                        )
                        .border(
                            width = 0.8.dp,
                            color = if (isToolbarExpanded) palette.accentColor.copy(alpha = 0.5f) else palette.keyBorderColor.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleToolbar() }
                        .padding(horizontal = 6.dp, vertical = 5.dp)
                        .testTag("toolbar_expand_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = if (isToolbarExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                            contentDescription = if (isToolbarExpanded) "Collapse Toolbar" else "Expand Toolbar",
                            tint = if (isToolbarExpanded) palette.accentColor else palette.textColor,
                            modifier = Modifier.size(16.dp)
                        )
                        if (!isToolbarExpanded && clipboardCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(palette.accentColor)
                            )
                        }
                    }
                }

                // Suggestions Area, Live Voice Bar, or TikTok Downloader Bar
                if (isVoiceListening) {
                    LiveVoiceSuggestionContent(
                        voiceStatus = voiceStatusText,
                        voiceLiveText = voiceLiveText,
                        rmsLevel = voiceRmsLevel,
                        errorMessage = voiceErrorMessage,
                        currentLanguage = currentLanguage,
                        palette = palette,
                        onCancel = onStopVoice,
                        onRequestPermission = onRequestVoicePermission,
                        modifier = Modifier.weight(1f)
                    )
                } else if (tikTokState !is TikTokDownloadState.Idle) {
                    TikTokSuggestionBar(
                        state = tikTokState,
                        palette = palette,
                        onDownloadClicked = onTikTokDownloadClicked,
                        onFormatChosen = onTikTokFormatChosen,
                        onQualityChosen = onTikTokQualityChosen,
                        onCancel = onTikTokCancel,
                        onDismiss = onTikTokDismiss,
                        onRetry = onTikTokRetry,
                        onOpenFile = onTikTokOpenFile,
                        onShareFile = onTikTokShareFile,
                        onPlayInKeyboard = onTikTokPlayInKeyboard,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Paste chip when clipboard has content and no suggestions yet
                        if (recentClip != null && onQuickPaste != null && suggestions.isEmpty()) {
                            val preview = if (recentClip.text.length > 22) recentClip.text.take(22) + "…" else recentClip.text
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.accentColor.copy(alpha = 0.16f))
                                    .clickable { onQuickPaste(recentClip.text) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("toolbar_quick_paste_chip"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = null,
                                        tint = palette.accentColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Paste \"$preview\"",
                                        color = palette.accentColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (suggestions.isEmpty() && recentClip == null) {
                            Text(
                                text = "NXV Keyboard",
                                color = palette.secondaryTextColor.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        } else {
                            suggestions.forEachIndexed { index, item ->
                                val isPrimary = item.isPrimary

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isPrimary) palette.accentColor.copy(alpha = 0.16f)
                                            else Color.Transparent
                                        )
                                        .clickable { onSuggestionClick(item) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("suggestion_$index"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.displayText,
                                        color = if (isPrimary) palette.accentColor else palette.textColor,
                                        fontSize = 14.sp,
                                        fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (index < suggestions.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .height(16.dp)
                                            .width(1.dp)
                                            .background(palette.dividerColor.copy(alpha = 0.45f))
                                    )
                                }
                            }
                        }
                    }
                }

                // Voice Mic Button (Positioned on the RIGHT side of the suggestion bar)
                VoiceMicButton(
                    isListening = isVoiceListening,
                    palette = palette,
                    onClick = if (isVoiceListening) onStopVoice else onVoiceClick,
                    modifier = Modifier.padding(start = 4.dp, end = 2.dp)
                )
            }

            // 2. Middle Layer: Dedicated Action Bar when expanded
            AnimatedVisibility(
                visible = isToolbarExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.keyActionBackground.copy(alpha = 0.35f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(palette.dividerColor.copy(alpha = 0.35f))
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Voice Typing Action
                        ActionToolChip(
                            icon = Icons.Default.Mic,
                            label = if (isVoiceListening) "Voice (Active)" else "Voice",
                            palette = palette,
                            isActive = isVoiceListening,
                            tag = "toolbar_voice_typing",
                            onClick = if (isVoiceListening) onStopVoice else onVoiceClick
                        )

                        // 2. Copypad / Clipboard Action
                        ActionToolChip(
                            icon = Icons.Default.ContentPaste,
                            label = if (clipboardCount > 0) "Clipboard ($clipboardCount)" else "Clipboard",
                            palette = palette,
                            isActive = clipboardCount > 0,
                            tag = "toolbar_clipboard",
                            onClick = onClipboardClick
                        )

                        // 2.1 TikTok Downloader Action
                        ActionToolChip(
                            icon = Icons.Default.Download,
                            label = "TikTok",
                            palette = palette,
                            isActive = tikTokState !is TikTokDownloadState.Idle,
                            tag = "toolbar_tiktok_downloader",
                            onClick = onTikTokToolbarClick
                        )

                        // 2. Text Edit Pad Action
                        ActionToolChip(
                            icon = Icons.Default.Edit,
                            label = "Text Edit",
                            palette = palette,
                            tag = "toolbar_text_edit",
                            onClick = onTextEditClick
                        )

                        // 3. Dialer / Numpad Action
                        ActionToolChip(
                            icon = Icons.Default.Dialpad,
                            label = "Numpad",
                            palette = palette,
                            tag = "toolbar_numpad",
                            onClick = onNumberPadClick
                        )

                        // 4. Password Vault Action
                        ActionToolChip(
                            icon = Icons.Default.Lock,
                            label = "Vault",
                            palette = palette,
                            tag = "toolbar_vault",
                            onClick = onVaultClick
                        )

                        // 3. Theme Library Action
                        ActionToolChip(
                            icon = Icons.Default.Palette,
                            label = "Themes",
                            palette = palette,
                            tag = "toolbar_themes",
                            onClick = onThemesClick,
                            onLongClick = onThemesLongClick
                        )

                        // 4. Language Switcher Action
                        val langLabel = when (currentLanguage.lowercase()) {
                            "bangla" -> "বাংলা"
                            "avro" -> "অভ্র"
                            else -> "English"
                        }
                        ActionToolChip(
                            icon = Icons.Default.Language,
                            label = langLabel,
                            palette = palette,
                            tag = "toolbar_language_toggle",
                            onClick = onLanguageCycle,
                            onLongClick = onLanguageLongPress
                        )

                        // 5. Vibration / Haptics Toggle Action
                        ActionToolChip(
                            icon = if (keyVibrationEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                            label = if (keyVibrationEnabled) "Vibration ON" else "Vibration OFF",
                            palette = palette,
                            isActive = keyVibrationEnabled,
                            tag = "toolbar_toggle_vibration",
                            onClick = onToggleVibration
                        )

                        // 6. Sound Toggle Action
                        ActionToolChip(
                            icon = if (keySoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            label = if (keySoundEnabled) "Sound ON" else "Sound OFF",
                            palette = palette,
                            isActive = keySoundEnabled,
                            tag = "toolbar_toggle_sound",
                            onClick = onToggleSound
                        )

                        // 7. One-Handed Mode Action
                        val oneHandLabel = when (oneHandedMode) {
                            "right" -> "Right Hand"
                            "left" -> "Left Hand"
                            else -> "Full Width"
                        }
                        ActionToolChip(
                            icon = Icons.Default.AspectRatio,
                            label = oneHandLabel,
                            palette = palette,
                            isActive = oneHandedMode != "none",
                            tag = "toolbar_one_handed",
                            onClick = onToggleOneHanded
                        )

                        // 8. Settings Action
                        ActionToolChip(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            palette = palette,
                            tag = "toolbar_settings",
                            onClick = onSettingsClick
                        )
                    }
                }
            }
        }

        // Bottom border line for geometric precision
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(palette.dividerColor.copy(alpha = 0.35f))
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActionToolChip(
    icon: ImageVector,
    label: String,
    palette: KeyboardPalette,
    isActive: Boolean = false,
    tag: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isActive) palette.accentColor.copy(alpha = 0.15f)
                else palette.keyBackground
            )
            .border(
                width = 0.8.dp,
                color = if (isActive) palette.accentColor.copy(alpha = 0.4f) else palette.keyBorderColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 9.dp, vertical = 6.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) palette.accentColor else palette.textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = if (isActive) palette.accentColor else palette.textColor,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
