package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.CurationConfigValidationError
import com.hartwig.actin.clinical.feed.emc.FeedValidationWarning
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCurationError
import com.hartwig.actin.datamodel.clinical.ClinicalRecord

data class IngestionResult(
    val configValidationErrors: Set<CurationConfigValidationError> = emptySet(),
    val patientResults: List<PatientIngestionResult> = emptyList(),
    val unusedConfigs: Set<UnusedCurationConfig> = emptySet()
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
    val curationResults: Set<CurationResult>,
    val questionnaireCurationErrors: Set<QuestionnaireCurationError>,
    val feedValidationWarnings: Set<FeedValidationWarning>
) {

    companion object {
        fun create(
            questionnaire: Questionnaire?,
            record: ClinicalRecord,
            warnings: List<CurationWarning>,
            questionnaireCurationErrors: Set<QuestionnaireCurationError>,
            feedValidationWarnings: Set<FeedValidationWarning>
        ): PatientIngestionResult {
            return PatientIngestionResult(
                record.patientId,
                status(questionnaire, warnings),
                record,
                curationResults(warnings),
                questionnaireCurationErrors,
                feedValidationWarnings
            )
        }

        fun curationResults(warnings: List<CurationWarning>): Set<CurationResult> {
            return warnings.groupBy { it.category.categoryName }.map { (categoryName, warnings) ->
                CurationResult(
                    categoryName,
                    warnings.map { CurationRequirement(it.feedInput, it.message) }
                )
            }.toSet()
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

data class CurationResult(val categoryName: String, val requirements: List<CurationRequirement>) : Comparable<CurationResult> {

    override fun compareTo(other: CurationResult): Int {
        return Comparator.comparing(CurationResult::categoryName)
            .thenComparing({ it.requirements.size }, Int::compareTo)
            .compare(this, other)
    }
}

data class UnusedCurationConfig(val categoryName: String, val input: String) : Comparable<UnusedCurationConfig> {

    override fun compareTo(other: UnusedCurationConfig): Int {
        return Comparator.comparing(UnusedCurationConfig::categoryName)
            .thenComparing(UnusedCurationConfig::input)
            .compare(this, other)
    }
}