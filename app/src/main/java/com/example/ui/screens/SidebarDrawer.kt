package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ConversationEntity
import com.example.data.db.UserAccountEntity
import com.example.ui.components.NovaLogo
import com.example.ui.theme.LocalNovaTokens
import com.example.util.TimeFormatter

enum class DrawerTab {
    RECENT, FAVORITES, ARCHIVED
}

@Composable
fun SidebarDrawer(
    activeConversations: List<ConversationEntity>,
    favoriteConversations: List<ConversationEntity>,
    archivedConversations: List<ConversationEntity>,
    currentConversation: ConversationEntity?,
    currentUser: UserAccountEntity?,
    onSelectConversation: (ConversationEntity) -> Unit,
    onNewChat: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenApiSettings: () -> Unit,
    onOpenAuth: () -> Unit,
    onRenameConversation: (ConversationEntity) -> Unit,
    onToggleFavorite: (ConversationEntity) -> Unit,
    onToggleArchive: (ConversationEntity) -> Unit,
    onDeleteConversation: (ConversationEntity) -> Unit,
    onExportChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalNovaTokens.current
    var selectedTab by remember { mutableStateOf(DrawerTab.RECENT) }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(310.dp),
        color = tokens.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 40.dp, bottom = 16.dp, start = 14.dp, end = 14.dp)
        ) {
            // App Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NovaLogo(size = 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "NOVA AI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "Think. Create. Discover.",
                            fontSize = 11.sp,
                            color = tokens.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.surfaceElevated)
                    .clickable(onClick = onOpenAuth)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(tokens.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentUser != null) currentUser.name.take(1).uppercase() else "G",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser?.name ?: "Guest Explorer",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentUser?.email ?: "Tap to sign in",
                        fontSize = 11.sp,
                        color = tokens.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // + New Chat Button
            Button(
                onClick = onNewChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tokens.primary)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "New Chat",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tokens.surfaceElevated)
                    .border(1.dp, tokens.border, RoundedCornerShape(10.dp))
                    .clickable(onClick = onOpenSearch)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = tokens.textMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Search conversations...",
                    fontSize = 12.sp,
                    color = tokens.textMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs Row: Recent, Favorites, Archived
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(tokens.surfaceElevated)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DrawerTabButton(
                    title = "Recent (${activeConversations.size})",
                    isSelected = selectedTab == DrawerTab.RECENT,
                    onClick = { selectedTab = DrawerTab.RECENT },
                    modifier = Modifier.weight(1f)
                )
                DrawerTabButton(
                    title = "★ (${favoriteConversations.size})",
                    isSelected = selectedTab == DrawerTab.FAVORITES,
                    onClick = { selectedTab = DrawerTab.FAVORITES },
                    modifier = Modifier.weight(0.7f)
                )
                DrawerTabButton(
                    title = "Archive (${archivedConversations.size})",
                    isSelected = selectedTab == DrawerTab.ARCHIVED,
                    onClick = { selectedTab = DrawerTab.ARCHIVED },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Conversation List
            val displayList = when (selectedTab) {
                DrawerTab.RECENT -> activeConversations
                DrawerTab.FAVORITES -> favoriteConversations
                DrawerTab.ARCHIVED -> archivedConversations
            }

            if (displayList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (selectedTab) {
                            DrawerTab.RECENT -> "No conversations yet."
                            DrawerTab.FAVORITES -> "No favorited chats."
                            DrawerTab.ARCHIVED -> "No archived chats."
                        },
                        fontSize = 13.sp,
                        color = tokens.textMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(displayList, key = { it.id }) { conv ->
                        ConversationItemRow(
                            conversation = conv,
                            isSelected = conv.id == currentConversation?.id,
                            onSelect = { onSelectConversation(conv) },
                            onRename = { onRenameConversation(conv) },
                            onToggleFavorite = { onToggleFavorite(conv) },
                            onToggleArchive = { onToggleArchive(conv) },
                            onDelete = { onDeleteConversation(conv) },
                            onExport = onExportChat
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = tokens.border
            )

            // Bottom Actions: Settings & Connect API
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenApiSettings,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Key,
                        contentDescription = null,
                        tint = tokens.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("API", fontSize = 12.sp, color = tokens.textPrimary)
                }

                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = tokens.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Settings", fontSize = 12.sp, color = tokens.textPrimary)
                }
            }
        }
    }
}

@Composable
private fun DrawerTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalNovaTokens.current
    Box(
        modifier = modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) tokens.primary else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else tokens.textSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun ConversationItemRow(
    conversation: ConversationEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onRename: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit
) {
    val tokens = LocalNovaTokens.current
    var isMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) tokens.primary.copy(alpha = 0.15f) else Color.Transparent
            )
            .border(
                1.dp,
                if (isSelected) tokens.primary.copy(alpha = 0.35f) else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (conversation.isFavorite) Icons.Rounded.Star else Icons.Rounded.ChatBubbleOutline,
            contentDescription = null,
            tint = if (conversation.isFavorite) Color(0xFFF59E0B) else if (isSelected) tokens.primary else tokens.textMuted,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) tokens.primary else tokens.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = TimeFormatter.formatConversationDate(conversation.updatedAt),
                fontSize = 10.sp,
                color = tokens.textMuted
            )
        }

        Box {
            IconButton(
                onClick = { isMenuExpanded = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Options",
                    tint = tokens.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (conversation.isFavorite) "Remove from Favorites" else "Add to Favorites") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (conversation.isFavorite) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        isMenuExpanded = false
                        onToggleFavorite()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = { Icon(Icons.Rounded.DriveFileRenameOutline, contentDescription = null) },
                    onClick = {
                        isMenuExpanded = false
                        onRename()
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (conversation.isArchived) "Unarchive" else "Archive") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (conversation.isArchived) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        isMenuExpanded = false
                        onToggleArchive()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Export / Share") },
                    leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null) },
                    onClick = {
                        isMenuExpanded = false
                        onExport()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color(0xFFEF4444)) },
                    leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444)) },
                    onClick = {
                        isMenuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}
