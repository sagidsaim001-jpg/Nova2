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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.api.ApiConnectionState
import com.example.data.api.ApiTestResult
import com.example.ui.components.StatusPill
import com.example.ui.theme.LocalNovaTokens

@Composable
fun ApiSettingsDialog(
    currentApiKey: String,
    isSystemKeyConfigured: Boolean = false,
    maskedSystemKey: String? = null,
    connectionState: ApiConnectionState,
    testResult: ApiTestResult?,
    onSaveKey: (String) -> Unit,
    onTestKey: (String?) -> Unit,
    onRemoveKey: () -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalNovaTokens.current
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current

    var keyInput by remember { mutableStateOf(currentApiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var showHelpGuide by remember { mutableStateOf(false) }
    var copiedFeedback by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(tokens.surface)
                .border(1.dp, tokens.border, RoundedCornerShape(24.dp)),
            color = tokens.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(tokens.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Key,
                                contentDescription = null,
                                tint = tokens.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Connect Gemini",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary
                            )
                            Text(
                                text = "Real Google Gemini API Integration",
                                fontSize = 12.sp,
                                color = tokens.textSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = tokens.textSecondary
                        )
                    }
                }

                // Current Connection Status Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(tokens.surfaceElevated)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Connection Status",
                            fontSize = 11.sp,
                            color = tokens.textSecondary
                        )
                        Text(
                            text = when (connectionState) {
                                ApiConnectionState.CONNECTED -> "Online & Operational"
                                ApiConnectionState.CONNECTING -> "Verifying live connection..."
                                ApiConnectionState.INVALID_KEY -> "Authentication Failed (401)"
                                ApiConnectionState.PERMISSION_DENIED -> "Permission Denied (403)"
                                ApiConnectionState.MODEL_NOT_FOUND -> "Model Not Found (404)"
                                ApiConnectionState.RATE_LIMITED -> "Rate Limit Exceeded (429)"
                                ApiConnectionState.SERVER_ERROR -> "Google Gemini Server Issue"
                                ApiConnectionState.NETWORK_ERROR -> "Network Unreachable"
                                ApiConnectionState.DISCONNECTED -> "No API Key Configured"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.textPrimary
                        )
                    }

                    StatusPill(
                        state = connectionState,
                        onClick = { onTestKey(keyInput.ifBlank { null }) }
                    )
                }

                // System Pre-configured Key Notice (if present)
                if (isSystemKeyConfigured && keyInput.isBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.primary.copy(alpha = 0.08f))
                            .border(1.dp, tokens.primary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Build Key Active: ${maskedSystemKey ?: "Configured"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = tokens.primary
                            )
                            Text(
                                text = "Ready to chat out of the box! You can override it with your personal key below.",
                                fontSize = 11.sp,
                                color = tokens.textSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // API Key Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Google AI Studio API Key",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = tokens.textPrimary
                    )

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input"),
                        placeholder = {
                            Text(
                                text = "AIzaSy... or AQ....",
                                color = tokens.textMuted,
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            onSaveKey(keyInput)
                        }),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (isKeyVisible) "Hide Key" else "Show Key",
                                    tint = tokens.textSecondary
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = tokens.primary,
                            unfocusedBorderColor = tokens.border,
                            focusedTextColor = tokens.textPrimary,
                            unfocusedTextColor = tokens.textPrimary
                        )
                    )

                    Text(
                        text = "Supports all Google AI Studio formats: standard 'AIzaSy...' and newer 'AQ....' keys. Stored securely on device.",
                        fontSize = 11.sp,
                        color = tokens.textMuted,
                        lineHeight = 15.sp
                    )
                }

                // Detailed Test Result Feedback
                testResult?.let { res ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (res.isSuccess) Color(0x1510B981) else Color(0x15EF4444))
                            .border(1.dp, if (res.isSuccess) Color(0x4010B981) else Color(0x40EF4444), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (res.isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                                    contentDescription = null,
                                    tint = if (res.isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (res.isSuccess) "Real Gemini Test: Success" else "Gemini API Test: Error",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (res.isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (res.latencyMs != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Speed,
                                            contentDescription = null,
                                            tint = tokens.textSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${res.latencyMs}ms",
                                            fontSize = 11.sp,
                                            color = tokens.textSecondary
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(res.message))
                                        copiedFeedback = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "Copy diagnostic",
                                        tint = tokens.textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = res.message,
                            fontSize = 12.sp,
                            color = if (res.isSuccess) (if (tokens.isDark) Color(0xFFA7F3D0) else Color(0xFF065F46)) else (if (tokens.isDark) Color(0xFFFECDD3) else Color(0xFF991B1B)),
                            lineHeight = 16.sp
                        )

                        if (copiedFeedback) {
                            Text(
                                text = "✓ Diagnostic copied to clipboard",
                                fontSize = 11.sp,
                                color = tokens.primary
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            onTestKey(keyInput.ifBlank { null })
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("test_api_key_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = tokens.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Test Connection",
                            color = tokens.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onSaveKey(keyInput)
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(46.dp)
                            .testTag("save_api_key_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.primary)
                    ) {
                        Text(
                            text = "Save Key",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (currentApiKey.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            keyInput = ""
                            onRemoveKey()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x50EF4444))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Remove Custom Key", fontSize = 13.sp)
                    }
                }

                // Step by Step Help Guide Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showHelpGuide = !showHelpGuide }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.HelpOutline,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How to get a Gemini API key",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = tokens.primary
                        )
                    }
                    Text(
                        text = if (showHelpGuide) "Hide" else "Show",
                        fontSize = 12.sp,
                        color = tokens.textSecondary
                    )
                }

                AnimatedVisibility(visible = showHelpGuide) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.surfaceElevated)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Quick Instructions:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = tokens.textPrimary
                        )
                        HelpStepRow(step = "1", text = "Open Google AI Studio (aistudio.google.com)")
                        HelpStepRow(step = "2", text = "Sign in with your Google account")
                        HelpStepRow(step = "3", text = "Click 'Get API key' -> 'Create API key'")
                        HelpStepRow(step = "4", text = "Copy the key (starts with AIzaSy... or AQ....), return here, and tap 'Save Key'")
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpStepRow(step: String, text: String) {
    val tokens = LocalNovaTokens.current
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(tokens.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = tokens.primary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = tokens.textSecondary,
            lineHeight = 16.sp
        )
    }
}
