package com.hartwig.actin.trial

import com.hartwig.actin.treatment.datamodel.Trial
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.trial.config.InclusionCriteriaReferenceConfig
import com.hartwig.actin.trial.config.TrialConfig
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.ctc.config.CTCDatabaseEntry

enum class TrialIngestionStatus {
    PASS,
    FAIL,
    WARN;

    companion object {
        fun from(
            ctcDatabaseValidation: CtcDatabaseValidation,
            trialValidationResult: TrialDatabaseValidation,
        ): TrialIngestionStatus {
            return if (ctcDatabaseValidation.hasErrors() || trialValidationResult.hasErrors()) FAIL else PASS
        }
    }
}

interface ValidationError<T> {
    val config: T
    val message: String
}

interface TrialValidationError : ValidationError<TrialConfig>

data class CTCDatabaseValidationError(override val config: CTCDatabaseEntry, override val message: String) :
    ValidationError<CTCDatabaseEntry>

data class CTCIgnoreValidationError(override val config: String, override val message: String) :
    ValidationError<String>

data class CTCUnmappedValidationError(override val config: Int, override val message: String) :
    ValidationError<Int>

data class TrialDatabaseValidation(
    val inclusionCriteriaValidationErrors: List<InclusionCriteriaValidationError>,
    val inclusionReferenceValidationErrors: List<InclusionReferenceValidationError>,
    val cohortDefinitionValidationErrors: List<CohortDefinitionValidationError>,
    val trialDefinitionValidationErrors: List<TrialDefinitionValidationError>
) {
    fun hasErrors(): Boolean {
        return (inclusionCriteriaValidationErrors +
                inclusionReferenceValidationErrors +
                cohortDefinitionValidationErrors +
                trialDefinitionValidationErrors).isEmpty()
    }
}

data class CtcDatabaseValidation(
    val trialDefinitionValidationErrors: List<TrialDefinitionValidationError>,
    val ctcDatabaseValidationErrors: List<CTCDatabaseValidationError>,
) {
    fun hasErrors(): Boolean {
        return (trialDefinitionValidationErrors + ctcDatabaseValidationErrors).isEmpty()
    }
}

data class InclusionCriteriaValidationError(
    override val config: InclusionCriteriaConfig,
    override val message: String
) : TrialValidationError

data class InclusionReferenceValidationError(
    override val config: InclusionCriteriaReferenceConfig,
    override val message: String
) : TrialValidationError

data class CohortDefinitionValidationError(
    override val config: CohortDefinitionConfig,
    override val message: String
) : TrialValidationError

data class TrialDefinitionValidationError(
    override val config: TrialDefinitionConfig,
    override val message: String
) : TrialValidationError


data class TrialIngestionResult(
    val ingestionStatus: TrialIngestionStatus,
    val ctcDatabaseValidation: CtcDatabaseValidation,
    val trialValidationResult: TrialDatabaseValidation,
    @Transient val trials: List<Trial>
)