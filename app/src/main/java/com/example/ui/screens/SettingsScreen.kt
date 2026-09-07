package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.ModeNight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiModelConstants
import com.example.data.db.UserAccountEntity
import com.example.data.repository.AccentColor
import com.example.data.repository.ChatDensity
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.ThemeMode
import com.example.ui.components.NovaLogo
import com.example.ui.theme.LocalNovaTokens
import com.example.ui.theme.NovaAccents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesRepository: PreferencesRepository,
    currentUser: UserAccountEntity?,
    onOpenApiDialog: () -> Unit,
    onOpenAuthDialog: () -> Unit,
    onClearAllConversations: () -> Unit,
    onBack: () -> Unit
) {
    val tokens = LocalNovaTokens.current

    var currentThemeMode by remember { mutableStateOf(preferencesRepository.getThemeMode()) }
    var currentAccent by remember { mutableStateOf(preferencesRepository.getAccentColor()) }
    var currentDensity by remember { mutableStateOf(preferencesRepository.getChatDensity()) }
    var currentDefaultModel by remember { mutableStateOf(preferencesRepository.getDefaultModel()) }
    var customInstructions by remember { mutableStateOf(preferencesRepository.getCustomInstructions()) }

    var isEnterToSend by remember { mutableStateOf(preferencesRepository.isEnterToSend()) }
    var isAutoTitle by remember { mutableStateOf(preferencesRepository.isAutoTitle()) }
    var isShowTimestamps by remember { mutableStateOf(preferencesRepository.isShowTimestamps()) }
    var isStreaming by remember { mutableStateOf(preferencesRepository.isStreamingEnabled()) }
    var isSyntaxHighlighting by remember { mutableStateOf(preferencesRepository.isSyntaxHighlighting()) }

    var isModelMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back", tint = tokens.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface)
            )
        },
        containerColor = tokens.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Appearance Section
            SettingsSectionCard(title = "Appearance", icon = Icons.Rounded.ColorLens) {
                Text(text = "Theme Mode", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = tokens.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeModeButton(
                        title = "System",
                        isSelected = currentThemeMode == ThemeMode.SYSTEM,
                        onClick = {
                            currentThemeMode = ThemeMode.SYSTEM
                            preferencesRepository.setThemeMode(ThemeMode.SYSTEM)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeModeButton(
                        title = "Dark",
                        isSelected = currentThemeMode == ThemeMode.DARK,
                        onClick = {
                            currentThemeMode = ThemeMode.DARK
                            preferencesRepository.setThemeMode(ThemeMode.DARK)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeModeButton(
                        title = "Light",
                        isSelected = currentThemeMode == ThemeMode.LIGHT,
                        onClick = {
                            currentThemeMode = ThemeMode.LIGHT
                            preferencesRepository.setThemeMode(ThemeMode.LIGHT)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Accent Color", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = tokens.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AccentColorDot(color = NovaAccents.PurplePrimary, isSelected = currentAccent == AccentColor.PURPLE) {
                        currentAccent = AccentColor.PURPLE
                        preferencesRepository.setAccentColor(AccentColor.PURPLE)
                    }
                    AccentColorDot(color = NovaAccents.BluePrimary, isSelected = currentAccent == AccentColor.BLUE) {
                        currentAccent = AccentColor.BLUE
                        preferencesRepository.setAccentColor(AccentColor.BLUE)
                    }
                    AccentColorDot(color = NovaAccents.PinkPrimary, isSelected = currentAccent == AccentColor.PINK) {
                        currentAccent = AccentColor.PINK
                        preferencesRepository.setAccentColor(AccentColor.PINK)
                    }
                    AccentColorDot(color = NovaAccents.GreenPrimary, isSelected = currentAccent == AccentColor.GREEN) {
                        currentAccent = AccentColor.GREEN
                        preferencesRepository.setAccentColor(AccentColor.GREEN)
                    }
                    AccentColorDot(color = NovaAccents.OrangePrimary, isSelected = currentAccent == AccentColor.ORANGE) {
                        currentAccent = AccentColor.ORANGE
                        preferencesRepository.setAccentColor(AccentColor.ORANGE)
                    }
                    AccentColorDot(color = NovaAccents.CyanPrimary, isSelected = currentAccent == AccentColor.CYAN) {
                        currentAccent = AccentColor.CYAN
                        preferencesRepository.setAccentColor(AccentColor.CYAN)
                    }
                }
            }

            // 2. Gemini API Section
            SettingsSectionCard(title = "Gemini API Connection", icon = Icons.Rounded.Key) {
                Text(
                    text = "Configure your own Gemini API key for private, high-speed usage.",
                    fontSize = 12.sp,
                    color = tokens.textSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpenApiDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary)
                ) {
                    Icon(imageVector = Icons.Rounded.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("API Configuration & Status", fontSize = 13.sp)
                }
            }

            // 3. Model & AI Behavior
            SettingsSectionCard(title = "Model & Behavior", icon = Icons.Rounded.Psychology) {
                Text(text = "Default Model", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = tokens.textPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                val selectedModelInfo = GeminiModelConstants.AVAILABLE_MODELS.find { it.id == currentDefaultModel }
                    ?: GeminiModelConstants.AVAILABLE_MODELS.first()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.surfaceElevated)
                        .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                        .clickable { isModelMenuExpanded = true }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = selectedModelInfo.displayName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = tokens.textPrimary)
                            Text(text = selectedModelInfo.tag, fontSize = 11.sp, color = tokens.primary)
                        }
                        Text(text = "▼", fontSize = 12.sp, color = tokens.textMuted)
                    }

                    DropdownMenu(
                        expanded = isModelMenuExpanded,
                        onDismissRequest = { isModelMenuExpanded = false }
                    ) {
                        for (model in GeminiModelConstants.AVAILABLE_MODELS) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = model.displayName, fontWeight = FontWeight.SemiBold)
                                        Text(text = model.tag, fontSize = 11.sp, color = tokens.textSecondary)
                                    }
                                },
                                onClick = {
                                    currentDefaultModel = model.id
                                    preferencesRepository.setDefaultModel(model.id)
                                    isModelMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Custom Instructions (System Prompt)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = tokens.textPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = customInstructions,
                    onValueChange = {
                        customInstructions = it
                        preferencesRepository.setCustomInstructions(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., You are an expert Android Kotlin software engineer...", fontSize = 13.sp, color = tokens.textMuted) },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tokens.primary,
                        unfocusedBorderColor = tokens.border
                    )
                )
            }

            // 4. Chat Experience
            SettingsSectionCard(title = "Chat Preferences", icon = Icons.Rounded.Tune) {
                SettingSwitchRow(
                    title = "Real-Time Streaming",
                    subtitle = "Stream responses word-by-word as Gemini generates them",
                    isChecked = isStreaming,
                    onCheckedChange = {
                        isStreaming = it
                        preferencesRepository.setStreamingEnabled(it)
                    }
                )
                HorizontalDivider(color = tokens.border.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                SettingSwitchRow(
                    title = "Show Timestamps",
                    subtitle = "Display message timestamps in chat bubbles",
                    isChecked = isShowTimestamps,
                    onCheckedChange = {
                        isShowTimestamps = it
                        preferencesRepository.setShowTimestamps(it)
                    }
                )
                HorizontalDivider(color = tokens.border.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                SettingSwitchRow(
                    title = "Auto-Title Chats",
                    subtitle = "Automatically name new chats from the first question",
                    isChecked = isAutoTitle,
                    onCheckedChange = {
                        isAutoTitle = it
                        preferencesRepository.setAutoTitle(it)
                    }
                )
                HorizontalDivider(color = tokens.border.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                SettingSwitchRow(
                    title = "Syntax Highlighting",
                    subtitle = "Render formatted code blocks with 1-tap copy button",
                    isChecked = isSyntaxHighlighting,
                    onCheckedChange = {
                        isSyntaxHighlighting = it
                        preferencesRepository.setSyntaxHighlighting(it)
                    }
                )
            }

            // 5. Account & Privacy
            SettingsSectionCard(title = "Account & Data", icon = Icons.Rounded.Person) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.surfaceElevated)
                        .clickable(onClick = onOpenAuthDialog)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentUser?.name ?: "Guest Explorer",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = currentUser?.email ?: "Tap to sign in / manage account",
                            fontSize = 12.sp,
                            color = tokens.textSecondary
                        )
                    }
                    Text(text = "Manage", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = tokens.primary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x50EF4444))
                ) {
                    Icon(imageVector = Icons.Rounded.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All Chats & History", fontSize = 13.sp)
                }
            }

            // 6. About Section
            SettingsSectionCard(title = "About", icon = Icons.Rounded.Info) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NovaLogo(size = 40.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "NOVA AI Mobile", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = tokens.textPrimary)
                        Text(text = "Version 1.0.0 • \"Think. Create. Discover.\"", fontSize = 12.sp, color = tokens.textSecondary)
                        Text(text = "Powered by Google Gemini REST Engine", fontSize = 11.sp, color = tokens.primary)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "All Conversations",
            onConfirm = {
                onClearAllConversations()
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val tokens = LocalNovaTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(tokens.surface)
            .border(1.dp, tokens.border, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = tokens.textPrimary)
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun ThemeModeButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalNovaTokens.current
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) tokens.primary else tokens.surfaceElevated)
            .border(1.dp, if (isSelected) tokens.primary else tokens.border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else tokens.textPrimary
        )
    }
}

@Composable
private fun AccentColorDot(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokens = LocalNovaTokens.current
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                2.dp,
                if (isSelected) (if (tokens.isDark) Color.White else Color.Black) else Color.Transparent,
                CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val tokens = LocalNovaTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = tokens.textPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = tokens.textSecondary)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = tokens.primary
            )
        )
    }
}
