package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emoji.EmojiData
import com.example.theme.KeyboardPalette

@Composable
fun EmojiKeyboardLayout(
    palette: KeyboardPalette,
    onEmojiSelected: (String) -> Unit,
    onBackspace: () -> Unit,
    onCloseEmoji: () -> Unit,
    onSwitchToStickers: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedCategoryId by remember { mutableStateOf(EmojiData.CATEGORIES.first().id) }
    var searchQuery by remember { mutableStateOf("") }

    val currentEmojis = remember(selectedCategoryId, searchQuery) {
        if (searchQuery.isNotBlank()) {
            EmojiData.search(searchQuery)
        } else {
            EmojiData.CATEGORIES.find { it.id == selectedCategoryId }?.emojis
                ?: EmojiData.CATEGORIES.first().emojis
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(palette.keyboardBackground)
    ) {
        // Category Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(palette.suggestionBarBackground)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmojiData.CATEGORIES.forEach { cat ->
                val isSelected = cat.id == selectedCategoryId && searchQuery.isBlank()
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) palette.accentColor.copy(alpha = 0.25f)
                            else palette.keyBackground.copy(alpha = 0.3f)
                        )
                        .clickable {
                            searchQuery = ""
                            selectedCategoryId = cat.id
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("emoji_cat_${cat.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat.icon,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Emoji Grid
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 42.dp),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(currentEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onEmojiSelected(emoji) }
                            .testTag("emoji_item_$emoji"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }

        // Bottom Bar (ABC switch, Stickers switch, Backspace)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(palette.suggestionBarBackground)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.keyActionBackground)
                        .clickable { onCloseEmoji() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("emoji_close_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ABC",
                        color = palette.textColor,
                        fontSize = 14.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                }

                if (onSwitchToStickers != null) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.keyBackground.copy(alpha = 0.4f))
                            .clickable { onSwitchToStickers() }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("emoji_to_stickers_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🐱 Stickers",
                            color = palette.textColor,
                            fontSize = 12.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                    }
                }
            }

            KeyboardKeyView(
                label = "⌫",
                modifier = Modifier.width(60.dp),
                isSpecialAction = true,
                isRepeatable = true,
                height = 36.dp,
                palette = palette,
                onTap = { onBackspace() }
            )
        }
    }
}
