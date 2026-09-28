package com.example

import com.example.ai.AIRequest
import com.example.ai.local.GgufFileValidator
import com.example.ai.local.LlamaCppRuntimeAdapter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GgufRuntimeTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testNonExistentFileFailsValidation() {
        val nonExistent = File(tempFolder.root, "does_not_exist.gguf")
        val result = GgufFileValidator.validate(nonExistent)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("does not exist") == true)
    }

    @Test
    fun testEmptyFileFailsValidation() {
        val emptyFile = tempFolder.newFile("empty.gguf")
        val result = GgufFileValidator.validate(emptyFile)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("too small") == true)
    }

    @Test
    fun testMalformedFileWithoutGgufMagicFailsValidation() {
        val malformedFile = tempFolder.newFile("random.bin")
        malformedFile.writeBytes("This is not a GGUF binary at all!".toByteArray())

        val result = GgufFileValidator.validate(malformedFile)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("magic header mismatch") == true)
    }

    @Test
    fun testValidGgufHeaderSucceeds() {
        val ggufFile = tempFolder.newFile("valid_header.gguf")

        // Construct 24 bytes of valid GGUF header
        // 0..3: 'G','G','U','F'
        // 4..7: uint32 version = 3
        // 8..15: uint64 tensorCount = 42L
        // 16..23: uint64 metadataCount = 10L
        val buffer = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put('G'.code.toByte())
        buffer.put('G'.code.toByte())
        buffer.put('U'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.putInt(3) // Version 3
        buffer.putLong(42L) // Tensor count
        buffer.putLong(10L) // Metadata count

        FileOutputStream(ggufFile).use { it.write(buffer.array()) }

        val result = GgufFileValidator.validate(ggufFile)
        assertTrue("Validation should succeed for valid header", result.isSuccess)
        val info = result.getOrNull()
        assertEquals(3, info?.version)
        assertEquals(42L, info?.tensorCount)
        assertEquals(10L, info?.metadataKvCount)
    }

    @Test
    fun testLlamaCppRuntimeAdapterLifecycleAndSafety() = runBlocking {
        val adapter = LlamaCppRuntimeAdapter()
        assertFalse(adapter.isModelLoaded)

        // Loading malformed file fails
        val badFile = tempFolder.newFile("bad.gguf")
        badFile.writeBytes(ByteArray(20)) // all zeros
        val loadBadResult = adapter.loadModel(badFile, 4)
        assertTrue(loadBadResult.isFailure)
        assertFalse(adapter.isModelLoaded)

        // Loading valid header file succeeds in validation
        val validFile = tempFolder.newFile("good.gguf")
        val buffer = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(byteArrayOf('G'.code.toByte(), 'G'.code.toByte(), 'U'.code.toByte(), 'F'.code.toByte()))
        buffer.putInt(2) // Version 2
        buffer.putLong(100L)
        buffer.putLong(5L)
        FileOutputStream(validFile).use { it.write(buffer.array()) }

        val loadResult = adapter.loadModel(validFile, 4)
        assertTrue(loadResult.isSuccess)
        assertTrue(adapter.isModelLoaded)
        assertEquals(validFile.absolutePath, adapter.currentModelPath)

        // If native library is not bundled, generate returns failure rather than fake canned output
        if (!adapter.isNativeInferenceAvailable) {
            val genResult = adapter.generate(AIRequest("test prompt"))
            assertTrue("Generate should fail when native library is missing", genResult.isFailure)
            assertTrue(genResult.exceptionOrNull() is UnsupportedOperationException)
        }

        // Unload resets state
        adapter.unloadModel()
        assertFalse(adapter.isModelLoaded)
        assertEquals(null, adapter.currentModelPath)
    }
}
