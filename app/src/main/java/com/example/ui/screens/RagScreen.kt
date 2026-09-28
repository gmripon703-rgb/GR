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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.rag.RagSearchResult
import com.example.storage.entity.RagDocumentEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun RagScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.ragDocuments.collectAsState()
    val isDriveConnected by viewModel.isDriveConnected.collectAsState()
    val driveAccountEmail by viewModel.driveAccountEmail.collectAsState()
    val isDriveSyncing by viewModel.isDriveSyncing.collectAsState()
    var driveSyncMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<RagSearchResult>>(emptyList()) }
    var showImportDialog by remember { mutableStateOf(false) }

    var docTitle by remember { mutableStateOf("") }
    var docType by remember { mutableStateOf("MARKDOWN") }
    var docContent by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Privacy Banner
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Local Searchable Knowledge Base (RAG)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Project files and docs are indexed in local SQLite on this device. Never transmitted to external cloud servers.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Search Input
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { q ->
                        searchQuery = q
                        if (q.isNotBlank()) {
                            scope.launch {
                                searchResults = viewModel.ragEngine.searchRelevantChunks(q, topK = 3)
                            }
                        } else {
                            searchResults = emptyList()
                        }
                    },
                    placeholder = { Text("Search local indexed chunks...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rag_search_field"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { showImportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Google Drive Cloud Backup Bar
        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDriveConnected) CyberGreen.copy(alpha = 0.4f) else DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDriveConnected) Icons.Default.CloudDone else Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = if (isDriveConnected) CyberGreen else CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isDriveConnected) "Google Drive: ${driveAccountEmail.ifBlank { "gmripon703@gmail.com" }}" else "Google Drive: Offload documents to Cloud",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = driveSyncMsg ?: if (isDriveConnected) "Keep phone storage free by backing up RAG index" else "Connect Google Account in Settings to sync RAG archive",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isDriveConnected) {
                        Button(
                            onClick = {
                                viewModel.syncRagToGoogleDrive { _, msg ->
                                    driveSyncMsg = msg
                                }
                            },
                            enabled = !isDriveSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen.copy(alpha = 0.2f), contentColor = CyberGreen),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(if (isDriveSyncing) "Syncing..." else "Sync to Drive", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Search Results (if searching)
        if (searchResults.isNotEmpty()) {
            item {
                Text(
                    text = "Matching Knowledge Chunks (${searchResults.size}):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
            }

            items(searchResults) { result ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Doc ID #${result.docId}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyberCyan
                            )
                            Text(
                                text = "Relevance: ${"%.1f".format(result.score)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.content,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Documents Header
        item {
            Text(
                text = "Indexed Local Documents (${documents.size}):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (documents.isEmpty()) {
            item {
                Text(
                    text = "No documents imported yet. Tap 'Import' to add architecture specs, internal guidelines, or code docs for offline retrieval.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(documents, key = { it.id }) { doc ->
                DocumentCard(
                    document = doc,
                    onDelete = { viewModel.deleteDocument(doc.id) }
                )
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Local Document", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Document Title (e.g. auth-architecture.md)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = docType,
                        onValueChange = { docType = it },
                        label = { Text("Type (MARKDOWN, TEXT, KOTLIN, SPEC)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = docContent,
                        onValueChange = { docContent = it },
                        label = { Text("Content / Notes") },
                        placeholder = { Text("Paste document text, architecture notes, or code documentation...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (docTitle.isNotBlank() && docContent.isNotBlank()) {
                            viewModel.importDocument(docTitle, docType, docContent)
                            docTitle = ""
                            docContent = ""
                            showImportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                    enabled = docTitle.isNotBlank() && docContent.isNotBlank()
                ) {
                    Text("Index & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DocumentCard(
    document: RagDocumentEntity,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = document.fileType,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${document.chunkCount} Chunks",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = document.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = document.rawContent.take(90).replace("\n", " ") + "...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = CyberRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
