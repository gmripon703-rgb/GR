package com.example.ai.custom

import com.example.ai.AIChunk
import com.example.ai.AIProvider
import com.example.ai.AIRequest
import com.example.ai.AIResponse
import com.example.ai.ProviderType
import com.example.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * OptionalCustomProvider enables connecting to custom LAN AI servers,
 * such as Ollama, vLLM, LM Studio, LocalAI, or an internal team AI gateway.
 * Compatible with the standard OpenAI /v1/chat/completions schema.
 */
class OptionalCustomProvider(
    private val secureStorage: SecureStorage,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) : AIProvider {

    override val name: String = "Custom LAN / Self-Hosted (Ollama, vLLM, LM Studio)"
    override val providerType: ProviderType = ProviderType.CUSTOM

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        val customUrl = secureStorage.customBaseUrl.trim()
        if (customUrl.isBlank()) return@withContext false

        try {
            // Quick health check / models query on endpoint
            val testUrl = if (customUrl.endsWith("/")) customUrl else "$customUrl/"
            val healthRequest = Request.Builder()
                .url("${testUrl}v1/models")
                .get()
                .build()
            val response = okHttpClient.newCall(healthRequest).execute()
            response.isSuccessful || response.code == 401 // Endpoint responds
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun generate(request: AIRequest): Result<AIResponse> = withContext(Dispatchers.IO) {
        val customUrl = secureStorage.customBaseUrl.trim()
        if (customUrl.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("No custom LAN server URL specified. Set endpoint in Settings.")
            )
        }

        val start = System.currentTimeMillis()
        try {
            val base = if (customUrl.endsWith("/")) customUrl else "$customUrl/"
            val endpoint = "${base}v1/chat/completions"
            val apiKey = secureStorage.customApiKey.trim()

            val payload = buildOpenAiChatPayload(request)
            val reqBody = payload.toRequestBody("application/json; charset=utf-8".toMediaType())

            val reqBuilder = Request.Builder()
                .url(endpoint)
                .post(reqBody)

            if (apiKey.isNotBlank()) {
                reqBuilder.addHeader("Authorization", "Bearer $apiKey")
            }

            val response = okHttpClient.newCall(reqBuilder.build()).execute()
            val latency = System.currentTimeMillis() - start

            if (!response.isSuccessful) {
                val err = response.body?.string() ?: ""
                return@withContext Result.failure(
                    IllegalStateException("LAN server HTTP error (${response.code}): $err")
                )
            }

            val respBody = response.body?.string() ?: ""
            val json = JSONObject(respBody)
            val choices = json.optJSONArray("choices")
            val firstChoice = choices?.optJSONObject(0)
            val message = firstChoice?.optJSONObject("message")
            val content = message?.optString("content") ?: "No content in response"

            Result.success(
                AIResponse(
                    text = content,
                    modelName = secureStorage.activeOnlineModel.ifBlank { "custom-lan-model" },
                    providerType = ProviderType.CUSTOM,
                    latencyMs = latency
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun stream(request: AIRequest): Flow<AIChunk> = flow {
        val genResult = generate(request)
        if (genResult.isSuccess) {
            val text = genResult.getOrNull()?.text ?: ""
            val words = text.split(" ")
            val acc = StringBuilder()
            for ((idx, word) in words.withIndex()) {
                delay(10)
                val chunk = if (idx == 0) word else " $word"
                acc.append(chunk)
                emit(
                    AIChunk(
                        deltaText = chunk,
                        isFinal = idx == words.size - 1,
                        fullTextAccumulated = acc.toString(),
                        finishReason = if (idx == words.size - 1) "stop" else null
                    )
                )
            }
        } else {
            val err = genResult.exceptionOrNull()?.message ?: "Custom LAN provider error"
            emit(
                AIChunk(
                    deltaText = "⚠️ Custom LAN AI Error: $err",
                    isFinal = true,
                    fullTextAccumulated = "⚠️ Custom LAN AI Error: $err"
                )
            )
        }
    }

    private fun buildOpenAiChatPayload(request: AIRequest): String {
        val root = JSONObject()
        val model = secureStorage.activeOnlineModel.ifBlank { "default" }
        root.put("model", model)
        root.put("temperature", request.temperature)
        root.put("max_tokens", request.maxTokens)

        val messages = JSONArray()

        request.systemInstruction?.let { sys ->
            val sysMsg = JSONObject()
            sysMsg.put("role", "system")
            sysMsg.put("content", sys)
            messages.put(sysMsg)
        }

        val userMsg = JSONObject()
        userMsg.put("role", "user")

        if (request.attachedImageData != null) {
            val contents = JSONArray()
            val textPart = JSONObject()
            textPart.put("type", "text")
            textPart.put("text", request.prompt)
            contents.put(textPart)

            val imagePart = JSONObject()
            imagePart.put("type", "image_url")
            val imageUrl = JSONObject()
            val mime = request.imageMimeType ?: "image/jpeg"
            imageUrl.put("url", "data:$mime;base64,${request.attachedImageData}")
            imagePart.put("image_url", imageUrl)
            contents.put(imagePart)

            userMsg.put("content", contents)
        } else {
            userMsg.put("content", request.prompt)
        }

        messages.put(userMsg)
        root.put("messages", messages)

        return root.toString()
    }
}
