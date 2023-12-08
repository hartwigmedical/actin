package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.CurationConfigValidationError
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire

data class IngestionResult(
    val curationValidationErrors: Set<CurationConfigValidationError>,
    val patientResults: List<PatientIngestionResult>
)

enum class PatientIngestionStatus {
    PASS,
    WARN_CURATION_REQUIRED,
    WARN_NO_QUESTIONNAIRE
}

data class PatientIngestionResult(
    val patientId: String,
    val status: PatientIngestionStatus,
    @Transient val clinicalRecord: ClinicalRecord,
    val curationResults: Set<CurationResult>
) {
    companion object {
        fun create(questionnaire: Questionnaire?, record: ClinicalRecord, warnings: List<CurationWarning>): PatientIngestionResult {
            return PatientIngestionResult(
                record.patientId(),
                status(questionnaire, warnings),
                record,
                warnings.groupBy { it.category.categoryName }.map { (categoryName, warnings) ->
                    CurationResult(
                        categoryName,
                        warnings.map { CurationRequirement(it.feedInput, it.message) }
                    )
                }.toSet()
            )
        }

        private fun status(questionnaire: Questionnaire?, warnings: List<CurationWarning>): PatientIngestionStatus {
            return if (questionnaire == null) {
                PatientIngestionStatus.WARN_NO_QUESTIONNAIRE
            } else if (warnings.isNotEmpty()) {
                PatientIngestionStatus.WARN_CURATION_REQUIRED
            } else {
                PatientIngestionStatus.PASS
            }
        }
    }
}

data class CurationRequirement(val feedInput: String, val message: String)
data class CurationResult(val categoryName: String, val requirements: List<CurationRequirement>)