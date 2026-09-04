package com.example.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.theme.KeyboardPalette

/**
 * Animated audio equalizer bars representing live voice input volume levels.
 */
@Composable
fun AudioEqualizerVisualizer(
    rmsLevel: Float,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_anim")
    val anim1 by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar1"
    )
    val anim2 by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 0.20f,
        animationSpec = infiniteRepeatable(tween(310, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar2"
    )
    val anim3 by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(490, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar3"
    )
    val anim4 by infiniteTransition.animateFloat(
        initialValue = 0.75f, targetValue = 0.30f,
        animationSpec = infiniteRepeatable(tween(370, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar4"
    )

    Canvas(modifier = modifier.size(width = 22.dp, height = 18.dp)) {
        val barCount = 4
        val spacing = 2.dp.toPx()
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = (size.width - totalSpacing) / barCount
        val heights = listOf(anim1, anim2, anim3, anim4)

        heights.forEachIndexed { i, animFactor ->
            val effectiveFactor = ((animFactor * 0.35f) + (rmsLevel * 0.65f)).coerceIn(0.18f, 1.0f)
            val barHeight = size.height * effectiveFactor
            val left = i * (barWidth + spacing)
            val top = (size.height - barHeight) / 2f

            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

/**
 * Live voice recognition content rendered right inside the suggestion bar while the keyboard
 * remains fully visible and active underneath.
 */
@Composable
fun LiveVoiceSuggestionContent(
    voiceStatus: String,
    voiceLiveText: String,
    rmsLevel: Float,
    errorMessage: String?,
    currentLanguage: String,
    palette: KeyboardPalette,
    onCancel: () -> Unit,
    onRequestPermission: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isBangla = currentLanguage.equals("bangla", ignoreCase = true)
    val isAvro = currentLanguage.equals("avro", ignoreCase = true)

    val listeningPrompt = when {
        isBangla -> "শুনছি... বলুন..."
        isAvro -> "শুনছি... বলুন / Speak..."
        else -> "Listening... speak..."
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_prompt")
    val promptAlpha by infiniteTransition.animateFloat(
        initialValue = 0.60f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "promptAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Cancel / Close voice mode button (✕)
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(palette.keyActionBackground.copy(alpha = 0.70f))
                .clickable { onCancel() }
                .testTag("voice_cancel_btn"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Stop Voice Typing",
                tint = palette.secondaryTextColor,
                modifier = Modifier.size(14.dp)
            )
        }

        // 2. Animated audio equalizer visualizer
        AudioEqualizerVisualizer(
            rmsLevel = rmsLevel,
            barColor = if (errorMessage != null) Color(0xFFE53935) else palette.accentColor
        )

        // 3. Central live text / status display
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (errorMessage != null) {
                // Error state or permission prompt
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFE53935),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (errorMessage.contains("permission", ignoreCase = true) && onRequestPermission != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.accentColor)
                                .clickable { onRequestPermission() }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .testTag("voice_grant_perm_inline_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Allow",
                                color = palette.onAccentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else if (voiceLiveText.isNotBlank()) {
                // Real-time live recognized speech text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(palette.accentColor.copy(alpha = 0.18f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            color = palette.accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = "“$voiceLiveText”",
                        color = palette.textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("voice_live_transcript")
                    )
                }
            } else {
                // Listening prompt (listion/bolun/speak.......)
                Text(
                    text = "( $listeningPrompt )",
                    color = palette.accentColor.copy(alpha = promptAlpha),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("voice_listening_prompt")
                )
            }
        }
    }
}

/**
 * Microphone button placed on the right side of the suggestion strip or toolbar.
 * Pulses with recording animation when active.
 */
@Composable
fun VoiceMicButton(
    isListening: Boolean,
    palette: KeyboardPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isListening) Color(0xFFE53935) else palette.accentColor.copy(alpha = 0.18f)
            )
            .border(
                width = 0.8.dp,
                color = if (isListening) Color(0xFFFF5252) else palette.accentColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("toolbar_voice_quick_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = if (isListening) "Stop Voice Typing" else "Start Voice Typing",
                tint = if (isListening) Color.White else palette.accentColor,
                modifier = Modifier
                    .size(16.dp)
                    .scale(if (isListening) pulseScale else 1.0f)
            )

            if (isListening) {
                // Live recording dot
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}
