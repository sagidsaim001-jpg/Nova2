package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.ApiConnectionState
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiModelConstants
import com.example.data.repository.PreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GeminiClientTest {

    private lateinit var client: GeminiClient

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesRepository(context)
        client = GeminiClient(prefs)
    }

    @Test
    fun maskApiKey_masksStandardAizaSyKeysSafely() {
        val key = "AIzaSyB1234567890abcdef"
        val masked = GeminiClient.maskApiKey(key)
        assertFalse("Masked key should never contain the full key", masked.contains(key))
        assertTrue("Should keep prefix", masked.startsWith("AIzaSy"))
        assertTrue("Should end with suffix", masked.endsWith("cdef (23 chars, prefix: AIz)"))
    }

    @Test
    fun maskApiKey_masksModernAqFormatKeysSafely() {
        val aqKey = "AQ.AbCdEf123456789_XYZ"
        val masked = GeminiClient.maskApiKey(aqKey)
        assertFalse("Masked key should never contain the full key", masked.contains(aqKey))
        assertTrue("Should preserve AQ. prefix", masked.startsWith("AQ.AbC"))
    }

    @Test
    fun parseGeminiError_accuratelyCategorizes401Unauthenticated() {
        val rawJson = """{"error":{"code":401,"message":"API key not valid. Please pass a valid API key.","status":"UNAUTHENTICATED"}}"""
        val error = client.parseGeminiError(rawJson, 401)
        assertEquals(ApiConnectionState.INVALID_KEY, error.state)
        assertEquals("UNAUTHENTICATED", error.status)
        assertTrue(error.formattedSummary.contains("HTTP 401"))
    }

    @Test
    fun parseGeminiError_accuratelyCategorizes403PermissionDenied() {
        val rawJson = """{"error":{"code":403,"message":"Generative Language API has not been used in project before or it is disabled.","status":"PERMISSION_DENIED","details":[{"@type":"type.googleapis.com/google.rpc.ErrorInfo","reason":"SERVICE_DISABLED"}]}}"""
        val error = client.parseGeminiError(rawJson, 403)
        assertEquals(ApiConnectionState.PERMISSION_DENIED, error.state)
        assertEquals("PERMISSION_DENIED", error.status)
        assertEquals("SERVICE_DISABLED", error.reason)
        assertTrue(error.formattedSummary.contains("HTTP 403"))
    }

    @Test
    fun parseGeminiError_accuratelyCategorizes404ModelNotFound() {
        val rawJson = """{"error":{"code":404,"message":"models/gemini-old-model is not found for API version v1beta","status":"NOT_FOUND"}}"""
        val error = client.parseGeminiError(rawJson, 404)
        assertEquals(ApiConnectionState.MODEL_NOT_FOUND, error.state)
        assertEquals("NOT_FOUND", error.status)
        assertTrue(error.formattedSummary.contains("HTTP 404"))
    }

    @Test
    fun parseGeminiError_accuratelyCategorizes429QuotaExhausted() {
        val rawJson = """{"error":{"code":429,"message":"Resource has been exhausted (e.g. check quota).","status":"RESOURCE_EXHAUSTED"}}"""
        val error = client.parseGeminiError(rawJson, 429)
        assertEquals(ApiConnectionState.RATE_LIMITED, error.state)
        assertEquals("RESOURCE_EXHAUSTED", error.status)
        assertTrue(error.formattedSummary.contains("HTTP 429"))
    }

    @Test
    fun defaultModel_isSupportedGemini3_5Flash() {
        assertEquals("gemini-3.5-flash", GeminiModelConstants.DEFAULT_MODEL)
        assertTrue(GeminiModelConstants.AVAILABLE_MODELS.any { it.id == "gemini-3.5-flash" })
    }
}
