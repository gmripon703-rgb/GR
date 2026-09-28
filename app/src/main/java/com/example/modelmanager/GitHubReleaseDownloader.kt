package com.example.modelmanager

import android.content.Context
import com.example.core.DeviceHardwareProfiler
import com.example.security.ChecksumVerifier
import com.example.security.PathValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

enum class GitHubDownloadStatus {
    IDLE,
    CHECKING_STORAGE,
    CONNECTING,
    DOWNLOADING,
    PAUSED,
    VERIFYING_SHA256,
    COMPLETED,
    ERROR
}

data class GitHubDownloadProgress(
    val status: GitHubDownloadStatus = GitHubDownloadStatus.IDLE,
    val url: String = "",
    val targetFileName: String = "",
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0,
    val speedBytesPerSec: Long = 0,
    val percent: Int = 0,
    val errorMessage: String? = null,
    val localFilePath: String? = null,
    val verifiedSha256: String? = null,
    val isSha256Verified: Boolean = false
) {
    val speedText: String
        get() {
            if (speedBytesPerSec <= 0) return "-- KB/s"
            val mbPerSec = speedBytesPerSec / (1024.0 * 1024.0)
            return if (mbPerSec >= 1.0) "%.2f MB/s".format(mbPerSec) else "${speedBytesPerSec / 1024} KB/s"
        }

    val downloadedText: String
        get() {
            val dlMb = bytesDownloaded / (1024.0 * 1024.0)
            val totalMb = totalBytes / (1024.0 * 1024.0)
            return if (totalMb >= 1024.0) {
                "%.2f GB / %.2f GB".format(dlMb / 1024.0, totalMb / 1024.0)
            } else if (totalBytes > 0) {
                "%.1f MB / %.1f MB".format(dlMb, totalMb)
            } else {
                "%.1f MB downloaded".format(dlMb)
            }
        }

    val etaText: String
        get() {
            if (speedBytesPerSec <= 0 || totalBytes <= bytesDownloaded) return "--"
            val remainingSec = (totalBytes - bytesDownloaded) / speedBytesPerSec
            return if (remainingSec < 60) {
                "${remainingSec}s"
            } else {
                "${remainingSec / 60}m ${remainingSec % 60}s"
            }
        }
}

/**
 * Downloads large files (~1.5 GB) from public GitHub Releases or HTTPS endpoints
 * directly into app-accessible storage with pause/resume, SHA-256 verification,
 * storage safety checks, and zero RAM bloat.
 */
class GitHubReleaseDownloader(
    private val context: Context,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {

    private val targetDirectory: File = File(context.filesDir, "models").apply {
        if (!exists()) mkdirs()
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private var downloadJob: Job? = null

    private val _progress = MutableStateFlow(GitHubDownloadProgress())
    val progress: StateFlow<GitHubDownloadProgress> = _progress.asStateFlow()

    init {
        checkExistingCompletedFile()
    }

    private fun checkExistingCompletedFile() {
        val lastFilename = _progress.value.targetFileName.ifBlank { "custom-team-model.gguf" }
        val target = File(targetDirectory, lastFilename)
        if (target.exists() && target.length() > 0) {
            _progress.value = GitHubDownloadProgress(
                status = GitHubDownloadStatus.COMPLETED,
                targetFileName = target.name,
                bytesDownloaded = target.length(),
                totalBytes = target.length(),
                percent = 100,
                localFilePath = target.absolutePath,
                isSha256Verified = true
            )
        }
    }

    fun startDownload(
        url: String,
        expectedSha256: String = "",
        minStorageBytes: Long = GitHubReleaseConfig.DEFAULT_MIN_STORAGE_BYTES,
        targetFileName: String? = null
    ) {
        val cleanUrl = GitHubReleaseConfig.convertGoogleDriveUrl(url.trim())
        if (cleanUrl.isBlank()) {
            _progress.value = _progress.value.copy(
                status = GitHubDownloadStatus.ERROR,
                errorMessage = "Invalid download URL. Please specify a valid HTTPS link."
            )
            return
        }

        val resolvedFileName = targetFileName?.ifBlank { null }
            ?: GitHubReleaseConfig.extractFilename(cleanUrl, "custom-team-model.gguf")

        downloadJob?.cancel()
        downloadJob = scope.launch {
            executeDownload(cleanUrl, expectedSha256.trim().lowercase(), minStorageBytes, resolvedFileName)
        }
    }

    private suspend fun executeDownload(
        url: String,
        expectedSha256: String,
        minStorageBytes: Long,
        resolvedFileName: String
    ) = withContext(Dispatchers.IO) {
        // Step 1: Storage pre-check
        _progress.value = GitHubDownloadProgress(
            status = GitHubDownloadStatus.CHECKING_STORAGE,
            url = url,
            targetFileName = resolvedFileName
        )

        val hardwareProfile = DeviceHardwareProfiler.profile(context)
        val availableBytes = hardwareProfile.availableStorageBytes

        if (availableBytes < minStorageBytes) {
            val neededGb = "%.2f".format(minStorageBytes / (1024.0 * 1024.0 * 1024.0))
            val availGb = "%.2f".format(availableBytes / (1024.0 * 1024.0 * 1024.0))
            _progress.value = _progress.value.copy(
                status = GitHubDownloadStatus.ERROR,
                errorMessage = "Insufficient storage! Minimum required: ${neededGb} GB, Available: ${availGb} GB. Please free up space on device."
            )
            return@withContext
        }

        val targetFile = File(targetDirectory, resolvedFileName)
        val partFile = File(targetDirectory, "$resolvedFileName.part")

        // Verify sandbox safety
        if (!PathValidator.isPathWithinSandbox(targetFile, targetDirectory)) {
            _progress.value = _progress.value.copy(
                status = GitHubDownloadStatus.ERROR,
                errorMessage = "Security violation: Destination path escaped app sandbox."
            )
            return@withContext
        }

        var existingBytes = if (partFile.exists()) partFile.length() else 0L

        // Step 2: Connect
        _progress.value = _progress.value.copy(
            status = GitHubDownloadStatus.CONNECTING,
            bytesDownloaded = existingBytes,
            totalBytes = 0,
            percent = 0
        )

        try {
            val requestBuilder = Request.Builder().url(url)
            if (existingBytes > 0) {
                requestBuilder.addHeader("Range", "bytes=$existingBytes-")
            }

            val response = okHttpClient.newCall(requestBuilder.build()).execute()

            if (response.code == 404) {
                response.close()
                _progress.value = _progress.value.copy(
                    status = GitHubDownloadStatus.ERROR,
                    errorMessage = "GitHub Release asset not found (HTTP 404). Please verify that the repository is public and the release tag/filename are exact."
                )
                return@withContext
            }

            if (response.code == 403) {
                response.close()
                _progress.value = _progress.value.copy(
                    status = GitHubDownloadStatus.ERROR,
                    errorMessage = "Access Forbidden (HTTP 403). Ensure this GitHub Release is public. Private releases require authentication which is not bundled."
                )
                return@withContext
            }

            if (!response.isSuccessful && response.code != 206) {
                response.close()
                // Retry without range header
                existingBytes = 0L
                partFile.delete()
                val retryResponse = okHttpClient.newCall(Request.Builder().url(url).build()).execute()
                if (!retryResponse.isSuccessful) {
                    val code = retryResponse.code
                    val msg = retryResponse.message
                    retryResponse.close()
                    _progress.value = _progress.value.copy(
                        status = GitHubDownloadStatus.ERROR,
                        errorMessage = "Download server returned HTTP $code: $msg"
                    )
                    return@withContext
                }
                streamResponseBody(retryResponse, partFile, targetFile, 0L, expectedSha256)
            } else {
                streamResponseBody(response, partFile, targetFile, existingBytes, expectedSha256)
            }

        } catch (ce: CancellationException) {
            _progress.value = _progress.value.copy(
                status = GitHubDownloadStatus.PAUSED,
                bytesDownloaded = if (partFile.exists()) partFile.length() else 0L,
                speedBytesPerSec = 0
            )
        } catch (e: Exception) {
            _progress.value = _progress.value.copy(
                status = GitHubDownloadStatus.ERROR,
                errorMessage = "Network error: ${e.localizedMessage ?: "Connection interrupted"}. You can tap Resume to retry.",
                speedBytesPerSec = 0
            )
        }
    }

    private suspend fun streamResponseBody(
        response: okhttp3.Response,
        partFile: File,
        targetFile: File,
        startBytes: Long,
        expectedSha256: String
    ) = withContext(Dispatchers.IO) {
        val body = response.body ?: throw IllegalStateException("Empty HTTP response body received from server.")
        val contentLength = body.contentLength()
        val totalBytes = if (contentLength > 0) startBytes + contentLength else 0L

        val raf = RandomAccessFile(partFile, "rw")
        raf.seek(startBytes)

        val inputStream = body.byteStream()
        val buffer = ByteArray(64 * 1024) // 64 KB buffer (memory safe)
        var bytesRead: Int
        var currentBytes = startBytes

        var lastSpeedTime = System.currentTimeMillis()
        var bytesSinceLastSpeed = 0L
        var currentSpeed = 0L

        _progress.value = _progress.value.copy(
            status = GitHubDownloadStatus.DOWNLOADING,
            bytesDownloaded = currentBytes,
            totalBytes = totalBytes,
            percent = if (totalBytes > 0) ((currentBytes * 100) / totalBytes).toInt().coerceIn(0, 99) else 0
        )

        try {
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                raf.write(buffer, 0, bytesRead)
                currentBytes += bytesRead
                bytesSinceLastSpeed += bytesRead

                val now = System.currentTimeMillis()
                val elapsed = now - lastSpeedTime
                if (elapsed >= 800) {
                    currentSpeed = (bytesSinceLastSpeed * 1000) / elapsed
                    bytesSinceLastSpeed = 0L
                    lastSpeedTime = now

                    val percent = if (totalBytes > 0) {
                        ((currentBytes * 100) / totalBytes).toInt().coerceIn(0, 99)
                    } else {
                        0
                    }

                    _progress.value = _progress.value.copy(
                        status = GitHubDownloadStatus.DOWNLOADING,
                        bytesDownloaded = currentBytes,
                        totalBytes = totalBytes,
                        speedBytesPerSec = currentSpeed,
                        percent = percent
                    )
                }
            }
        } finally {
            raf.close()
            inputStream.close()
            response.close()
        }

        // Step 3: SHA-256 Checksum Verification
        _progress.value = _progress.value.copy(
            status = GitHubDownloadStatus.VERIFYING_SHA256,
            bytesDownloaded = currentBytes,
            totalBytes = currentBytes,
            percent = 99,
            speedBytesPerSec = 0
        )

        val computedHash = calculateSha256(partFile)

        if (expectedSha256.isNotBlank()) {
            val matches = computedHash.equals(expectedSha256, ignoreCase = true)
            if (!matches) {
                partFile.delete()
                _progress.value = _progress.value.copy(
                    status = GitHubDownloadStatus.ERROR,
                    errorMessage = "SHA-256 Checksum Mismatch!\nExpected: $expectedSha256\nComputed: $computedHash\nThe file was untrusted and purged for security.",
                    verifiedSha256 = computedHash,
                    isSha256Verified = false
                )
                return@withContext
            }
        }

        // Atomic swap to final file
        if (targetFile.exists()) targetFile.delete()
        val successRename = partFile.renameTo(targetFile)
        if (!successRename) {
            partFile.copyTo(targetFile, overwrite = true)
            partFile.delete()
        }

        _progress.value = _progress.value.copy(
            status = GitHubDownloadStatus.COMPLETED,
            bytesDownloaded = targetFile.length(),
            totalBytes = targetFile.length(),
            percent = 100,
            localFilePath = targetFile.absolutePath,
            verifiedSha256 = computedHash,
            isSha256Verified = true,
            errorMessage = null
        )
    }

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buf = ByteArray(128 * 1024)
            var n: Int
            while (fis.read(buf).also { n = it } != -1) {
                digest.update(buf, 0, n)
            }
        }
        val bytes = digest.digest()
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun pauseDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _progress.value = _progress.value.copy(
            status = GitHubDownloadStatus.PAUSED,
            speedBytesPerSec = 0
        )
    }

    fun resumeDownload(
        url: String,
        expectedSha256: String = "",
        minStorageBytes: Long = GitHubReleaseConfig.DEFAULT_MIN_STORAGE_BYTES,
        targetFileName: String? = null
    ) {
        startDownload(url, expectedSha256, minStorageBytes, targetFileName)
    }

    fun cancelAndDelete(targetFileName: String) {
        pauseDownload()
        val targetFile = File(targetDirectory, targetFileName)
        val partFile = File(targetDirectory, "$targetFileName.part")
        if (targetFile.exists()) targetFile.delete()
        if (partFile.exists()) partFile.delete()

        _progress.value = GitHubDownloadProgress(
            status = GitHubDownloadStatus.IDLE,
            targetFileName = targetFileName,
            bytesDownloaded = 0,
            totalBytes = 0,
            percent = 0
        )
    }

    fun getDownloadedFile(targetFileName: String): File? {
        val file = File(targetDirectory, targetFileName)
        return if (file.exists() && file.length() > 0) file else null
    }
}
