package com.example.security

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class SecureStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("devsec_ai_secure_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ALLOW_CLOUD_AI = "allow_cloud_ai"
        private const val KEY_PROVIDER_MODE = "provider_mode" // "AUTO", "OFFLINE_ONLY", "ONLINE_ONLY"
        private const val KEY_CUSTOM_API_KEY = "custom_api_key"
        private const val KEY_CUSTOM_BASE_URL = "custom_base_url"
        private const val KEY_ACTIVE_MODEL_ID = "active_model_id"
        private const val KEY_ACTIVE_ONLINE_MODEL = "active_online_model"
        private const val KEY_GITHUB_RELEASE_URL = "github_release_url"
        private const val KEY_GITHUB_EXPECTED_SHA256 = "github_expected_sha256"
        private const val KEY_GITHUB_MIN_STORAGE_GB = "github_min_storage_gb"
        private const val KEY_GITHUB_TARGET_FILENAME = "github_target_filename"
        private const val KEY_GOOGLE_DRIVE_CONNECTED = "google_drive_connected"
        private const val KEY_GOOGLE_ACCOUNT_EMAIL = "google_account_email"
        private const val KEY_GOOGLE_ACCOUNT_DISPLAY_NAME = "google_account_display_name"
        private const val KEY_GOOGLE_DRIVE_ACCESS_TOKEN = "google_drive_access_token"
        private const val KEY_GOOGLE_DRIVE_LAST_SYNC = "google_drive_last_sync"
        private const val KEY_PREFER_CLOUD_STORAGE = "prefer_cloud_storage"
    }

    var googleDriveConnected: Boolean
        get() = prefs.getBoolean(KEY_GOOGLE_DRIVE_CONNECTED, false)
        set(value) = prefs.edit().putBoolean(KEY_GOOGLE_DRIVE_CONNECTED, value).apply()

    var googleAccountEmail: String
        get() = prefs.getString(KEY_GOOGLE_ACCOUNT_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GOOGLE_ACCOUNT_EMAIL, value.trim()).apply()

    var googleAccountDisplayName: String
        get() = prefs.getString(KEY_GOOGLE_ACCOUNT_DISPLAY_NAME, "GM Ripon") ?: "GM Ripon"
        set(value) = prefs.edit().putString(KEY_GOOGLE_ACCOUNT_DISPLAY_NAME, value.trim()).apply()

    var googleDriveAccessToken: String
        get() = prefs.getString(KEY_GOOGLE_DRIVE_ACCESS_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GOOGLE_DRIVE_ACCESS_TOKEN, value.trim()).apply()

    var googleDriveLastSync: Long
        get() = prefs.getLong(KEY_GOOGLE_DRIVE_LAST_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_GOOGLE_DRIVE_LAST_SYNC, value).apply()

    var preferCloudStorage: Boolean
        get() = prefs.getBoolean(KEY_PREFER_CLOUD_STORAGE, true)
        set(value) = prefs.edit().putBoolean(KEY_PREFER_CLOUD_STORAGE, value).apply()

    var gitHubReleaseUrl: String
        get() = prefs.getString(
            KEY_GITHUB_RELEASE_URL,
            "https://github.com/OWNER/REPOSITORY/releases/download/v1.0.0/custom-team-model.gguf"
        ) ?: "https://github.com/OWNER/REPOSITORY/releases/download/v1.0.0/custom-team-model.gguf"
        set(value) = prefs.edit().putString(KEY_GITHUB_RELEASE_URL, value.trim()).apply()

    var gitHubReleaseExpectedSha256: String
        get() = prefs.getString(KEY_GITHUB_EXPECTED_SHA256, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GITHUB_EXPECTED_SHA256, value.trim().lowercase()).apply()

    var gitHubReleaseMinStorageGb: Float
        get() = prefs.getFloat(KEY_GITHUB_MIN_STORAGE_GB, 2.0f)
        set(value) = prefs.edit().putFloat(KEY_GITHUB_MIN_STORAGE_GB, value).apply()

    var gitHubReleaseTargetFileName: String
        get() = prefs.getString(KEY_GITHUB_TARGET_FILENAME, "custom-team-model.gguf") ?: "custom-team-model.gguf"
        set(value) = prefs.edit().putString(KEY_GITHUB_TARGET_FILENAME, value.trim()).apply()

    var allowCloudAi: Boolean
        get() = prefs.getBoolean(KEY_ALLOW_CLOUD_AI, true)
        set(value) = prefs.edit().putBoolean(KEY_ALLOW_CLOUD_AI, value).apply()

    var providerMode: String
        get() = prefs.getString(KEY_PROVIDER_MODE, "AUTO") ?: "AUTO"
        set(value) = prefs.edit().putString(KEY_PROVIDER_MODE, value).apply()

    var customApiKey: String
        get() = prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_API_KEY, value.trim()).apply()

    var customBaseUrl: String
        get() = prefs.getString(KEY_CUSTOM_BASE_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_BASE_URL, value.trim()).apply()

    var activeModelId: String
        get() = prefs.getString(KEY_ACTIVE_MODEL_ID, "offline-rule-coder") ?: "offline-rule-coder"
        set(value) = prefs.edit().putString(KEY_ACTIVE_MODEL_ID, value).apply()

    var activeOnlineModel: String
        get() = prefs.getString(KEY_ACTIVE_ONLINE_MODEL, "gemini-3.5-flash") ?: "gemini-3.5-flash"
        set(value) = prefs.edit().putString(KEY_ACTIVE_ONLINE_MODEL, value).apply()

    fun getEffectiveApiKey(): String {
        val userKey = customApiKey
        if (userKey.isNotBlank()) return userKey
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") key else ""
        } catch (_: Exception) {
            ""
        }
    }
}
