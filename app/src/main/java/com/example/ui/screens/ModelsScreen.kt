package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.platform.LocalContext
import com.example.modelmanager.GitHubDownloadProgress
import com.example.modelmanager.GitHubDownloadStatus
import com.example.modelmanager.GitHubReleaseConfig
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.DeviceHardwareProfiler
import com.example.modelmanager.DefaultModelCatalog
import com.example.modelmanager.DownloadState
import com.example.modelmanager.ModelDownloadProgress
import com.example.modelmanager.ModelInfo
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ModelsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val hardwareProfile by viewModel.hardwareProfile.collectAsState()
    val progressMap by viewModel.downloadProgressMap.collectAsState()
    val models = DefaultModelCatalog.curatedModels

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Curated Models", "GitHub Release", "Google Drive", "Installed", "Active Downloads")

    var confirmingModelDownload by remember { mutableStateOf<ModelInfo?>(null) }
    var modelDetailsToShow by remember { mutableStateOf<ModelInfo?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hardware Resource Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Hardware Profiler & Device Compatibility",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HardwareMetric(
                        icon = Icons.Default.Memory,
                        label = "RAM (Avail / Total)",
                        value = "${"%.1f".format(hardwareProfile.availableRamGb)} / ${"%.1f".format(hardwareProfile.totalRamGb)} GB"
                    )
                    HardwareMetric(
                        icon = Icons.Default.Storage,
                        label = "Storage Free",
                        value = "${"%.1f".format(hardwareProfile.availableStorageGb)} GB"
                    )
                    HardwareMetric(
                        icon = Icons.Default.Verified,
                        label = "CPU Arch / Cores",
                        value = "${hardwareProfile.cpuArchitecture.take(5)} / ${hardwareProfile.cpuCoreCount}c"
                    )
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = DarkSurfaceVariant,
            contentColor = CyberGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = CyberGreen
                )
            }
        ) {
            tabTitles.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTabIndex == idx,
                    onClick = { selectedTabIndex = idx },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
            }
        }

        when (selectedTabIndex) {
            1 -> {
                GitHubReleaseDownloaderView(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
            }
            2 -> {
                GoogleDriveCloudModelsView(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
            }
            else -> {
                // Filter models by tab
                val filteredModels = when (selectedTabIndex) {
                    3 -> models.filter { progressMap[it.id]?.state == DownloadState.INSTALLED }
                    4 -> models.filter {
                        val state = progressMap[it.id]?.state
                        state == DownloadState.DOWNLOADING || state == DownloadState.PAUSED || state == DownloadState.VERIFYING
                    }
                    else -> models
                }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredModels, key = { it.id }) { model ->
                    val progress = progressMap[model.id] ?: ModelDownloadProgress(
                        modelId = model.id,
                        state = DownloadState.NOT_DOWNLOADED
                    )
                    val isRamCompatible = DeviceHardwareProfiler.isModelRecommended(hardwareProfile, model.minimumRamGb)
                    val hasStorage = DeviceHardwareProfiler.hasSufficientStorage(hardwareProfile, model.minimumStorageBytes)

                    ModelCard(
                        model = model,
                        progress = progress,
                        isRamCompatible = isRamCompatible,
                        hasStorage = hasStorage,
                        onStartDownload = { confirmingModelDownload = model },
                        onPauseDownload = { viewModel.pauseModelDownload(model.id) },
                        onDeleteModel = { viewModel.deleteModel(model.id) },
                        onLoadIntoMemory = { viewModel.loadLocalModel(model.id) },
                        onShowDetails = { modelDetailsToShow = model }
                    )
                }
            }
        }
    }
}

    // Download Confirmation Dialog (Explicit user confirmation required)
    confirmingModelDownload?.let { targetModel ->
        val hasStorage = DeviceHardwareProfiler.hasSufficientStorage(hardwareProfile, targetModel.minimumStorageBytes)
        AlertDialog(
            onDismissRequest = { confirmingModelDownload = null },
            title = {
                Text(
                    text = "Confirm Model Download",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Download ${targetModel.name} (${targetModel.sizeGbText}) from Hugging Face into private app storage?",
                        fontSize = 13.sp
                    )

                    if (!hasStorage) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyberRed.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ Storage Warning: You need at least ${targetModel.minimumStorageBytes / (1024 * 1024)}MB free space.",
                                color = CyberRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Text(
                        text = "• License: ${targetModel.license}\n• Quantization: ${targetModel.quantization}\n• SHA-256 Checksum: will be verified automatically after download completes.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.startModelDownload(targetModel)
                        confirmingModelDownload = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                    enabled = hasStorage
                ) {
                    Text("Start Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingModelDownload = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Model Details Dialog
    modelDetailsToShow?.let { details ->
        AlertDialog(
            onDismissRequest = { modelDetailsToShow = null },
            title = { Text(details.name, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Description: ${details.description}", fontSize = 12.sp)
                    Text("Author: ${details.author}", fontSize = 11.sp, color = CyberCyan)
                    Text("Format: ${details.format} (${details.quantization})", fontSize = 11.sp)
                    Text("License: ${details.license}", fontSize = 11.sp)
                    Text("Min RAM: ${details.minimumRamGb} GB (Rec: ${details.recommendedRamGb} GB)", fontSize = 11.sp)
                    Text("Source: ${details.sourceUrl}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Expected SHA-256:\n${details.sha256}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { modelDetailsToShow = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun HardwareMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ModelCard(
    model: ModelInfo,
    progress: ModelDownloadProgress,
    isRamCompatible: Boolean,
    hasStorage: Boolean,
    onStartDownload: () -> Unit,
    onPauseDownload: () -> Unit,
    onDeleteModel: () -> Unit,
    onLoadIntoMemory: () -> Unit,
    onShowDetails: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = model.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1B2636))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = model.quantization,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Text(
                    text = model.sizeGbText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = model.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = model.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // RAM Compatibility & License Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isRamCompatible) CyberGreen else CyberAmber)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRamCompatible) "Recommended (${model.minimumRamGb}GB RAM)" else "High RAM (${model.minimumRamGb}GB)",
                        fontSize = 10.sp,
                        color = if (isRamCompatible) CyberGreen else CyberAmber
                    )
                }

                Text(
                    text = "License: ${model.license}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Download State / Progress
            when (progress.state) {
                DownloadState.DOWNLOADING, DownloadState.PAUSED, DownloadState.VERIFYING -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress.percent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyberGreen,
                        trackColor = Color(0xFF0A0F17)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${progress.percent}% • ${progress.speedText}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberCyan
                        )
                        Text(
                            text = progress.remainingMbText,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                DownloadState.ERROR -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ ${progress.errorMessage ?: "Download failed"}",
                        fontSize = 10.sp,
                        color = CyberRed
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (progress.state) {
                    DownloadState.NOT_DOWNLOADED, DownloadState.ERROR -> {
                        Button(
                            onClick = onStartDownload,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download Model", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    DownloadState.DOWNLOADING -> {
                        Button(
                            onClick = onPauseDownload,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyberAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pause", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    DownloadState.PAUSED -> {
                        Button(
                            onClick = onStartDownload,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    DownloadState.VERIFYING -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = CyberGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verifying SHA-256...", fontSize = 11.sp, color = CyberGreen)
                        }
                    }
                    DownloadState.INSTALLED -> {
                        Button(
                            onClick = onLoadIntoMemory,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen.copy(alpha = 0.15f), contentColor = CyberGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Active in Offline Engine", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    DownloadState.UPDATE_AVAILABLE -> {
                        Button(
                            onClick = onStartDownload,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberAmber, contentColor = Color(0xFF452B00)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Update Model", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (progress.state == DownloadState.INSTALLED || progress.state == DownloadState.PAUSED) {
                    IconButton(
                        onClick = onDeleteModel,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = CyberRed, modifier = Modifier.size(18.dp))
                    }
                }

                TextButton(onClick = onShowDetails) {
                    Text("Details", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun GitHubReleaseDownloaderView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val progress by viewModel.gitHubDownloadProgress.collectAsState()
    val hardwareProfile by viewModel.hardwareProfile.collectAsState()

    val configuredUrl by viewModel.gitHubReleaseUrl.collectAsState()
    val configuredSha256 by viewModel.gitHubExpectedSha256.collectAsState()
    val configuredMinStorageGb by viewModel.gitHubMinStorageGb.collectAsState()
    val configuredTargetFileName by viewModel.gitHubTargetFileName.collectAsState()

    var inputUrl by remember(configuredUrl) { mutableStateOf(configuredUrl) }
    var inputSha256 by remember(configuredSha256) { mutableStateOf(configuredSha256) }
    var inputTargetFileName by remember(configuredTargetFileName) { mutableStateOf(configuredTargetFileName) }
    var inputMinStorageGb by remember(configuredMinStorageGb) { mutableStateOf(configuredMinStorageGb.toString()) }

    var configSavedMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Educational & Architecture Guide Banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberGreen.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hosting 1.5GB Files Free via GitHub Releases",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 2GB Free Limit: GitHub Releases allow attaching files up to 2.0 GB per asset at zero cost.\n" +
                               "• No Git Repo Bloat: Binary files live in GitHub Release asset storage, keeping your .git repository tiny.\n" +
                               "• Anonymous Direct HTTPS: Public assets require no login, tokens, or credentials in the APK.\n" +
                               "• Memory-Safe Streaming: Streams directly to app storage in 64KB chunks — never spikes RAM.\n" +
                               "• SHA-256 Verification: Automatically validates file integrity before allowing execution.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Configuration Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Configure Release Asset & Checksum",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Quick Presets:", fontSize = 11.sp, color = CyberCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GitHubReleaseConfig.PRESET_ASSETS.forEach { preset ->
                            FilterChip(
                                selected = inputUrl == preset.url,
                                onClick = {
                                    inputUrl = preset.url
                                    if (preset.expectedSha256.isNotBlank()) {
                                        inputSha256 = preset.expectedSha256
                                    }
                                    inputTargetFileName = GitHubReleaseConfig.extractFilename(preset.url, "custom-team-model.gguf")
                                },
                                label = { Text(preset.label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = CyberGreen
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("GitHub Release Asset / HTTPS Direct URL:", fontSize = 11.sp, color = CyberCyan)
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        placeholder = { Text(GitHubReleaseConfig.GITHUB_RELEASE_URL_TEMPLATE, fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("github_release_url_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberGreen,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Expected SHA-256 Checksum (Optional):", fontSize = 11.sp, color = CyberCyan)
                        Row {
                            TextButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = cm.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        inputSha256 = clip.getItemAt(0).text.toString().trim()
                                    }
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Paste", fontSize = 10.sp, color = CyberGreen)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = { inputSha256 = "" },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Clear", fontSize = 10.sp, color = CyberRed)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = inputSha256,
                        onValueChange = { inputSha256 = it.trim().lowercase() },
                        placeholder = { Text("e.g. 679f22580a58a6ee5f272a8d5f30999...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberGreen,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Target Filename:", fontSize = 11.sp, color = CyberCyan)
                            OutlinedTextField(
                                value = inputTargetFileName,
                                onValueChange = { inputTargetFileName = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Min Free Storage (GB):", fontSize = 11.sp, color = CyberCyan)
                            OutlinedTextField(
                                value = inputMinStorageGb,
                                onValueChange = { inputMinStorageGb = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Free on device: ${"%.1f".format(hardwareProfile.availableStorageGb)} GB",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                val minGb = inputMinStorageGb.toFloatOrNull() ?: 2.0f
                                viewModel.saveGitHubConfig(
                                    url = inputUrl,
                                    expectedSha256 = inputSha256,
                                    minStorageGb = minGb,
                                    targetFileName = inputTargetFileName
                                )
                                configSavedMessage = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyberGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (configSavedMessage) "Config Saved ✓" else "Save Config", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Download & Status Dashboard Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when (progress.status) {
                        GitHubDownloadStatus.COMPLETED -> CyberGreen
                        GitHubDownloadStatus.DOWNLOADING -> CyberCyan
                        GitHubDownloadStatus.ERROR -> CyberRed
                        else -> DarkCardBorder
                    }
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Download Dashboard (~1.5 GB)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = progress.targetFileName.ifBlank { inputTargetFileName },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (progress.status) {
                                        GitHubDownloadStatus.DOWNLOADING -> CyberCyan.copy(alpha = 0.2f)
                                        GitHubDownloadStatus.COMPLETED -> CyberGreen.copy(alpha = 0.2f)
                                        GitHubDownloadStatus.PAUSED -> CyberAmber.copy(alpha = 0.2f)
                                        GitHubDownloadStatus.ERROR -> CyberRed.copy(alpha = 0.2f)
                                        GitHubDownloadStatus.VERIFYING_SHA256 -> CyberGreen.copy(alpha = 0.2f)
                                        else -> DarkSurfaceVariant
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = progress.status.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (progress.status) {
                                    GitHubDownloadStatus.DOWNLOADING -> CyberCyan
                                    GitHubDownloadStatus.COMPLETED -> CyberGreen
                                    GitHubDownloadStatus.PAUSED -> CyberAmber
                                    GitHubDownloadStatus.ERROR -> CyberRed
                                    GitHubDownloadStatus.VERIFYING_SHA256 -> CyberGreen
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { (progress.percent.coerceIn(0, 100)) / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when (progress.status) {
                            GitHubDownloadStatus.COMPLETED -> CyberGreen
                            GitHubDownloadStatus.ERROR -> CyberRed
                            else -> CyberCyan
                        },
                        trackColor = DarkSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${progress.percent}% • ${progress.downloadedText}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Speed: ${progress.speedText} • ETA: ${progress.etaText}",
                            fontSize = 11.sp,
                            color = CyberCyan
                        )
                    }

                    // Error Message Banner
                    progress.errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyberRed.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ $err",
                                color = CyberRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Completed Details Banner
                    if (progress.status == GitHubDownloadStatus.COMPLETED) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyberGreen.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberGreen.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Downloaded & Verified Clean", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                                }
                                progress.verifiedSha256?.let { sha ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "SHA-256: ${sha.take(16)}...${sha.takeLast(8)}",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (progress.status) {
                            GitHubDownloadStatus.IDLE, GitHubDownloadStatus.ERROR -> {
                                Button(
                                    onClick = {
                                        val minGb = inputMinStorageGb.toFloatOrNull() ?: 2.0f
                                        viewModel.startGitHubReleaseDownload(
                                            url = inputUrl,
                                            expectedSha256 = inputSha256,
                                            minStorageGb = minGb,
                                            targetFileName = inputTargetFileName
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Start Download", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            GitHubDownloadStatus.DOWNLOADING -> {
                                Button(
                                    onClick = { viewModel.pauseGitHubReleaseDownload() },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyberAmber),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pause", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            GitHubDownloadStatus.PAUSED -> {
                                Button(
                                    onClick = { viewModel.resumeGitHubReleaseDownload() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00381B)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Resume", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            GitHubDownloadStatus.VERIFYING_SHA256, GitHubDownloadStatus.CHECKING_STORAGE, GitHubDownloadStatus.CONNECTING -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = CyberGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Processing...", fontSize = 11.sp, color = CyberGreen)
                                }
                            }

                            GitHubDownloadStatus.COMPLETED -> {
                                Button(
                                    onClick = {
                                        if (inputTargetFileName.endsWith(".json")) {
                                            progress.localFilePath?.let { path ->
                                                val file = java.io.File(path)
                                                if (file.exists()) {
                                                    viewModel.importTeamRulesFromJson(file.readText()) { count ->
                                                        android.widget.Toast.makeText(context, "Imported $count team rules!", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        } else {
                                            viewModel.loadLocalModel("github-custom-model")
                                            android.widget.Toast.makeText(context, "Loaded into local AI engine!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen.copy(alpha = 0.2f), contentColor = CyberGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (inputTargetFileName.endsWith(".json")) "Import as Team Rules" else "Active in Offline Engine",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (progress.status != GitHubDownloadStatus.IDLE) {
                            IconButton(
                                onClick = { viewModel.cancelGitHubReleaseDownload() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Cancel & Delete", tint = CyberRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleDriveCloudModelsView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isConnected by viewModel.isDriveConnected.collectAsState()
    val accountEmail by viewModel.driveAccountEmail.collectAsState()
    val driveQuota by viewModel.driveQuota.collectAsState()
    val driveFiles by viewModel.driveFiles.collectAsState()
    val isSyncing by viewModel.isDriveSyncing.collectAsState()
    val preferCloudStorage by viewModel.preferCloudStorage.collectAsState()

    var driveUrlInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf(accountEmail.ifBlank { "gmripon703@gmail.com" }) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Purpose Card
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isConnected) CyberGreen else DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = if (isConnected) CyberGreen else CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google Drive Cloud Space",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isConnected) CyberGreen.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isConnected) "CONNECTED" else "NOT LINKED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) CyberGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Store massive AI developer resources, free GPT prompt bundles, and GGUF models directly in Google Drive cloud storage without occupying internal device storage.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isConnected) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(accountEmail.ifBlank { "gmripon703@gmail.com" }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Text("15 GB Quota", fontSize = 10.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                                }

                                driveQuota?.let { q ->
                                    LinearProgressIndicator(
                                        progress = { (q.usagePercent / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = CyberGreen,
                                        trackColor = Color(0xFF1E2836)
                                    )
                                    Text(
                                        text = "${q.formattedUsed} used of ${q.formattedLimit} (${q.formattedFree} free on Google Drive)",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Prefer Google Drive over phone flash", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Saves internal disk space by streaming AI data from Google Drive.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = preferCloudStorage,
                                onCheckedChange = { viewModel.setPreferCloudStorage(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen, checkedTrackColor = Color(0xFF00532B))
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Google Account Email:") },
                            placeholder = { Text("gmripon703@gmail.com") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = { viewModel.connectGoogleAccount(emailInput, "GM Ripon") },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connect Google Account for Google Drive", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Direct Google Drive Link Importer Card
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Import Large AI Model / Resource from Google Drive",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )

                    Text(
                        text = "Paste any Google Drive sharing link (e.g. https://drive.google.com/file/d/FILE_ID/view). GR AI will convert it into a streaming endpoint without consuming phone storage.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = driveUrlInput,
                        onValueChange = { driveUrlInput = it },
                        label = { Text("Google Drive File Link / URL:") },
                        placeholder = { Text("https://drive.google.com/file/d/...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = CyberCyan) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val converted = GitHubReleaseConfig.convertGoogleDriveUrl(driveUrlInput)
                                if (converted.isNotBlank()) {
                                    statusMessage = "Converted Google Drive direct stream: $converted"
                                }
                            },
                            enabled = driveUrlInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import from Drive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                driveUrlInput = "https://drive.google.com/file/d/1A2B3C4D5E6F7G8H9I0J/view?usp=sharing"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Sample Link", fontSize = 11.sp)
                        }
                    }

                    statusMessage?.let { msg ->
                        Text(text = msg, fontSize = 10.sp, color = CyberGreen, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Synced Google Drive Files List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Google Drive Synced AI Assets (${driveFiles.size}):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberGreen, strokeWidth = 2.dp)
                }
            }
        }

        items(driveFiles, key = { it.id }) { file ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(file.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = "${file.sizeBytes / 1024} KB • Google Drive Cloud • ${file.modifiedTime}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text("CLOUD STORED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                    }
                }
            }
        }
    }
}

