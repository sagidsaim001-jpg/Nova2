package com.example.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiConnectionState
import com.example.data.api.ApiContent
import com.example.data.api.ApiInlineData
import com.example.data.api.ApiPart
import com.example.data.api.ApiTestResult
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiModelConstants
import com.example.data.api.GroundingSource
import com.example.data.api.ResponseParser
import com.example.data.db.ConversationEntity
import com.example.data.db.MessageEntity
import com.example.data.db.SearchResult
import com.example.data.db.UserAccountEntity
import com.example.data.model.AiMode
import com.example.data.repository.AccentColor
import com.example.data.repository.ChatDensity
import com.example.data.repository.ChatRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.ThemeMode
import com.example.util.FileHelper
import com.example.util.SpeechHelper
import com.example.util.SpeechState
import com.example.util.TtsHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class AttachmentData(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val base64Data: String? = null,
    val extractedText: String? = null,
    val isPdf: Boolean = false,
    val formattedSize: String = "",
    val tokenEstimate: Int = 0,
    val lineCount: Int? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val preferencesRepository: PreferencesRepository,
    private val geminiClient: GeminiClient,
    private val context: Context
) : ViewModel() {

    val speechHelper = SpeechHelper(context)
    val ttsHelper = TtsHelper(context)

    // Current active user
    val activeUserId: StateFlow<String> = preferencesRepository.activeUserIdFlow
    private val _currentUserAccount = MutableStateFlow<UserAccountEntity?>(null)
    val currentUserAccount: StateFlow<UserAccountEntity?> = _currentUserAccount.asStateFlow()

    // Preferences & Theme
    val themeMode: StateFlow<ThemeMode> = preferencesRepository.themeModeFlow
    val accentColor: StateFlow<AccentColor> = preferencesRepository.accentColorFlow
    val customApiKey: StateFlow<String> = preferencesRepository.customApiKeyFlow

    // Pro AI Modes & Parameters
    private val _currentAiMode = MutableStateFlow(AiMode.fromId(preferencesRepository.getAiMode()))
    val currentAiMode: StateFlow<AiMode> = _currentAiMode.asStateFlow()

    private val _currentThinkingEffort = MutableStateFlow(preferencesRepository.getThinkingEffort())
    val currentThinkingEffort: StateFlow<Int> = _currentThinkingEffort.asStateFlow()

    private val _isGoogleSearchGrounding = MutableStateFlow(preferencesRepository.isGoogleSearchGrounding())
    val isGoogleSearchGrounding: StateFlow<Boolean> = _isGoogleSearchGrounding.asStateFlow()

    private val _isCodeExecution = MutableStateFlow(false)
    val isCodeExecution: StateFlow<Boolean> = _isCodeExecution.asStateFlow()

    // Voice & Model Dialogs
    private val _showVoiceAssistant = MutableStateFlow(false)
    val showVoiceAssistant: StateFlow<Boolean> = _showVoiceAssistant.asStateFlow()

    private val _showModelSelector = MutableStateFlow(false)
    val showModelSelector: StateFlow<Boolean> = _showModelSelector.asStateFlow()

    // Conversations Flow
    private val _activeConversations = MutableStateFlow<List<ConversationEntity>>(emptyList())
    val activeConversations: StateFlow<List<ConversationEntity>> = _activeConversations.asStateFlow()

    private val _favoriteConversations = MutableStateFlow<List<ConversationEntity>>(emptyList())
    val favoriteConversations: StateFlow<List<ConversationEntity>> = _favoriteConversations.asStateFlow()

    private val _archivedConversations = MutableStateFlow<List<ConversationEntity>>(emptyList())
    val archivedConversations: StateFlow<List<ConversationEntity>> = _archivedConversations.asStateFlow()

    // Current active conversation
    private val _currentConversation = MutableStateFlow<ConversationEntity?>(null)
    val currentConversation: StateFlow<ConversationEntity?> = _currentConversation.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    // Composer State
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _currentAttachment = MutableStateFlow<AttachmentData?>(null)
    val currentAttachment: StateFlow<AttachmentData?> = _currentAttachment.asStateFlow()

    // Generation State
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationStatus = MutableStateFlow("Thinking...")
    val generationStatus: StateFlow<String> = _generationStatus.asStateFlow()

    private var currentGenerationJob: Job? = null

    // API Connection State
    private val _apiConnectionState = MutableStateFlow(ApiConnectionState.DISCONNECTED)
    val apiConnectionState: StateFlow<ApiConnectionState> = _apiConnectionState.asStateFlow()

    private val _lastApiTestResult = MutableStateFlow<ApiTestResult?>(null)
    val lastApiTestResult: StateFlow<ApiTestResult?> = _lastApiTestResult.asStateFlow()

    // Search Results
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // App Navigation & Dialogs
    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showApiSettingsDialog = MutableStateFlow(false)
    val showApiSettingsDialog: StateFlow<Boolean> = _showApiSettingsDialog.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _showSearchDialog = MutableStateFlow(false)
    val showSearchDialog: StateFlow<Boolean> = _showSearchDialog.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _conversationToRename = MutableStateFlow<ConversationEntity?>(null)
    val conversationToRename: StateFlow<ConversationEntity?> = _conversationToRename.asStateFlow()

    private val _conversationToDelete = MutableStateFlow<ConversationEntity?>(null)
    val conversationToDelete: StateFlow<ConversationEntity?> = _conversationToDelete.asStateFlow()

    private val _editingMessage = MutableStateFlow<MessageEntity?>(null)
    val editingMessage: StateFlow<MessageEntity?> = _editingMessage.asStateFlow()

    init {
        // Observe speech recognition
        viewModelScope.launch {
            speechHelper.recognizedText.collectLatest { text ->
                if (text.isNotBlank()) {
                    _inputText.value = if (_inputText.value.isBlank()) text else "${_inputText.value} $text"
                }
            }
        }

        // Observe active user changes to reload conversations
        viewModelScope.launch {
            preferencesRepository.activeUserIdFlow.collectLatest { userId ->
                loadUserData(userId)
            }
        }

        // Initial test connection to verify API status silently
        testApiConnection()
    }

    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            _currentUserAccount.value = chatRepository.getUserById(userId)
        }

        viewModelScope.launch {
            chatRepository.getActiveConversations(userId).collectLatest { list ->
                _activeConversations.value = list
                // If current conversation is null or doesn't belong to current user, select latest or create one
                if (_currentConversation.value == null && list.isNotEmpty()) {
                    selectConversation(list.first())
                }
            }
        }

        viewModelScope.launch {
            chatRepository.getFavoriteConversations(userId).collectLatest { list ->
                _favoriteConversations.value = list
            }
        }

        viewModelScope.launch {
            chatRepository.getArchivedConversations(userId).collectLatest { list ->
                _archivedConversations.value = list
            }
        }
    }

    fun selectConversation(conversation: ConversationEntity) {
        _currentConversation.value = conversation
        _inputText.value = conversation.draft
        _currentAttachment.value = null
        _isDrawerOpen.value = false

        viewModelScope.launch {
            chatRepository.getMessages(conversation.id).collectLatest { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun startNewConversation(model: String? = null) {
        viewModelScope.launch {
            val defaultModel = model ?: preferencesRepository.getDefaultModel()
            val newConv = ConversationEntity(
                userId = preferencesRepository.getActiveUserId(),
                title = "New Chat",
                model = defaultModel,
                systemInstruction = preferencesRepository.getCustomInstructions()
            )
            chatRepository.createOrUpdateConversation(newConv)
            selectConversation(newConv)
        }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
        _currentConversation.value?.let { conv ->
            if (preferencesRepository.isAutoScroll()) {
                viewModelScope.launch {
                    chatRepository.updateDraft(conv.id, text)
                }
            }
        }
    }

    private var baseTextBeforeSpeech: String = ""

    fun toggleVoiceInput(onNeedPermission: () -> Unit) {
        if (speechHelper.speechState.value == SpeechState.LISTENING) {
            speechHelper.stopListening()
            return
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            onNeedPermission()
            return
        }

        startVoiceListeningSession()
    }

    fun onAudioPermissionGranted() {
        startVoiceListeningSession()
    }

    private fun startVoiceListeningSession() {
        baseTextBeforeSpeech = _inputText.value.trim()
        speechHelper.startListening(
            onPartial = { partial ->
                val combined = if (baseTextBeforeSpeech.isBlank()) {
                    partial.trim()
                } else {
                    "$baseTextBeforeSpeech ${partial.trim()}"
                }
                updateInputText(combined)
            },
            onResult = { finalResult ->
                if (finalResult.isNotBlank()) {
                    val combined = if (baseTextBeforeSpeech.isBlank()) {
                        finalResult.trim()
                    } else {
                        "$baseTextBeforeSpeech ${finalResult.trim()}"
                    }
                    updateInputText(combined)
                }
            }
        )
    }

    fun stopVoiceInput() {
        speechHelper.stopListening()
    }

    fun attachImage(uri: Uri) {
        val name = FileHelper.getFileName(context, uri)
        val size = FileHelper.getFileSize(context, uri)
        val base64Pair = FileHelper.uriToBase64(context, uri)
        if (base64Pair != null) {
            _currentAttachment.value = AttachmentData(
                uri = uri,
                name = name,
                mimeType = base64Pair.second,
                base64Data = base64Pair.first,
                formattedSize = FileHelper.formatBytes(size),
                tokenEstimate = 258 // standard vision prompt tokens
            )
        }
    }

    fun attachFile(uri: Uri) {
        val name = FileHelper.getFileName(context, uri)
        val size = FileHelper.getFileSize(context, uri)
        val isPdf = name.endsWith(".pdf", ignoreCase = true)

        if (isPdf) {
            val base64Pdf = FileHelper.readPdfAsBase64(context, uri)
            _currentAttachment.value = AttachmentData(
                uri = uri,
                name = name,
                mimeType = "application/pdf",
                base64Data = base64Pdf,
                isPdf = true,
                formattedSize = FileHelper.formatBytes(size),
                tokenEstimate = (size / 80).toInt().coerceAtLeast(20)
            )
        } else {
            val text = FileHelper.readTextFromUri(context, uri)
            val lines = text?.lines()?.size
            val tokens = if (text != null) FileHelper.estimateTokens(text.length) else 0
            _currentAttachment.value = AttachmentData(
                uri = uri,
                name = name,
                mimeType = "text/plain",
                extractedText = text,
                formattedSize = FileHelper.formatBytes(size),
                tokenEstimate = tokens,
                lineCount = lines
            )
        }
    }

    fun removeAttachment() {
        _currentAttachment.value = null
    }

    fun sendMessage(customPrompt: String? = null) {
        val prompt = (customPrompt ?: _inputText.value).trim()
        val attachment = _currentAttachment.value
        if (prompt.isEmpty() && attachment == null) return

        if (_isGenerating.value) {
            stopGeneration()
            return
        }

        val conv = _currentConversation.value ?: run {
            val defaultModel = preferencesRepository.getDefaultModel()
            val newConv = ConversationEntity(
                userId = preferencesRepository.getActiveUserId(),
                title = if (prompt.isNotEmpty()) prompt.take(30) else "Document Query",
                model = defaultModel,
                systemInstruction = preferencesRepository.getCustomInstructions()
            )
            _currentConversation.value = newConv
            viewModelScope.launch {
                chatRepository.createOrUpdateConversation(newConv)
            }
            newConv
        }

        // Clear composer
        _inputText.value = ""
        _currentAttachment.value = null
        _editingMessage.value = null

        val userMessage = MessageEntity(
            conversationId = conv.id,
            role = "user",
            content = if (attachment?.extractedText != null) {
                "$prompt\n\n[Attached File: ${attachment.name}]\n${attachment.extractedText}"
            } else if (attachment?.isPdf == true) {
                "$prompt\n\n[Attached PDF Document: ${attachment.name} (${attachment.formattedSize})]"
            } else {
                prompt
            },
            model = conv.model,
            attachmentUri = attachment?.uri?.toString(),
            attachmentMimeType = attachment?.mimeType,
            attachmentName = attachment?.name
        )

        val assistantMessageId = UUID.randomUUID().toString()
        val assistantPlaceholder = MessageEntity(
            id = assistantMessageId,
            conversationId = conv.id,
            role = "model",
            content = "",
            model = conv.model
        )

        viewModelScope.launch {
            chatRepository.insertMessage(userMessage)
            chatRepository.insertMessage(assistantPlaceholder)

            // Auto-title conversation if it's the first message
            if (conv.title == "New Chat" && prompt.isNotEmpty() && preferencesRepository.isAutoTitle()) {
                val autoTitle = prompt.take(32).trim()
                chatRepository.renameConversation(conv.id, autoTitle)
                _currentConversation.value = conv.copy(title = autoTitle)
            }

            executeGeminiRequest(
                conversation = conv,
                assistantMessageId = assistantMessageId,
                attachment = attachment
            )
        }
    }

    private fun executeGeminiRequest(
        conversation: ConversationEntity,
        assistantMessageId: String,
        attachment: AttachmentData?
    ) {
        currentGenerationJob?.cancel()
        _isGenerating.value = true
        _generationStatus.value = "Thinking..."

        currentGenerationJob = viewModelScope.launch {
            try {
                val allDbMessages = chatRepository.getMessagesList(conversation.id)
                val contents = mutableListOf<ApiContent>()

                // Construct conversation history for Gemini multi-turn API
                for (msg in allDbMessages) {
                    if (msg.id == assistantMessageId) continue // skip the empty placeholder
                    if (msg.role == "user") {
                        val parts = mutableListOf<ApiPart>()
                        if (msg.content.isNotBlank()) {
                            parts.add(ApiPart(text = msg.content))
                        }
                        // If this is the current message with image or PDF attachment
                        if (msg.attachmentUri != null && attachment?.base64Data != null) {
                            parts.add(
                                ApiPart(
                                    inlineData = ApiInlineData(
                                        mimeType = attachment.mimeType,
                                        data = attachment.base64Data
                                    )
                                )
                            )
                        }
                        if (parts.isNotEmpty()) {
                            contents.add(ApiContent(role = "user", parts = parts))
                        }
                    } else if (msg.role == "model" && msg.content.isNotBlank()) {
                        contents.add(
                            ApiContent(
                                role = "model",
                                parts = listOf(ApiPart(text = msg.content))
                            )
                        )
                    }
                }

                if (contents.isEmpty()) {
                    contents.add(ApiContent(role = "user", parts = listOf(ApiPart(text = "Hello"))))
                }

                val activeMode = _currentAiMode.value
                val baseInstructions = preferencesRepository.getCustomInstructions()
                val convInstructions = conversation.systemInstruction

                val systemPrompt = buildString {
                    append(activeMode.systemInstruction)
                    if (baseInstructions.isNotBlank()) {
                        append("\n\nUser Preferences: ")
                        append(baseInstructions)
                    }
                    if (convInstructions.isNotBlank()) {
                        append("\n\nSession Context: ")
                        append(convInstructions)
                    }
                }.trim().ifBlank { null }

                val streaming = preferencesRepository.isStreamingEnabled()
                val thinkingBudget = if (activeMode == AiMode.THINKING || _currentThinkingEffort.value > 0) {
                    _currentThinkingEffort.value
                } else null
                val enableSearch = _isGoogleSearchGrounding.value || activeMode == AiMode.RESEARCH
                val enableCode = activeMode == AiMode.CODING || _isCodeExecution.value
                val temperature = activeMode.temperature

                if (streaming) {
                    _generationStatus.value = if (activeMode == AiMode.THINKING) "Reasoning deeply..." else "Generating..."
                    val collectedSources = mutableListOf<GroundingSource>()

                    val streamFlow = geminiClient.streamGenerateContent(
                        model = conversation.model,
                        contents = contents,
                        systemInstructionText = systemPrompt,
                        temperature = temperature,
                        thinkingBudget = thinkingBudget,
                        enableGoogleSearch = enableSearch,
                        enableCodeExecution = enableCode,
                        onGroundingSources = { sources ->
                            collectedSources.addAll(sources)
                        }
                    )

                    val responseAccumulator = StringBuilder()
                    streamFlow.collect { chunk ->
                        responseAccumulator.append(chunk)
                        chatRepository.updateMessageContent(
                            id = assistantMessageId,
                            content = responseAccumulator.toString()
                        )
                    }

                    if (responseAccumulator.isEmpty()) {
                        chatRepository.updateMessageContent(
                            id = assistantMessageId,
                            content = "I was unable to generate a response. Please check your prompt or API status.",
                            isError = true
                        )
                    } else {
                        // Append Grounding Sources if received
                        if (collectedSources.isNotEmpty()) {
                            val uniqueSources = collectedSources.distinctBy { it.url }
                            val sourcesBlock = "\n\n### 🌐 Sources & Web Grounding\n" + uniqueSources.joinToString("\n") {
                                "- [${it.title}](${it.url})"
                            }
                            val finalResponse = responseAccumulator.toString() + sourcesBlock
                            chatRepository.updateMessageContent(
                                id = assistantMessageId,
                                content = finalResponse
                            )
                        }
                        _apiConnectionState.value = ApiConnectionState.CONNECTED
                    }
                } else {
                    _generationStatus.value = "Generating..."
                    val directResponse = geminiClient.generateContent(
                        model = conversation.model,
                        contents = contents,
                        systemInstructionText = systemPrompt,
                        temperature = temperature,
                        thinkingBudget = thinkingBudget,
                        enableGoogleSearch = enableSearch,
                        enableCodeExecution = enableCode
                    )
                    chatRepository.updateMessageContent(
                        id = assistantMessageId,
                        content = directResponse
                    )
                    _apiConnectionState.value = ApiConnectionState.CONNECTED
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Failed to generate response."
                chatRepository.updateMessageContent(
                    id = assistantMessageId,
                    content = "⚠️ $errorMsg",
                    isError = true
                )
                if (errorMsg.contains("401") || errorMsg.contains("UNAUTHENTICATED") || errorMsg.contains("API_KEY_INVALID")) {
                    _apiConnectionState.value = ApiConnectionState.INVALID_KEY
                } else if (errorMsg.contains("403") || errorMsg.contains("PERMISSION_DENIED")) {
                    _apiConnectionState.value = ApiConnectionState.PERMISSION_DENIED
                } else if (errorMsg.contains("404") || errorMsg.contains("NOT_FOUND")) {
                    _apiConnectionState.value = ApiConnectionState.MODEL_NOT_FOUND
                } else if (errorMsg.contains("429") || errorMsg.contains("RESOURCE_EXHAUSTED")) {
                    _apiConnectionState.value = ApiConnectionState.RATE_LIMITED
                } else if (errorMsg.contains("500") || errorMsg.contains("503") || errorMsg.contains("UNAVAILABLE")) {
                    _apiConnectionState.value = ApiConnectionState.SERVER_ERROR
                } else if (errorMsg.contains("Network", ignoreCase = true)) {
                    _apiConnectionState.value = ApiConnectionState.NETWORK_ERROR
                } else {
                    _apiConnectionState.value = ApiConnectionState.DISCONNECTED
                }
            } finally {
                _isGenerating.value = false
                _generationStatus.value = ""
            }
        }
    }

    fun stopGeneration() {
        currentGenerationJob?.cancel()
        currentGenerationJob = null
        _isGenerating.value = false
        _generationStatus.value = ""
    }

    fun regenerateResponse(message: MessageEntity) {
        val conv = _currentConversation.value ?: return
        if (_isGenerating.value) return

        viewModelScope.launch {
            // Remove previous error or message from timestamp and execute again
            chatRepository.deleteMessagesFromTimestamp(conv.id, message.timestamp)
            val assistantMessageId = UUID.randomUUID().toString()
            val assistantPlaceholder = MessageEntity(
                id = assistantMessageId,
                conversationId = conv.id,
                role = "model",
                content = "",
                model = conv.model
            )
            chatRepository.insertMessage(assistantPlaceholder)
            executeGeminiRequest(conv, assistantMessageId, null)
        }
    }

    fun continueResponse() {
        val conv = _currentConversation.value ?: return
        if (_isGenerating.value) return

        sendMessage("Please continue directly from where you left off.")
    }

    fun editUserMessage(message: MessageEntity) {
        _editingMessage.value = message
        _inputText.value = message.content
    }

    fun deleteMessage(message: MessageEntity) {
        viewModelScope.launch {
            chatRepository.deleteMessage(message.id)
        }
    }

    fun setMessageFeedback(message: MessageEntity, feedback: Int) {
        viewModelScope.launch {
            chatRepository.setMessageFeedback(message.id, feedback)
        }
    }

    // Model selection
    fun setModelForCurrentConversation(modelId: String) {
        val conv = _currentConversation.value ?: return
        _currentConversation.value = conv.copy(model = modelId)
        viewModelScope.launch {
            chatRepository.updateModel(conv.id, modelId)
        }
    }

    // Favorite / Archive / Rename / Delete Conversation
    fun toggleFavorite(conversation: ConversationEntity) {
        viewModelScope.launch {
            val newFav = !conversation.isFavorite
            chatRepository.setFavorite(conversation.id, newFav)
            if (_currentConversation.value?.id == conversation.id) {
                _currentConversation.value = _currentConversation.value?.copy(isFavorite = newFav)
            }
        }
    }

    fun toggleArchive(conversation: ConversationEntity) {
        viewModelScope.launch {
            val newArchived = !conversation.isArchived
            chatRepository.setArchived(conversation.id, newArchived)
            if (_currentConversation.value?.id == conversation.id) {
                _currentConversation.value = _currentConversation.value?.copy(isArchived = newArchived)
            }
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch {
            chatRepository.renameConversation(id, newTitle)
            if (_currentConversation.value?.id == id) {
                _currentConversation.value = _currentConversation.value?.copy(title = newTitle)
            }
            _conversationToRename.value = null
        }
    }

    fun deleteConversation(conversation: ConversationEntity) {
        viewModelScope.launch {
            chatRepository.deleteConversation(conversation.id)
            if (_currentConversation.value?.id == conversation.id) {
                _currentConversation.value = null
                _messages.value = emptyList()
            }
            _conversationToDelete.value = null
        }
    }

    fun deleteAllConversations() {
        viewModelScope.launch {
            chatRepository.deleteAllConversations(preferencesRepository.getActiveUserId())
            _currentConversation.value = null
            _messages.value = emptyList()
        }
    }

    // API Testing & Key Configuration
    fun testApiConnection(customKey: String? = null, model: String? = null) {
        viewModelScope.launch {
            _apiConnectionState.value = ApiConnectionState.CONNECTING
            val effectiveModel = model ?: _currentConversation.value?.model ?: GeminiModelConstants.DEFAULT_MODEL
            val result = geminiClient.testConnection(customKey, effectiveModel)
            _lastApiTestResult.value = result
            _apiConnectionState.value = result.state
        }
    }

    fun isSystemKeyConfigured(): Boolean = geminiClient.isSystemKeyConfigured()

    fun getMaskedSystemKey(): String? {
        val buildKey = try {
            com.example.BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") {
            GeminiClient.maskApiKey(buildKey)
        } else null
    }

    fun saveCustomApiKey(key: String) {
        val cleanKey = key.trim()
        preferencesRepository.setCustomApiKey(cleanKey)
        testApiConnection(cleanKey)
    }

    fun removeCustomApiKey() {
        preferencesRepository.setCustomApiKey("")
        _lastApiTestResult.value = null
        testApiConnection()
    }

    // Search
    fun searchConversationsAndMessages(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _searchResults.value = chatRepository.search(preferencesRepository.getActiveUserId(), query)
        }
    }

    fun openSearchResult(result: SearchResult) {
        viewModelScope.launch {
            val conv = chatRepository.getConversationById(result.conversationId)
            if (conv != null) {
                selectConversation(conv)
                _showSearchDialog.value = false
            }
        }
    }

    // User Account
    fun registerAccount(name: String, email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val user = chatRepository.registerUser(name, email, pass)
                preferencesRepository.setActiveUserId(user.id)
                preferencesRepository.setActiveUserName(user.name)
                preferencesRepository.setActiveUserEmail(user.email)
                _currentUserAccount.value = user
                onResult(true, "Welcome, ${user.name}!")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Registration failed")
            }
        }
    }

    fun loginAccount(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = chatRepository.loginUser(email, pass)
            if (user != null) {
                preferencesRepository.setActiveUserId(user.id)
                preferencesRepository.setActiveUserName(user.name)
                preferencesRepository.setActiveUserEmail(user.email)
                _currentUserAccount.value = user
                onResult(true, "Logged in as ${user.name}")
            } else {
                onResult(false, "Invalid email or password.")
            }
        }
    }

    fun logoutAccount() {
        preferencesRepository.setActiveUserId("guest")
        preferencesRepository.setActiveUserName("Guest Explorer")
        preferencesRepository.setActiveUserEmail("guest@nova.ai")
        _currentUserAccount.value = null
        startNewConversation()
    }

    // Share & Export
    fun exportCurrentChat(format: String) {
        val conv = _currentConversation.value ?: return
        val msgs = _messages.value
        val text = when (format.lowercase()) {
            "markdown", "md" -> FileHelper.exportToMarkdown(conv, msgs)
            "json" -> FileHelper.exportToJson(conv, msgs)
            else -> FileHelper.exportToText(conv, msgs)
        }
        FileHelper.shareText(context, "NOVA AI - ${conv.title}", text)
        _showExportDialog.value = false
    }

    // UI Dialog Triggers
    fun setDrawerOpen(open: Boolean) { _isDrawerOpen.value = open }
    fun setShowSettingsDialog(show: Boolean) { _showSettingsDialog.value = show }
    fun setShowApiSettingsDialog(show: Boolean) { _showApiSettingsDialog.value = show }
    fun setShowAuthDialog(show: Boolean) { _showAuthDialog.value = show }
    fun setShowSearchDialog(show: Boolean) { _showSearchDialog.value = show }
    fun setShowExportDialog(show: Boolean) { _showExportDialog.value = show }
    fun setShowVoiceAssistant(show: Boolean) { _showVoiceAssistant.value = show }
    fun setShowModelSelector(show: Boolean) { _showModelSelector.value = show }
    fun setConversationToRename(conv: ConversationEntity?) { _conversationToRename.value = conv }
    fun setConversationToDelete(conv: ConversationEntity?) { _conversationToDelete.value = conv }

    // Pro AI Modes & Parameters
    fun setAiMode(mode: AiMode) {
        _currentAiMode.value = mode
        preferencesRepository.setAiMode(mode.id)
    }

    fun setThinkingEffort(effortTokens: Int) {
        _currentThinkingEffort.value = effortTokens
        preferencesRepository.setThinkingEffort(effortTokens)
    }

    fun setGoogleSearchGrounding(enabled: Boolean) {
        _isGoogleSearchGrounding.value = enabled
        preferencesRepository.setGoogleSearchGrounding(enabled)
    }

    fun setCodeExecution(enabled: Boolean) {
        _isCodeExecution.value = enabled
    }

    suspend fun verifyModel(modelId: String): ApiTestResult {
        return geminiClient.verifyModelAvailability(modelId)
    }

    fun speakLastAssistantResponse(speechRate: Float = 1.0f, onReply: (String) -> Unit = {}) {
        viewModelScope.launch {
            while (_isGenerating.value) {
                delay(200)
            }
            val conv = _currentConversation.value ?: return@launch
            val msgs = chatRepository.getMessagesList(conv.id)
            val lastAssistant = msgs.lastOrNull { it.role == "model" && it.content.isNotBlank() }
            if (lastAssistant != null) {
                val parsed = ResponseParser.parse(lastAssistant.content)
                onReply(parsed.cleanText)
                ttsHelper.speak(parsed.cleanText, speechRate = speechRate)
            }
        }
    }

    fun readMessageAloud(text: String, speechRate: Float = 1.0f) {
        val parsed = ResponseParser.parse(text)
        ttsHelper.speak(parsed.cleanText, speechRate = speechRate)
    }

    fun stopTts() {
        ttsHelper.stop()
    }

    // Settings modifiers
    fun setThemeMode(mode: ThemeMode) = preferencesRepository.setThemeMode(mode)
    fun setAccentColor(color: AccentColor) = preferencesRepository.setAccentColor(color)
    fun setChatDensity(density: ChatDensity) = preferencesRepository.setChatDensity(density)
    fun setDefaultModel(model: String) = preferencesRepository.setDefaultModel(model)
    fun setCustomInstructions(inst: String) = preferencesRepository.setCustomInstructions(inst)
    fun setStreamingEnabled(enabled: Boolean) = preferencesRepository.setStreamingEnabled(enabled)
    fun setEnterToSend(enabled: Boolean) = preferencesRepository.setEnterToSend(enabled)
    fun setAutoTitle(enabled: Boolean) = preferencesRepository.setAutoTitle(enabled)
    fun setShowTimestamps(enabled: Boolean) = preferencesRepository.setShowTimestamps(enabled)
    fun setSyntaxHighlighting(enabled: Boolean) = preferencesRepository.setSyntaxHighlighting(enabled)
    fun setConfirmDelete(enabled: Boolean) = preferencesRepository.setConfirmDelete(enabled)

    override fun onCleared() {
        super.onCleared()
        speechHelper.shutdown()
        ttsHelper.shutdown()
    }
}

class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val preferencesRepository: PreferencesRepository,
    private val geminiClient: GeminiClient,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(chatRepository, preferencesRepository, geminiClient, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
