package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.OwaspItemEntity
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.DevSecViewModel

@Composable
fun OwaspScreen(
    viewModel: DevSecViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.owaspItems.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val categories = listOf("WEB", "MOBILE")

    val activeCategory = categories[selectedTabIndex]
    val filteredItems = items.filter { it.category == activeCategory }

    val passedCount = filteredItems.count { it.status == "PASSED" }
    val flaggedCount = filteredItems.count { it.status == "FLAGGED" }
    val inProgressCount = filteredItems.count { it.status == "IN_PROGRESS" }
    val totalCount = filteredItems.size.coerceAtLeast(1)
    val progressFraction = passedCount.toFloat() / totalCount.toFloat()

    var editingNotesItem by remember { mutableStateOf<OwaspItemEntity?>(null) }
    var notesText by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = DarkSurface,
            contentColor = CyberGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = CyberGreen
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("OWASP Web Top 10", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                modifier = Modifier.testTag("tab_owasp_web")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("OWASP Mobile Top 10", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                modifier = Modifier.testTag("tab_owasp_mobile")
            )
        }

        // Progress Overview Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurfaceVariant,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Security Compliance Score",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$passedCount of $totalCount checks verified as secure (${(progressFraction * 100).toInt()}%)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Audit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CyberGreen,
                    trackColor = Color(0xFF090D13)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusBadge(label = "Passed", count = passedCount, color = CyberGreen)
                    StatusBadge(label = "Flagged", count = flaggedCount, color = CyberRed)
                    StatusBadge(label = "In Progress", count = inProgressCount, color = CyberAmber)
                    StatusBadge(
                        label = "Pending",
                        count = totalCount - passedCount - flaggedCount - inProgressCount,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Checklist Items
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredItems, key = { it.code }) { item ->
                OwaspItemCard(
                    item = item,
                    onStatusChange = { newStatus ->
                        viewModel.updateOwaspItem(item.code, newStatus, item.notes)
                    },
                    onEditNotes = {
                        editingNotesItem = item
                        notesText = item.notes
                    }
                )
            }
        }
    }

    // Notes Editor Dialog
    editingNotesItem?.let { currentItem ->
        AlertDialog(
            onDismissRequest = { editingNotesItem = null },
            title = {
                Text(
                    text = "Audit Notes: ${currentItem.code}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = currentItem.title,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        placeholder = { Text("Record testing evidence, tickets, or mitigation notes...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateOwaspItem(currentItem.code, currentItem.status, notesText)
                        editingNotesItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B))
                ) {
                    Text("Save Notes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNotesItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Compliance Audit?") },
            text = { Text("All statuses and notes will be cleared to default state.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetOwasp()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed)
                ) {
                    Text("Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusBadge(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: $count",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun OwaspItemCard(
    item: OwaspItemEntity,
    onStatusChange: (String) -> Unit,
    onEditNotes: () -> Unit
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberCyan.copy(alpha = 0.15f))
                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.code,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan
                    )
                }

                IconButton(
                    onClick = onEditNotes,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Edit notes",
                        tint = if (item.notes.isNotBlank()) CyberAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Defense strategy box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F151F))
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Defense Strategy",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.defenseStrategy,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 15.sp
                )
            }

            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📝 Notes: ${item.notes}",
                    fontSize = 11.sp,
                    color = CyberAmber,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusSelectChip(
                    label = "Pass",
                    selected = item.status == "PASSED",
                    activeColor = CyberGreen,
                    icon = Icons.Default.Check,
                    onClick = { onStatusChange(if (item.status == "PASSED") "NOT_STARTED" else "PASSED") }
                )
                StatusSelectChip(
                    label = "Flag",
                    selected = item.status == "FLAGGED",
                    activeColor = CyberRed,
                    icon = Icons.Default.Close,
                    onClick = { onStatusChange(if (item.status == "FLAGGED") "NOT_STARTED" else "FLAGGED") }
                )
                StatusSelectChip(
                    label = "Review",
                    selected = item.status == "IN_PROGRESS",
                    activeColor = CyberAmber,
                    icon = Icons.Default.HourglassTop,
                    onClick = { onStatusChange(if (item.status == "IN_PROGRESS") "NOT_STARTED" else "IN_PROGRESS") }
                )
            }
        }
    }
}

@Composable
fun StatusSelectChip(
    label: String,
    selected: Boolean,
    activeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = DarkSurfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = activeColor.copy(alpha = 0.2f),
            selectedLabelColor = activeColor,
            selectedLeadingIconColor = activeColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = if (selected) activeColor else DarkCardBorder,
            selectedBorderColor = activeColor,
            enabled = true,
            selected = selected
        )
    )
}
