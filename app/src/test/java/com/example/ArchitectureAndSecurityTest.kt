package com.example

import com.example.modelmanager.DefaultModelCatalog
import com.example.security.ChecksumVerifier
import com.example.security.DangerLevel
import com.example.security.PathValidator
import com.example.security.SafeCommandGuard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ArchitectureAndSecurityTest {

    @Test
    fun testSafeCommandGuardCatchesDestructiveCommands() {
        val eval1 = SafeCommandGuard.evaluate("rm -rf /var/log/*")
        assertTrue(eval1.isDangerous)
        assertEquals(DangerLevel.DESTRUCTIVE, eval1.dangerLevel)
        assertTrue(eval1.requiresExplicitConfirmation)

        val eval2 = SafeCommandGuard.evaluate("sudo iptables -F")
        assertTrue(eval2.isDangerous)
        assertEquals(DangerLevel.HIGH_RISK, eval2.dangerLevel)

        val eval3 = SafeCommandGuard.evaluate("git status")
        assertFalse(eval3.isDangerous)
        assertEquals(DangerLevel.SAFE, eval3.dangerLevel)
    }

    @Test
    fun testPathValidatorPreventsTraversal() {
        assertFalse("Traversal with .. must be rejected", PathValidator.validateSafeModelFileName("../secret.key"))
        assertFalse("Slash in filename must be rejected", PathValidator.validateSafeModelFileName("sub/model.gguf"))
        assertTrue("Clean GGUF file must be accepted", PathValidator.validateSafeModelFileName("qwen2.5-coder-0.5b.gguf"))
    }

    @Test
    fun testModelCatalogHasValidFreeModels() {
        val models = DefaultModelCatalog.curatedModels
        assertTrue("Curated models should not be empty", models.isNotEmpty())
        for (m in models) {
            assertTrue("Model ID must be defined", m.id.isNotBlank())
            assertTrue("Model downloadUrl must point to public repository", m.downloadUrl.startsWith("https://"))
            assertTrue("Model license must be non-empty", m.license.isNotBlank())
            assertTrue("Model format must be GGUF or LiteRT", m.format == "GGUF" || m.format == "LiteRT")
            assertTrue("Model size must be positive", m.sizeBytes > 0)
        }
    }

    @Test
    fun testChecksumVerifier() {
        val tempFile = File.createTempFile("test_model", ".bin")
        try {
            tempFile.writeText("hello world model weights")
            val sha256 = ChecksumVerifier.calculateSha256(tempFile)
            assertNotNull(sha256)
            assertEquals(64, sha256.length)
            assertTrue(ChecksumVerifier.verifySha256(tempFile, sha256))
            assertFalse(ChecksumVerifier.verifySha256(tempFile, "badhash123"))
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun testGitHubReleaseConfigValidation() {
        val validReleaseUrl = "https://github.com/myorg/ai-models/releases/download/v1.0.0/custom-team-model.gguf"
        assertTrue("GitHub release URL should match", com.example.modelmanager.GitHubReleaseConfig.isGitHubReleaseUrl(validReleaseUrl))

        val invalidUrl = "https://example.com/file.bin"
        assertFalse("Non-GitHub release URL should not match", com.example.modelmanager.GitHubReleaseConfig.isGitHubReleaseUrl(invalidUrl))

        val filename = com.example.modelmanager.GitHubReleaseConfig.extractFilename(validReleaseUrl)
        assertEquals("custom-team-model.gguf", filename)

        // Google Drive conversion test
        val driveShareUrl = "https://drive.google.com/file/d/1A2B3C4D5E6F/view?usp=sharing"
        val converted = com.example.modelmanager.GitHubReleaseConfig.convertGoogleDriveUrl(driveShareUrl)
        assertEquals("https://drive.google.com/uc?export=download&id=1A2B3C4D5E6F", converted)
    }
}
