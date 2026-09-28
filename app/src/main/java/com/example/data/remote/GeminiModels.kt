package com.example.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<ContentDto>,
    val generationConfig: GenerationConfigDto? = null,
    val systemInstruction: ContentDto? = null
)

@JsonClass(generateAdapter = true)
data class ContentDto(
    val role: String? = null,
    val parts: List<PartDto>
)

@JsonClass(generateAdapter = true)
data class PartDto(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfigDto(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<CandidateDto>? = null,
    val error: ApiErrorDto? = null
)

@JsonClass(generateAdapter = true)
data class CandidateDto(
    val content: ContentDto? = null,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiErrorDto(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)
