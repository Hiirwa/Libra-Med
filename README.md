# Library Medicine for Android

An offline-first clinical and epidemiological research data collection application written in **Kotlin** and **Jetpack Compose**. Designed for clinical investigators, epidemiologists, and field research teams working in regional hospitals, community clinics, and rural health posts without reliable network connectivity.

## Overview & Architecture

- **Platform Target:** Android (API 26+)
- **UI Framework:** Jetpack Compose with Material Design 3 (M3)
- **Language:** Kotlin 2.2+ (with Coroutines & Flow)
- **Architecture:** Clean Architecture & MVVM with unidirectional data flow
- **Styling System:** Library Medicine Clinical Design System (Brand Blue `#2D4BD8`, Clinical Teal `#0D7D72`, and status indicators)
- **Persistence & Offline Engine:** Reactive StateFlow repository with outbox queue and millisecond-precision UTC conflict resolution
- **Sync Protocol:** Compatible with Library Medicine server endpoints (`/api/v1/sync` and `/api/v1/me`)

---

## Core Features Ported

1. **Research Projects Dashboard (`HomeScreen`):**
   - Live research metrics: Active projects, Total records, New records this week, and Waiting to send (outbox).
   - 14-day longitudinal activity trend visualization.
   - Project search, sorting (A–Z, record counts), and status filtering (All, Active, To Send, Archived).
   - Quick action shortcuts: Data entry, Sync now, Sync Centre, Settings, and Help.

2. **Full-Featured Project Workspace (`ProjectDetailScreen`):**
   - **Overview:** Study health audit, record completeness progress ring, key indicators, and quick shortcuts.
   - **Data Management:** Search all columns, filter by status (Complete, Draft, Flagged, To Send), duplicate records, edit, and delete.
   - **Variables & Codebook:** Complete data dictionary with variable types (text, integer, decimal, select, radio, date, boolean, GPS), required flags, PII indicators, and coded option mappings.
   - **Forms:** Survey sections, structure hierarchy, and question layouts.
   - **Files:** Offline clinical consent PDFs and lab attachments with download and pending upload status.
   - **Statistics:** Summary metrics (mean, min, max, sample size) and frequency distributions.
   - **Workbench:** Inferential statistics including Cross-tabulation with Pearson Chi-Square, One-Way ANOVA, and Bivariate Pearson Correlation.
   - **Reports:** Study quality and executive summary report generation with export and share previews.
   - **Export Center:** Multi-format dataset export (CSV with labels, CSV with codes, Data Dictionary Codebook, and JSON data packages).
   - **Import Wizard:** Field data ingestion and column matching tool.
   - **Sharing & Access:** Team collaborator management with role-based permissions (Principal Investigator, Researcher, Field Collector, Viewer).

3. **Dynamic Clinical Record Editor (`RecordEditorScreen`):**
   - Stepper section navigation with answered progress tracking.
   - Dynamic form controls for text, textarea, integer, decimal, boolean switches, dropdown selects, radio option chips, and one-tap GPS coordinate acquisition.
   - Draft saving, complete record validation, duplicate record, and delete actions.

4. **Sync Centre (`SyncCenterScreen`):**
   - Outbox queue inspection for pending creates, edits, and deletions.
   - Connection status monitoring (with simulated online/offline field mode toggle).
   - On-demand "Sync Now" trigger with real-time sync progress animations and conflict preservation.

5. **Settings & Field Documentation (`SettingsScreen`, `HelpScreen`):**
   - Configurable research server URL and personal API token.
   - Live background sync toggles, attachment caching preferences, and auto-sync intervals.
   - Light and Dark theme modes optimized for hospital wards and low-light field conditions.
   - Offline research knowledge base with searchable FAQ topics.

---

## Preloaded Research Cohorts

- **Maternal & Child Health Study 2026 (`proj-mch-01`):** Longitudinal cohort assessing antenatal care visits, gestational age, maternal hemoglobin, delivery mode, and neonatal birth weight.
- **Malaria Surveillance Field Cohort (`proj-malaria-02`):** Point-of-care rapid diagnostic testing (RDT), axillary temperature, parasite densities, and vector bed net adoption across village clusters.
- **Cardiovascular Risk Registry (`proj-cvd-03`):** Primary prevention registry tracking blood pressure hemodynamics, lipid profiles, fasting glucose, and smoking history.
