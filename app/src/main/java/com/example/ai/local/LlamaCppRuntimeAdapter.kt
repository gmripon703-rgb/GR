package com.example.ai.local

import com.example.ai.AIChunk
import com.example.ai.AIRequest
import com.example.ai.AIResponse
import com.example.ai.ProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Adapter for GGUF model loading and native llama.cpp execution.
 *
 * When native JNI libraries (libllama.so) are not bundled in the build, this adapter
 * validates GGUF binary format and header integrity, but transparently reports that
 * native neural inference is pending JNI library linkage. It does NOT fabricate fake
 * neural outputs or pretend to run neural weights.
 */
class LlamaCppRuntimeAdapter : LocalModelRuntime {

    override val runtimeName: String = "GGUF Runtime (llama.cpp JNI placeholder)"
    override var isModelLoaded: Boolean = false
        private set
    override var currentModelPath: String? = null
        private set

    var headerInfo: GgufHeaderInfo? = null
        private set

    val isNativeInferenceAvailable: Boolean by lazy {
        try {
            System.loadLibrary("llama")
            true
        } catch (_: UnsatisfiedLinkError) {
            false
        } catch (_: Exception) {
            false
        }
    }

    private var activeThreadCount: Int = 4
    private var loadedModelName: String = "none"

    override suspend fun loadModel(modelFile: File, threadCount: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val validation = GgufFileValidator.validate(modelFile)
        if (validation.isFailure) {
            return@withContext Result.failure(
                validation.exceptionOrNull() ?: IllegalArgumentException("Failed to validate GGUF file")
            )
        }

        headerInfo = validation.getOrNull()
        currentModelPath = modelFile.absolutePath
        activeThreadCount = threadCount.coerceIn(1, 8)
        loadedModelName = modelFile.nameWithoutExtension
        isModelLoaded = true

        Result.success(Unit)
    }

    override suspend fun unloadModel() = withContext(Dispatchers.IO) {
        isModelLoaded = false
        currentModelPath = null
        loadedModelName = "none"
        headerInfo = null
    }

    override suspend fun generate(request: AIRequest): Result<AIResponse> = withContext(Dispatchers.Default) {
        if (!isModelLoaded) {
            return@withContext Result.failure(IllegalStateException("No model loaded in GGUF runtime"))
        }

        if (!isNativeInferenceAvailable) {
            return@withContext Result.failure(
                UnsupportedOperationException(
                    "Native llama.cpp JNI library (libllama.so) is not bundled in this APK build. " +
                    "GGUF binary format is validated, but neural weights execution requires compiling libllama.so for device ABI. " +
                    "Please use the on-device Developer Rule Engine or Cloud AI."
                )
            )
        }

        // Native JNI inference call would occur here when libllama.so is linked
        Result.failure(UnsupportedOperationException("Native inference not implemented without libllama.so"))
    }

    override fun stream(request: AIRequest): Flow<AIChunk> = flow {
        if (!isModelLoaded) {
            emit(
                AIChunk(
                    deltaText = "Error: Local model is not loaded.",
                    isFinal = true,
                    fullTextAccumulated = "Error: Local model is not loaded."
                )
            )
            return@flow
        }

        if (!isNativeInferenceAvailable) {
            val notice = "⚠️ GGUF model format validated, but native llama.cpp library (libllama.so) is not bundled in this APK build. Switching to on-device Rule Engine."
            emit(
                AIChunk(
                    deltaText = notice,
                    isFinal = true,
                    fullTextAccumulated = notice
                )
            )
            return@flow
        }

        emit(
            AIChunk(
                deltaText = "Native runtime not available.",
                isFinal = true,
                fullTextAccumulated = "Native runtime not available."
            )
        )
    }
}
