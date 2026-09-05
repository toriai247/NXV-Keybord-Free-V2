package com.example.ui.keyboard

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiKeyboardAiService
import com.example.data.preferences.KeyboardPreferences
import com.example.theme.KeyboardPalette
import kotlinx.coroutines.launch

enum class AiTabMode {
    PROOFREAD,
    REPLY_SUGGEST,
    POETRY,
    VOICE_POLISH,
    CUSTOM_PROMPT
}

@Composable
fun AiAssistantSheet(
    palette: KeyboardPalette,
    apiKey: String,
    selectedModel: String,
    totalTokensUsed: Long,
    preferences: KeyboardPreferences?,
    onInsertText: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val aiService = remember(preferences) { GeminiKeyboardAiService(preferences) }

    var currentApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var currentModel by remember(selectedModel) { mutableStateOf(selectedModel) }
    var showApiKeySection by remember { mutableStateOf(currentApiKey.isBlank()) }
    var keyInputText by remember { mutableStateOf(currentApiKey) }

    var selectedMode by remember { mutableStateOf(AiTabMode.PROOFREAD) }
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var replySuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var statusError by remember { mutableStateOf<String?>(null) }
    var lastTokenCount by remember { mutableStateOf(0) }

    val primaryColor = palette.accentColor
    val bgCard = palette.keyBackground
    val textColor = palette.textColor

    fun runAiFeature() {
        if (isLoading) return
        isLoading = true
        statusError = null
        outputText = ""
        replySuggestions = emptyList()

        coroutineScope.launch {
            when (selectedMode) {
                AiTabMode.PROOFREAD -> {
                    val res = aiService.fixGrammarAndPolish(inputText, currentApiKey, currentModel)
                    isLoading = false
                    if (res.isSuccess) {
                        outputText = res.outputText
                        lastTokenCount = res.tokensUsed
                    } else {
                        statusError = res.error
                    }
                }
                AiTabMode.REPLY_SUGGEST -> {
                    val list = aiService.suggestReplies(inputText, currentApiKey, currentModel)
                    isLoading = false
                    replySuggestions = list
                }
                AiTabMode.POETRY -> {
                    val res = aiService.generatePoetry(inputText, currentApiKey, currentModel)
                    isLoading = false
                    if (res.isSuccess) {
                        outputText = res.outputText
                        lastTokenCount = res.tokensUsed
                    } else {
                        statusError = res.error
                    }
                }
                AiTabMode.VOICE_POLISH -> {
                    val res = aiService.polishVoiceInput(inputText, currentApiKey, currentModel)
                    isLoading = false
                    if (res.isSuccess) {
                        outputText = res.outputText
                        lastTokenCount = res.tokensUsed
                    } else {
                        statusError = res.error
                    }
                }
                AiTabMode.CUSTOM_PROMPT -> {
                    val res = aiService.generateCustom(inputText, currentApiKey, currentModel)
                    isLoading = false
                    if (res.isSuccess) {
                        outputText = res.outputText
                        lastTokenCount = res.tokensUsed
                    } else {
                        statusError = res.error
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.keyboardBackground, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .border(1.dp, palette.keyBorderColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gemini AI Assistant",
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Token Count Badge
                    Box(
                        modifier = Modifier
                            .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚡ $totalTokensUsed tokens",
                            color = primaryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { showApiKeySection = !showApiKeySection },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "API Key",
                            tint = if (currentApiKey.isNotBlank()) primaryColor else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Model Selection Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Select Model:",
                    color = palette.secondaryTextColor,
                    fontSize = 12.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val models = listOf(
                        "gemini-3.5-flash" to "3.5 Flash ⚡",
                        "gemini-3.1-pro-preview" to "3.1 Pro 🧠",
                        "gemini-3.1-flash-lite-preview" to "Flash Lite 🚀"
                    )
                    models.forEach { (mCode, label) ->
                        val isSel = currentModel == mCode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) primaryColor else bgCard)
                                .clickable {
                                    currentModel = mCode
                                    preferences?.let { prefs ->
                                        coroutineScope.launch { prefs.updateGeminiModel(mCode) }
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.White else textColor,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // API Key Expandable Banner
            AnimatedVisibility(visible = showApiKeySection) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.10f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🔑 Enter Gemini Free Tier API Key",
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent)
                                }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Get Free Key", fontSize = 11.sp, color = primaryColor)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp), tint = primaryColor)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = keyInputText,
                            onValueChange = { keyInputText = it },
                            placeholder = { Text("Paste AI Studio API Key (AIzaSy...)", fontSize = 11.sp, color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = palette.keyBorderColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                currentApiKey = keyInputText.trim()
                                preferences?.let { prefs ->
                                    coroutineScope.launch { prefs.updateGeminiApiKey(currentApiKey) }
                                }
                                showApiKeySection = false
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("Save & Activate Key", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI Feature Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tabs = listOf(
                    AiTabMode.PROOFREAD to "✨ লেখা ঠিক করো",
                    AiTabMode.REPLY_SUGGEST to "💬 রিপ্লাই আইডিয়া",
                    AiTabMode.POETRY to "📜 কবিতা ও ছন্দ",
                    AiTabMode.VOICE_POLISH to "🎙️ ভয়েস পলিশ",
                    AiTabMode.CUSTOM_PROMPT to "✍️ নিজের মতো লেখো"
                )

                tabs.forEach { (mode, label) ->
                    val isSel = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) primaryColor else bgCard)
                            .border(1.dp, if (isSel) primaryColor else palette.keyBorderColor, RoundedCornerShape(20.dp))
                            .clickable {
                                selectedMode = mode
                                statusError = null
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) Color.White else textColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text Input Box
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    val hint = when (selectedMode) {
                        AiTabMode.PROOFREAD -> "এখানে ভুল বা বানান ঠিক করার টেক্সট লিখুন..."
                        AiTabMode.REPLY_SUGGEST -> "কারো মেসেজ বা কথা লিখুন যার রিপ্লাই চান..."
                        AiTabMode.POETRY -> "কবিতা বা ছন্দের বিষয় লিখুন (যেমন: বৃষ্টি, ভালোবাসা, বন্ধুত্ব)..."
                        AiTabMode.VOICE_POLISH -> "ভয়েস টাইপিংয়ের অগোছালো লেখা লিখুন..."
                        AiTabMode.CUSTOM_PROMPT -> "এআই কে কী করতে বলতে চান লিখুন..."
                    }
                    Text(hint, fontSize = 12.sp, color = Color.Gray)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = palette.keyBorderColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = primaryColor, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gemini Processing...", fontSize = 11.sp, color = palette.secondaryTextColor)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = { runAiFeature() },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI জেনারেট করুন", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Error Display
            if (statusError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "❌ $statusError",
                    color = Color(0xFFFF5252),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Output Display: Reply Suggestions List
            if (replySuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("💬 রিপ্লাই অপশনসমূহ (ইনসার্ট করতে ট্যাপ করুন):", fontSize = 11.sp, color = palette.secondaryTextColor, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                replySuggestions.forEach { rep ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                onInsertText(rep)
                                onClose()
                            },
                        colors = CardDefaults.cardColors(containerColor = bgCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.keyBorderColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(rep, color = textColor, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Send, contentDescription = "Insert", tint = primaryColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Output Display: Generated Text
            if (outputText.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = bgCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨ AI Result (${lastTokenCount} tokens):",
                                fontSize = 11.sp,
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )

                            Row {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(outputText))
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = textColor, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = outputText,
                            color = textColor,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                onInsertText(outputText)
                                onClose()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("এক ক্লিকে ইনসার্ট করুন", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
