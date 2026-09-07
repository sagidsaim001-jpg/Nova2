package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ApiConnectionState
import com.example.ui.theme.LocalNovaTokens

@Composable
fun StatusPill(
    state: ApiConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalNovaTokens.current

    val (dotColor, text, bgColor, borderColor) = when (state) {
        ApiConnectionState.CONNECTED -> Tuple4(
            Color(0xFF10B981), // Emerald Green
            "Connected",
            Color(0x1F10B981),
            Color(0x4D10B981)
        )
        ApiConnectionState.CONNECTING -> Tuple4(
            Color(0xFFF59E0B), // Amber
            "Testing...",
            Color(0x1FF59E0B),
            Color(0x4DF59E0B)
        )
        ApiConnectionState.INVALID_KEY -> Tuple4(
            Color(0xFFEF4444), // Red
            "Auth Error (401)",
            Color(0x1FEF4444),
            Color(0x4DEF4444)
        )
        ApiConnectionState.PERMISSION_DENIED -> Tuple4(
            Color(0xFFF43F5E), // Rose
            "Permission (403)",
            Color(0x1FF43F5E),
            Color(0x4DF43F5E)
        )
        ApiConnectionState.MODEL_NOT_FOUND -> Tuple4(
            Color(0xFFF97316), // Orange
            "Model 404",
            Color(0x1FF97316),
            Color(0x4DF97316)
        )
        ApiConnectionState.RATE_LIMITED -> Tuple4(
            Color(0xFFF59E0B), // Amber/Orange
            "Quota (429)",
            Color(0x1FF59E0B),
            Color(0x4DF59E0B)
        )
        ApiConnectionState.SERVER_ERROR -> Tuple4(
            Color(0xFFE11D48), // Crimson
            "Server Issue",
            Color(0x1FE11D48),
            Color(0x4DE11D48)
        )
        ApiConnectionState.NETWORK_ERROR -> Tuple4(
            Color(0xFF64748B), // Slate
            "Offline / Network",
            Color(0x1F64748B),
            Color(0x4D64748B)
        )
        ApiConnectionState.DISCONNECTED -> Tuple4(
            Color(0xFF94A3B8), // Gray
            "No Key",
            if (tokens.isDark) Color(0x22334155) else Color(0x1A64748B),
            tokens.border.copy(alpha = 0.5f)
        )
    }

    val animatedDotColor by animateColorAsState(targetValue = dotColor, animationSpec = tween(400), label = "dot_color")

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(animatedDotColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (tokens.isDark) tokens.textPrimary else Color(0xFF1E293B)
        )
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
