package com.example.data.repository

import com.example.data.db.ConversationDao
import com.example.data.db.ConversationEntity
import com.example.data.db.MessageDao
import com.example.data.db.MessageEntity
import com.example.data.db.SearchResult
import com.example.data.db.UserAccountDao
import com.example.data.db.UserAccountEntity
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class ChatRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val userAccountDao: UserAccountDao
) {
    fun getActiveConversations(userId: String): Flow<List<ConversationEntity>> =
        conversationDao.getActiveConversations(userId)

    fun getFavoriteConversations(userId: String): Flow<List<ConversationEntity>> =
        conversationDao.getFavoriteConversations(userId)

    fun getArchivedConversations(userId: String): Flow<List<ConversationEntity>> =
        conversationDao.getArchivedConversations(userId)

    fun getConversationFlow(id: String): Flow<ConversationEntity?> =
        conversationDao.getConversationFlow(id)

    suspend fun getConversationById(id: String): ConversationEntity? =
        conversationDao.getConversationById(id)

    suspend fun createOrUpdateConversation(conversation: ConversationEntity) {
        conversationDao.insertConversation(conversation)
    }

    suspend fun renameConversation(id: String, newTitle: String) {
        conversationDao.renameConversation(id, newTitle, System.currentTimeMillis())
    }

    suspend fun setFavorite(id: String, isFavorite: Boolean) {
        conversationDao.setFavorite(id, isFavorite)
    }

    suspend fun setArchived(id: String, isArchived: Boolean) {
        conversationDao.setArchived(id, isArchived)
    }

    suspend fun updateDraft(id: String, draft: String) {
        conversationDao.updateDraft(id, draft)
    }

    suspend fun updateModel(id: String, model: String) {
        conversationDao.updateModel(id, model)
    }

    suspend fun deleteConversation(id: String) {
        messageDao.deleteMessagesForConversation(id)
        conversationDao.deleteConversationById(id)
    }

    suspend fun deleteAllConversations(userId: String) {
        val conversations = conversationDao.getConversationById(userId)
        conversationDao.deleteAllConversations(userId)
    }

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)

    suspend fun getMessagesList(conversationId: String): List<MessageEntity> =
        messageDao.getMessagesList(conversationId)

    suspend fun insertMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    suspend fun updateMessage(message: MessageEntity) {
        messageDao.updateMessage(message)
    }

    suspend fun updateMessageContent(id: String, content: String, isError: Boolean = false) {
        messageDao.updateMessageContent(id, content, isError)
    }

    suspend fun setMessageFeedback(id: String, feedback: Int) {
        messageDao.setMessageFeedback(id, feedback)
    }

    suspend fun deleteMessage(id: String) {
        messageDao.deleteMessageById(id)
    }

    suspend fun deleteMessagesFromTimestamp(conversationId: String, timestamp: Long) {
        messageDao.deleteMessagesFromTimestamp(conversationId, timestamp)
    }

    suspend fun search(userId: String, query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()
        return messageDao.searchMessages(userId, query.trim())
    }

    // User Account Methods
    suspend fun registerUser(name: String, email: String, passwordPlain: String): UserAccountEntity {
        val hash = hashPassword(passwordPlain)
        val user = UserAccountEntity(
            name = name.trim(),
            email = email.trim().lowercase(),
            passwordHash = hash,
            avatarColor = (email.hashCode() and 0x7FFFFFFF) % 6
        )
        userAccountDao.insertUser(user)
        return user
    }

    suspend fun loginUser(email: String, passwordPlain: String): UserAccountEntity? {
        val user = userAccountDao.getUserByEmail(email.trim().lowercase()) ?: return null
        val hash = hashPassword(passwordPlain)
        return if (user.passwordHash == hash) user else null
    }

    suspend fun getUserById(id: String): UserAccountEntity? = userAccountDao.getUserById(id)

    suspend fun deleteUser(id: String) {
        userAccountDao.deleteUserById(id)
        conversationDao.deleteAllConversations(id)
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
