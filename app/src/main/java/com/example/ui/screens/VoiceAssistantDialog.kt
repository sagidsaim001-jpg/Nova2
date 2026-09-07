package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.LocalNovaTokens
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.SpeechState
import com.example.util.TtsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class VoiceAssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

@Composable
fun VoiceAssistantDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val tokens = LocalNovaTokens.current
    val scope = rememberCoroutineScope()

    val speechState by viewModel.speechHelper.speechState.collectAsState()
    val speechRms by viewModel.speechHelper.rmsDb.collectAsState()
    val ttsState by viewModel.ttsHelper.ttsState.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    var assistantState by remember { mutableStateOf(VoiceAssistantState.IDLE) }
    var liveTranscript by remember { mutableStateOf("") }
    var aiSpokenResponse by remember { mutableStateOf("") }
    var speechRate by remember { mutableFloatStateOf(1.0f) }

    // Synchronize voice assistant state with system engines
    LaunchedEffect(speechState, ttsState, isGenerating) {
        assistantState = when {
            speechState == SpeechState.LISTENING -> VoiceAssistantState.LISTENING
            isGenerating -> VoiceAssistantState.THINKING
            ttsState == TtsState.SPEAKING -> VoiceAssistantState.SPEAKING
            else -> VoiceAssistantState.IDLE
        }
    }

    // Auto-start listening on launch
    LaunchedEffect(Unit) {
        delay(300)
        startListening(viewModel, onTranscript = { liveTranscript = it }, onFinal = { query ->
            liveTranscript = query
            if (query.isNotBlank()) {
                sendVoiceQuery(viewModel, query, speechRate) { reply ->
                    aiSpokenResponse = reply
                }
            }
        })
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.speechHelper.stopListening()
            viewModel.ttsHelper.stop()
        }
    }

    Dialog(
        onDismissRequest = {
            viewModel.speechHelper.stopListening()
            viewModel.ttsHelper.stop()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF07080F),
                            Color(0xFF0F1221),
                            Color(0xFF080A14)
                        )
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (assistantState) {
                                    VoiceAssistantState.LISTENING -> Color(0xFF10B981)
                                    VoiceAssistantState.THINKING -> Color(0xFFF59E0B)
                                    VoiceAssistantState.SPEAKING -> Color(0xFF8B5CF6)
                                    VoiceAssistantState.IDLE -> Color(0xFF6B7280)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NOVA LIVE ASSISTANT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.speechHelper.stopListening()
                        viewModel.ttsHelper.stop()
                        onDismiss()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Live Voice",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Center Content: Glowing Reactive Canvas Orb
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Interactive Reactive Orb
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            // Tap to interrupt / barge-in
                            if (assistantState == VoiceAssistantState.SPEAKING) {
                                viewModel.ttsHelper.stop()
                                startListening(viewModel, { liveTranscript = it }, { query ->
                                    liveTranscript = query
                                    if (query.isNotBlank()) {
                                        sendVoiceQuery(viewModel, query, speechRate) { reply ->
                                            aiSpokenResponse = reply
                                        }
                                    }
                                })
                            } else if (assistantState == VoiceAssistantState.LISTENING) {
                                viewModel.speechHelper.stopListening()
                            } else {
                                startListening(viewModel, { liveTranscript = it }, { query ->
                                    liveTranscript = query
                                    if (query.isNotBlank()) {
                                        sendVoiceQuery(viewModel, query, speechRate) { reply ->
                                            aiSpokenResponse = reply
                                        }
                                    }
                                })
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    ReactiveVoiceOrbCanvas(
                        state = assistantState,
                        volumeDb = speechRms,
                        primaryColor = tokens.primary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Status Indicator
                Text(
                    text = when (assistantState) {
                        VoiceAssistantState.LISTENING -> "Listening to you..."
                        VoiceAssistantState.THINKING -> "Nova is reasoning..."
                        VoiceAssistantState.SPEAKING -> "Speaking (Tap orb to interrupt)"
                        VoiceAssistantState.IDLE -> "Tap orb to speak"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live Transcript / Response Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141729).copy(alpha = 0.75f))
                        .border(1.dp, Color(0xFF282F4D), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val displayText = when {
                        assistantState == VoiceAssistantState.LISTENING && liveTranscript.isNotBlank() -> "\"$liveTranscript\""
                        assistantState == VoiceAssistantState.SPEAKING && aiSpokenResponse.isNotBlank() -> aiSpokenResponse.take(180) + "..."
                        assistantState == VoiceAssistantState.THINKING -> "Synthesizing answer with Gemini..."
                        else -> "Ask anything naturally. Speak your mind."
                    }

                    Text(
                        text = displayText,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center,
                        color = if (assistantState == VoiceAssistantState.LISTENING) Color(0xFF6EE7B7) else Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // Bottom Floating Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speech Rate Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1A1F36))
                        .clickable {
                            speechRate = when (speechRate) {
                                1.0f -> 1.25f
                                1.25f -> 1.5f
                                else -> 1.0f
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = "Speed",
                        tint = tokens.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${speechRate}x",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Center Mic Toggle Action
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            if (assistantState == VoiceAssistantState.LISTENING) Color(0xFFEF4444) else tokens.primary
                        )
                        .clickable {
                            if (assistantState == VoiceAssistantState.LISTENING) {
                                viewModel.speechHelper.stopListening()
                            } else if (assistantState == VoiceAssistantState.SPEAKING) {
                                viewModel.ttsHelper.stop()
                            } else {
                                startListening(viewModel, { liveTranscript = it }, { query ->
                                    liveTranscript = query
                                    if (query.isNotBlank()) {
                                        sendVoiceQuery(viewModel, query, speechRate) { reply ->
                                            aiSpokenResponse = reply
                                        }
                                    }
                                })
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (assistantState == VoiceAssistantState.LISTENING) Icons.Rounded.Stop else Icons.Rounded.Mic,
                        contentDescription = "Microphone",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Done / End Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1A1F36))
                        .clickable {
                            viewModel.speechHelper.stopListening()
                            viewModel.ttsHelper.stop()
                            onDismiss()
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "End",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReactiveVoiceOrbCanvas(
    state: VoiceAssistantState,
    volumeDb: Float,
    primaryColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = size.minDimension / 3.4f

        // Dynamic extra scale reacting to speech volume or state
        val dynamicExtra = when (state) {
            VoiceAssistantState.LISTENING -> (volumeDb / 10f).coerceIn(0f, 25f)
            VoiceAssistantState.SPEAKING -> 15f * pulseScale
            VoiceAssistantState.THINKING -> 8f
            VoiceAssistantState.IDLE -> 0f
        }

        val animatedRadius = (baseRadius + dynamicExtra) * (if (state == VoiceAssistantState.SPEAKING) pulseScale else 1f)

        // Outer Ambient Radial Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = if (state == VoiceAssistantState.THINKING) 0.35f else 0.25f),
                    primaryColor.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = center,
                radius = animatedRadius * 1.6f
            ),
            radius = animatedRadius * 1.6f,
            center = center
        )

        // Rotating Orbital Rings for Thinking / Active Mode
        if (state == VoiceAssistantState.THINKING || state == VoiceAssistantState.SPEAKING) {
            val ringCount = 3
            for (i in 0 until ringCount) {
                val phaseOffset = i * 45f
                val ringRadius = animatedRadius * (1.1f + i * 0.15f)
                val strokeColor = when (i) {
                    0 -> Color(0xFFEC4899).copy(alpha = 0.4f)
                    1 -> Color(0xFF8B5CF6).copy(alpha = 0.5f)
                    else -> Color(0xFF38BDF8).copy(alpha = 0.3f)
                }

                drawCircle(
                    color = strokeColor,
                    radius = ringRadius,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        // Inner Core Spherical Gradient
        val coreGradient = when (state) {
            VoiceAssistantState.LISTENING -> listOf(
                Color(0xFF34D399),
                Color(0xFF059669),
                Color(0xFF064E3B)
            )
            VoiceAssistantState.THINKING -> listOf(
                Color(0xFFFBBF24),
                Color(0xFFD97706),
                Color(0xFF78350F)
            )
            VoiceAssistantState.SPEAKING -> listOf(
                Color(0xFFA78BFA),
                Color(0xFF7C3AED),
                Color(0xFF4C1D95)
            )
            VoiceAssistantState.IDLE -> listOf(
                primaryColor.copy(alpha = 0.9f),
                Color(0xFF4338CA),
                Color(0xFF1E1B4B)
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = coreGradient,
                center = center.copy(y = center.y - animatedRadius * 0.2f),
                radius = animatedRadius
            ),
            radius = animatedRadius,
            center = center
        )

        // Highlighting Star Core Sparkle
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = (animatedRadius * 0.12f),
            center = Offset(center.x - animatedRadius * 0.25f, center.y - animatedRadius * 0.25f)
        )
    }
}

private fun startListening(
    viewModel: ChatViewModel,
    onTranscript: (String) -> Unit,
    onFinal: (String) -> Unit
) {
    viewModel.speechHelper.startListening(
        onPartial = { partial ->
            onTranscript(partial)
        },
        onResult = { text ->
            onFinal(text)
        },
        onError = {
            // handle error
        }
    )
}

private fun sendVoiceQuery(
    viewModel: ChatViewModel,
    prompt: String,
    speechRate: Float,
    onAiReply: (String) -> Unit
) {
    viewModel.sendMessage(prompt)

    // Collect newest assistant answer and speak aloud
    viewModel.speakLastAssistantResponse(speechRate) { reply ->
        onAiReply(reply)
    }
}
