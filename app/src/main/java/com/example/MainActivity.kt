package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CybersecurityScreen
import com.example.ui.screens.DeveloperScreen
import com.example.ui.screens.ModelsScreen
import com.example.ui.screens.RagScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TeamRulesScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

enum class MainNavTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
) {
    CHAT("Chat", Icons.Default.SmartToy, "nav_chat"),
    DEVELOPER("Developer", Icons.Default.Code, "nav_dev"),
    CYBERSECURITY("Security", Icons.Default.Security, "nav_sec"),
    FILES("Files (RAG)", Icons.Default.Folder, "nav_rag"),
    MODELS("Models", Icons.Default.Memory, "nav_models"),
    RULES("Rules", Icons.Default.Gavel, "nav_rules"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppRootView()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRootView(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val isOnline by viewModel.isOnline.collectAsState()
    val providerMode by viewModel.providerMode.collectAsState()

    // Handle back button: return to Chat tab first before closing app
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberGreen.copy(alpha = 0.2f))
                                .border(1.dp, CyberGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyberGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyberCyan.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "OFFLINE-FIRST",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberCyan
                                    )
                                }
                            }
                            Text(
                                text = "Private AI Developer Platform",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        onClick = { selectedTab = 6 }, // navigate to Settings
                        shape = RoundedCornerShape(14.dp),
                        color = DarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) CyberGreen else Color(0xFFFFB300))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = providerMode,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isOnline) CyberGreen else Color(0xFFFFB300)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Column {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/8801911527072"))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+8801911527072"))
                                context.startActivity(dial)
                            }
                        },
                    color = Color(0xFF041409),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberGreen.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "WhatsApp",
                            tint = CyberGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Developed and Idea by : GM Ripon, Whatsapp: +8801911527072 (Tap to Chat/Call)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberGreen
                        )
                    }
                }

                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 6.dp
                ) {
                MainNavTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00381B),
                            selectedTextColor = CyberGreen,
                            indicatorColor = CyberGreen,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ChatScreen(viewModel = viewModel)
                1 -> DeveloperScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { selectedTab = 0 }
                )
                2 -> CybersecurityScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { selectedTab = 0 }
                )
                3 -> RagScreen(viewModel = viewModel)
                4 -> ModelsScreen(viewModel = viewModel)
                5 -> TeamRulesScreen(viewModel = viewModel)
                6 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
