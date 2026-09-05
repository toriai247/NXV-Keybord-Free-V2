package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ClipboardItem
import com.example.data.entity.DictionaryWord
import com.example.data.entity.SavedCredential
import com.example.data.entity.TextShortcut
import com.example.data.preferences.KeyboardSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    settings: KeyboardSettings,
    isKeyboardEnabled: Boolean = true,
    isKeyboardSelected: Boolean = true,
    onNavigate: (SettingsScreen) -> Unit
) {
    val context = LocalContext.current
    var testText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NX",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "NXV Keyboard",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Geometric Balance Edition",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Keyboard Activation Status Card
            if (!isKeyboardEnabled) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().testTag("card_enable_ime")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "কিবোর্ড চালু করা নেই (Not Enabled)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "ধাপ ১: Android সেটিংসে গিয়ে NXV Keyboard এনাবল করুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_action_enable_settings"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Keyboard, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("সেটিংসে কিবোর্ড চালু করুন", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else if (!isKeyboardSelected) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().testTag("card_select_ime")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "ডিফল্ট কিবোর্ড নির্বাচন করুন (Set Default)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "ধাপ ২: NXV Keyboard-কে সক্রিয় কিবোর্ড হিসেবে নির্বাচন করুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                imm?.showInputMethodPicker()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_action_select_default"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("NXV Keyboard সিলেক্ট করুন", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth().testTag("card_ime_ready")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NXV Keyboard সক্রিয় ও প্রস্তুত!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "সব অ্যাপে (Google Search, WhatsApp ইত্যাদি) টাইপ করার জন্য প্রস্তুত।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Interactive Typing Bench Card (Geometric Balance chat preview)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Chat preview messages
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Incoming message
                        Box(
                            modifier = Modifier
                                .align(Alignment.Start)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "How do I type in Bengali using Avro phonetic?",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Outgoing message
                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Just write 'ami banglay likhi' and it translates instantly!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // Translated output
                        Box(
                            modifier = Modifier
                                .align(Alignment.Start)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "আমি বাংলায় লিখি ✨",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Interactive Input Field
                    OutlinedTextField(
                        value = testText,
                        onValueChange = { testText = it },
                        placeholder = { Text("এখানে ট্যাপ করে কিবোর্ড পরীক্ষা করুন...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("test_typing_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_show_keyboard_now")
                        ) {
                            Icon(imageVector = Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("কিবোর্ড ওপেন করুন", fontSize = 13.sp)
                        }

                        TextButton(
                            onClick = {
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                imm?.showInputMethodPicker()
                            }
                        ) {
                            Text("Switch Keyboard", fontSize = 13.sp)
                        }
                    }
                }
            }

            // Keyboard Settings Group
            SettingsCategoryHeader("KEYBOARD")
            SettingsTile(
                icon = Icons.Default.Keyboard,
                title = "Languages & Layouts",
                subtitle = "English, বাংলা, Avro phonetic typing",
                onClick = { onNavigate(SettingsScreen.KEYBOARD_PREFS) },
                tag = "tile_keyboard"
            )
            SettingsTile(
                icon = Icons.Default.Tune,
                title = "Layout, Buttons & Popups (কাস্টমাইজেশন)",
                subtitle = "Toggle Emoji / Lang buttons, key hints, popup modes",
                onClick = { onNavigate(SettingsScreen.CUSTOMIZE_KEYS) },
                tag = "tile_customize_keys"
            )
            SettingsTile(
                icon = Icons.Default.Palette,
                title = "Themes & Appearance",
                subtitle = "Puppy Pop, Strawberry Dessert, AMOLED, Dark, Light",
                onClick = { onNavigate(SettingsScreen.THEME_LIBRARY) },
                tag = "tile_theme"
            )

            // Typing Group
            SettingsCategoryHeader("TYPING")
            SettingsTile(
                icon = Icons.Default.TextFields,
                title = "Text Correction & Suggestions",
                subtitle = "Autocorrect, suggestions strip, auto-spacing",
                onClick = { onNavigate(SettingsScreen.TYPING_PREFS) },
                tag = "tile_typing"
            )
            SettingsTile(
                icon = Icons.Default.VolumeUp,
                title = "Sound & Haptic Feedback",
                subtitle = "Key sound, vibration strength",
                onClick = { onNavigate(SettingsScreen.SOUND_HAPTIC) },
                tag = "tile_sound"
            )

            // Intelligent AI Group
            SettingsCategoryHeader("INTELLIGENT AI")
            SettingsTile(
                icon = Icons.Default.AutoAwesome,
                title = "Gemini AI Assistant ✦",
                subtitle = "API key, models, token budget & usage info",
                onClick = { onNavigate(SettingsScreen.GEMINI_AI_PREFS) },
                tag = "tile_gemini_ai"
            )

            // Data & Tools Group
            SettingsCategoryHeader("DATA & TOOLS")
            SettingsTile(
                icon = Icons.Default.Lock,
                title = "Password & Account Vault",
                subtitle = "Auto-saved passwords, credentials, 1-tap autofill",
                onClick = { onNavigate(SettingsScreen.VAULT) },
                tag = "tile_vault"
            )
            SettingsTile(
                icon = Icons.Default.ContentPaste,
                title = "Copypad (Clipboard)",
                subtitle = "Permanent storage until deleted or cleared",
                onClick = { onNavigate(SettingsScreen.CLIPBOARD) },
                tag = "tile_clipboard"
            )
            SettingsTile(
                icon = Icons.Default.Book,
                title = "Personal Dictionary",
                subtitle = "Learned words for English and Bangla",
                onClick = { onNavigate(SettingsScreen.DICTIONARY) },
                tag = "tile_dictionary"
            )
            SettingsTile(
                icon = Icons.Default.TextFields,
                title = "Text Shortcuts",
                subtitle = "Expand abbreviations (e.g. brb -> Be right back)",
                onClick = { onNavigate(SettingsScreen.SHORTCUTS) },
                tag = "tile_shortcuts"
            )

            // Privacy & Info Group
            SettingsCategoryHeader("SECURITY & ABOUT")
            SettingsTile(
                icon = Icons.Default.Security,
                title = "Privacy & Security",
                subtitle = "100% offline, zero text tracking guarantee",
                onClick = { onNavigate(SettingsScreen.PRIVACY) },
                tag = "tile_privacy"
            )
            SettingsTile(
                icon = Icons.Default.Info,
                title = "About NXV Keyboard",
                subtitle = "Developed by Rony Ahmmad • Naxxivo",
                onClick = { onNavigate(SettingsScreen.ABOUT) },
                tag = "tile_about"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Branding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "NXV Keyboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Official Brand: Naxxivo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "© 2026 Naxxivo • All Rights Reserved.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsCategoryHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
private fun SettingsTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// Keyboard & Themes Preferences Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardPrefsScreen(
    settings: KeyboardSettings,
    onUpdateActiveLanguages: (Set<String>) -> Unit,
    onUpdateTheme: (String) -> Unit,
    onUpdateHeight: (Float) -> Unit,
    onUpdateNumberRow: (Boolean) -> Unit,
    onUpdateOneHanded: (String) -> Unit,
    onUpdateOneHandedHeightDp: ((Int) -> Unit)? = null,
    onUpdateOneHandedTheme: ((String) -> Unit)? = null,
    onUpdateOneHandedRotateText: ((Boolean) -> Unit)? = null,
    onUpdateOneHandedArcScale: ((Float) -> Unit)? = null,
    onUpdateOneHandedShowSuggestions: ((Boolean) -> Unit)? = null,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keyboard & Layout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Languages
            Text("Active Languages", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            val langs = listOf(
                "english" to "English (QWERTY)",
                "bangla" to "বাংলা (Bangla Unicode)",
                "avro" to "Avro Phonetic (Bangla)"
            )
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    langs.forEach { (id, name) ->
                        val isChecked = settings.activeLanguages.contains(id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val set = settings.activeLanguages.toMutableSet()
                                    if (isChecked) {
                                        if (set.size > 1) set.remove(id)
                                    } else {
                                        set.add(id)
                                    }
                                    onUpdateActiveLanguages(set)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    val set = settings.activeLanguages.toMutableSet()
                                    if (checked) set.add(id) else if (set.size > 1) set.remove(id)
                                    onUpdateActiveLanguages(set)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = name, fontSize = 15.sp)
                        }
                    }
                }
            }

            // Theme Selector
            Text("Keyboard Theme", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            val themes = listOf(
                "puppy_pop" to "Puppy Pop (Neumorphic White)",
                "strawberry_dessert" to "Strawberry Dessert (Sweet Pink)",
                "geometric" to "Geometric Balance (Default)",
                "dark" to "Dark (Slate)",
                "light" to "Light (Crisp)",
                "amoled" to "AMOLED (Pure Black)",
                "custom" to "Indigo Glow"
            )
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    themes.forEach { (themeId, themeName) ->
                        val isSelected = settings.theme.equals(themeId, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateTheme(themeId) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onUpdateTheme(themeId) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = themeName, fontSize = 15.sp)
                        }
                    }
                }
            }

            // Keyboard Height
            Text("Keyboard Height: ${(settings.keyboardHeightRatio * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Slider(
                        value = settings.keyboardHeightRatio,
                        onValueChange = { onUpdateHeight(it) },
                        valueRange = 0.85f..1.25f,
                        steps = 7
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Short", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Normal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tall", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Number Row Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Number Row", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("Always display number row above QWERTY", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = settings.showNumberRow,
                    onCheckedChange = { onUpdateNumberRow(it) }
                )
            }

            // One-Handed Mode Settings
            Text("One-Handed Mode Options", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            val oneHandedOptions = listOf(
                "none" to "Standard (Full Width)",
                "left" to "Left-Handed Arc Keyboard",
                "right" to "Right-Handed Arc Keyboard"
            )
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    oneHandedOptions.forEach { (modeId, modeName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateOneHanded(modeId) }
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.oneHandedMode == modeId,
                                onClick = { onUpdateOneHanded(modeId) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = modeName, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    if (settings.oneHandedMode != "none") {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // One-Handed Size (Height)
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Arc Keyboard Size (Height)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("${settings.oneHandedHeightDp}dp", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = settings.oneHandedHeightDp.toFloat(),
                                onValueChange = { onUpdateOneHandedHeightDp?.invoke(it.toInt()) },
                                valueRange = 260f..380f,
                                steps = 5
                            )
                        }

                        // Arc Reach Scale
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Thumb Reach (Arc Scale)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("${(settings.oneHandedArcScale * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = settings.oneHandedArcScale,
                                onValueChange = { onUpdateOneHandedArcScale?.invoke(it) },
                                valueRange = 0.80f..1.25f,
                                steps = 8
                            )
                        }

                        // Theme Selection for Arc Keyboard
                        Text("Arc Color Style", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        val arcThemes = listOf(
                            "theme_match" to "Keyboard Match",
                            "light_crisp" to "Light Crisp",
                            "amoled_dark" to "AMOLED Dark",
                            "rose_pastel" to "Rose Pastel",
                            "sky_cyan" to "Sky Cyan"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            arcThemes.forEach { (tId, tName) ->
                                val isSel = settings.oneHandedTheme == tId
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                        .clickable { onUpdateOneHandedTheme?.invoke(tId) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Rotate Text Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Rotate Text Along Arc", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(if (settings.oneHandedRotateText) "Text follows arc angle" else "Text stays upright", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = settings.oneHandedRotateText,
                                onCheckedChange = { onUpdateOneHandedRotateText?.invoke(it) }
                            )
                        }

                        // Predictive Suggestions Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Predictive Word Bubbles", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Circular pills for word suggestions", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = settings.oneHandedShowSuggestions,
                                onCheckedChange = { onUpdateOneHandedShowSuggestions?.invoke(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Typing Preferences Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypingPrefsScreen(
    settings: KeyboardSettings,
    onUpdateSuggestions: (Boolean) -> Unit,
    onUpdateAutoCorrection: (Boolean) -> Unit,
    onUpdateAutoCap: (Boolean) -> Unit,
    onUpdateAutoSpace: (Boolean) -> Unit,
    onUpdateKeyPreview: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Typing Preferences") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingSwitchRow(
                title = "Suggestion Strip",
                subtitle = "Show word suggestions, Avro candidates, and shortcuts",
                checked = settings.suggestionsEnabled,
                onCheckedChange = onUpdateSuggestions
            )

            SettingSwitchRow(
                title = "Auto-Correction",
                subtitle = "Automatically correct misspelled words when Space is pressed",
                checked = settings.autoCorrection,
                onCheckedChange = onUpdateAutoCorrection
            )

            SettingSwitchRow(
                title = "Auto-Capitalization",
                subtitle = "Capitalize the first letter of sentences automatically",
                checked = settings.autoCapitalization,
                onCheckedChange = onUpdateAutoCap
            )

            SettingSwitchRow(
                title = "Auto-Spacing",
                subtitle = "Insert a space automatically after picking a suggestion",
                checked = settings.autoSpacing,
                onCheckedChange = onUpdateAutoSpace
            )

            SettingSwitchRow(
                title = "Key Press Preview",
                subtitle = "Show popup preview of the key being touched",
                checked = settings.keyPreviewEnabled,
                onCheckedChange = onUpdateKeyPreview
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// -------------------------------------------------------------
// Sound & Haptic Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundHapticScreen(
    settings: KeyboardSettings,
    onUpdateSound: (Boolean) -> Unit,
    onUpdateVibration: (Boolean) -> Unit,
    onUpdateStrength: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sound & Haptics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingSwitchRow(
                title = "Key Sound",
                subtitle = "Play subtle sound click on key press",
                checked = settings.keySoundEnabled,
                onCheckedChange = onUpdateSound
            )

            SettingSwitchRow(
                title = "Key Vibration",
                subtitle = if (settings.keyVibrationEnabled) "Haptic vibration feedback is ON" else "Haptic vibration feedback is OFF (Silent)",
                checked = settings.keyVibrationEnabled,
                onCheckedChange = onUpdateVibration
            )

            // Quick Vibration Turn Off / Turn On Shortcut Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onUpdateVibration(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!settings.keyVibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (!settings.keyVibrationEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Vibration OFF", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = { onUpdateVibration(true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (settings.keyVibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (settings.keyVibrationEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Vibration ON", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (settings.keyVibrationEnabled) {
                Text("Vibration Strength", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                val strengths = listOf("low" to "Low (Subtle)", "medium" to "Medium (Standard)", "high" to "High (Crisp)")
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        strengths.forEach { (id, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUpdateStrength(id) }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.vibrationStrength == id,
                                    onClick = { onUpdateStrength(id) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = label, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Personal Dictionary Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    words: List<DictionaryWord>,
    onDeleteWord: (Long) -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Dictionary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (words.isNotEmpty()) {
                        TextButton(onClick = onClearAll) {
                            Text("Clear All")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (words.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No learned words yet", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Words you type frequently will be learned locally here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(words, key = { it.id }) { word ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = word.word, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(text = "Language: ${word.locale.uppercase()} • Frequency: ${word.frequency}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onDeleteWord(word.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Text Shortcuts Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutsScreen(
    shortcuts: List<TextShortcut>,
    onAddShortcut: (String, String) -> Unit,
    onDeleteShortcut: (Long) -> Unit,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var shortcutText by remember { mutableStateOf("") }
    var expansionText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Text Shortcuts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Shortcut")
                    }
                }
            )
        }
    ) { padding ->
        if (shortcuts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No shortcuts created", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tap + to create abbreviations (e.g. brb -> Be right back)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shortcuts, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = item.shortcut, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                Text(text = item.expansion, fontSize = 14.sp)
                            }
                            IconButton(onClick = { onDeleteShortcut(item.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Text Shortcut") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = shortcutText,
                            onValueChange = { shortcutText = it },
                            label = { Text("Shortcut (e.g. gm)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = expansionText,
                            onValueChange = { expansionText = it },
                            label = { Text("Expansion (e.g. Good morning)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (shortcutText.isNotBlank() && expansionText.isNotBlank()) {
                                onAddShortcut(shortcutText, expansionText)
                                shortcutText = ""
                                expansionText = ""
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// -------------------------------------------------------------
// Clipboard Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardPrefsScreen(
    items: List<ClipboardItem>,
    onTogglePin: (ClipboardItem) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clipboard History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        TextButton(onClick = onClearAll) {
                            Text("Clear")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Privacy Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clipboard data is stored strictly in your local device database. It never leaves your phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No clipboard items saved", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.text,
                                        fontSize = 14.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = { onTogglePin(item) }) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = if (item.isPinned) "Unpin" else "Pin",
                                        tint = if (item.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { onDeleteItem(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Password & Account Vault Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultPrefsScreen(
    credentials: List<SavedCredential>,
    onAddCredential: (service: String, user: String, pass: String) -> Unit,
    onDeleteCredential: (Long) -> Unit,
    onTogglePin: (SavedCredential) -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var revealedIds by remember { mutableStateOf(setOf<Long>()) }

    var newService by remember { mutableStateOf("") }
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    val filtered = if (searchQuery.isBlank()) {
        credentials
    } else {
        credentials.filter {
            it.serviceName.contains(searchQuery, ignoreCase = true) ||
            it.username.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Password Vault") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (credentials.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Password")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Security Guarantee Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔐", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "100% On-Device Encrypted Storage",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Auto-saves passwords when you type them so you never forget your login credentials. Offline and secure.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Search Bar
            if (credentials.size > 2) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search accounts or services...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (credentials.isEmpty()) "No saved passwords yet" else "No matching accounts found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "When you type passwords in any app, NXV Keyboard will offer to auto-save them for you.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { item ->
                        val isRevealed = revealedIds.contains(item.id)

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isPinned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.serviceName.ifBlank { "Account" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (item.isPinned) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = "Pinned",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { onTogglePin(item) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = if (item.isPinned) "Unpin" else "Pin",
                                                tint = if (item.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteCredential(item.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                if (item.username.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .clickable {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                                clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Username", item.username))
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = item.username,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text("Copy", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                            clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Password", item.password))
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isRevealed) item.password else "••••••••••••",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clickable {
                                                    revealedIds = if (isRevealed) revealedIds - item.id else revealedIds + item.id
                                                }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (isRevealed) "Hide" else "Show",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isRevealed) "Hide" else "Show",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Copy", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Account / Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newService,
                        onValueChange = { newService = it },
                        label = { Text("Service / Website / App") },
                        placeholder = { Text("e.g. Google, Facebook, Bank") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("Email / Username / Phone") },
                        placeholder = { Text("e.g. user@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Password") },
                        placeholder = { Text("Enter password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword.isNotBlank()) {
                            onAddCredential(newService, newUsername, newPassword)
                            newService = ""
                            newUsername = ""
                            newPassword = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Save to Vault")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Saved Passwords?") },
            text = { Text("This will permanently delete all saved credentials from your device.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// Privacy & Security Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Privacy-First Guarantee", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "NXV Keyboard is engineered from the ground up to protect your privacy. Your keystrokes belong to you and nobody else.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            PrivacyPoint(
                icon = Icons.Default.Language,
                title = "100% Offline Operation",
                description = "NXV Keyboard does not request or hold internet permissions. Keystrokes cannot be transmitted across the network."
            )

            PrivacyPoint(
                icon = Icons.Default.Lock,
                title = "Zero Text Logging",
                description = "We never log your keystrokes, personal messages, credit cards, or passwords."
            )

            PrivacyPoint(
                icon = Icons.Default.Security,
                title = "Password Field Isolation",
                description = "When a password field is focused, suggestion algorithms and dictionary learning are completely disabled."
            )

            PrivacyPoint(
                icon = Icons.Default.Storage,
                title = "Local Device Storage Only",
                description = "Your personal dictionary words, text shortcuts, and clipboard history exist exclusively in an encrypted-at-rest SQLite database on your hardware."
            )

            PrivacyPoint(
                icon = Icons.Default.Person,
                title = "No Account Required",
                description = "All features are immediately available without creating an account or providing an email address."
            )
        }
    }
}

@Composable
private fun PrivacyPoint(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// -------------------------------------------------------------
// About Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About NXV Keyboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text("NXV Keyboard", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(
                "Version 1.0.0 (Production Build)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Brand & Ownership Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Official Brand",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Naxxivo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Owned & Developed By",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rony Ahmmad",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Co-Owner & Developer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rony",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Official Websites Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Official Websites",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Link 1: naxxivo.online
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://naxxivo.online"))
                                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(browserIntent)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌐", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("naxxivo.online", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Official Website Portal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Link 2: naxxivo.xyz
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://naxxivo.xyz"))
                                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(browserIntent)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌐", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("naxxivo.xyz", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Official Mirror Domain", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Architecture Highlights
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Architecture Highlights", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("• Native Android InputMethodService (IME)", style = MaterialTheme.typography.bodySmall)
                    Text("• Jetpack Compose Hardware Accelerated Rendering", style = MaterialTheme.typography.bodySmall)
                    Text("• Modular Avro Phonetic Parser & Unicode Conversion", style = MaterialTheme.typography.bodySmall)
                    Text("• Grapheme-Aware Deletion & Combining Unicode Safety", style = MaterialTheme.typography.bodySmall)
                    Text("• Room Database Local Persistence & Preferences DataStore", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Copyright Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "© 2026 Naxxivo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "All Rights Reserved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiAiPrefsScreen(
    settings: KeyboardSettings,
    onUpdateApiKey: (String) -> Unit,
    onUpdateModel: (String) -> Unit,
    onResetTokens: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var keyText by remember(settings.geminiApiKey) { mutableStateOf(settings.geminiApiKey) }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Gemini AI Assistant ✦", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intro Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "কিবোর্ডে সরাসরি ইন্টেলিজেন্ট এআই!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gemini API-র সাহায্যে সরাসরি টাইপিংয়ের সময় ভুল বানান/ব্যাকরণ ঠিক করুন, দ্রুত চ্যাট রিপ্লাই জেনারেট করুন, কবিতা বা ছন্দ লিখুন, অথবা যেকোনো কাস্টম প্রম্পট রান করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // API Key Card
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔑 Gemini Free API Key",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        TextButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Get Free Key", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(androidx.compose.material.icons.Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }

                    OutlinedTextField(
                        value = keyText,
                        onValueChange = { keyText = it },
                        placeholder = { Text("Paste AI Studio API Key (AIzaSy...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Button(
                        onClick = {
                            onUpdateApiKey(keyText.trim())
                            android.widget.Toast.makeText(context, "Gemini API Key Saved!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Key")
                    }
                }
            }

            // Model Selection Card
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "🧠 Choose Gemini AI Model",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "1.5 Flash দ্রুত গতির জন্য সেরা, আর 1.5 Pro আরও ক্রিয়েটিভ ও বুদ্ধিমান উত্তরের জন্য ভালো।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val modelsList = listOf(
                        "gemini-1.5-flash" to "Gemini 1.5 Flash ⚡ (Recommended / Fast & Reliable)",
                        "gemini-1.5-pro" to "Gemini 1.5 Pro 🧠 (Creative & Deep Reasoning)",
                        "gemini-1.5-flash-8b" to "Gemini 1.5 Flash-8b 🚀 (Super Lite / Ultra Cost Efficient)"
                    )

                    modelsList.forEach { (code, desc) ->
                        val isSelected = settings.geminiModel == code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { onUpdateModel(code) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onUpdateModel(code) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = desc,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Token Usage & Budget Card
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "📊 AI Resource Usage (এআই টোকেন বাজেট)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Tokens Used",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${settings.totalTokensUsed} tokens",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = onResetTokens,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(androidx.compose.material.icons.Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Count", fontSize = 12.sp)
                        }
                    }

                    Text(
                        text = "গড়পড়তা ১টি বাংলা বাক্যে ১৫-৫০টি টোকেন খরচ হয়। ফ্রি টায়ারে প্রতি মিনিটে ১৫টি রিকোয়েস্ট পর্যন্ত ফ্রিতে ব্যবহার করা যায়। টোকেন ব্যবহারের হিসাব দেখে আপনি সহজে বাজেট নিয়ন্ত্রণ করতে পারবেন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Key AI Features Description
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💡 এআই কীভাবে ওপেন করবেন?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("যেকোনো অ্যাপে কিবোর্ড ওপেন করে সাজেশন স্ট্রিপে (কিবোর্ডের ওপরের অংশ) 'AI ✦' বাটনটিতে ট্যাপ করলেই এআই এসিস্ট্যান্ট প্যানেল খুলে যাবে। সেখান থেকে সরাসরি লেখা ঠিক করতে বা কবিতা তৈরি করে ১-ক্লিকে ইনসার্ট করতে পারবেন!", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
