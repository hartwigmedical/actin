package com.hartwig.actin.clinical.curation

data class CurationWarning(val patientId: String, val category: CurationCategory, val feedInput: String, val message: String)

enum class CurationCategory(val categoryName: String) {
    PRIMARY_TUMOR("Primary Tumor"),
    ADMINISTRATION_ROUTE_TRANSLATION("Administration Route Translation"),
    BLOOD_TRANSFUSION_TRANSLATION("Blood Transfusion Translation"),
    COMPLICATION("Complication"),
    CYP_INTERACTIONS("CYP Interactions"),
    QT_PROLONGATING("QT Prolongating"),
    DOSAGE_UNIT_TRANSLATION("Dosage Unit Translation"),
    ECG("ECG"),
    INFECTION("Infection"),
    INTOLERANCE("Intolerance"),
    LABORATORY_TRANSLATION("Laboratory Translation"),
    LESION_LOCATION("Lesion Location"),
    MEDICATION_DOSAGE("Medication Dosage"),
    MEDICATION_NAME("Medication Name"),
    MOLECULAR_TEST_IHC("Molecular Test IHC"),
    MOLECULAR_TEST_PDL1("Molecular Test PDL1"),
    SEQUENCING_TEST("Sequencing Test"),
    NON_ONCOLOGICAL_HISTORY("Non Oncological History"),
    ONCOLOGICAL_HISTORY("Oncological History"),
    PERIOD_BETWEEN_UNIT_INTERPRETATION("Period Between Unit Interpretation"),
    QUESTIONNAIRE_MAPPING("Questionnaire mapping"),
    SECOND_PRIMARY("Second Primary"),
    TOXICITY("Toxicity"),
    TOXICITY_TRANSLATION("Toxicity Translation")
}