package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AuditFindingEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.OwaspItemEntity
import com.example.data.remote.ApiClientFactory
import com.example.data.remote.ContentDto
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.GenerationConfigDto
import com.example.data.remote.PartDto
import com.example.domain.analyzer.OfflineVulnerabilityScanner
import com.example.domain.analyzer.OwaspCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class DevSecRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val chatDao = db.chatDao()
    private val auditDao = db.auditDao()
    private val owaspDao = db.owaspDao()

    private val prefs: SharedPreferences = context.getSharedPreferences("devsec_settings", Context.MODE_PRIVATE)

    companion object {
        const val PREF_SELECTED_MODEL = "selected_model"
        const val PREF_CUSTOM_API_KEY = "custom_api_key"
        const val PREF_CUSTOM_BASE_URL = "custom_base_url"

        const val DEFAULT_MODEL = "gemini-3.5-flash"
        const val MODEL_PRO = "gemini-3.1-pro-preview"
        const val MODEL_LITE = "gemini-3.1-flash-lite-preview"

        private const val SYSTEM_PROMPT = """You are DevSec AI, an expert private IT Security & DevSecOps Engineering Assistant.
Your core objectives are defensive cybersecurity, secure coding, self-penetration testing guidance to protect company systems, and all-round IT engineering.
When analyzing vulnerabilities:
1. Identify the CWE/OWASP classification and explain the exact exploit mechanism clearly.
2. Provide a production-ready, hardened code patch demonstrating proper security controls.
3. Include actionable defensive hardening tips (e.g. firewall, headers, least privilege).
Keep answers structured, concise, and professional with code snippets formatted clearly.
All advice is for defensive development and protecting authorized infrastructure."""
    }

    fun getSelectedModel(): String {
        return prefs.getString(PREF_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    fun setSelectedModel(model: String) {
        prefs.edit().putString(PREF_SELECTED_MODEL, model).apply()
    }

    fun getCustomApiKey(): String {
        return prefs.getString(PREF_CUSTOM_API_KEY, "") ?: ""
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(PREF_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun getCustomBaseUrl(): String {
        return prefs.getString(PREF_CUSTOM_BASE_URL, "") ?: ""
    }

    fun setCustomBaseUrl(url: String) {
        prefs.edit().putString(PREF_CUSTOM_BASE_URL, url.trim()).apply()
    }

    private fun resolveApiKey(): String {
        val customKey = getCustomApiKey()
        if (customKey.isNotBlank()) return customKey

        // Fallback to BuildConfig injected from .env / Secrets panel
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") key else ""
        } catch (_: Exception) {
            ""
        }
    }

    // Chat Flows
    val chatMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

    suspend fun sendChatMessage(userText: String, isCodeAudit: Boolean = false): Result<String> = withContext(Dispatchers.IO) {
        // Save user message to Room DB
        val userEntity = ChatMessageEntity(
            role = "user",
            content = userText,
            modelUsed = getSelectedModel(),
            isCodeAudit = isCodeAudit
        )
        chatDao.insertMessage(userEntity)

        val apiKey = resolveApiKey()
        val customUrl = getCustomBaseUrl()
        val selectedModel = getSelectedModel()

        if (apiKey.isBlank() && customUrl.isBlank()) {
            val fallbackResponse = """
⚠️ **API Key Not Configured Yet**

To enable live Gemini AI model generation:
1. Open the top-right **Settings (⚙️)** in DevSec AI.
2. Enter your Gemini API key (or custom endpoint/local LLM URL).
*(Or inject `GEMINI_API_KEY` in the AI Studio Secrets panel)*

🛡️ **Good news:** All offline cybersecurity tools, static code vulnerability scanners, OWASP checklists, CIDR calculators, and local playbooks work 100% offline on-device without an API key!
            """.trimIndent()

            val aiEntity = ChatMessageEntity(
                role = "model",
                content = fallbackResponse,
                modelUsed = "offline-assistant"
            )
            chatDao.insertMessage(aiEntity)
            return@withContext Result.success(fallbackResponse)
        }

        try {
            val apiService = ApiClientFactory.createService(customUrl.ifBlank { null })

            // Collect recent conversation context (last 6 messages)
            val history = chatDao.getAllMessages().first().takeLast(6)
            val contents = history.map { msg ->
                ContentDto(
                    role = if (msg.role == "user") "user" else "model",
                    parts = listOf(PartDto(text = msg.content))
                )
            }

            val request = GenerateContentRequest(
                contents = contents,
                generationConfig = GenerationConfigDto(temperature = 0.3f),
                systemInstruction = ContentDto(parts = listOf(PartDto(text = SYSTEM_PROMPT)))
            )

            val response = apiService.generateContent(
                model = selectedModel,
                apiKey = apiKey,
                request = request
            )

            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: response.error?.message
                ?: "No response generated by the model."

            val modelEntity = ChatMessageEntity(
                role = "model",
                content = replyText,
                modelUsed = selectedModel,
                isCodeAudit = isCodeAudit
            )
            chatDao.insertMessage(modelEntity)
            Result.success(replyText)
        } catch (e: Exception) {
            val errorMsg = "⚠️ Connection error: ${e.localizedMessage ?: "Unknown error"}. Check your API Key or Network connection in Settings."
            val errorEntity = ChatMessageEntity(
                role = "model",
                content = errorMsg,
                modelUsed = selectedModel
            )
            chatDao.insertMessage(errorEntity)
            Result.failure(e)
        }
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        chatDao.clearHistory()
    }

    // Code Scanner
    val auditFindings: Flow<List<AuditFindingEntity>> = auditDao.getAllFindings()

    suspend fun runCodeScan(code: String): List<AuditFindingEntity> = withContext(Dispatchers.IO) {
        val findings = OfflineVulnerabilityScanner.scanCode(code)
        auditDao.clearFindings()
        auditDao.insertFindings(findings)
        findings
    }

    suspend fun clearAuditFindings() = withContext(Dispatchers.IO) {
        auditDao.clearFindings()
    }

    // OWASP Items
    val owaspItems: Flow<List<OwaspItemEntity>> = owaspDao.getAllItems()

    suspend fun ensureOwaspDataInitialized() = withContext(Dispatchers.IO) {
        val count = owaspDao.getCount()
        if (count == 0) {
            owaspDao.insertAll(OwaspCatalog.initialItems)
        }
    }

    suspend fun updateOwaspItemStatus(code: String, status: String, notes: String) = withContext(Dispatchers.IO) {
        owaspDao.updateStatus(code, status, notes)
    }

    suspend fun resetOwaspAudit() = withContext(Dispatchers.IO) {
        owaspDao.resetAllStatus()
    }
}
