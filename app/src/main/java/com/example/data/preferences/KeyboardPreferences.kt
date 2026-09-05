package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nxv_keyboard_prefs")

data class KeyboardSettings(
    val activeLanguages: Set<String> = setOf("english", "bangla", "avro"),
    val currentLanguage: String = "english",
    val theme: String = "dark",
    val uiMode: String = "original",
    val keyboardHeightRatio: Float = 1.0f,
    val showNumberRow: Boolean = false,
    val oneHandedMode: String = "none",
    val preferredHand: String = "right",
    val oneHandedHeightDp: Int = 330,
    val oneHandedTheme: String = "theme_match",
    val oneHandedRotateText: Boolean = true,
    val oneHandedArcScale: Float = 1.0f,
    val oneHandedShowSuggestions: Boolean = true,
    val oneHandedKeyStyle: String = "clean_arc",
    val keySoundEnabled: Boolean = false,
    val keyVibrationEnabled: Boolean = true,
    val vibrationStrength: String = "medium",
    val autoCapitalization: Boolean = true,
    val autoSpacing: Boolean = true,
    val suggestionsEnabled: Boolean = true,
    val autoCorrection: Boolean = true,
    val keyPreviewEnabled: Boolean = true,
    val showEmojiKey: Boolean = true,
    val showLanguageKey: Boolean = true,
    val showKeySubLabels: Boolean = true,
    val keyPopupMode: String = "popup",
    val onboardingCompleted: Boolean = false,
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-1.5-flash",
    val totalTokensUsed: Long = 0L
)

class KeyboardPreferences(private val context: Context) {

    companion object {
        val KEY_ACTIVE_LANGUAGES = stringSetPreferencesKey("active_languages")
        val KEY_CURRENT_LANGUAGE = stringPreferencesKey("current_language")
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_UI_MODE = stringPreferencesKey("selected_ui_mode")
        val KEY_HEIGHT_RATIO = floatPreferencesKey("keyboard_height_ratio")
        val KEY_SHOW_NUMBER_ROW = booleanPreferencesKey("show_number_row")
        val KEY_ONE_HANDED_MODE = stringPreferencesKey("one_handed_mode")
        val KEY_PREFERRED_HAND = stringPreferencesKey("preferred_hand")
        val KEY_ONE_HANDED_HEIGHT = floatPreferencesKey("one_handed_height_dp")
        val KEY_ONE_HANDED_THEME = stringPreferencesKey("one_handed_theme")
        val KEY_ONE_HANDED_ROTATE_TEXT = booleanPreferencesKey("one_handed_rotate_text")
        val KEY_ONE_HANDED_ARC_SCALE = floatPreferencesKey("one_handed_arc_scale")
        val KEY_ONE_HANDED_SHOW_SUGG = booleanPreferencesKey("one_handed_show_sugg")
        val KEY_ONE_HANDED_KEY_STYLE = stringPreferencesKey("one_handed_key_style")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("key_sound_enabled")
        val KEY_VIBRATION_ENABLED = booleanPreferencesKey("key_vibration_enabled")
        val KEY_VIBRATION_STRENGTH = stringPreferencesKey("vibration_strength")
        val KEY_AUTO_CAPITALIZATION = booleanPreferencesKey("auto_capitalization")
        val KEY_AUTO_SPACING = booleanPreferencesKey("auto_spacing")
        val KEY_SUGGESTIONS_ENABLED = booleanPreferencesKey("suggestions_enabled")
        val KEY_AUTO_CORRECTION = booleanPreferencesKey("auto_correction")
        val KEY_KEY_PREVIEW = booleanPreferencesKey("key_preview_enabled")
        val KEY_SHOW_EMOJI_KEY = booleanPreferencesKey("show_emoji_key")
        val KEY_SHOW_LANGUAGE_KEY = booleanPreferencesKey("show_language_key")
        val KEY_SHOW_KEY_SUBLABELS = booleanPreferencesKey("show_key_sublabels")
        val KEY_KEY_POPUP_MODE = stringPreferencesKey("key_popup_mode")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val KEY_TOTAL_TOKENS_USED = androidx.datastore.preferences.core.longPreferencesKey("total_tokens_used")
    }

    val settingsFlow: Flow<KeyboardSettings> = context.dataStore.data.map { prefs ->
        KeyboardSettings(
            activeLanguages = prefs[KEY_ACTIVE_LANGUAGES] ?: setOf("english", "bangla", "avro"),
            currentLanguage = prefs[KEY_CURRENT_LANGUAGE] ?: "english",
            theme = prefs[KEY_THEME] ?: "dark",
            uiMode = prefs[KEY_UI_MODE] ?: "original",
            keyboardHeightRatio = prefs[KEY_HEIGHT_RATIO] ?: 1.0f,
            showNumberRow = prefs[KEY_SHOW_NUMBER_ROW] ?: false,
            oneHandedMode = prefs[KEY_ONE_HANDED_MODE] ?: "none",
            preferredHand = prefs[KEY_PREFERRED_HAND] ?: "right",
            oneHandedHeightDp = (prefs[KEY_ONE_HANDED_HEIGHT] ?: 330f).toInt(),
            oneHandedTheme = prefs[KEY_ONE_HANDED_THEME] ?: "theme_match",
            oneHandedRotateText = prefs[KEY_ONE_HANDED_ROTATE_TEXT] ?: true,
            oneHandedArcScale = prefs[KEY_ONE_HANDED_ARC_SCALE] ?: 1.0f,
            oneHandedShowSuggestions = prefs[KEY_ONE_HANDED_SHOW_SUGG] ?: true,
            oneHandedKeyStyle = prefs[KEY_ONE_HANDED_KEY_STYLE] ?: "clean_arc",
            keySoundEnabled = prefs[KEY_SOUND_ENABLED] ?: false,
            keyVibrationEnabled = prefs[KEY_VIBRATION_ENABLED] ?: true,
            vibrationStrength = prefs[KEY_VIBRATION_STRENGTH] ?: "medium",
            autoCapitalization = prefs[KEY_AUTO_CAPITALIZATION] ?: true,
            autoSpacing = prefs[KEY_AUTO_SPACING] ?: true,
            suggestionsEnabled = prefs[KEY_SUGGESTIONS_ENABLED] ?: true,
            autoCorrection = prefs[KEY_AUTO_CORRECTION] ?: true,
            keyPreviewEnabled = prefs[KEY_KEY_PREVIEW] ?: true,
            showEmojiKey = prefs[KEY_SHOW_EMOJI_KEY] ?: true,
            showLanguageKey = prefs[KEY_SHOW_LANGUAGE_KEY] ?: true,
            showKeySubLabels = prefs[KEY_SHOW_KEY_SUBLABELS] ?: true,
            keyPopupMode = prefs[KEY_KEY_POPUP_MODE] ?: "popup",
            onboardingCompleted = prefs[KEY_ONBOARDING_COMPLETED] ?: false,
            geminiApiKey = prefs[KEY_GEMINI_API_KEY] ?: "",
            geminiModel = prefs[KEY_GEMINI_MODEL] ?: "gemini-1.5-flash",
            totalTokensUsed = prefs[KEY_TOTAL_TOKENS_USED] ?: 0L
        )
    }

    suspend fun updateGeminiApiKey(apiKey: String) {
        context.dataStore.edit { it[KEY_GEMINI_API_KEY] = apiKey.trim() }
    }

    suspend fun updateGeminiModel(model: String) {
        context.dataStore.edit { it[KEY_GEMINI_MODEL] = model }
    }

    suspend fun addTokensUsed(tokens: Long) {
        context.dataStore.edit {
            val current = it[KEY_TOTAL_TOKENS_USED] ?: 0L
            it[KEY_TOTAL_TOKENS_USED] = current + tokens
        }
    }

    suspend fun resetTokensUsed() {
        context.dataStore.edit { it[KEY_TOTAL_TOKENS_USED] = 0L }
    }

    suspend fun updateActiveLanguages(languages: Set<String>) {
        context.dataStore.edit { it[KEY_ACTIVE_LANGUAGES] = languages }
    }

    suspend fun updateCurrentLanguage(language: String) {
        context.dataStore.edit { it[KEY_CURRENT_LANGUAGE] = language }
    }

    suspend fun updateTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun updateUiMode(mode: String) {
        context.dataStore.edit { it[KEY_UI_MODE] = mode }
    }

    suspend fun updateKeyboardHeightRatio(ratio: Float) {
        context.dataStore.edit { it[KEY_HEIGHT_RATIO] = ratio.coerceIn(0.80f, 1.30f) }
    }

    suspend fun updateShowNumberRow(show: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_NUMBER_ROW] = show }
    }

    suspend fun updateOneHandedMode(mode: String) {
        context.dataStore.edit { it[KEY_ONE_HANDED_MODE] = mode }
    }

    suspend fun updatePreferredHand(hand: String) {
        context.dataStore.edit { it[KEY_PREFERRED_HAND] = hand }
    }

    suspend fun updateOneHandedHeightDp(heightDp: Int) {
        context.dataStore.edit { it[KEY_ONE_HANDED_HEIGHT] = heightDp.toFloat() }
    }

    suspend fun updateOneHandedTheme(theme: String) {
        context.dataStore.edit { it[KEY_ONE_HANDED_THEME] = theme }
    }

    suspend fun updateOneHandedRotateText(rotate: Boolean) {
        context.dataStore.edit { it[KEY_ONE_HANDED_ROTATE_TEXT] = rotate }
    }

    suspend fun updateOneHandedArcScale(scale: Float) {
        context.dataStore.edit { it[KEY_ONE_HANDED_ARC_SCALE] = scale }
    }

    suspend fun updateOneHandedShowSuggestions(show: Boolean) {
        context.dataStore.edit { it[KEY_ONE_HANDED_SHOW_SUGG] = show }
    }

    suspend fun updateOneHandedKeyStyle(style: String) {
        context.dataStore.edit { it[KEY_ONE_HANDED_KEY_STYLE] = style }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SOUND_ENABLED] = enabled }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_VIBRATION_ENABLED] = enabled }
    }

    suspend fun updateVibrationStrength(strength: String) {
        context.dataStore.edit { it[KEY_VIBRATION_STRENGTH] = strength }
    }

    suspend fun updateAutoCapitalization(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_CAPITALIZATION] = enabled }
    }

    suspend fun updateAutoSpacing(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_SPACING] = enabled }
    }

    suspend fun updateSuggestionsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SUGGESTIONS_ENABLED] = enabled }
    }

    suspend fun updateAutoCorrection(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_CORRECTION] = enabled }
    }

    suspend fun updateKeyPreviewEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_KEY_PREVIEW] = enabled }
    }

    suspend fun updateShowEmojiKey(show: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_EMOJI_KEY] = show }
    }

    suspend fun updateShowLanguageKey(show: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_LANGUAGE_KEY] = show }
    }

    suspend fun updateShowKeySubLabels(show: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_KEY_SUBLABELS] = show }
    }

    suspend fun updateKeyPopupMode(mode: String) {
        context.dataStore.edit { it[KEY_KEY_POPUP_MODE] = mode }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }
}
