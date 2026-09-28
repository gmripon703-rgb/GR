package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.storage.entity.TeamRuleEntity
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.MainViewModel

@Composable
fun TeamRulesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val rules by viewModel.teamRules.collectAsState()
    val syncStatus by viewModel.ruleSyncStatus.collectAsState()

    var showAddRuleDialog by remember { mutableStateOf(false) }
    var showSyncUrlDialog by remember { mutableStateOf(false) }
    var showImportJsonDialog by remember { mutableStateOf(false) }

    var syncUrlInput by remember {
        mutableStateOf("https://raw.githubusercontent.com/OWNER/REPOSITORY/main/team-rules.json")
    }
    var importJsonInput by remember { mutableStateOf("") }

    var ruleTitle by remember { mutableStateOf("") }
    var ruleCategory by remember { mutableStateOf("CODING_STANDARDS") }
    var ruleContent by remember { mutableStateOf("") }
    var rulePriority by remember { mutableIntStateOf(2) }
    var ruleIsStrict by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hierarchy Banner
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Team Rules & System Governance",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rules are prepended hierarchically to all AI prompts before conversation context:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Platform/Security Guardrails > 2. Team Rules > 3. Project Rules > 4. User Prefs > 5. Chat History",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan
                    )
                }
            }
        }

        // Cloud & GitHub Team Sync Action Card
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "GitHub & Team Rule Synchronization",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sync rules across your developer team via GitHub Release, Raw file, or Google Drive link.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showSyncUrlDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyberCyan),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync from URL", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val json = viewModel.exportTeamRulesJson()
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("team-rules.json", json))
                                    Toast.makeText(context, "Copied team rules JSON to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyberGreen),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export JSON", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showImportJsonDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import JSON", fontSize = 11.sp)
                        }
                    }

                    syncStatus?.let { status ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = status,
                            fontSize = 10.sp,
                            color = CyberCyan
                        )
                    }
                }
            }
        }

        // Add Rule Button Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configured Governance Rules (${rules.size}):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = { showAddRuleDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Rule", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(rules, key = { it.id }) { rule ->
            RuleItemCard(
                rule = rule,
                onToggle = { viewModel.toggleTeamRule(rule) },
                onDelete = { viewModel.deleteTeamRule(rule.id) }
            )
        }
    }

    // Sync from URL Dialog
    if (showSyncUrlDialog) {
        AlertDialog(
            onDismissRequest = { showSyncUrlDialog = false },
            title = { Text("Sync Rules from GitHub / Cloud", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter direct HTTPS URL for team rules JSON (e.g. GitHub raw URL, Release asset, or Google Drive link):",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = syncUrlInput,
                        onValueChange = { syncUrlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.syncTeamRulesFromUrl(syncUrlInput) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) showSyncUrlDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B))
                ) {
                    Text("Sync Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSyncUrlDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import JSON Dialog
    if (showImportJsonDialog) {
        AlertDialog(
            onDismissRequest = { showImportJsonDialog = false },
            title = { Text("Import Team Rules JSON", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paste team rules JSON schema exported from another device or repository:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = { Text("{\"version\": 1, \"rules\": [...]}", fontSize = 11.sp) },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importTeamRulesFromJson(importJsonInput) { count ->
                            Toast.makeText(context, "Imported $count rules!", Toast.LENGTH_SHORT).show()
                            if (count > 0) showImportJsonDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                    enabled = importJsonInput.isNotBlank()
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportJsonDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddRuleDialog) {
        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            title = { Text("Add Team Governance Rule", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ruleTitle,
                        onValueChange = { ruleTitle = it },
                        label = { Text("Rule Title (e.g. Disallow Dynamic Exec)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ruleCategory,
                        onValueChange = { ruleCategory = it },
                        label = { Text("Category (SECURITY, CODING_STANDARDS, PRIVACY)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ruleContent,
                        onValueChange = { ruleContent = it },
                        label = { Text("Rule Specification / Instruction") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ruleTitle.isNotBlank() && ruleContent.isNotBlank()) {
                            viewModel.addTeamRule(ruleTitle, ruleCategory, rulePriority, ruleContent, ruleIsStrict)
                            ruleTitle = ""
                            ruleContent = ""
                            showAddRuleDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                    enabled = ruleTitle.isNotBlank() && ruleContent.isNotBlank()
                ) {
                    Text("Save Rule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RuleItemCard(
    rule: TeamRuleEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (rule.isEnabled) DarkCardBorder else DarkCardBorder.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (rule.priority == 1) CyberRed.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "P${rule.priority} • ${rule.category}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (rule.priority == 1) CyberRed else CyberCyan
                        )
                    }

                    if (rule.isStrict) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[STRICT]",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberAmber
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = rule.isEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen, checkedTrackColor = Color(0xFF00532B))
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = rule.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (rule.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = rule.ruleContent,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}
