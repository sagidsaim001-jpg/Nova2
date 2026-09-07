package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class SpeechState {
    IDLE, LISTENING, PROCESSING, ERROR, UNAVAILABLE
}

class SpeechHelper(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _speechState = MutableStateFlow(SpeechState.IDLE)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private var onPartialCallback: ((String) -> Unit)? = null
    private var onResultCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(
        languageCode: String = Locale.getDefault().language,
        onPartial: ((String) -> Unit)? = null,
        onResult: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (!isAvailable()) {
            _speechState.value = SpeechState.UNAVAILABLE
            _errorMessage.value = "Speech recognition is not supported on this device."
            onError?.invoke("Speech recognition is not supported on this device.")
            return
        }

        stopListening()
        this.onPartialCallback = onPartial
        this.onResultCallback = onResult
        this.onErrorCallback = onError
        _recognizedText.value = ""
        _errorMessage.value = null

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _speechState.value = SpeechState.LISTENING
                        _errorMessage.value = null
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {
                        _rmsDb.value = rmsdB
                    }
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _speechState.value = SpeechState.PROCESSING
                    }

                    override fun onError(error: Int) {
                        _speechState.value = SpeechState.ERROR
                        val errorText = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error during recognition"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                            else -> "Speech recognition error ($error)"
                        }
                        _errorMessage.value = errorText
                        onErrorCallback?.invoke(errorText)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotBlank()) {
                            _recognizedText.value = text
                        }
                        _speechState.value = SpeechState.IDLE
                        onResultCallback?.invoke(text)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotBlank()) {
                            _recognizedText.value = text
                            onPartialCallback?.invoke(text)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _speechState.value = SpeechState.ERROR
            _errorMessage.value = "Failed to start speech recognizer: ${e.message}"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // ignore cleanup errors
        } finally {
            speechRecognizer = null
            if (_speechState.value == SpeechState.LISTENING) {
                _speechState.value = SpeechState.IDLE
            }
        }
    }

    fun reset() {
        _recognizedText.value = ""
        _errorMessage.value = null
        _speechState.value = SpeechState.IDLE
    }

    fun shutdown() {
        stopListening()
    }
}
