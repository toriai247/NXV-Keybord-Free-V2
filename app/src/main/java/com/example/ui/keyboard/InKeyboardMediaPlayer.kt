package com.example.ui.keyboard

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.theme.KeyboardPalette
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

@Composable
fun InKeyboardMediaPlayer(
    filePath: String,
    isAudio: Boolean,
    title: String,
    palette: KeyboardPalette,
    onClosePlayer: () -> Unit,
    onOpenExternal: (filePath: String, isAudio: Boolean) -> Unit,
    onShare: (filePath: String, isAudio: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(0) }
    var isDraggingSeek by remember { mutableStateOf(false) }
    var seekProgress by remember { mutableFloatStateOf(0f) }

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var audioPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    // Audio Disc rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_angle"
    )

    // Periodic position updater
    LaunchedEffect(isPlaying, isDraggingSeek) {
        while (true) {
            if (!isDraggingSeek) {
                if (isAudio) {
                    audioPlayerRef?.let { mp ->
                        try {
                            if (mp.isPlaying) {
                                currentPositionMs = mp.currentPosition
                                durationMs = mp.duration.coerceAtLeast(1)
                            }
                        } catch (_: Exception) {}
                    }
                } else {
                    videoViewRef?.let { vv ->
                        try {
                            if (vv.isPlaying) {
                                currentPositionMs = vv.currentPosition
                                durationMs = vv.duration.coerceAtLeast(1)
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
            delay(250)
        }
    }

    // Audio media player setup
    DisposableEffect(filePath, isAudio) {
        if (isAudio) {
            val file = File(filePath)
            if (file.exists()) {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(context, Uri.fromFile(file))
                    prepare()
                    start()
                    setOnCompletionListener {
                        isPlaying = false
                        currentPositionMs = duration
                    }
                }
                audioPlayerRef = mp
                durationMs = mp.duration.coerceAtLeast(1)
                isPlaying = true
            }
        }

        onDispose {
            try {
                audioPlayerRef?.stop()
                audioPlayerRef?.release()
            } catch (_: Exception) {}
            audioPlayerRef = null

            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {}
            videoViewRef = null
        }
    }

    fun togglePlayPause() {
        if (isAudio) {
            audioPlayerRef?.let { mp ->
                try {
                    if (mp.isPlaying) {
                        mp.pause()
                        isPlaying = false
                    } else {
                        if (currentPositionMs >= durationMs) {
                            mp.seekTo(0)
                        }
                        mp.start()
                        isPlaying = true
                    }
                } catch (_: Exception) {}
            }
        } else {
            videoViewRef?.let { vv ->
                try {
                    if (vv.isPlaying) {
                        vv.pause()
                        isPlaying = false
                    } else {
                        if (currentPositionMs >= durationMs) {
                            vv.seekTo(0)
                        }
                        vv.start()
                        isPlaying = true
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun seekToFraction(fraction: Float) {
        val targetMs = (fraction * durationMs).toInt().coerceIn(0, durationMs)
        currentPositionMs = targetMs
        if (isAudio) {
            audioPlayerRef?.seekTo(targetMs)
        } else {
            videoViewRef?.seekTo(targetMs)
        }
    }

    fun forward10() {
        val target = (currentPositionMs + 10000).coerceAtMost(durationMs)
        currentPositionMs = target
        if (isAudio) audioPlayerRef?.seekTo(target) else videoViewRef?.seekTo(target)
    }

    fun replay10() {
        val target = (currentPositionMs - 10000).coerceAtLeast(0)
        currentPositionMs = target
        if (isAudio) audioPlayerRef?.seekTo(target) else videoViewRef?.seekTo(target)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(palette.keyboardBackground)
            .testTag("in_keyboard_media_player")
    ) {
        // Top Header Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(palette.suggestionBarBackground)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Back to typing chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.keyBackground)
                    .clickable { onClosePlayer() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("player_btn_back_typing")
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Back to Typing",
                    tint = palette.accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Typing",
                    color = palette.textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Center: Title badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = if (isAudio) Icons.Default.Audiotrack else Icons.Default.Videocam,
                    contentDescription = null,
                    tint = palette.accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (title.isNotBlank()) title else if (isAudio) "Downloaded Audio" else "Downloaded Video",
                    color = palette.textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right: Pop-out external & Share buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.keyBackground)
                        .clickable { onOpenExternal(filePath, isAudio) }
                        .padding(5.dp)
                        .testTag("player_btn_open_external"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open in app",
                        tint = palette.textColor,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.keyBackground)
                        .clickable { onShare(filePath, isAudio) }
                        .padding(5.dp)
                        .testTag("player_btn_share"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = palette.textColor,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.keyBackground)
                        .clickable { onClosePlayer() }
                        .padding(5.dp)
                        .testTag("player_btn_close"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = palette.textColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // Center Viewport: Video or Audio Visualizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0A0A0A)),
            contentAlignment = Alignment.Center
        ) {
            if (isAudio) {
                // Audio Vinyl Disc + Waves Animation
                Row(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Spinning vinyl record
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .rotate(if (isPlaying) rotationAngle else 0f)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1E1E))
                            .border(3.dp, Color(0xFF333333), CircleShape)
                            .border(1.dp, palette.accentColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(palette.accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (title.isNotBlank()) title else "Audio Track",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPlaying) "Playing in-keyboard..." else "Paused",
                            color = palette.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // VideoView Player
                AndroidView(
                    factory = { ctx ->
                        val frameLayout = FrameLayout(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                        val videoView = VideoView(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            ).apply {
                                gravity = android.view.Gravity.CENTER
                            }
                            setVideoPath(filePath)
                            setOnPreparedListener { mp ->
                                durationMs = mp.duration.coerceAtLeast(1)
                                if (isMuted) {
                                    mp.setVolume(0f, 0f)
                                }
                                start()
                                isPlaying = true
                            }
                            setOnCompletionListener {
                                isPlaying = false
                                currentPositionMs = durationMs
                            }
                            setOnClickListener {
                                togglePlayPause()
                            }
                        }
                        frameLayout.addView(videoView)
                        videoViewRef = videoView
                        frameLayout
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Large center overlay play button when paused
                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .clickable { togglePlayPause() }
                            .testTag("player_center_play_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
        }

        // Bottom Controls: Scrub Slider + Time + Playback buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.keyBackground.copy(alpha = 0.95f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            // Seek bar with time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPositionMs),
                    color = palette.textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )

                Slider(
                    value = if (durationMs > 0) {
                        (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
                    } else 0f,
                    onValueChange = { fraction ->
                        isDraggingSeek = true
                        seekProgress = fraction
                        currentPositionMs = (fraction * durationMs).toInt()
                    },
                    onValueChangeFinished = {
                        isDraggingSeek = false
                        seekToFraction(seekProgress)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = palette.accentColor,
                        activeTrackColor = palette.accentColor,
                        inactiveTrackColor = palette.secondaryTextColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .padding(horizontal = 6.dp)
                        .testTag("player_seek_bar")
                )

                Text(
                    text = formatTime(durationMs),
                    color = palette.secondaryTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Action row: Replay 10s, Play/Pause, Forward 10s, Mute
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Replay 10s
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { replay10() }
                        .padding(6.dp)
                        .testTag("player_btn_replay10"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10s",
                        tint = palette.textColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Primary Play / Pause Toggle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(palette.accentColor)
                        .clickable { togglePlayPause() }
                        .testTag("player_btn_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Forward 10s
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { forward10() }
                        .padding(6.dp)
                        .testTag("player_btn_forward10"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Forward 10s",
                        tint = palette.textColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
