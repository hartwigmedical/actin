package com.hartwig.actin.datamodel.clinical.ingestion

import java.time.LocalDate

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
    val patientRegistrationDate: LocalDate,
    val status: PatientIngestionStatus,
    val curationResults: Set<CurationResult>,
    val questionnaireCurationErrors: Set<QuestionnaireCurationError>,
    val feedValidationWarnings: Set<FeedValidationWarning>
)

data class CurationWarning(val patientId: String, val category: CurationCategory, val feedInput: String, val message: String)

data class CurationRequirement(val feedInput: String, val message: String)

data class CurationResult(val category: CurationCategory, val requirements: List<CurationRequirement>) : Comparable<CurationResult> {

    override fun compareTo(other: CurationResult): Int {
        return Comparator.comparing(CurationResult::category)
            .thenComparing({ it.requirements.size }, Int::compareTo)
            .compare(this, other)
    }
}

data class CurationConfigValidationError(
    val category: CurationCategory,
    val input: String,
    val fieldName: String,
    val invalidValue: String,
    val validType: String,
    val additionalMessage: String? = null
) : Comparable<CurationConfigValidationError> {

    override fun compareTo(other: CurationConfigValidationError): Int {
        return Comparator.comparing(CurationConfigValidationError::category)
            .thenComparing(CurationConfigValidationError::fieldName)
            .thenComparing(CurationConfigValidationError::input)
            .thenComparing(CurationConfigValidationError::invalidValue)
            .thenComparing(CurationConfigValidationError::validType)
            .compare(this, other)
    }
}

data class UnusedCurationConfig(val category: CurationCategory, val input: String) : Comparable<UnusedCurationConfig> {

    override fun compareTo(other: UnusedCurationConfig): Int {
        return Comparator.comparing(UnusedCurationConfig::category)
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

data class FeedValidationWarning(val subject: String, val message: String, val registrationDate: LocalDate? = null) : Comparable<FeedValidationWarning> {

    override fun compareTo(other: FeedValidationWarning): Int {
        return Comparator.comparing(FeedValidationWarning::subject)
            .thenComparing(FeedValidationWarning::message)
            .compare(this, other)
    }
}