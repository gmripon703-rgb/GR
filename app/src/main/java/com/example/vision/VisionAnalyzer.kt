package com.example.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

data class VisualAnalysisResult(
    val imageType: VisualImageType,
    val description: String,
    val suggestedPrompt: String,
    val base64Data: String?,
    val detectedCodeSnippet: String? = null
)

enum class VisualImageType(val label: String) {
    CODE_SCREENSHOT("Code Screenshot"),
    ARCHITECTURE_DIAGRAM("Architecture Diagram"),
    TERMINAL_LOG("Terminal / Crash Log"),
    UI_MOCKUP("UI Mockup / Wireframe"),
    SECURITY_REPORT("Penetration Test / Security Report"),
    GENERAL("General Technical Image")
}

object VisionAnalyzer {

    suspend fun processImageUri(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024
    ): VisualAnalysisResult = withContext(Dispatchers.IO) {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (originalBitmap == null) {
            return@withContext VisualAnalysisResult(
                imageType = VisualImageType.GENERAL,
                description = "Could not decode image from provided URI.",
                suggestedPrompt = "Please analyze this image.",
                base64Data = null
            )
        }

        // Scale bitmap to reasonable dimensions for memory efficiency and token limits
        val scaledBitmap = scaleBitmapToMax(originalBitmap, maxWidth, maxHeight)
        val base64String = bitmapToBase64(scaledBitmap)

        // Offline heuristic classification
        val imageType = classifyImageTypeHeuristically(scaledBitmap)
        val (description, suggestedPrompt) = generatePromptForType(imageType)

        VisualAnalysisResult(
            imageType = imageType,
            description = description,
            suggestedPrompt = suggestedPrompt,
            base64Data = base64String
        )
    }

    private fun scaleBitmapToMax(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxWidth && height <= maxHeight) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (ratio > 1) {
            newWidth = maxWidth
            newHeight = (maxWidth / ratio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (maxHeight * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun classifyImageTypeHeuristically(bitmap: Bitmap): VisualImageType {
        // Sample pixel characteristics:
        // Dark backgrounds with monospaced code usually have low average brightness and high contrast
        var darkPixelCount = 0
        val sampleStep = 8
        var totalSamples = 0

        for (x in 0 until bitmap.width step sampleStep) {
            for (y in 0 until bitmap.height step sampleStep) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff
                val brightness = (r * 299 + g * 587 + b * 114) / 1000
                if (brightness < 50) {
                    darkPixelCount++
                }
                totalSamples++
            }
        }

        val darkRatio = darkPixelCount.toFloat() / totalSamples.coerceAtLeast(1)

        return when {
            darkRatio > 0.65f -> VisualImageType.CODE_SCREENSHOT // Typical IDE/Terminal dark theme
            darkRatio > 0.40f -> VisualImageType.TERMINAL_LOG
            bitmap.width > bitmap.height * 1.5 -> VisualImageType.ARCHITECTURE_DIAGRAM
            else -> VisualImageType.UI_MOCKUP
        }
    }

    private fun generatePromptForType(type: VisualImageType): Pair<String, String> {
        return when (type) {
            VisualImageType.CODE_SCREENSHOT -> Pair(
                "Code screenshot detected (dark theme / editor layout).",
                "Please perform OCR on this code screenshot, transcribe it into markdown code block, and identify any bugs, performance issues, or security flaws."
            )
            VisualImageType.ARCHITECTURE_DIAGRAM -> Pair(
                "System architecture or flow diagram detected.",
                "Explain the components, data flows, and potential single points of failure or STRIDE security threats in this architecture diagram."
            )
            VisualImageType.TERMINAL_LOG -> Pair(
                "Terminal crash log or console output detected.",
                "Analyze this terminal error or stack trace screenshot, identify the root cause, and provide exact bash/cli fix commands."
            )
            VisualImageType.UI_MOCKUP -> Pair(
                "User interface mockup or wireframe detected.",
                "Review this UI layout from an Android Jetpack Compose perspective. Suggest composable structure, M3 theming, and accessibility improvements."
            )
            VisualImageType.SECURITY_REPORT -> Pair(
                "Penetration testing or vulnerability scan report detected.",
                "Review these scan findings, prioritize vulnerabilities by CVSS severity, and provide actionable remediation steps."
            )
            VisualImageType.GENERAL -> Pair(
                "Technical image attached.",
                "Analyze the technical diagram or image attached and explain its key details."
            )
        }
    }
}
