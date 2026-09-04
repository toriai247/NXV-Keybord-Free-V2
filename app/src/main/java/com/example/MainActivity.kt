package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.entity.ClipboardItem
import com.example.data.entity.DictionaryWord
import com.example.data.entity.SavedCredential
import com.example.data.entity.TextShortcut
import com.example.data.preferences.KeyboardSettings
import com.example.ui.settings.AboutScreen
import com.example.ui.settings.ClipboardPrefsScreen
import com.example.ui.settings.DictionaryScreen
import com.example.ui.settings.KeyboardCustomizeScreen
import com.example.ui.settings.KeyboardPrefsScreen
import com.example.ui.settings.OnboardingView
import com.example.ui.settings.PrivacyScreen
import com.example.ui.settings.SettingsHomeScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.ShortcutsScreen
import com.example.ui.settings.SoundHapticScreen
import com.example.ui.settings.ThemeLibraryScreen
import com.example.ui.settings.TypingPrefsScreen
import com.example.ui.settings.VaultPrefsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.ImeStatusHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as? NXVApplication ?: NXVApplication.instance

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val lifecycleOwner = LocalLifecycleOwner.current

                    var isKeyboardEnabled by remember { mutableStateOf(ImeStatusHelper.isKeyboardEnabled(context)) }
                    var isKeyboardSelected by remember { mutableStateOf(ImeStatusHelper.isKeyboardSelected(context)) }

                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                isKeyboardEnabled = ImeStatusHelper.isKeyboardEnabled(context)
                                isKeyboardSelected = ImeStatusHelper.isKeyboardSelected(context)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    val scope = rememberCoroutineScope()
                    val settings by app.preferences.settingsFlow.collectAsState(initial = KeyboardSettings())
                    val clipboardItems by app.repository.clipboardItems.collectAsState(initial = emptyList())
                    val savedCredentials by app.repository.savedCredentials.collectAsState(initial = emptyList())
                    val dictionaryWords by app.repository.allDictionaryWords.collectAsState(initial = emptyList())
                    val shortcuts by app.repository.shortcuts.collectAsState(initial = emptyList())

                    val initialTarget = remember {
                        when (intent?.getStringExtra("NAVIGATE_TO")) {
                            "THEME_LIBRARY" -> SettingsScreen.THEME_LIBRARY
                            "VAULT" -> SettingsScreen.VAULT
                            "CLIPBOARD" -> SettingsScreen.CLIPBOARD
                            "SOUND_HAPTIC" -> SettingsScreen.SOUND_HAPTIC
                            else -> null
                        }
                    }

                    var currentScreen by remember(settings.onboardingCompleted, initialTarget) {
                        mutableStateOf(
                            when {
                                !settings.onboardingCompleted -> SettingsScreen.ONBOARDING
                                initialTarget != null -> initialTarget
                                else -> SettingsScreen.HOME
                            }
                        )
                    }

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            SettingsScreen.ONBOARDING -> {
                                OnboardingView(
                                    activeLanguages = settings.activeLanguages,
                                    isKeyboardEnabled = isKeyboardEnabled,
                                    isKeyboardSelected = isKeyboardSelected,
                                    onUpdateLanguages = { langs ->
                                        scope.launch { app.preferences.updateActiveLanguages(langs) }
                                    },
                                    onComplete = {
                                        scope.launch {
                                            app.preferences.setOnboardingCompleted(true)
                                            currentScreen = SettingsScreen.HOME
                                        }
                                    }
                                )
                            }

                            SettingsScreen.HOME -> {
                                SettingsHomeScreen(
                                    settings = settings,
                                    isKeyboardEnabled = isKeyboardEnabled,
                                    isKeyboardSelected = isKeyboardSelected,
                                    onNavigate = { target -> currentScreen = target }
                                )
                            }

                            SettingsScreen.THEME_LIBRARY -> {
                                ThemeLibraryScreen(
                                    currentThemeId = settings.theme,
                                    currentUiMode = settings.uiMode,
                                    onSelectTheme = { themeId ->
                                        scope.launch { app.preferences.updateTheme(themeId) }
                                    },
                                    onUpdateUiMode = { mode ->
                                        scope.launch { app.preferences.updateUiMode(mode) }
                                    },
                                    onBack = { currentScreen = SettingsScreen.HOME }
                                )
                            }

                            SettingsScreen.KEYBOARD_PREFS -> {
                                KeyboardPrefsScreen(
                                    settings = settings,
                                    onUpdateActiveLanguages = { langs ->
                                        scope.launch { app.preferences.updateActiveLanguages(langs) }
                                    },
                                    onUpdateTheme = { theme ->
                                        scope.launch { app.preferences.updateTheme(theme) }
                                    },
                                    onUpdateHeight = { height ->
                                        scope.launch { app.preferences.updateKeyboardHeightRatio(height) }
                                    },
                                    onUpdateNumberRow = { enabled ->
                                        scope.launch { app.preferences.updateShowNumberRow(enabled) }
                                    },
                                    onUpdateOneHanded = { mode ->
                                        scope.launch { app.preferences.updateOneHandedMode(mode) }
                                    },
                                    onUpdateOneHandedHeightDp = { height ->
                                        scope.launch { app.preferences.updateOneHandedHeightDp(height) }
                                    },
                                    onUpdateOneHandedTheme = { theme ->
                                        scope.launch { app.preferences.updateOneHandedTheme(theme) }
                                    },
                                    onUpdateOneHandedRotateText = { rotate ->
                                        scope.launch { app.preferences.updateOneHandedRotateText(rotate) }
                                    },
                                    onUpdateOneHandedArcScale = { scale ->
                                        scope.launch { app.preferences.updateOneHandedArcScale(scale) }
                                    },
                                    onUpdateOneHandedShowSuggestions = { show ->
                                        scope.launch { app.preferences.updateOneHandedShowSuggestions(show) }
                                    },
                                    onBack = { currentScreen = SettingsScreen.HOME }
                                )
                            }

                            SettingsScreen.CUSTOMIZE_KEYS -> {
                                KeyboardCustomizeScreen(
                                    settings = settings,
                                    onUpdateShowEmojiKey = { show ->
                                        scope.launch { app.preferences.updateShowEmojiKey(show) }
                                    },
                                    onUpdateShowLanguageKey = { show ->
                                        scope.launch { app.preferences.updateShowLanguageKey(show) }
                                    },
                                    onUpdateShowKeySubLabels = { show ->
                                        scope.launch { app.preferences.updateShowKeySubLabels(show) }
                                    },
                                    onUpdateKeyPopupMode = { mode ->
                                        scope.launch { app.preferences.updateKeyPopupMode(mode) }
                                    },
                                    onUpdateShowNumberRow = { enabled ->
                                        scope.launch { app.preferences.updateShowNumberRow(enabled) }
                                    },
                                    onUpdateHeightRatio = { height ->
                                        scope.launch { app.preferences.updateKeyboardHeightRatio(height) }
                                    },
                                    onBack = { currentScreen = SettingsScreen.HOME }
                                )
                            }

                            SettingsScreen.TYPING_PREFS -> {
                                TypingPrefsScreen(
                                    settings = settings,
                                    onUpdateSuggestions = { enabled ->
                                        scope.launch { app.preferences.updateSuggestionsEnabled(enabled) }
                                    },
                                    onUpdateAutoCorrection = { enabled ->
                                        scope.launch { app.preferences.updateAutoCorrection(enabled) }
                                    },
                                    onUpdateAutoCap = { enabled ->
                                        scope.launch { app.preferences.updateAutoCapitalization(enabled) }
                                    },
                                    onUpdateAutoSpace = { enabled ->
                                        scope.launch { app.preferences.updateAutoSpacing(enabled) }
                                    },
                                    onUpdateKeyPreview = { enabled ->
                                        scope.launch { app.preferences.updateKeyPreviewEnabled(enabled) }
                                    },
                                    onBack = { currentScreen = SettingsScreen.HOME }
                                )
                            }

                            SettingsScreen.SOUND_HAPTIC -> {
                                SoundHapticScreen(
                                    settings = settings,
                                    onUpdateSound = { enabled ->
                                        scope.launch { app.preferences.updateSoundEnabled(enabled) }
                                    },
                                    onUpdateVibration = { enabled ->
                                        scope.launch { app.preferences.updateVibrationEnabled(enabled) }
                                    },
                                    onUpdateStrength = { strength ->
                                        scope.launch { app.preferences.updateVibrationStrength(strength) }
                                    },
                                    onBack = { currentScreen = SettingsScreen.HOME }
                                )
                            }

                            SettingsScreen.DICTIONARY -> {
                                DictionaryScreen(
                                    words = dictionaryWords,
                                    onDeleteWord = { id ->
                                        scope.launch { app.repository.deleteWord(id) }
                                    },
                                    onClearAll = {
                                        scope.launch { app.repository.clearDictionary() }
                                    },
                                    onBack = { currentScreen = SettingsScreen.HOME }
                                )
                            }

                            SettingsScreen.SHORTCUTS -> {
                                ShortcutsScreen(
                                    shortcuts = shortcuts,
                                    onAddShortcut = { s, e ->
                                        scope.launch { app.repository.addOrUpdateShortcut(s, e) }
                                    },
                                    onDeleteShortcut = { id ->
                                        scope.launch { app.repository.deleteShortcut(id) }
                                    },
                                    onBack = { currentScreen = SettingsScreen.HOME }
                                )
                            }

                            SettingsScreen.CLIPBOARD -> {
                                ClipboardPrefsScreen(
                                    items = clipboardItems,
                                    onTogglePin = { item ->
                                        scope.launch { app.repository.togglePinClipboardItem(item) }
                                    },
                                    onDeleteItem = { id ->
                                        scope.launch { app.repository.deleteClipboardItem(id) }
                                    },
                                    onClearAll = {
                                        scope.launch { app.repository.clearClipboard() }
                                    },
                                    onBack = { currentScreen = SettingsScreen.HOME }
                                )
                            }

                            SettingsScreen.VAULT -> {
                                VaultPrefsScreen(
                                    credentials = savedCredentials,
                                    onAddCredential = { service, user, pass ->
                                        scope.launch { app.repository.saveCredential(service, user, pass) }
                                    },
                                    onDeleteCredential = { id ->
                                        scope.launch { app.repository.deleteCredential(id) }
                                    },
                                    onTogglePin = { cred ->
                                        scope.launch { app.repository.togglePinCredential(cred) }
                                    },
                                    onClearAll = {
                                        scope.launch { app.repository.clearAllCredentials() }
                                    },
                                    onBack = { currentScreen = SettingsScreen.HOME }
                                )
                            }

                            SettingsScreen.PRIVACY -> {
                                PrivacyScreen(onBack = { currentScreen = SettingsScreen.HOME })
                            }

                            SettingsScreen.ABOUT -> {
                                AboutScreen(onBack = { currentScreen = SettingsScreen.HOME })
                            }
                        }
                    }
                }
            }
        }
    }
}
