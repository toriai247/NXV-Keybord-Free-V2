package com.example.ui.keyboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.theme.KeyboardPalette

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SUCCESS,
    ERROR,
    NO_PERMISSION
}

@Composable
fun VoiceTypingKeyboardLayout(
    currentLanguage: String,
    palette: KeyboardPalette,
    onCharTyped: (String) -> Unit,
    onDelete: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onClose: () -> Unit,
    onOpenSettingsForPermission: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Voice Language Target Mapping
    var activeLang by remember(currentLanguage) { mutableStateOf(currentLanguage.lowercase()) }
    val (langTag, langDisplayName) = remember(activeLang) {
        when (activeLang) {
            "bangla" -> "bn-BD" to "বাংলা (Bangla)"
            "avro" -> "bn-BD" to "অভ্র (Banglish/Bangla)"
            else -> "en-US" to "English (US)"
        }
    }

    var voiceState by remember { mutableStateOf(VoiceState.IDLE) }
    var statusText by remember { mutableStateOf("Tap microphone to speak") }
    var partialResultText by remember { mutableStateOf("") }
    var lastRecognizedText by remember { mutableStateOf("") }
    var audioRmsLevel by remember { mutableFloatStateOf(0f) }
    var errorMessage by remember { mutableStateOf("") }

    // Check Audio Permission
    val hasPermission = remember(context) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Speech Recognizer instance
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    fun startListening() {
        if (!hasPermission) {
            voiceState = VoiceState.NO_PERMISSION
            statusText = "Microphone permission needed"
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            voiceState = VoiceState.ERROR
            errorMessage = "Speech recognition is not available on this device."
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        voiceState = VoiceState.LISTENING
                        statusText = "🎙️ Listening in $langDisplayName..."
                        errorMessage = ""
                    }

                    override fun onBeginningOfSpeech() {
                        statusText = "🗣️ Speaking..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize rmsdB (typically 0..12) to 0.0 .. 1.0
                        audioRmsLevel = (rmsdB / 10f).coerceIn(0.1f, 1.0f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        voiceState = VoiceState.PROCESSING
                        statusText = "⏳ Converting voice to text..."
                        audioRmsLevel = 0f
                    }

                    override fun onError(error: Int) {
                        voiceState = VoiceState.ERROR
                        audioRmsLevel = 0f
                        errorMessage = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. Tap mic to retry."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network required for speech engine."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            else -> "Speech recognition error ($error)."
                        }
                        statusText = errorMessage
                    }

                    override fun onResults(results: Bundle?) {
                        audioRmsLevel = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val text = matches[0]
                            if (text.isNotBlank()) {
                                lastRecognizedText = text
                                partialResultText = ""
                                voiceState = VoiceState.SUCCESS
                                statusText = "✓ Typed: \"$text\""
                                onCharTyped("$text ")
                            } else {
                                voiceState = VoiceState.ERROR
                                statusText = "No speech detected. Tap mic to retry."
                            }
                        } else {
                            voiceState = VoiceState.ERROR
                            statusText = "No speech detected. Tap mic to retry."
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            partialResultText = matches[0]
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, langTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
            voiceState = VoiceState.LISTENING
            statusText = "Starting speech engine..."
        } catch (e: Exception) {
            voiceState = VoiceState.ERROR
            errorMessage = e.localizedMessage ?: "Failed to start speech recognizer."
            statusText = errorMessage
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        voiceState = VoiceState.IDLE
        statusText = "Tap microphone to speak"
        audioRmsLevel = 0f
    }

    // Auto-start listening on launch if permission is granted
    LaunchedEffect(hasPermission, langTag) {
        if (hasPermission) {
            startListening()
        } else {
            voiceState = VoiceState.NO_PERMISSION
            statusText = "Microphone permission required"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
        }
    }

    // Pulse animation when listening
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (voiceState == VoiceState.LISTENING) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    val animatedAudioRms by animateFloatAsState(
        targetValue = if (voiceState == VoiceState.LISTENING) audioRmsLevel else 0f,
        animationSpec = tween(100),
        label = "audio_rms"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.keyboardBackground)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Bar: Language Selector & Dismiss
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Language Selection Chips inside Voice Mode
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language",
                    tint = palette.accentColor,
                    modifier = Modifier.size(16.dp)
                )

                val languages = listOf("english" to "English", "bangla" to "বাংলা", "avro" to "অভ্র")
                languages.forEach { (code, name) ->
                    val isSel = activeLang == code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSel) palette.accentColor
                                else palette.keyBackground
                            )
                            .clickable {
                                activeLang = code
                                onLanguageSelected(code)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) palette.onAccentColor else palette.textColor
                        )
                    }
                }
            }

            // Close Voice Mode Button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(palette.keyActionBackground)
                    .clickable {
                        stopListening()
                        onClose()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Voice Mode",
                    tint = palette.textColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Central Mic & Speech Visualization
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (voiceState == VoiceState.NO_PERMISSION) {
                // Permission Request Box
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "No Permission",
                        tint = palette.accentColor,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Microphone Access Required",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = palette.textColor
                    )
                    Text(
                        text = "Allow microphone permission to use voice typing in $langDisplayName",
                        fontSize = 12.sp,
                        color = palette.secondaryTextColor,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            onOpenSettingsForPermission?.invoke()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.accentColor,
                            contentColor = palette.onAccentColor
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Grant Permission", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            } else {
                // Mic Button & Live Pulsing Waves
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(110.dp)
                    ) {
                        // Outer Pulsing Audio Volume Wave Ring
                        if (voiceState == VoiceState.LISTENING) {
                            Box(
                                modifier = Modifier
                                    .size((80 + (animatedAudioRms * 35)).dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(palette.accentColor.copy(alpha = 0.25f))
                            )
                            Box(
                                modifier = Modifier
                                    .size((70 + (animatedAudioRms * 20)).dp)
                                    .clip(CircleShape)
                                    .background(palette.accentColor.copy(alpha = 0.40f))
                            )
                        }

                        // Main Interactive Mic Button
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(
                                    when (voiceState) {
                                        VoiceState.LISTENING -> palette.accentColor
                                        VoiceState.PROCESSING -> palette.accentColor.copy(alpha = 0.7f)
                                        VoiceState.SUCCESS -> Color(0xFF2E7D32)
                                        VoiceState.ERROR -> Color(0xFFC62828)
                                        else -> palette.keyBackground
                                    }
                                )
                                .border(
                                    width = 2.dp,
                                    color = palette.keyBorderColor,
                                    shape = CircleShape
                                )
                                .clickable {
                                    if (voiceState == VoiceState.LISTENING) {
                                        stopListening()
                                    } else {
                                        startListening()
                                    }
                                }
                                .testTag("voice_mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (voiceState) {
                                    VoiceState.SUCCESS -> Icons.Default.CheckCircle
                                    VoiceState.ERROR -> Icons.Default.Refresh
                                    else -> Icons.Default.Mic
                                },
                                contentDescription = "Voice Mic",
                                tint = if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.SUCCESS) palette.onAccentColor else palette.textColor,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    // Status and Live Partial Transcript Preview
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (voiceState) {
                                VoiceState.LISTENING -> palette.accentColor
                                VoiceState.SUCCESS -> Color(0xFF4CAF50)
                                VoiceState.ERROR -> Color(0xFFEF5350)
                                else -> palette.secondaryTextColor
                            },
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (partialResultText.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.suggestionBarBackground)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🗣️ \"$partialResultText…\"",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else if (lastRecognizedText.isNotBlank() && voiceState == VoiceState.SUCCESS) {
                            Text(
                                text = "Last typed: \"$lastRecognizedText\"",
                                fontSize = 12.sp,
                                color = palette.secondaryTextColor,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Quick Action Key Row (Keyboard return, Space, Backspace, Enter)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Return to Normal Keyboard
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.keyActionBackground)
                    .clickable {
                        stopListening()
                        onClose()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "ABC Keyboard",
                        tint = palette.textColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("ABC", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.textColor)
                }
            }

            // Space key
            Box(
                modifier = Modifier
                    .weight(2f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.keyBackground)
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

            // Backspace key
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.keyActionBackground)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    tint = palette.textColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Enter key
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.accentColor)
                    .clickable { onEnter() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                    contentDescription = "Enter",
                    tint = palette.onAccentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
