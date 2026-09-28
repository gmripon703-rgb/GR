package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.developer.DeveloperAction
import com.example.developer.DeveloperToolsEngine
import com.example.ui.components.CodeBlockView
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DeveloperScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Code Copilot", "Crash & Stack Trace", "Regex & Schema", "Git & PR Generator")

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
            0 -> CodeCopilotSubView(viewModel, onNavigateToChat)
            1 -> StackTraceSubView(viewModel, onNavigateToChat)
            2 -> RegexSchemaSubView(viewModel, onNavigateToChat)
            3 -> GitPrSubView(viewModel, onNavigateToChat)
        }
    }
}

@Composable
fun CodeCopilotSubView(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit
) {
    var selectedLanguage by remember { mutableStateOf("Kotlin") }
    var selectedAction by remember { mutableStateOf(DeveloperAction.EXPLAIN) }
    var codeInput by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }

    val languages = DeveloperToolsEngine.supportedLanguages

    val sampleSnippets = mapOf(
        "Kotlin" to """
suspend fun fetchUserData(userId: String) {
    // Potential issue: Missing error boundary & blocking dispatch
    val user = api.getUser(userId)
    cache.save(user)
}
        """.trimIndent(),
        "Python" to """
def execute_query(conn, user_input):
    # SQLi Vulnerability
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM records WHERE name = '" + user_input + "'")
    return cursor.fetchall()
        """.trimIndent(),
        "Docker" to """
FROM ubuntu:latest
# Issue: running as root
RUN apt-get update && apt-get install -y openjdk-17-jre
COPY app.jar /app.jar
CMD ["java", "-jar", "/app.jar"]
        """.trimIndent()
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("1. Target Language & Ecosystem", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        languages.forEach { lang ->
                            FilterChip(
                                selected = selectedLanguage == lang,
                                onClick = {
                                    selectedLanguage = lang
                                    sampleSnippets[lang]?.let { codeInput = it }
                                },
                                label = { Text(lang, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = CyberGreen
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("2. Developer Action", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeveloperAction.values().forEach { action ->
                            FilterChip(
                                selected = selectedAction == action,
                                onClick = { selectedAction = action },
                                label = { Text(action.label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = CyberCyan
                                )
                            )
                        }
                    }
                }
            }
        }

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
                        Text("3. Source Code / Specification", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                        sampleSnippets[selectedLanguage]?.let { sample ->
                            Button(
                                onClick = { codeInput = sample },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyberCyan),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("Load Sample", fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        placeholder = { Text("Paste $selectedLanguage code or feature requirement here...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("dev_code_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberGreen,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = Color(0xFF090D13),
                            unfocusedContainerColor = Color(0xFF090D13)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = additionalNotes,
                        onValueChange = { additionalNotes = it },
                        placeholder = { Text("Specific instructions (e.g. use Room DB, avoid third party libraries)...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.runDeveloperAction(selectedAction, selectedLanguage, codeInput, additionalNotes)
                            onNavigateToChat()
                        },
                        enabled = codeInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("dev_execute_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Execute ${selectedAction.label}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun StackTraceSubView(viewModel: MainViewModel, onNavigateToChat: () -> Unit) {
    var rawStackTrace by remember {
        mutableStateOf(
            """java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String com.example.model.User.getName()' on a null object reference
	at com.example.ui.MainActivity.updateHeader(MainActivity.kt:142)
	at com.example.ui.MainActivity.onDataLoaded(MainActivity.kt:89)
	at kotlinx.coroutines.flow.FlowKt__CollectKt.collect(Collect.kt:12)"""
        )
    }

    var analysisResult by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Crash Log & Exception Analyzer", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Paste Android logcat or server crash trace for instant deconstruction and fix recommendation.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = rawStackTrace,
                        onValueChange = { rawStackTrace = it },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF090D13),
                            unfocusedContainerColor = Color(0xFF090D13)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFE2E8F0))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                analysisResult = DeveloperToolsEngine.parseStackTrace(rawStackTrace)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00381B)),
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Fast Triage", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.sendMessage(
                                    "Analyze and fix this crash stack trace with root cause, reproducible unit test, and corrected code:\n```\n$rawStackTrace\n```",
                                    taskType = com.example.ai.TaskType.CODING
                                )
                                onNavigateToChat()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Ask AI to Fix", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        analysisResult?.let { res ->
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(res, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun RegexSchemaSubView(viewModel: MainViewModel, onNavigateToChat: () -> Unit) {
    var regexPattern by remember { mutableStateOf("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$") }
    var testSample by remember { mutableStateOf("security.team@internal-dev.company.corp") }

    val regexMatch = remember(regexPattern, testSample) {
        try {
            val r = Regex(regexPattern)
            r.matches(testSample)
        } catch (_: Exception) {
            null
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Interactive Regex Tester & Builder", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Pattern (Regex):", fontSize = 11.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = regexPattern,
                        onValueChange = { regexPattern = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFFE2E8F0))
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Test Input String:", fontSize = 11.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = testSample,
                        onValueChange = { testSample = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFFE2E8F0))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (regexMatch) {
                            true -> CyberGreen.copy(alpha = 0.2f)
                            false -> CyberRed.copy(alpha = 0.2f)
                            null -> CyberAmber.copy(alpha = 0.2f)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when (regexMatch) {
                                true -> "✓ MATCH SUCCESS: Input satisfies regex."
                                false -> "✗ NO MATCH: Input does not conform to regex."
                                null -> "⚠️ SYNTAX ERROR: Invalid regular expression pattern."
                            },
                            modifier = Modifier.padding(10.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (regexMatch) {
                                true -> CyberGreen
                                false -> CyberRed
                                null -> CyberAmber
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.sendMessage(
                                "Optimize, explain, and write unit test boundary cases for this regex:\nPattern: `$regexPattern`\nTest target: `$testSample`",
                                taskType = com.example.ai.TaskType.CODING
                            )
                            onNavigateToChat()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00381B)),
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Explain / Optimize with AI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GitPrSubView(viewModel: MainViewModel, onNavigateToChat: () -> Unit) {
    val context = LocalContext.current
    var commitType by remember { mutableStateOf("feat") }
    var scope by remember { mutableStateOf("security") }
    var changeDesc by remember { mutableStateOf("Implement AES-256-GCM local storage and enforce SHA-256 model verification") }

    val commitTypes = listOf("feat", "fix", "refactor", "sec", "perf", "docs", "chore")
    val conventionalCommit = "$commitType($scope): $changeDesc"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Conventional Commit & PR Generator", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Commit Type:", fontSize = 11.sp, color = CyberCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        commitTypes.forEach { type ->
                            FilterChip(
                                selected = commitType == type,
                                onClick = { commitType = type },
                                label = { Text(type, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = CyberGreen
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Scope (module/package):", fontSize = 11.sp, color = CyberCyan)
                    OutlinedTextField(
                        value = scope,
                        onValueChange = { scope = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Change Summary:", fontSize = 11.sp, color = CyberCyan)
                    OutlinedTextField(
                        value = changeDesc,
                        onValueChange = { changeDesc = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Generated Conventional Commit:", fontSize = 11.sp, color = CyberGreen, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF090D13),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(conventionalCommit, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = CyberGreen)
                            Button(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("commit", conventionalCommit))
                                    Toast.makeText(context, "Commit copied", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyberGreen),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Copy", fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.sendMessage(
                                "Generate a complete GitHub Pull Request description with Motivation, Changes, Testing Checklist, and Security Considerations for this change:\n`$conventionalCommit`",
                                taskType = com.example.ai.TaskType.CODING
                            )
                            onNavigateToChat()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00381B)),
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Generate Full PR Description with AI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
