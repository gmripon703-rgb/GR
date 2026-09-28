package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val providerMode by viewModel.providerMode.collectAsState()
    val allowCloudAi by viewModel.allowCloudAi.collectAsState()
    val hardwareProfile by viewModel.hardwareProfile.collectAsState()
    val activeOnlineModel by viewModel.activeOnlineModel.collectAsState()

    val isDriveConnected by viewModel.isDriveConnected.collectAsState()
    val driveAccountEmail by viewModel.driveAccountEmail.collectAsState()
    val driveAccountDisplayName by viewModel.driveAccountDisplayName.collectAsState()
    val driveQuota by viewModel.driveQuota.collectAsState()
    val isDriveSyncing by viewModel.isDriveSyncing.collectAsState()
    val driveSyncStatus by viewModel.driveSyncStatus.collectAsState()
    val preferCloudStorage by viewModel.preferCloudStorage.collectAsState()

    var googleEmailInput by remember { mutableStateOf(if (driveAccountEmail.isNotBlank()) driveAccountEmail else "gmripon703@gmail.com") }
    var syncFeedbackMsg by remember { mutableStateOf<String?>(null) }

    var apiKeyText by remember { mutableStateOf(viewModel.secureStorage.customApiKey) }
    var baseUrlText by remember { mutableStateOf(viewModel.secureStorage.customBaseUrl) }
    var selectedOnlineModel by remember { mutableStateOf(activeOnlineModel) }
    var isKeyVisible by remember { mutableStateOf(false) }

    val modeOptions = listOf(
        Triple("AUTO", "Auto Router", "Tries cloud if internet available; falls back seamlessly to local on-device engine."),
        Triple("OFFLINE_ONLY", "Offline Only", "100% On-Device compute. Never uses network under any condition."),
        Triple("CUSTOM_LAN", "Private LAN (Ollama / vLLM)", "Direct connection to internal self-hosted server on your team's local network."),
        Triple("ONLINE_ONLY", "Online Cloud", "Queries configured Gemini cloud model.")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section: Provider Mode
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "AI Provider Routing Mode",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    modeOptions.forEach { (modeKey, title, desc) ->
                        val isSelected = providerMode == modeKey
                        Surface(
                            onClick = { viewModel.setProviderMode(modeKey) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CyberGreen.copy(alpha = 0.15f) else DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CyberGreen else DarkCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CyberGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Privacy Controls
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Allow Cloud AI",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (allowCloudAi) "Enabled: Requests can be sent to external/LAN model endpoints." else "Disabled: Cloud queries blocked. All prompts remain strictly on device.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = allowCloudAi,
                            onCheckedChange = { viewModel.setAllowCloudAi(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen, checkedTrackColor = Color(0xFF00532B))
                        )
                    }
                }
            }
        }

        // Section: Real Google Account & Google Drive Cloud Storage
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDriveConnected) CyberGreen else DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDriveConnected) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = "Google Drive",
                                tint = if (isDriveConnected) CyberGreen else CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google Account & Google Drive",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isDriveConnected) CyberGreen.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isDriveConnected) "SYNC ACTIVE" else "NOT CONNECTED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDriveConnected) CyberGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Store large developer AI data, team rules, and GGUF models in Google Drive / GitHub instead of eating phone storage space.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isDriveConnected) {
                        // Account details & Quota
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
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = CyberGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = driveAccountEmail.ifBlank { "gmripon703@gmail.com" },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Text(
                                        text = "15 GB Free",
                                        fontSize = 10.sp,
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                driveQuota?.let { quota ->
                                    LinearProgressIndicator(
                                        progress = { (quota.usagePercent / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = CyberGreen,
                                        trackColor = Color(0xFF1E2836)
                                    )
                                    Text(
                                        text = "${quota.formattedUsed} used of ${quota.formattedLimit} (${quota.formattedFree} free on Google Drive)",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Storage Preference switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Offload Large AI Files to Google Drive",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Keeps internal phone storage free by referencing Google Drive assets.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = preferCloudStorage,
                                onCheckedChange = { viewModel.setPreferCloudStorage(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen, checkedTrackColor = Color(0xFF00532B))
                            )
                        }

                        // Drive Sync Actions
                        Text("Cloud Backup & Sync Actions:", fontSize = 11.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.syncTeamRulesToGoogleDrive { success, msg ->
                                        syncFeedbackMsg = msg
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyberGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text("Sync Rules", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.syncRagToGoogleDrive { success, msg ->
                                        syncFeedbackMsg = msg
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyberCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text("Backup RAG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.syncChatHistoryToGoogleDrive { success, msg ->
                                        syncFeedbackMsg = msg
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text("Save Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.disconnectGoogleAccount() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381414), contentColor = Color(0xFFFF8B8B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("Disconnect Google Account", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Connect flow
                        OutlinedTextField(
                            value = googleEmailInput,
                            onValueChange = { googleEmailInput = it },
                            label = { Text("Google Account Email:") },
                            placeholder = { Text("gmripon703@gmail.com") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                viewModel.connectGoogleAccount(googleEmailInput, "GM Ripon")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connect Google Account for Google Drive", fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "Configured with Google OAuth Client (Project directed-strata-503219-e4, scopes: drive.file, drive.appdata).",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isDriveSyncing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = CyberGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Syncing with Google Drive...", fontSize = 11.sp, color = CyberGreen)
                        }
                    }

                    syncFeedbackMsg?.let { msg ->
                        Text(
                            text = msg,
                            fontSize = 11.sp,
                            color = if (msg.contains("failed") || msg.contains("Error")) CyberAmber else CyberGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Section: Cloud & LAN Endpoint Configuration
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Online / LAN Provider Configuration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Online Model Picker
                    Text("Target Cloud Model:", fontSize = 12.sp, color = CyberCyan, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("gemini-3.5-flash", "gemini-3.1-pro-preview", "gemini-3.1-flash-lite-preview").forEach { m ->
                            val isSel = selectedOnlineModel == m
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedOnlineModel = m },
                                label = { Text(if (m.contains("pro")) "Pro" else if (m.contains("lite")) "Lite" else "Flash", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = CyberGreen
                                )
                            )
                        }
                    }

                    // API Key
                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        label = { Text("Cloud API Key (Optional Override):") },
                        placeholder = { Text("AI Studio or custom key...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    // Custom Base URL
                    OutlinedTextField(
                        value = baseUrlText,
                        onValueChange = { baseUrlText = it },
                        label = { Text("Custom Endpoint / Local LAN LLM:") },
                        placeholder = { Text("e.g. http://192.168.1.100:11434 for Ollama") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            viewModel.saveCloudSettings(apiKeyText, baseUrlText, selectedOnlineModel)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Configuration", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Governance & Provider Limits Transparency (Rule 27 Compliance)
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Model Architecture & Tier Distinctions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }

                    Text(
                        text = "• Local Compute (GGUF / LiteRT): 100% free, private, on-device compute. No rate limits or external dependencies.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Free Hosting (Hugging Face / GitHub): Model weights hosted freely on public open repositories. Downloads occur directly over HTTP.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Cloud APIs: Free tier quotas are subject to provider limits (RPM/TPM). The app remains fully functional offline if cloud limits are reached.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Section: About & Developer Credits
        item {
            val context = LocalContext.current
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberGreen.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "GR AI — About Developer",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen
                    )
                    Text(
                        text = "Developed and Idea by : GM Ripon",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Private developer & defensive cybersecurity mobile workbench with offline GGUF model execution, custom team rules, and GitHub Release 1.5GB asset synchronization.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/8801911527072"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+8801911527072"))
                                    context.startActivity(dial)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+8801911527072"))
                                context.startActivity(dial)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyberCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+8801911527072", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
