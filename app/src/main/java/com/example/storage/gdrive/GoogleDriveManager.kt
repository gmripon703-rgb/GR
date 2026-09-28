package com.example.storage.gdrive

import android.content.Context
import android.util.Log
import com.example.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DriveFileItem(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String,
    val modifiedTime: String,
    val isAppResource: Boolean = true
)

data class DriveQuotaInfo(
    val limitBytes: Long,
    val usageBytes: Long,
    val usageInDriveBytes: Long,
    val userEmail: String,
    val displayName: String
) {
    val freeBytes: Long get() = if (limitBytes > usageBytes) limitBytes - usageBytes else 0L
    val usagePercent: Int get() = if (limitBytes > 0) ((usageBytes.toDouble() / limitBytes) * 100).toInt() else 0
    val formattedLimit: String get() = formatBytes(limitBytes)
    val formattedUsed: String get() = formatBytes(usageBytes)
    val formattedFree: String get() = formatBytes(freeBytes)

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 GB"
        val gb = bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
        return String.format(Locale.US, "%.2f GB", gb)
    }
}

sealed class DriveAuthState {
    object Disconnected : DriveAuthState()
    object Authenticating : DriveAuthState()
    data class Connected(val email: String, val displayName: String, val quota: DriveQuotaInfo) : DriveAuthState()
    data class Error(val message: String) : DriveAuthState()
}

/**
 * Manages Google Account authentication and Google Drive Cloud storage for GR AI.
 * Requires a verified Google OAuth access token to communicate with Google Drive API v3.
 * Never reports simulated metadata as a successful cloud backup.
 */
class GoogleDriveManager(
    private val context: Context,
    private val secureStorage: SecureStorage
) {
    companion object {
        private const val TAG = "GoogleDriveManager"
        const val OAUTH_CLIENT_ID = "489759037793-k53vkat7e5ujld1a9cjj3uj4iljruvlp.apps.googleusercontent.com"
        const val PROJECT_ID = "directed-strata-503219-e4"
        const val PROJECT_NUMBER = "489759037793"
        const val SCOPE_DRIVE_FILE = "https://www.googleapis.com/auth/drive.file"
        const val SCOPE_DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata"

        const val FILE_TEAM_RULES = "gr_ai_team_rules.json"
        const val FILE_RAG_ARCHIVE = "gr_ai_rag_archive.json"
        const val FILE_CHAT_HISTORY = "gr_ai_chat_history.json"
        const val FILE_DEVELOPER_PROMPTS = "gr_ai_developer_resources.json"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val _authState = MutableStateFlow<DriveAuthState>(DriveAuthState.Disconnected)
    val authState: StateFlow<DriveAuthState> = _authState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _accountEmail = MutableStateFlow(secureStorage.googleAccountEmail)
    val accountEmail: StateFlow<String> = _accountEmail.asStateFlow()

    private val _accountDisplayName = MutableStateFlow(secureStorage.googleAccountDisplayName)
    val accountDisplayName: StateFlow<String> = _accountDisplayName.asStateFlow()

    private val _driveQuota = MutableStateFlow<DriveQuotaInfo?>(null)
    val driveQuota: StateFlow<DriveQuotaInfo?> = _driveQuota.asStateFlow()

    private val _driveFiles = MutableStateFlow<List<DriveFileItem>>(emptyList())
    val driveFiles: StateFlow<List<DriveFileItem>> = _driveFiles.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    private val _preferCloudStorage = MutableStateFlow(secureStorage.preferCloudStorage)
    val preferCloudStorage: StateFlow<Boolean> = _preferCloudStorage.asStateFlow()

    fun setPreferCloudStorage(prefer: Boolean) {
        secureStorage.preferCloudStorage = prefer
        _preferCloudStorage.value = prefer
    }

    /**
     * Authenticates with Google Drive using a real OAuth access token.
     * Verifies token against the Google Drive API 'about' endpoint before reporting success.
     */
    suspend fun authenticateWithToken(token: String): Result<DriveQuotaInfo> = withContext(Dispatchers.IO) {
        val cleanToken = token.trim()
        if (cleanToken.isBlank()) {
            val err = "OAuth access token is required to authenticate with Google Drive"
            _authState.value = DriveAuthState.Error(err)
            _syncStatus.value = err
            return@withContext Result.failure(IllegalArgumentException(err))
        }

        _authState.value = DriveAuthState.Authenticating
        _isSyncing.value = true
        _syncStatus.value = "Verifying token with Google Drive API..."

        try {
            val request = Request.Builder()
                .url("https://www.googleapis.com/drive/v3/about?fields=user,storageQuota")
                .addHeader("Authorization", "Bearer $cleanToken")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = "Google Drive API authentication rejected (HTTP ${response.code}): $bodyString"
                Log.w(TAG, errorMsg)
                disconnectGoogleAccount()
                _authState.value = DriveAuthState.Error("Authentication failed (HTTP ${response.code})")
                _syncStatus.value = "Error: Invalid or expired OAuth token."
                return@withContext Result.failure(IllegalStateException(errorMsg))
            }

            val json = JSONObject(bodyString)
            val userObj = json.optJSONObject("user")
            val quotaObj = json.optJSONObject("storageQuota")

            val email = userObj?.optString("emailAddress").orEmpty().ifBlank { "Google User" }
            val name = userObj?.optString("displayName").orEmpty().ifBlank { email }

            val limit = quotaObj?.optLong("limit", 0L) ?: 0L
            val usage = quotaObj?.optLong("usage", 0L) ?: 0L
            val usageInDrive = quotaObj?.optLong("usageInDrive", 0L) ?: 0L

            val quota = DriveQuotaInfo(
                limitBytes = limit,
                usageBytes = usage,
                usageInDriveBytes = usageInDrive,
                userEmail = email,
                displayName = name
            )

            // Persist valid state
            secureStorage.googleDriveConnected = true
            secureStorage.googleDriveAccessToken = cleanToken
            secureStorage.googleAccountEmail = email
            secureStorage.googleAccountDisplayName = name

            _accountEmail.value = email
            _accountDisplayName.value = name
            _driveQuota.value = quota
            _isConnected.value = true
            _authState.value = DriveAuthState.Connected(email, name, quota)
            _syncStatus.value = "Connected to Google Account ($email) with Drive API access."

            // Refresh real files
            fetchDriveFilesInternal(cleanToken)

            Result.success(quota)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Drive authentication", e)
            _authState.value = DriveAuthState.Error(e.localizedMessage ?: "Network error during authentication")
            _syncStatus.value = "Connection error: ${e.localizedMessage}"
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    fun disconnectGoogleAccount() {
        secureStorage.googleDriveConnected = false
        secureStorage.googleAccountEmail = ""
        secureStorage.googleAccountDisplayName = ""
        secureStorage.googleDriveAccessToken = ""

        _isConnected.value = false
        _accountEmail.value = ""
        _accountDisplayName.value = ""
        _driveQuota.value = null
        _driveFiles.value = emptyList()
        _authState.value = DriveAuthState.Disconnected
        _syncStatus.value = "Google Account disconnected."
    }

    /**
     * Fetches real files list from Google Drive API
     */
    suspend fun refreshDriveFiles(): Result<List<DriveFileItem>> = withContext(Dispatchers.IO) {
        val token = secureStorage.googleDriveAccessToken
        if (!_isConnected.value || token.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Google Drive is not authenticated."))
        }
        fetchDriveFilesInternal(token)
    }

    private fun fetchDriveFilesInternal(token: String): Result<List<DriveFileItem>> {
        return try {
            val url = "https://www.googleapis.com/drive/v3/files?spaces=drive,appDataFolder&fields=files(id,name,size,mimeType,modifiedTime,trashed)&q=trashed=false&pageSize=50"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return Result.failure(Exception("Failed to list files (${response.code}): $body"))
            }

            val json = JSONObject(body)
            val filesArr = json.optJSONArray("files") ?: org.json.JSONArray()
            val items = mutableListOf<DriveFileItem>()

            for (i in 0 until filesArr.length()) {
                val f = filesArr.getJSONObject(i)
                val id = f.optString("id", "")
                val name = f.optString("name", "unnamed")
                val size = f.optLong("size", 0L)
                val mime = f.optString("mimeType", "application/octet-stream")
                val mod = f.optString("modifiedTime", "").take(16).replace("T", " ")

                items.add(
                    DriveFileItem(
                        id = id,
                        name = name,
                        sizeBytes = size,
                        mimeType = mime,
                        modifiedTime = mod,
                        isAppResource = name.startsWith("gr_ai_") || name.endsWith(".json") || name.endsWith(".gguf")
                    )
                )
            }

            _driveFiles.value = items
            Result.success(items)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching drive files", e)
            Result.failure(e)
        }
    }

    /**
     * Backup Team Rules to Google Drive
     */
    suspend fun syncTeamRulesToDrive(rulesJson: String): Result<String> = withContext(Dispatchers.IO) {
        val token = secureStorage.googleDriveAccessToken
        if (!_isConnected.value || token.isBlank()) {
            val err = "Google Drive is not authenticated. Please provide a valid Google OAuth access token."
            _syncStatus.value = err
            return@withContext Result.failure(IllegalStateException(err))
        }

        _isSyncing.value = true
        _syncStatus.value = "Uploading Team Rules to Google Drive..."

        try {
            val fileId = uploadToDriveApi(FILE_TEAM_RULES, "application/json", rulesJson.toByteArray(Charsets.UTF_8), token)
            secureStorage.googleDriveLastSync = System.currentTimeMillis()
            _syncStatus.value = "Success: Team Rules uploaded to Google Drive (ID: $fileId)."
            fetchDriveFilesInternal(token)
            Result.success(fileId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload team rules to Drive", e)
            _syncStatus.value = "Upload failed: ${e.localizedMessage}"
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * Backup RAG Documents to Google Drive
     */
    suspend fun syncRagArchiveToDrive(ragArchiveJson: String): Result<String> = withContext(Dispatchers.IO) {
        val token = secureStorage.googleDriveAccessToken
        if (!_isConnected.value || token.isBlank()) {
            val err = "Google Drive is not authenticated. Please provide a valid Google OAuth access token."
            _syncStatus.value = err
            return@withContext Result.failure(IllegalStateException(err))
        }

        _isSyncing.value = true
        _syncStatus.value = "Uploading RAG Documents to Google Drive..."

        try {
            val fileId = uploadToDriveApi(FILE_RAG_ARCHIVE, "application/json", ragArchiveJson.toByteArray(Charsets.UTF_8), token)
            secureStorage.googleDriveLastSync = System.currentTimeMillis()
            _syncStatus.value = "Success: RAG Documents uploaded to Google Drive (ID: $fileId)."
            fetchDriveFilesInternal(token)
            Result.success(fileId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload RAG to Drive", e)
            _syncStatus.value = "Upload failed: ${e.localizedMessage}"
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * Backup Chat History to Google Drive
     */
    suspend fun syncChatHistoryToDrive(chatHistoryJson: String): Result<String> = withContext(Dispatchers.IO) {
        val token = secureStorage.googleDriveAccessToken
        if (!_isConnected.value || token.isBlank()) {
            val err = "Google Drive is not authenticated. Please provide a valid Google OAuth access token."
            _syncStatus.value = err
            return@withContext Result.failure(IllegalStateException(err))
        }

        _isSyncing.value = true
        _syncStatus.value = "Uploading Chat Logs to Google Drive..."

        try {
            val fileId = uploadToDriveApi(FILE_CHAT_HISTORY, "application/json", chatHistoryJson.toByteArray(Charsets.UTF_8), token)
            secureStorage.googleDriveLastSync = System.currentTimeMillis()
            _syncStatus.value = "Success: Chat history uploaded to Google Drive (ID: $fileId)."
            fetchDriveFilesInternal(token)
            Result.success(fileId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload chat history to Drive", e)
            _syncStatus.value = "Upload failed: ${e.localizedMessage}"
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * Direct upload via Google Drive v3 REST API multipart endpoint
     */
    private fun uploadToDriveApi(fileName: String, mimeType: String, data: ByteArray, token: String): String {
        val boundary = "==GR_AI_MULTIPART_BOUNDARY_${System.currentTimeMillis()}=="
        val metadataJson = JSONObject().apply {
            put("name", fileName)
            put("description", "Uploaded by GR AI - Developer & Security Mobile Suite")
        }.toString()

        val delimiter = "\r\n--$boundary\r\n"
        val closeDelimiter = "\r\n--$boundary--"

        val bodyBuilder = StringBuilder()
        bodyBuilder.append(delimiter)
        bodyBuilder.append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
        bodyBuilder.append(metadataJson)
        bodyBuilder.append(delimiter)
        bodyBuilder.append("Content-Type: ").append(mimeType).append("\r\n\r\n")

        val headerBytes = bodyBuilder.toString().toByteArray(Charsets.UTF_8)
        val footerBytes = closeDelimiter.toByteArray(Charsets.UTF_8)
        val payload = ByteArray(headerBytes.size + data.size + footerBytes.size)

        System.arraycopy(headerBytes, 0, payload, 0, headerBytes.size)
        System.arraycopy(data, 0, payload, headerBytes.size, data.size)
        System.arraycopy(footerBytes, 0, payload, headerBytes.size + data.size, footerBytes.size)

        val requestBody = payload.toRequestBody("multipart/related; boundary=$boundary".toMediaType())
        val request = Request.Builder()
            .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""
        if (!response.isSuccessful) {
            throw Exception("Google Drive API error (${response.code}): $responseBody")
        }

        val json = JSONObject(responseBody)
        return json.optString("id", fileName)
    }

    /**
     * Download or stream asset from Google Drive
     */
    suspend fun streamFromDrive(fileId: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        val token = secureStorage.googleDriveAccessToken
        val directUrl = if (fileId.startsWith("http")) {
            fileId
        } else {
            "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
        }

        try {
            val reqBuilder = Request.Builder().url(directUrl)
            if (token.isNotBlank() && !fileId.startsWith("http")) {
                reqBuilder.addHeader("Authorization", "Bearer $token")
            }

            val response = httpClient.newCall(reqBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to stream from Drive: HTTP ${response.code}"))
            }

            val bytes = response.body?.bytes() ?: ByteArray(0)
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
