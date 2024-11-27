package com.hartwig.actin.trial.config

import com.hartwig.actin.trial.TrialConfigValidationError
import com.hartwig.actin.trial.ValidationError

data class TrialConfigDatabaseValidation(
    val trialDefinitionValidationErrors: Set<TrialDefinitionValidationError> = emptySet(),
    val cohortDefinitionValidationErrors: Set<CohortDefinitionValidationError> = emptySet(),
    val inclusionCriteriaValidationErrors: Set<InclusionCriteriaValidationError> = emptySet(),
    val inclusionCriteriaReferenceValidationErrors: Set<InclusionCriteriaReferenceValidationError> = emptySet(),
    val unusedRulesToKeepValidationErrors: Set<UnusedRulesToKeepValidationError> = emptySet()
) {
    fun hasErrors(): Boolean {
        return listOf(
            trialDefinitionValidationErrors,
            cohortDefinitionValidationErrors,
            inclusionCriteriaValidationErrors,
            inclusionCriteriaReferenceValidationErrors,
            unusedRulesToKeepValidationErrors
        ).any { it.isNotEmpty() }
    }
}

data class TrialDefinitionValidationError(
    override val config: TrialDefinitionConfig,
    override val message: String
) : TrialConfigValidationError<TrialDefinitionConfig> {
    override fun configFormat(config: TrialDefinitionConfig): String {
        return "trial id=${config.nctId}"
    }
}

data class CohortDefinitionValidationError(
    override val config: CohortDefinitionConfig,
    override val message: String
) : TrialConfigValidationError<CohortDefinitionConfig> {
    override fun configFormat(config: CohortDefinitionConfig): String {
        return "trial id=${config.nctId} cohort id=${config.cohortId}"
    }
}

data class InclusionCriteriaValidationError(
    override val config: InclusionCriteriaConfig,
    override val message: String
) : TrialConfigValidationError<InclusionCriteriaConfig> {
    override fun configFormat(config: InclusionCriteriaConfig): String {
        return "trial id=${config.nctId} rule=${config.inclusionRule}"
    }
}

data class InclusionCriteriaReferenceValidationError(
    override val config: InclusionCriteriaReferenceConfig,
    override val message: String
) : TrialConfigValidationError<InclusionCriteriaReferenceConfig> {
    override fun configFormat(config: InclusionCriteriaReferenceConfig): String {
        return "trial id=${config.nctId} reference id=${config.referenceId}"
    }
}

data class UnusedRulesToKeepValidationError(
    override val config: String
) : ValidationError<String> {
    override val message = config

    override fun configFormat(config: String): String {
        return "Unrecognized rule to keep"
    }
}