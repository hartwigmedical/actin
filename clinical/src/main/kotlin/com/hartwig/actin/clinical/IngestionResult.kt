package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire

enum class IngestionStatus {
    PASS,
    WARN_CURATION_REQUIRED,
    WARN_NO_QUESTIONNAIRE
}

data class CurationResult(val categoryName: String, val input: List<CurationWarning>)

data class IngestionResult(
    val patientId: String,
    val curationQCStatus: IngestionStatus,
    @Transient val clinicalRecord: ClinicalRecord,
    val curationResults: List<CurationResult>
) {
    companion object {
        fun create(questionnaire: Questionnaire?, record: ClinicalRecord, warnings: List<CurationWarning>): IngestionResult {
            return IngestionResult(
                record.patientId(),
                status(questionnaire, warnings),
                record,
                warnings.groupBy { it.category.categoryName }.map {
                    CurationResult(it.key, it.value)
                })
        }

        private fun status(questionnaire: Questionnaire?, warnings: List<CurationWarning>): IngestionStatus {
            return if (questionnaire == null) {
                IngestionStatus.WARN_NO_QUESTIONNAIRE
            } else if (warnings.isNotEmpty()) {
                IngestionStatus.WARN_CURATION_REQUIRED
            } else {
                IngestionStatus.PASS
            }
        }
    }
}