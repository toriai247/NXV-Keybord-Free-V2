package com.example.ui.keyboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sticker.StickerItem
import com.example.sticker.StickerManager
import com.example.sticker.StickerPack
import com.example.theme.KeyboardPalette

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StickerKeyboardLayout(
    palette: KeyboardPalette,
    onStickerSelected: (StickerItem) -> Unit,
    onSwitchToEmoji: () -> Unit,
    onBackspace: () -> Unit,
    onCloseStickers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stickerManager = remember { StickerManager.getInstance(context) }
    val packs by stickerManager.stickerPacks.collectAsState()
    val recents by stickerManager.recentStickers.collectAsState()

    var selectedPackId by remember { mutableStateOf<String>("samu") }
    var previewSticker by remember { mutableStateOf<StickerItem?>(null) }

    LaunchedEffect(packs) {
        if (packs.isNotEmpty() && selectedPackId.isBlank()) {
            selectedPackId = packs.first().id
        }
    }

    val currentStickers: List<StickerItem> = remember(selectedPackId, packs, recents) {
        if (selectedPackId == "recents") {
            recents
        } else {
            packs.find { it.id == selectedPackId }?.stickers ?: emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(palette.keyboardBackground)
    ) {
        // Top Pack Tabs Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(palette.suggestionBarBackground)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recents tab
            if (recents.isNotEmpty()) {
                val isRecentsSelected = selectedPackId == "recents"
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isRecentsSelected) palette.accentColor.copy(alpha = 0.25f)
                            else palette.keyBackground.copy(alpha = 0.35f)
                        )
                        .clickable { selectedPackId = "recents" }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("sticker_tab_recents"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Recent Stickers",
                            tint = if (isRecentsSelected) palette.accentColor else palette.textColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Recent",
                            color = if (isRecentsSelected) palette.accentColor else palette.textColor,
                            fontSize = 12.sp,
                            fontWeight = if (isRecentsSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Category Pack Tabs
            packs.forEach { pack ->
                val isSelected = selectedPackId == pack.id
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) palette.accentColor.copy(alpha = 0.25f)
                            else palette.keyBackground.copy(alpha = 0.35f)
                        )
                        .clickable { selectedPackId = pack.id }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("sticker_tab_${pack.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data("file:///android_asset/${pack.previewAssetPath}")
                                .crossfade(true)
                                .build(),
                            contentDescription = pack.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = pack.name,
                            color = if (isSelected) palette.accentColor else palette.textColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Stickers Grid Body
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            if (currentStickers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedPackId == "recents") "No recent stickers yet" else "Loading stickers...",
                        color = palette.secondaryTextColor,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 64.dp),
                    contentPadding = PaddingValues(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(currentStickers, key = { it.assetPath }) { sticker ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.keyBackground.copy(alpha = 0.2f))
                                .combinedClickable(
                                    onClick = { onStickerSelected(sticker) },
                                    onLongClick = { previewSticker = sticker }
                                )
                                .padding(4.dp)
                                .testTag("sticker_item_${sticker.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data("file:///android_asset/${sticker.assetPath}")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = sticker.displayName,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Bottom Bar (ABC switch, Emoji/Sticker toggle, Backspace)
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
                // ABC button to return to text keyboard
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.keyActionBackground)
                        .clickable { onCloseStickers() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("sticker_close_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ABC",
                        color = palette.textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Switch to Emojis
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.keyBackground.copy(alpha = 0.4f))
                        .clickable { onSwitchToEmoji() }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("sticker_to_emoji_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "😊 Emoji",
                        color = palette.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
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

    // Zoomed Preview Dialog on Long Press
    if (previewSticker != null) {
        val st = previewSticker!!
        Dialog(onDismissRequest = { previewSticker = null }) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.keyboardBackground)
                    .border(1.dp, palette.accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("file:///android_asset/${st.assetPath}")
                            .crossfade(true)
                            .build(),
                        contentDescription = st.displayName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(130.dp)
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.accentColor)
                            .clickable {
                                onStickerSelected(st)
                                previewSticker = null
                            }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Sticker",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Send",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
