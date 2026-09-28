package com.example.ai.cloud

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

class CloudAIProvider(
    private val secureStorage: SecureStorage,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) : AIProvider {

    override val name: String = "Cloud / Remote LAN AI"
    override val providerType: ProviderType = ProviderType.CLOUD

    override suspend fun isAvailable(): Boolean {
        if (!secureStorage.allowCloudAi) return false
        val key = secureStorage.getEffectiveApiKey()
        val customUrl = secureStorage.customBaseUrl
        return key.isNotBlank() || customUrl.isNotBlank()
    }

    override suspend fun generate(request: AIRequest): Result<AIResponse> = withContext(Dispatchers.IO) {
        if (!secureStorage.allowCloudAi) {
            return@withContext Result.failure(IllegalStateException("Cloud AI is disabled in privacy settings."))
        }

        val apiKey = secureStorage.getEffectiveApiKey()
        val customUrl = secureStorage.customBaseUrl
        val model = secureStorage.activeOnlineModel

        if (apiKey.isBlank() && customUrl.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("No API key or custom endpoint configured for Cloud AI.")
            )
        }

        val start = System.currentTimeMillis()
        try {
            val endpoint = if (customUrl.isNotBlank()) {
                val base = if (customUrl.endsWith("/")) customUrl else "$customUrl/"
                "${base}v1beta/models/$model:generateContent?key=$apiKey"
            } else {
                "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            }

            val payload = buildJsonPayload(request)
            val reqBody = payload.toRequestBody("application/json; charset=utf-8".toMediaType())

            val httpRequest = Request.Builder()
                .url(endpoint)
                .post(reqBody)
                .build()

            val response = okHttpClient.newCall(httpRequest).execute()
            val latency = System.currentTimeMillis() - start

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                return@withContext Result.failure(
                    IllegalStateException("Cloud API error (${response.code}): $errorBody")
                )
            }

            val respBody = response.body?.string() ?: ""
            val json = JSONObject(respBody)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")
                ?: json.optJSONObject("error")?.optString("message")
                ?: "Empty cloud response"

            Result.success(
                AIResponse(
                    text = text,
                    modelName = model,
                    providerType = ProviderType.CLOUD,
                    latencyMs = latency
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun stream(request: AIRequest): Flow<AIChunk> = flow {
        // First try to fetch response
        val genResult = generate(request)
        if (genResult.isSuccess) {
            val text = genResult.getOrNull()?.text ?: ""
            val words = text.split(" ")
            val acc = StringBuilder()
            for ((idx, word) in words.withIndex()) {
                delay(12)
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
            val err = genResult.exceptionOrNull()?.message ?: "Unknown cloud error"
            emit(
                AIChunk(
                    deltaText = "⚠️ Cloud Request Failed: $err",
                    isFinal = true,
                    fullTextAccumulated = "⚠️ Cloud Request Failed: $err"
                )
            )
        }
    }

    private fun buildJsonPayload(request: AIRequest): String {
        val root = JSONObject()
        val contents = JSONArray()
        val contentObj = JSONObject()
        contentObj.put("role", "user")

        val parts = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", request.prompt)
        parts.put(partObj)

        if (!request.attachedImageData.isNullOrBlank()) {
            val imagePart = JSONObject()
            val inlineData = JSONObject()
            inlineData.put("mimeType", request.imageMimeType ?: "image/jpeg")
            inlineData.put("data", request.attachedImageData)
            imagePart.put("inlineData", inlineData)
            parts.put(imagePart)
        }

        contentObj.put("parts", parts)
        contents.put(contentObj)
        root.put("contents", contents)

        request.systemInstruction?.let { sys ->
            val sysObj = JSONObject()
            val sysParts = JSONArray()
            val sysPart = JSONObject()
            sysPart.put("text", sys)
            sysParts.put(sysPart)
            sysObj.put("parts", sysParts)
            root.put("systemInstruction", sysObj)
        }

        val genConfig = JSONObject()
        genConfig.put("temperature", request.temperature)
        genConfig.put("maxOutputTokens", request.maxTokens)
        root.put("generationConfig", genConfig)

        return root.toString()
    }
}
