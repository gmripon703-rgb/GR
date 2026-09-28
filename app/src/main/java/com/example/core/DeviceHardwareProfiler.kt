package com.example.core

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File

data class DeviceHardwareProfile(
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val totalStorageBytes: Long,
    val availableStorageBytes: Long,
    val cpuArchitecture: String,
    val cpuCoreCount: Int,
    val androidVersion: String,
    val sdkInt: Int,
    val hasGpuNpuSupport: Boolean,
    val recommendedMaxModelRamGb: Float
) {
    val totalRamGb: Float get() = totalRamBytes / (1024f * 1024f * 1024f)
    val availableRamGb: Float get() = availableRamBytes / (1024f * 1024f * 1024f)
    val availableStorageGb: Float get() = availableStorageBytes / (1024f * 1024f * 1024f)
    val totalStorageGb: Float get() = totalStorageBytes / (1024f * 1024f * 1024f)
}

object DeviceHardwareProfiler {

    fun profile(context: Context): DeviceHardwareProfile {
        // Memory Info
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalRam = memInfo.totalMem
        val availRam = memInfo.availMem

        // Storage Info
        val appDataDir = context.filesDir
        val stat = StatFs(appDataDir.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availBlocks = stat.availableBlocksLong
        val totalStorage = totalBlocks * blockSize
        val availStorage = availBlocks * blockSize

        // CPU & Architecture
        val cpuArch = if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else "unknown"
        val cpuCores = Runtime.getRuntime().availableProcessors()

        // GPU / NPU capability heuristic (ARM64 with Android 10+ / SDK 29+ supports NNAPI and Vulkan)
        val hasNpuSupport = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                (cpuArch.contains("arm64") || cpuArch.contains("v8a"))

        // Safe RAM allocation threshold (leave at least 1.5GB - 2GB for Android OS & app)
        val totalRamGb = totalRam / (1024f * 1024f * 1024f)
        val recommendedModelRamGb = when {
            totalRamGb >= 12f -> 7.0f
            totalRamGb >= 8f -> 4.5f
            totalRamGb >= 6f -> 3.0f
            totalRamGb >= 4f -> 1.8f
            else -> 0.8f
        }

        return DeviceHardwareProfile(
            totalRamBytes = totalRam,
            availableRamBytes = availRam,
            totalStorageBytes = totalStorage,
            availableStorageBytes = availStorage,
            cpuArchitecture = cpuArch,
            cpuCoreCount = cpuCores,
            androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            sdkInt = Build.VERSION.SDK_INT,
            hasGpuNpuSupport = hasNpuSupport,
            recommendedMaxModelRamGb = recommendedModelRamGb
        )
    }

    fun isModelRecommended(profile: DeviceHardwareProfile, minimumRamGb: Float): Boolean {
        return profile.totalRamGb >= minimumRamGb
    }

    fun hasSufficientStorage(profile: DeviceHardwareProfile, requiredBytes: Long): Boolean {
        // Require at least requiredBytes + 500MB safety buffer
        val safetyBuffer = 500L * 1024 * 1024
        return profile.availableStorageBytes >= (requiredBytes + safetyBuffer)
    }
}
