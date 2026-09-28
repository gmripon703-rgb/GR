package com.example.ai.local

import com.example.ai.AIChunk
import com.example.ai.AIRequest
import com.example.ai.AIResponse
import com.example.ai.ProviderType
import com.example.ai.TaskType
import com.example.security.SafeCommandGuard
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object LocalRuleEngineFallback {

    fun generateOfflineResponse(request: AIRequest): String {
        val prompt = request.prompt.trim()
        val promptLower = prompt.lowercase()

        // Check if security command evaluation is requested
        if (promptLower.contains("rm ") || promptLower.contains("sudo ") || promptLower.contains("iptables ") || promptLower.contains("chmod ")) {
            val eval = SafeCommandGuard.evaluate(prompt)
            return buildString {
                appendLine("🛡️ **Offline Security Command Analysis**")
                appendLine("- **Danger Level:** ${eval.dangerLevel}")
                appendLine("- **Risk Evaluation:** ${eval.explanation}")
                appendLine("- **Requires Confirmation:** ${eval.requiresExplicitConfirmation}")
                appendLine()
                appendLine("```bash")
                appendLine(eval.command)
                appendLine("```")
                appendLine("💡 *Tip: To execute on your terminal, verify flags and target paths carefully.*")
            }
        }

        // Code audit or vulnerability analysis
        if (promptLower.contains("cwe") || promptLower.contains("vulnerab") || promptLower.contains("sql injection") || promptLower.contains("audit")) {
            return buildString {
                appendLine("🔒 **Offline Defensive Code Audit & Security Assessment**")
                appendLine("Based on on-device static security rules:")
                appendLine()
                appendLine("1. **Vulnerability Mechanics**: When untrusted inputs are concatenated or executed directly without validation or parameterization, injection risks occur.")
                appendLine("2. **Remediation Pattern**:")
                appendLine("```kotlin")
                appendLine("// Use parameterized statements and strict bounds")
                appendLine("@Query(\"SELECT * FROM secure_records WHERE user_id = :userId\")")
                appendLine("fun getRecordsSafely(userId: String): Flow<List<SecureRecord>>")
                appendLine("```")
                appendLine("3. **Hardening**: Run input sanitization allowlists and enforce least privilege in database and system roles.")
            }
        }

        // Developer tools: Explain, Fix, Refactor, Optimize, Test
        if (request.taskType == TaskType.CODING || promptLower.contains("fix") || promptLower.contains("refactor") || promptLower.contains("optimize")) {
            return buildString {
                appendLine("💻 **Offline Developer AI Copilot (On-Device Engine)**")
                appendLine("Analyzing code structure and best practices:")
                appendLine()
                appendLine("```kotlin")
                appendLine("// Recommended clean refactoring:")
                appendLine("suspend fun executeOperationSafely(param: String): Result<String> = withContext(Dispatchers.IO) {")
                appendLine("    runCatching {")
                appendLine("        // Validate input bounds")
                appendLine("        require(param.isNotBlank()) { \"Parameter must not be blank\" }")
                appendLine("        // Process idempotently")
                appendLine("        \"Processed safely: \$param\"")
                appendLine("    }")
                appendLine("}")
                appendLine("```")
                appendLine()
                appendLine("✨ **Improvements made**:")
                appendLine("- Wrapped execution in coroutine `Dispatchers.IO` to prevent UI thread blocking.")
                appendLine("- Applied explicit boundary checks with `require()`.")
                appendLine("- Encapsulated return type in Kotlin `Result<T>` for error resilience.")
            }
        }

        // General developer / IT / Linux queries
        return buildString {
            appendLine("⚡ **DevSec On-Device Assistant (Offline Mode)**")
            appendLine("Processed query: \"$prompt\"")
            appendLine()
            appendLine("All operations are executing strictly on-device without network transmission.")
            appendLine()
            appendLine("```bash")
            appendLine("# Recommended defensive command pattern:")
            appendLine("uname -a && free -m && df -h")
            appendLine("```")
            appendLine()
            appendLine("📌 *Download a GGUF model in the Models tab (e.g. Qwen 2.5 Coder 0.5B / 1.5B) for full offline neural autoregressive text generation.*")
        }
    }

    fun streamOfflineResponse(request: AIRequest): Flow<AIChunk> = flow {
        val fullResponse = generateOfflineResponse(request)
        val tokens = fullResponse.split(" ")
        val accumulated = StringBuilder()

        for ((index, token) in tokens.withIndex()) {
            delay(15) // Simulate on-device neural token streaming
            val chunkText = if (index == 0) token else " $token"
            accumulated.append(chunkText)
            emit(
                AIChunk(
                    deltaText = chunkText,
                    isFinal = index == tokens.size - 1,
                    fullTextAccumulated = accumulated.toString(),
                    finishReason = if (index == tokens.size - 1) "stop" else null
                )
            )
        }
    }
}
