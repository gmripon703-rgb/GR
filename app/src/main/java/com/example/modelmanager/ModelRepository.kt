package com.example.modelmanager

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

interface ModelRepository {
    val repositoryName: String
    suspend fun getModels(): List<ModelInfo>
    suspend fun fetchRemoteManifest(manifestUrl: String): ModelManifest?
}

class HuggingFaceModelRepository(private val okHttpClient: OkHttpClient) : ModelRepository {
    override val repositoryName: String = "Hugging Face Public Hub"

    override suspend fun getModels(): List<ModelInfo> {
        // Returns the catalog of Hugging Face hosted open-source models
        return DefaultModelCatalog.curatedModels.filter { it.sourceUrl.contains("huggingface.co") }
    }

    override suspend fun fetchRemoteManifest(manifestUrl: String): ModelManifest? {
        return try {
            val request = Request.Builder().url(manifestUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            parseManifestJson(body)
        } catch (_: Exception) {
            null
        }
    }
}

class GitHubReleaseRepository(private val okHttpClient: OkHttpClient) : ModelRepository {
    override val repositoryName: String = "GitHub Releases"

    override suspend fun getModels(): List<ModelInfo> {
        return DefaultModelCatalog.curatedModels.filter { it.sourceUrl.contains("github.com") }
    }

    override suspend fun fetchRemoteManifest(manifestUrl: String): ModelManifest? {
        return try {
            val request = Request.Builder().url(manifestUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            parseManifestJson(body)
        } catch (_: Exception) {
            null
        }
    }
}

class GenericHttpModelRepository(private val okHttpClient: OkHttpClient) : ModelRepository {
    override val repositoryName: String = "Generic HTTP Endpoint"

    override suspend fun getModels(): List<ModelInfo> {
        return DefaultModelCatalog.curatedModels
    }

    override suspend fun fetchRemoteManifest(manifestUrl: String): ModelManifest? {
        return try {
            val request = Request.Builder().url(manifestUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            parseManifestJson(body)
        } catch (_: Exception) {
            null
        }
    }
}

private fun parseManifestJson(jsonString: String): ModelManifest? {
    return try {
        val root = JSONObject(jsonString)
        val version = root.optInt("version", 1)
        val lastUpdated = root.optLong("lastUpdated", System.currentTimeMillis())
        val modelsArray = root.getJSONArray("models")
        val models = mutableListOf<ModelInfo>()

        for (i in 0 until modelsArray.length()) {
            val obj = modelsArray.getJSONObject(i)
            models.add(
                ModelInfo(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    version = obj.optString("version", "1.0.0"),
                    format = obj.optString("format", "GGUF"),
                    quantization = obj.optString("quantization", "Q4_K_M"),
                    sizeBytes = obj.optLong("sizeBytes", 0L),
                    minimumRamGb = obj.optDouble("minimumRamGb", 4.0).toFloat(),
                    recommendedRamGb = obj.optDouble("recommendedRamGb", 6.0).toFloat(),
                    minimumStorageBytes = obj.optLong("minimumStorageBytes", 1000000000L),
                    architecture = obj.optString("architecture", "llama"),
                    downloadUrl = obj.getString("downloadUrl"),
                    sha256 = obj.optString("sha256", ""),
                    license = obj.optString("license", "Apache-2.0"),
                    sourceUrl = obj.optString("sourceUrl", ""),
                    author = obj.optString("author", "Community"),
                    category = obj.optString("category", "CODING"),
                    description = obj.optString("description", "")
                )
            )
        }
        ModelManifest(version, lastUpdated, models)
    } catch (_: Exception) {
        null
    }
}
