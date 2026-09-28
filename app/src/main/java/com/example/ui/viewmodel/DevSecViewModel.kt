package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AuditFindingEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.OwaspItemEntity
import com.example.data.repository.DevSecRepository
import com.example.domain.analyzer.OfflineVulnerabilityScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DevSecViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DevSecRepository(application)

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditFindings: StateFlow<List<AuditFindingEntity>> = repository.auditFindings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val owaspItems: StateFlow<List<OwaspItemEntity>> = repository.owaspItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _selectedModel = MutableStateFlow(repository.getSelectedModel())
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _customApiKey = MutableStateFlow(repository.getCustomApiKey())
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _customBaseUrl = MutableStateFlow(repository.getCustomBaseUrl())
    val customBaseUrl: StateFlow<String> = _customBaseUrl.asStateFlow()

    private val _codeInput = MutableStateFlow(OfflineVulnerabilityScanner.SAMPLE_KOTLIN_ANDROID)
    val codeInput: StateFlow<String> = _codeInput.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureOwaspDataInitialized()
        }
    }

    fun setCodeInput(code: String) {
        _codeInput.value = code
    }

    fun setShowSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    fun sendMessage(text: String, isCodeAudit: Boolean = false) {
        if (text.isBlank() || _isGenerating.value) return
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                repository.sendChatMessage(text.trim(), isCodeAudit)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun scanCode() {
        val code = _codeInput.value
        if (code.isBlank()) return
        viewModelScope.launch {
            _isScanning.value = true
            try {
                repository.runCodeScan(code)
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun loadSample(type: Int) {
        val sample = when (type) {
            1 -> OfflineVulnerabilityScanner.SAMPLE_KOTLIN_ANDROID
            2 -> OfflineVulnerabilityScanner.SAMPLE_PYTHON_BACKEND
            3 -> OfflineVulnerabilityScanner.SAMPLE_JAVA_SERVICE
            else -> ""
        }
        _codeInput.value = sample
    }

    fun askAiToRemediate(finding: AuditFindingEntity) {
        val prompt = """
🛡️ **Security Audit Remediation Request**
- **Vulnerability:** ${finding.title} (${finding.cweId})
- **Severity:** ${finding.severity}
- **Vulnerable Line/Snippet:**
```
${finding.matchedSnippet}
```
- **Context/Explanation:** ${finding.explanation}

Please provide:
1. Exact exploitation risk for an internal system.
2. A hardened, production-ready code replacement/patch.
3. Relevant defensive checks to prevent regressions.
""".trimIndent()

        sendMessage(prompt, isCodeAudit = true)
    }

    fun updateOwaspItem(code: String, status: String, notes: String) {
        viewModelScope.launch {
            repository.updateOwaspItemStatus(code, status, notes)
        }
    }

    fun resetOwasp() {
        viewModelScope.launch {
            repository.resetOwaspAudit()
        }
    }

    fun saveSettings(model: String, key: String, url: String) {
        repository.setSelectedModel(model)
        repository.setCustomApiKey(key)
        repository.setCustomBaseUrl(url)

        _selectedModel.value = model
        _customApiKey.value = key
        _customBaseUrl.value = url
        _showSettingsDialog.value = false
    }
}
