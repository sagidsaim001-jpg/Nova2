package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

enum class TtsState {
    IDLE, INITIALIZING, SPEAKING, ERROR
}

class TtsHelper(context: Context) {
    private val TAG = "NOVA_TTS"
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _ttsState = MutableStateFlow(TtsState.IDLE)
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentUtteranceId = MutableStateFlow<String?>(null)
    val currentUtteranceId: StateFlow<String?> = _currentUtteranceId.asStateFlow()

    private var onDoneCallback: (() -> Unit)? = null

    init {
        _ttsState.value = TtsState.INITIALIZING
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    val result = engine.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(TAG, "Default US English language missing data or not supported")
                        engine.language = Locale.getDefault()
                    }
                    isInitialized = true
                    _ttsState.value = TtsState.IDLE
                    _isSpeaking.value = false
                    setupListener()
                }
            } else {
                Log.e(TAG, "TextToSpeech init failed with status: $status")
                _ttsState.value = TtsState.ERROR
                _isSpeaking.value = false
            }
        }
    }

    private fun setupListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _ttsState.value = TtsState.SPEAKING
                _isSpeaking.value = true
                _currentUtteranceId.value = utteranceId
            }

            override fun onDone(utteranceId: String?) {
                _ttsState.value = TtsState.IDLE
                _isSpeaking.value = false
                _currentUtteranceId.value = null
                onDoneCallback?.invoke()
                onDoneCallback = null
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _ttsState.value = TtsState.IDLE
                _isSpeaking.value = false
                _currentUtteranceId.value = null
                onDoneCallback = null
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.w(TAG, "TTS Utterance error code: $errorCode")
                _ttsState.value = TtsState.IDLE
                _isSpeaking.value = false
                _currentUtteranceId.value = null
                onDoneCallback = null
            }
        })
    }

    fun speak(
        text: String,
        speechRate: Float = 1.0f,
        speechPitch: Float = 1.0f,
        onDone: (() -> Unit)? = null
    ) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS speak requested before initialized")
            return
        }

        stop()
        this.onDoneCallback = onDone

        val cleanText = sanitizeMarkdownForSpeech(text)
        if (cleanText.isBlank()) return

        tts?.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
        tts?.setPitch(speechPitch.coerceIn(0.5f, 2.0f))

        val utteranceId = UUID.randomUUID().toString()
        _currentUtteranceId.value = utteranceId
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            if (tts?.isSpeaking == true) {
                tts?.stop()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping TTS: ${e.message}")
        }
        _ttsState.value = TtsState.IDLE
        _currentUtteranceId.value = null
        onDoneCallback = null
    }

    fun isSpeaking(): Boolean = _ttsState.value == TtsState.SPEAKING

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // ignore
        }
        tts = null
        isInitialized = false
    }

    private fun sanitizeMarkdownForSpeech(markdown: String): String {
        return markdown
            // Remove code blocks
            .replace(Regex("```[\\s\\S]*?```"), " [Code block omitted] ")
            // Remove inline code
            .replace(Regex("`([^`]+)`"), "$1")
            // Remove thinking tags
            .replace(Regex("<thought>[\\s\\S]*?</thought>"), "")
            .replace(Regex("<think>[\\s\\S]*?</think>"), "")
            // Remove image links
            .replace(Regex("!\\[.*?\\]\\(.*?\\)"), "")
            // Simplify markdown links to anchor text
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            // Remove formatting marks like bold/italic asterisks, hashes, underscores
            .replace(Regex("[#*_~>]+"), " ")
            // Replace multiple newlines/spaces with single space
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
