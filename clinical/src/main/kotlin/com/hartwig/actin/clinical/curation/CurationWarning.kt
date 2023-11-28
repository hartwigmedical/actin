package com.hartwig.actin.clinical.curation

enum class CurationWarningType { NOT_FOUND, MULTIPLE_FOUND }

data class CurationWarningTemplate(val input: String, val type: CurationWarningType) {

    fun toCurationWarning(patientId: String, category: CurationCategory, configType: String): CurationWarning {
        val message = when (type) {
            CurationWarningType.NOT_FOUND -> "Could not find $configType config for input '$input'"
            CurationWarningType.MULTIPLE_FOUND -> "Multiple $configType configs found for input '$input'"
        }
        return CurationWarning(patientId, category, input, message)
    }
}

data class CurationWarning(val patientId: String, val category: CurationCategory, val feedInput: String, val message: String)

enum class CurationCategory(val categoryName: String) {
    PRIMARY_TUMOR("Primary Tumor"),
    ADMINISTRATION_ROUTE_TRANSLATION("Administration Route Translation"),
    BLOOD_TRANSFUSION_TRANSLATION("Blood Transfusion Translation"),
    COMPLICATION("Complication"),
    CYP_INTERACTION("CYP Interaction"),
    QT_PROLONGATION("QT Prolongation"),
    DOSAGE_UNIT_TRANSLATION("Dosage Unit Translation"),
    ECG("ECG"),
    INFECTION("Infection"),
    INTOLERANCE("Intolerance"),
    LABORATORY_TRANSLATION("Laboratory Translation"),
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