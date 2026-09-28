package com.example.ai.local

import com.example.ai.AIChunk
import com.example.ai.AIRequest
import com.example.ai.AIResponse
import kotlinx.coroutines.flow.Flow
import java.io.File

interface LocalModelRuntime {
    val runtimeName: String
    val isModelLoaded: Boolean
    val currentModelPath: String?
    suspend fun loadModel(modelFile: File, threadCount: Int): Result<Unit>
    suspend fun unloadModel()
    suspend fun generate(request: AIRequest): Result<AIResponse>
    fun stream(request: AIRequest): Flow<AIChunk>
}
