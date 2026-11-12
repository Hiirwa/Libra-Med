package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordEditorScreen(
    recordUuid: String?,
    projectUuid: String,
    repository: DataRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val project = repository.getProject(projectUuid)
    val variables = remember(projectUuid) { repository.getVariables(projectUuid) }
    val forms = remember(projectUuid) { repository.getForms(projectUuid) }
    val existingRecord = remember(recordUuid) {
        if (recordUuid != null) repository.getRecord(recordUuid) else null
    }

    val form = forms.firstOrNull()
    val sections = form?.sections ?: listOf(
        FormSection("all", "Clinical Data Entry", "Complete all required research fields", variables.map { it.name })
    )

    var currentSectionIndex by remember { mutableIntStateOf(0) }
    val currentSection = sections.getOrElse(currentSectionIndex) { sections.first() }

    // Form data state
    val formData = remember {
        mutableStateMapOf<String, String>().apply {
            if (existingRecord != null) {
                putAll(existingRecord.data)
            } else {
                // Default UUID and identifiers
                val prefix = project?.title?.take(3)?.uppercase() ?: "REC"
                put("patient_id", "$prefix-2026-" + (1000..9999).random())
                put("subject_code", "$prefix-SUB-" + (100..999).random())
                put("registry_id", "$prefix-REG-" + (100..999).random())
            }
        }
    }

    var recordStatus by remember {
        mutableStateOf(existingRecord?.status ?: "draft")
    }

    val totalVariables = variables.size
    val answeredCount = variables.count { !formData[it.name].isNullOrBlank() }
    val answeredPct = if (totalVariables > 0) (answeredCount.toFloat() / totalVariables) else 0f

    val currentSectionVariables = variables.filter { currentSection.variableNames.contains(it.name) }

    fun doSave(statusToSet: String) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val finalUuid = existingRecord?.uuid ?: ("rec-" + UUID.randomUUID().toString().take(8))
        val newRecord = RecordItem(
            uuid = finalUuid,
            projectUuid = projectUuid,
            formUuid = form?.uuid,
            status = statusToSet,
            version = (existingRecord?.version ?: 0) + 1,
            createdAt = existingRecord?.createdAt ?: now,
            updatedAt = now,
            dirty = true,
            data = formData.toMap()
        )
        repository.saveRecord(newRecord)
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (existingRecord == null) "New Clinical Record" else "Edit Record",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = project?.title ?: "Study Intake",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(onClick = { doSave("draft") }) {
                        Text("Save Draft", fontWeight = FontWeight.SemiBold, color = BrandBlue)
                    }
                    Button(
                        onClick = { doSave("complete") },
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Complete", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Completion Progress Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress: $answeredCount of $totalVariables answered (${(answeredPct * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StatusBadge(status = recordStatus)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { answeredPct },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ClinicalTeal,
                    trackColor = ClinicalTeal.copy(alpha = 0.15f)
                )
            }

            // Section Stepper Header
            if (sections.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sections.forEachIndexed { idx, sec ->
                        FilterChip(
                            selected = currentSectionIndex == idx,
                            onClick = { currentSectionIndex = idx },
                            label = { Text(sec.title.split(".").firstOrNull() ?: "Step ${idx + 1}") },
                            leadingIcon = {
                                Text(
                                    "${idx + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Form Fields
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = currentSection.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentSection.description.isNotBlank()) {
                                Text(
                                    text = currentSection.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(currentSectionVariables, key = { it.name }) { v ->
                    FormFieldItem(
                        variable = v,
                        currentValue = formData[v.name] ?: "",
                        onValueChange = { formData[v.name] = it }
                    )
                }

                // Section Navigation Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (currentSectionIndex > 0) {
                            OutlinedButton(
                                onClick = { currentSectionIndex-- },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Previous Step")
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        if (currentSectionIndex < sections.size - 1) {
                            Button(
                                onClick = { currentSectionIndex++ },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                            ) {
                                Text("Next Step")
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormFieldItem(
    variable: Variable,
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = variable.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (variable.required) {
                        Text("*", color = StatusDanger, fontWeight = FontWeight.Bold)
                    }
                }

                if (variable.isPii) {
                    Surface(shape = RoundedCornerShape(4.dp), color = StatusWarningSoft) {
                        Text("PII", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusWarning, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }

            if (variable.helpText != null) {
                Text(
                    text = variable.helpText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (variable.type) {
                "integer", "decimal" -> {
                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = {
                            if (variable.type == "decimal") {
                                Text("12.3", style = MaterialTheme.typography.bodySmall, color = TextMuted, modifier = Modifier.padding(end = 8.dp))
                            }
                        }
                    )
                }

                "textarea" -> {
                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                "boolean" -> {
                    val isChecked = currentValue.equals("true", ignoreCase = true)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isChecked) "Yes (Positive / Confirmed)" else "No (Negative / Not given)",
                            fontWeight = FontWeight.SemiBold,
                            color = if (isChecked) StatusSuccess else TextMuted
                        )
                        Switch(
                            checked = isChecked,
                            onCheckedChange = { onValueChange(it.toString()) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ClinicalTeal
                            )
                        )
                    }
                }

                "select", "radio" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        variable.options.forEach { opt ->
                            val selected = currentValue == opt.value
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onValueChange(opt.value) }
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) BrandBlue else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                color = if (selected) BrandBlueSoft else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = { onValueChange(opt.value) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = opt.label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) BrandBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                "gps" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = currentValue,
                            onValueChange = onValueChange,
                            placeholder = { Text("e.g. -1.9536, 30.0606") },
                            leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = ClinicalTeal) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Button(
                            onClick = {
                                onValueChange("-1.9536, 30.0606 (Kigali Health Post)")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealSoft),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.MyLocation, contentDescription = null, tint = ClinicalTeal, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use Current GPS Location", color = ClinicalTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                else -> {
                    // Default text field
                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }
    }
}
