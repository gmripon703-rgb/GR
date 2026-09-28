package com.example.modelmanager

data class ModelInfo(
    val id: String,
    val name: String,
    val version: String,
    val format: String, // "GGUF", "LiteRT", "ONNX"
    val quantization: String, // "Q4_K_M", "Q4_0", "INT8", "FP16"
    val sizeBytes: Long,
    val minimumRamGb: Float,
    val recommendedRamGb: Float,
    val minimumStorageBytes: Long,
    val architecture: String, // "qwen2", "llama", "deepseek", "smollm"
    val downloadUrl: String,
    val sha256: String,
    val license: String, // "Apache-2.0", "MIT", "Llama-3.2-Community", etc.
    val sourceUrl: String,
    val author: String,
    val category: String, // "CODING", "GENERAL", "CYBERSECURITY", "EMBEDDINGS"
    val description: String
) {
    val sizeMb: Long get() = sizeBytes / (1024 * 1024)
    val sizeGbText: String get() = "%.2f GB".format(sizeBytes / (1024.0 * 1024.0 * 1024.0))
}

data class ModelManifest(
    val version: Int,
    val lastUpdated: Long,
    val models: List<ModelInfo>
)

enum class DownloadState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    PAUSED,
    VERIFYING,
    INSTALLED,
    ERROR,
    UPDATE_AVAILABLE
}

data class ModelDownloadProgress(
    val modelId: String,
    val state: DownloadState,
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0,
    val speedBytesPerSec: Long = 0,
    val percent: Int = 0,
    val errorMessage: String? = null,
    val localFilePath: String? = null
) {
    val speedText: String get() {
        if (speedBytesPerSec <= 0) return "-- KB/s"
        val mbPerSec = speedBytesPerSec / (1024.0 * 1024.0)
        return if (mbPerSec >= 1.0) "%.1f MB/s".format(mbPerSec) else "${speedBytesPerSec / 1024} KB/s"
    }

    val remainingBytes: Long get() = (totalBytes - bytesDownloaded).coerceAtLeast(0)
    val remainingMbText: String get() = "${remainingBytes / (1024 * 1024)} MB remaining"
}
