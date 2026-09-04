package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.theme.KeyboardPalette

@Composable
fun LanguageSelectorDialog(
    activeLanguages: Set<String>,
    currentLanguage: String,
    palette: KeyboardPalette,
    onSelectLanguage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // English, Bangla, and Avro phonetic input modes
    val languages = listOf(
        Triple("english", "English (US)", "Standard Latin QWERTY layout"),
        Triple("bangla", "বাংলা (Bangla)", "Traditional national Bengali script layout"),
        Triple("avro", "Avro Phonetic (অভ্র)", "Type English phonetics to output Bengali")
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(palette.keyBackground)
                .padding(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Input Language & Mode",
                        color = palette.textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = palette.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                languages.forEach { (id, label, description) ->
                    val isSelected = id == currentLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) palette.accentColor.copy(alpha = 0.18f)
                                else palette.keyboardBackground
                            )
                            .clickable {
                                onSelectLanguage(id)
                                onDismiss()
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("lang_opt_$id"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                color = if (isSelected) palette.accentColor else palette.textColor,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                            Text(
                                text = description,
                                color = palette.secondaryTextColor,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(palette.accentColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = palette.onAccentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
