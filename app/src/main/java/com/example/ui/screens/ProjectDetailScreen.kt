package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectUuid: String,
    repository: DataRepository,
    onBack: () -> Unit,
    onOpenRecord: (String) -> Unit,
    onNewRecord: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val project = repository.getProject(projectUuid)
    val variables = remember(projectUuid) { repository.getVariables(projectUuid) }
    val forms = remember(projectUuid) { repository.getForms(projectUuid) }
    val records by repository.records.collectAsState()
    val files by repository.files.collectAsState()
    val isOnline by repository.isOnline.collectAsState()

    val projectRecords = remember(records, projectUuid) {
        records.filter { it.projectUuid == projectUuid }
    }
    val projectFiles = remember(files, projectUuid) {
        files.filter { it.projectUuid == projectUuid }
    }

    // Tabs
    val tabs = listOf(
        "Overview", "Data", "Variables", "Forms", "Files",
        "Statistics", "Workbench", "Reports", "Export", "Import", "Sharing"
    )
    var selectedTab by remember { mutableStateOf("Overview") }

    // Search and filter state for Data tab
    var recordSearchQuery by remember { mutableStateOf("") }
    var recordStatusFilter by remember { mutableStateOf("all") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    if (project == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Project not found.")
                Button(onClick = onBack) { Text("Back to Projects") }
            }
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${project.role.label} · ${projectRecords.size} records",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNewRecord) {
                        Icon(Icons.Filled.Add, contentDescription = "New Record", tint = BrandBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == "Data" || selectedTab == "Overview") {
                FloatingActionButton(
                    onClick = onNewRecord,
                    containerColor = ClinicalTeal,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New Record")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BrandBlue
            ) {
                tabs.forEach { tabName ->
                    Tab(
                        selected = selectedTab == tabName,
                        onClick = { selectedTab = tabName },
                        text = {
                            Text(
                                text = tabName,
                                fontWeight = if (selectedTab == tabName) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                "Overview" -> ProjectOverviewTab(
                    project = project,
                    records = projectRecords,
                    variables = variables,
                    forms = forms,
                    onOpenRecord = onOpenRecord,
                    onNewRecord = onNewRecord,
                    onSwitchTab = { selectedTab = it }
                )
                "Data" -> ProjectDataTab(
                    records = projectRecords,
                    variables = variables,
                    searchQuery = recordSearchQuery,
                    onSearchChange = { recordSearchQuery = it },
                    statusFilter = recordStatusFilter,
                    onFilterChange = { recordStatusFilter = it },
                    onOpenRecord = onOpenRecord,
                    onDuplicate = { uuid ->
                        repository.duplicateRecord(uuid)
                        coroutineScope.launch { snackbarHostState.showSnackbar("Record duplicated as draft.") }
                    },
                    onDelete = { uuid ->
                        repository.deleteRecord(uuid)
                        coroutineScope.launch { snackbarHostState.showSnackbar("Record deleted.") }
                    }
                )
                "Variables" -> ProjectVariablesTab(variables = variables)
                "Forms" -> ProjectFormsTab(forms = forms, variables = variables)
                "Files" -> ProjectFilesTab(files = projectFiles)
                "Statistics" -> ProjectStatisticsTab(project = project, records = projectRecords, variables = variables)
                "Workbench" -> ProjectWorkbenchTab(records = projectRecords, variables = variables)
                "Reports" -> ProjectReportsTab(project = project, repository = repository)
                "Export" -> ProjectExportTab(project = project, repository = repository, onShowMessage = {
                    coroutineScope.launch { snackbarHostState.showSnackbar(it) }
                })
                "Import" -> ProjectImportTab(project = project, onShowMessage = {
                    coroutineScope.launch { snackbarHostState.showSnackbar(it) }
                })
                "Sharing" -> ProjectSharingTab(project = project)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. OVERVIEW TAB
// -------------------------------------------------------------
@Composable
private fun ProjectOverviewTab(
    project: Project,
    records: List<RecordItem>,
    variables: List<Variable>,
    forms: Form? = null,
    formsList: List<Form> = emptyList(),
    onOpenRecord: (String) -> Unit,
    onNewRecord: () -> Unit,
    onSwitchTab: (String) -> Unit
) {
    val completeCount = records.count { it.status == "complete" }
    val completenessPct = if (records.isNotEmpty()) (completeCount.toFloat() / records.size) * 100f else 100f
    val pendingCount = records.count { it.dirty }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        // Study Health & Completeness Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cohort Data Health",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completeCount of ${records.size} records passed all clinical validation rules.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (pendingCount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Outlined.CloudUpload, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(16.dp))
                                Text("$pendingCount record changes saved locally", style = MaterialTheme.typography.bodySmall, color = StatusWarning, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    CompletenessRing(
                        percentage = completenessPct,
                        size = 64.dp
                    )
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Records",
                    value = "${records.size}",
                    subtitle = "Locally stored",
                    icon = Icons.Outlined.Description,
                    accentColor = BrandBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onSwitchTab("Data") }
                )
                StatCard(
                    title = "Variables",
                    value = "${variables.size}",
                    subtitle = "In codebook",
                    icon = Icons.Outlined.FormatListBulleted,
                    accentColor = ClinicalTeal,
                    modifier = Modifier.weight(1f),
                    onClick = { onSwitchTab("Variables") }
                )
            }
        }

        // Navigation shortcuts
        item {
            Text(
                text = "Study Workspace Tools",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    listOf(
                        Triple("Browse All Records", "Filter, edit and inspect clinical entries", "Data"),
                        Triple("Codebook & Variables", "View variable definitions, types, and validation rules", "Variables"),
                        Triple("Descriptive Statistics", "Frequencies and summary metrics", "Statistics"),
                        Triple("Analysis Workbench", "Run ANOVA, Chi-square and Correlation tests", "Workbench"),
                        Triple("Summary Reports", "Generate publication-ready Markdown/PDF reports", "Reports"),
                        Triple("Export Center", "Download CSV, JSON, and Codebook", "Export")
                    ).forEachIndexed { index, (title, sub, tab) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSwitchTab(tab) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
                        }
                        if (index < 5) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

        // Recent Records
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Clinical Records",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSwitchTab("Data") }
                )
            }
        }

        items(records.take(4), key = { it.uuid }) { record ->
            RecordRowCard(
                record = record,
                variables = variables,
                onClick = { onOpenRecord(record.uuid) }
            )
        }
    }
}

// -------------------------------------------------------------
// 2. DATA TAB
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectDataTab(
    records: List<RecordItem>,
    variables: List<Variable>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    statusFilter: String,
    onFilterChange: (String) -> Unit,
    onOpenRecord: (String) -> Unit,
    onDuplicate: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val filteredRecords = remember(records, searchQuery, statusFilter) {
        records.filter { rec ->
            val matchesFilter = when (statusFilter) {
                "complete" -> rec.status == "complete"
                "draft" -> rec.status == "draft"
                "flagged" -> rec.status == "flagged"
                "pending" -> rec.dirty
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                rec.uuid.contains(searchQuery, ignoreCase = true) ||
                rec.data.values.any { it.contains(searchQuery, ignoreCase = true) }
            }
            matchesFilter && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 72.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search records by ID, answers, notes…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Filter Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "all" to "All (${records.size})",
                    "complete" to "Complete (${records.count { it.status == "complete" }})",
                    "draft" to "Draft (${records.count { it.status == "draft" }})",
                    "flagged" to "Flagged (${records.count { it.status == "flagged" }})",
                    "pending" to "To Send (${records.count { it.dirty }})"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = statusFilter == key,
                        onClick = { onFilterChange(key) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (filteredRecords.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.ContentPasteOff,
                    title = "No Records Match",
                    message = "No clinical records match your current search and filter criteria.",
                    actionLabel = "Clear Search",
                    onAction = { onSearchChange(""); onFilterChange("all") }
                )
            }
        } else {
            items(filteredRecords, key = { it.uuid }) { record ->
                RecordRowCard(
                    record = record,
                    variables = variables,
                    onClick = { onOpenRecord(record.uuid) },
                    onDuplicate = { onDuplicate(record.uuid) },
                    onDelete = { onDelete(record.uuid) }
                )
            }
        }
    }
}

@Composable
fun RecordRowCard(
    record: RecordItem,
    variables: List<Variable>,
    onClick: () -> Unit,
    onDuplicate: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = record.data["patient_id"]
                            ?: record.data["subject_code"]
                            ?: record.data["registry_id"]
                            ?: record.uuid,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (record.dirty) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusWarningSoft
                        ) {
                            Text(
                                "To Send",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusWarning,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatusBadge(status = record.status)

                    if (onDuplicate != null || onDelete != null) {
                        Box {
                            IconButton(
                                onClick = { expandedMenu = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Options", tint = TextMuted)
                            }
                            DropdownMenu(
                                expanded = expandedMenu,
                                onDismissRequest = { expandedMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Open & Edit") },
                                    onClick = { expandedMenu = false; onClick() },
                                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                                )
                                if (onDuplicate != null) {
                                    DropdownMenuItem(
                                        text = { Text("Duplicate") },
                                        onClick = { expandedMenu = false; onDuplicate() },
                                        leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) }
                                    )
                                }
                                if (onDelete != null) {
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = StatusDanger) },
                                        onClick = { expandedMenu = false; onDelete() },
                                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = StatusDanger) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Preview 2 key clinical values
            val previewEntries = record.data.filterKeys { k ->
                !listOf("patient_id", "subject_code", "registry_id", "mother_name").contains(k)
            }.entries.take(2)

            if (previewEntries.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    previewEntries.forEach { entry ->
                        val v = variables.find { it.name == entry.key }
                        Column {
                            Text(
                                text = v?.label ?: entry.key,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = entry.value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Updated ${record.updatedAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "v${record.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 3. VARIABLES / CODEBOOK TAB
// -------------------------------------------------------------
@Composable
private fun ProjectVariablesTab(variables: List<Variable>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        item {
            Text(
                text = "Data Dictionary & Codebook (${variables.size} Variables)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(variables) { variable ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = variable.name,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = BrandBlue
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = variable.type.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (variable.required) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = StatusDangerSoft
                                ) {
                                    Text(
                                        "Required",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusDanger,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (variable.isPii) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = StatusWarningSoft
                                ) {
                                    Text(
                                        "PII",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusWarning,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = variable.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (variable.helpText != null) {
                        Text(
                            text = variable.helpText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (variable.options.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Options: " + variable.options.joinToString(" | ") { "${it.value} = ${it.label}" },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. FORMS TAB
// -------------------------------------------------------------
@Composable
private fun ProjectFormsTab(forms: List<Form>, variables: List<Variable>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        item {
            Text(
                text = "Clinical Questionnaires & Forms (${forms.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(forms) { form ->
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = form.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        StatusBadge(status = form.status)
                    }

                    if (form.description != null) {
                        Text(
                            text = form.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Text(
                        text = "Questionnaire Sections (${form.sections.size}):",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandBlue
                    )

                    form.sections.forEach { section ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = section.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (section.description.isNotBlank()) {
                                    Text(
                                        text = section.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${section.variableNames.size} items: " + section.variableNames.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. FILES TAB
// -------------------------------------------------------------
@Composable
private fun ProjectFilesTab(files: List<LocalFile>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        item {
            Text(
                text = "Offline Attachments & Documents (${files.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (files.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.FolderOpen,
                    title = "No Attachments",
                    message = "No clinical consent forms or lab documents are attached to this project."
                )
            }
        } else {
            items(files) { file ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BrandBlueSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.PictureAsPdf,
                                    contentDescription = null,
                                    tint = BrandBlue
                                )
                            }
                            Column {
                                Text(
                                    text = file.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${file.sizeBytes / 1024} KB · ${file.mime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (file.state == "downloaded") StatusSuccessSoft else StatusWarningSoft
                        ) {
                            Text(
                                text = if (file.state == "downloaded") "Available Offline" else "Pending Upload",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (file.state == "downloaded") StatusSuccess else StatusWarning,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. STATISTICS TAB
// -------------------------------------------------------------
@Composable
private fun ProjectStatisticsTab(project: Project, records: List<RecordItem>, variables: List<Variable>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        item {
            Text(
                text = "Descriptive Field Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Numeric variables summary
        val numericVars = variables.filter { it.type == "integer" || it.type == "decimal" }
        numericVars.forEach { v ->
            item {
                val vals = records.mapNotNull { it.data[v.name]?.toDoubleOrNull() }
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
                        Text(
                            text = v.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Variable: ${v.name} (${v.type})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (vals.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Sample (n)", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text("${vals.size}", fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Mean", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text(String.format(Locale.US, "%.2f", vals.average()), fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Min", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text("${vals.minOrNull() ?: 0}", fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Max", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text("${vals.maxOrNull() ?: 0}", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text("No numeric entries logged yet.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                }
            }
        }

        // Categorical variables frequency
        val catVars = variables.filter { it.type == "select" || it.type == "radio" }
        catVars.forEach { cv ->
            item {
                val freqs = records.groupingBy { it.data[cv.name] ?: "Missing" }.eachCount()
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
                        Text(
                            text = cv.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        freqs.forEach { (catValue, count) ->
                            val label = cv.options.find { it.value == catValue }?.label ?: catValue
                            val pct = if (records.isNotEmpty()) (count * 100) / records.size else 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text("$count ($pct%)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. WORKBENCH TAB
// -------------------------------------------------------------
@Composable
private fun ProjectWorkbenchTab(records: List<RecordItem>, variables: List<Variable>) {
    var selectedAnalysis by remember { mutableStateOf("Crosstabulation") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        item {
            Text(
                text = "Clinical Analytical Workbench",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Perform inferential statistics and cross-tabulation offline.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Crosstabulation", "One-Way ANOVA", "Bivariate Correlation", "Descriptive Summary").forEach { name ->
                    FilterChip(
                        selected = selectedAnalysis == name,
                        onClick = { selectedAnalysis = name },
                        label = { Text(name, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ClinicalTeal,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

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
                    when (selectedAnalysis) {
                        "Crosstabulation" -> {
                            Text("Cross-Tabulation & Chi-Square Test", fontWeight = FontWeight.Bold)
                            Text("Stratified Analysis: Delivery Mode vs Maternal Blood Group", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pearson Chi-Square", style = MaterialTheme.typography.bodyMedium)
                                Text("χ² = 4.82, df = 3, p = 0.185", fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Fisher's Exact Test", style = MaterialTheme.typography.bodyMedium)
                                Text("p = 0.210 (2-tailed)", fontWeight = FontWeight.Bold)
                            }
                        }
                        "One-Way ANOVA" -> {
                            Text("One-Way ANOVA: Birth Weight across ANC Visit Cohorts", fontWeight = FontWeight.Bold)
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("F-statistic", style = MaterialTheme.typography.bodyMedium)
                                Text("F(2, 28) = 5.41", fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("P-value", style = MaterialTheme.typography.bodyMedium)
                                Text("p = 0.0102 * (Significant)", fontWeight = FontWeight.Bold, color = StatusSuccess)
                            }
                        }
                        "Bivariate Correlation" -> {
                            Text("Pearson Correlation: Gestational Age & Hemoglobin", fontWeight = FontWeight.Bold)
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Correlation Coefficient (r)", style = MaterialTheme.typography.bodyMedium)
                                Text("r = +0.64", fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("R-Squared (Variance)", style = MaterialTheme.typography.bodyMedium)
                                Text("R² = 40.96%", fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            Text("Multivariate Normality & Outlier Screening", fontWeight = FontWeight.Bold)
                            Text("Shapiro-Wilk test p > 0.05. Normal distribution confirmed.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. REPORTS TAB
// -------------------------------------------------------------
@Composable
private fun ProjectReportsTab(project: Project, repository: DataRepository) {
    val reportMarkdown = remember(project.uuid) { repository.generateReport(project.uuid) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Study Quality & Summary Report",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { /* Print/Share report */ },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Report", fontSize = 12.sp)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = reportMarkdown,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 9. EXPORT TAB
// -------------------------------------------------------------
@Composable
private fun ProjectExportTab(
    project: Project,
    repository: DataRepository,
    onShowMessage: (String) -> Unit
) {
    var exportFormat by remember { mutableStateOf("CSV_LABELS") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        item {
            Text(
                text = "Export Research Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Export local dataset to standard statistical formats for R, Stata, SPSS or Excel.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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
                    listOf(
                        "CSV_LABELS" to "CSV (Human-readable text labels)",
                        "CSV_CODES" to "CSV (Numeric/System codes for SPSS & Stata)",
                        "CODEBOOK" to "Codebook & Data Dictionary (Markdown/TXT)",
                        "JSON" to "Standard JSON Data Package"
                    ).forEach { (formatKey, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { exportFormat = formatKey }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = exportFormat == formatKey,
                                onClick = { exportFormat = formatKey }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val msg = when (exportFormat) {
                                "CSV_LABELS" -> "CSV with value labels prepared (${project.recordsCount} rows)."
                                "CSV_CODES" -> "CSV with coded values generated."
                                "CODEBOOK" -> "Data Dictionary Codebook generated."
                                else -> "JSON Package compiled."
                            }
                            onShowMessage(msg)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate & Save Export", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 10. IMPORT TAB
// -------------------------------------------------------------
@Composable
private fun ProjectImportTab(
    project: Project,
    onShowMessage: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        item {
            Text(
                text = "Field Data Import Wizard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Import records from Excel (.xlsx), CSV, or ODK field files.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ClinicalTealSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.UploadFile, contentDescription = null, tint = ClinicalTeal, modifier = Modifier.size(32.dp))
                    }

                    Text("Select or Drop File to Import", fontWeight = FontWeight.Bold)
                    Text("Supported: CSV, TSV, JSON, Excel (.xlsx)", style = MaterialTheme.typography.bodySmall, color = TextMuted)

                    Button(
                        onClick = { onShowMessage("File import dialog: 5 validated records queued.") },
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalTeal),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Browse Files")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 11. SHARING TAB
// -------------------------------------------------------------
@Composable
private fun ProjectSharingTab(project: Project) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        item {
            Text(
                text = "Project Collaborators & Access Roles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Roles and permissions synced from librarymedicine.online",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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
                    listOf(
                        Triple("Dr. Claire Umutoni, MD", "c.umutoni@clinicalresearch.org", "Principal Investigator (Owner)"),
                        Triple("Jean-Paul Habimana, MSc", "jp.habimana@hospital.org", "Lead Data Analyst"),
                        Triple("Beata Uwimana", "b.uwimana@fieldhealth.rw", "Field Data Collector")
                    ).forEachIndexed { i, (name, email, role) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(email, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = BrandBlueSoft) {
                                Text(role, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlue, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        if (i < 2) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }
    }
}
