package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.repository.DevSecRepository
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.DevSecViewModel

@Composable
fun SettingsDialog(
    viewModel: DevSecViewModel,
    onDismiss: () -> Unit
) {
    val currentModel by viewModel.selectedModel.collectAsState()
    val currentKey by viewModel.customApiKey.collectAsState()
    val currentUrl by viewModel.customBaseUrl.collectAsState()

    var selectedModel by remember { mutableStateOf(currentModel) }
    var apiKeyText by remember { mutableStateOf(currentKey) }
    var baseUrlText by remember { mutableStateOf(currentUrl) }
    var isKeyVisible by remember { mutableStateOf(false) }

    val models = listOf(
        Triple(DevSecRepository.DEFAULT_MODEL, "Gemini 3.5 Flash", "Default fast model for code audit, triage, and Q&A"),
        Triple(DevSecRepository.MODEL_PRO, "Gemini 3.1 Pro Preview", "Deep reasoning for complex vulnerability auditing"),
        Triple(DevSecRepository.MODEL_LITE, "Gemini 3.1 Flash Lite", "Ultra-fast lightweight assistant for quick queries")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = CyberGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DevSec AI Configuration",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Privacy statement
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyberGreen.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 100% Private & Standalone: No custom web app server exists. Your audit records and chats are stored locally in Room SQLite on this device.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp),
                        lineHeight = 15.sp
                    )
                }

                // AI Model Selection
                Text(
                    text = "Select Active AI Model:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    models.forEach { (modelId, displayName, desc) ->
                        val isSelected = selectedModel == modelId
                        Surface(
                            onClick = { selectedModel = modelId },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CyberGreen.copy(alpha = 0.15f) else DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CyberGreen else DarkCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("model_select_$modelId")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CyberGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = CyberGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // API Key Override
                Text(
                    text = "Gemini API Key (Optional Override):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    placeholder = { Text("AI Studio or custom key...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_field"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle visibility",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                // Custom Base URL / LAN Endpoint
                Text(
                    text = "Custom Endpoint / Local LAN LLM (Optional):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                OutlinedTextField(
                    value = baseUrlText,
                    onValueChange = { baseUrlText = it },
                    placeholder = { Text("e.g. http://10.0.2.2:11434 for Ollama") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("base_url_field"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.saveSettings(selectedModel, apiKeyText, baseUrlText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                modifier = Modifier.testTag("save_settings_btn")
            ) {
                Text("Save Configuration", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
