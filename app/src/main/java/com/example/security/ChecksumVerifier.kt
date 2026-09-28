package com.example.security

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object ChecksumVerifier {

    fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(64 * 1024)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val hashBytes = digest.digest()
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifySha256(file: File, expectedSha256: String): Boolean {
        if (!file.exists() || !file.isFile) return false
        val cleanExpected = expectedSha256.trim().lowercase()
        if (cleanExpected.isEmpty()) return false
        val calculated = calculateSha256(file).lowercase()
        return calculated == cleanExpected
    }
}
