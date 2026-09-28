package com.example

import com.example.domain.analyzer.NetworkUtils
import com.example.domain.analyzer.OfflineVulnerabilityScanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityLogicTest {

    @Test
    fun testVulnerabilityScannerDetectsHardcodedSecretAndSqlInjection() {
        val sampleCode = """
            val apiKey = "sk_live_9948274a81bc920485adfa"
            val query = "SELECT * FROM users WHERE id = " + userId
        """.trimIndent()

        val findings = OfflineVulnerabilityScanner.scanCode(sampleCode)
        assertTrue("Expected findings to not be empty", findings.isNotEmpty())
        val cweIds = findings.map { it.cweId }
        assertTrue("Should detect CWE-798 Hardcoded secret", cweIds.contains("CWE-798"))
        assertTrue("Should detect CWE-89 SQL injection", cweIds.contains("CWE-89"))
    }

    @Test
    fun testSubnetCalculatorComputesValidNetwork() {
        val result = NetworkUtils.calculateSubnet("192.168.1.100/24").getOrNull()
        assertNotNull("Subnet calculation should succeed", result)
        assertEquals("192.168.1.0", result!!.networkAddress)
        assertEquals("192.168.1.255", result.broadcastAddress)
        assertEquals("192.168.1.1", result.firstUsableHost)
        assertEquals("192.168.1.254", result.lastUsableHost)
        assertEquals("255.255.255.0", result.netmask)
        assertEquals(254L, result.usableHosts)
    }

    @Test
    fun testCryptoHashAndEncoding() {
        val text = "hello"
        val sha256 = NetworkUtils.computeHash(text, "SHA-256")
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", sha256)

        val base64 = NetworkUtils.encodeBase64(text)
        val decoded = NetworkUtils.decodeBase64(base64).getOrNull()
        assertEquals(text, decoded)
    }
}
