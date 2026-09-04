package com.example.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.theme.KeyboardPalette

data class LanguageOption(
    val id: String,
    val title: String,
    val flag: String,
    val subtitle: String
)

val AVAILABLE_LANGUAGES = listOf(
    LanguageOption("english", "English", "🇬🇧", "QWERTY"),
    LanguageOption("bangla", "বাংলা", "🇧🇩", "জাতীয় (National)"),
    LanguageOption("avro", "Avro", "🅰️", "Phonetic (অভ্র)")
)

@Composable
fun LanguageSideSlider(
    currentLanguage: String,
    palette: KeyboardPalette,
    onSelectLanguage: (String) -> Unit,
    onDismiss: () -> Unit,
    onFeedback: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val initialIdx = AVAILABLE_LANGUAGES.indexOfFirst { it.id.equals(currentLanguage, ignoreCase = true) }
        .coerceAtLeast(0)

    var selectedIndex by remember { mutableIntStateOf(initialIdx) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val threshold = 40f // drag sensitivity in px

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("language_side_slider"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            palette.keyBackground,
                            palette.keyboardBackground
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = palette.accentColor.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp)
                )
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(20.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Title & Instructions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Side Slider",
                        tint = palette.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Slide to Switch Language",
                        color = palette.textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Active Language Chip
                val activeOpt = AVAILABLE_LANGUAGES[selectedIndex]
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.accentColor.copy(alpha = 0.2f))
                        .border(0.8.dp, palette.accentColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${activeOpt.flag} ${activeOpt.title}",
                        color = palette.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Horizontal Segmented Slider Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.keyActionBackground.copy(alpha = 0.7f))
                    .border(0.5.dp, palette.keyBorderColor, RoundedCornerShape(14.dp))
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                onSelectLanguage(AVAILABLE_LANGUAGES[selectedIndex].id)
                                onDismiss()
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                dragAccumulator += dragAmount
                                if (dragAccumulator > threshold) {
                                    if (selectedIndex < AVAILABLE_LANGUAGES.size - 1) {
                                        selectedIndex++
                                        onFeedback()
                                    }
                                    dragAccumulator = 0f
                                } else if (dragAccumulator < -threshold) {
                                    if (selectedIndex > 0) {
                                        selectedIndex--
                                        onFeedback()
                                    }
                                    dragAccumulator = 0f
                                }
                            }
                        )
                    }
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AVAILABLE_LANGUAGES.forEachIndexed { index, option ->
                    val isSelected = index == selectedIndex

                    val bg = if (isSelected) palette.accentColor else Color.Transparent
                    val textColor = if (isSelected) palette.onAccentColor else palette.textColor

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .clickable {
                                selectedIndex = index
                                onFeedback()
                                onSelectLanguage(option.id)
                                onDismiss()
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = option.flag, fontSize = 14.sp)
                            Text(
                                text = option.title,
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Subtitle Description
            val currentOpt = AVAILABLE_LANGUAGES[selectedIndex]
            Text(
                text = "${currentOpt.flag} ${currentOpt.title} • ${currentOpt.subtitle}",
                color = palette.secondaryTextColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
