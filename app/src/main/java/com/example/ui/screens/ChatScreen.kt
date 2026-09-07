package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbDownOffAlt
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.ThumbUpOffAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.VolumeOff
import coil.compose.AsyncImage
import com.example.data.api.GeminiModelConstants
import com.example.data.api.ResponseParser
import com.example.data.db.ConversationEntity
import com.example.data.db.MessageEntity
import com.example.ui.components.AiModeSelector
import com.example.ui.components.GroundingSourcesView
import com.example.ui.components.MarkdownText
import com.example.ui.components.NovaLogo
import com.example.ui.components.StatusPill
import com.example.ui.components.ThinkingProcessCard
import com.example.ui.theme.LocalNovaTokens
import com.example.ui.viewmodel.AttachmentData
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.FileHelper
import com.example.util.SpeechState
import com.example.util.TimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val tokens = LocalNovaTokens.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentConversation by viewModel.currentConversation.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val currentAttachment by viewModel.currentAttachment.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationStatus by viewModel.generationStatus.collectAsState()
    val apiConnectionState by viewModel.apiConnectionState.collectAsState()
    val speechState by viewModel.speechHelper.speechState.collectAsState()

    val listState = rememberLazyListState()
    var isTopMenuExpanded by remember { mutableStateOf(false) }
    var isModelMenuExpanded by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }

    // Media and File Pickers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.attachImage(it) }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.attachFile(it) }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.onAudioPermissionGranted()
        }
    }

    // Auto-scroll to bottom when new messages arrive or when generating
    LaunchedEffect(messages.size, messages.lastOrNull()?.content?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.clickable { viewModel.setShowModelSelector(true) }
                    ) {
                        Text(
                            text = currentConversation?.title ?: "NOVA AI",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val activeModelName = GeminiModelConstants.AVAILABLE_MODELS.find {
                                it.id == (currentConversation?.model ?: GeminiModelConstants.DEFAULT_MODEL)
                            }?.displayName ?: "Gemini 3.5 Flash"

                            Text(
                                text = "$activeModelName ▼",
                                fontSize = 11.sp,
                                color = tokens.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(imageVector = Icons.Rounded.Menu, contentDescription = "Open Drawer", tint = tokens.textPrimary)
                    }
                },
                actions = {
                    // Quick New Chat Action
                    IconButton(
                        onClick = { viewModel.startNewConversation() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddComment,
                            contentDescription = "New Chat",
                            tint = tokens.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = { viewModel.setShowVoiceAssistant(true) }) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Voice Assistant",
                            tint = tokens.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    StatusPill(
                        state = apiConnectionState,
                        onClick = { viewModel.setShowApiSettingsDialog(true) }
                    )

                    IconButton(onClick = { isTopMenuExpanded = true }) {
                        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "More", tint = tokens.textPrimary)
                    }

                    DropdownMenu(
                        expanded = isTopMenuExpanded,
                        onDismissRequest = { isTopMenuExpanded = false }
                    ) {
                        currentConversation?.let { conv ->
                            DropdownMenuItem(
                                text = { Text(if (conv.isFavorite) "Unfavorite" else "Favorite") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (conv.isFavorite) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    isTopMenuExpanded = false
                                    viewModel.toggleFavorite(conv)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Model Engine") },
                                leadingIcon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                                onClick = {
                                    isTopMenuExpanded = false
                                    viewModel.setShowModelSelector(true)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Rename Chat") },
                                leadingIcon = { Icon(Icons.Rounded.DriveFileRenameOutline, contentDescription = null) },
                                onClick = {
                                    isTopMenuExpanded = false
                                    viewModel.setConversationToRename(conv)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export & Share") },
                                leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null) },
                                onClick = {
                                    isTopMenuExpanded = false
                                    viewModel.setShowExportDialog(true)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            leadingIcon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                            onClick = {
                                isTopMenuExpanded = false
                                onOpenSettings()
                            }
                        )
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
                .imePadding()
        ) {
            // Pro AI Mode Selector Bar
            val activeAiMode by viewModel.currentAiMode.collectAsState()
            AiModeSelector(
                currentMode = activeAiMode,
                onSelectMode = { viewModel.setAiMode(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Main Chat Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    EmptyChatGreeting(
                        onSuggestionClick = { prompt ->
                            viewModel.sendMessage(prompt)
                        }
                    )
                } else {
                    val isTtsSpeaking by viewModel.ttsHelper.isSpeaking.collectAsState()

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatMessageRow(
                                message = msg,
                                isGenerating = isGenerating,
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("NOVA Message", msg.content)
                                    clipboard.setPrimaryClip(clip)
                                },
                                onRegenerate = { viewModel.regenerateResponse(msg) },
                                onContinue = { viewModel.continueResponse() },
                                onEdit = { viewModel.editUserMessage(msg) },
                                onDelete = { viewModel.deleteMessage(msg) },
                                onFeedback = { feedback -> viewModel.setMessageFeedback(msg, feedback) },
                                onShare = {
                                    FileHelper.shareText(context, "NOVA AI Response", msg.content)
                                },
                                onSpeak = { textToSpeak ->
                                    viewModel.readMessageAloud(textToSpeak)
                                },
                                isSpeaking = isTtsSpeaking,
                                onStopSpeak = {
                                    viewModel.stopTts()
                                }
                            )
                        }

                        // Thinking Indicator when generating
                        if (isGenerating) {
                            item {
                                ThinkingIndicator(
                                    status = generationStatus,
                                    onStop = { viewModel.stopGeneration() }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(10.dp)) }
                    }
                }
            }

            // Composer Area
            ChatComposer(
                inputText = inputText,
                onInputTextChange = { viewModel.updateInputText(it) },
                currentAttachment = currentAttachment,
                onRemoveAttachment = { viewModel.removeAttachment() },
                isGenerating = isGenerating,
                speechState = speechState,
                onAttachClick = { showAttachmentSheet = true },
                onMicClick = {
                    viewModel.toggleVoiceInput {
                        audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    }
                },
                onSendClick = { viewModel.sendMessage() },
                onStopClick = { viewModel.stopGeneration() }
            )
        }
    }

    // Attachment Modal Bottom Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            containerColor = tokens.surface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Attachment",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(tokens.surfaceElevated)
                        .clickable {
                            showAttachmentSheet = false
                            imagePickerLauncher.launch("image/*")
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(tokens.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Image, contentDescription = null, tint = tokens.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Photo / Image", fontWeight = FontWeight.SemiBold, color = tokens.textPrimary, fontSize = 14.sp)
                        Text("Analyze images, photos, diagrams, and visual queries", color = tokens.textSecondary, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(tokens.surfaceElevated)
                        .clickable {
                            showAttachmentSheet = false
                            filePickerLauncher.launch("*/*")
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(tokens.secondary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.AttachFile, contentDescription = null, tint = tokens.secondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Document / PDF / Code", fontWeight = FontWeight.SemiBold, color = tokens.textPrimary, fontSize = 14.sp)
                        Text("Analyze PDFs, codebases, text documents, CSV and logs", color = tokens.textSecondary, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Pro Advanced Model Selector Sheet
    val showModelSelector by viewModel.showModelSelector.collectAsState()
    if (showModelSelector) {
        ModelSelectorSheet(
            currentModel = currentConversation?.model ?: GeminiModelConstants.DEFAULT_MODEL,
            onSelectModel = { modelId ->
                viewModel.setModelForCurrentConversation(modelId)
            },
            onVerifyModel = { modelId ->
                viewModel.verifyModel(modelId)
            },
            onDismiss = { viewModel.setShowModelSelector(false) }
        )
    }

    // Real-Time Voice Assistant Dialog
    val showVoiceAssistant by viewModel.showVoiceAssistant.collectAsState()
    if (showVoiceAssistant) {
        VoiceAssistantDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setShowVoiceAssistant(false) }
        )
    }
}

@Composable
fun EmptyChatGreeting(
    onSuggestionClick: (String) -> Unit
) {
    val tokens = LocalNovaTokens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NovaLogo(size = 72.dp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hello, I'm NOVA AI ✨",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = tokens.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "What would you like to discover or create today?",
            fontSize = 14.sp,
            color = tokens.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Suggestion Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionCard(
                    title = "⚡ Explain quantum physics simply",
                    onClick = { onSuggestionClick("Explain the fundamentals of quantum physics in simple, engaging terms.") },
                    modifier = Modifier.weight(1f)
                )
                SuggestionCard(
                    title = "🚀 Brainstorm startup ideas",
                    onClick = { onSuggestionClick("Brainstorm 5 innovative, practical startup ideas in AI and sustainability.") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionCard(
                    title = "💻 Write a Kotlin Compose script",
                    onClick = { onSuggestionClick("Show me a clean Jetpack Compose animation snippet with best practices.") },
                    modifier = Modifier.weight(1f)
                )
                SuggestionCard(
                    title = "✍️ Draft a professional email",
                    onClick = { onSuggestionClick("Help me draft a concise, persuasive email proposing a new partnership.") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalNovaTokens.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tokens.surface)
            .border(1.dp, tokens.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = tokens.textPrimary,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun ChatMessageRow(
    message: MessageEntity,
    isGenerating: Boolean,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onContinue: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFeedback: (Int) -> Unit,
    onShare: () -> Unit,
    onSpeak: (String) -> Unit = {},
    isSpeaking: Boolean = false,
    onStopSpeak: () -> Unit = {}
) {
    val tokens = LocalNovaTokens.current
    val isUser = message.role == "user"
    var isCopied by remember { mutableStateOf(false) }
    val parsed = remember(message.content) { ResponseParser.parse(message.content) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            NovaLogo(size = 32.dp, isAnimated = false, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 340.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // User Attachment Preview
            if (message.attachmentUri != null) {
                if (message.attachmentMimeType?.startsWith("image/") == true) {
                    AsyncImage(
                        model = Uri.parse(message.attachmentUri),
                        contentDescription = "Attachment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, tokens.border, RoundedCornerShape(16.dp))
                            .padding(bottom = 6.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(tokens.surfaceElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.AttachFile, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = message.attachmentName ?: "Document", fontSize = 12.sp, color = tokens.textPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Collapsible Ultra Thinking / Reasoning Card
            if (!isUser && parsed.thinkingText != null) {
                ThinkingProcessCard(
                    thinkingText = parsed.thinkingText,
                    isStreaming = isGenerating && message.content.isNotEmpty()
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Message Bubble
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        )
                    )
                    .background(
                        if (isUser) tokens.primary else tokens.surface
                    )
                    .border(
                        1.dp,
                        if (isUser) Color.Transparent else tokens.border,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (message.content.isEmpty() && !isUser) {
                    // Empty assistant stream placeholder
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = tokens.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thinking...", fontSize = 13.sp, color = tokens.textSecondary)
                    }
                } else {
                    MarkdownText(
                        markdown = if (isUser) message.content else parsed.cleanText,
                        isUser = isUser
                    )
                }
            }

            // Grounding Web Sources
            if (!isUser && parsed.sources.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                GroundingSourcesView(sources = parsed.sources)
            }

            // Actions & Metadata Bar
            Row(
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = TimeFormatter.formatMessageTime(message.timestamp),
                    fontSize = 10.sp,
                    color = tokens.textMuted
                )

                if (isUser) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = tokens.textMuted, modifier = Modifier.size(13.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = tokens.textMuted, modifier = Modifier.size(13.dp))
                    }
                } else {
                    // Assistant Actions
                    IconButton(
                        onClick = {
                            onCopy()
                            isCopied = true
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                            contentDescription = "Copy",
                            tint = if (isCopied) Color(0xFF10B981) else tokens.textMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // TTS Read Aloud Action
                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                onStopSpeak()
                            } else {
                                onSpeak(parsed.cleanText)
                            }
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = if (isSpeaking) "Stop Reading" else "Read Aloud",
                            tint = if (isSpeaking) tokens.primary else tokens.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    if (!isGenerating) {
                        IconButton(onClick = onRegenerate, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Regenerate", tint = tokens.textMuted, modifier = Modifier.size(13.dp))
                        }

                        IconButton(onClick = onContinue, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = "Continue", tint = tokens.textMuted, modifier = Modifier.size(14.dp))
                        }

                        IconButton(onClick = { onFeedback(if (message.feedback == 1) 0 else 1) }, modifier = Modifier.size(20.dp)) {
                            Icon(
                                imageVector = if (message.feedback == 1) Icons.Rounded.ThumbUp else Icons.Rounded.ThumbUpOffAlt,
                                contentDescription = "Like",
                                tint = if (message.feedback == 1) tokens.primary else tokens.textMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        IconButton(onClick = { onFeedback(if (message.feedback == -1) 0 else -1) }, modifier = Modifier.size(20.dp)) {
                            Icon(
                                imageVector = if (message.feedback == -1) Icons.Rounded.ThumbDown else Icons.Rounded.ThumbDownOffAlt,
                                contentDescription = "Dislike",
                                tint = if (message.feedback == -1) Color(0xFFEF4444) else tokens.textMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        IconButton(onClick = onShare, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share", tint = tokens.textMuted, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingIndicator(
    status: String,
    onStop: () -> Unit
) {
    val tokens = LocalNovaTokens.current
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tokens.surface)
            .border(1.dp, tokens.border, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = tokens.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = status,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = tokens.textPrimary.copy(alpha = alpha)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x20EF4444))
                .clickable(onClick = onStop)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Stop, contentDescription = "Stop", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Stop", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ChatComposer(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    currentAttachment: AttachmentData?,
    onRemoveAttachment: () -> Unit,
    isGenerating: Boolean,
    speechState: SpeechState,
    onAttachClick: () -> Unit,
    onMicClick: () -> Unit,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val tokens = LocalNovaTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(tokens.surface)
            .border(1.dp, tokens.border, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .navigationBarsPadding()
    ) {
        // Active Attachment Chip
        AnimatedVisibility(visible = currentAttachment != null) {
            currentAttachment?.let { att ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tokens.surfaceElevated)
                        .border(1.dp, tokens.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (att.mimeType.startsWith("image/")) Icons.Rounded.Image else Icons.Rounded.AttachFile,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = att.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = tokens.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val details = buildString {
                                if (att.formattedSize.isNotBlank()) append(att.formattedSize)
                                if (att.lineCount != null) append(" • ").append(att.lineCount).append(" lines")
                                if (att.tokenEstimate > 0) append(" • ~").append(att.tokenEstimate).append(" tokens")
                            }
                            if (details.isNotBlank()) {
                                Text(
                                    text = details,
                                    fontSize = 10.sp,
                                    color = tokens.textMuted
                                )
                            }
                        }
                    }
                    IconButton(onClick = onRemoveAttachment, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Remove", tint = tokens.textSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Voice Listening Banner
        AnimatedVisibility(visible = speechState == SpeechState.LISTENING) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tokens.primary.copy(alpha = 0.15f))
                    .clickable { onMicClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Listening... Speak now (tap to finish)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.primary
                )
            }
        }

        // Input Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Attachment Button
            IconButton(
                onClick = onAttachClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(tokens.surfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Attach image or file",
                    tint = tokens.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Input Text Field
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 42.dp, max = 130.dp),
                placeholder = {
                    Text(
                        text = "Ask NOVA anything...",
                        fontSize = 14.sp,
                        color = tokens.textMuted
                    )
                },
                maxLines = 5,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = tokens.primary,
                    unfocusedBorderColor = tokens.border,
                    focusedContainerColor = tokens.surfaceElevated,
                    unfocusedContainerColor = tokens.surfaceElevated
                )
            )

            // Voice Input Button
            IconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (speechState == SpeechState.LISTENING) tokens.primary else tokens.surfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = if (speechState == SpeechState.LISTENING) "Stop Listening" else "Voice Input",
                    tint = if (speechState == SpeechState.LISTENING) Color.White else tokens.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Send / Stop Button
            IconButton(
                onClick = if (isGenerating) onStopClick else onSendClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isGenerating) Color(0xFFEF4444) else tokens.primary)
            ) {
                Icon(
                    imageVector = if (isGenerating) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.Send,
                    contentDescription = if (isGenerating) "Stop Generation" else "Send Message",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
