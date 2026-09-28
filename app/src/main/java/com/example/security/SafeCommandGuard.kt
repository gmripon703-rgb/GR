package com.example.security

data class CommandSafetyEvaluation(
    val command: String,
    val isDangerous: Boolean,
    val dangerLevel: DangerLevel,
    val explanation: String,
    val requiresExplicitConfirmation: Boolean
)

enum class DangerLevel {
    SAFE,
    CAUTION,
    HIGH_RISK,
    DESTRUCTIVE
}

object SafeCommandGuard {

    private val destructivePatterns = listOf(
        Regex("""(?i)\brm\s+(-[a-zA-Z]*r[a-zA-Z]*f*|-rf|-fr)\s+.*(/|\*|~|etc|var|usr)"""),
        Regex("""(?i)\bmkfs(\.[a-z0-9]+)?\s+"""),
        Regex("""(?i)\bdd\s+if=.*of=/dev/"""),
        Regex("""(?i):\(\)\s*\{\s*:\s*\|\s*:\s*&\s*\}\s*;\s*:"""), // fork bomb
        Regex("""(?i)\bchmod\s+(-R\s+)?777\s+/"""),
        Regex("""(?i)\bshutdown\b|\breboot\b|\binit\s+0\b""")
    )

    private val highRiskPatterns = listOf(
        Regex("""(?i)\bsudo\s+"""),
        Regex("""(?i)\biptables\s+(-F|-X|--flush)"""),
        Regex("""(?i)\bufw\s+disable"""),
        Regex("""(?i)\bcurl\s+.*\|\s*(bash|sh)"""),
        Regex("""(?i)\bwget\s+.*\|\s*(bash|sh)"""),
        Regex("""(?i)\bkillall\s+-9"""),
        Regex("""(?i)\bDROP\s+DATABASE\b|\bDROP\s+TABLE\b"""),
        Regex("""(?i)\buserdel\b|\bgroupdel\b""")
    )

    private val cautionPatterns = listOf(
        Regex("""(?i)\bchmod\s+"""),
        Regex("""(?i)\bchown\s+"""),
        Regex("""(?i)\bkill\s+"""),
        Regex("""(?i)\bsystemctl\s+(stop|restart|disable)"""),
        Regex("""(?i)\bdocker\s+system\s+prune"""),
        Regex("""(?i)\bgit\s+reset\s+--hard"""),
        Regex("""(?i)\bgit\s+push\s+.*--force""")
    )

    fun evaluate(command: String): CommandSafetyEvaluation {
        val trimmed = command.trim()

        for (pattern in destructivePatterns) {
            if (pattern.containsMatchIn(trimmed)) {
                return CommandSafetyEvaluation(
                    command = trimmed,
                    isDangerous = true,
                    dangerLevel = DangerLevel.DESTRUCTIVE,
                    explanation = "Destructive command detected! This can delete entire filesystems, reformat storage, or permanently destroy system state.",
                    requiresExplicitConfirmation = true
                )
            }
        }

        for (pattern in highRiskPatterns) {
            if (pattern.containsMatchIn(trimmed)) {
                return CommandSafetyEvaluation(
                    command = trimmed,
                    isDangerous = true,
                    dangerLevel = DangerLevel.HIGH_RISK,
                    explanation = "High-risk command detected (elevated privileges, remote code piping, or firewall modification). Review parameters before execution.",
                    requiresExplicitConfirmation = true
                )
            }
        }

        for (pattern in cautionPatterns) {
            if (pattern.containsMatchIn(trimmed)) {
                return CommandSafetyEvaluation(
                    command = trimmed,
                    isDangerous = true,
                    dangerLevel = DangerLevel.CAUTION,
                    explanation = "Modifies access permissions, services, or repository history. Inspect arguments carefully.",
                    requiresExplicitConfirmation = false
                )
            }
        }

        return CommandSafetyEvaluation(
            command = trimmed,
            isDangerous = false,
            dangerLevel = DangerLevel.SAFE,
            explanation = "Standard developer command.",
            requiresExplicitConfirmation = false
        )
    }
}
