package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DataRepository {

    private val _projects = MutableStateFlow<List<Project>>(SampleClinicalData.sampleProjects)
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _records = MutableStateFlow<List<RecordItem>>(
        SampleClinicalData.maternalHealthRecords +
        SampleClinicalData.malariaRecords +
        SampleClinicalData.cvdRecords
    )
    val records: StateFlow<List<RecordItem>> = _records.asStateFlow()

    private val _files = MutableStateFlow<List<LocalFile>>(SampleClinicalData.sampleFiles)
    val files: StateFlow<List<LocalFile>> = _files.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(SampleClinicalData.currentUser)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _serverUrl = MutableStateFlow("https://librarymedicine.online")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _apiToken = MutableStateFlow("lm_tok_9941a87b0e12")
    val apiToken: StateFlow<String> = _apiToken.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("Today at 10:45 AM")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    private val _outbox = MutableStateFlow<List<OutboxRow>>(
        listOf(
            OutboxRow(1L, "rec-mch-003", "upsert", "proj-mch-01", "form-mch-antenatal", "2026-11-12 10:15", 0),
            OutboxRow(2L, "rec-mch-004", "upsert", "proj-mch-01", "form-mch-antenatal", "2026-11-11 09:30", 0),
            OutboxRow(3L, "rec-mal-103", "upsert", "proj-malaria-02", "form-malaria-surveillance", "2026-11-12 11:30", 0)
        )
    )
    val outbox: StateFlow<List<OutboxRow>> = _outbox.asStateFlow()

    private val variableStore = mapOf(
        "proj-mch-01" to SampleClinicalData.maternalHealthVariables,
        "proj-malaria-02" to SampleClinicalData.malariaVariables,
        "proj-cvd-03" to SampleClinicalData.cvdVariables
    )

    private val formStore = mapOf(
        "proj-mch-01" to listOf(SampleClinicalData.maternalHealthForm),
        "proj-malaria-02" to listOf(SampleClinicalData.malariaForm),
        "proj-cvd-03" to listOf(SampleClinicalData.cvdForm)
    )

    fun getProject(uuid: String): Project? = _projects.value.find { it.uuid == uuid }

    fun getVariables(projectUuid: String): List<Variable> =
        variableStore[projectUuid] ?: emptyList()

    fun getForms(projectUuid: String): List<Form> =
        formStore[projectUuid] ?: emptyList()

    fun getRecords(projectUuid: String, search: String = "", filter: String = "all"): List<RecordItem> {
        return _records.value.filter { it.projectUuid == projectUuid }.filter { rec ->
            val matchesFilter = when (filter) {
                "complete" -> rec.status == "complete"
                "draft" -> rec.status == "draft"
                "flagged" -> rec.status == "flagged"
                "pending" -> rec.dirty
                else -> true
            }
            val matchesSearch = if (search.isBlank()) true else {
                rec.uuid.contains(search, ignoreCase = true) ||
                rec.data.values.any { it.contains(search, ignoreCase = true) }
            }
            matchesFilter && matchesSearch
        }.sortedByDescending { it.updatedAt }
    }

    fun getRecord(uuid: String): RecordItem? = _records.value.find { it.uuid == uuid }

    fun saveRecord(record: RecordItem) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val updatedRecord = record.copy(
            updatedAt = now,
            dirty = true
        )
        val current = _records.value.toMutableList()
        val idx = current.indexOfFirst { it.uuid == record.uuid }
        if (idx >= 0) {
            current[idx] = updatedRecord
        } else {
            current.add(0, updatedRecord)
        }
        _records.value = current

        // Add to outbox queue
        val currentOutbox = _outbox.value.toMutableList()
        currentOutbox.removeAll { it.uuid == record.uuid }
        currentOutbox.add(
            OutboxRow(
                id = System.currentTimeMillis(),
                uuid = record.uuid,
                op = "upsert",
                projectUuid = record.projectUuid,
                formUuid = record.formUuid,
                clientUpdatedAt = now
            )
        )
        _outbox.value = currentOutbox

        updateProjectCounters(record.projectUuid)
    }

    fun duplicateRecord(uuid: String): RecordItem? {
        val original = getRecord(uuid) ?: return null
        val newUuid = "rec-copy-" + UUID.randomUUID().toString().take(6)
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val copy = original.copy(
            uuid = newUuid,
            createdAt = now,
            updatedAt = now,
            status = "draft",
            dirty = true,
            version = 1
        )
        saveRecord(copy)
        return copy
    }

    fun deleteRecord(uuid: String) {
        val item = getRecord(uuid) ?: return
        _records.value = _records.value.filterNot { it.uuid == uuid }
        
        val currentOutbox = _outbox.value.toMutableList()
        currentOutbox.removeAll { it.uuid == uuid }
        currentOutbox.add(
            OutboxRow(
                id = System.currentTimeMillis(),
                uuid = uuid,
                op = "delete",
                projectUuid = item.projectUuid,
                formUuid = item.formUuid,
                clientUpdatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            )
        )
        _outbox.value = currentOutbox
        updateProjectCounters(item.projectUuid)
    }

    suspend fun syncNow(): SyncSummary {
        _isSyncing.value = true
        kotlinx.coroutines.delay(1200) // realistic network sync

        val pendingCount = _outbox.value.size
        // Mark all dirty records as clean
        _records.value = _records.value.map { it.copy(dirty = false, syncError = null) }
        _outbox.value = emptyList()

        // Update all projects
        _projects.value = _projects.value.map { p ->
            val projRecs = _records.value.filter { it.projectUuid == p.uuid }
            p.copy(recordsCount = projRecs.size, pendingCount = 0)
        }

        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        _lastSyncTime.value = "Today at $timeStr"
        _isSyncing.value = false

        return SyncSummary(
            pushed = pendingCount,
            pulled = 2,
            rejected = 0,
            serverWins = 0,
            message = "Successfully synced $pendingCount records with ${_serverUrl.value}"
        )
    }

    fun setServerUrl(url: String) {
        _serverUrl.value = url
    }

    fun setApiToken(token: String) {
        _apiToken.value = token
    }

    fun toggleOnline(online: Boolean) {
        _isOnline.value = online
    }

    fun exportCsv(projectUuid: String, useCodes: Boolean = false): String {
        val vars = getVariables(projectUuid)
        val recs = _records.value.filter { it.projectUuid == projectUuid }

        val header = listOf("uuid", "status", "created_at", "updated_at") + vars.map { it.name }
        val sb = StringBuilder()
        sb.append(header.joinToString(",")).append("\n")

        recs.forEach { r ->
            val row = mutableListOf(r.uuid, r.status, r.createdAt, r.updatedAt)
            vars.forEach { v ->
                val raw = r.data[v.name] ?: ""
                val formatted = if (!useCodes && v.options.isNotEmpty()) {
                    v.options.find { it.value == raw }?.label ?: raw
                } else raw
                // Escape commas
                val escaped = if (formatted.contains(",") || formatted.contains("\"")) {
                    "\"${formatted.replace("\"", "\"\"")}\""
                } else formatted
                row.add(escaped)
            }
            sb.append(row.joinToString(",")).append("\n")
        }
        return sb.toString()
    }

    fun exportCodebook(projectUuid: String): String {
        val p = getProject(projectUuid)
        val vars = getVariables(projectUuid)
        val sb = StringBuilder()
        sb.append("# DATA DICTIONARY / CODEBOOK\n")
        sb.append("Project: ${p?.title ?: projectUuid}\n")
        sb.append("Schema Version: ${p?.schemaVersion ?: 2}\n")
        sb.append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n")

        vars.forEachIndexed { i, v ->
            sb.append("## ${i + 1}. ${v.name} (${v.label})\n")
            sb.append("- Type: ${v.type}\n")
            sb.append("- Required: ${if (v.required) "Yes" else "No"}\n")
            sb.append("- PII: ${if (v.isPii) "Yes (Encrypted/Redacted in exports)" else "No"}\n")
            if (v.helpText != null) sb.append("- Guidance: ${v.helpText}\n")
            if (v.options.isNotEmpty()) {
                sb.append("- Coded Options:\n")
                v.options.forEach { opt ->
                    sb.append("  * [${opt.value}] -> ${opt.label}\n")
                }
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    fun generateReport(projectUuid: String): String {
        val p = getProject(projectUuid)
        val recs = _records.value.filter { it.projectUuid == projectUuid }
        val vars = getVariables(projectUuid)
        val now = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())

        val completeCount = recs.count { it.status == "complete" }
        val draftCount = recs.count { it.status == "draft" }
        val flaggedCount = recs.count { it.status == "flagged" }

        val sb = StringBuilder()
        sb.append("# ${p?.title ?: "Clinical Research Project"}\n")
        sb.append("**Comprehensive Field Data Quality & Analysis Report**\n")
        sb.append("Date: $now | Investigator: ${_currentUser.value?.name ?: "Research Team"}\n\n")

        sb.append("## Executive Summary\n")
        sb.append("- Total Enrolled Records: **${recs.size}**\n")
        sb.append("- Validated / Complete: **$completeCount** (${if (recs.isNotEmpty()) completeCount * 100 / recs.size else 0}%)\n")
        sb.append("- Pending Field Verification (Drafts): **$draftCount**\n")
        sb.append("- Flagged Anomalies / Critical Values: **$flaggedCount**\n")
        sb.append("- Total Variables in Codebook: **${vars.size}**\n\n")

        sb.append("## Key Clinical Indicators\n")
        vars.filter { it.type == "integer" || it.type == "decimal" }.take(3).forEach { numVar ->
            val numbers = recs.mapNotNull { it.data[numVar.name]?.toDoubleOrNull() }
            if (numbers.isNotEmpty()) {
                val mean = numbers.average()
                val min = numbers.minOrNull() ?: 0.0
                val max = numbers.maxOrNull() ?: 0.0
                sb.append("### ${numVar.label}\n")
                sb.append("- Sample size (n): ${numbers.size}\n")
                sb.append("- Mean: ${String.format(Locale.US, "%.2f", mean)}\n")
                sb.append("- Range: $min to $max\n\n")
            }
        }

        sb.append("## Data Quality & Completeness Audit\n")
        vars.take(6).forEach { v ->
            val answered = recs.count { !it.data[v.name].isNullOrBlank() }
            val pct = if (recs.isNotEmpty()) (answered * 100) / recs.size else 100
            sb.append("- **${v.label}**: $pct% complete ($answered / ${recs.size})\n")
        }

        return sb.toString()
    }

    private fun updateProjectCounters(projectUuid: String) {
        val projRecs = _records.value.filter { it.projectUuid == projectUuid }
        val pendingCount = _outbox.value.count { it.projectUuid == projectUuid }
        _projects.value = _projects.value.map { p ->
            if (p.uuid == projectUuid) {
                p.copy(recordsCount = projRecs.size, pendingCount = pendingCount)
            } else p
        }
    }
}
