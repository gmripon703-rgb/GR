package com.example.developer

import com.example.ai.AIRequest
import com.example.ai.TaskType

enum class DeveloperAction(val label: String, val promptPrefix: String) {
    EXPLAIN("Explain", "Explain the logic, architecture, and potential edge cases of the following code in detail:"),
    FIX("Fix Bugs", "Analyze the following code for syntax errors, logical bugs, and concurrency defects. Provide a robust corrected version:"),
    REFACTOR("Refactor", "Refactor the following code for clean architecture, SOLID principles, idiomatic style, and maintainability:"),
    OPTIMIZE("Optimize", "Optimize the following code for computational complexity (time & memory efficiency) and Android thread safety:"),
    TEST("Generate Tests", "Write comprehensive unit tests (JUnit 4/5, Kotlin coroutine test, and boundary condition checks) for:"),
    GENERATE("Generate Code", "Write a complete, production-ready implementation meeting this specification:")
}

object DeveloperToolsEngine {

    val supportedLanguages = listOf(
        "Kotlin",
        "Java",
        "Python",
        "Bash / Shell",
        "C / C++",
        "Gradle (KTS)",
        "Docker / Container",
        "Git",
        "SQL"
    )

    fun buildDeveloperRequest(
        action: DeveloperAction,
        language: String,
        codeSnippet: String,
        additionalNotes: String? = null
    ): AIRequest {
        val prompt = buildString {
            appendLine("${action.promptPrefix} [$language]")
            appendLine()
            appendLine("```${language.lowercase().substringBefore(" ")}")
            appendLine(codeSnippet.trim())
            appendLine("```")
            if (!additionalNotes.isNullOrBlank()) {
                appendLine()
                appendLine("Additional Context / Constraints:")
                appendLine(additionalNotes.trim())
            }
        }

        return AIRequest(
            prompt = prompt,
            taskType = TaskType.CODING,
            temperature = 0.2f,
            attachedCodeSnippet = codeSnippet,
            language = language
        )
    }

    fun parseStackTrace(rawTrace: String): String {
        val lines = rawTrace.lines()
        val exceptionLine = lines.firstOrNull { it.contains("Exception") || it.contains("Error") } ?: "Unknown Exception"
        val topFrame = lines.firstOrNull { it.trim().startsWith("at ") && !it.contains("android.os") } ?: "Unknown Frame"

        return buildString {
            appendLine("🔍 **Stack Trace Analysis Summary**")
            appendLine("- **Root Cause:** `$exceptionLine`")
            appendLine("- **Probable Culprit Frame:** `$topFrame`")
            appendLine()
            appendLine("Ask the Copilot: *'How to resolve $exceptionLine in $topFrame'*")
        }
    }
}
