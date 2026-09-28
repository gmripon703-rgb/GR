package com.example.ai.local

import com.example.ai.AIChunk
import com.example.ai.AIProvider
import com.example.ai.AIRequest
import com.example.ai.AIResponse
import com.example.ai.ProviderType
import com.example.modelmanager.ModelDownloadManager
import kotlinx.coroutines.flow.Flow
import java.io.File

class LocalAIProvider(
    private val modelDownloadManager: ModelDownloadManager,
    val runtime: LocalModelRuntime = LlamaCppRuntimeAdapter()
) : AIProvider {

    override val name: String = "Local On-Device Engine"
    override val providerType: ProviderType = ProviderType.LOCAL

    override suspend fun isAvailable(): Boolean {
        // Always available offline because of the on-device rule engine fallback,
        // and optionally backed by an installed GGUF model binary
        return true
    }

    suspend fun tryLoadInstalledModel(modelId: String): Boolean {
        val file = modelDownloadManager.getModelFile(modelId)
        if (file.exists() && file.length() > 0) {
            val cores = Runtime.getRuntime().availableProcessors().coerceIn(2, 4)
            val res = runtime.loadModel(file, cores)
            return res.isSuccess
        }
        return false
    }

    override suspend fun generate(request: AIRequest): Result<AIResponse> {
        val llamaAdapter = runtime as? LlamaCppRuntimeAdapter
        return if (runtime.isModelLoaded && llamaAdapter?.isNativeInferenceAvailable == true) {
            runtime.generate(request)
        } else {
            val answer = LocalRuleEngineFallback.generateOfflineResponse(request)
            Result.success(
                AIResponse(
                    text = answer,
                    modelName = "On-Device Rule Engine (Offline)",
                    providerType = ProviderType.LOCAL
                )
            )
        }
    }

    override fun stream(request: AIRequest): Flow<AIChunk> {
        val llamaAdapter = runtime as? LlamaCppRuntimeAdapter
        return if (runtime.isModelLoaded && llamaAdapter?.isNativeInferenceAvailable == true) {
            runtime.stream(request)
        } else {
            LocalRuleEngineFallback.streamOfflineResponse(request)
        }
    }
}
