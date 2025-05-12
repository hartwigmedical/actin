package com.hartwig.actin.datamodel.clinical.ingestion

enum class CurationCategory(val categoryName: String) {
    PRIMARY_TUMOR("Primary Tumor"),
    ADMINISTRATION_ROUTE_TRANSLATION("Administration Route Translation"),
    BLOOD_TRANSFUSION_TRANSLATION("Blood Transfusion Translation"),
    COMORBIDITY("Comorbidity"),
    COMPLICATION("Complication"),
    DOSAGE_UNIT_TRANSLATION("Dosage Unit Translation"),
    ECG("ECG"),
    INFECTION("Infection"),
    INTOLERANCE("Intolerance"),
    LAB_MEASUREMENT("Lab Measurement"),
    LESION_LOCATION("Lesion Location"),
    MEDICATION_DOSAGE("Medication Dosage"),
    MEDICATION_NAME("Medication Name"),
    MOLECULAR_TEST_IHC("Molecular Test IHC"),
    MOLECULAR_TEST_PDL1("Molecular Test PDL1"),
    SEQUENCING_TEST("Sequencing Test"),
    SEQUENCING_TEST_RESULT("Sequencing Test Result"),
    NON_ONCOLOGICAL_HISTORY("Non Oncological History"),
    ONCOLOGICAL_HISTORY("Oncological History"),
    PERIOD_BETWEEN_UNIT_INTERPRETATION("Period Between Unit Interpretation"),
    QUESTIONNAIRE_MAPPING("Questionnaire mapping"),
    PRIOR_PRIMARY("Prior Primary"),
    TOXICITY("Toxicity"),
    TOXICITY_TRANSLATION("Toxicity Translation"),
    SURGERY_NAME("Surgery Name")
}