package com.example.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ClipboardItem
import com.example.theme.KeyboardPalette
import com.example.util.ClipboardUtils
import kotlinx.coroutines.delay

enum class ClipboardTab {
    ALL,
    PINNED
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClipboardKeyboardLayout(
    items: List<ClipboardItem>,
    palette: KeyboardPalette,
    onPasteItem: (text: String, returnToKeyboard: Boolean) -> Unit,
    onTogglePin: (ClipboardItem) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearAll: () -> Unit,
    onSyncClipboard: () -> Unit = {},
    onCloseClipboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ClipboardTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var autoCloseOnPaste by remember { mutableStateOf(true) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var recentlyPastedId by remember { mutableStateOf<Long?>(null) }
    var selectedItemForAction by remember { mutableStateOf<ClipboardItem?>(null) }

    // Automatically clear recently pasted visual feedback after 1.2s
    LaunchedEffect(recentlyPastedId) {
        if (recentlyPastedId != null) {
            delay(1200)
            recentlyPastedId = null
        }
    }

    val pinnedCount = items.count { it.isPinned }
    val filteredItems = items.filter { item ->
        val matchesTab = when (selectedTab) {
            ClipboardTab.ALL -> true
            ClipboardTab.PINNED -> item.isPinned
        }
        val matchesSearch = searchQuery.isBlank() ||
                item.text.contains(searchQuery.trim(), ignoreCase = true)
        matchesTab && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(palette.keyboardBackground)
    ) {
        // Top Tab Navigation Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(palette.suggestionBarBackground)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Back to typing tab + Mode tabs
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Tab: Back to Keyboard
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.keyBackground.copy(alpha = 0.8f))
                        .clickable { onCloseClipboard() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("clipboard_tab_keyboard"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Typing",
                            tint = palette.textColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Typing",
                            color = palette.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Tab: All Clips
                val isAllSelected = selectedTab == ClipboardTab.ALL
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isAllSelected) palette.accentColor.copy(alpha = 0.22f)
                            else Color.Transparent
                        )
                        .clickable {
                            selectedTab = ClipboardTab.ALL
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("clipboard_tab_all"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "All Clips",
                            tint = if (isAllSelected) palette.accentColor else palette.secondaryTextColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "All",
                            color = if (isAllSelected) palette.accentColor else palette.secondaryTextColor,
                            fontSize = 11.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        if (items.isNotEmpty()) {
                            Text(
                                text = "(${items.size})",
                                color = if (isAllSelected) palette.accentColor else palette.secondaryTextColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Tab: Pinned Clips
                val isPinnedSelected = selectedTab == ClipboardTab.PINNED
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isPinnedSelected) palette.accentColor.copy(alpha = 0.22f)
                            else Color.Transparent
                        )
                        .clickable {
                            selectedTab = ClipboardTab.PINNED
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("clipboard_tab_pinned"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned Clips",
                            tint = if (isPinnedSelected) palette.accentColor else palette.secondaryTextColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Pinned",
                            color = if (isPinnedSelected) palette.accentColor else palette.secondaryTextColor,
                            fontSize = 11.sp,
                            fontWeight = if (isPinnedSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        if (pinnedCount > 0) {
                            Text(
                                text = "($pinnedCount)",
                                color = if (isPinnedSelected) palette.accentColor else palette.secondaryTextColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Right: Toolbar actions (Search, Sync, Clear, Close)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Search Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSearchActive) palette.accentColor.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) searchQuery = ""
                        }
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .testTag("clipboard_btn_search"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = palette.textColor,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Sync System Clipboard
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onSyncClipboard() }
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .testTag("clipboard_btn_sync"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync",
                        tint = palette.textColor,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Clear All (Unpinned)
                if (items.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showClearConfirmation = true }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .testTag("clipboard_clear_all"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear All",
                            tint = palette.textColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onCloseClipboard() }
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .testTag("clipboard_close_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = palette.textColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Search Bar (Visible when search is active)
        AnimatedVisibility(visible = isSearchActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(palette.keyBackground)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = palette.secondaryTextColor,
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(14.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search copied fragments...",
                            color = palette.secondaryTextColor,
                            fontSize = 12.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = palette.textColor,
                            fontSize = 12.sp
                        ),
                        cursorBrush = SolidColor(palette.accentColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clipboard_search_input")
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { searchQuery = "" }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = palette.secondaryTextColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // Sub-bar: Status and Auto-Close Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Status label
            Text(
                text = when {
                    searchQuery.isNotEmpty() -> "${filteredItems.size} match${if (filteredItems.size == 1) "" else "es"}"
                    selectedTab == ClipboardTab.PINNED -> "$pinnedCount pinned fragment${if (pinnedCount == 1) "" else "s"}"
                    else -> "${items.size} copied fragment${if (items.size == 1) "" else "s"}"
                },
                fontSize = 10.sp,
                color = palette.secondaryTextColor,
                fontWeight = FontWeight.Medium
            )

            // Auto-close toggle pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (autoCloseOnPaste) palette.accentColor.copy(alpha = 0.14f)
                        else palette.keyBackground
                    )
                    .clickable { autoCloseOnPaste = !autoCloseOnPaste }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                    .testTag("clipboard_toggle_autoclose")
            ) {
                Icon(
                    imageVector = if (autoCloseOnPaste) Icons.Default.Bolt else Icons.Default.ContentPaste,
                    contentDescription = null,
                    tint = if (autoCloseOnPaste) palette.accentColor else palette.secondaryTextColor,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = if (autoCloseOnPaste) "Return after paste: ON" else "Multi-paste mode: ON",
                    fontSize = 10.sp,
                    color = if (autoCloseOnPaste) palette.accentColor else palette.secondaryTextColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Long-press detail/action card popup
        selectedItemForAction?.let { targetItem ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = palette.keyBackground),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected Fragment Options",
                            fontSize = 11.sp,
                            color = palette.accentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ClipboardUtils.formatRelativeTime(targetItem.timestamp),
                            fontSize = 10.sp,
                            color = palette.secondaryTextColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = targetItem.text,
                        fontSize = 12.sp,
                        color = palette.textColor,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { selectedItemForAction = null },
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("Cancel", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                onTogglePin(targetItem)
                                selectedItemForAction = null
                            },
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.accentColor),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(if (targetItem.isPinned) "Unpin" else "Pin", fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                onDeleteItem(targetItem.id)
                                selectedItemForAction = null
                            },
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text("Delete", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // List of Copied Fragments
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    val emptyIcon = if (searchQuery.isNotEmpty()) Icons.Default.Search
                    else if (selectedTab == ClipboardTab.PINNED) Icons.Default.PushPin
                    else Icons.Default.ContentPaste

                    Icon(
                        imageVector = emptyIcon,
                        contentDescription = null,
                        tint = palette.secondaryTextColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matching clips"
                        else if (selectedTab == ClipboardTab.PINNED) "No pinned fragments"
                        else "Clipboard is empty",
                        color = palette.textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try a different search term"
                        else if (selectedTab == ClipboardTab.PINNED) "Pin important fragments to keep them permanently at the top."
                        else "Text you copy anywhere on your device is automatically captured here for 1-tap pasting.",
                        color = palette.secondaryTextColor,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    if (items.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.accentColor.copy(alpha = 0.15f))
                                .clickable { onSyncClipboard() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("clipboard_sync_empty_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = palette.accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Check System Clipboard",
                                    color = palette.accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val isRecentlyPasted = recentlyPastedId == item.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .combinedClickable(
                                onClick = {
                                    recentlyPastedId = item.id
                                    onPasteItem(item.text, autoCloseOnPaste)
                                },
                                onLongClick = {
                                    selectedItemForAction = item
                                }
                            )
                            .testTag("clipboard_item_${item.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isPinned) palette.accentColor.copy(alpha = 0.12f)
                            else palette.keyBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = if (item.isPinned) {
                            androidx.compose.foundation.BorderStroke(1.dp, palette.accentColor.copy(alpha = 0.4f))
                        } else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            // Header row: metadata + pin + delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (item.isPinned) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(palette.accentColor)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "PINNED",
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Text(
                                        text = ClipboardUtils.formatRelativeTime(item.timestamp),
                                        color = palette.secondaryTextColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = "•",
                                        color = palette.secondaryTextColor.copy(alpha = 0.5f),
                                        fontSize = 10.sp
                                    )

                                    Text(
                                        text = ClipboardUtils.getWordAndCharCount(item.text),
                                        color = palette.secondaryTextColor,
                                        fontSize = 10.sp
                                    )
                                }

                                // Quick actions: Pin & Delete
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    // Pin toggle button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { onTogglePin(item) }
                                            .padding(4.dp)
                                            .testTag("clipboard_pin_${item.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = if (item.isPinned) "Unpin" else "Pin",
                                            tint = if (item.isPinned) palette.accentColor else palette.secondaryTextColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }

                                    // Delete button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { onDeleteItem(item.id) }
                                            .padding(4.dp)
                                            .testTag("clipboard_delete_${item.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = palette.secondaryTextColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Text preview
                            Text(
                                text = item.text,
                                color = palette.textColor,
                                fontSize = 13.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick Paste Action Bar on Card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (isRecentlyPasted) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Pasted into field!",
                                            color = Color(0xFF4CAF50),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Tap to paste",
                                        color = palette.secondaryTextColor.copy(alpha = 0.7f),
                                        fontSize = 10.sp
                                    )
                                }

                                // Explicit Quick Paste Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(palette.accentColor.copy(alpha = 0.18f))
                                        .clickable {
                                            recentlyPastedId = item.id
                                            onPasteItem(item.text, autoCloseOnPaste)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                        .testTag("clipboard_paste_btn_${item.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = null,
                                            tint = palette.accentColor,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "Paste",
                                            color = palette.accentColor,
                                            fontSize = 11.sp,
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
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear Clipboard History?") },
            text = {
                Text(
                    if (pinnedCount > 0)
                        "This will delete all unpinned copied fragments. Your $pinnedCount pinned fragments will be safely preserved."
                    else
                        "This will delete all copied text fragments from your device storage."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Clear", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
