package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cybersecurity.CybersecurityEngine
import com.example.domain.analyzer.NetworkUtils
import com.example.domain.analyzer.OfflineVulnerabilityScanner
import com.example.domain.analyzer.PortDictionary
import com.example.security.CommandSafetyEvaluation
import com.example.security.DangerLevel
import com.example.ui.components.CodeBlockView
import com.example.ui.components.SeverityChip
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CybersecurityScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Safe Commands", "Log Triage", "STRIDE Threat Model", "Code Scanner", "Network & Crypto", "Playbooks", "OWASP Audit")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = CyberGreen,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyberGreen
                )
            }
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        when (selectedTab) {
            0 -> SafeCommandGuardView(viewModel, onNavigateToChat)
            1 -> LogTriageView(viewModel, onNavigateToChat)
            2 -> StrideThreatModelView(viewModel, onNavigateToChat)
            3 -> CodeScannerSubView(viewModel, onNavigateToChat)
            4 -> ToolkitScreen()
            5 -> PlaybooksScreen()
            6 -> OwaspAuditSubView(viewModel, onNavigateToChat)
        }
    }
}

@Composable
fun SafeCommandGuardView(viewModel: MainViewModel, onNavigateToChat: () -> Unit) {
    val context = LocalContext.current
    var commandInput by remember { mutableStateOf("sudo iptables -F && rm -rf /var/log/app/*") }
    var evaluation by remember { mutableStateOf<CommandSafetyEvaluation?>(null) }
    var showDangerousConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Defensive Safe Command Guard",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Autonomous execution of arbitrary commands is prohibited. Inspect destructive flags, elevate privilege risks, and require explicit confirmation.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = commandInput,
                        onValueChange = {
                            commandInput = it
                            evaluation = null
                        },
                        label = { Text("Command to verify:") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            evaluation = CybersecurityEngine.inspectCommand(commandInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Analyze Command Safety", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        evaluation?.let { eval ->
            item {
                val bannerColor = when (eval.dangerLevel) {
                    DangerLevel.DESTRUCTIVE -> CyberRed
                    DangerLevel.HIGH_RISK -> Color(0xFFFF7043)
                    DangerLevel.CAUTION -> CyberAmber
                    DangerLevel.SAFE -> CyberGreen
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, bannerColor.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DANGER LEVEL: ${eval.dangerLevel}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = bannerColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = eval.explanation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (eval.requiresExplicitConfirmation) {
                                        showDangerousConfirmDialog = true
                                    } else {
                                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cb.setPrimaryClip(ClipData.newPlainText("cmd", eval.command))
                                        Toast.makeText(context, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = bannerColor, contentColor = Color.White),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Command", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.sendMessage("Explain line by line what this command does and how to run it safely:\n```bash\n${eval.command}\n```")
                                    onNavigateToChat()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = CyberCyan),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Explain Risk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDangerousConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDangerousConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CyberRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Destructive / High-Risk Command")
                }
            },
            text = {
                Text("This command contains potentially destructive parameters (e.g. system wipe, firewall drop, or root elevation). Do you explicitly confirm copying this to your clipboard?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cb.setPrimaryClip(ClipData.newPlainText("cmd", commandInput))
                        Toast.makeText(context, "Copied with explicit confirmation", Toast.LENGTH_SHORT).show()
                        showDangerousConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed)
                ) {
                    Text("I Understand, Copy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDangerousConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LogTriageView(viewModel: MainViewModel, onNavigateToChat: () -> Unit) {
    var rawLogs by remember {
        mutableStateOf(
            """
Sep 26 10:14:22 srv sshd[8412]: Failed password for invalid user admin from 198.51.100.24 port 54122 ssh2
Sep 26 10:14:24 srv sshd[8414]: Failed password for invalid user admin from 198.51.100.24 port 54124 ssh2
Sep 26 10:14:27 srv sshd[8418]: Failed password for root from 198.51.100.24 port 54128 ssh2
Sep 26 10:15:01 srv nginx[2910]: 198.51.100.24 - - [26/Sep/2026:10:15:01 +0000] "GET /phpmyadmin/scripts/setup.php HTTP/1.1" 404
Sep 26 10:15:03 srv nginx[2910]: 198.51.100.24 - - [26/Sep/2026:10:15:03 +0000] "POST /api/v1/auth HTTP/1.1" 401
            """.trimIndent()
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Incident Log Triage:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Paste syslog, auth.log, or Nginx access logs to identify attacker IPs and automated brute-force attacks.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = rawLogs,
            onValueChange = { rawLogs = it },
            modifier = Modifier.fillMaxWidth().height(220.dp),
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val req = CybersecurityEngine.analyzeLogSnippet(rawLogs)
                viewModel.sendMessage(req.prompt, isCodeAudit = true)
                onNavigateToChat()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) {
            Text("Run Incident Triage with Copilot", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StrideThreatModelView(viewModel: MainViewModel, onNavigateToChat: () -> Unit) {
    var systemDesc by remember {
        mutableStateOf("REST API service with JWT authentication, PostgreSQL database, and S3 bucket storage for user uploads.")
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("STRIDE Threat Modeling:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Describe your application architecture or service to map threats across Spoofing, Tampering, Repudiation, Information Disclosure, DoS, and Elevation of Privilege.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = systemDesc,
            onValueChange = { systemDesc = it },
            modifier = Modifier.fillMaxWidth().height(160.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val req = CybersecurityEngine.generateThreatModel(systemDesc)
                viewModel.sendMessage(req.prompt)
                onNavigateToChat()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) {
            Text("Generate STRIDE Threat Model", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CodeScannerSubView(viewModel: MainViewModel, onNavigateToChat: () -> Unit) {
    var code by remember { mutableStateOf(OfflineVulnerabilityScanner.SAMPLE_PYTHON_BACKEND) }
    var findings by remember { mutableStateOf(OfflineVulnerabilityScanner.scanCode(code)) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it
                    findings = OfflineVulnerabilityScanner.scanCode(it)
                },
                modifier = Modifier.fillMaxWidth().height(180.dp),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            )
        }

        item {
            Text("Offline AST Findings (${findings.size}):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        items(findings.size) { idx ->
            val f = findings[idx]
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SeverityChip(severity = f.severity)
                        Text(f.cweId, fontSize = 11.sp, color = CyberCyan, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(f.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(f.matchedSnippet, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            viewModel.sendMessage("Remediate ${f.title} (${f.cweId}) with secure code patch:\n```\n${f.matchedSnippet}\n```", isCodeAudit = true)
                            onNavigateToChat()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyberGreen),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Ask AI to Fix This Vulnerability", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PortMatrixSubView() {
    val entries = PortDictionary.entries
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(entries.size) { i ->
            val p = entries[i]
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${p.port} / ${p.protocol} (${p.service})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberGreen, fontFamily = FontFamily.Monospace)
                        SeverityChip(severity = p.riskLevel)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("⚡ Threat: ${p.attackVectors}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("🛡️ Hardening: ${p.hardeningTips}", fontSize = 11.sp, color = CyberCyan)
                }
            }
        }
    }
}

@Composable
fun OwaspAuditSubView(viewModel: MainViewModel, onNavigateToChat: () -> Unit) {
    var selectedCategory by remember { mutableStateOf("WEB") }
    val allItems = com.example.domain.analyzer.OwaspCatalog.initialItems
    val filtered = remember(selectedCategory) { allItems.filter { it.category == selectedCategory } }

    var itemStatuses by remember {
        mutableStateOf(filtered.associate { it.code to "PENDING" }.toMutableMap())
    }

    val passedCount = itemStatuses.values.count { it == "PASSED" }
    val totalCount = filtered.size.coerceAtLeast(1)
    val compliancePercent = ((passedCount.toFloat() / totalCount.toFloat()) * 100).toInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("OWASP Security Audit Matrix", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                        Text("$compliancePercent% Compliant", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { passedCount.toFloat() / totalCount.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = CyberGreen,
                        trackColor = DarkSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedCategory == "WEB",
                            onClick = { selectedCategory = "WEB" },
                            label = { Text("OWASP Web Top 10", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberGreen.copy(alpha = 0.2f),
                                selectedLabelColor = CyberGreen
                            )
                        )
                        FilterChip(
                            selected = selectedCategory == "MOBILE",
                            onClick = { selectedCategory = "MOBILE" },
                            label = { Text("OWASP Mobile Top 10", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberGreen.copy(alpha = 0.2f),
                                selectedLabelColor = CyberGreen
                            )
                        )
                    }
                }
            }
        }

        items(filtered.size) { i ->
            val item = filtered[i]
            val status = itemStatuses[item.code] ?: "PENDING"

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when (status) {
                        "PASSED" -> CyberGreen.copy(alpha = 0.5f)
                        "FLAGGED" -> CyberRed.copy(alpha = 0.5f)
                        else -> DarkCardBorder
                    }
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${item.code}: ${item.title}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (status) {
                                        "PASSED" -> CyberGreen.copy(alpha = 0.2f)
                                        "FLAGGED" -> CyberRed.copy(alpha = 0.2f)
                                        else -> DarkSurfaceVariant
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (status) {
                                    "PASSED" -> CyberGreen
                                    "FLAGGED" -> CyberRed
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🛡️ Defense: ${item.defenseStrategy}", fontSize = 11.sp, color = CyberCyan)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val next = if (status == "PASSED") "PENDING" else "PASSED"
                                itemStatuses = itemStatuses.toMutableMap().apply { put(item.code, next) }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status == "PASSED") CyberGreen else DarkSurfaceVariant,
                                contentColor = if (status == "PASSED") Color(0xFF00381B) else CyberGreen
                            ),
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (status == "PASSED") "✓ Passed" else "Mark Pass", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val next = if (status == "FLAGGED") "PENDING" else "FLAGGED"
                                itemStatuses = itemStatuses.toMutableMap().apply { put(item.code, next) }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status == "FLAGGED") CyberRed else DarkSurfaceVariant,
                                contentColor = if (status == "FLAGGED") Color.White else CyberRed
                            ),
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (status == "FLAGGED") "⚠️ Flagged" else "Flag Risk", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.sendMessage(
                                    "Provide a detailed defensive self-audit and implementation guide for ${item.code} (${item.title}):\n${item.description}\nRecommended defense:\n${item.defenseStrategy}",
                                    isCodeAudit = true
                                )
                                onNavigateToChat()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyberCyan),
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("AI Guide", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
