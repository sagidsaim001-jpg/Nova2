package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "conversations", indices = [Index(value = ["userId", "updatedAt"])])
data class ConversationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "guest",
    val title: String = "New Chat",
    val model: String = "gemini-3.5-flash",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val systemInstruction: String = "",
    val draft: String = ""
)

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversationId", "timestamp"]),
        Index(value = ["conversationId"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String = "",
    val attachmentUri: String? = null,
    val attachmentMimeType: String? = null,
    val attachmentName: String? = null,
    val isError: Boolean = false,
    val feedback: Int = 0 // 0 = none, 1 = thumb up, -1 = thumb down
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val passwordHash: String,
    val avatarColor: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class SearchResult(
    val conversationId: String,
    val conversationTitle: String,
    val messageSnippet: String,
    val timestamp: Long
)
