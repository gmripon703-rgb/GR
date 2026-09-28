package com.example.ai

import com.example.ai.cloud.CloudAIProvider
import com.example.ai.custom.OptionalCustomProvider
import com.example.ai.local.LocalAIProvider
import com.example.core.NetworkMonitor
import com.example.security.SecureStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AIProviderRouter(
    private val localAIProvider: LocalAIProvider,
    private val cloudAIProvider: CloudAIProvider,
    private val networkMonitor: NetworkMonitor,
    private val secureStorage: SecureStorage,
    private val customProvider: OptionalCustomProvider = OptionalCustomProvider(secureStorage)
) {

    fun determineActiveProvider(): Pair<AIProvider, String> {
        val mode = secureStorage.providerMode
        val isOnline = networkMonitor.isOnline()
        val allowCloud = secureStorage.allowCloudAi

        return when (mode) {
            "OFFLINE_ONLY" -> {
                localAIProvider to "Offline Only Mode (Local Compute)"
            }
            "CUSTOM_LAN" -> {
                if (secureStorage.customBaseUrl.isNotBlank()) {
                    customProvider to "Self-Hosted LAN AI (${secureStorage.activeOnlineModel.ifBlank { "LAN Model" }})"
                } else {
                    localAIProvider to "No LAN URL configured (Fallback to Local)"
                }
            }
            "ONLINE_ONLY" -> {
                if (!isOnline) {
                    localAIProvider to "Online Only selected, but device is offline (Fallback to Local)"
                } else if (!allowCloud) {
                    localAIProvider to "Cloud AI disabled in privacy settings (Fallback to Local)"
                } else {
                    cloudAIProvider to "Online Cloud AI"
                }
            }
            else -> { // "AUTO"
                val hasCloudCredentials = secureStorage.getEffectiveApiKey().isNotBlank() || secureStorage.customBaseUrl.isNotBlank()
                if (isOnline && allowCloud && hasCloudCredentials) {
                    cloudAIProvider to "Auto: Cloud Available"
                } else {
                    localAIProvider to "Auto: On-Device Engine (Offline)"
                }
            }
        }
    }

    suspend fun generate(request: AIRequest): Result<AIResponse> {
        val (provider, status) = determineActiveProvider()

        val result = provider.generate(request)
        if (result.isSuccess) {
            return result
        }

        // If cloud fails, gracefully fall back to local provider
        if (provider is CloudAIProvider) {
            return localAIProvider.generate(request)
        }

        return result
    }

    fun stream(request: AIRequest): Flow<AIChunk> = flow {
        val (provider, status) = determineActiveProvider()

        try {
            provider.stream(request).collect { chunk ->
                emit(chunk)
            }
        } catch (e: Exception) {
            // Graceful fallback to local provider if cloud streaming fails mid-request
            if (provider is CloudAIProvider) {
                emit(
                    AIChunk(
                        deltaText = "\n[Cloud stream interrupted, falling back to on-device engine]\n",
                        isFinal = false
                    )
                )
                localAIProvider.stream(request).collect { localChunk ->
                    emit(localChunk)
                }
            } else {
                emit(
                    AIChunk(
                        deltaText = "\nGeneration failed: ${e.localizedMessage}",
                        isFinal = true
                    )
                )
            }
        }
    }
}
