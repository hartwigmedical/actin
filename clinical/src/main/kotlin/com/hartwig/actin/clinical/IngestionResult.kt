package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.datamodel.ClinicalRecord

enum class IngestionStatus {
    PASS,
    WARN_CURATION_REQUIRED,
    WARN_NO_QUESTIONNAIRE
}

data class IngestionResult(
    val patientId: String,
    val curationQCStatus: IngestionStatus,
    @Transient val clinicalRecord: ClinicalRecord,
    val warnings: List<String>
)