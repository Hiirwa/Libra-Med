package com.example.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole(val label: String) {
    OWNER("Principal Investigator"),
    RESEARCHER("Lead Researcher"),
    COLLECTOR("Field Data Collector"),
    VIEWER("Observer / Auditor")
}

@Serializable
data class Project(
    val uuid: String,
    val title: String,
    val description: String,
    val role: UserRole,
    val status: String = "active", // active, archived
    val startDate: String? = null,
    val endDate: String? = null,
    val period: String? = "2026 Cohort",
    val schemaVersion: Int = 2,
    val canAdd: Boolean = true,
    val canEdit: Boolean = true,
    val canDelete: Boolean = true,
    val canPii: Boolean = true,
    val recordsCount: Int = 0,
    val pendingCount: Int = 0
)

@Serializable
data class FormSection(
    val id: String,
    val title: String,
    val description: String = "",
    val variableNames: List<String> = emptyList()
)

@Serializable
data class Form(
    val uuid: String,
    val projectUuid: String,
    val title: String,
    val status: String = "active",
    val description: String? = null,
    val sections: List<FormSection> = emptyList()
)

@Serializable
data class FieldOption(
    val value: String,
    val label: String
)

@Serializable
data class Variable(
    val name: String,
    val label: String,
    val type: String, // text, textarea, integer, decimal, date, time, select, radio, checkbox, boolean, gps, file
    val description: String? = null,
    val helpText: String? = null,
    val required: Boolean = false,
    val options: List<FieldOption> = emptyList(),
    val min: Double? = null,
    val max: Double? = null,
    val isPii: Boolean = false,
    val sortOrder: Int = 0
)

@Serializable
data class RecordItem(
    val uuid: String,
    val projectUuid: String,
    val formUuid: String? = null,
    val status: String = "complete", // complete, draft, flagged
    val version: Int = 1,
    val createdAt: String,
    val updatedAt: String,
    val dirty: Boolean = false, // true = in outbox waiting to sync
    val syncError: String? = null,
    val data: Map<String, String> = emptyMap()
)

@Serializable
data class OutboxRow(
    val id: Long,
    val uuid: String,
    val op: String, // upsert, delete
    val projectUuid: String,
    val formUuid: String? = null,
    val clientUpdatedAt: String,
    val attempts: Int = 0,
    val lastError: String? = null
)

@Serializable
data class LocalFile(
    val uuid: String,
    val recordUuid: String? = null,
    val projectUuid: String,
    val variableName: String? = null,
    val name: String,
    val mime: String = "application/pdf",
    val sizeBytes: Long = 1024,
    val state: String = "downloaded" // downloaded, pending_upload, remote
)

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val institution: String,
    val role: String = "researcher"
)

data class SyncSummary(
    val pushed: Int = 0,
    val pulled: Int = 0,
    val rejected: Int = 0,
    val serverWins: Int = 0,
    val message: String = ""
)

data class WorkbenchAnalysis(
    val title: String,
    val variableName: String,
    val analysisType: String, // frequencies, descriptives, crosstab, correlation
    val summaryMetrics: Map<String, String>,
    val tableHeaders: List<String> = emptyList(),
    val tableRows: List<List<String>> = emptyList()
)
