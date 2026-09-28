package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AIChunk
import com.example.ai.AIProviderRouter
import com.example.ai.AIRequest
import com.example.ai.TaskType
import com.example.ai.cloud.CloudAIProvider
import com.example.ai.local.LocalAIProvider
import com.example.core.DeviceHardwareProfile
import com.example.core.DeviceHardwareProfiler
import com.example.core.NetworkMonitor
import com.example.developer.DeveloperAction
import com.example.developer.DeveloperToolsEngine
import com.example.modelmanager.DefaultModelCatalog
import com.example.modelmanager.ModelDownloadManager
import com.example.modelmanager.ModelDownloadProgress
import com.example.modelmanager.ModelInfo
import com.example.rag.LocalRagEngine
import com.example.security.SecureStorage
import com.example.storage.AppDatabase
import com.example.storage.entity.ChatMessageEntity
import com.example.storage.entity.RagDocumentEntity
import com.example.storage.entity.TeamRuleEntity
import com.example.team.TeamRuleEngine
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val secureStorage = SecureStorage(application)
    val networkMonitor = NetworkMonitor(application)
    val modelDownloadManager = ModelDownloadManager(application)
    val voiceAssistant = VoiceAssistantManager(application)
    val gitHubReleaseDownloader = com.example.modelmanager.GitHubReleaseDownloader(application)
    val gitHubDownloadProgress: StateFlow<com.example.modelmanager.GitHubDownloadProgress> =
        gitHubReleaseDownloader.progress

    val localAiProvider = LocalAIProvider(modelDownloadManager)
    val cloudAiProvider = CloudAIProvider(secureStorage)
    val router = AIProviderRouter(localAiProvider, cloudAiProvider, networkMonitor, secureStorage)

    val ragEngine = LocalRagEngine(db.ragDao())
    val teamRuleEngine = TeamRuleEngine(db.teamRuleDao())
    val googleDriveManager = com.example.storage.gdrive.GoogleDriveManager(application, secureStorage)

    val isDriveConnected: StateFlow<Boolean> = googleDriveManager.isConnected
    val driveAccountEmail: StateFlow<String> = googleDriveManager.accountEmail
    val driveAccountDisplayName: StateFlow<String> = googleDriveManager.accountDisplayName
    val driveQuota: StateFlow<com.example.storage.gdrive.DriveQuotaInfo?> = googleDriveManager.driveQuota
    val driveFiles: StateFlow<List<com.example.storage.gdrive.DriveFileItem>> = googleDriveManager.driveFiles
    val isDriveSyncing: StateFlow<Boolean> = googleDriveManager.isSyncing
    val driveSyncStatus: StateFlow<String?> = googleDriveManager.syncStatus
    val preferCloudStorage: StateFlow<Boolean> = googleDriveManager.preferCloudStorage

    private val _gitHubReleaseUrl = MutableStateFlow(secureStorage.gitHubReleaseUrl)
    val gitHubReleaseUrl: StateFlow<String> = _gitHubReleaseUrl.asStateFlow()

    private val _gitHubExpectedSha256 = MutableStateFlow(secureStorage.gitHubReleaseExpectedSha256)
    val gitHubExpectedSha256: StateFlow<String> = _gitHubExpectedSha256.asStateFlow()

    private val _gitHubMinStorageGb = MutableStateFlow(secureStorage.gitHubReleaseMinStorageGb)
    val gitHubMinStorageGb: StateFlow<Float> = _gitHubMinStorageGb.asStateFlow()

    private val _gitHubTargetFileName = MutableStateFlow(secureStorage.gitHubReleaseTargetFileName)
    val gitHubTargetFileName: StateFlow<String> = _gitHubTargetFileName.asStateFlow()

    private val _ruleSyncStatus = MutableStateFlow<String?>(null)
    val ruleSyncStatus: StateFlow<String?> = _ruleSyncStatus.asStateFlow()

    // UI State flows
    val chatMessages: StateFlow<List<ChatMessageEntity>> = db.chatDao().getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ragDocuments: StateFlow<List<RagDocumentEntity>> = ragEngine.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teamRules: StateFlow<List<TeamRuleEntity>> = teamRuleEngine.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadProgressMap: StateFlow<Map<String, ModelDownloadProgress>> =
        modelDownloadManager.downloadProgressMap

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnlineFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), networkMonitor.isOnline())

    private val _hardwareProfile = MutableStateFlow(DeviceHardwareProfiler.profile(application))
    val hardwareProfile: StateFlow<DeviceHardwareProfile> = _hardwareProfile.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _currentStreamingText = MutableStateFlow("")
    val currentStreamingText: StateFlow<String> = _currentStreamingText.asStateFlow()

    private val _providerMode = MutableStateFlow(secureStorage.providerMode)
    val providerMode: StateFlow<String> = _providerMode.asStateFlow()

    private val _allowCloudAi = MutableStateFlow(secureStorage.allowCloudAi)
    val allowCloudAi: StateFlow<Boolean> = _allowCloudAi.asStateFlow()

    private val _activeOnlineModel = MutableStateFlow(secureStorage.activeOnlineModel)
    val activeOnlineModel: StateFlow<String> = _activeOnlineModel.asStateFlow()

    private val _attachedVisualAsset = MutableStateFlow<com.example.vision.VisualAnalysisResult?>(null)
    val attachedVisualAsset: StateFlow<com.example.vision.VisualAnalysisResult?> = _attachedVisualAsset.asStateFlow()

    val isListening: StateFlow<Boolean> = voiceAssistant.isListening
    val recognizedVoiceText: StateFlow<String> = voiceAssistant.recognizedText
    val isSpeaking: StateFlow<Boolean> = voiceAssistant.isSpeaking

    private var activeGenerationJob: Job? = null

    init {
        viewModelScope.launch {
            teamRuleEngine.ensureDefaultRulesInitialized()
        }
    }

    fun startVoiceInput() {
        voiceAssistant.startListening()
    }

    fun stopVoiceInput() {
        voiceAssistant.stopListening()
    }

    fun speakText(text: String) {
        voiceAssistant.speak(text)
    }

    fun stopSpeaking() {
        voiceAssistant.stopSpeaking()
    }

    fun attachImageUri(uri: android.net.Uri) {
        viewModelScope.launch {
            val result = com.example.vision.VisionAnalyzer.processImageUri(getApplication(), uri)
            _attachedVisualAsset.value = result
        }
    }

    fun clearAttachedImage() {
        _attachedVisualAsset.value = null
    }

    fun getActiveProviderStatus(): String {
        return router.determineActiveProvider().second
    }

    fun setProviderMode(mode: String) {
        secureStorage.providerMode = mode
        _providerMode.value = mode
    }

    fun setAllowCloudAi(allow: Boolean) {
        secureStorage.allowCloudAi = allow
        _allowCloudAi.value = allow
    }

    fun saveCloudSettings(apiKey: String, baseUrl: String, onlineModel: String) {
        secureStorage.customApiKey = apiKey
        secureStorage.customBaseUrl = baseUrl
        secureStorage.activeOnlineModel = onlineModel
        _activeOnlineModel.value = onlineModel
    }

    fun sendMessage(
        promptText: String,
        taskType: TaskType = TaskType.TEXT,
        isCodeAudit: Boolean = false,
        attachedImageData: String? = null
    ) {
        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isBlank() || _isGenerating.value) return

        val imgData = attachedImageData ?: _attachedVisualAsset.value?.base64Data
        _attachedVisualAsset.value = null

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            _isGenerating.value = true
            _currentStreamingText.value = ""

            val (activeProvider, _) = router.determineActiveProvider()

            // Save user message to database
            val userMsg = ChatMessageEntity(
                role = "user",
                content = cleanPrompt,
                modelUsed = if (activeProvider.providerType == com.example.ai.ProviderType.LOCAL) "on-device" else secureStorage.activeOnlineModel,
                providerType = activeProvider.providerType.name,
                isCodeAudit = isCodeAudit
            )
            db.chatDao().insertMessage(userMsg)

            // Local RAG lookup (search local notes and project docs)
            val ragResults = ragEngine.searchRelevantChunks(cleanPrompt, topK = 2)
            val ragContext = ragEngine.buildRagContext(ragResults)

            // Team rules hierarchical prompt
            val hierarchicalPrompt = teamRuleEngine.buildHierarchicalSystemPrompt()

            val combinedSystemInstruction = buildString {
                appendLine(hierarchicalPrompt)
                if (ragContext.isNotBlank()) {
                    appendLine()
                    appendLine(ragContext)
                }
            }

            val request = AIRequest(
                prompt = cleanPrompt,
                systemInstruction = combinedSystemInstruction,
                taskType = taskType,
                stream = true,
                attachedImageData = imgData
            )

            val accumulatedResponse = StringBuilder()

            try {
                router.stream(request).collect { chunk ->
                    accumulatedResponse.append(chunk.deltaText)
                    _currentStreamingText.value = accumulatedResponse.toString()
                }

                val finalResponseText = accumulatedResponse.toString().trim()
                if (finalResponseText.isNotBlank()) {
                    val aiMsg = ChatMessageEntity(
                        role = "model",
                        content = finalResponseText,
                        modelUsed = if (activeProvider.providerType == com.example.ai.ProviderType.LOCAL) "on-device-engine" else secureStorage.activeOnlineModel,
                        providerType = activeProvider.providerType.name,
                        isCodeAudit = isCodeAudit
                    )
                    db.chatDao().insertMessage(aiMsg)
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessageEntity(
                    role = "model",
                    content = "⚠️ Generation stopped: ${e.localizedMessage}",
                    modelUsed = "error-handler",
                    providerType = "LOCAL"
                )
                db.chatDao().insertMessage(errorMsg)
            } finally {
                _isGenerating.value = false
                _currentStreamingText.value = ""
            }
        }
    }

    fun stopGeneration() {
        activeGenerationJob?.cancel()
        _isGenerating.value = false
        val current = _currentStreamingText.value.trim()
        if (current.isNotBlank()) {
            viewModelScope.launch {
                db.chatDao().insertMessage(
                    ChatMessageEntity(
                        role = "model",
                        content = "$current\n\n*(Generation stopped by user)*",
                        modelUsed = "stopped",
                        providerType = "LOCAL"
                    )
                )
                _currentStreamingText.value = ""
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            db.chatDao().clearHistory()
        }
    }

    // Model manager controls
    fun startModelDownload(model: ModelInfo) {
        viewModelScope.launch {
            modelDownloadManager.startOrResumeDownload(model)
        }
    }

    fun pauseModelDownload(modelId: String) {
        modelDownloadManager.pauseDownload(modelId)
    }

    fun deleteModel(modelId: String) {
        modelDownloadManager.deleteModel(modelId)
        viewModelScope.launch {
            db.modelDao().deleteModel(modelId)
        }
    }

    fun loadLocalModel(modelId: String) {
        viewModelScope.launch {
            val success = localAiProvider.tryLoadInstalledModel(modelId)
            if (success) {
                db.modelDao().setActiveModel(modelId)
            }
        }
    }

    // Developer actions
    fun runDeveloperAction(action: DeveloperAction, language: String, codeSnippet: String, notes: String?) {
        val request = DeveloperToolsEngine.buildDeveloperRequest(action, language, codeSnippet, notes)
        sendMessage(request.prompt, taskType = TaskType.CODING, isCodeAudit = action == DeveloperAction.FIX)
    }

    // RAG Knowledge base controls
    fun importDocument(title: String, fileType: String, content: String) {
        viewModelScope.launch {
            ragEngine.importDocument(title, fileType, content)
        }
    }

    fun deleteDocument(docId: Long) {
        viewModelScope.launch {
            ragEngine.deleteDocument(docId)
        }
    }

    // Team Rules controls
    fun toggleTeamRule(rule: TeamRuleEntity) {
        viewModelScope.launch {
            teamRuleEngine.updateRule(rule.copy(isEnabled = !rule.isEnabled))
        }
    }

    fun addTeamRule(title: String, category: String, priority: Int, content: String, isStrict: Boolean) {
        viewModelScope.launch {
            teamRuleEngine.addRule(title, category, priority, content, isStrict)
        }
    }

    fun deleteTeamRule(ruleId: Long) {
        viewModelScope.launch {
            teamRuleEngine.deleteRule(ruleId)
        }
    }

    // GitHub Release & Large Asset Downloader Actions
    fun saveGitHubConfig(
        url: String,
        expectedSha256: String,
        minStorageGb: Float,
        targetFileName: String
    ) {
        secureStorage.gitHubReleaseUrl = url
        secureStorage.gitHubReleaseExpectedSha256 = expectedSha256
        secureStorage.gitHubReleaseMinStorageGb = minStorageGb
        secureStorage.gitHubReleaseTargetFileName = targetFileName

        _gitHubReleaseUrl.value = url
        _gitHubExpectedSha256.value = expectedSha256
        _gitHubMinStorageGb.value = minStorageGb
        _gitHubTargetFileName.value = targetFileName
    }

    fun startGitHubReleaseDownload(
        url: String = _gitHubReleaseUrl.value,
        expectedSha256: String = _gitHubExpectedSha256.value,
        minStorageGb: Float = _gitHubMinStorageGb.value,
        targetFileName: String = _gitHubTargetFileName.value
    ) {
        val minBytes = (minStorageGb * 1024.0 * 1024.0 * 1024.0).toLong()
        gitHubReleaseDownloader.startDownload(
            url = url,
            expectedSha256 = expectedSha256,
            minStorageBytes = minBytes,
            targetFileName = targetFileName
        )
    }

    fun pauseGitHubReleaseDownload() {
        gitHubReleaseDownloader.pauseDownload()
    }

    fun resumeGitHubReleaseDownload() {
        startGitHubReleaseDownload()
    }

    fun cancelGitHubReleaseDownload() {
        gitHubReleaseDownloader.cancelAndDelete(_gitHubTargetFileName.value)
    }

    suspend fun exportTeamRulesJson(): String {
        return teamRuleEngine.exportRulesToJson()
    }

    fun importTeamRulesFromJson(json: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val result = teamRuleEngine.importRulesFromJson(json)
            if (result.isSuccess) {
                _ruleSyncStatus.value = "Successfully imported ${result.getOrDefault(0)} rules."
                onComplete(result.getOrDefault(0))
            } else {
                _ruleSyncStatus.value = "Import failed: ${result.exceptionOrNull()?.message}"
                onComplete(0)
            }
        }
    }

    fun syncTeamRulesFromUrl(url: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _ruleSyncStatus.value = "Syncing rules from remote URL..."
            val okHttp = okhttp3.OkHttpClient()
            val result = teamRuleEngine.fetchAndSyncRemoteRules(url, okHttp)
            if (result.isSuccess) {
                val count = result.getOrDefault(0)
                val msg = "Synced $count team rules from remote!"
                _ruleSyncStatus.value = msg
                onComplete(true, msg)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to sync rules"
                _ruleSyncStatus.value = "Error: $err"
                onComplete(false, err)
            }
        }
    }

    fun connectGoogleAccount(email: String, name: String = "GM Ripon", token: String = "") {
        googleDriveManager.connectGoogleAccount(email, name, token)
    }

    fun disconnectGoogleAccount() {
        googleDriveManager.disconnectGoogleAccount()
    }

    fun setPreferCloudStorage(prefer: Boolean) {
        googleDriveManager.setPreferCloudStorage(prefer)
    }

    fun syncTeamRulesToGoogleDrive(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val rulesJson = teamRuleEngine.exportRulesToJson()
                val result = googleDriveManager.syncTeamRulesToDrive(rulesJson)
                if (result.isSuccess) {
                    onComplete(true, result.getOrNull() ?: "Team rules synced to Google Drive successfully.")
                } else {
                    onComplete(false, result.exceptionOrNull()?.message ?: "Failed to sync to Google Drive")
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun syncRagToGoogleDrive(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val docs = ragDocuments.value
                val jsonArr = org.json.JSONArray()
                docs.forEach { doc ->
                    val obj = org.json.JSONObject().apply {
                        put("id", doc.id)
                        put("title", doc.title)
                        put("fileType", doc.fileType)
                        put("rawContent", doc.rawContent)
                        put("chunkCount", doc.chunkCount)
                        put("createdAt", doc.createdAt)
                    }
                    jsonArr.put(obj)
                }
                val result = googleDriveManager.syncRagArchiveToDrive(jsonArr.toString(2))
                if (result.isSuccess) {
                    onComplete(true, "RAG archive synced to Google Drive (${docs.size} documents).")
                } else {
                    onComplete(false, result.exceptionOrNull()?.message ?: "Failed to sync RAG archive")
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun syncChatHistoryToGoogleDrive(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val msgs = chatMessages.value
                val jsonArr = org.json.JSONArray()
                msgs.forEach { m ->
                    val obj = org.json.JSONObject().apply {
                        put("id", m.id)
                        put("role", m.role)
                        put("content", m.content)
                        put("modelUsed", m.modelUsed)
                        put("timestamp", m.timestamp)
                    }
                    jsonArr.put(obj)
                }
                val result = googleDriveManager.syncChatHistoryToDrive(jsonArr.toString(2))
                if (result.isSuccess) {
                    onComplete(true, "Chat history saved to Google Drive (${msgs.size} messages).")
                } else {
                    onComplete(false, result.exceptionOrNull()?.message ?: "Failed to sync chat history")
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Unknown error")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceAssistant.release()
    }
}
