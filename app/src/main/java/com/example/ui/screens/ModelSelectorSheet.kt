package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ApiTestResult
import com.example.data.api.ModelCapabilities
import com.example.data.api.ModelRegistry
import com.example.ui.theme.LocalNovaTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorSheet(
    currentModel: String,
    onSelectModel: (String) -> Unit,
    onVerifyModel: suspend (String) -> ApiTestResult,
    onDismiss: () -> Unit
) {
    val tokens = LocalNovaTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var verifyingModelId by remember { mutableStateOf<String?>(null) }
    var verificationResults by remember { mutableStateOf<Map<String, ApiTestResult>>(emptyMap()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Gemini Models",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary
                    )
                    Text(
                        text = "Select model engine & inspect live capabilities",
                        fontSize = 12.sp,
                        color = tokens.textSecondary
                    )
                }

                IconButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = tokens.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = tokens.border.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ModelRegistry.MODELS) { model ->
                    val isSelected = model.id == currentModel || model.id == currentModel.removePrefix("models/")
                    val isVerifying = verifyingModelId == model.id
                    val testResult = verificationResults[model.id]

                    ModelCard(
                        model = model,
                        isSelected = isSelected,
                        isVerifying = isVerifying,
                        testResult = testResult,
                        onSelect = {
                            onSelectModel(model.id)
                            scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                        },
                        onVerify = {
                            scope.launch {
                                verifyingModelId = model.id
                                val res = onVerifyModel(model.id)
                                verificationResults = verificationResults + (model.id to res)
                                verifyingModelId = null
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ModelCard(
    model: ModelCapabilities,
    isSelected: Boolean,
    isVerifying: Boolean,
    testResult: ApiTestResult?,
    onSelect: () -> Unit,
    onVerify: () -> Unit
) {
    val tokens = LocalNovaTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) tokens.primary.copy(alpha = 0.08f) else tokens.surfaceElevated.copy(alpha = 0.4f))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) tokens.primary else tokens.border.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .padding(14.dp)
    ) {
        // Title row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = model.displayName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tokens.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = model.badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.primary
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(tokens.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = model.description,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = tokens.textSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Capability badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CapabilityChip(icon = Icons.Rounded.Hub, label = model.contextWindow)
            if (model.supportsThinking) {
                CapabilityChip(icon = Icons.Rounded.Psychology, label = "Thinking")
            }
            if (model.supportsVision) {
                CapabilityChip(icon = Icons.Rounded.Visibility, label = "Vision")
            }
            if (model.supportsSearchGrounding) {
                CapabilityChip(icon = Icons.Rounded.TravelExplore, label = "Search")
            }
            if (model.supportsCodeExecution) {
                CapabilityChip(icon = Icons.Rounded.Code, label = "Code")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actions & Verification Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScoreBar(label = "Speed", score = model.speedScore)
                ScoreBar(label = "Reasoning", score = model.reasoningScore)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (testResult != null) {
                    Text(
                        text = if (testResult.isSuccess) "Active (${testResult.latencyMs}ms)" else "Error",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (testResult.isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                OutlinedButton(
                    onClick = onVerify,
                    enabled = !isVerifying,
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(12.dp),
                            color = tokens.primary
                        )
                    } else {
                        Text(
                            text = if (testResult != null) "Re-test" else "Verify",
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CapabilityChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    val tokens = LocalNovaTokens.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tokens.surfaceElevated.copy(alpha = 0.6f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tokens.textSecondary,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = tokens.textSecondary
        )
    }
}

@Composable
private fun ScoreBar(label: String, score: Int) {
    val tokens = LocalNovaTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label:",
            fontSize = 10.5.sp,
            color = tokens.textSecondary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 1..5) {
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 9.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            if (i <= score) tokens.primary else tokens.border.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}
