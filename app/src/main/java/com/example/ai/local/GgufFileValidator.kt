package com.example.ai.local

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class GgufHeaderInfo(
    val version: Int,
    val tensorCount: Long,
    val metadataKvCount: Long
)

/**
 * Validates GGUF model files according to the official GGUF specification.
 * Valid files start with magic bytes 'GGUF' (0x47, 0x47, 0x55, 0x46)
 * followed by a 32-bit integer version (version 2 or 3).
 */
object GgufFileValidator {

    private val GGUF_MAGIC = byteArrayOf('G'.code.toByte(), 'G'.code.toByte(), 'U'.code.toByte(), 'F'.code.toByte())

    fun validate(file: File): Result<GgufHeaderInfo> {
        if (!file.exists()) {
            return Result.failure(IllegalArgumentException("Model file does not exist: ${file.name}"))
        }

        if (file.length() < 16L) {
            return Result.failure(IllegalArgumentException("Model file is too small to be a valid GGUF file (${file.length()} bytes)"))
        }

        return try {
            RandomAccessFile(file, "r").use { raf ->
                val headerBytes = ByteArray(16)
                raf.readFully(headerBytes)

                val buffer = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)

                // Check magic bytes
                val magic = ByteArray(4)
                buffer.get(magic)
                if (!magic.contentEquals(GGUF_MAGIC)) {
                    val magicStr = magic.joinToString("") { String.format("%02X", it) }
                    return Result.failure(
                        IllegalArgumentException("Invalid GGUF binary: magic header mismatch (expected 0x47475546, found 0x$magicStr)")
                    )
                }

                val version = buffer.int
                if (version !in 2..3) {
                    return Result.failure(
                        IllegalArgumentException("Unsupported GGUF version: $version (expected version 2 or 3)")
                    )
                }

                val tensorCount = buffer.long
                // If more bytes available, read metadata count
                val metadataKvCount = if (raf.length() >= 24) {
                    val metaBytes = ByteArray(8)
                    raf.readFully(metaBytes)
                    ByteBuffer.wrap(metaBytes).order(ByteOrder.LITTLE_ENDIAN).long
                } else {
                    0L
                }

                Result.success(
                    GgufHeaderInfo(
                        version = version,
                        tensorCount = tensorCount,
                        metadataKvCount = metadataKvCount
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Corrupted GGUF file or I/O error: ${e.message}", e))
        }
    }
}
