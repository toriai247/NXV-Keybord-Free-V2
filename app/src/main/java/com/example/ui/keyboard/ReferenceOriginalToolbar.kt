package com.example.ui.keyboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suggestion.SuggestionItem
import com.example.theme.KeyboardPalette
import com.example.theme.ReferenceMinimalIcons

/**
 * Top Toolbar matching the reference keyboard for "Reference Minimal" (Original UI).
 * Features 5 horizontally distributed native controls:
 * 1. Grid/Options
 * 2. Emoji/Smiley
 * 3. Clipboard/Document
 * 4. Text Cursor/Editing
 * 5. Search
 *
 * Automatically and seamlessly displays suggestions when text is actively being typed or predicted.
 */
@Composable
fun ReferenceOriginalToolbar(
    suggestions: List<SuggestionItem>,
    palette: KeyboardPalette,
    onSuggestionClick: (SuggestionItem) -> Unit,
    onGridClick: () -> Unit,
    onEmojiClick: () -> Unit,
    onClipboardClick: () -> Unit,
    onTextEditClick: () -> Unit,
    onSearchClick: () -> Unit,
    onVoiceClick: () -> Unit = {},
    isVoiceListening: Boolean = false,
    voiceLiveText: String = "",
    voiceStatusText: String = "",
    voiceRmsLevel: Float = 0f,
    voiceErrorMessage: String? = null,
    currentLanguage: String = "english",
    onStopVoice: () -> Unit = {},
    onRequestVoicePermission: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var forceShowTools by remember { mutableStateOf(false) }
    val showSuggestions = (suggestions.isNotEmpty() || isVoiceListening) && !forceShowTools

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFFFFFFFF))
            .testTag("reference_original_toolbar")
    ) {
        if (isVoiceListening) {
            // In-place live voice bar with mic button on the right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                VoiceMicButton(
                    isListening = true,
                    palette = palette,
                    onClick = onStopVoice,
                    modifier = Modifier.padding(start = 2.dp, end = 6.dp)
                )
            }
        } else {
            AnimatedContent(
                targetState = showSuggestions,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "toolbar_content_switch"
            ) { isShowingSuggestions ->
                if (isShowingSuggestions) {
                    // Seamless minimalist suggestion row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Small grid button to quickly toggle back to tools
                        ToolbarControlButton(
                            onClick = { forceShowTools = true },
                            modifier = Modifier.width(44.dp),
                            testTag = "toolbar_toggle_tools_btn"
                        ) {
                            ReferenceMinimalIcons.GridAppsIcon(size = 18.dp)
                        }

                        // Thin divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight(0.6f)
                                .background(Color(0xFFE0E0E0))
                        )

                        // Horizontally scrollable suggestions
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            suggestions.forEachIndexed { index, item ->
                                val isPrimary = index == 0
                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()

                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isPressed) Color(0xFFEEEEEE) else Color.Transparent)
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) {
                                            onSuggestionClick(item)
                                        }
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.displayText,
                                        fontSize = 16.sp,
                                        fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Normal,
                                        color = Color(0xFF111111),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Thin divider between suggestions
                                if (index < suggestions.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .fillMaxHeight(0.45f)
                                            .background(Color(0xFFE0E0E0))
                                    )
                                }
                            }
                        }

                        // Right-side Voice Mic Button
                        VoiceMicButton(
                            isListening = false,
                            palette = palette,
                            onClick = onVoiceClick,
                            modifier = Modifier.padding(start = 2.dp, end = 6.dp)
                        )
                    }
                } else {
                // 5 Horizontally distributed minimalist controls (from reference screenshot)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Grid / options icon
                    ToolbarControlButton(
                        onClick = {
                            forceShowTools = false
                            onGridClick()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "toolbar_btn_grid"
                    ) {
                        ReferenceMinimalIcons.GridAppsIcon(size = 20.dp)
                    }

                    // Thin divider
                    ToolbarDivider()

                    // 2. Emoji / smiley icon
                    ToolbarControlButton(
                        onClick = {
                            forceShowTools = false
                            onEmojiClick()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "toolbar_btn_emoji"
                    ) {
                        ReferenceMinimalIcons.SmileyEmojiIcon(size = 20.dp)
                    }

                    // Thin divider
                    ToolbarDivider()

                    // 3. Clipboard / document icon
                    ToolbarControlButton(
                        onClick = {
                            forceShowTools = false
                            onClipboardClick()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "toolbar_btn_clipboard"
                    ) {
                        ReferenceMinimalIcons.DocumentClipboardIcon(size = 20.dp)
                    }

                    // Thin divider
                    ToolbarDivider()

                    // 4. Text cursor / editing icon (< I >)
                    ToolbarControlButton(
                        onClick = {
                            forceShowTools = false
                            onTextEditClick()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "toolbar_btn_text_edit"
                    ) {
                        ReferenceMinimalIcons.CursorEditIcon(size = 20.dp)
                    }

                    // Thin divider
                    ToolbarDivider()

                    // 5. Search icon
                    ToolbarControlButton(
                        onClick = {
                            forceShowTools = false
                            onSearchClick()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "toolbar_btn_search"
                    ) {
                        ReferenceMinimalIcons.SearchIcon(size = 20.dp)
                    }
                }
            }
        }
    }

        // Bottom border line (0.8dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.BottomCenter)
                .background(Color(0xFFE0E0E0))
        )
    }
}

@Composable
private fun ToolbarControlButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(2.dp))
            .background(if (isPressed) Color(0xFFEEEEEE) else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun ToolbarDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight(0.5f)
            .background(Color(0xFFEAEAEA))
    )
}
