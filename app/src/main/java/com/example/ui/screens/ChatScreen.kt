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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.storage.entity.ChatMessageEntity
import com.example.ui.components.CodeBlockView
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingText by viewModel.currentStreamingText.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val providerMode by viewModel.providerMode.collectAsState()
    val isListening by viewModel.voiceAssistant.isListening.collectAsState()
    val voiceText by viewModel.voiceAssistant.recognizedText.collectAsState()
    val attachedImage by viewModel.attachedVisualAsset.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.attachImageUri(uri)
            }
        }
    )

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(voiceText) {
        if (voiceText.isNotBlank()) {
            inputText = voiceText
        }
    }

    LaunchedEffect(messages.size, streamingText.length) {
        if (messages.isNotEmpty() || streamingText.isNotEmpty()) {
            val targetIdx = (messages.size + if (streamingText.isNotEmpty()) 1 else 0) - 1
            if (targetIdx >= 0) {
                listState.animateScrollToItem(targetIdx)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Status & Provider Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) CyberGreen else Color(0xFFFFB300))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) "ONLINE" else "OFFLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isOnline) CyberGreen else Color(0xFFFFB300)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = providerMode,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyberCyan
                        )
                    }
                }

                if (messages.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Messages List
        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty() && streamingText.isEmpty()) {
                EmptyChatWelcome(onSelectPrompt = { inputText = it })
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubbleItem(
                            message = msg,
                            onSpeak = { viewModel.voiceAssistant.speak(msg.content) }
                        )
                    }

                    if (streamingText.isNotEmpty()) {
                        item {
                            ChatStreamingBubble(
                                text = streamingText,
                                onStop = { viewModel.stopGeneration() }
                            )
                        }
                    }
                }
            }
        }

        // Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (isGenerating) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { viewModel.stopGeneration() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRed.copy(alpha = 0.2f), contentColor = CyberRed),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop Generating", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Attached Visual Asset Preview
                attachedImage?.let { img ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = img.imageType.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                    Text(
                                        text = "Tap to autofill suggested prompt",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { inputText = img.suggestedPrompt },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyberCyan.copy(alpha = 0.15f),
                                        contentColor = CyberCyan
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Use Prompt", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { viewModel.clearAttachedImage() },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove attached image",
                                        tint = CyberRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vision / Screenshot Picker Button (Zero permission)
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (attachedImage != null) CyberCyan.copy(alpha = 0.25f) else DarkSurfaceVariant)
                            .testTag("attach_image_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach screenshot or diagram",
                            tint = if (attachedImage != null) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            if (isListening) viewModel.voiceAssistant.stopListening() else viewModel.voiceAssistant.startListening()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isListening) CyberRed.copy(alpha = 0.2f) else DarkSurfaceVariant)
                            .testTag("voice_input_btn")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice",
                            tint = if (isListening) CyberRed else CyberGreen,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Ask code, architecture, or cybersecurity...",
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

                    Spacer(modifier = Modifier.width(6.dp))

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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isGenerating) CyberGreen else DarkSurfaceVariant)
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isGenerating) Color(0xFF00381B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatWelcome(onSelectPrompt: (String) -> Unit) {
    val prompts = listOf(
        "Explain memory safety in Kotlin coroutines vs threads",
        "Refactor Android Retrofit call with clean Result wrapper",
        "Generate a defensive Dockerfile for Java with non-root UID",
        "Analyze SQL injection risk in dynamic WHERE clause"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(CyberGreen.copy(alpha = 0.15f))
                .border(1.dp, CyberGreen.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = CyberGreen,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Private Developer AI Platform",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "100% Offline-First with optional cloud. Local models, RAG documents, and team rules execute without internet access.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 17.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            prompts.forEach { prompt ->
                Surface(
                    onClick = { onSelectPrompt(prompt) },
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = prompt,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(message: ChatMessageEntity, onSpeak: () -> Unit) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(CyberGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = CyberGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = if (isUser) Color(0xFF0F2B26) else DarkSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) CyberGreen.copy(alpha = 0.3f) else DarkCardBorder
            ),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.92f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${message.providerType} • ${message.modelUsed}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyberCyan
                        )

                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Read out loud",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                MarkdownContentRenderer(content = message.content)
            }
        }
    }
}

@Composable
fun ChatStreamingBubble(text: String, onStop: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(CyberGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = CyberGreen
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DarkSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberGreen.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GENERATING STREAM...",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyberGreen
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                MarkdownContentRenderer(content = text)
            }
        }
    }
}

@Composable
fun MarkdownContentRenderer(content: String) {
    val chunks = remember(content) { parseMarkdownChunks(content) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        chunks.forEach { chunk ->
            if (chunk.isCode) {
                CodeBlockView(code = chunk.text, language = chunk.language)
            } else {
                Text(
                    text = chunk.text,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

data class ParsedChunk(val text: String, val isCode: Boolean, val language: String = "CODE")

fun parseMarkdownChunks(text: String): List<ParsedChunk> {
    val list = mutableListOf<ParsedChunk>()
    val lines = text.lines()
    var inCode = false
    var codeLang = "CODE"
    val buf = StringBuilder()

    for (line in lines) {
        if (line.trim().startsWith("```")) {
            if (inCode) {
                list.add(ParsedChunk(buf.toString().trimEnd(), true, codeLang))
                buf.clear()
                inCode = false
            } else {
                if (buf.isNotEmpty()) {
                    list.add(ParsedChunk(buf.toString().trimEnd(), false))
                    buf.clear()
                }
                inCode = true
                codeLang = line.trim().removePrefix("```").trim().ifEmpty { "CODE" }
            }
        } else {
            if (buf.isNotEmpty()) buf.append("\n")
            buf.append(line)
        }
    }
    if (buf.isNotEmpty()) {
        list.add(ParsedChunk(buf.toString().trimEnd(), inCode, codeLang))
    }
    return list
}
