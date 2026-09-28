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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.analyzer.NetworkUtils
import com.example.domain.analyzer.PortDictionary
import com.example.domain.analyzer.PortSecurityEntry
import com.example.domain.analyzer.SubnetCalculationResult
import com.example.ui.components.CodeBlockView
import com.example.ui.components.SeverityChip
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun ToolkitScreen(
    modifier: Modifier = Modifier
) {
    var selectedToolIndex by remember { mutableIntStateOf(0) }
    val toolTabs = listOf("CIDR Subnet", "Hash & Crypto", "Port Matrix", "HTTP Headers")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedToolIndex,
            containerColor = DarkSurface,
            contentColor = CyberGreen,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedToolIndex]),
                    color = CyberGreen
                )
            }
        ) {
            toolTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedToolIndex == index,
                    onClick = { selectedToolIndex = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("tool_tab_$index")
                )
            }
        }

        when (selectedToolIndex) {
            0 -> SubnetCalculatorView()
            1 -> CryptoHashingView()
            2 -> PortMatrixView()
            3 -> SecurityHeadersView()
        }
    }
}

@Composable
fun SubnetCalculatorView() {
    var cidrInput by remember { mutableStateOf("192.168.1.50/24") }
    var result by remember { mutableStateOf<SubnetCalculationResult?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

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
                        text = "IPv4 CIDR Subnet Calculator",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Calculate network boundaries, broadcast addresses, and usable host ranges for firewall rule isolation.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = cidrInput,
                            onValueChange = { cidrInput = it },
                            placeholder = { Text("e.g. 10.0.0.1/16") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cidr_input"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val res = NetworkUtils.calculateSubnet(cidrInput)
                                if (res.isSuccess) {
                                    result = res.getOrNull()
                                    errorMsg = null
                                } else {
                                    errorMsg = res.exceptionOrNull()?.message ?: "Invalid CIDR"
                                    result = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF00381B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("calc_subnet_btn")
                        ) {
                            Text("Compute", fontWeight = FontWeight.Bold)
                        }
                    }

                    errorMsg?.let {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "⚠️ $it", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        }

        result?.let { res ->
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Calculated Subnet Plan (${res.inputCidr})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )

                        SubnetRow("Network Address", res.networkAddress)
                        SubnetRow("Broadcast Address", res.broadcastAddress)
                        SubnetRow("First Usable Host", res.firstUsableHost)
                        SubnetRow("Last Usable Host", res.lastUsableHost)
                        SubnetRow("Netmask", res.netmask)
                        SubnetRow("Wildcard Mask", res.wildcardMask)
                        SubnetRow("Total IP Addresses", res.totalHosts.toString())
                        SubnetRow("Usable Host IPs", res.usableHosts.toString())
                        SubnetRow("IP Classification", res.ipClass)
                    }
                }
            }
        }
    }
}

@Composable
fun SubnetRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun CryptoHashingView() {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("AdminPassword123!") }

    val sha256 = remember(inputText) { NetworkUtils.computeHash(inputText, "SHA-256") }
    val sha512 = remember(inputText) { NetworkUtils.computeHash(inputText, "SHA-512") }
    val md5 = remember(inputText) { NetworkUtils.computeHash(inputText, "MD5") }
    val base64 = remember(inputText) { NetworkUtils.encodeBase64(inputText) }
    val hex = remember(inputText) { NetworkUtils.encodeHex(inputText) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Plaintext Input:") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            Text(
                text = "Cryptographic Hashes & Encodings (Computed On-Device):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item { HashResultCard("SHA-256", sha256, context) }
        item { HashResultCard("Base64 Encoded", base64, context) }
        item { HashResultCard("Hexadecimal (Byte Array)", hex, context) }
        item { HashResultCard("MD5 (Legacy / Non-Secure)", md5, context, isWarning = true) }
        item { HashResultCard("SHA-512", sha512, context) }
    }
}

@Composable
fun HashResultCard(title: String, value: String, context: Context, isWarning: Boolean = false) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isWarning) CyberAmber.copy(alpha = 0.5f) else DarkCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isWarning) CyberAmber else CyberCyan
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(title, value))
                    Toast.makeText(context, "$title copied", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PortMatrixView() {
    var searchQuery by remember { mutableStateOf("") }
    val allPorts = PortDictionary.entries
    val filteredPorts = allPorts.filter {
        it.port.toString().contains(searchQuery) ||
                it.service.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by port number, service (SSH, RDP, SMB)...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredPorts) { entry ->
                PortCard(entry = entry)
            }
        }
    }
}

@Composable
fun PortCard(entry: PortSecurityEntry) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${entry.port} / ${entry.protocol}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CyberGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.service,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                SeverityChip(severity = entry.riskLevel)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "⚡ Attack Vectors: ${entry.attackVectors}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🛡️ Hardening: ${entry.hardeningTips}",
                fontSize = 11.sp,
                color = CyberCyan,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun SecurityHeadersView() {
    var hsts by remember { mutableStateOf(true) }
    var xFrame by remember { mutableStateOf(true) }
    var nosniff by remember { mutableStateOf(true) }
    var strictCsp by remember { mutableStateOf(true) }

    val headers = remember(hsts, xFrame, nosniff, strictCsp) {
        NetworkUtils.generateSecurityHeaders(hsts, xFrame, nosniff, strictCsp)
    }

    val nginxConfig = buildString {
        appendLine("# Nginx Security Headers Configuration")
        headers.forEach { (key, value) ->
            appendLine("add_header $key \"$value\" always;")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "HTTP Security Header Generator",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Generate hardened web response headers to defend against clickjacking, MIME-type sniffing, and cross-site attacks.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    HeaderToggleRow("Strict-Transport-Security (HSTS)", hsts) { hsts = it }
                    HeaderToggleRow("X-Frame-Options (DENY Clickjacking)", xFrame) { xFrame = it }
                    HeaderToggleRow("X-Content-Type-Options (nosniff)", nosniff) { nosniff = it }
                    HeaderToggleRow("Content-Security-Policy (Strict CSP)", strictCsp) { strictCsp = it }
                }
            }
        }

        item {
            Text(
                text = "Generated Nginx Config Snippet:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyberGreen
            )
        }

        item {
            CodeBlockView(code = nginxConfig, language = "NGINX")
        }
    }
}

@Composable
fun HeaderToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen, checkedTrackColor = Color(0xFF00532B))
        )
    }
}
