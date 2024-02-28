package com.hartwig.actin.trial

import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.trial.config.InclusionCriteriaReferenceConfig
import com.hartwig.actin.trial.config.TrialConfig
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.ctc.config.CTCDatabaseEntry
import com.hartwig.actin.trial.datamodel.Trial

enum class TrialIngestionStatus {
    PASS,
    FAIL;

    companion object {
        fun from(
            ctcDatabaseValidation: CtcDatabaseValidation,
            trialValidationResult: TrialDatabaseValidation,
        ): TrialIngestionStatus {
            return if (ctcDatabaseValidation.hasErrors() || trialValidationResult.hasErrors()) FAIL else PASS
        }
    }
}

interface ValidationError<T> : Comparable<ValidationError<T>> {
    val config: T
    val message: String

    fun configFormat(config: T): String
    fun warningMessage(): String {
        return "${this::class.java.simpleName} ${configFormat(config)}: $message"
    }

    override fun compareTo(other: ValidationError<T>): Int {
        return this.message.compareTo(other.message)
    }
}

interface TrialValidationError<T : TrialConfig> : ValidationError<T>

data class CTCDatabaseValidationError(
    override val config: CTCDatabaseEntry, override val message: String
) : ValidationError<CTCDatabaseEntry> {
    override fun configFormat(config: CTCDatabaseEntry): String {
        return "METC=${config.studyMETC} cohort=${config.cohortName}"
    }
}

data class CTCIgnoreValidationError(override val config: String, override val message: String) : ValidationError<String> {
    override fun configFormat(config: String): String {
        return "METC=${config}"
    }
}

data class CTCUnmappedValidationError(override val config: Int, override val message: String) : ValidationError<Int> {
    override fun configFormat(config: Int): String {
        return "cohort id=${config}"
    }
}

data class TrialDatabaseValidation(
    val inclusionCriteriaValidationErrors: Set<InclusionCriteriaValidationError>,
    val inclusionReferenceValidationErrors: Set<InclusionReferenceValidationError>,
    val cohortDefinitionValidationErrors: Set<CohortDefinitionValidationError>,
    val trialDefinitionValidationErrors: Set<TrialDefinitionValidationError>,
    val unusedRulesToKeepErrors: Set<UnusedRuleToKeepError>
) {
    fun hasErrors(): Boolean {
        return listOf(
            inclusionCriteriaValidationErrors,
            inclusionReferenceValidationErrors,
            cohortDefinitionValidationErrors,
            trialDefinitionValidationErrors,
            unusedRulesToKeepErrors
        ).any { it.isNotEmpty() }
    }
}

data class CtcDatabaseValidation(
    val trialDefinitionValidationErrors: List<TrialDefinitionValidationError>,
    val ctcDatabaseValidationErrors: List<CTCDatabaseValidationError>,
) {
    fun hasErrors(): Boolean {
        return (trialDefinitionValidationErrors + ctcDatabaseValidationErrors).isNotEmpty()
    }
}

data class InclusionCriteriaValidationError(
    override val config: InclusionCriteriaConfig,
    override val message: String
) : TrialValidationError<InclusionCriteriaConfig> {
    override fun configFormat(config: InclusionCriteriaConfig): String {
        return "trial id=${config.trialId} cohorts=${config.appliesToCohorts}"
    }
}

data class InclusionReferenceValidationError(
    override val config: InclusionCriteriaReferenceConfig,
    override val message: String
) : TrialValidationError<InclusionCriteriaReferenceConfig> {
    override fun configFormat(config: InclusionCriteriaReferenceConfig): String {
        return "trial id=${config.trialId} reference id=${config.referenceId}"
    }
}

data class CohortDefinitionValidationError(
    override val config: CohortDefinitionConfig,
    override val message: String
) : TrialValidationError<CohortDefinitionConfig> {
    override fun configFormat(config: CohortDefinitionConfig): String {
        return "trial id=${config.trialId} cohort id=${config.cohortId}"
    }
}

data class TrialDefinitionValidationError(
    override val config: TrialDefinitionConfig,
    override val message: String
) : TrialValidationError<TrialDefinitionConfig> {
    override fun configFormat(config: TrialDefinitionConfig): String {
        return "trial id=${config.trialId}"
    }
}

data class UnusedRuleToKeepError(
    override val config: String
) : ValidationError<String> {
    override val message = config

    override fun configFormat(config: String): String {
        return "Unrecognized rule to keep"
    }
}

data class TrialIngestionResult(
    val ingestionStatus: TrialIngestionStatus,
    val ctcDatabaseValidation: CtcDatabaseValidation,
    val trialValidationResult: TrialDatabaseValidation,
    @Transient val trials: List<Trial>
)