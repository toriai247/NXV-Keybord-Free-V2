package com.example.ime

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.NXVApplication
import com.example.data.entity.ClipboardItem
import com.example.data.entity.SavedCredential
import com.example.data.preferences.KeyboardSettings
import com.example.keyboard.KeyboardMode
import com.example.keyboard.ShiftState
import com.example.language.avro.AvroPhoneticEngine
import com.example.suggestion.SuggestionEngine
import com.example.suggestion.SuggestionItem
import com.example.downloader.tiktok.TikTokDownloadManager
import com.example.ui.keyboard.AutoSavePromptData
import com.example.ui.keyboard.KeyboardRootView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NXVInputMethodService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val inputConnectionManager = InputConnectionManager()
    private val avroEngine = AvroPhoneticEngine()
    private lateinit var suggestionEngine: SuggestionEngine
    private lateinit var feedbackManager: FeedbackManager

    // Observable states
    private var currentMode by mutableStateOf(KeyboardMode.ENGLISH)
    private var shiftState by mutableStateOf(ShiftState.LOWERCASE)
    private var enterActionLabel by mutableStateOf("↵")
    private val isKeyboardVisibleState = MutableStateFlow(true)

    // Shift double-tap detection
    private var lastShiftTapTime = 0L
    private val DOUBLE_TAP_TIMEOUT_MS = 380L

    private val suggestionsState = MutableStateFlow<List<SuggestionItem>>(emptyList())
    private val clipboardItemsState = MutableStateFlow<List<ClipboardItem>>(emptyList())
    private val savedCredentialsState = MutableStateFlow<List<SavedCredential>>(emptyList())
    private val currentSettingsState = MutableStateFlow(KeyboardSettings())

    private var autoSavePromptState by mutableStateOf<AutoSavePromptData?>(null)

    // Password & account detection tracking
    private var currentPackageName: String = ""
    private var currentAppName: String = ""
    private var lastObservedUsername: String = ""
    private var currentPasswordBuffer = StringBuilder()
    private var isCurrentFieldPassword: Boolean = false

    // Avro composing buffer
    private val avroRawBuffer = StringBuilder()

    // Spacebar cursor navigation state
    private var accumulatedCursorDrag = 0f
    private val cursorStepPx = 28f

    // Context tracking for bigram predictions
    private var lastCommittedWord: String? = null

    private var clipboardManager: ClipboardManager? = null
    private val clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        syncSystemClipboard()
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        feedbackManager = FeedbackManager(this)
        val app = application as? NXVApplication ?: NXVApplication.instance
        suggestionEngine = SuggestionEngine(app.repository, this)

        // Initialize ClipboardManager listener to capture copied text fragments
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        try {
            clipboardManager?.addPrimaryClipChangedListener(clipChangedListener)
        } catch (_: Exception) {}
        syncSystemClipboard()

        // Observe Settings
        serviceScope.launch {
            app.preferences.settingsFlow.collectLatest { settings ->
                currentSettingsState.value = settings
                updateModeForLanguage(settings.currentLanguage)
            }
        }

        // Observe Clipboard
        serviceScope.launch {
            app.repository.clipboardItems.collectLatest { items ->
                clipboardItemsState.value = items
            }
        }

        // Observe Credentials
        serviceScope.launch {
            app.repository.savedCredentials.collectLatest { creds ->
                savedCredentialsState.value = creds
            }
        }
    }

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        window?.window?.decorView?.let { decor ->
            decor.setViewTreeLifecycleOwner(this)
            decor.setViewTreeViewModelStoreOwner(this)
            decor.setViewTreeSavedStateRegistryOwner(this)
        }
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean {
        return true
    }

    override fun onWindowShown() {
        super.onWindowShown()
        isKeyboardVisibleState.value = true
        syncSystemClipboard()
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
    }

    private var isHiding = false

    override fun hideWindow() {
        if (isHiding) {
            super.hideWindow()
            return
        }
        isHiding = true
        serviceScope.launch {
            isKeyboardVisibleState.value = false
            delay(300L) // Let slide-down exit animation complete
            super.hideWindow()
            isHiding = false
        }
    }

    private var inputComposeView: ComposeView? = null

    override fun onCreateInputView(): View {
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        window?.window?.decorView?.let { decor ->
            decor.setViewTreeLifecycleOwner(this)
            decor.setViewTreeViewModelStoreOwner(this)
            decor.setViewTreeSavedStateRegistryOwner(this)
        }

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@NXVInputMethodService))
            setViewTreeLifecycleOwner(this@NXVInputMethodService)
            setViewTreeViewModelStoreOwner(this@NXVInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@NXVInputMethodService)
            setContent {
                val settings by currentSettingsState.collectAsState()
                val suggestions by suggestionsState.collectAsState()
                val clipboardItems by clipboardItemsState.collectAsState()
                val savedCredentials by savedCredentialsState.collectAsState()
                val isKeyboardVisible by isKeyboardVisibleState.collectAsState()

                AnimatedVisibility(
                    visible = isKeyboardVisible,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    KeyboardRootView(
                    settings = settings,
                    currentMode = currentMode,
                    shiftState = shiftState,
                    suggestions = suggestions,
                    clipboardItems = clipboardItems,
                    savedCredentials = savedCredentials,
                    autoSavePrompt = autoSavePromptState,
                    enterLabel = enterActionLabel,
                    feedbackManager = feedbackManager,
                    onCharTyped = { char -> handleCharTyped(char) },
                    onDelete = { handleDelete() },
                    onSpace = { handleSpace() },
                    onSpaceDrag = { delta -> handleSpaceDrag(delta) },
                    onEnter = { handleEnter() },
                    onShiftToggle = { handleShiftToggle() },
                    onModeSwitch = { mode ->
                        if (mode == KeyboardMode.CLIPBOARD) {
                            syncSystemClipboard()
                        }
                        currentMode = mode
                    },
                    onLanguageCycle = { cycleLanguage() },
                    onLanguageSelected = { lang -> selectLanguage(lang) },
                    onSuggestionClicked = { item -> handleSuggestionClicked(item) },
                    onPasteClipboard = { text, returnToKeyboard -> handlePaste(text, returnToKeyboard) },
                    onTogglePinClipboard = { item ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.repository.togglePinClipboardItem(item)
                        }
                    },
                    onDeleteClipboard = { id ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.repository.deleteClipboardItem(id)
                        }
                    },
                    onClearClipboard = {
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.repository.clearClipboard()
                        }
                    },
                    onSyncClipboard = { syncSystemClipboard() },
                    onSaveCredential = { service, user, pass ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.repository.saveCredential(
                                serviceName = service,
                                username = user,
                                password = pass,
                                packageName = currentPackageName
                            )
                        }
                    },
                    onDeleteCredential = { id ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.repository.deleteCredential(id)
                        }
                    },
                    onTogglePinCredential = { cred ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.repository.togglePinCredential(cred)
                        }
                    },
                    onMoveCursorUp = { isSelecting -> inputConnectionManager.moveCursorUp(isSelecting) },
                    onMoveCursorDown = { isSelecting -> inputConnectionManager.moveCursorDown(isSelecting) },
                    onMoveCursorLeft = { isSelecting -> inputConnectionManager.moveCursorLeft(isSelecting) },
                    onMoveCursorRight = { isSelecting -> inputConnectionManager.moveCursorRight(isSelecting) },
                    onMoveToStartOfLine = { isSelecting -> inputConnectionManager.moveToStartOfLine(isSelecting) },
                    onMoveToEndOfLine = { isSelecting -> inputConnectionManager.moveToEndOfLine(isSelecting) },
                    onSelectAll = { inputConnectionManager.selectAll() },
                    onCopyText = { inputConnectionManager.copyText() },
                    onPasteText = { inputConnectionManager.pasteText() },
                    onOpenSettings = {
                        val intent = Intent(this@NXVInputMethodService, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    },
                    onOpenThemes = {
                        val intent = Intent(this@NXVInputMethodService, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra("NAVIGATE_TO", "THEME_LIBRARY")
                        }
                        startActivity(intent)
                    },
                    onUpdateTheme = { theme ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.preferences.updateTheme(theme)
                        }
                    },
                    onToggleVibration = { enabled ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.preferences.updateVibrationEnabled(enabled)
                        }
                    },
                    onToggleSound = { enabled ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.preferences.updateSoundEnabled(enabled)
                        }
                    },
                    onUpdateOneHanded = { mode ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.preferences.updateOneHandedMode(mode)
                        }
                    },
                    onUpdateOneHandedHeightDp = { height ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.preferences.updateOneHandedHeightDp(height)
                        }
                    },
                    onUpdateOneHandedTheme = { theme ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.preferences.updateOneHandedTheme(theme)
                        }
                    },
                    onUpdateOneHandedRotateText = { rotate ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.preferences.updateOneHandedRotateText(rotate)
                        }
                    },
                    onUpdateOneHandedArcScale = { scale ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.preferences.updateOneHandedArcScale(scale)
                        }
                    },
                    onUpdateOneHandedShowSuggestions = { show ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.preferences.updateOneHandedShowSuggestions(show)
                        }
                    },
                    onUpdateOneHandedKeyStyle = { style ->
                        serviceScope.launch {
                            val app = application as? NXVApplication ?: NXVApplication.instance
                            app.preferences.updateOneHandedKeyStyle(style)
                        }
                    },
                    onVoiceLiveInput = { text, isFinal ->
                        if (text.isEmpty() && isFinal) {
                            inputConnectionManager.finishComposing()
                        } else if (isFinal) {
                            inputConnectionManager.finishComposing(text)
                            inputConnectionManager.commitText(" ")
                            refreshSuggestions()
                        } else {
                            inputConnectionManager.setComposing(text, text)
                        }
                    }
                )
                }
            }
        }

        inputComposeView = composeView
        attachLifecycleOwners(composeView)
        return composeView
    }

    private fun attachLifecycleOwners(view: View?) {
        window?.window?.decorView?.let { decor ->
            decor.setViewTreeLifecycleOwner(this)
            decor.setViewTreeViewModelStoreOwner(this)
            decor.setViewTreeSavedStateRegistryOwner(this)
        }
        view?.let { v ->
            v.setViewTreeLifecycleOwner(this)
            v.setViewTreeViewModelStoreOwner(this)
            v.setViewTreeSavedStateRegistryOwner(this)
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        attachLifecycleOwners(inputComposeView)
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
        inputConnectionManager.update(currentInputConnection, info)
        avroRawBuffer.clear()

        // Extract package & app name
        val pkg = info?.packageName ?: ""
        if (pkg.isNotBlank() && pkg != currentPackageName) {
            currentPackageName = pkg
            currentAppName = try {
                val pm = packageManager
                val appInfo = pm.getApplicationInfo(pkg, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                pkg.substringAfterLast('.').replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }

        isCurrentFieldPassword = inputConnectionManager.isPasswordField()
        if (isCurrentFieldPassword) {
            currentPasswordBuffer.clear()
            shiftState = ShiftState.LOWERCASE
        } else {
            val capsFlags = (info?.inputType ?: 0) and InputType.TYPE_MASK_FLAGS
            val isCapCharacters = (capsFlags and InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS) != 0
            val isCapSentences = (capsFlags and InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) != 0
            val isCapWords = (capsFlags and InputType.TYPE_TEXT_FLAG_CAP_WORDS) != 0
            val settings = currentSettingsState.value

            if (isCapCharacters) {
                shiftState = ShiftState.CAPS_LOCK
            } else if (settings.autoCapitalization && (isCapSentences || isCapWords)) {
                shiftState = ShiftState.SHIFT_ONCE
            } else {
                shiftState = ShiftState.LOWERCASE
            }
        }

        enterActionLabel = inputConnectionManager.getEnterActionLabel()

        // Adapt layout to input field
        if (inputConnectionManager.isNumberOnlyField()) {
            currentMode = KeyboardMode.NUMBERS
        } else {
            val settings = currentSettingsState.value
            updateModeForLanguage(settings.currentLanguage)
        }

        // Safe clipboard capture
        syncSystemClipboard()

        // Refresh suggestions
        refreshSuggestions()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        commitAvroBuffer()
        checkAndOfferPasswordSave()
        inputConnectionManager.clearComposing()
        avroRawBuffer.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            clipboardManager?.removePrimaryClipChangedListener(clipChangedListener)
        } catch (_: Exception) {}
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        serviceScope.cancel()
    }

    private fun updateModeForLanguage(lang: String) {
        currentMode = when (lang.lowercase()) {
            "bangla" -> {
                if (shiftState == ShiftState.SHIFT_ONCE) {
                    shiftState = ShiftState.LOWERCASE
                }
                KeyboardMode.BANGLA
            }
            "avro" -> KeyboardMode.AVRO
            else -> KeyboardMode.ENGLISH
        }
    }

    fun handleSpaceDrag(dragAmountX: Float) {
        commitAvroBuffer()
        accumulatedCursorDrag += dragAmountX
        val steps = (accumulatedCursorDrag / cursorStepPx).toInt()
        if (steps != 0) {
            inputConnectionManager.moveCursor(steps)
            accumulatedCursorDrag -= steps * cursorStepPx
        }
    }

    private fun handleCharTyped(char: String) {
        val isLetter = char.length == 1 && char[0].isLetter()
        val effectiveChar = if (isLetter) {
            if (shiftState.isUppercase) char.uppercase() else char.lowercase()
        } else {
            char
        }

        if (isCurrentFieldPassword) {
            currentPasswordBuffer.append(effectiveChar)
        } else {
            // Track potential email/username input
            val word = inputConnectionManager.getCurrentWordBeforeCursor() + effectiveChar
            if (word.contains("@") || word.length >= 3) {
                lastObservedUsername = word
            }
        }

        if (currentMode == KeyboardMode.AVRO) {
            avroRawBuffer.append(effectiveChar)
            val converted = avroEngine.convertWord(avroRawBuffer.toString())
            inputConnectionManager.setComposing(converted, avroRawBuffer.toString())
            refreshSuggestions()
        } else {
            inputConnectionManager.commitText(effectiveChar)
            refreshSuggestions()
        }

        // Single tap shift returns to LOWERCASE only after typing an alphabetic letter.
        // Numbers, symbols, punctuation and emoji do NOT change shift state.
        // CAPS_LOCK and MANUAL_UPPERCASE remain active until explicitly toggled off.
        if (isLetter && shiftState == ShiftState.SHIFT_ONCE) {
            shiftState = ShiftState.LOWERCASE
            lastShiftTapTime = 0L
        }
    }

    private fun handleDelete() {
        if (isCurrentFieldPassword && currentPasswordBuffer.isNotEmpty()) {
            currentPasswordBuffer.deleteCharAt(currentPasswordBuffer.length - 1)
        }

        if (currentMode == KeyboardMode.AVRO && avroRawBuffer.isNotEmpty()) {
            avroRawBuffer.deleteCharAt(avroRawBuffer.length - 1)
            if (avroRawBuffer.isEmpty()) {
                inputConnectionManager.clearComposing()
            } else {
                val converted = avroEngine.convertWord(avroRawBuffer.toString())
                inputConnectionManager.setComposing(converted, avroRawBuffer.toString())
            }
            refreshSuggestions()
        } else {
            inputConnectionManager.handleDelete()
            refreshSuggestions()
        }
    }

    private fun handleSpace() {
        if (currentMode == KeyboardMode.AVRO && avroRawBuffer.isNotEmpty()) {
            val raw = avroRawBuffer.toString()
            val converted = avroEngine.convertWord(raw)
            inputConnectionManager.finishComposing(converted)
            learnWord(converted, "bn")
            suggestionEngine.nextWordPredictor.recordBigram(lastCommittedWord, converted)
            lastCommittedWord = converted
            avroRawBuffer.clear()
        } else {
            val currentWord = inputConnectionManager.getCurrentWordBeforeCursor()
            if (currentWord.isNotEmpty()) {
                val locale = if (currentMode == KeyboardMode.ENGLISH) "en" else "bn"
                learnWord(currentWord, locale)
                suggestionEngine.nextWordPredictor.recordBigram(lastCommittedWord, currentWord)
                lastCommittedWord = currentWord
            }
        }

        inputConnectionManager.commitText(" ")
        refreshSuggestions()
    }

    private fun handleEnter() {
        if (currentMode == KeyboardMode.AVRO && avroRawBuffer.isNotEmpty()) {
            val raw = avroRawBuffer.toString()
            val converted = avroEngine.convertWord(raw)
            inputConnectionManager.finishComposing(converted)
            learnWord(converted, "bn")
            suggestionEngine.nextWordPredictor.recordBigram(lastCommittedWord, converted)
            lastCommittedWord = converted
            avroRawBuffer.clear()
        }
        checkAndOfferPasswordSave()
        inputConnectionManager.handleEnter()
        refreshSuggestions()
    }

    private fun checkAndOfferPasswordSave() {
        if (isCurrentFieldPassword && currentPasswordBuffer.length >= 3) {
            val passToSave = currentPasswordBuffer.toString()
            val serviceName = currentAppName.ifBlank { "Account" }
            val username = lastObservedUsername
            val pkg = currentPackageName

            autoSavePromptState = AutoSavePromptData(
                serviceName = serviceName,
                username = username,
                password = passToSave,
                onConfirmSave = {
                    serviceScope.launch {
                        val app = application as? NXVApplication ?: NXVApplication.instance
                        app.repository.saveCredential(
                            serviceName = serviceName,
                            username = username,
                            password = passToSave,
                            packageName = pkg
                        )
                    }
                    autoSavePromptState = null
                },
                onDismiss = {
                    autoSavePromptState = null
                }
            )
            currentPasswordBuffer.clear()
        }
    }

    fun handleShiftToggle() {
        val now = System.currentTimeMillis()
        shiftState = when (shiftState) {
            ShiftState.LOWERCASE -> {
                lastShiftTapTime = now
                ShiftState.SHIFT_ONCE
            }
            ShiftState.SHIFT_ONCE -> {
                if (now - lastShiftTapTime <= DOUBLE_TAP_TIMEOUT_MS) {
                    // Double tap within timeout enables CAPS LOCK
                    lastShiftTapTime = 0L
                    ShiftState.CAPS_LOCK
                } else {
                    // Tapped after delay cancels Shift, returns to LOWERCASE
                    lastShiftTapTime = 0L
                    ShiftState.LOWERCASE
                }
            }
            ShiftState.CAPS_LOCK, ShiftState.MANUAL_UPPERCASE -> {
                // Tapping Shift again disables Caps Lock / Manual Uppercase
                lastShiftTapTime = 0L
                ShiftState.LOWERCASE
            }
        }
    }

    fun setManualUppercase(enable: Boolean) {
        shiftState = if (enable) ShiftState.MANUAL_UPPERCASE else ShiftState.LOWERCASE
    }

    // Exposed helpers for testing
    internal fun getCurrentShiftState(): ShiftState = shiftState
    internal fun setCurrentShiftState(state: ShiftState) { shiftState = state }
    internal fun setLastShiftTapTimeForTest(time: Long) { lastShiftTapTime = time }

    private fun handleSuggestionClicked(item: SuggestionItem) {
        val replacement = item.replacementText
        inputConnectionManager.replaceCurrentWord(replacement)
        if (currentSettingsState.value.autoSpacing) {
            inputConnectionManager.commitText(" ")
        }
        avroRawBuffer.clear()

        val locale = if (currentMode == KeyboardMode.ENGLISH) "en" else "bn"
        learnWord(replacement, locale)
        suggestionEngine.nextWordPredictor.recordBigram(lastCommittedWord, replacement)
        lastCommittedWord = replacement
        refreshSuggestions()
    }

    private fun handlePaste(text: String, returnToKeyboard: Boolean = true) {
        TikTokDownloadManager.getInstance(this).onClipboardUpdated(text)
        inputConnectionManager.commitText(text)
        if (returnToKeyboard) {
            currentMode = when (currentSettingsState.value.currentLanguage) {
                "bangla" -> KeyboardMode.BANGLA
                "avro" -> KeyboardMode.AVRO
                else -> KeyboardMode.ENGLISH
            }
        }
    }

    private fun commitAvroBuffer() {
        if (avroRawBuffer.isNotEmpty()) {
            val converted = avroEngine.convertWord(avroRawBuffer.toString())
            inputConnectionManager.finishComposing(converted)
            avroRawBuffer.clear()
        }
    }

    private fun cycleLanguage() {
        commitAvroBuffer()
        val settings = currentSettingsState.value
        // Maintain consistent natural toggle order: English -> Bangla -> Avro -> English
        val orderedAll = listOf("english", "bangla", "avro")
        val active = orderedAll.filter { settings.activeLanguages.contains(it) }.ifEmpty { orderedAll }

        val currentIndex = active.indexOf(settings.currentLanguage.lowercase())
        val nextIndex = if (currentIndex == -1 || currentIndex >= active.size - 1) 0 else currentIndex + 1
        val nextLang = active[nextIndex]
        selectLanguage(nextLang)
    }

    private fun selectLanguage(lang: String) {
        commitAvroBuffer()
        serviceScope.launch {
            val app = application as? NXVApplication ?: NXVApplication.instance
            app.preferences.updateCurrentLanguage(lang)
            updateModeForLanguage(lang)
            refreshSuggestions()
        }
    }

    private fun refreshSuggestions() {
        serviceScope.launch {
            val settings = currentSettingsState.value
            val isPass = inputConnectionManager.isPasswordField()

            if (isPass) {
                // In password fields, standard text prediction is disabled for security,
                // but we check Encrypted Storage for matching saved credentials to offer 1-tap autofill!
                val app = application as? NXVApplication ?: NXVApplication.instance
                val matchingCreds = app.repository.getCredentialsForPackageOrQuery(
                    packageName = currentPackageName,
                    query = currentAppName
                )

                if (matchingCreds.isNotEmpty()) {
                    val autofillChips = matchingCreds.take(2).map { cred ->
                        val label = if (cred.username.isNotBlank()) {
                            "🔑 Fill: ${cred.username}"
                        } else {
                            "🔑 Fill ${cred.serviceName}"
                        }
                        SuggestionItem(
                            displayText = label,
                            replacementText = cred.password,
                            isPrimary = true
                        )
                    }
                    suggestionsState.value = autofillChips
                } else {
                    suggestionsState.value = emptyList()
                }
                return@launch
            }

            if (!settings.suggestionsEnabled) {
                suggestionsState.value = emptyList()
                return@launch
            }

            val query = if (currentMode == KeyboardMode.AVRO && avroRawBuffer.isNotEmpty()) {
                avroRawBuffer.toString()
            } else {
                inputConnectionManager.getCurrentWordBeforeCursor()
            }

            val lang = when (currentMode) {
                KeyboardMode.AVRO -> "avro"
                KeyboardMode.BANGLA -> "bangla"
                else -> "english"
            }

            val contextText = inputConnectionManager.getTextBeforeCursor(120)
            val list = suggestionEngine.getSuggestions(
                currentWord = query,
                mode = lang,
                contextTextBeforeCursor = contextText,
                isPasswordField = false,
                autoCorrectEnabled = settings.autoCorrection
            )
            suggestionsState.value = list
        }
    }

    private fun learnWord(word: String, locale: String) {
        serviceScope.launch {
            val app = application as? NXVApplication ?: NXVApplication.instance
            app.repository.learnWord(word, locale)
        }
    }

    internal fun syncSystemClipboard() {
        try {
            val cm = clipboardManager ?: getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
            if (!cm.hasPrimaryClip()) return
            val clipData = cm.primaryClip ?: return
            for (i in 0 until clipData.itemCount) {
                val item = clipData.getItemAt(i)
                val text = item?.text?.toString() ?: item?.coerceToText(this)?.toString()
                if (!text.isNullOrBlank()) {
                    TikTokDownloadManager.getInstance(this).onClipboardUpdated(text)
                    serviceScope.launch {
                        val app = application as? NXVApplication ?: NXVApplication.instance
                        app.repository.addClipboardItem(text)
                    }
                }
            }
        } catch (_: Exception) {}
    }
}
