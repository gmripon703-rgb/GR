package com.example.cybersecurity

import com.example.ai.AIRequest
import com.example.ai.TaskType
import com.example.security.CommandSafetyEvaluation
import com.example.security.SafeCommandGuard

enum class SecurityWorkspaceMode(val title: String) {
    DEFENSIVE_AUDIT("Defensive Audit"),
    LOG_ANALYZER("Log Triage"),
    THREAT_MODELING("Threat Model (STRIDE)"),
    CVE_EXPLORER("CVE / CWE Guidance"),
    COMMAND_INSPECTOR("Safe Command Guard")
}

object CybersecurityEngine {

    fun analyzeLogSnippet(rawLogs: String): AIRequest {
        val prompt = buildString {
            appendLine("🛡️ **Defensive Incident Analysis Request: Log Triage**")
            appendLine("Examine the following system/application log entries:")
            appendLine()
            appendLine("```log")
            appendLine(rawLogs.take(2000))
            appendLine("```")
            appendLine()
            appendLine("Please provide:")
            appendLine("1. Identified anomalies, failed authentication spikes, or suspicious IP access patterns.")
            appendLine("2. Severity classification (Low, Medium, High, Critical).")
            appendLine("3. Recommended immediate defensive containment actions (e.g. firewall rule, credential revocation).")
        }

        return AIRequest(
            prompt = prompt,
            taskType = TaskType.TEXT,
            temperature = 0.2f
        )
    }

    fun generateThreatModel(assetDescription: String): AIRequest {
        val prompt = buildString {
            appendLine("🛡️ **STRIDE Threat Modeling Evaluation**")
            appendLine("System / Component description:")
            appendLine(assetDescription)
            appendLine()
            appendLine("Map threats across all 6 STRIDE pillars:")
            appendLine("• **S**poofing identity")
            appendLine("• **T**ampering with data")
            appendLine("• **R**epudiation")
            appendLine("• **I**nformation disclosure")
            appendLine("• **D**enial of service")
            appendLine("• **E**levation of privilege")
            appendLine()
            appendLine("For each pillar, detail concrete defensive architecture controls.")
        }

        return AIRequest(
            prompt = prompt,
            taskType = TaskType.TEXT,
            temperature = 0.2f
        )
    }

    fun inspectCommand(command: String): CommandSafetyEvaluation {
        return SafeCommandGuard.evaluate(command)
    }
}
