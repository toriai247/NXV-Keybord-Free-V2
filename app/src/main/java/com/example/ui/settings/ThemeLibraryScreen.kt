package com.example.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.KeyboardMode
import com.example.keyboard.ShiftState
import com.example.theme.KeyboardPalette
import com.example.theme.KeyboardThemes
import com.example.theme.ThemeSpecialIconStyle
import com.example.ui.keyboard.QwertyKeyLayout
import com.example.ui.keyboard.ReferenceOriginalKeyboardLayout
import com.example.ui.keyboard.ReferenceOriginalToolbar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ThemeLibraryScreen(
    currentThemeId: String,
    currentUiMode: String = "original",
    onSelectTheme: (String) -> Unit,
    onUpdateUiMode: (String) -> Unit = {},
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var testInputText by remember { mutableStateOf("") }
    var testShiftState by remember { mutableStateOf(ShiftState.LOWERCASE) }

    val categories = listOf("All", "Featured & 3D", "Dark & AMOLED", "Light & Clean", "Aesthetic")

    val allThemes = KeyboardThemes.ALL_THEMES
    val filteredThemes = remember(selectedCategory) {
        when (selectedCategory) {
            "Featured & 3D" -> allThemes.filter { it.themeId in listOf("reference_minimal", "retro_mech", "puppy_pop", "strawberry_dessert", "kawaii_kitten") }
            "Dark & AMOLED" -> allThemes.filter { it.themeId in listOf("retro_mech", "geometric", "amoled", "custom") }
            "Light & Clean" -> allThemes.filter { it.themeId in listOf("reference_minimal", "puppy_pop", "light", "strawberry_dessert", "kawaii_kitten") }
            "Aesthetic" -> allThemes.filter { it.themeId in listOf("reference_minimal", "retro_mech", "puppy_pop", "strawberry_dessert", "custom", "kawaii_kitten") }
            else -> allThemes
        }
    }

    val activePalette = KeyboardThemes.getPalette(currentThemeId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Theme Library",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Personalize your keyboard styling & artwork",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("theme_library_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            leadingIcon = if (cat == "Featured & 3D") {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Theme Cards
            items(filteredThemes, key = { it.themeId }) { palette ->
                val isSelected = palette.themeId.equals(currentThemeId, ignoreCase = true)

                ThemeCardItem(
                    palette = palette,
                    isSelected = isSelected,
                    onSelect = {
                        onSelectTheme(palette.themeId)
                    }
                )
            }

            // Live Interactive Typing Playground
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Interactive Theme Tester",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Test keys, popup previews, and character feedback live with the applied '${activePalette.themeName}' theme:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        // Dual-UI Mode Selector for Reference Minimal Theme
                        if (activePalette.themeId == "reference_minimal") {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "UI Layout Mode",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Choose between the authentic Reference 10-column layout or the standard NXV layout.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = currentUiMode == "original",
                                            onClick = { onUpdateUiMode("original") },
                                            label = { Text("1. Original UI (Reference)") },
                                            modifier = Modifier.weight(1f).testTag("ui_mode_original_chip")
                                        )
                                        FilterChip(
                                            selected = currentUiMode == "nxv",
                                            onClick = { onUpdateUiMode("nxv") },
                                            label = { Text("2. NXV UI (Adaptive)") },
                                            modifier = Modifier.weight(1f).testTag("ui_mode_nxv_chip")
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = testInputText,
                            onValueChange = { testInputText = it },
                            placeholder = { Text("Tap keys below to type here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("theme_tester_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Mini Keyboard Layout
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(activePalette.keyboardBackground),
                            color = activePalette.keyboardBackground
                        ) {
                            if (activePalette.themeId == "reference_minimal" && currentUiMode == "original") {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    ReferenceOriginalToolbar(
                                        suggestions = emptyList(),
                                        palette = activePalette,
                                        onSuggestionClick = {},
                                        onGridClick = {},
                                        onEmojiClick = { testInputText += "😊" },
                                        onClipboardClick = {},
                                        onTextEditClick = {},
                                        onSearchClick = { testInputText += "\n" }
                                    )
                                    ReferenceOriginalKeyboardLayout(
                                        isAvro = false,
                                        isBangla = false,
                                        shiftState = testShiftState,
                                        keyHeight = 38.dp,
                                        palette = activePalette,
                                        enterLabel = "↵",
                                        onCharTyped = { char ->
                                            testInputText += char
                                            if (testShiftState == ShiftState.SHIFT_ONCE) {
                                                testShiftState = ShiftState.LOWERCASE
                                            }
                                        },
                                        onDelete = {
                                            if (testInputText.isNotEmpty()) {
                                                testInputText = testInputText.dropLast(1)
                                            }
                                        },
                                        onSpace = {
                                            testInputText += " "
                                        },
                                        onEnter = {
                                            testInputText += "\n"
                                        },
                                        onShift = {
                                            testShiftState = when (testShiftState) {
                                                ShiftState.LOWERCASE -> ShiftState.SHIFT_ONCE
                                                ShiftState.SHIFT_ONCE -> ShiftState.CAPS_LOCK
                                                ShiftState.CAPS_LOCK, ShiftState.MANUAL_UPPERCASE -> ShiftState.LOWERCASE
                                            }
                                        },
                                        onSwitchMode = {},
                                        onLanguageCycle = {},
                                        onLongPressLanguage = {}
                                    )
                                }
                            } else {
                                QwertyKeyLayout(
                                    isAvro = false,
                                    shiftState = testShiftState,
                                    showNumberRow = false,
                                    keyHeight = 38.dp,
                                    palette = activePalette,
                                    enterLabel = "↵",
                                    onCharTyped = { char ->
                                        testInputText += char
                                        if (testShiftState == ShiftState.SHIFT_ONCE) {
                                            testShiftState = ShiftState.LOWERCASE
                                        }
                                    },
                                    onDelete = {
                                        if (testInputText.isNotEmpty()) {
                                            testInputText = testInputText.dropLast(1)
                                        }
                                    },
                                    onSpace = {
                                        testInputText += " "
                                    },
                                    onEnter = {
                                        testInputText += "\n"
                                    },
                                    onShift = {
                                        testShiftState = when (testShiftState) {
                                            ShiftState.LOWERCASE -> ShiftState.SHIFT_ONCE
                                            ShiftState.SHIFT_ONCE -> ShiftState.CAPS_LOCK
                                            ShiftState.CAPS_LOCK, ShiftState.MANUAL_UPPERCASE -> ShiftState.LOWERCASE
                                        }
                                    },
                                    onSwitchMode = {},
                                    onLanguageCycle = {},
                                    onLongPressLanguage = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeCardItem(
    palette: KeyboardPalette,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "border_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onSelect() }
            .testTag("theme_card_${palette.themeId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Miniature Keyboard Mockup Preview
            MiniKeyboardMockup(
                palette = palette,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Title and Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = palette.themeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (palette.themeId in listOf("puppy_pop", "strawberry_dessert")) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (palette.themeId == "puppy_pop") Color(0xFF1E88E5) else Color(0xFFE5395A)
                        ) {
                            Text(
                                text = "FEATURED",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Swatches
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    palette.previewColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(0.5.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = palette.themeDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = palette.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (isSelected) {
                    Button(
                        onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Applied", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onSelect,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Apply Theme")
                    }
                }
            }
        }
    }
}

/**
 * Renders an authentic miniature representation of the keyboard using the exact theme colors and styles.
 */
@Composable
fun MiniKeyboardMockup(
    palette: KeyboardPalette,
    modifier: Modifier = Modifier
) {
    val corner = RoundedCornerShape(palette.keyCornerRadius * 0.6f)
    val isStrawberry = palette.specialIconStyle == ThemeSpecialIconStyle.STRAWBERRY_DESSERT
    val isPuppy = palette.specialIconStyle == ThemeSpecialIconStyle.PUPPY_MINIMAL
    val isRgbNeon = palette.specialIconStyle == ThemeSpecialIconStyle.RGB_NEON

    Box(
        modifier = modifier
            .background(palette.keyboardBackground)
            .border(1.dp, palette.dividerColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Row 1: Q W E R T Y U I O P
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P").forEachIndexed { idx, char ->
                    MiniKey(
                        char = char,
                        modifier = Modifier.weight(1f),
                        palette = palette,
                        colIndex = idx,
                        totalCols = 10,
                        corner = corner
                    )
                }
            }

            // Row 2: A S D F G H J K L
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Spacer(modifier = Modifier.weight(0.4f))
                listOf("A", "S", "D", "F", "G", "H", "J", "K", "L").forEachIndexed { idx, char ->
                    MiniKey(
                        char = char,
                        modifier = Modifier.weight(1f),
                        palette = palette,
                        colIndex = idx,
                        totalCols = 9,
                        corner = corner
                    )
                }
                Spacer(modifier = Modifier.weight(0.4f))
            }

            // Row 3: [Shift] Z X C V B N M [Backspace]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniKey(
                    char = if (isStrawberry) "🍓" else "⇧",
                    modifier = Modifier.weight(1.4f),
                    palette = palette,
                    isAction = true,
                    colIndex = 0,
                    totalCols = 10,
                    corner = corner
                )

                listOf("Z", "X", "C", "V", "B", "N", "M").forEachIndexed { idx, char ->
                    MiniKey(
                        char = char,
                        modifier = Modifier.weight(1f),
                        palette = palette,
                        colIndex = idx + 1,
                        totalCols = 10,
                        corner = corner
                    )
                }

                MiniKey(
                    char = if (isStrawberry) "🧋" else "⌫",
                    modifier = Modifier.weight(1.4f),
                    palette = palette,
                    isAction = true,
                    colIndex = 9,
                    totalCols = 10,
                    corner = corner
                )
            }

            // Row 4: [123] [Globe] [ Space ] [Enter]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniKey(
                    char = "123",
                    modifier = Modifier.weight(1.2f),
                    palette = palette,
                    isAction = true,
                    colIndex = 0,
                    totalCols = 10,
                    corner = corner
                )

                MiniKey(
                    char = if (isStrawberry) "🍨" else "🌐",
                    modifier = Modifier.weight(1.0f),
                    palette = palette,
                    isAction = true,
                    colIndex = 2,
                    totalCols = 10,
                    corner = corner
                )

                // Spacebar
                val spaceBorderColor = if (isRgbNeon) Color(0xFFEF4444) else palette.keyBorderColor
                Box(
                    modifier = Modifier
                        .weight(4.0f)
                        .height(20.dp)
                        .clip(corner)
                        .background(palette.keyBackground)
                        .border(if (isRgbNeon) 1.5.dp else palette.keyBorderWidth * 0.8f, spaceBorderColor, corner),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPuppy) {
                        Text(
                            text = "nxv",
                            color = palette.secondaryTextColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (isRgbNeon) {
                        Text(
                            text = "space",
                            color = Color(0xFFEF4444),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                MiniKey(
                    char = if (isStrawberry) "🍰" else if (isRgbNeon) "return" else "↵",
                    modifier = Modifier.weight(1.5f),
                    palette = palette,
                    isAction = true,
                    isPrimary = true,
                    colIndex = 9,
                    totalCols = 10,
                    corner = corner
                )
            }
        }
    }
}

@Composable
private fun MiniKey(
    char: String,
    modifier: Modifier = Modifier,
    palette: KeyboardPalette,
    corner: RoundedCornerShape,
    colIndex: Int = -1,
    totalCols: Int = 10,
    isAction: Boolean = false,
    isPrimary: Boolean = false
) {
    val isRgbNeon = palette.specialIconStyle == ThemeSpecialIconStyle.RGB_NEON
    val rgbColor = if (isRgbNeon) com.example.theme.RgbSpectrumUtils.getColorForKey(char, colIndex, totalCols) else palette.textColor

    val bg = when {
        isRgbNeon -> palette.keyBackground
        isPrimary -> if (palette.specialIconStyle == ThemeSpecialIconStyle.PUPPY_MINIMAL) palette.keyActionBackground else palette.accentColor
        isAction -> palette.keyActionBackground
        else -> palette.keyBackground
    }
    val textCol = when {
        isRgbNeon -> rgbColor
        isPrimary && palette.specialIconStyle != ThemeSpecialIconStyle.PUPPY_MINIMAL -> palette.onAccentColor
        else -> palette.textColor
    }
    val borderCol = if (isRgbNeon) rgbColor else palette.keyBorderColor
    val borderWidth = if (isRgbNeon) 1.5.dp else palette.keyBorderWidth * 0.7f

    Box(
        modifier = modifier
            .height(20.dp)
            .clip(corner)
            .background(bg)
            .border(borderWidth, borderCol, corner),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            color = textCol,
            fontSize = if (char.length > 3) 6.sp else if (char.length > 2) 7.sp else 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
