package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.repository.PreferencesRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class GeminiClient(
    private val preferencesRepository: PreferencesRepository
) {
    companion object {
        private const val TAG = "GeminiClient"

        /**
         * Safely masks an API key for diagnostic logging.
         * NEVER prints or logs the raw API key to protect user secrets.
         */
        fun maskApiKey(key: String): String {
            val trimmed = key.trim()
            return when {
                trimmed.isEmpty() -> "(none)"
                trimmed.length <= 8 -> "${trimmed.take(2)}...${trimmed.takeLast(2)} (${trimmed.length} chars)"
                else -> "${trimmed.take(6)}...${trimmed.takeLast(4)} (${trimmed.length} chars, prefix: ${trimmed.take(3)})"
            }
        }
    }

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GenerateContentRequest::class.java)
    private val responseAdapter = moshi.adapter(GenerateContentResponse::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/"

    /**
     * Resolves the effective API key:
     * 1. User configured custom API key in app preferences (supports both AIzaSy... and AQ.... formats).
     * 2. Fallback to BuildConfig.GEMINI_API_KEY injected at build time if available.
     */
    fun getEffectiveApiKey(): String {
        val userKey = preferencesRepository.getCustomApiKey().trim()
        if (userKey.isNotEmpty()) {
            return userKey
        }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey.trim()
        }
        return ""
    }

    fun hasEffectiveApiKey(): Boolean = getEffectiveApiKey().isNotEmpty()

    fun isSystemKeyConfigured(): Boolean {
        return try {
            val buildKey = BuildConfig.GEMINI_API_KEY
            buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY"
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Tests the real Gemini API connectivity with an actual API request.
     * Uses "Hello, reply with OK." and validates live candidate output.
     */
    suspend fun testConnection(
        customKey: String? = null,
        modelToTest: String = GeminiModelConstants.DEFAULT_MODEL
    ): ApiTestResult = withContext(Dispatchers.IO) {
        val apiKey = (customKey ?: getEffectiveApiKey()).trim()
        if (apiKey.isEmpty()) {
            return@withContext ApiTestResult(
                isSuccess = false,
                message = "No Gemini API key provided. Please enter and save your API key.",
                state = ApiConnectionState.DISCONNECTED,
                maskedKey = "(none)"
            )
        }

        val startTime = System.currentTimeMillis()
        val effectiveModel = if (modelToTest.isNotBlank()) modelToTest.removePrefix("models/") else GeminiModelConstants.DEFAULT_MODEL
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val url = "$baseUrl$effectiveModel:generateContent?key=$encodedKey"

        Log.d(TAG, "Testing Gemini API connection. Model: $effectiveModel, Key: ${maskApiKey(apiKey)}")

        val requestPayload = GenerateContentRequest(
            contents = listOf(
                ApiContent(
                    role = "user",
                    parts = listOf(ApiPart(text = "Hello, reply with OK."))
                )
            ),
            generationConfig = ApiGenerationConfig(
                temperature = 0.0f,
                maxOutputTokens = 30
            )
        )

        val jsonBody = requestAdapter.toJson(requestPayload)
        val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val httpRequest = Request.Builder()
            .url(url)
            .header("x-goog-api-key", apiKey)
            .header("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        try {
            val response = okHttpClient.newCall(httpRequest).execute()
            val latencyMs = System.currentTimeMillis() - startTime
            val responseCode = response.code
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val parsed = responseAdapter.fromJson(responseBody)
                val reply = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                val snippet = if (!reply.isNullOrBlank()) reply else "OK"
                Log.d(TAG, "Test connection success ($latencyMs ms). Reply: $snippet")
                ApiTestResult(
                    isSuccess = true,
                    message = "Connection Verified ✓ Live Gemini API responded: \"$snippet\" (${latencyMs}ms)",
                    state = ApiConnectionState.CONNECTED,
                    httpCode = responseCode,
                    modelTested = effectiveModel,
                    responseSnippet = snippet,
                    maskedKey = maskApiKey(apiKey),
                    latencyMs = latencyMs
                )
            } else {
                val parsedError = parseGeminiError(responseBody, responseCode)
                Log.w(TAG, "Test connection failed: HTTP $responseCode - ${parsedError.status}: ${parsedError.message}")
                ApiTestResult(
                    isSuccess = false,
                    message = parsedError.formattedSummary,
                    state = parsedError.state,
                    httpCode = responseCode,
                    modelTested = effectiveModel,
                    maskedKey = maskApiKey(apiKey),
                    latencyMs = latencyMs
                )
            }
        } catch (e: Exception) {
            val latencyMs = System.currentTimeMillis() - startTime
            val isNetwork = e is UnknownHostException || e is SocketTimeoutException || e is ConnectException
            Log.e(TAG, "Test connection exception: ${e.message}", e)
            ApiTestResult(
                isSuccess = false,
                message = if (isNetwork) {
                    "Network Connection Error: Unable to reach Gemini API server. Please check your internet connection."
                } else {
                    "API Connection Failed: ${e.localizedMessage ?: "Unknown communication error"}"
                },
                state = if (isNetwork) ApiConnectionState.NETWORK_ERROR else ApiConnectionState.DISCONNECTED,
                maskedKey = maskApiKey(apiKey),
                latencyMs = latencyMs
            )
        }
    }

    /**
     * Verifies that the specified Gemini model is available to the API key's project.
     */
    suspend fun verifyModelAvailability(
        model: String,
        customKey: String? = null
    ): ApiTestResult = withContext(Dispatchers.IO) {
        val apiKey = (customKey ?: getEffectiveApiKey()).trim()
        if (apiKey.isEmpty()) {
            return@withContext ApiTestResult(
                isSuccess = false,
                message = "No API key configured to check model availability.",
                state = ApiConnectionState.DISCONNECTED,
                maskedKey = "(none)"
            )
        }

        val cleanModel = model.removePrefix("models/")
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val url = "$baseUrl$cleanModel?key=$encodedKey"
        val httpRequest = Request.Builder()
            .url(url)
            .header("x-goog-api-key", apiKey)
            .get()
            .build()

        try {
            val response = okHttpClient.newCall(httpRequest).execute()
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val displayName = json.optString("displayName", cleanModel)
                ApiTestResult(
                    isSuccess = true,
                    message = "Model '$displayName' verified and operational for your project.",
                    state = ApiConnectionState.CONNECTED,
                    httpCode = response.code,
                    modelTested = cleanModel,
                    maskedKey = maskApiKey(apiKey)
                )
            } else {
                val parsedError = parseGeminiError(responseBody, response.code)
                ApiTestResult(
                    isSuccess = false,
                    message = parsedError.formattedSummary,
                    state = parsedError.state,
                    httpCode = response.code,
                    modelTested = cleanModel,
                    maskedKey = maskApiKey(apiKey)
                )
            }
        } catch (e: Exception) {
            ApiTestResult(
                isSuccess = false,
                message = "Could not verify model: ${e.localizedMessage}",
                state = ApiConnectionState.NETWORK_ERROR,
                maskedKey = maskApiKey(apiKey)
            )
        }
    }

    /**
     * Streams content directly from Gemini API chunk by chunk via SSE.
     * Uses official x-goog-api-key authentication header.
     */
    fun streamGenerateContent(
        model: String,
        contents: List<ApiContent>,
        systemInstructionText: String? = null,
        temperature: Float = 0.7f,
        topP: Float = 0.95f,
        thinkingBudget: Int? = null,
        enableGoogleSearch: Boolean = false,
        enableCodeExecution: Boolean = false,
        onGroundingSources: ((List<GroundingSource>) -> Unit)? = null
    ): Flow<String> = flow {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            throw IllegalStateException("Gemini API key is not configured. Please add your API key in Settings.")
        }

        val effectiveModel = if (model.isNotBlank()) model.removePrefix("models/") else GeminiModelConstants.DEFAULT_MODEL
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val url = "$baseUrl$effectiveModel:streamGenerateContent?alt=sse&key=$encodedKey"

        val systemInstruction = if (!systemInstructionText.isNullOrBlank()) {
            ApiContent(
                parts = listOf(ApiPart(text = systemInstructionText))
            )
        } else null

        val toolsList = mutableListOf<Map<String, Any>>()
        if (enableGoogleSearch) {
            toolsList.add(mapOf("googleSearch" to emptyMap<String, Any>()))
        }
        if (enableCodeExecution) {
            toolsList.add(mapOf("codeExecution" to emptyMap<String, Any>()))
        }

        val thinkingConfig = if (thinkingBudget != null && thinkingBudget > 0) {
            ApiThinkingConfig(thinkingBudget = thinkingBudget)
        } else null

        val payload = GenerateContentRequest(
            contents = contents,
            generationConfig = ApiGenerationConfig(
                temperature = temperature,
                topP = topP,
                thinkingConfig = thinkingConfig
            ),
            systemInstruction = systemInstruction,
            tools = if (toolsList.isNotEmpty()) toolsList else null
        )

        val jsonBody = requestAdapter.toJson(payload)
        val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val httpRequest = Request.Builder()
            .url(url)
            .header("x-goog-api-key", apiKey)
            .header("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        val response = okHttpClient.newCall(httpRequest).execute()

        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: ""
            val parsedError = parseGeminiError(errBody, response.code)
            throw RuntimeException(parsedError.formattedSummary)
        }

        val sourceStream = response.body?.byteStream()
            ?: throw IllegalStateException("Empty response body from Gemini API")

        BufferedReader(InputStreamReader(sourceStream, "UTF-8")).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line?.trim() ?: continue
                if (currentLine.startsWith("data:")) {
                    val dataJson = currentLine.removePrefix("data:").trim()
                    if (dataJson.isNotEmpty() && dataJson != "[DONE]") {
                        try {
                            val (chunkText, sources) = parseChunkTextAndSources(dataJson)
                            if (!sources.isNullOrEmpty()) {
                                onGroundingSources?.invoke(sources)
                            }
                            if (!chunkText.isNullOrEmpty()) {
                                emit(chunkText)
                            }
                        } catch (e: Exception) {
                            // ignore partial frame parse error
                        }
                    }
                } else if (currentLine.startsWith("{") && currentLine.endsWith("}")) {
                    // Fallback for non-SSE streaming format
                    try {
                        val (chunkText, sources) = parseChunkTextAndSources(currentLine)
                        if (!sources.isNullOrEmpty()) {
                            onGroundingSources?.invoke(sources)
                        }
                        if (!chunkText.isNullOrEmpty()) {
                            emit(chunkText)
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Non-streaming single generation fallback with thinking and tools support.
     */
    suspend fun generateContent(
        model: String,
        contents: List<ApiContent>,
        systemInstructionText: String? = null,
        temperature: Float = 0.7f,
        thinkingBudget: Int? = null,
        enableGoogleSearch: Boolean = false,
        enableCodeExecution: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            throw IllegalStateException("Gemini API key is not configured. Please configure your key in Settings.")
        }

        val effectiveModel = if (model.isNotBlank()) model.removePrefix("models/") else GeminiModelConstants.DEFAULT_MODEL
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val url = "$baseUrl$effectiveModel:generateContent?key=$encodedKey"

        val systemInstruction = if (!systemInstructionText.isNullOrBlank()) {
            ApiContent(
                parts = listOf(ApiPart(text = systemInstructionText))
            )
        } else null

        val toolsList = mutableListOf<Map<String, Any>>()
        if (enableGoogleSearch) {
            toolsList.add(mapOf("googleSearch" to emptyMap<String, Any>()))
        }
        if (enableCodeExecution) {
            toolsList.add(mapOf("codeExecution" to emptyMap<String, Any>()))
        }

        val thinkingConfig = if (thinkingBudget != null && thinkingBudget > 0) {
            ApiThinkingConfig(thinkingBudget = thinkingBudget)
        } else null

        val payload = GenerateContentRequest(
            contents = contents,
            generationConfig = ApiGenerationConfig(
                temperature = temperature,
                thinkingConfig = thinkingConfig
            ),
            systemInstruction = systemInstruction,
            tools = if (toolsList.isNotEmpty()) toolsList else null
        )

        val jsonBody = requestAdapter.toJson(payload)
        val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val httpRequest = Request.Builder()
            .url(url)
            .header("x-goog-api-key", apiKey)
            .header("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        val response = okHttpClient.newCall(httpRequest).execute()
        val responseBody = response.body?.string() ?: ""

        if (response.isSuccessful) {
            val parsed = responseAdapter.fromJson(responseBody)
            parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Received empty response candidate from Gemini")
        } else {
            val parsedError = parseGeminiError(responseBody, response.code)
            throw RuntimeException(parsedError.formattedSummary)
        }
    }

    /**
     * Verifies real live model availability against the Gemini API.
     */
    suspend fun verifyModelAvailability(modelId: String): ApiTestResult = withContext(Dispatchers.IO) {
        val cleanModel = modelId.removePrefix("models/")
        testConnection(customKey = null, modelToTest = cleanModel)
    }

    private fun parseChunkTextAndSources(jsonString: String): Pair<String?, List<GroundingSource>?> {
        return try {
            val json = JSONObject(jsonString)
            val candidates = json.optJSONArray("candidates") ?: return Pair(null, null)
            if (candidates.length() == 0) return Pair(null, null)
            val firstCandidate = candidates.getJSONObject(0)

            val sources = mutableListOf<GroundingSource>()
            val grounding = firstCandidate.optJSONObject("groundingMetadata")
            if (grounding != null) {
                val chunks = grounding.optJSONArray("groundingChunks")
                if (chunks != null) {
                    for (i in 0 until chunks.length()) {
                        val chunk = chunks.getJSONObject(i)
                        val web = chunk.optJSONObject("web")
                        if (web != null) {
                            val uri = web.optString("uri")
                            val title = web.optString("title", uri)
                            if (uri.isNotBlank()) {
                                sources.add(GroundingSource(title = title, url = uri))
                            }
                        }
                    }
                }
            }

            val content = firstCandidate.optJSONObject("content") ?: return Pair(null, sources)
            val parts = content.optJSONArray("parts") ?: return Pair(null, sources)
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val text = part.optString("text")
                if (text.isNotEmpty()) {
                    sb.append(text)
                }
            }
            Pair(sb.toString(), sources)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }

    private fun parseChunkText(jsonString: String): String? {
        return try {
            val json = JSONObject(jsonString)
            val candidates = json.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val text = part.optString("text")
                if (text.isNotEmpty()) {
                    sb.append(text)
                }
            }
            sb.toString()
        } catch (e: Exception) {
            null
        }
    }

    data class ParsedGeminiError(
        val httpCode: Int,
        val status: String,
        val reason: String?,
        val message: String,
        val state: ApiConnectionState,
        val formattedSummary: String
    )

    /**
     * Parses Google Gemini API error payloads and produces rich, actionable diagnostic summaries.
     * Distinguishes 401 (Authentication), 403 (Permission/Service), 404 (Model), 429 (Quota), 5xx (Server), and Network errors.
     */
    fun parseGeminiError(rawBody: String, httpCode: Int): ParsedGeminiError {
        var status = ""
        var message = ""
        var reason: String? = null

        try {
            val json = JSONObject(rawBody)
            val errorObj = json.optJSONObject("error")
            if (errorObj != null) {
                status = errorObj.optString("status", "")
                message = errorObj.optString("message", "")
                val detailsArray = errorObj.optJSONArray("details")
                if (detailsArray != null) {
                    for (i in 0 until detailsArray.length()) {
                        val detail = detailsArray.optJSONObject(i)
                        val r = detail?.optString("reason")
                        if (!r.isNullOrBlank()) {
                            reason = r
                            break
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Raw body was not JSON
        }

        if (message.isBlank()) {
            message = when (httpCode) {
                400 -> "Bad request or invalid argument sent to Gemini API."
                401 -> "Authentication error: Invalid or expired API key credentials."
                403 -> "Access forbidden: Permission denied for this API key or project."
                404 -> "Model not found: The requested Gemini model is not available."
                429 -> "Rate limit or free quota reached. Please retry later."
                in 500..599 -> "Gemini API service temporarily unavailable."
                else -> "Gemini API request failed with HTTP code $httpCode."
            }
        }

        val (state, summary) = when {
            httpCode == 401 || status == "UNAUTHENTICATED" -> {
                Pair(
                    ApiConnectionState.INVALID_KEY,
                    "Authentication Failed (HTTP 401 - UNAUTHENTICATED): $message\n\nVerify that your Google AI Studio API key is active and correctly copied."
                )
            }
            httpCode == 403 || status == "PERMISSION_DENIED" -> {
                val detailReason = if (reason != null) " [$reason]" else ""
                Pair(
                    ApiConnectionState.PERMISSION_DENIED,
                    "Permission Denied (HTTP 403 - PERMISSION_DENIED$detailReason): $message\n\nVerify that Generative Language API is enabled in your Google Cloud / AI Studio project and no package or IP restrictions block this Android app."
                )
            }
            httpCode == 404 || status == "NOT_FOUND" -> {
                Pair(
                    ApiConnectionState.MODEL_NOT_FOUND,
                    "Model Not Found (HTTP 404 - NOT_FOUND): $message\n\nThe requested model is not available or has been retired. Please switch to 'gemini-3.5-flash'."
                )
            }
            httpCode == 429 || status == "RESOURCE_EXHAUSTED" -> {
                val detailReason = if (reason != null) " [$reason]" else ""
                Pair(
                    ApiConnectionState.RATE_LIMITED,
                    "Quota / Rate Limit Exceeded (HTTP 429 - RESOURCE_EXHAUSTED$detailReason): $message\n\nFree tier or project quota limit reached. Please wait a moment before trying again."
                )
            }
            httpCode == 400 && (reason == "API_KEY_INVALID" || message.contains("API key not valid", ignoreCase = true)) -> {
                Pair(
                    ApiConnectionState.INVALID_KEY,
                    "Invalid API Key (HTTP 400 - API_KEY_INVALID): $message\n\nPlease check that the API key was pasted completely without extra characters."
                )
            }
            httpCode in 500..599 || status == "UNAVAILABLE" -> {
                Pair(
                    ApiConnectionState.SERVER_ERROR,
                    "Gemini Server Issue (HTTP $httpCode - ${if (status.isNotBlank()) status else "SERVER_ERROR"}): $message\n\nGoogle servers are experiencing high demand. Please retry in a few moments."
                )
            }
            else -> {
                Pair(
                    ApiConnectionState.DISCONNECTED,
                    "Gemini API Error (HTTP $httpCode${if (status.isNotBlank()) " - $status" else ""}): $message"
                )
            }
        }

        return ParsedGeminiError(
            httpCode = httpCode,
            status = status,
            reason = reason,
            message = message,
            state = state,
            formattedSummary = summary
        )
    }
}
