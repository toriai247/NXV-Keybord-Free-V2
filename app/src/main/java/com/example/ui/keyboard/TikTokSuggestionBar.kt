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
                    // Badge
                    TikTokBadge(palette = palette)

                    Text(
                        text = "TikTok link detected",
                        color = palette.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Download Action Chip
                    TikTokActionButton(
                        icon = Icons.Default.Download,
                        label = "Download",
                        palette = palette,
                        isPrimary = true,
                        tag = "tiktok_download_btn",
                        onClick = onDownloadClicked
                    )

                    // Dismiss Button
                    TikTokDismissButton(palette = palette, onClick = onDismiss)
                }

                is TikTokDownloadState.ChoosingFormat -> {
                    TikTokBadge(palette = palette)

                    Text(
                        text = "Choose format:",
                        color = palette.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    TikTokActionButton(
                        icon = Icons.Default.Videocam,
                        label = "Video",
                        palette = palette,
                        isPrimary = true,
                        tag = "tiktok_format_video",
                        onClick = { onFormatChosen(false) }
                    )

                    TikTokActionButton(
                        icon = Icons.Default.Audiotrack,
                        label = "Audio (MP3)",
                        palette = palette,
                        isPrimary = false,
                        tag = "tiktok_format_audio",
                        onClick = { onFormatChosen(true) }
                    )

                    TikTokDismissButton(palette = palette, onClick = onDismiss)
                }

                is TikTokDownloadState.ChoosingQuality -> {
                    TikTokBadge(palette = palette)

                    Text(
                        text = "Select quality:",
                        color = palette.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    TikTokActionButton(
                        label = "480p",
                        palette = palette,
                        isPrimary = false,
                        tag = "tiktok_quality_480p",
                        onClick = { onQualityChosen("480p") }
                    )

                    TikTokActionButton(
                        label = "720p (HD)",
                        palette = palette,
                        isPrimary = true,
                        tag = "tiktok_quality_720p",
                        onClick = { onQualityChosen("720p") }
                    )

                    TikTokActionButton(
                        label = "1080p (FHD)",
                        palette = palette,
                        isPrimary = true,
                        tag = "tiktok_quality_1080p",
                        onClick = { onQualityChosen("1080p") }
                    )

                    TikTokDismissButton(palette = palette, onClick = onDismiss)
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

                    TikTokDismissButton(palette = palette, onClick = onCancel)
                }

                is TikTokDownloadState.Downloading -> {
                    TikTokBadge(palette = palette)

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .width(150.dp)
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
                                modifier = Modifier.testTag("tiktok_progress_text")
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
                                    .testTag("tiktok_progress_bar"),
                                color = palette.accentColor,
                                trackColor = palette.keyActionBackground
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .testTag("tiktok_progress_bar"),
                                color = palette.accentColor,
                                trackColor = palette.keyActionBackground
                            )
                        }
                    }

                    // Cancel button
                    TikTokActionButton(
                        icon = Icons.Default.Close,
                        label = "Cancel",
                        palette = palette,
                        isPrimary = false,
                        tag = "tiktok_cancel_btn",
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

                    TikTokActionButton(
                        icon = Icons.Default.FolderOpen,
                        label = "Open",
                        palette = palette,
                        isPrimary = true,
                        tag = "tiktok_open_btn",
                        onClick = { onOpenFile(state.filePath, state.isAudio) }
                    )

                    TikTokActionButton(
                        icon = Icons.Default.Share,
                        label = "Share",
                        palette = palette,
                        isPrimary = false,
                        tag = "tiktok_share_btn",
                        onClick = { onShareFile(state.filePath, state.isAudio) }
                    )

                    TikTokDismissButton(palette = palette, onClick = onDismiss)
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

                    TikTokActionButton(
                        icon = Icons.Default.Refresh,
                        label = "Retry",
                        palette = palette,
                        isPrimary = true,
                        tag = "tiktok_retry_btn",
                        onClick = onRetry
                    )

                    TikTokDismissButton(palette = palette, onClick = onDismiss)
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun TikTokBadge(palette: KeyboardPalette) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFFE2C55).copy(alpha = 0.15f))
            .border(1.dp, Color(0xFFFE2C55).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "TikTok",
            color = Color(0xFFFE2C55),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TikTokActionButton(
    label: String,
    palette: KeyboardPalette,
    isPrimary: Boolean,
    tag: String,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    val bg = if (isPrimary) palette.accentColor else palette.keyBackground
    val fg = if (isPrimary) palette.onAccentColor else palette.textColor

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 5.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = label,
                color = fg,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TikTokDismissButton(
    palette: KeyboardPalette,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(palette.keyBackground.copy(alpha = 0.8f))
            .clickable { onClick() }
            .testTag("tiktok_dismiss_btn"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss",
            tint = palette.textColor.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
    }
}
