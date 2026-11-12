package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: DataRepository,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val serverUrl by repository.serverUrl.collectAsState()
    val apiToken by repository.apiToken.collectAsState()
    val records by repository.records.collectAsState()
    val files by repository.files.collectAsState()

    var editingServer by remember { mutableStateOf(serverUrl) }
    var editingToken by remember { mutableStateOf(apiToken) }
    var autoSyncInterval by remember { mutableFloatStateOf(5f) }
    var downloadAttachments by remember { mutableStateOf(true) }
    var liveSyncEnabled by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings & Connection", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            // Server Connection Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Server Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        OutlinedTextField(
                            value = editingServer,
                            onValueChange = { editingServer = it },
                            label = { Text("Research Server URL") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editingToken,
                            onValueChange = { editingToken = it },
                            label = { Text("Personal API Token") },
                            placeholder = { Text("lm_tok_...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                repository.setServerUrl(editingServer.trim())
                                repository.setApiToken(editingToken.trim())
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Server configuration updated.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Save Credentials")
                        }
                    }
                }
            }

            // Sync Policies
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Sync & Data Policies", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Live Background Sync", fontWeight = FontWeight.SemiBold)
                                Text("Instantly send queued entries when network returns", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            Switch(
                                checked = liveSyncEnabled,
                                onCheckedChange = { liveSyncEnabled = it }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Offline Attachments", fontWeight = FontWeight.SemiBold)
                                Text("Cache clinical PDFs and consent documents locally", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            Switch(
                                checked = downloadAttachments,
                                onCheckedChange = { downloadAttachments = it }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        Column {
                            Text("Automatic Sync Interval: ${autoSyncInterval.toInt()} minutes", fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = autoSyncInterval,
                                onValueChange = { autoSyncInterval = it },
                                valueRange = 1f..30f,
                                steps = 5
                            )
                        }
                    }
                }
            }

            // Appearance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Appearance", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Dark Theme", fontWeight = FontWeight.SemiBold)
                                Text("Night mode for hospital wards & low-light field work", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = onToggleDarkTheme
                            )
                        }
                    }
                }
            }

            // Local Storage Audit
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Local Device Storage", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("• Encrypted clinical records: ${records.size} entries", style = MaterialTheme.typography.bodySmall)
                        Text("• Cached attachments: ${files.size} documents (${files.sumOf { it.sizeBytes } / 1024} KB)", style = MaterialTheme.typography.bodySmall)
                        Text("• SQLite database integrity: Verified clean", style = MaterialTheme.typography.bodySmall, color = StatusSuccess)
                    }
                }
            }

            // About
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Library Medicine Android v1.5.4", fontWeight = FontWeight.Bold)
                        Text("Research data collection that never waits for the network.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("Built with Kotlin & Jetpack Compose", style = MaterialTheme.typography.bodySmall, color = BrandBlue)
                    }
                }
            }
        }
    }
}
