package com.example.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.downloader.tiktok.TikTokDownloadState
import com.example.theme.KeyboardPalette

@Composable
fun TikTokSuggestionBar(
    state: TikTokDownloadState,
    palette: KeyboardPalette,
    onDownloadClicked: () -> Unit,
    onFormatChosen: (isAudio: Boolean) -> Unit,
    onQualityChosen: (quality: String) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onOpenFile: (filePath: String, isAudio: Boolean) -> Unit,
    onShareFile: (filePath: String, isAudio: Boolean) -> Unit,
    onPlayInKeyboard: ((filePath: String, isAudio: Boolean, title: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (state is TikTokDownloadState.Idle) return

    AnimatedVisibility(
        visible = state !is TikTokDownloadState.Idle,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(palette.keyBackground.copy(alpha = 0.95f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState())
                .testTag("tiktok_bar"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            when (state) {
                is TikTokDownloadState.LinkDetected -> {
                    SocialPlatformBadge(platformName = state.platformName, palette = palette)

                    Text(
                        text = "${state.platformName} link detected",
                        color = palette.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Download Action Chip
                    SocialActionButton(
                        icon = Icons.Default.Download,
                        label = "Download",
                        palette = palette,
                        isPrimary = true,
                        tag = "social_download_btn",
                        onClick = onDownloadClicked
                    )

                    // Dismiss Button
                    SocialDismissButton(palette = palette, onClick = onDismiss)
                }

                is TikTokDownloadState.ChoosingFormat -> {
                    SocialPlatformBadge(platformName = state.platformName, palette = palette)

                    Text(
                        text = "Choose format:",
                        color = palette.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    SocialActionButton(
                        icon = Icons.Default.Videocam,
                        label = "Video",
                        palette = palette,
                        isPrimary = true,
                        tag = "social_format_video",
                        onClick = { onFormatChosen(false) }
                    )

                    SocialActionButton(
                        icon = Icons.Default.Audiotrack,
                        label = "Audio (MP3)",
                        palette = palette,
                        isPrimary = false,
                        tag = "social_format_audio",
                        onClick = { onFormatChosen(true) }
                    )

                    SocialDismissButton(palette = palette, onClick = onDismiss)
                }

                is TikTokDownloadState.ChoosingQuality -> {
                    SocialPlatformBadge(platformName = state.platformName, palette = palette)

                    Text(
                        text = "Select quality:",
                        color = palette.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    SocialActionButton(
                        label = "SD (480p)",
                        palette = palette,
                        isPrimary = false,
                        tag = "social_quality_480p",
                        onClick = { onQualityChosen("480p SD") }
                    )

                    SocialActionButton(
                        label = "HD (720p)",
                        palette = palette,
                        isPrimary = true,
                        tag = "social_quality_720p",
                        onClick = { onQualityChosen("720p HD") }
                    )

                    SocialActionButton(
                        label = "Full HD (1080p)",
                        palette = palette,
                        isPrimary = true,
                        tag = "social_quality_1080p",
                        onClick = { onQualityChosen("1080p FHD") }
                    )

                    SocialDismissButton(palette = palette, onClick = onDismiss)
                }

                is TikTokDownloadState.Resolving -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = palette.accentColor
                    )

                    Text(
                        text = state.message,
                        color = palette.textColor,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    SocialDismissButton(palette = palette, onClick = onCancel)
                }

                is TikTokDownloadState.Downloading -> {
                    SocialPlatformBadge(platformName = state.platformName, palette = palette)

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .width(160.dp)
                            .padding(end = 4.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Downloading ${state.quality}",
                                color = palette.textColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (state.progress >= 0) "${state.progress}%" else "...",
                                color = palette.accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("social_progress_text")
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        if (state.progress >= 0) {
                            LinearProgressIndicator(
                                progress = { state.progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .testTag("social_progress_bar"),
                                color = palette.accentColor,
                                trackColor = palette.keyActionBackground
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .testTag("social_progress_bar"),
                                color = palette.accentColor,
                                trackColor = palette.keyActionBackground
                            )
                        }
                    }

                    // Cancel button
                    SocialActionButton(
                        icon = Icons.Default.Close,
                        label = "Cancel",
                        palette = palette,
                        isPrimary = false,
                        tag = "social_cancel_btn",
                        onClick = onCancel
                    )
                }

                is TikTokDownloadState.Success -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = "Downloaded! (${state.quality})",
                        color = palette.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // IN-KEYBOARD PLAY BUTTON (No need to open other apps!)
                    if (onPlayInKeyboard != null) {
                        SocialActionButton(
                            icon = Icons.Default.PlayArrow,
                            label = "Play in Keyboard",
                            palette = palette,
                            isPrimary = true,
                            tag = "social_play_keyboard_btn",
                            onClick = { onPlayInKeyboard(state.filePath, state.isAudio, state.title) }
                        )
                    }

                    SocialActionButton(
                        icon = Icons.Default.FolderOpen,
                        label = "Open",
                        palette = palette,
                        isPrimary = false,
                        tag = "social_open_btn",
                        onClick = { onOpenFile(state.filePath, state.isAudio) }
                    )

                    SocialActionButton(
                        icon = Icons.Default.Share,
                        label = "Share",
                        palette = palette,
                        isPrimary = false,
                        tag = "social_share_btn",
                        onClick = { onShareFile(state.filePath, state.isAudio) }
                    )

                    SocialDismissButton(palette = palette, onClick = onDismiss)
                }

                is TikTokDownloadState.Error -> {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = state.message,
                        color = palette.textColor,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    SocialActionButton(
                        icon = Icons.Default.Refresh,
                        label = "Retry",
                        palette = palette,
                        isPrimary = true,
                        tag = "social_retry_btn",
                        onClick = onRetry
                    )

                    SocialDismissButton(palette = palette, onClick = onDismiss)
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun SocialPlatformBadge(platformName: String, palette: KeyboardPalette) {
    val badgeColor = when {
        platformName.contains("Facebook", ignoreCase = true) -> Color(0xFF1877F2)
        platformName.contains("TikTok", ignoreCase = true) -> Color(0xFFFE2C55)
        else -> palette.accentColor
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .border(1.dp, badgeColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = platformName,
            color = badgeColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SocialActionButton(
    label: String,
    palette: KeyboardPalette,
    isPrimary: Boolean,
    tag: String,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isPrimary) palette.accentColor
                else palette.keyActionBackground.copy(alpha = 0.7f)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPrimary) Color.White else palette.textColor,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = label,
                color = if (isPrimary) Color.White else palette.textColor,
                fontSize = 11.sp,
                fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SocialDismissButton(palette: KeyboardPalette, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(4.dp)
            .testTag("social_dismiss_btn"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss",
            tint = palette.secondaryTextColor,
            modifier = Modifier.size(14.dp)
        )
    }
}
