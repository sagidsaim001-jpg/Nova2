package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE userId = :userId AND isArchived = 0 ORDER BY updatedAt DESC")
    fun getActiveConversations(userId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE userId = :userId AND isFavorite = 1 AND isArchived = 0 ORDER BY updatedAt DESC")
    fun getFavoriteConversations(userId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE userId = :userId AND isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedConversations(userId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    fun getConversationFlow(id: String): Flow<ConversationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET title = :newTitle, updatedAt = :updatedAt WHERE id = :id")
    suspend fun renameConversation(id: String, newTitle: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE conversations SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE conversations SET isArchived = :isArchived WHERE id = :id")
    suspend fun setArchived(id: String, isArchived: Boolean)

    @Query("UPDATE conversations SET draft = :draft WHERE id = :id")
    suspend fun updateDraft(id: String, draft: String)

    @Query("UPDATE conversations SET model = :model WHERE id = :id")
    suspend fun updateModel(id: String, model: String)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: String)

    @Query("DELETE FROM conversations WHERE userId = :userId")
    suspend fun deleteAllConversations(userId: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesList(conversationId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET content = :newContent, isError = :isError WHERE id = :id")
    suspend fun updateMessageContent(id: String, newContent: String, isError: Boolean = false)

    @Query("UPDATE messages SET feedback = :feedback WHERE id = :id")
    suspend fun setMessageFeedback(id: String, feedback: Int)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId AND timestamp >= :timestamp")
    suspend fun deleteMessagesFromTimestamp(conversationId: String, timestamp: Long)

    @Query("""
        SELECT c.id AS conversationId, c.title AS conversationTitle, m.content AS messageSnippet, m.timestamp AS timestamp
        FROM messages m
        INNER JOIN conversations c ON m.conversationId = c.id
        WHERE c.userId = :userId AND (m.content LIKE '%' || :query || '%' OR c.title LIKE '%' || :query || '%')
        ORDER BY m.timestamp DESC
        LIMIT 50
    """)
    suspend fun searchMessages(userId: String, query: String): List<SearchResult>
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity)

    @Query("DELETE FROM user_accounts WHERE id = :id")
    suspend fun deleteUserById(id: String)
}
