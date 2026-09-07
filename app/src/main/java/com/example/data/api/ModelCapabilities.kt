package com.example.data.api

data class ModelCapabilities(
    val id: String,
    val displayName: String,
    val category: String,
    val badge: String,
    val description: String,
    val contextWindow: String,
    val supportsVision: Boolean,
    val supportsPdf: Boolean,
    val supportsThinking: Boolean,
    val supportsSearchGrounding: Boolean,
    val supportsCodeExecution: Boolean,
    val speedScore: Int,      // 1-5
    val reasoningScore: Int,  // 1-5
    val maxOutputTokens: Int
)

object ModelRegistry {
    val MODELS = listOf(
        ModelCapabilities(
            id = GeminiModelConstants.GEMINI_3_5_FLASH,
            displayName = "Gemini 3.5 Flash",
            category = "Standard",
            badge = "Default",
            description = "Balanced, responsive, state-of-the-art model for text, code, logic, and multimodal documents.",
            contextWindow = "1M tokens",
            supportsVision = true,
            supportsPdf = true,
            supportsThinking = true,
            supportsSearchGrounding = true,
            supportsCodeExecution = true,
            speedScore = 5,
            reasoningScore = 4,
            maxOutputTokens = 8192
        ),
        ModelCapabilities(
            id = GeminiModelConstants.GEMINI_3_6_FLASH,
            displayName = "Gemini 3.6 Flash",
            category = "Preview",
            badge = "Latest Flash",
            description = "Next-generation high efficiency Flash model with advanced reasoning, vision, and comprehension.",
            contextWindow = "1M tokens",
            supportsVision = true,
            supportsPdf = true,
            supportsThinking = true,
            supportsSearchGrounding = true,
            supportsCodeExecution = true,
            speedScore = 5,
            reasoningScore = 5,
            maxOutputTokens = 8192
        ),
        ModelCapabilities(
            id = GeminiModelConstants.GEMINI_3_1_PRO,
            displayName = "Gemini 3.1 Pro",
            category = "Pro Reasoning",
            badge = "Deep Reasoning",
            description = "High-parameter frontier model engineered for complex multi-step reasoning, mathematical proofs, and STEM.",
            contextWindow = "2M tokens",
            supportsVision = true,
            supportsPdf = true,
            supportsThinking = true,
            supportsSearchGrounding = true,
            supportsCodeExecution = true,
            speedScore = 3,
            reasoningScore = 5,
            maxOutputTokens = 8192
        ),
        ModelCapabilities(
            id = GeminiModelConstants.GEMINI_3_1_FLASH_LITE,
            displayName = "Gemini 3.1 Flash Lite",
            category = "Lightweight",
            badge = "Ultra Fast",
            description = "Super low-latency model designed for rapid summaries, instant classification, and quick conversations.",
            contextWindow = "1M tokens",
            supportsVision = true,
            supportsPdf = true,
            supportsThinking = false,
            supportsSearchGrounding = true,
            supportsCodeExecution = false,
            speedScore = 5,
            reasoningScore = 3,
            maxOutputTokens = 4096
        ),
        ModelCapabilities(
            id = GeminiModelConstants.GEMINI_2_5_FLASH_IMAGE,
            displayName = "Gemini 2.5 Flash Image",
            category = "Multimodal Vision",
            badge = "Vision & Art",
            description = "Specialized for advanced visual inspection, fine-grained diagram breakdown, and multimodal creation.",
            contextWindow = "128K tokens",
            supportsVision = true,
            supportsPdf = false,
            supportsThinking = false,
            supportsSearchGrounding = false,
            supportsCodeExecution = false,
            speedScore = 4,
            reasoningScore = 4,
            maxOutputTokens = 4096
        )
    )

    fun getCapabilities(modelId: String): ModelCapabilities {
        val clean = modelId.removePrefix("models/")
        return MODELS.find { it.id == clean } ?: MODELS.first()
    }
}
