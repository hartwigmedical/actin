package com.hartwig.actin.datamodel.clinical.ingestion

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
)

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