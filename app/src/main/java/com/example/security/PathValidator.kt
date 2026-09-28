package com.example.security

import java.io.File

object PathValidator {

    fun validateSafeModelFileName(fileName: String): Boolean {
        if (fileName.isBlank()) return false
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) return false
        if (fileName.contains("\u0000")) return false
        // Must end with safe model extension such as .gguf, .bin, .onnx, .tflite, .json
        val allowedExtensions = listOf(".gguf", ".bin", ".onnx", ".tflite", ".json")
        return allowedExtensions.any { fileName.endsWith(it, ignoreCase = true) }
    }

    fun isPathWithinSandbox(targetFile: File, baseDirectory: File): Boolean {
        return try {
            val canonicalTarget = targetFile.canonicalPath
            val canonicalBase = baseDirectory.canonicalPath
            canonicalTarget.startsWith(canonicalBase)
        } catch (_: Exception) {
            false
        }
    }
}
