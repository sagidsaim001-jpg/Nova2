package com.example.data.model

import com.example.data.api.GeminiModelConstants

enum class AiMode(
    val id: String,
    val title: String,
    val tagline: String,
    val description: String,
    val systemInstruction: String,
    val temperature: Float,
    val topP: Float,
    val recommendedModel: String,
    val enableGoogleSearch: Boolean,
    val enableCodeExecution: Boolean,
    val defaultThinkingBudget: Int // 0 = off, >0 = token budget
) {
    QUICK(
        id = "quick",
        title = "Quick",
        tagline = "Snappy & Direct",
        description = "Optimized for speed. Delivers concise, clear, and actionable answers without unnecessary fluff.",
        systemInstruction = "You are NOVA AI operating in Quick Mode. Provide direct, highly concise, crystal-clear answers. Get straight to the point without excessive preamble, filler, or unnecessary conversational pleasantries. Format with scannable bullet points when appropriate.",
        temperature = 0.3f,
        topP = 0.85f,
        recommendedModel = GeminiModelConstants.GEMINI_3_5_FLASH,
        enableGoogleSearch = false,
        enableCodeExecution = false,
        defaultThinkingBudget = 0
    ),

    THINKING(
        id = "thinking",
        title = "Thinking",
        tagline = "Deep Reasoning & Logic",
        description = "Engages multi-step analytical reasoning. Deconstructs assumptions and verifies logical conclusions.",
        systemInstruction = "You are NOVA AI operating in Deep Thinking Mode. When tackling queries, reason systematically step-by-step. Analyze underlying assumptions, consider counter-arguments, verify mathematical or logical consistency, and clearly structure your rationale before delivering your final conclusion.",
        temperature = 0.5f,
        topP = 0.95f,
        recommendedModel = GeminiModelConstants.GEMINI_3_1_PRO,
        enableGoogleSearch = false,
        enableCodeExecution = false,
        defaultThinkingBudget = 4096
    ),

    RESEARCH(
        id = "research",
        title = "Research",
        tagline = "Grounding & Citations",
        description = "Provides comprehensive, factual reports with real-world grounding and synthesis of authoritative knowledge.",
        systemInstruction = "You are NOVA AI operating in Deep Research Mode. Provide thorough, authoritative, and well-structured analysis. Synthesize findings with clear topic headings, background context, key takeaways, and objective evaluations. Cite specific sources and data points whenever available.",
        temperature = 0.4f,
        topP = 0.90f,
        recommendedModel = GeminiModelConstants.GEMINI_3_5_FLASH,
        enableGoogleSearch = true,
        enableCodeExecution = false,
        defaultThinkingBudget = 1024
    ),

    STUDY(
        id = "study",
        title = "Study",
        tagline = "Socratic Tutor & Mentor",
        description = "Breaks complex subjects down with intuitive analogies, interactive check-ins, and step-by-step guidance.",
        systemInstruction = "You are NOVA AI operating in Study & Learn Mode. Act as a world-class Socratic mentor and supportive tutor. Explain concepts clearly using vivid real-world analogies, highlight foundational principles, and end explanations with a short check-for-understanding question or thought-provoking puzzle to test comprehension.",
        temperature = 0.6f,
        topP = 0.90f,
        recommendedModel = GeminiModelConstants.GEMINI_3_5_FLASH,
        enableGoogleSearch = false,
        enableCodeExecution = false,
        defaultThinkingBudget = 0
    ),

    CODING(
        id = "coding",
        title = "Coding",
        tagline = "Senior Software Engineer",
        description = "Architects robust software, writes production-ready code with types, error handling, and complexity notes.",
        systemInstruction = "You are NOVA AI operating in Senior Software Engineer Mode. Write clean, production-ready, idiomatic code adhering to best practices and design patterns. Include brief comments for tricky logic, note time/space complexity, handle edge cases, and ensure all snippets are fully typed and runnable.",
        temperature = 0.2f,
        topP = 0.85f,
        recommendedModel = GeminiModelConstants.GEMINI_3_1_PRO,
        enableGoogleSearch = false,
        enableCodeExecution = true,
        defaultThinkingBudget = 2048
    ),

    CREATIVE(
        id = "creative",
        title = "Creative",
        tagline = "Vivid Prose & Studio",
        description = "Crafts rich storytelling, evocative prose, unique metaphors, and imaginative world-building.",
        systemInstruction = "You are NOVA AI operating in Creative Studio Mode. Produce richly evocative, imaginative prose, compelling narratives, and inventive concepts. Avoid clichés and predictable tropes. Employ vibrant sensory details, striking metaphors, and distinct stylistic voices suited to the context.",
        temperature = 0.9f,
        topP = 0.98f,
        recommendedModel = GeminiModelConstants.GEMINI_3_5_FLASH,
        enableGoogleSearch = false,
        enableCodeExecution = false,
        defaultThinkingBudget = 0
    );

    companion object {
        val DEFAULT = QUICK

        fun fromId(id: String?): AiMode {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }
}
