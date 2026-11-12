package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.*

data class FaqItem(val question: String, val answer: String, val category: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }
    var expandedFaq by remember { mutableStateOf<String?>(null) }

    val faqs = listOf(
        FaqItem(
            "How does offline data collection work?",
            "Every clinical record, form response and photo attachment you enter is instantly saved and timestamped on your device's encrypted local SQLite storage. When you reconnect to Wi-Fi or mobile data, changes automatically upload to the Library Medicine server.",
            "Offline"
        ),
        FaqItem(
            "What happens if two people edit the same record?",
            "Library Medicine utilizes millisecond-precision UTC conflict resolution. If two field workers edit the same record, the newer timestamp wins on the server, but your local draft copy is preserved in the Conflicts archive under Sync Centre so no clinical data is ever lost.",
            "Sync"
        ),
        FaqItem(
            "How do I obtain or refresh my API Token?",
            "Sign into your account at librarymedicine.online, open Account & Security → API Tokens, and generate a new token with 'Read & Write' permissions for your assigned projects. Paste it in Settings.",
            "Account"
        ),
        FaqItem(
            "Can I collect GPS coordinates in the field without internet?",
            "Yes! Your device's internal GPS hardware functions independently of cellular or internet coverage. Tapping 'Use Current GPS Location' reads direct satellite coordinates and locks them into the form.",
            "Field"
        ),
        FaqItem(
            "Are patient identifiers and PII protected?",
            "Variables designated as PII (Personally Identifiable Information) in the study codebook are encrypted on device and automatically redacted or hashed during statistical exports for Stata/SPSS/R.",
            "Security"
        )
    )

    val filteredFaqs = faqs.filter { faq ->
        val matchesCat = selectedCategory == "all" || faq.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                faq.question.contains(searchQuery, ignoreCase = true) ||
                faq.answer.contains(searchQuery, ignoreCase = true)
        matchesCat && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Documentation", fontWeight = FontWeight.Bold) },
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
            // Field Guide Banner
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.School, contentDescription = null, tint = ClinicalTeal)
                            Text("Field Researcher Knowledge Base", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(
                            "Guides and documentation cached locally so you can troubleshoot in remote clinics and rural health posts without internet connectivity.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Search FAQs
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search help topics, sync questions…") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Category Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("all" to "All Topics", "Offline" to "Offline", "Sync" to "Sync", "Security" to "Security").forEach { (cat, label) ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandBlue,
                                selectedLabelColor = androidx.compose.ui.graphics.Color.White
                            )
                        )
                    }
                }
            }

            // FAQ Accordion List
            items(filteredFaqs) { faq ->
                val isExpanded = expandedFaq == faq.question
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedFaq = if (isExpanded) null else faq.question },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = faq.question,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        }

                        if (isExpanded) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Text(
                                text = faq.answer,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
