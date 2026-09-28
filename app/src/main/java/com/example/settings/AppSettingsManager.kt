package com.example.settings

import android.content.Context
import com.example.security.SecureStorage

/**
 * AppSettingsManager encapsulates user preferences, inference parameters,
 * privacy controls, and team synchronization options.
 */
class AppSettingsManager(context: Context) {

    private val secureStorage = SecureStorage(context)

    // Provider preference: AUTO, OFFLINE_ONLY, ONLINE_ONLY, CUSTOM_LAN
    var providerMode: String
        get() = secureStorage.providerMode
        set(value) { secureStorage.providerMode = value }

    // Privacy toggle: whether cloud calls are allowed
    var allowCloudAi: Boolean
        get() = secureStorage.allowCloudAi
        set(value) { secureStorage.allowCloudAi = value }

    // Cloud & LAN configuration
    var customApiKey: String
        get() = secureStorage.customApiKey
        set(value) { secureStorage.customApiKey = value }

    var customBaseUrl: String
        get() = secureStorage.customBaseUrl
        set(value) { secureStorage.customBaseUrl = value }

    var activeOnlineModel: String
        get() = secureStorage.activeOnlineModel
        set(value) { secureStorage.activeOnlineModel = value }

    // Inference configuration
    var temperature: Float = 0.3f
    var maxTokens: Int = 2048
    var topP: Float = 0.9f
    var cpuThreads: Int = 4

    // Feature toggles
    var autoInjectRag: Boolean = true
    var autoInjectTeamRules: Boolean = true
    var requireSafeCommandConfirmation: Boolean = true
    var voiceAutoSpeak: Boolean = false

    fun getEffectiveApiKey(): String = secureStorage.getEffectiveApiKey()
}
