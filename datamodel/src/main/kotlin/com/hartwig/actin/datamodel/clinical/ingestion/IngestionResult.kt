package com.hartwig.actin.datamodel.clinical.ingestion

import com.hartwig.actin.datamodel.clinical.ClinicalRecord

data class IngestionResult(
    val configValidationErrors: Set<CurationConfigValidationError> = emptySet(),
    val patientResults: List<PatientIngestionResult> = emptyList(),
    val unusedConfigs: Set<UnusedCurationConfig> = emptySet()
)

enum class PatientIngestionStatus {
    PASS,
    WARN
}

data class PatientIngestionResult(
    val patientId: String,
    val status: PatientIngestionStatus,
    @Transient val clinicalRecord: ClinicalRecord,
    val curationResults: Set<CurationResult>,
    val questionnaireCurationErrors: Set<QuestionnaireCurationError>,
    val feedValidationWarnings: Set<FeedValidationWarning>
)

data class CurationWarning(val patientId: String, val category: CurationCategory, val feedInput: String, val message: String)

data class CurationRequirement(val feedInput: String, val message: String)

data class CurationResult(val categoryName: String, val requirements: List<CurationRequirement>) : Comparable<CurationResult> {

    override fun compareTo(other: CurationResult): Int {
        return Comparator.comparing(CurationResult::categoryName)
            .thenComparing({ it.requirements.size }, Int::compareTo)
            .compare(this, other)
    }
}

data class CurationConfigValidationError(
    val categoryName: String,
    val input: String,
    val fieldName: String,
    val invalidValue: String,
    val validType: String,
    val additionalMessage: String? = null
) : Comparable<CurationConfigValidationError> {

    override fun compareTo(other: CurationConfigValidationError): Int {
        return Comparator.comparing(CurationConfigValidationError::categoryName)
            .thenComparing(CurationConfigValidationError::fieldName)
            .thenComparing(CurationConfigValidationError::input)
            .thenComparing(CurationConfigValidationError::invalidValue)
            .thenComparing(CurationConfigValidationError::validType)
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

data class QuestionnaireCurationError(val subject: String, val message: String) : Comparable<QuestionnaireCurationError> {

    override fun compareTo(other: QuestionnaireCurationError): Int {
        return Comparator.comparing(QuestionnaireCurationError::subject)
            .thenComparing(QuestionnaireCurationError::message)
            .compare(this, other)
    }
}

data class FeedValidationWarning(val subject: String, val message: String) : Comparable<FeedValidationWarning> {

    override fun compareTo(other: FeedValidationWarning): Int {
        return Comparator.comparing(FeedValidationWarning::subject)
            .thenComparing(FeedValidationWarning::message)
            .compare(this, other)
    }
}