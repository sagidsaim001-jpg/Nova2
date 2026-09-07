package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

enum class AccentColor {
    BLUE, PURPLE, PINK, GREEN, ORANGE, CYAN
}

enum class ChatDensity {
    COMFORTABLE, COMPACT
}

enum class AnimationLevel {
    FULL, REDUCED, OFF
}

class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nova_preferences", Context.MODE_PRIVATE)

    private val _customApiKeyFlow = MutableStateFlow(getCustomApiKey())
    val customApiKeyFlow: StateFlow<String> = _customApiKeyFlow.asStateFlow()

    private val _themeModeFlow = MutableStateFlow(getThemeMode())
    val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

    private val _accentColorFlow = MutableStateFlow(getAccentColor())
    val accentColorFlow: StateFlow<AccentColor> = _accentColorFlow.asStateFlow()

    private val _activeUserIdFlow = MutableStateFlow(getActiveUserId())
    val activeUserIdFlow: StateFlow<String> = _activeUserIdFlow.asStateFlow()

    fun getCustomApiKey(): String = prefs.getString("custom_api_key", "") ?: ""

    fun setCustomApiKey(key: String) {
        prefs.edit().putString("custom_api_key", key.trim()).apply()
        _customApiKeyFlow.value = key.trim()
    }

    fun getActiveUserId(): String = prefs.getString("active_user_id", "guest") ?: "guest"

    fun setActiveUserId(id: String) {
        prefs.edit().putString("active_user_id", id).apply()
        _activeUserIdFlow.value = id
    }

    fun getActiveUserName(): String = prefs.getString("active_user_name", "Guest Explorer") ?: "Guest Explorer"

    fun setActiveUserName(name: String) {
        prefs.edit().putString("active_user_name", name).apply()
    }

    fun getActiveUserEmail(): String = prefs.getString("active_user_email", "guest@nova.ai") ?: "guest@nova.ai"

    fun setActiveUserEmail(email: String) {
        prefs.edit().putString("active_user_email", email).apply()
    }

    fun getThemeMode(): ThemeMode {
        val name = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeModeFlow.value = mode
    }

    fun getAccentColor(): AccentColor {
        val name = prefs.getString("accent_color", AccentColor.PURPLE.name) ?: AccentColor.PURPLE.name
        return try {
            AccentColor.valueOf(name)
        } catch (e: Exception) {
            AccentColor.PURPLE
        }
    }

    fun setAccentColor(color: AccentColor) {
        prefs.edit().putString("accent_color", color.name).apply()
        _accentColorFlow.value = color
    }

    fun getChatDensity(): ChatDensity {
        val name = prefs.getString("chat_density", ChatDensity.COMFORTABLE.name) ?: ChatDensity.COMFORTABLE.name
        return try {
            ChatDensity.valueOf(name)
        } catch (e: Exception) {
            ChatDensity.COMFORTABLE
        }
    }

    fun setChatDensity(density: ChatDensity) {
        prefs.edit().putString("chat_density", density.name).apply()
    }

    fun getAnimationLevel(): AnimationLevel {
        val name = prefs.getString("animation_level", AnimationLevel.FULL.name) ?: AnimationLevel.FULL.name
        return try {
            AnimationLevel.valueOf(name)
        } catch (e: Exception) {
            AnimationLevel.FULL
        }
    }

    fun setAnimationLevel(level: AnimationLevel) {
        prefs.edit().putString("animation_level", level.name).apply()
    }

    fun getDefaultModel(): String = prefs.getString("default_model", "gemini-3.5-flash") ?: "gemini-3.5-flash"

    fun setDefaultModel(model: String) {
        prefs.edit().putString("default_model", model).apply()
    }

    fun getCustomInstructions(): String = prefs.getString("custom_instructions", "") ?: ""

    fun setCustomInstructions(instructions: String) {
        prefs.edit().putString("custom_instructions", instructions).apply()
    }

    fun isEnterToSend(): Boolean = prefs.getBoolean("enter_to_send", false)
    fun setEnterToSend(enabled: Boolean) = prefs.edit().putBoolean("enter_to_send", enabled).apply()

    fun isAutoTitle(): Boolean = prefs.getBoolean("auto_title", true)
    fun setAutoTitle(enabled: Boolean) = prefs.edit().putBoolean("auto_title", enabled).apply()

    fun isShowTimestamps(): Boolean = prefs.getBoolean("show_timestamps", true)
    fun setShowTimestamps(enabled: Boolean) = prefs.edit().putBoolean("show_timestamps", enabled).apply()

    fun isStreamingEnabled(): Boolean = prefs.getBoolean("streaming_enabled", true)
    fun setStreamingEnabled(enabled: Boolean) = prefs.edit().putBoolean("streaming_enabled", enabled).apply()

    fun isSyntaxHighlighting(): Boolean = prefs.getBoolean("syntax_highlighting", true)
    fun setSyntaxHighlighting(enabled: Boolean) = prefs.edit().putBoolean("syntax_highlighting", enabled).apply()

    fun isConfirmDelete(): Boolean = prefs.getBoolean("confirm_delete", true)
    fun setConfirmDelete(enabled: Boolean) = prefs.edit().putBoolean("confirm_delete", enabled).apply()

    fun isAutoScroll(): Boolean = prefs.getBoolean("auto_scroll", true)
    fun setAutoScroll(enabled: Boolean) = prefs.edit().putBoolean("auto_scroll", enabled).apply()

    fun hasCompletedWelcome(): Boolean = prefs.getBoolean("has_completed_welcome", false)
    fun setCompletedWelcome(completed: Boolean) = prefs.edit().putBoolean("has_completed_welcome", completed).apply()

    // Professional AI Modes & Reasoning
    fun getAiMode(): String = prefs.getString("active_ai_mode", "quick") ?: "quick"
    fun setAiMode(modeId: String) = prefs.edit().putString("active_ai_mode", modeId).apply()

    fun getThinkingEffort(): Int = prefs.getInt("thinking_effort", 4096)
    fun setThinkingEffort(tokens: Int) = prefs.edit().putInt("thinking_effort", tokens).apply()

    fun isGoogleSearchGrounding(): Boolean = prefs.getBoolean("google_search_grounding", false)
    fun setGoogleSearchGrounding(enabled: Boolean) = prefs.edit().putBoolean("google_search_grounding", enabled).apply()

    // Voice Assistant Settings
    fun getVoiceSpeed(): Float = prefs.getFloat("voice_speed", 1.0f)
    fun setVoiceSpeed(speed: Float) = prefs.edit().putFloat("voice_speed", speed).apply()

    fun getVoicePitch(): Float = prefs.getFloat("voice_pitch", 1.0f)
    fun setVoicePitch(pitch: Float) = prefs.edit().putFloat("voice_pitch", pitch).apply()

    fun getVoicePersonality(): String = prefs.getString("voice_personality", "Friendly & Warm") ?: "Friendly & Warm"
    fun setVoicePersonality(personality: String) = prefs.edit().putString("voice_personality", personality).apply()
}

