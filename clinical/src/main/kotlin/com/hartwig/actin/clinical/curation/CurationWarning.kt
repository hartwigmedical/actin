package com.hartwig.actin.clinical.curation

data class CurationWarning(val patientId: String, val category: CurationCategory, val input: String, val message: String)

enum class CurationCategory(val sheet: String) {
    PRIMARY_TUMOR("Primary Tumor"),
    ADMISTRATION_ROUTE_TRANSLATION("Administration Route Translation"),
    COMPLICATION("Complication"),
    BLOOD_TRANSFUSION_TRANSLATION("Blood Transfusion Translation"),
    DOSAGE_UNIT_TRANSLATION("Dosage Unit Translation"),
    ECG("ECG"),
    INFECTION("Infection"),
    INTOLERANCE("Intolerance"),
    LABORATORY_TRANSLATION("Laboratory"),
    LESION_LOCATION("Lesion Location"),
    MEDICATION_DOSAGE("Medication Dosage"),
    MEDICATION_NAME("Medication Name"),
    MOLECULAR_TEST("Molecular Test"),
    NON_ONCOLOGICAL_HISTORY("Non Oncological History"),
    ONCOLOGICAL_HISTORY("Oncological History"),
    PERIOD_BETWEEN_UNIT_INTERPRETATION("Period Between Unit Interpretation"),
    SECOND_PRIMARY("Second Primary"),
    TOXICITY("Toxicity"),
    TOXICITY_TRANSLATION("Toxicity Translation")
}