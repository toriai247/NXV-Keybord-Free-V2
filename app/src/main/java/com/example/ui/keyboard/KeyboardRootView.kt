package com.example.ui.keyboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import android.content.Context
import com.example.downloader.tiktok.TikTokDownloadManager
import com.example.downloader.tiktok.TikTokDownloadState
import com.example.data.entity.ClipboardItem
import com.example.data.entity.SavedCredential
import com.example.data.preferences.KeyboardSettings
import com.example.sticker.StickerItem
import com.example.ime.FeedbackManager
import com.example.keyboard.KeyboardMode
import com.example.keyboard.OneHandedMode
import com.example.keyboard.ShiftState
import com.example.language.bangla.BanglaLayouts
import com.example.language.english.EnglishEngine
import com.example.suggestion.SuggestionItem
import com.example.theme.KeyboardThemes

@Composable
fun KeyboardRootView(
    settings: KeyboardSettings,
    currentMode: KeyboardMode,
    shiftState: ShiftState,
    suggestions: List<SuggestionItem>,
    clipboardItems: List<ClipboardItem>,
    savedCredentials: List<SavedCredential> = emptyList(),
    autoSavePrompt: AutoSavePromptData? = null,
    enterLabel: String,
    feedbackManager: FeedbackManager,
    onCharTyped: (String) -> Unit,
    onDelete: () -> Unit,
    onSpace: () -> Unit,
    onSpaceDrag: ((Float) -> Unit)? = null,
    onEnter: () -> Unit,
    onShiftToggle: () -> Unit,
    onModeSwitch: (KeyboardMode) -> Unit,
    onLanguageCycle: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onSuggestionClicked: (SuggestionItem) -> Unit,
    onPasteClipboard: (String, Boolean) -> Unit,
    onTogglePinClipboard: (ClipboardItem) -> Unit,
    onDeleteClipboard: (Long) -> Unit,
    onClearClipboard: () -> Unit,
    onSyncClipboard: () -> Unit = {},
    onStickerSelected: (StickerItem) -> Unit = {},
    onSaveCredential: (service: String, user: String, pass: String) -> Unit = { _, _, _ -> },
    onDeleteCredential: (Long) -> Unit = {},
    onTogglePinCredential: (SavedCredential) -> Unit = {},
    onMoveCursorUp: (Boolean) -> Unit = {},
    onMoveCursorDown: (Boolean) -> Unit = {},
    onMoveCursorLeft: (Boolean) -> Unit = {},
    onMoveCursorRight: (Boolean) -> Unit = {},
    onMoveToStartOfLine: (Boolean) -> Unit = {},
    onMoveToEndOfLine: (Boolean) -> Unit = {},
    onSelectAll: () -> Unit = {},
    onCopyText: () -> Unit = {},
    onPasteText: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenThemes: (() -> Unit)? = null,
    onUpdateTheme: ((String) -> Unit)? = null,
    onToggleVibration: ((Boolean) -> Unit)? = null,
    onToggleSound: ((Boolean) -> Unit)? = null,
    onUpdateOneHanded: (String) -> Unit,
    onUpdateOneHandedHeightDp: ((Int) -> Unit)? = null,
    onUpdateOneHandedTheme: ((String) -> Unit)? = null,
    onUpdateOneHandedRotateText: ((Boolean) -> Unit)? = null,
    onUpdateOneHandedArcScale: ((Float) -> Unit)? = null,
    onUpdateOneHandedShowSuggestions: ((Boolean) -> Unit)? = null,
    onUpdateOneHandedKeyStyle: ((String) -> Unit)? = null,
    onVoiceLiveInput: ((text: String, isFinal: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = remember(settings.theme) {
        KeyboardThemes.getPalette(settings.theme)
    }

    val context = LocalContext.current
    val view = LocalView.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLanguageSlider by remember { mutableStateOf(false) }
    var showThemeGridPicker by remember { mutableStateOf(false) }
    var isToolbarExpanded by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }

    // TikTok & Social Downloader State
    val tikTokManager = remember { TikTokDownloadManager.getInstance(context) }
    val tikTokState by tikTokManager.state.collectAsState()

    // In-Keyboard Media Player State
    var activeMediaPlayerPath by remember { mutableStateOf<String?>(null) }
    var activeMediaPlayerIsAudio by remember { mutableStateOf(false) }
    var activeMediaPlayerTitle by remember { mutableStateOf("") }

    // Auto-detect TikTok URL when clipboard items update
    LaunchedEffect(clipboardItems) {
        val latestClip = clipboardItems.firstOrNull()?.text
        if (!latestClip.isNullOrBlank()) {
            tikTokManager.onClipboardUpdated(latestClip)
        }
    }

    // Live in-place Voice Typing state
    var isVoiceListening by remember { mutableStateOf(false) }
    var voiceLiveText by remember { mutableStateOf("") }
    var voiceStatusText by remember { mutableStateOf("") }
    var voiceRmsLevel by remember { mutableFloatStateOf(0f) }
    var voiceErrorMessage by remember { mutableStateOf<String?>(null) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
            speechRecognizer = null
        }
    }

    fun stopVoiceListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        isVoiceListening = false
        voiceLiveText = ""
        voiceStatusText = ""
        voiceRmsLevel = 0f
        voiceErrorMessage = null
        onVoiceLiveInput?.invoke("", true)
    }

    fun startVoiceListening() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            VoicePermissionActivity.requestPermission(context) { granted ->
                if (granted) {
                    startVoiceListening()
                }
            }
            isVoiceListening = true
            voiceErrorMessage = "Microphone permission required."
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            isVoiceListening = true
            voiceErrorMessage = "Speech recognition unavailable on device."
            return
        }

        voiceErrorMessage = null
        voiceLiveText = ""
        isVoiceListening = true

        val isBangla = settings.currentLanguage.equals("bangla", ignoreCase = true)
        val isAvro = settings.currentLanguage.equals("avro", ignoreCase = true)
        val langTag = if (isBangla || isAvro) "bn-BD" else "en-US"

        voiceStatusText = if (isBangla) "শুনছি... বলুন..." else if (isAvro) "শুনছি... বলুন / Speak..." else "Listening... speak..."

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        voiceStatusText = if (isBangla) "শুনছি... বলুন..." else if (isAvro) "শুনছি... বলুন / Speak..." else "Listening... speak..."
                        voiceErrorMessage = null
                    }

                    override fun onBeginningOfSpeech() {
                        voiceStatusText = if (isBangla) "শুনছি..." else "Listening..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        voiceRmsLevel = (rmsdB / 10f).coerceIn(0.1f, 1f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        voiceStatusText = if (isBangla) "প্রসেসিং..." else "Processing..."
                        voiceRmsLevel = 0f
                    }

                    override fun onError(error: Int) {
                        voiceRmsLevel = 0f
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> if (isBangla) "কিছু শোনা যায়নি" else "No speech heard"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> if (isBangla) "সময় শেষ" else "Speech timeout"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission required"
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network required"
                            else -> "Voice error ($error)"
                        }
                        voiceErrorMessage = msg
                        voiceStatusText = msg
                    }

                    override fun onResults(results: Bundle?) {
                        voiceRmsLevel = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val text = matches[0]
                            if (text.isNotBlank()) {
                                voiceLiveText = text
                                voiceStatusText = "✓ \"$text\""
                                if (onVoiceLiveInput != null) {
                                    onVoiceLiveInput.invoke(text, true)
                                } else {
                                    onCharTyped("$text ")
                                }
                            }
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val partial = matches[0]
                            voiceLiveText = partial
                            onVoiceLiveInput?.invoke(partial, false)
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
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-BD", "en-US"))
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            voiceErrorMessage = e.localizedMessage ?: "Failed to start speech engine."
            voiceStatusText = voiceErrorMessage ?: ""
        }
    }

    fun playFeedback() {
        feedbackManager.playKeySound(settings.keySoundEnabled)
        feedbackManager.performHapticFeedback(view, settings.keyVibrationEnabled, settings.vibrationStrength)
    }

    val keyHeight = (44.dp * settings.keyboardHeightRatio).coerceIn(38.dp, 56.dp)
    val oneHandedMode = OneHandedMode.fromString(settings.oneHandedMode)

    // Smooth layout transformation animation
    val animWidthFraction by animateFloatAsState(
        targetValue = if (oneHandedMode == OneHandedMode.NORMAL) 1f else 0.82f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "one_handed_width_anim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.keyboardBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top border line for geometric precision
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(palette.dividerColor.copy(alpha = 0.5f))
            )

            // Hold progress indicator when activating 5-second gesture
            if (holdProgress > 0f) {
                OneHandedHoldProgressBar(
                    progress = holdProgress,
                    isOneHandedActive = oneHandedMode != OneHandedMode.NORMAL,
                    palette = palette
                )
            }

            // Main layout container (Curved Radial Arc layout when in one-handed mode, standard layout when normal)
            if (oneHandedMode != OneHandedMode.NORMAL &&
                !(palette.themeId == "reference_minimal" && settings.uiMode == "original") &&
                (currentMode == KeyboardMode.ENGLISH || currentMode == KeyboardMode.BANGLA ||
                 currentMode == KeyboardMode.AVRO || currentMode == KeyboardMode.NUMBERS || currentMode == KeyboardMode.SYMBOLS)
            ) {
                CurvedArcKeyboardLayout(
                    mode = oneHandedMode,
                    currentLanguage = settings.currentLanguage,
                    shiftState = shiftState,
                    suggestions = suggestions,
                    palette = palette,
                    enterLabel = enterLabel,
                    oneHandedHeightDp = settings.oneHandedHeightDp,
                    oneHandedTheme = settings.oneHandedTheme,
                    oneHandedRotateText = settings.oneHandedRotateText,
                    oneHandedArcScale = settings.oneHandedArcScale,
                    oneHandedShowSuggestions = settings.oneHandedShowSuggestions,
                    oneHandedKeyStyle = settings.oneHandedKeyStyle,
                    onCharTyped = {
                        playFeedback()
                        val isLetter = it.length == 1 && it[0].isLetter()
                        val finalChar = if (isLetter) {
                            if (shiftState.isUppercase) it.uppercase() else it.lowercase()
                        } else {
                            it
                        }
                        onCharTyped(finalChar)
                    },
                    onDelete = {
                        playFeedback()
                        onDelete()
                    },
                    onSpace = {
                        playFeedback()
                        onSpace()
                    },
                    onEnter = {
                        playFeedback()
                        onEnter()
                    },
                    onShiftToggle = {
                        playFeedback()
                        onShiftToggle()
                    },
                    onSwitchMode = { mode ->
                        playFeedback()
                        onModeSwitch(mode)
                    },
                    onLanguageCycle = {
                        playFeedback()
                        onLanguageCycle()
                    },
                    onSuggestionClicked = { sugg ->
                        playFeedback()
                        onSuggestionClicked(sugg)
                    },
                    onHoldProgressUpdate = { progress ->
                        holdProgress = progress
                    },
                    onFiveSecondHoldComplete = {
                        playFeedback()
                        holdProgress = 0f
                        onUpdateOneHanded("none")
                    },
                    onHoldCancelled = {
                        holdProgress = 0f
                    },
                    onSwitchSide = {
                        playFeedback()
                        val next = if (oneHandedMode == OneHandedMode.RIGHT) "left" else "right"
                        onUpdateOneHanded(next)
                    },
                    onExpandNormal = {
                        playFeedback()
                        onUpdateOneHanded("none")
                    },
                    onUpdateOneHandedHeightDp = onUpdateOneHandedHeightDp,
                    onUpdateOneHandedTheme = onUpdateOneHandedTheme,
                    onUpdateOneHandedRotateText = onUpdateOneHandedRotateText,
                    onUpdateOneHandedArcScale = onUpdateOneHandedArcScale,
                    onUpdateOneHandedShowSuggestions = onUpdateOneHandedShowSuggestions,
                    onUpdateOneHandedKeyStyle = onUpdateOneHandedKeyStyle
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                // Left dock control if in RIGHT-handed mode
                if (oneHandedMode == OneHandedMode.RIGHT) {
                    OneHandedSideDock(
                        mode = oneHandedMode,
                        palette = palette,
                        onSwitchSide = {
                            playFeedback()
                            onUpdateOneHanded("left")
                        },
                        onExpand = {
                            playFeedback()
                            onUpdateOneHanded("none")
                        }
                    )
                }

                // Center Column containing SuggestionStrip and Keyboard body
                Column(
                    modifier = Modifier.weight(animWidthFraction)
                ) {
                    // Suggestion Strip (shown unless in Emoji, Stickers, Clipboard, or Vault mode)
                    if (currentMode != KeyboardMode.EMOJI && currentMode != KeyboardMode.STICKERS && currentMode != KeyboardMode.CLIPBOARD && currentMode != KeyboardMode.VAULT) {
                        if (settings.suggestionsEnabled) {
                            if (palette.themeId == "reference_minimal" && settings.uiMode == "original") {
                                ReferenceOriginalToolbar(
                                    suggestions = suggestions,
                                    palette = palette,
                                    isVoiceListening = isVoiceListening,
                                    voiceLiveText = voiceLiveText,
                                    voiceStatusText = voiceStatusText,
                                    voiceRmsLevel = voiceRmsLevel,
                                    voiceErrorMessage = voiceErrorMessage,
                                    currentLanguage = settings.currentLanguage,
                                    onStopVoice = {
                                        playFeedback()
                                        stopVoiceListening()
                                    },
                                    onRequestVoicePermission = {
                                        VoicePermissionActivity.requestPermission(context) { granted ->
                                            if (granted) startVoiceListening()
                                        }
                                    },
                                    onSuggestionClick = { sugg ->
                                        playFeedback()
                                        onSuggestionClicked(sugg)
                                    },
                                    onGridClick = {
                                        playFeedback()
                                        onOpenSettings()
                                    },
                                    onEmojiClick = {
                                        playFeedback()
                                        onModeSwitch(KeyboardMode.EMOJI)
                                    },
                                    onClipboardClick = {
                                        playFeedback()
                                        onSyncClipboard()
                                        onModeSwitch(KeyboardMode.CLIPBOARD)
                                    },
                                    onTextEditClick = {
                                        playFeedback()
                                        onModeSwitch(KeyboardMode.TEXT_EDIT)
                                    },
                                    onSearchClick = {
                                        playFeedback()
                                        onEnter()
                                    },
                                    onVoiceClick = {
                                        playFeedback()
                                        if (isVoiceListening) {
                                            stopVoiceListening()
                                        } else {
                                            startVoiceListening()
                                        }
                                    }
                                )
                            } else {
                                SuggestionStrip(
                                    suggestions = suggestions,
                                    palette = palette,
                                    currentLanguage = settings.currentLanguage,
                                    autoSavePrompt = autoSavePrompt,
                                    recentClip = clipboardItems.firstOrNull(),
                                    clipboardCount = clipboardItems.size,
                                    isToolbarExpanded = isToolbarExpanded,
                                    keyVibrationEnabled = settings.keyVibrationEnabled,
                                    keySoundEnabled = settings.keySoundEnabled,
                                    oneHandedMode = settings.oneHandedMode,
                                    isVoiceListening = isVoiceListening,
                                    voiceLiveText = voiceLiveText,
                                    voiceStatusText = voiceStatusText,
                                    voiceRmsLevel = voiceRmsLevel,
                                    voiceErrorMessage = voiceErrorMessage,
                                    onStopVoice = {
                                        playFeedback()
                                        stopVoiceListening()
                                    },
                                    onRequestVoicePermission = {
                                        VoicePermissionActivity.requestPermission(context) { granted ->
                                            if (granted) startVoiceListening()
                                        }
                                    },
                                    onToggleToolbar = {
                                        playFeedback()
                                        isToolbarExpanded = !isToolbarExpanded
                                    },
                                    onSuggestionClick = {
                                        playFeedback()
                                        onSuggestionClicked(it)
                                    },
                                    onClipboardClick = {
                                        playFeedback()
                                        onSyncClipboard()
                                        onModeSwitch(KeyboardMode.CLIPBOARD)
                                    },
                                    onQuickPaste = { text ->
                                        playFeedback()
                                        onPasteClipboard(text, true)
                                    },
                                    onVaultClick = {
                                        playFeedback()
                                        onModeSwitch(KeyboardMode.VAULT)
                                    },
                                    onTextEditClick = {
                                        playFeedback()
                                        onModeSwitch(KeyboardMode.TEXT_EDIT)
                                    },
                                    onNumberPadClick = {
                                        playFeedback()
                                        onModeSwitch(KeyboardMode.NUMBER_PAD)
                                    },
                                    onStickersClick = {
                                        playFeedback()
                                        onModeSwitch(KeyboardMode.STICKERS)
                                    },
                                    onVoiceClick = {
                                        playFeedback()
                                        if (isVoiceListening) {
                                            stopVoiceListening()
                                        } else {
                                            startVoiceListening()
                                        }
                                    },
                                    onThemesClick = {
                                        playFeedback()
                                        val allThemes = KeyboardThemes.ALL_THEMES
                                        val otherThemes = allThemes.filter { !it.themeId.equals(settings.theme, ignoreCase = true) }
                                        val randomTheme = (if (otherThemes.isNotEmpty()) otherThemes else allThemes).random()
                                        onUpdateTheme?.invoke(randomTheme.themeId)
                                    },
                                    onThemesLongClick = {
                                        playFeedback()
                                        showThemeGridPicker = true
                                    },
                                    onToggleVibration = {
                                        val newVib = !settings.keyVibrationEnabled
                                        onToggleVibration?.invoke(newVib)
                                        if (newVib) {
                                            feedbackManager.performHapticFeedback(view, true, settings.vibrationStrength)
                                        }
                                    },
                                    onToggleSound = {
                                        val newSound = !settings.keySoundEnabled
                                        onToggleSound?.invoke(newSound)
                                        if (newSound) {
                                            feedbackManager.playKeySound(true)
                                        }
                                    },
                                    onToggleOneHanded = {
                                        playFeedback()
                                        val next = when (settings.oneHandedMode) {
                                            "none" -> settings.preferredHand.ifBlank { "right" }
                                            "right" -> "left"
                                            else -> "none"
                                        }
                                        onUpdateOneHanded(next)
                                    },
                                    onSettingsClick = {
                                        onOpenSettings()
                                    },
                                    onLanguageCycle = {
                                        playFeedback()
                                        onLanguageCycle()
                                    },
                                    onLanguageLongPress = {
                                        playFeedback()
                                        showLanguageSlider = true
                                    },
                                    tikTokState = tikTokState,
                                    onTikTokDownloadClicked = {
                                        playFeedback()
                                        tikTokManager.onDownloadClicked()
                                    },
                                    onTikTokFormatChosen = { isAudio ->
                                        playFeedback()
                                        tikTokManager.onFormatChosen(isAudio)
                                    },
                                    onTikTokQualityChosen = { quality ->
                                        playFeedback()
                                        tikTokManager.onQualityChosen(quality)
                                    },
                                    onTikTokCancel = {
                                        playFeedback()
                                        tikTokManager.cancelDownload()
                                    },
                                    onTikTokDismiss = {
                                        playFeedback()
                                        tikTokManager.dismiss()
                                    },
                                    onTikTokRetry = {
                                        playFeedback()
                                        tikTokManager.retry()
                                    },
                                    onTikTokOpenFile = { filePath, isAudio ->
                                        playFeedback()
                                        tikTokManager.openFile(filePath, isAudio)
                                    },
                                    onTikTokShareFile = { filePath, isAudio ->
                                        playFeedback()
                                        tikTokManager.shareFile(filePath, isAudio)
                                    },
                                    onTikTokPlayInKeyboard = { filePath, isAudio, title ->
                                        playFeedback()
                                        activeMediaPlayerPath = filePath
                                        activeMediaPlayerIsAudio = isAudio
                                        activeMediaPlayerTitle = title
                                    },
                                    onTikTokToolbarClick = {
                                        playFeedback()
                                        val latest = clipboardItems.firstOrNull()?.text
                                        if (latest != null && tikTokManager.onClipboardUpdated(latest)) {
                                            // detected
                                        } else {
                                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                            val clip = cm?.primaryClip?.getItemAt(0)?.text?.toString()
                                            tikTokManager.onClipboardUpdated(clip)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Keyboard body
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (activeMediaPlayerPath != null) {
                            InKeyboardMediaPlayer(
                                filePath = activeMediaPlayerPath!!,
                                isAudio = activeMediaPlayerIsAudio,
                                title = activeMediaPlayerTitle,
                                palette = palette,
                                onClosePlayer = { activeMediaPlayerPath = null },
                                onOpenExternal = { path, isAudio ->
                                    playFeedback()
                                    tikTokManager.openFile(path, isAudio)
                                },
                                onShare = { path, isAudio ->
                                    playFeedback()
                                    tikTokManager.shareFile(path, isAudio)
                                }
                            )
                        } else {
                            when (currentMode) {
                            KeyboardMode.EMOJI -> {
                                EmojiKeyboardLayout(
                                    palette = palette,
                                    onEmojiSelected = { emoji ->
                                        playFeedback()
                                        onCharTyped(emoji)
                                    },
                                    onBackspace = {
                                        playFeedback()
                                        onDelete()
                                    },
                                    onCloseEmoji = {
                                        playFeedback()
                                        onModeSwitch(
                                            when (settings.currentLanguage) {
                                                "bangla" -> KeyboardMode.BANGLA
                                                "avro" -> KeyboardMode.AVRO
                                                else -> KeyboardMode.ENGLISH
                                            }
                                        )
                                    },
                                    onSwitchToStickers = {
                                        playFeedback()
                                        onModeSwitch(KeyboardMode.STICKERS)
                                    }
                                )
                            }

                            KeyboardMode.STICKERS -> {
                                StickerKeyboardLayout(
                                    palette = palette,
                                    onStickerSelected = { sticker ->
                                        playFeedback()
                                        onStickerSelected(sticker)
                                    },
                                    onSwitchToEmoji = {
                                        playFeedback()
                                        onModeSwitch(KeyboardMode.EMOJI)
                                    },
                                    onBackspace = {
                                        playFeedback()
                                        onDelete()
                                    },
                                    onCloseStickers = {
                                        playFeedback()
                                        onModeSwitch(
                                            when (settings.currentLanguage) {
                                                "bangla" -> KeyboardMode.BANGLA
                                                "avro" -> KeyboardMode.AVRO
                                                else -> KeyboardMode.ENGLISH
                                            }
                                        )
                                    }
                                )
                            }

                            KeyboardMode.CLIPBOARD -> {
                                ClipboardKeyboardLayout(
                                    items = clipboardItems,
                                    palette = palette,
                                    onPasteItem = { text, returnToKeyboard ->
                                        playFeedback()
                                        onPasteClipboard(text, returnToKeyboard)
                                    },
                                    onTogglePin = { item ->
                                        playFeedback()
                                        onTogglePinClipboard(item)
                                    },
                                    onDeleteItem = { id ->
                                        playFeedback()
                                        onDeleteClipboard(id)
                                    },
                                    onClearAll = {
                                        playFeedback()
                                        onClearClipboard()
                                    },
                                    onSyncClipboard = {
                                        playFeedback()
                                        onSyncClipboard()
                                    },
                                    onCloseClipboard = {
                                        playFeedback()
                                        onModeSwitch(
                                            when (settings.currentLanguage) {
                                                "bangla" -> KeyboardMode.BANGLA
                                                "avro" -> KeyboardMode.AVRO
                                                else -> KeyboardMode.ENGLISH
                                            }
                                        )
                                    },
                                    onTriggerDownload = { url ->
                                        playFeedback()
                                        tikTokManager.onClipboardUpdated(url)
                                        tikTokManager.onDownloadClicked()
                                    },
                                    onPlayInKeyboard = { filePath, isAudio, title ->
                                        playFeedback()
                                        activeMediaPlayerPath = filePath
                                        activeMediaPlayerIsAudio = isAudio
                                        activeMediaPlayerTitle = title
                                    }
                                )
                            }

                            KeyboardMode.VAULT -> {
                                CredentialVaultKeyboardLayout(
                                    credentials = savedCredentials,
                                    palette = palette,
                                    onAutofillText = { text ->
                                        playFeedback()
                                        onPasteClipboard(text, true)
                                    },
                                    onSaveNewCredential = { service, user, pass ->
                                        playFeedback()
                                        onSaveCredential(service, user, pass)
                                    },
                                    onDeleteCredential = { id ->
                                        playFeedback()
                                        onDeleteCredential(id)
                                    },
                                    onTogglePin = { cred ->
                                        playFeedback()
                                        onTogglePinCredential(cred)
                                    },
                                    onCloseVault = {
                                        playFeedback()
                                        onModeSwitch(
                                            when (settings.currentLanguage) {
                                                "bangla" -> KeyboardMode.BANGLA
                                                "avro" -> KeyboardMode.AVRO
                                                else -> KeyboardMode.ENGLISH
                                            }
                                        )
                                    }
                                )
                            }

                            KeyboardMode.TEXT_EDIT -> {
                                TextEditingKeyboardLayout(
                                    keyHeight = keyHeight,
                                    palette = palette,
                                    onMoveUp = { isSelecting ->
                                        playFeedback()
                                        onMoveCursorUp(isSelecting)
                                    },
                                    onMoveDown = { isSelecting ->
                                        playFeedback()
                                        onMoveCursorDown(isSelecting)
                                    },
                                    onMoveLeft = { isSelecting ->
                                        playFeedback()
                                        onMoveCursorLeft(isSelecting)
                                    },
                                    onMoveRight = { isSelecting ->
                                        playFeedback()
                                        onMoveCursorRight(isSelecting)
                                    },
                                    onMoveToStart = { isSelecting ->
                                        playFeedback()
                                        onMoveToStartOfLine(isSelecting)
                                    },
                                    onMoveToEnd = { isSelecting ->
                                        playFeedback()
                                        onMoveToEndOfLine(isSelecting)
                                    },
                                    onSelectAll = {
                                        playFeedback()
                                        onSelectAll()
                                    },
                                    onCopy = {
                                        playFeedback()
                                        onCopyText()
                                    },
                                    onPaste = {
                                        playFeedback()
                                        onPasteText()
                                    },
                                    onDelete = {
                                        playFeedback()
                                        onDelete()
                                    },
                                    onClose = {
                                        playFeedback()
                                        onModeSwitch(
                                            when (settings.currentLanguage) {
                                                "bangla" -> KeyboardMode.BANGLA
                                                "avro" -> KeyboardMode.AVRO
                                                else -> KeyboardMode.ENGLISH
                                            }
                                        )
                                    }
                                )
                            }

                            KeyboardMode.NUMBER_PAD -> {
                                NumberPadKeyboardLayout(
                                    keyHeight = keyHeight,
                                    palette = palette,
                                    enterLabel = enterLabel,
                                    onCharTyped = { char ->
                                        playFeedback()
                                        onCharTyped(char)
                                    },
                                    onDelete = {
                                        playFeedback()
                                        onDelete()
                                    },
                                    onSpace = {
                                        playFeedback()
                                        onSpace()
                                    },
                                    onEnter = {
                                        playFeedback()
                                        onEnter()
                                    },
                                    onClose = {
                                        playFeedback()
                                        onModeSwitch(
                                            when (settings.currentLanguage) {
                                                "bangla" -> KeyboardMode.BANGLA
                                                "avro" -> KeyboardMode.AVRO
                                                else -> KeyboardMode.ENGLISH
                                            }
                                        )
                                    }
                                )
                            }

                            KeyboardMode.VOICE -> {
                                LaunchedEffect(Unit) {
                                    startVoiceListening()
                                    onModeSwitch(
                                        when (settings.currentLanguage.lowercase()) {
                                            "bangla" -> KeyboardMode.BANGLA
                                            "avro" -> KeyboardMode.AVRO
                                            else -> KeyboardMode.ENGLISH
                                        }
                                    )
                                }
                            }

                            KeyboardMode.NUMBERS -> {
                                NumberAndSymbolLayout(
                                    isSymbolPage = false,
                                    keyHeight = keyHeight,
                                    palette = palette,
                                    enterLabel = enterLabel,
                                    showEmojiKey = settings.showEmojiKey,
                                    showLanguageKey = settings.showLanguageKey,
                                    showKeySubLabels = settings.showKeySubLabels,
                                    popupMode = if (settings.keyPreviewEnabled) settings.keyPopupMode else "disabled",
                                    onCharTyped = { char ->
                                        playFeedback()
                                        onCharTyped(char)
                                    },
                                    onDelete = {
                                        playFeedback()
                                        onDelete()
                                    },
                                    onSpace = {
                                        playFeedback()
                                        onSpace()
                                    },
                                    onSpaceDrag = onSpaceDrag,
                                    onEnter = {
                                        playFeedback()
                                        onEnter()
                                    },
                                    onSwitchMode = { mode ->
                                        playFeedback()
                                        onModeSwitch(mode)
                                    },
                                    onTogglePage = {
                                        playFeedback()
                                        onModeSwitch(KeyboardMode.SYMBOLS)
                                    },
                                    onLanguageCycle = {
                                        playFeedback()
                                        onLanguageCycle()
                                    },
                                    onLongPressLanguage = {
                                        playFeedback()
                                        showLanguageSlider = true
                                    }
                                )
                            }

                            KeyboardMode.SYMBOLS -> {
                                NumberAndSymbolLayout(
                                    isSymbolPage = true,
                                    keyHeight = keyHeight,
                                    palette = palette,
                                    enterLabel = enterLabel,
                                    showEmojiKey = settings.showEmojiKey,
                                    showLanguageKey = settings.showLanguageKey,
                                    showKeySubLabels = settings.showKeySubLabels,
                                    popupMode = if (settings.keyPreviewEnabled) settings.keyPopupMode else "disabled",
                                    onCharTyped = { char ->
                                        playFeedback()
                                        onCharTyped(char)
                                    },
                                    onDelete = {
                                        playFeedback()
                                        onDelete()
                                    },
                                    onSpace = {
                                        playFeedback()
                                        onSpace()
                                    },
                                    onSpaceDrag = onSpaceDrag,
                                    onEnter = {
                                        playFeedback()
                                        onEnter()
                                    },
                                    onSwitchMode = { mode ->
                                        playFeedback()
                                        onModeSwitch(mode)
                                    },
                                    onTogglePage = {
                                        playFeedback()
                                        onModeSwitch(KeyboardMode.NUMBERS)
                                    },
                                    onLanguageCycle = {
                                        playFeedback()
                                        onLanguageCycle()
                                    },
                                    onLongPressLanguage = {
                                        playFeedback()
                                        showLanguageSlider = true
                                    }
                                )
                            }

                            KeyboardMode.BANGLA -> {
                                if (palette.themeId == "reference_minimal" && settings.uiMode == "original") {
                                    ReferenceOriginalKeyboardLayout(
                                        isAvro = false,
                                        isBangla = true,
                                        shiftState = shiftState,
                                        keyHeight = keyHeight,
                                        palette = palette,
                                        enterLabel = enterLabel,
                                        currentLanguage = settings.currentLanguage,
                                        popupMode = if (settings.keyPreviewEnabled) settings.keyPopupMode else "disabled",
                                        onHoldProgressUpdate = { progress ->
                                            holdProgress = progress
                                        },
                                        onFiveSecondHoldComplete = {
                                            playFeedback()
                                            holdProgress = 0f
                                            val nextMode = if (settings.oneHandedMode == "none") {
                                                settings.preferredHand.ifBlank { "right" }
                                            } else {
                                                "none"
                                            }
                                            onUpdateOneHanded(nextMode)
                                        },
                                        onHoldCancelled = {
                                            holdProgress = 0f
                                        },
                                        onCharTyped = { char ->
                                            playFeedback()
                                            onCharTyped(char)
                                        },
                                        onDelete = {
                                            playFeedback()
                                            onDelete()
                                        },
                                        onSpace = {
                                            playFeedback()
                                            onSpace()
                                        },
                                        onSpaceDrag = onSpaceDrag,
                                        onEnter = {
                                            playFeedback()
                                            onEnter()
                                        },
                                        onShift = {
                                            playFeedback()
                                            onShiftToggle()
                                        },
                                        onSwitchMode = { mode ->
                                            playFeedback()
                                            onModeSwitch(mode)
                                        },
                                        onLanguageCycle = {
                                            playFeedback()
                                            onLanguageCycle()
                                        },
                                        onLongPressLanguage = {
                                            playFeedback()
                                            showLanguageSlider = true
                                        }
                                    )
                                } else {
                                    BanglaKeyLayout(
                                        isShift = shiftState.isUppercase,
                                        keyHeight = keyHeight,
                                        palette = palette,
                                        enterLabel = enterLabel,
                                        showEmojiKey = settings.showEmojiKey,
                                        showLanguageKey = settings.showLanguageKey,
                                        showKeySubLabels = settings.showKeySubLabels,
                                        popupMode = if (settings.keyPreviewEnabled) settings.keyPopupMode else "disabled",
                                        onHoldProgressUpdate = { progress ->
                                            holdProgress = progress
                                        },
                                        onFiveSecondHoldComplete = {
                                            playFeedback()
                                            holdProgress = 0f
                                            val nextMode = if (settings.oneHandedMode == "none") {
                                                settings.preferredHand.ifBlank { "right" }
                                            } else {
                                                "none"
                                            }
                                            onUpdateOneHanded(nextMode)
                                        },
                                        onHoldCancelled = {
                                            holdProgress = 0f
                                        },
                                        onCharTyped = { char ->
                                            playFeedback()
                                            onCharTyped(char)
                                        },
                                        onDelete = {
                                            playFeedback()
                                            onDelete()
                                        },
                                        onSpace = {
                                            playFeedback()
                                            onSpace()
                                        },
                                        onSpaceDrag = onSpaceDrag,
                                        onEnter = {
                                            playFeedback()
                                            onEnter()
                                        },
                                        onShift = {
                                            playFeedback()
                                            onShiftToggle()
                                        },
                                        onSwitchMode = { mode ->
                                            playFeedback()
                                            onModeSwitch(mode)
                                        },
                                        onLanguageCycle = {
                                            playFeedback()
                                            onLanguageCycle()
                                        },
                                        onLongPressLanguage = {
                                            playFeedback()
                                            showLanguageSlider = true
                                        }
                                    )
                                }
                            }

                            KeyboardMode.ENGLISH, KeyboardMode.AVRO -> {
                                val isAvro = currentMode == KeyboardMode.AVRO
                                if (palette.themeId == "reference_minimal" && settings.uiMode == "original") {
                                    ReferenceOriginalKeyboardLayout(
                                        isAvro = isAvro,
                                        isBangla = false,
                                        shiftState = shiftState,
                                        keyHeight = keyHeight,
                                        palette = palette,
                                        enterLabel = enterLabel,
                                        currentLanguage = settings.currentLanguage,
                                        popupMode = if (settings.keyPreviewEnabled) settings.keyPopupMode else "disabled",
                                        onHoldProgressUpdate = { progress ->
                                            holdProgress = progress
                                        },
                                        onFiveSecondHoldComplete = {
                                            playFeedback()
                                            holdProgress = 0f
                                            val nextMode = if (settings.oneHandedMode == "none") {
                                                settings.preferredHand.ifBlank { "right" }
                                            } else {
                                                "none"
                                            }
                                            onUpdateOneHanded(nextMode)
                                        },
                                        onHoldCancelled = {
                                            holdProgress = 0f
                                        },
                                        onCharTyped = { char ->
                                            playFeedback()
                                            val isLetter = char.length == 1 && char[0].isLetter()
                                            val finalChar = if (isLetter) {
                                                if (shiftState.isUppercase) char.uppercase() else char.lowercase()
                                            } else {
                                                char
                                            }
                                            onCharTyped(finalChar)
                                        },
                                        onDelete = {
                                            playFeedback()
                                            onDelete()
                                        },
                                        onSpace = {
                                            playFeedback()
                                            onSpace()
                                        },
                                        onSpaceDrag = onSpaceDrag,
                                        onEnter = {
                                            playFeedback()
                                            onEnter()
                                        },
                                        onShift = {
                                            playFeedback()
                                            onShiftToggle()
                                        },
                                        onSwitchMode = { mode ->
                                            playFeedback()
                                            onModeSwitch(mode)
                                        },
                                        onLanguageCycle = {
                                            playFeedback()
                                            onLanguageCycle()
                                        },
                                        onLongPressLanguage = {
                                            playFeedback()
                                            showLanguageSlider = true
                                        }
                                    )
                                } else {
                                    QwertyKeyLayout(
                                        isAvro = isAvro,
                                        shiftState = shiftState,
                                        showNumberRow = settings.showNumberRow,
                                        keyHeight = keyHeight,
                                        palette = palette,
                                        enterLabel = enterLabel,
                                        showEmojiKey = settings.showEmojiKey,
                                        showLanguageKey = settings.showLanguageKey,
                                        showKeySubLabels = settings.showKeySubLabels,
                                        popupMode = if (settings.keyPreviewEnabled) settings.keyPopupMode else "disabled",
                                        onHoldProgressUpdate = { progress ->
                                            holdProgress = progress
                                        },
                                        onFiveSecondHoldComplete = {
                                            playFeedback()
                                            holdProgress = 0f
                                            val nextMode = if (settings.oneHandedMode == "none") {
                                                settings.preferredHand.ifBlank { "right" }
                                            } else {
                                                "none"
                                            }
                                            onUpdateOneHanded(nextMode)
                                        },
                                        onHoldCancelled = {
                                            holdProgress = 0f
                                        },
                                        onCharTyped = { char ->
                                            playFeedback()
                                            val isLetter = char.length == 1 && char[0].isLetter()
                                            val finalChar = if (isLetter) {
                                                if (shiftState.isUppercase) char.uppercase() else char.lowercase()
                                            } else {
                                                char
                                            }
                                            onCharTyped(finalChar)
                                        },
                                        onDelete = {
                                            playFeedback()
                                            onDelete()
                                        },
                                        onSpace = {
                                            playFeedback()
                                            onSpace()
                                        },
                                        onSpaceDrag = onSpaceDrag,
                                        onEnter = {
                                            playFeedback()
                                            onEnter()
                                        },
                                        onShift = {
                                            playFeedback()
                                            onShiftToggle()
                                        },
                                        onSwitchMode = { mode ->
                                            playFeedback()
                                            onModeSwitch(mode)
                                        },
                                        onLanguageCycle = {
                                            playFeedback()
                                            onLanguageCycle()
                                        },
                                        onLongPressLanguage = {
                                            playFeedback()
                                            showLanguageSlider = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                }

                // Right dock control if in LEFT-handed mode
                if (oneHandedMode == OneHandedMode.LEFT) {
                    OneHandedSideDock(
                        mode = oneHandedMode,
                        palette = palette,
                        onSwitchSide = {
                            playFeedback()
                            onUpdateOneHanded("right")
                        },
                        onExpand = {
                            playFeedback()
                            onUpdateOneHanded("none")
                        }
                    )
                }
            }
        }

            // Android gesture pill bar matching Geometric Balance layout
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 6.dp)
                    .width(80.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(palette.textColor.copy(alpha = 0.2f))
            )
        }

        // Floating Language Side Slider overlay
        if (showLanguageSlider) {
            LanguageSideSlider(
                currentLanguage = settings.currentLanguage,
                palette = palette,
                onSelectLanguage = { lang ->
                    onLanguageSelected(lang)
                },
                onDismiss = {
                    showLanguageSlider = false
                },
                onFeedback = {
                    playFeedback()
                }
            )
        }

        // Language Selector Popup Dialog
        if (showLanguageDialog) {
            LanguageSelectorDialog(
                activeLanguages = settings.activeLanguages,
                currentLanguage = settings.currentLanguage,
                palette = palette,
                onSelectLanguage = { lang ->
                    onLanguageSelected(lang)
                },
                onDismiss = {
                    showLanguageDialog = false
                }
            )
        }

        // Live Theme Quick Picker (2-Column Grid Modal)
        if (showThemeGridPicker) {
            ThemeQuickPickerGrid(
                currentThemeId = settings.theme,
                onSelectTheme = { themeId ->
                    playFeedback()
                    onUpdateTheme?.invoke(themeId)
                },
                onDismiss = {
                    showThemeGridPicker = false
                }
            )
        }
    }
}

@Composable
private fun ThemeQuickPickerGrid(
    currentThemeId: String,
    onSelectTheme: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val allThemes = KeyboardThemes.ALL_THEMES

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.65f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(enabled = false) {}
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎨 Pick Keyboard Theme",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap any theme to switch live on keyboard",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 2-Column Grid of Available Themes
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 290.dp)
                ) {
                    items(allThemes, key = { it.themeId }) { palette ->
                        val isSelected = palette.themeId.equals(currentThemeId, ignoreCase = true)
                        Card(
                            onClick = {
                                onSelectTheme(palette.themeId)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 4 Color Swatch Chips Preview
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(palette.keyboardBackground),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(palette.keyBackground)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(palette.accentColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(palette.textColor)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = palette.themeName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "✓",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Text(
                                    text = palette.category,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
