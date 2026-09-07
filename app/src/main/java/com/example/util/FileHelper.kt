package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.data.db.ConversationEntity
import com.example.data.db.MessageEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileHelper {

    fun uriToBase64(context: Context, uri: Uri, maxDimension: Int = 1024): Pair<String, String>? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Scale down if larger than maxDimension to conserve payload size & token budget
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                if (ratio > 1) {
                    Pair(maxDimension, (maxDimension / ratio).toInt())
                } else {
                    Pair((maxDimension * ratio).toInt(), maxDimension)
                }
            } else {
                Pair(width, height)
            }

            val scaledBitmap = if (scale.first != width || scale.second != height) {
                Bitmap.createScaledBitmap(originalBitmap, scale.first, scale.second, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            val format = if (mimeType.contains("png", ignoreCase = true)) {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }
            scaledBitmap.compress(format, 85, outputStream)
            val bytes = outputStream.toByteArray()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            Pair(base64, mimeType)
        } catch (e: Exception) {
            null
        }
    }

    fun readTextFromUri(context: Context, uri: Uri, maxChars: Int = 50000): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val content = inputStream.bufferedReader().use { it.readText() }
            if (content.length > maxChars) {
                content.substring(0, maxChars) + "\n\n... [File content truncated for length]"
            } else {
                content
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getFileName(context: Context, uri: Uri): String {
        var name = "attachment"
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = it.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            name = uri.lastPathSegment ?: "attachment"
        }
        return name
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        return it.getLong(sizeIndex)
                    }
                }
            }
            0L
        } catch (e: Exception) {
            0L
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    fun readPdfAsBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.use { it.readBytes() }
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    fun estimateTokens(charCount: Int): Int = (charCount / 4).coerceAtLeast(1)

    fun shareText(context: Context, title: String, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, title)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share chat via NOVA AI")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun exportToText(conversation: ConversationEntity, messages: List<MessageEntity>): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("=== NOVA AI Conversation ===\n")
        sb.append("Title: ${conversation.title}\n")
        sb.append("Model: ${conversation.model}\n")
        sb.append("Exported: ${sdf.format(Date())}\n")
        sb.append("============================\n\n")

        for (msg in messages) {
            val role = if (msg.role == "user") "You" else "NOVA AI"
            val time = sdf.format(Date(msg.timestamp))
            sb.append("[$time] $role:\n")
            sb.append("${msg.content}\n\n")
        }
        return sb.toString()
    }

    fun exportToMarkdown(conversation: ConversationEntity, messages: List<MessageEntity>): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("# ${conversation.title}\n\n")
        sb.append("> **Model:** `${conversation.model}`  \n")
        sb.append("> **Exported from NOVA AI:** ${sdf.format(Date())}\n\n---\n\n")

        for (msg in messages) {
            val role = if (msg.role == "user") "### 👤 You" else "### ✨ NOVA AI"
            val time = sdf.format(Date(msg.timestamp))
            sb.append("$role *($time)*\n\n")
            sb.append("${msg.content}\n\n---\n\n")
        }
        return sb.toString()
    }

    fun exportToJson(conversation: ConversationEntity, messages: List<MessageEntity>): String {
        val root = JSONObject()
        root.put("app", "NOVA AI")
        root.put("conversationId", conversation.id)
        root.put("title", conversation.title)
        root.put("model", conversation.model)
        root.put("createdAt", conversation.createdAt)
        root.put("updatedAt", conversation.updatedAt)

        val msgsArray = JSONArray()
        for (msg in messages) {
            val m = JSONObject()
            m.put("id", msg.id)
            m.put("role", msg.role)
            m.put("content", msg.content)
            m.put("timestamp", msg.timestamp)
            m.put("model", msg.model)
            if (msg.attachmentName != null) {
                m.put("attachmentName", msg.attachmentName)
            }
            msgsArray.put(m)
        }
        root.put("messages", msgsArray)
        return root.toString(2)
    }
}
