package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.ApiSettingsDialog
import com.example.ui.screens.AuthDialog
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DeleteConfirmDialog
import com.example.ui.screens.ExportDialog
import com.example.ui.screens.RenameDialog
import com.example.ui.screens.SearchDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SidebarDrawer
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.NovaTheme
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ChatViewModelFactory
import kotlinx.coroutines.launch

enum class AppDestination {
    SPLASH, WELCOME, CHAT, SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels {
        val app = application as NovaApplication
        ChatViewModelFactory(
            chatRepository = app.chatRepository,
            preferencesRepository = app.preferencesRepository,
            geminiClient = app.geminiClient,
            context = applicationContext
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val accentColor by viewModel.accentColor.collectAsState()

            NovaTheme(
                themeMode = themeMode,
                accentColor = accentColor
            ) {
                NovaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NovaApp(viewModel: ChatViewModel) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentScreen by remember { mutableStateOf(AppDestination.SPLASH) }

    val activeConversations by viewModel.activeConversations.collectAsState()
    val favoriteConversations by viewModel.favoriteConversations.collectAsState()
    val archivedConversations by viewModel.archivedConversations.collectAsState()
    val currentConversation by viewModel.currentConversation.collectAsState()
    val currentUserAccount by viewModel.currentUserAccount.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val apiConnectionState by viewModel.apiConnectionState.collectAsState()
    val lastApiTestResult by viewModel.lastApiTestResult.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Dialog State Observers
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()
    val showApiSettingsDialog by viewModel.showApiSettingsDialog.collectAsState()
    val showAuthDialog by viewModel.showAuthDialog.collectAsState()
    val showSearchDialog by viewModel.showSearchDialog.collectAsState()
    val showExportDialog by viewModel.showExportDialog.collectAsState()
    val conversationToRename by viewModel.conversationToRename.collectAsState()
    val conversationToDelete by viewModel.conversationToDelete.collectAsState()

    // Handle Back Press
    BackHandler(enabled = drawerState.isOpen || currentScreen == AppDestination.SETTINGS) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (currentScreen == AppDestination.SETTINGS) {
            currentScreen = AppDestination.CHAT
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentScreen == AppDestination.CHAT,
        drawerContent = {
            ModalDrawerSheet {
                SidebarDrawer(
                    activeConversations = activeConversations,
                    favoriteConversations = favoriteConversations,
                    archivedConversations = archivedConversations,
                    currentConversation = currentConversation,
                    currentUser = currentUserAccount,
                    onSelectConversation = { conv ->
                        viewModel.selectConversation(conv)
                        scope.launch { drawerState.close() }
                    },
                    onNewChat = {
                        viewModel.startNewConversation()
                        scope.launch { drawerState.close() }
                    },
                    onOpenSearch = { viewModel.setShowSearchDialog(true) },
                    onOpenSettings = {
                        scope.launch { drawerState.close() }
                        currentScreen = AppDestination.SETTINGS
                    },
                    onOpenApiSettings = {
                        scope.launch { drawerState.close() }
                        viewModel.setShowApiSettingsDialog(true)
                    },
                    onOpenAuth = {
                        scope.launch { drawerState.close() }
                        viewModel.setShowAuthDialog(true)
                    },
                    onRenameConversation = { conv -> viewModel.setConversationToRename(conv) },
                    onToggleFavorite = { conv -> viewModel.toggleFavorite(conv) },
                    onToggleArchive = { conv -> viewModel.toggleArchive(conv) },
                    onDeleteConversation = { conv -> viewModel.setConversationToDelete(conv) },
                    onExportChat = { viewModel.setShowExportDialog(true) }
                )
            }
        }
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                AppDestination.SPLASH -> {
                    SplashScreen(
                        onSplashFinished = {
                            if (activeConversations.isEmpty() && customApiKey.isBlank()) {
                                currentScreen = AppDestination.WELCOME
                            } else {
                                currentScreen = AppDestination.CHAT
                            }
                        }
                    )
                }
                AppDestination.WELCOME -> {
                    WelcomeScreen(
                        onStartChatting = {
                            viewModel.startNewConversation()
                            currentScreen = AppDestination.CHAT
                        },
                        onConfigureApi = {
                            viewModel.setShowApiSettingsDialog(true)
                        },
                        onOpenAuth = {
                            viewModel.setShowAuthDialog(true)
                        }
                    )
                }
                AppDestination.CHAT -> {
                    ChatScreen(
                        viewModel = viewModel,
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        },
                        onOpenSettings = {
                            currentScreen = AppDestination.SETTINGS
                        }
                    )
                }
                AppDestination.SETTINGS -> {
                    val app = (androidx.compose.ui.platform.LocalContext.current.applicationContext as NovaApplication)
                    SettingsScreen(
                        preferencesRepository = app.preferencesRepository,
                        currentUser = currentUserAccount,
                        onOpenApiDialog = { viewModel.setShowApiSettingsDialog(true) },
                        onOpenAuthDialog = { viewModel.setShowAuthDialog(true) },
                        onClearAllConversations = { viewModel.deleteAllConversations() },
                        onBack = { currentScreen = AppDestination.CHAT }
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (showApiSettingsDialog) {
        ApiSettingsDialog(
            currentApiKey = customApiKey,
            isSystemKeyConfigured = viewModel.isSystemKeyConfigured(),
            maskedSystemKey = viewModel.getMaskedSystemKey(),
            connectionState = apiConnectionState,
            testResult = lastApiTestResult,
            onSaveKey = { key ->
                viewModel.saveCustomApiKey(key)
            },
            onTestKey = { key ->
                viewModel.testApiConnection(key)
            },
            onRemoveKey = {
                viewModel.removeCustomApiKey()
            },
            onDismiss = { viewModel.setShowApiSettingsDialog(false) }
        )
    }

    if (showAuthDialog) {
        AuthDialog(
            currentUser = currentUserAccount,
            onRegister = { name, email, pass, cb ->
                viewModel.registerAccount(name, email, pass, cb)
            },
            onLogin = { email, pass, cb ->
                viewModel.loginAccount(email, pass, cb)
            },
            onLogout = {
                viewModel.logoutAccount()
            },
            onDismiss = { viewModel.setShowAuthDialog(false) }
        )
    }

    if (showSearchDialog) {
        SearchDialog(
            searchQuery = searchQuery,
            searchResults = searchResults,
            onQueryChange = { q -> viewModel.searchConversationsAndMessages(q) },
            onSelectResult = { res -> viewModel.openSearchResult(res) },
            onDismiss = { viewModel.setShowSearchDialog(false) }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            onExportFormat = { format -> viewModel.exportCurrentChat(format) },
            onDismiss = { viewModel.setShowExportDialog(false) }
        )
    }

    conversationToRename?.let { conv ->
        RenameDialog(
            initialTitle = conv.title,
            onConfirm = { newTitle -> viewModel.renameConversation(conv.id, newTitle) },
            onDismiss = { viewModel.setConversationToRename(null) }
        )
    }

    conversationToDelete?.let { conv ->
        DeleteConfirmDialog(
            title = conv.title,
            onConfirm = { viewModel.deleteConversation(conv) },
            onDismiss = { viewModel.setConversationToDelete(null) }
        )
    }
}
