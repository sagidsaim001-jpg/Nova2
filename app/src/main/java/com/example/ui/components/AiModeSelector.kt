package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiMode
import com.example.ui.theme.LocalNovaTokens

@Composable
fun AiModeSelector(
    currentMode: AiMode,
    onSelectMode: (AiMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalNovaTokens.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AiMode.entries.forEach { mode ->
            val isSelected = mode == currentMode
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) tokens.primary.copy(alpha = 0.18f) else tokens.surfaceElevated.copy(alpha = 0.5f),
                label = "mode_bg"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) tokens.primary else tokens.border.copy(alpha = 0.5f),
                label = "mode_border"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) tokens.primary else tokens.textSecondary,
                label = "mode_text"
            )

            val icon = getModeIcon(mode)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                    .clickable { onSelectMode(mode) }
                    .padding(horizontal = 11.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = mode.title,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = mode.title,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )

                if (isSelected) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(tokens.primary)
                    )
                }
            }
        }
    }
}

private fun getModeIcon(mode: AiMode): ImageVector {
    return when (mode) {
        AiMode.QUICK -> Icons.Rounded.Bolt
        AiMode.THINKING -> Icons.Rounded.Psychology
        AiMode.RESEARCH -> Icons.Rounded.TravelExplore
        AiMode.STUDY -> Icons.Rounded.MenuBook
        AiMode.CODING -> Icons.Rounded.Code
        AiMode.CREATIVE -> Icons.Rounded.AutoAwesome
    }
}
