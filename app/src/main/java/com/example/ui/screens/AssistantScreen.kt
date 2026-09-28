package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChatMessageEntity
import com.example.ui.components.CodeBlockView
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.DevSecViewModel

@Composable
fun AssistantScreen(
    viewModel: DevSecViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val promptSuggestions = listOf(
        "Audit this code for CWE/OWASP vulnerabilities",
        "Generate a hardened Linux UFW firewall ruleset",
        "How to defend against JWT signature exclusion & replay",
        "Create a hardened, non-root Dockerfile for Java",
        "Explain self-penetration testing scope and rules of engagement",
        "Analyze suspicious Linux auth.log failed attempts"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Status & Model Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CyberGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedModel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = CyberGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Private & Local DB",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Encrypted / Private",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (messages.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Chat Message List
        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                // Empty state with quick prompts
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(CyberGreen.copy(alpha = 0.15f))
                            .border(1.dp, CyberGreen.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CyberGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "DevSec AI Copilot",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Private IT security & coding defense engine. Ask for vulnerability analysis, secure patches, or penetration testing guidance.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Suggested Prompts:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        promptSuggestions.take(4).forEach { prompt ->
                            Surface(
                                onClick = {
                                    inputText = prompt
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = DarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("suggestion_${prompt.take(15)}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = CyberGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = prompt,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(messages) { message ->
                        ChatMessageItem(message = message)
                    }

                    if (isGenerating) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = CyberGreen
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Analyzing security posture & generating solution...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick prompts horizontal row if there are messages
        if (messages.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                promptSuggestions.forEach { prompt ->
                    FilterChip(
                        selected = false,
                        onClick = { inputText = prompt },
                        label = { Text(prompt, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkSurfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = DarkCardBorder,
                            enabled = true,
                            selected = false
                        )
                    )
                }
            }
        }

        // Bottom Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "Ask security question, audit snippet, or bash help...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberGreen,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val text = inputText
                        if (text.isNotBlank()) {
                            inputText = ""
                            viewModel.sendMessage(text)
                        }
                    },
                    enabled = inputText.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank() && !isGenerating) CyberGreen else DarkSurfaceVariant)
                        .testTag("send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isGenerating) Color(0xFF00381B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessageEntity) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CyberGreen.copy(alpha = 0.2f))
                    .border(1.dp, CyberGreen.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI",
                    tint = CyberGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) Color(0xFF0D2825) else DarkSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) CyberGreen.copy(alpha = 0.3f) else DarkCardBorder
            ),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.92f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.isCodeAudit) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CODE AUDIT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                        }
                    }
                }

                // Render content with code block support
                FormattedMessageContent(content = message.content)
            }
        }
    }
}

@Composable
fun FormattedMessageContent(content: String) {
    val parts = remember(content) {
        splitMarkdownCodeBlocks(content)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parts.forEach { part ->
            if (part.isCode) {
                CodeBlockView(code = part.text, language = part.language)
            } else {
                Text(
                    text = part.text,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

data class MessageChunk(val text: String, val isCode: Boolean, val language: String = "CODE")

fun splitMarkdownCodeBlocks(markdown: String): List<MessageChunk> {
    val chunks = mutableListOf<MessageChunk>()
    val lines = markdown.lines()

    var inCode = false
    var currentLanguage = "CODE"
    val buffer = StringBuilder()

    for (line in lines) {
        if (line.trim().startsWith("```")) {
            if (inCode) {
                // Ending code block
                chunks.add(MessageChunk(buffer.toString().trimEnd(), true, currentLanguage))
                buffer.clear()
                inCode = false
            } else {
                // Starting code block
                if (buffer.isNotEmpty()) {
                    chunks.add(MessageChunk(buffer.toString().trimEnd(), false))
                    buffer.clear()
                }
                inCode = true
                currentLanguage = line.trim().removePrefix("```").trim().ifEmpty { "CODE" }
            }
        } else {
            if (buffer.isNotEmpty()) buffer.append("\n")
            buffer.append(line)
        }
    }

    if (buffer.isNotEmpty()) {
        chunks.add(MessageChunk(buffer.toString().trimEnd(), inCode, currentLanguage))
    }

    return chunks
}
