package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object GeminiModelConstants {
    const val GEMINI_3_5_FLASH = "gemini-3.5-flash"
    const val GEMINI_3_6_FLASH = "gemini-3.6-flash"
    const val GEMINI_3_1_PRO = "gemini-3.1-pro-preview"
    const val GEMINI_3_1_FLASH_LITE = "gemini-3.1-flash-lite-preview"
    const val GEMINI_2_5_FLASH_IMAGE = "gemini-2.5-flash-image"
    const val DEFAULT_MODEL = GEMINI_3_5_FLASH

    val AVAILABLE_MODELS = listOf(
        ModelInfo(
            id = GEMINI_3_5_FLASH,
            displayName = "Gemini 3.5 Flash",
            tag = "Default • Fast, Smart & Vision",
            description = "Balanced, responsive, state-of-the-art model for text, code, logic, and multimodal images.",
            badge = "Default"
        ),
        ModelInfo(
            id = GEMINI_3_6_FLASH,
            displayName = "Gemini 3.6 Flash",
            tag = "Latest Flash Release",
            description = "Next-generation high efficiency Flash model with advanced reasoning and comprehension.",
            badge = "Latest"
        ),
        ModelInfo(
            id = GEMINI_3_1_FLASH_LITE,
            displayName = "Gemini 3.1 Flash Lite",
            tag = "Ultra Lightweight",
            description = "Super low-latency model designed for rapid summaries and lightweight conversations.",
            badge = "Lite"
        ),
        ModelInfo(
            id = GEMINI_3_1_PRO,
            displayName = "Gemini 3.1 Pro",
            tag = "Deep Reasoning & STEM",
            description = "High-parameter model engineered for multi-step reasoning, mathematical proofs, and code.",
            badge = "Pro"
        )
    )
}

data class ModelInfo(
    val id: String,
    val displayName: String,
    val tag: String,
    val description: String,
    val badge: String
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<ApiContent>,
    @Json(name = "generationConfig") val generationConfig: ApiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: ApiContent? = null,
    @Json(name = "tools") val tools: List<Map<String, Any>>? = null
)

@JsonClass(generateAdapter = true)
data class ApiContent(
    @Json(name = "role") val role: String? = null,
    @Json(name = "parts") val parts: List<ApiPart>
)

@JsonClass(generateAdapter = true)
data class ApiPart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: ApiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class ApiInlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class ApiThinkingConfig(
    @Json(name = "thinkingBudget") val thinkingBudget: Int? = null,
    @Json(name = "thinkingLevel") val thinkingLevel: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null,
    @Json(name = "thinkingConfig") val thinkingConfig: ApiThinkingConfig? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<ApiCandidate>? = null,
    @Json(name = "error") val error: ApiError? = null
)

@JsonClass(generateAdapter = true)
data class ApiCandidate(
    @Json(name = "content") val content: ApiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null,
    @Json(name = "groundingMetadata") val groundingMetadata: ApiGroundingMetadata? = null
)

@JsonClass(generateAdapter = true)
data class ApiGroundingMetadata(
    @Json(name = "webSearchQueries") val webSearchQueries: List<String>? = null,
    @Json(name = "groundingChunks") val groundingChunks: List<ApiGroundingChunk>? = null
)

@JsonClass(generateAdapter = true)
data class ApiGroundingChunk(
    @Json(name = "web") val web: ApiWebGrounding? = null
)

@JsonClass(generateAdapter = true)
data class ApiWebGrounding(
    @Json(name = "uri") val uri: String? = null,
    @Json(name = "title") val title: String? = null
)

data class GroundingSource(
    val title: String,
    val url: String
)

data class ExtractedAiResponse(
    val cleanText: String,
    val thinkingText: String? = null,
    val groundingSources: List<GroundingSource> = emptyList()
) {
    val sources: List<GroundingSource> get() = groundingSources
}

@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "status") val status: String? = null
)

enum class ApiConnectionState {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    INVALID_KEY,          // 401 Unauthenticated or malformed key
    PERMISSION_DENIED,    // 403 Permission denied or Generative Language API disabled
    MODEL_NOT_FOUND,      // 404 Model does not exist or has been retired
    RATE_LIMITED,         // 429 Resource exhausted / quota reached
    SERVER_ERROR,         // 500/503 Temporary Google infrastructure issue
    NETWORK_ERROR         // Network/DNS/connect failure
}

data class ApiTestResult(
    val isSuccess: Boolean,
    val message: String,
    val state: ApiConnectionState,
    val httpCode: Int? = null,
    val modelTested: String? = null,
    val responseSnippet: String? = null,
    val maskedKey: String? = null,
    val latencyMs: Long? = null
)

object ResponseParser {
    private val THOUGHT_REGEX = Regex("<thought>(.*?)</thought>", RegexOption.DOT_MATCHES_ALL)
    private val THINK_REGEX = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)
    private val SOURCE_REGEX = Regex("\\[(.*?)\\]\\((https?://.*?)\\)")

    fun parse(raw: String): ExtractedAiResponse {
        var thinking: String? = null
        var text = raw

        val thoughtMatch = THOUGHT_REGEX.find(text)
        if (thoughtMatch != null) {
            thinking = thoughtMatch.groupValues[1].trim()
            text = text.replace(thoughtMatch.value, "").trim()
        } else {
            val thinkMatch = THINK_REGEX.find(text)
            if (thinkMatch != null) {
                thinking = thinkMatch.groupValues[1].trim()
                text = text.replace(thinkMatch.value, "").trim()
            }
        }

        val sources = mutableListOf<GroundingSource>()
        if (text.contains("\n\n**Sources:**\n")) {
            val parts = text.split("\n\n**Sources:**\n", limit = 2)
            if (parts.size == 2) {
                text = parts[0].trim()
                val sourcesSection = parts[1]
                SOURCE_REGEX.findAll(sourcesSection).forEach { match ->
                    val title = match.groupValues[1].trim().removePrefix("•").trim()
                    val url = match.groupValues[2].trim()
                    sources.add(GroundingSource(title = title, url = url))
                }
            }
        }

        return ExtractedAiResponse(
            cleanText = text,
            thinkingText = thinking,
            groundingSources = sources
        )
    }
}

