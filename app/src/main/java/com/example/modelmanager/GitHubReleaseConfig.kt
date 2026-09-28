package com.example.modelmanager

/**
 * Configuration and presets for hosting and downloading large (~1.5 GB) files
 * via GitHub Releases, Google Drive, or Hugging Face over HTTPS.
 */
object GitHubReleaseConfig {

    /**
     * Standard GitHub Release asset format:
     * https://github.com/OWNER/REPOSITORY/releases/download/TAG/FILENAME
     */
    const val GITHUB_RELEASE_URL_TEMPLATE =
        "https://github.com/OWNER/REPOSITORY/releases/download/TAG/FILENAME"

    const val DEFAULT_GITHUB_RELEASE_URL =
        "https://github.com/OWNER/REPOSITORY/releases/download/v1.0.0/custom-team-rules.gguf"

    const val DEFAULT_MIN_STORAGE_GB = 2.0f
    const val DEFAULT_MIN_STORAGE_BYTES = (2.0 * 1024 * 1024 * 1024).toLong()

    data class PresetAsset(
        val label: String,
        val description: String,
        val url: String,
        val expectedSha256: String = "",
        val expectedSizeGb: Float = 1.0f,
        val assetType: String = "GGUF" // "GGUF", "JSON", "ZIP"
    )

    val PRESET_ASSETS = listOf(
        PresetAsset(
            label = "GitHub Release Template",
            description = "Standard 2GB free public release asset template (No login needed)",
            url = "https://github.com/OWNER/REPOSITORY/releases/download/v1.0.0/custom-team-model.gguf",
            expectedSizeGb = 1.5f,
            assetType = "GGUF"
        ),
        PresetAsset(
            label = "Qwen 2.5 Coder 1.5B (GGUF)",
            description = "Recommended 1.5B coding copilot hosted free on Hugging Face CDN (~940MB)",
            url = "https://huggingface.co/Qwen/Qwen2.5-Coder-1.5B-Instruct-GGUF/resolve/main/qwen2.5-coder-1.5b-instruct-q4_k_m.gguf",
            expectedSha256 = "679f22580a58a6ee5f272a8d5f30999554a938c5a0ec7b8cfd2a93da12484433",
            expectedSizeGb = 0.94f,
            assetType = "GGUF"
        ),
        PresetAsset(
            label = "DeepSeek Coder 1.3B (GGUF)",
            description = "High-accuracy defensive cybersecurity & code LLM (~830MB)",
            url = "https://huggingface.co/TheBloke/deepseek-coder-1.3b-instruct-GGUF/resolve/main/deepseek-coder-1.3b-instruct.Q4_K_M.gguf",
            expectedSha256 = "e2c38f16b251ce7efad2a0dca0bb7a33eeef117d7b3858fa7ee90a424e4f8809",
            expectedSizeGb = 0.83f,
            assetType = "GGUF"
        ),
        PresetAsset(
            label = "Custom Team Rules (JSON)",
            description = "Custom team governance & coding standards configuration bundle",
            url = "https://raw.githubusercontent.com/OWNER/REPOSITORY/main/team-rules.json",
            expectedSizeGb = 0.01f,
            assetType = "JSON"
        )
    )

    /**
     * Verifies if a given string matches the GitHub Release download asset structure.
     */
    fun isGitHubReleaseUrl(url: String): Boolean {
        val trimmed = url.trim()
        val regex = Regex("""^https://github\.com/[^/]+/[^/]+/releases/download/[^/]+/[^/]+""")
        return regex.containsMatchIn(trimmed)
    }

    /**
     * Extracts filename from URL, falling back to a default if unknown.
     */
    fun extractFilename(url: String, fallback: String = "custom-ai-asset.bin"): String {
        val clean = url.trim().substringBefore("?").substringBefore("#")
        val lastSegment = clean.substringAfterLast("/")
        return if (lastSegment.isNotBlank() && lastSegment.contains(".")) {
            lastSegment.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        } else {
            fallback
        }
    }

    /**
     * Converts a Google Drive sharing URL into a direct streaming download URL.
     * Handles formats like:
     * - https://drive.google.com/file/d/FILE_ID/view?usp=sharing
     * - https://drive.google.com/open?id=FILE_ID
     */
    fun convertGoogleDriveUrl(url: String): String {
        val trimmed = url.trim()
        if (!trimmed.contains("drive.google.com")) return trimmed

        val fileId = when {
            trimmed.contains("/file/d/") -> {
                trimmed.substringAfter("/file/d/").substringBefore("/")
            }
            trimmed.contains("id=") -> {
                trimmed.substringAfter("id=").substringBefore("&")
            }
            else -> null
        }

        return if (!fileId.isNullOrBlank()) {
            "https://drive.google.com/uc?export=download&id=$fileId"
        } else {
            trimmed
        }
    }
}
