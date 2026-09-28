package com.example.team

import com.example.storage.dao.TeamRuleDao
import com.example.storage.entity.TeamRuleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TeamRuleEngine(private val teamRuleDao: TeamRuleDao) {

    val allRules: Flow<List<TeamRuleEntity>> = teamRuleDao.getAllRules()

    companion object {
        val DEFAULT_RULES = listOf(
            TeamRuleEntity(
                title = "Zero Hardcoded Credentials",
                category = "SECURITY",
                priority = 1, // Platform / Security requirement (Highest)
                ruleContent = "Never store plaintext API tokens, AWS keys, passwords, or private cryptographic keys in source code. Inject via environment variables, BuildConfig, or hardware-backed Keystore.",
                isStrict = true
            ),
            TeamRuleEntity(
                title = "Mandatory Parameterized Queries",
                category = "SECURITY",
                priority = 1,
                ruleContent = "Never concatenate user variables into SQL or raw queries. Always use PreparedStatements, Room annotations (:param), or bound parameters to prevent SQL injection.",
                isStrict = true
            ),
            TeamRuleEntity(
                title = "Kotlin Coroutines & Flow Standards",
                category = "CODING_STANDARDS",
                priority = 2, // Team rule
                ruleContent = "All asynchronous operations and disk/network I/O must execute on Dispatchers.IO. UI state must be exposed as immutable StateFlow.",
                isStrict = false
            ),
            TeamRuleEntity(
                title = "No Destructive Automatic Shell Execution",
                category = "SECURITY",
                priority = 1,
                ruleContent = "Do not auto-execute shell commands. Present commands clearly inside interactive blocks with copy buttons and explain risks.",
                isStrict = true
            ),
            TeamRuleEntity(
                title = "Enforce HTTPS & Modern TLS",
                category = "DEPENDENCIES",
                priority = 2,
                ruleContent = "Cleartext HTTP traffic is strictly prohibited. Enforce TLS 1.3 or 1.2 with secure cipher suites (AES-GCM, ChaCha20-Poly1305).",
                isStrict = true
            )
        )
    }

    suspend fun ensureDefaultRulesInitialized() = withContext(Dispatchers.IO) {
        val count = teamRuleDao.getCount()
        if (count == 0) {
            teamRuleDao.insertAll(DEFAULT_RULES)
        }
    }

    suspend fun addRule(title: String, category: String, priority: Int, content: String, isStrict: Boolean) = withContext(Dispatchers.IO) {
        teamRuleDao.insertRule(
            TeamRuleEntity(
                title = title,
                category = category,
                priority = priority,
                ruleContent = content,
                isStrict = isStrict
            )
        )
    }

    suspend fun updateRule(rule: TeamRuleEntity) = withContext(Dispatchers.IO) {
        teamRuleDao.updateRule(rule)
    }

    suspend fun deleteRule(id: Long) = withContext(Dispatchers.IO) {
        teamRuleDao.deleteRule(id)
    }

    suspend fun buildHierarchicalSystemPrompt(projectRules: String? = null, userPreferences: String? = null): String = withContext(Dispatchers.IO) {
        val activeRules = teamRuleDao.getActiveRules()

        buildString {
            appendLine("=== SYSTEM GOVERNANCE & HIERARCHICAL INSTRUCTIONS ===")
            appendLine("Enforce the following order of precedence:")
            appendLine("1. Platform / Security Guardrails > 2. Team Rules > 3. Project Rules > 4. User Preferences > 5. Conversation Context")
            appendLine()

            appendLine("[Level 1 & 2: Active Security & Team Rules]")
            activeRules.forEach { rule ->
                val strictText = if (rule.isStrict) "[MANDATORY]" else "[GUIDELINE]"
                appendLine("• $strictText (${rule.category} - Priority ${rule.priority}) ${rule.title}: ${rule.ruleContent}")
            }

            if (!projectRules.isNullOrBlank()) {
                appendLine()
                appendLine("[Level 3: Project Rules]")
                appendLine(projectRules)
            }

            if (!userPreferences.isNullOrBlank()) {
                appendLine()
                appendLine("[Level 4: User Preferences]")
                appendLine(userPreferences)
            }
            appendLine("=== END SYSTEM GOVERNANCE ===")
        }
    }

    suspend fun exportRulesToJson(): String = withContext(Dispatchers.IO) {
        val activeRules = teamRuleDao.getActiveRules()
        val root = org.json.JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())
        root.put("app", "DevSec AI")

        val array = org.json.JSONArray()
        activeRules.forEach { rule ->
            val obj = org.json.JSONObject()
            obj.put("title", rule.title)
            obj.put("category", rule.category)
            obj.put("priority", rule.priority)
            obj.put("ruleContent", rule.ruleContent)
            obj.put("isEnabled", rule.isEnabled)
            obj.put("isStrict", rule.isStrict)
            array.put(obj)
        }
        root.put("rules", array)
        root.toString(2)
    }

    suspend fun importRulesFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = org.json.JSONObject(jsonString)
            val array = root.optJSONArray("rules") ?: org.json.JSONArray()
            var importedCount = 0

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val title = obj.optString("title", "Custom Team Rule")
                val category = obj.optString("category", "CODING_STANDARDS")
                val priority = obj.optInt("priority", 2)
                val content = obj.optString("ruleContent", "")
                val isStrict = obj.optBoolean("isStrict", true)

                if (content.isNotBlank()) {
                    teamRuleDao.insertRule(
                        TeamRuleEntity(
                            title = title,
                            category = category,
                            priority = priority,
                            ruleContent = content,
                            isEnabled = true,
                            isStrict = isStrict
                        )
                    )
                    importedCount++
                }
            }
            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAndSyncRemoteRules(url: String, okHttpClient: okhttp3.OkHttpClient): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = com.example.modelmanager.GitHubReleaseConfig.convertGoogleDriveUrl(url.trim())
            val request = okhttp3.Request.Builder().url(cleanUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(IllegalStateException("HTTP ${response.code}: ${response.message}"))
            }
            val body = response.body?.string() ?: return@withContext Result.failure(IllegalStateException("Empty response body"))
            importRulesFromJson(body)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

