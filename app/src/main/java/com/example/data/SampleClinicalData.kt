package com.example.data

import com.example.model.*

object SampleClinicalData {

    val currentUser = UserProfile(
        id = "usr-lm-904",
        name = "Dr. Claire Umutoni, MD",
        email = "c.umutoni@clinicalresearch.org",
        institution = "Kigali University Teaching Hospital (CHUK)",
        role = "Principal Investigator"
    )

    val maternalHealthVariables = listOf(
        Variable(
            name = "patient_id",
            label = "Participant ID",
            type = "text",
            required = true,
            helpText = "Format: MCH-2026-XXXX",
            sortOrder = 1
        ),
        Variable(
            name = "mother_name",
            label = "Mother's Full Name",
            type = "text",
            required = true,
            isPii = true,
            helpText = "Confidential patient identifier",
            sortOrder = 2
        ),
        Variable(
            name = "gestational_age_weeks",
            label = "Gestational Age (weeks)",
            type = "integer",
            required = true,
            min = 12.0,
            max = 44.0,
            sortOrder = 3
        ),
        Variable(
            name = "antenatal_visits",
            label = "Completed ANC Visits",
            type = "integer",
            required = true,
            min = 0.0,
            max = 12.0,
            sortOrder = 4
        ),
        Variable(
            name = "hemoglobin_level",
            label = "Hemoglobin Level (g/dL)",
            type = "decimal",
            required = true,
            min = 4.0,
            max = 20.0,
            sortOrder = 5
        ),
        Variable(
            name = "blood_group",
            label = "Maternal Blood Group",
            type = "select",
            required = true,
            options = listOf(
                FieldOption("A_POS", "A Positive (A+)"),
                FieldOption("B_POS", "B Positive (B+)"),
                FieldOption("O_POS", "O Positive (O+)"),
                FieldOption("AB_POS", "AB Positive (AB+)"),
                FieldOption("O_NEG", "O Negative (O-)"),
                FieldOption("A_NEG", "A Negative (A-)")
            ),
            sortOrder = 6
        ),
        Variable(
            name = "delivery_mode",
            label = "Mode of Delivery",
            type = "radio",
            required = false,
            options = listOf(
                FieldOption("VAGINAL", "Spontaneous Vaginal"),
                FieldOption("CS_ELECTIVE", "Planned Caesarean Section"),
                FieldOption("CS_EMERGENCY", "Emergency Caesarean Section"),
                FieldOption("ASSISTED", "Assisted Vacuum/Forceps")
            ),
            sortOrder = 7
        ),
        Variable(
            name = "birth_weight_kg",
            label = "Infant Birth Weight (kg)",
            type = "decimal",
            min = 0.5,
            max = 6.0,
            sortOrder = 8
        ),
        Variable(
            name = "tetanus_toxoid_received",
            label = "Tetanus Toxoid Vaccine Administered",
            type = "boolean",
            required = true,
            sortOrder = 9
        ),
        Variable(
            name = "clinic_location_gps",
            label = "Health Center GPS Coordinates",
            type = "gps",
            required = false,
            helpText = "Recorded at primary health post",
            sortOrder = 10
        ),
        Variable(
            name = "nurse_notes",
            label = "Clinical Observations & Follow-up Notes",
            type = "textarea",
            required = false,
            sortOrder = 11
        )
    )

    val maternalHealthForm = Form(
        uuid = "form-mch-antenatal",
        projectUuid = "proj-mch-01",
        title = "Antenatal & Birth Outcome Questionnaire",
        status = "active",
        description = "Standard protocol for longitudinal maternal health cohort tracking ANC adherence and birth outcomes.",
        sections = listOf(
            FormSection(
                id = "sec-1",
                title = "1. Participant Registration & Demographics",
                description = "Primary identification and enrollment variables",
                variableNames = listOf("patient_id", "mother_name", "gestational_age_weeks")
            ),
            FormSection(
                id = "sec-2",
                title = "2. Laboratory Biomarkers & Clinical Measurements",
                description = "Vital signs, hemoglobin levels and hematology screening",
                variableNames = listOf("antenatal_visits", "hemoglobin_level", "blood_group", "tetanus_toxoid_received")
            ),
            FormSection(
                id = "sec-3",
                title = "3. Delivery Outcome & Field Geolocation",
                description = "Birth weight metrics, mode of delivery and GPS confirmation",
                variableNames = listOf("delivery_mode", "birth_weight_kg", "clinic_location_gps", "nurse_notes")
            )
        )
    )

    val maternalHealthRecords = listOf(
        RecordItem(
            uuid = "rec-mch-001",
            projectUuid = "proj-mch-01",
            formUuid = "form-mch-antenatal",
            status = "complete",
            version = 2,
            createdAt = "2026-11-04 09:15",
            updatedAt = "2026-11-10 14:22",
            dirty = false,
            data = mapOf(
                "patient_id" to "MCH-2026-0041",
                "mother_name" to "Aline Mukamana",
                "gestational_age_weeks" to "38",
                "antenatal_visits" to "5",
                "hemoglobin_level" to "12.4",
                "blood_group" to "O_POS",
                "delivery_mode" to "VAGINAL",
                "birth_weight_kg" to "3.25",
                "tetanus_toxoid_received" to "true",
                "clinic_location_gps" to "-1.9536, 30.0606 (CHUK Maternity Wing)",
                "nurse_notes" to "Healthy term neonate. Mother discharged with iron and folate supplements."
            )
        ),
        RecordItem(
            uuid = "rec-mch-002",
            projectUuid = "proj-mch-01",
            formUuid = "form-mch-antenatal",
            status = "complete",
            version = 1,
            createdAt = "2026-11-06 11:30",
            updatedAt = "2026-11-09 16:45",
            dirty = false,
            data = mapOf(
                "patient_id" to "MCH-2026-0042",
                "mother_name" to "Diane Uwera",
                "gestational_age_weeks" to "36",
                "antenatal_visits" to "4",
                "hemoglobin_level" to "10.8",
                "blood_group" to "B_POS",
                "delivery_mode" to "CS_ELECTIVE",
                "birth_weight_kg" to "2.90",
                "tetanus_toxoid_received" to "true",
                "clinic_location_gps" to "-1.9441, 30.0892 (Kacyiru Hospital)",
                "nurse_notes" to "Moderate anemia managed with dietary counseling and intravenous iron sucrose."
            )
        ),
        RecordItem(
            uuid = "rec-mch-003",
            projectUuid = "proj-mch-01",
            formUuid = "form-mch-antenatal",
            status = "draft",
            version = 1,
            createdAt = "2026-11-11 08:20",
            updatedAt = "2026-11-12 10:15",
            dirty = true, // waiting in outbox
            data = mapOf(
                "patient_id" to "MCH-2026-0043",
                "mother_name" to "Chantal Ingabire",
                "gestational_age_weeks" to "28",
                "antenatal_visits" to "3",
                "hemoglobin_level" to "11.2",
                "blood_group" to "A_POS",
                "tetanus_toxoid_received" to "true",
                "clinic_location_gps" to "-1.9680, 30.1030 (Remera Health Center)",
                "nurse_notes" to "Routine 28-week ANC visit. Scheduled for third trimester ultrasound."
            )
        ),
        RecordItem(
            uuid = "rec-mch-004",
            projectUuid = "proj-mch-01",
            formUuid = "form-mch-antenatal",
            status = "flagged",
            version = 1,
            createdAt = "2026-11-08 14:00",
            updatedAt = "2026-11-11 09:30",
            dirty = true,
            data = mapOf(
                "patient_id" to "MCH-2026-0044",
                "mother_name" to "Grace Mutesi",
                "gestational_age_weeks" to "39",
                "antenatal_visits" to "2",
                "hemoglobin_level" to "8.9",
                "blood_group" to "O_NEG",
                "delivery_mode" to "CS_EMERGENCY",
                "birth_weight_kg" to "2.40",
                "tetanus_toxoid_received" to "false",
                "clinic_location_gps" to "-1.9750, 30.0450 (Muhima District Hospital)",
                "nurse_notes" to "Severe anemia and low birth weight. Transferred to neonatal intensive care unit."
            )
        )
    )

    val malariaVariables = listOf(
        Variable(
            name = "subject_code",
            label = "Cohort Subject Code",
            type = "text",
            required = true,
            sortOrder = 1
        ),
        Variable(
            name = "village_cluster",
            label = "Surveillance Village Ward",
            type = "select",
            required = true,
            options = listOf(
                FieldOption("NYAGATARE_1", "Nyagatare Sector - Cell 1"),
                FieldOption("KAYONZA_4", "Kayonza Eastern Basin"),
                FieldOption("BUGESERA_2", "Bugesera Lowland Lakes"),
                FieldOption("GICUMBI_3", "Gicumbi Highland Sector")
            ),
            sortOrder = 2
        ),
        Variable(
            name = "temperature_celsius",
            label = "Axillary Temperature (°C)",
            type = "decimal",
            required = true,
            min = 34.0,
            max = 43.0,
            sortOrder = 3
        ),
        Variable(
            name = "fever_reported",
            label = "Acute Fever in Past 48h",
            type = "boolean",
            required = true,
            sortOrder = 4
        ),
        Variable(
            name = "rdt_result",
            label = "Rapid Diagnostic Test (RDT)",
            type = "radio",
            required = true,
            options = listOf(
                FieldOption("POSITIVE", "P. falciparum Positive"),
                FieldOption("NEGATIVE", "Negative (Non-reactive)"),
                FieldOption("PAN_MALARIA", "Pan-Malaria Mixed Species")
            ),
            sortOrder = 5
        ),
        Variable(
            name = "parasite_density_ul",
            label = "Parasite Density (asexual / μL)",
            type = "integer",
            min = 0.0,
            max = 500000.0,
            sortOrder = 6
        ),
        Variable(
            name = "bed_net_usage",
            label = "LLIN Insecticide Bed Net Use",
            type = "select",
            options = listOf(
                FieldOption("EVERY_NIGHT", "Used every single night"),
                FieldOption("MOST_NIGHTS", "Used most nights (>4/wk)"),
                FieldOption("OCCASIONALLY", "Occasionally (1-3/wk)"),
                FieldOption("NEVER", "No bed net owned/used")
            ),
            sortOrder = 7
        ),
        Variable(
            name = "act_treatment_dispensed",
            label = "Artemether-Lumefantrine Dispensed",
            type = "boolean",
            sortOrder = 8
        )
    )

    val malariaForm = Form(
        uuid = "form-malaria-surveillance",
        projectUuid = "proj-malaria-02",
        title = "Malaria Field Case Report & Microscopic Confirmation",
        status = "active",
        description = "Epidemiological monitoring form for rapid diagnostics and antimalarial stewardship.",
        sections = listOf(
            FormSection(
                id = "sec-m1",
                title = "1. Field Demographics & Clinical Presentation",
                description = "Village location and patient presenting symptomatology",
                variableNames = listOf("subject_code", "village_cluster", "temperature_celsius", "fever_reported")
            ),
            FormSection(
                id = "sec-m2",
                title = "2. Diagnostic Testing & Vector Prevention",
                description = "RDT, microscopy count, and bed net protection",
                variableNames = listOf("rdt_result", "parasite_density_ul", "bed_net_usage", "act_treatment_dispensed")
            )
        )
    )

    val malariaRecords = listOf(
        RecordItem(
            uuid = "rec-mal-101",
            projectUuid = "proj-malaria-02",
            formUuid = "form-malaria-surveillance",
            status = "complete",
            version = 1,
            createdAt = "2026-11-05 10:00",
            updatedAt = "2026-11-08 17:10",
            dirty = false,
            data = mapOf(
                "subject_code" to "MAL-KYZ-092",
                "village_cluster" to "KAYONZA_4",
                "temperature_celsius" to "38.9",
                "fever_reported" to "true",
                "rdt_result" to "POSITIVE",
                "parasite_density_ul" to "14800",
                "bed_net_usage" to "OCCASIONALLY",
                "act_treatment_dispensed" to "true"
            )
        ),
        RecordItem(
            uuid = "rec-mal-102",
            projectUuid = "proj-malaria-02",
            formUuid = "form-malaria-surveillance",
            status = "complete",
            version = 1,
            createdAt = "2026-11-07 14:15",
            updatedAt = "2026-11-09 11:20",
            dirty = false,
            data = mapOf(
                "subject_code" to "MAL-NYA-114",
                "village_cluster" to "NYAGATARE_1",
                "temperature_celsius" to "36.8",
                "fever_reported" to "false",
                "rdt_result" to "NEGATIVE",
                "parasite_density_ul" to "0",
                "bed_net_usage" to "EVERY_NIGHT",
                "act_treatment_dispensed" to "false"
            )
        ),
        RecordItem(
            uuid = "rec-mal-103",
            projectUuid = "proj-malaria-02",
            formUuid = "form-malaria-surveillance",
            status = "draft",
            version = 1,
            createdAt = "2026-11-12 09:00",
            updatedAt = "2026-11-12 11:30",
            dirty = true,
            data = mapOf(
                "subject_code" to "MAL-BUG-208",
                "village_cluster" to "BUGESERA_2",
                "temperature_celsius" to "39.4",
                "fever_reported" to "true",
                "rdt_result" to "POSITIVE",
                "parasite_density_ul" to "32000",
                "bed_net_usage" to "NEVER",
                "act_treatment_dispensed" to "true"
            )
        )
    )

    val cvdVariables = listOf(
        Variable(name = "registry_id", label = "Registry Patient ID", type = "text", required = true, sortOrder = 1),
        Variable(name = "patient_age", label = "Patient Age (years)", type = "integer", required = true, min = 18.0, max = 110.0, sortOrder = 2),
        Variable(name = "gender", label = "Biological Sex", type = "radio", options = listOf(FieldOption("F", "Female"), FieldOption("M", "Male")), sortOrder = 3),
        Variable(name = "systolic_bp", label = "Systolic BP (mmHg)", type = "integer", min = 70.0, max = 260.0, sortOrder = 4),
        Variable(name = "diastolic_bp", label = "Diastolic BP (mmHg)", type = "integer", min = 40.0, max = 160.0, sortOrder = 5),
        Variable(name = "fasting_glucose", label = "Fasting Blood Glucose (mg/dL)", type = "decimal", min = 40.0, max = 500.0, sortOrder = 6),
        Variable(name = "total_cholesterol", label = "Total Serum Cholesterol (mg/dL)", type = "decimal", min = 80.0, max = 450.0, sortOrder = 7),
        Variable(name = "smoking_status", label = "Tobacco Smoking History", type = "select", options = listOf(FieldOption("NEVER", "Never smoked"), FieldOption("FORMER", "Former smoker"), FieldOption("CURRENT", "Current active smoker")), sortOrder = 8),
        Variable(name = "hypertension_diagnosed", label = "Confirmed Hypertension", type = "boolean", sortOrder = 9)
    )

    val cvdForm = Form(
        uuid = "form-cvd-registry",
        projectUuid = "proj-cvd-03",
        title = "Cardiovascular Risk Factor Clinical Intake",
        status = "active",
        sections = listOf(
            FormSection("s-cvd-1", "1. Patient Demographics", "", listOf("registry_id", "patient_age", "gender")),
            FormSection("s-cvd-2", "2. Hemodynamics & Lipid Panel", "", listOf("systolic_bp", "diastolic_bp", "fasting_glucose", "total_cholesterol", "smoking_status", "hypertension_diagnosed"))
        )
    )

    val cvdRecords = listOf(
        RecordItem(
            uuid = "rec-cvd-301",
            projectUuid = "proj-cvd-03",
            formUuid = "form-cvd-registry",
            status = "complete",
            version = 1,
            createdAt = "2026-10-28 14:00",
            updatedAt = "2026-11-03 10:45",
            dirty = false,
            data = mapOf(
                "registry_id" to "CVD-2026-881",
                "patient_age" to "56",
                "gender" to "M",
                "systolic_bp" to "148",
                "diastolic_bp" to "92",
                "fasting_glucose" to "118.5",
                "total_cholesterol" to "224.0",
                "smoking_status" to "CURRENT",
                "hypertension_diagnosed" to "true"
            )
        ),
        RecordItem(
            uuid = "rec-cvd-302",
            projectUuid = "proj-cvd-03",
            formUuid = "form-cvd-registry",
            status = "complete",
            version = 1,
            createdAt = "2026-11-02 09:30",
            updatedAt = "2026-11-06 15:00",
            dirty = false,
            data = mapOf(
                "registry_id" to "CVD-2026-882",
                "patient_age" to "42",
                "gender" to "F",
                "systolic_bp" to "118",
                "diastolic_bp" to "74",
                "fasting_glucose" to "92.0",
                "total_cholesterol" to "178.0",
                "smoking_status" to "NEVER",
                "hypertension_diagnosed" to "false"
            )
        )
    )

    val sampleProjects = listOf(
        Project(
            uuid = "proj-mch-01",
            title = "Maternal & Child Health Study 2026",
            description = "Longitudinal multicenter cohort assessing maternal antenatal care adherence, gestational weight and neonatal birth metrics across regional hospitals.",
            role = UserRole.OWNER,
            status = "active",
            startDate = "2026-01-15",
            endDate = "2026-12-31",
            period = "2026 Multi-Site Cohort",
            recordsCount = 4,
            pendingCount = 2
        ),
        Project(
            uuid = "proj-malaria-02",
            title = "Malaria Surveillance Field Cohort",
            description = "Rapid field surveillance evaluating point-of-care RDTs, vector control net adoption, and antimalarial resistance profiles in vulnerable eastern basins.",
            role = UserRole.RESEARCHER,
            status = "active",
            startDate = "2026-03-01",
            endDate = "2027-02-28",
            period = "Phase II Surveillance",
            recordsCount = 3,
            pendingCount = 1
        ),
        Project(
            uuid = "proj-cvd-03",
            title = "Cardiovascular Risk Registry",
            description = "Community clinic registry tracking blood pressure control, lipid panels, diabetes biomarkers, and lifestyle intervention response rates.",
            role = UserRole.RESEARCHER,
            status = "active",
            startDate = "2025-06-01",
            endDate = "2026-08-30",
            period = "Primary Prevention Trial",
            recordsCount = 2,
            pendingCount = 0
        )
    )

    val sampleFiles = listOf(
        LocalFile(
            uuid = "fil-001",
            recordUuid = "rec-mch-001",
            projectUuid = "proj-mch-01",
            name = "Patient_Consent_MCH0041.pdf",
            mime = "application/pdf",
            sizeBytes = 245760,
            state = "downloaded"
        ),
        LocalFile(
            uuid = "fil-002",
            recordUuid = "rec-mch-001",
            projectUuid = "proj-mch-01",
            name = "Hematology_CBC_Panel.pdf",
            mime = "application/pdf",
            sizeBytes = 512000,
            state = "downloaded"
        ),
        LocalFile(
            uuid = "fil-003",
            recordUuid = "rec-mch-004",
            projectUuid = "proj-mch-01",
            name = "NICU_Transfer_Referral.pdf",
            mime = "application/pdf",
            sizeBytes = 184320,
            state = "pending_upload"
        )
    )
}
