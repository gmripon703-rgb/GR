package com.example.domain.analyzer

import java.net.InetAddress
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import android.util.Base64

data class SubnetCalculationResult(
    val inputCidr: String,
    val networkAddress: String,
    val broadcastAddress: String,
    val firstUsableHost: String,
    val lastUsableHost: String,
    val netmask: String,
    val wildcardMask: String,
    val totalHosts: Long,
    val usableHosts: Long,
    val ipClass: String
)

object NetworkUtils {

    fun calculateSubnet(cidrString: String): Result<SubnetCalculationResult> {
        return runCatching {
            val parts = cidrString.trim().split("/")
            if (parts.size != 2) {
                throw IllegalArgumentException("Expected format: IP/CIDR (e.g. 192.168.1.1/24)")
            }
            val ipStr = parts[0].trim()
            val prefix = parts[1].trim().toIntOrNull() ?: throw IllegalArgumentException("Invalid prefix")
            if (prefix < 0 || prefix > 32) {
                throw IllegalArgumentException("Prefix must be between 0 and 32")
            }

            val ipBytes = InetAddress.getByName(ipStr).address
            if (ipBytes.size != 4) {
                throw IllegalArgumentException("Only IPv4 is supported for this calculation")
            }

            val ipInt = ((ipBytes[0].toLong() and 0xFF) shl 24) or
                    ((ipBytes[1].toLong() and 0xFF) shl 16) or
                    ((ipBytes[2].toLong() and 0xFF) shl 8) or
                    (ipBytes[3].toLong() and 0xFF)

            val maskInt = if (prefix == 0) 0L else (0xFFFFFFFFL shl (32 - prefix)) and 0xFFFFFFFFL
            val wildcardInt = maskInt.inv() and 0xFFFFFFFFL

            val networkInt = ipInt and maskInt
            val broadcastInt = networkInt or wildcardInt

            val total = if (prefix == 32) 1L else (1L shl (32 - prefix))
            val usable = if (prefix >= 31) (if (prefix == 32) 1L else 2L) else (total - 2L).coerceAtLeast(0L)

            val firstUsableInt = if (prefix >= 31) networkInt else networkInt + 1
            val lastUsableInt = if (prefix >= 31) broadcastInt else broadcastInt - 1

            val firstOctet = (ipBytes[0].toInt() and 0xFF)
            val ipClass = when {
                firstOctet in 1..126 -> "Class A (Private: 10.0.0.0/8)"
                firstOctet in 128..191 -> "Class B (Private: 172.16.0.0/12)"
                firstOctet in 192..223 -> "Class C (Private: 192.168.0.0/16)"
                firstOctet in 224..239 -> "Class D (Multicast)"
                else -> "Class E / Loopback"
            }

            SubnetCalculationResult(
                inputCidr = "$ipStr/$prefix",
                networkAddress = longToIp(networkInt),
                broadcastAddress = longToIp(broadcastInt),
                firstUsableHost = longToIp(firstUsableInt),
                lastUsableHost = longToIp(lastUsableInt),
                netmask = longToIp(maskInt),
                wildcardMask = longToIp(wildcardInt),
                totalHosts = total,
                usableHosts = usable,
                ipClass = ipClass
            )
        }
    }

    private fun longToIp(v: Long): String {
        return "${(v shr 24) and 0xFF}.${(v shr 16) and 0xFF}.${(v shr 8) and 0xFF}.${v and 0xFF}"
    }

    fun computeHash(text: String, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val hash = digest.digest(text.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun encodeBase64(text: String): String {
        return try {
            java.util.Base64.getEncoder().encodeToString(text.toByteArray(StandardCharsets.UTF_8))
        } catch (_: Throwable) {
            Base64.encodeToString(text.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        }
    }

    fun decodeBase64(encoded: String): Result<String> {
        return runCatching {
            val clean = encoded.trim()
            val bytes = try {
                java.util.Base64.getDecoder().decode(clean)
            } catch (_: Throwable) {
                Base64.decode(clean, Base64.DEFAULT)
            }
            String(bytes, StandardCharsets.UTF_8)
        }
    }

    fun encodeHex(text: String): String {
        return text.toByteArray(StandardCharsets.UTF_8).joinToString("") { "%02x".format(it) }
    }

    fun decodeHex(hex: String): Result<String> {
        return runCatching {
            val clean = hex.replace(" ", "").replace("0x", "")
            val bytes = ByteArray(clean.length / 2)
            for (i in bytes.indices) {
                val index = i * 2
                bytes[i] = clean.substring(index, index + 2).toInt(16).toByte()
            }
            String(bytes, StandardCharsets.UTF_8)
        }
    }

    fun encodeUrl(text: String): String {
        return URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
    }

    fun decodeUrl(text: String): Result<String> {
        return runCatching {
            URLDecoder.decode(text, StandardCharsets.UTF_8.toString())
        }
    }

    fun generateSecurityHeaders(
        enableHsts: Boolean = true,
        blockIframe: Boolean = true,
        blockSniffing: Boolean = true,
        strictCsp: Boolean = true
    ): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        if (enableHsts) {
            headers["Strict-Transport-Security"] = "max-age=63072000; includeSubDomains; preload"
        }
        if (blockIframe) {
            headers["X-Frame-Options"] = "DENY"
        }
        if (blockSniffing) {
            headers["X-Content-Type-Options"] = "nosniff"
        }
        headers["Referrer-Policy"] = "strict-origin-when-cross-origin"
        headers["Permissions-Policy"] = "camera=(), microphone=(), geolocation=(), payment=()"
        if (strictCsp) {
            headers["Content-Security-Policy"] = "default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none'; base-uri 'self'; form-action 'self';"
        }
        return headers
    }
}
