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
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class ModelDownloadManager(
    private val context: Context,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) {

    private val modelsDirectory: File = File(context.filesDir, "models").apply {
        if (!exists()) mkdirs()
    }

    private val downloadJobs = ConcurrentHashMap<String, Job>()
    private val _downloadProgressMap = MutableStateFlow<Map<String, ModelDownloadProgress>>(emptyMap())
    val downloadProgressMap: StateFlow<Map<String, ModelDownloadProgress>> = _downloadProgressMap.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Scan already installed model files in modelsDirectory
        refreshInstalledModels()
    }

    fun getModelsDirectory(): File = modelsDirectory

    fun getModelFile(modelId: String): File {
        val safeName = "${modelId.replace(Regex("[^a-zA-Z0-9._-]"), "_")}.gguf"
        return File(modelsDirectory, safeName)
    }

    fun isModelInstalled(modelId: String): Boolean {
        val file = getModelFile(modelId)
        return file.exists() && file.length() > 0 &&
                _downloadProgressMap.value[modelId]?.state == DownloadState.INSTALLED
    }

    fun refreshInstalledModels() {
        val currentMap = _downloadProgressMap.value.toMutableMap()
        for (model in DefaultModelCatalog.curatedModels) {
            val file = getModelFile(model.id)
            if (file.exists() && file.length() > 0) {
                currentMap[model.id] = ModelDownloadProgress(
                    modelId = model.id,
                    state = DownloadState.INSTALLED,
                    bytesDownloaded = file.length(),
                    totalBytes = if (model.sizeBytes > 0) model.sizeBytes else file.length(),
                    percent = 100,
                    localFilePath = file.absolutePath
                )
            } else if (!currentMap.containsKey(model.id) || currentMap[model.id]?.state == DownloadState.INSTALLED) {
                currentMap[model.id] = ModelDownloadProgress(
                    modelId = model.id,
                    state = DownloadState.NOT_DOWNLOADED,
                    bytesDownloaded = 0,
                    totalBytes = model.sizeBytes,
                    percent = 0
                )
            }
        }
        _downloadProgressMap.value = currentMap
    }

    suspend fun startOrResumeDownload(model: ModelInfo): Result<Unit> = withContext(Dispatchers.IO) {
        // Storage check
        val hardwareProfile = DeviceHardwareProfiler.profile(context)
        if (!DeviceHardwareProfiler.hasSufficientStorage(hardwareProfile, model.minimumStorageBytes)) {
            val neededMb = model.minimumStorageBytes / (1024 * 1024)
            val availableMb = hardwareProfile.availableStorageBytes / (1024 * 1024)
            val errorMsg = "Insufficient storage! Needed: ${neededMb}MB, Available: ${availableMb}MB."
            updateProgress(
                model.id,
                ModelDownloadProgress(
                    modelId = model.id,
                    state = DownloadState.ERROR,
                    errorMessage = errorMsg
                )
            )
            return@withContext Result.failure(IllegalStateException(errorMsg))
        }

        val targetFile = getModelFile(model.id)
        val partFile = File(modelsDirectory, "${targetFile.name}.part")

        // Check path traversal
        if (!PathValidator.isPathWithinSandbox(targetFile, modelsDirectory)) {
            return@withContext Result.failure(SecurityException("Invalid model destination path"))
        }

        // Cancel any existing active job
        downloadJobs[model.id]?.cancel()

        val job = scope.launch {
            var existingBytes = if (partFile.exists()) partFile.length() else 0L

            updateProgress(
                model.id,
                ModelDownloadProgress(
                    modelId = model.id,
                    state = DownloadState.DOWNLOADING,
                    bytesDownloaded = existingBytes,
                    totalBytes = model.sizeBytes,
                    percent = if (model.sizeBytes > 0) ((existingBytes * 100) / model.sizeBytes).toInt() else 0
                )
            )

            try {
                val requestBuilder = Request.Builder().url(model.downloadUrl)
                if (existingBytes > 0) {
                    requestBuilder.addHeader("Range", "bytes=$existingBytes-")
                }

                val response = okHttpClient.newCall(requestBuilder.build()).execute()

                if (!response.isSuccessful && response.code != 206) {
                    // Range request may not be supported or completed, reset from 0
                    existingBytes = 0L
                    partFile.delete()
                    val retryCall = okHttpClient.newCall(Request.Builder().url(model.downloadUrl).build()).execute()
                    if (!retryCall.isSuccessful) {
                        throw IllegalStateException("HTTP ${retryCall.code}: ${retryCall.message}")
                    }
                    processResponseBody(model, retryCall, partFile, targetFile, 0L)
                } else {
                    processResponseBody(model, response, partFile, targetFile, existingBytes)
                }
            } catch (ce: CancellationException) {
                // Paused / Cancelled
                updateProgress(
                    model.id,
                    ModelDownloadProgress(
                        modelId = model.id,
                        state = DownloadState.PAUSED,
                        bytesDownloaded = if (partFile.exists()) partFile.length() else 0L,
                        totalBytes = model.sizeBytes,
                        percent = if (model.sizeBytes > 0 && partFile.exists()) ((partFile.length() * 100) / model.sizeBytes).toInt() else 0
                    )
                )
            } catch (e: Exception) {
                updateProgress(
                    model.id,
                    ModelDownloadProgress(
                        modelId = model.id,
                        state = DownloadState.ERROR,
                        errorMessage = e.localizedMessage ?: "Download failed"
                    )
                )
            } finally {
                downloadJobs.remove(model.id)
            }
        }

        downloadJobs[model.id] = job
        Result.success(Unit)
    }

    private suspend fun processResponseBody(
        model: ModelInfo,
        response: okhttp3.Response,
        partFile: File,
        targetFile: File,
        startBytes: Long
    ) = withContext(Dispatchers.IO) {
        val body = response.body ?: throw IllegalStateException("Empty HTTP response body")
        val contentLength = body.contentLength()
        val totalBytes = if (model.sizeBytes > 0) model.sizeBytes else (startBytes + contentLength)

        val raf = RandomAccessFile(partFile, "rw")
        raf.seek(startBytes)

        val inputStream = body.byteStream()
        val buffer = ByteArray(64 * 1024)
        var bytesRead: Int
        var currentBytes = startBytes

        var lastSpeedCalcTime = System.currentTimeMillis()
        var bytesReadSinceLastCalc = 0L
        var currentSpeed = 0L

        try {
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                raf.write(buffer, 0, bytesRead)
                currentBytes += bytesRead
                bytesReadSinceLastCalc += bytesRead

                val now = System.currentTimeMillis()
                val elapsed = now - lastSpeedCalcTime
                if (elapsed >= 1000) {
                    currentSpeed = (bytesReadSinceLastCalc * 1000) / elapsed
                    bytesReadSinceLastCalc = 0L
                    lastSpeedCalcTime = now

                    val percent = if (totalBytes > 0) ((currentBytes * 100) / totalBytes).toInt().coerceIn(0, 99) else 0
                    updateProgress(
                        model.id,
                        ModelDownloadProgress(
                            modelId = model.id,
                            state = DownloadState.DOWNLOADING,
                            bytesDownloaded = currentBytes,
                            totalBytes = totalBytes,
                            speedBytesPerSec = currentSpeed,
                            percent = percent
                        )
                    )
                }
            }
        } finally {
            raf.close()
            inputStream.close()
            response.close()
        }

        // Verify SHA-256
        updateProgress(
            model.id,
            ModelDownloadProgress(
                modelId = model.id,
                state = DownloadState.VERIFYING,
                bytesDownloaded = currentBytes,
                totalBytes = totalBytes,
                percent = 99
            )
        )

        val isValid = if (model.sha256.isNotBlank()) {
            ChecksumVerifier.verifySha256(partFile, model.sha256)
        } else {
            true // No checksum declared
        }

        if (!isValid) {
            // Checksum mismatch! Delete corrupted file immediately.
            partFile.delete()
            val errorMsg = "SHA-256 verification failed! Corrupted model file was securely removed."
            updateProgress(
                model.id,
                ModelDownloadProgress(
                    modelId = model.id,
                    state = DownloadState.ERROR,
                    errorMessage = errorMsg
                )
            )
            throw SecurityException(errorMsg)
        }

        // Rename partFile to targetFile
        if (targetFile.exists()) targetFile.delete()
        val renamed = partFile.renameTo(targetFile)
        if (!renamed) {
            partFile.copyTo(targetFile, overwrite = true)
            partFile.delete()
        }

        updateProgress(
            model.id,
            ModelDownloadProgress(
                modelId = model.id,
                state = DownloadState.INSTALLED,
                bytesDownloaded = targetFile.length(),
                totalBytes = targetFile.length(),
                percent = 100,
                localFilePath = targetFile.absolutePath
            )
        )
    }

    fun pauseDownload(modelId: String) {
        downloadJobs[modelId]?.cancel()
        downloadJobs.remove(modelId)
    }

    fun deleteModel(modelId: String) {
        pauseDownload(modelId)
        val targetFile = getModelFile(modelId)
        val partFile = File(modelsDirectory, "${targetFile.name}.part")
        if (targetFile.exists()) targetFile.delete()
        if (partFile.exists()) partFile.delete()

        updateProgress(
            modelId,
            ModelDownloadProgress(
                modelId = modelId,
                state = DownloadState.NOT_DOWNLOADED,
                bytesDownloaded = 0,
                totalBytes = 0,
                percent = 0
            )
        )
    }

    private fun updateProgress(modelId: String, progress: ModelDownloadProgress) {
        val current = _downloadProgressMap.value.toMutableMap()
        current[modelId] = progress
        _downloadProgressMap.value = current
    }
}
