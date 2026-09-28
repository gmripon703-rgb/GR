package com.example.ai

import kotlinx.coroutines.flow.Flow

enum class TaskType {
    TEXT,
    CODING,
    VISION,
    EMBEDDINGS,
    SPEECH_TO_TEXT,
    TEXT_TO_SPEECH,
    IMAGE_GENERATION,
    VIDEO_GENERATION
}

enum class ProviderType {
    LOCAL,
    CLOUD,
    CUSTOM
}

enum class ProviderMode {
    AUTO,
    OFFLINE_ONLY,
    ONLINE_ONLY
}

data class AIRequest(
    val prompt: String,
    val systemInstruction: String? = null,
    val taskType: TaskType = TaskType.TEXT,
    val temperature: Float = 0.3f,
    val maxTokens: Int = 2048,
    val stream: Boolean = true,
    val attachedCodeSnippet: String? = null,
    val language: String? = null,
    val attachedImageData: String? = null,
    val imageMimeType: String? = "image/jpeg"
)

data class AIResponse(
    val text: String,
    val modelName: String,
    val providerType: ProviderType,
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val finishReason: String = "stop",
    val latencyMs: Long = 0
)

data class AIChunk(
    val deltaText: String,
    val isFinal: Boolean = false,
    val fullTextAccumulated: String = "",
    val finishReason: String? = null
)

interface AIProvider {
    val name: String
    val providerType: ProviderType
    suspend fun isAvailable(): Boolean
    suspend fun generate(request: AIRequest): Result<AIResponse>
    fun stream(request: AIRequest): Flow<AIChunk>
}
