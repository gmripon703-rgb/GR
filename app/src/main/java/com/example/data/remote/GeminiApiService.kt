package com.example.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object ApiClientFactory {
    private const val DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/"

    private fun createOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    fun createService(baseUrl: String? = null): GeminiApiService {
        val targetUrl = if (!baseUrl.isNullOrBlank() && baseUrl.startsWith("http")) {
            if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        } else {
            DEFAULT_BASE_URL
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(targetUrl)
            .client(createOkHttpClient())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        return retrofit.create(GeminiApiService::class.java)
    }
}
