package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalNovaTokens
import kotlinx.coroutines.delay

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    isUser: Boolean = false,
    fontSize: Int = 15
) {
    val tokens = LocalNovaTokens.current
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.Heading -> {
                    val size = when (block.level) {
                        1 -> 20.sp
                        2 -> 18.sp
                        else -> 16.sp
                    }
                    Text(
                        text = buildAnnotatedStringWithFormatting(block.text, isUser, tokens.textPrimary),
                        fontSize = size,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) Color.White else tokens.textPrimary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = buildAnnotatedStringWithFormatting(block.text, isUser, tokens.textPrimary),
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize + 7).sp,
                        color = if (isUser) Color.White else tokens.textPrimary
                    )
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            fontWeight = FontWeight.Bold,
                            color = if (isUser) Color.White else tokens.primary,
                            fontSize = fontSize.sp
                        )
                        Text(
                            text = buildAnnotatedStringWithFormatting(block.text, isUser, tokens.textPrimary),
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize + 6).sp,
                            color = if (isUser) Color.White else tokens.textPrimary
                        )
                    }
                }
                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}. ",
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUser) Color.White else tokens.primary,
                            fontSize = fontSize.sp
                        )
                        Text(
                            text = buildAnnotatedStringWithFormatting(block.text, isUser, tokens.textPrimary),
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize + 6).sp,
                            color = if (isUser) Color.White else tokens.textPrimary
                        )
                    }
                }
                is MarkdownBlock.BlockQuote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tokens.primary.copy(alpha = 0.08f))
                            .border(1.dp, tokens.primary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(tokens.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buildAnnotatedStringWithFormatting(block.text, isUser, tokens.textPrimary),
                            fontSize = fontSize.sp,
                            fontStyle = FontStyle.Italic,
                            color = if (isUser) Color.White.copy(alpha = 0.9f) else tokens.textSecondary
                        )
                    }
                }
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockView(
                        language = block.language,
                        code = block.code
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlockView(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalNovaTokens.current
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (tokens.isDark) Color(0xFF0B0C12) else Color(0xFF1E2130))
            .border(1.dp, if (tokens.isDark) tokens.border.copy(alpha = 0.6f) else Color(0xFF333850), RoundedCornerShape(12.dp))
    ) {
        // Code Block Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (tokens.isDark) Color(0xFF131520) else Color(0xFF262B3E))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language.isBlank()) "code" else language.lowercase(),
                color = Color(0xFFA0A8C0),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("NOVA Code", code)
                        clipboard.setPrimaryClip(clip)
                        isCopied = true
                    }
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                    contentDescription = "Copy code",
                    tint = if (isCopied) Color(0xFF10B981) else Color(0xFF9AA2BA),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isCopied) "Copied ✓" else "Copy",
                    color = if (isCopied) Color(0xFF10B981) else Color(0xFF9AA2BA),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Code Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code,
                color = Color(0xFFE2E8F0),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

private sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock()
    data class BlockQuote(val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
}

private fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = text.split("\n")
    var inCodeBlock = false
    var codeLang = ""
    val codeContent = StringBuilder()

    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            if (!inCodeBlock) {
                inCodeBlock = true
                codeLang = trimmed.removePrefix("```").trim()
                codeContent.clear()
            } else {
                inCodeBlock = false
                blocks.add(MarkdownBlock.CodeBlock(codeLang, codeContent.toString().trimEnd()))
                codeContent.clear()
                codeLang = ""
            }
            i++
            continue
        }

        if (inCodeBlock) {
            codeContent.append(line).append("\n")
            i++
            continue
        }

        if (trimmed.isEmpty()) {
            i++
            continue
        }

        when {
            trimmed.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Heading(1, trimmed.removePrefix("# ").trim()))
            }
            trimmed.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Heading(2, trimmed.removePrefix("## ").trim()))
            }
            trimmed.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Heading(3, trimmed.removePrefix("### ").trim()))
            }
            trimmed.startsWith("> ") -> {
                blocks.add(MarkdownBlock.BlockQuote(trimmed.removePrefix("> ").trim()))
            }
            trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                blocks.add(MarkdownBlock.BulletItem(trimmed.substring(2).trim()))
            }
            trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                val num = trimmed.substringBefore(".")
                val content = trimmed.substringAfter(". ").trim()
                blocks.add(MarkdownBlock.NumberedItem(num, content))
            }
            else -> {
                blocks.add(MarkdownBlock.Paragraph(line))
            }
        }
        i++
    }

    if (inCodeBlock && codeContent.isNotEmpty()) {
        blocks.add(MarkdownBlock.CodeBlock(codeLang, codeContent.toString().trimEnd()))
    }

    return blocks
}

private fun buildAnnotatedStringWithFormatting(text: String, isUser: Boolean, defaultTextColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val pattern = Regex("(\\*{1,3}|_{1,2}|`)(.*?)\\1")
        val matches = pattern.findAll(text)

        for (match in matches) {
            if (match.range.first > cursor) {
                append(text.substring(cursor, match.range.first))
            }

            val delimiter = match.groupValues[1]
            val content = match.groupValues[2]

            when (delimiter) {
                "***" -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                        append(content)
                    }
                }
                "**", "__" -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(content)
                    }
                }
                "*", "_" -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(content)
                    }
                }
                "`" -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = if (isUser) Color(0x33FFFFFF) else Color(0x228B5CF6),
                            color = if (isUser) Color.White else Color(0xFF9333EA)
                        )
                    ) {
                        append(" $content ")
                    }
                }
                else -> append(match.value)
            }

            cursor = match.range.last + 1
        }

        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}
