package com.hartwig.actin.trial.config

import com.hartwig.actin.trial.TrialValidationError
import com.hartwig.actin.trial.ValidationError

data class TrialConfigDatabaseValidation(
    val trialDefinitionValidationErrors: Set<TrialDefinitionValidationError>,
    val cohortDefinitionValidationErrors: Set<CohortDefinitionValidationError>,
    val inclusionCriteriaValidationErrors: Set<InclusionCriteriaValidationError>,
    val inclusionCriteriaReferenceValidationErrors: Set<InclusionCriteriaReferenceValidationError>,
    val unusedRuleToKeepValidationErrors: Set<UnusedRuleToKeepValidationError>
) {
    fun hasErrors(): Boolean {
        return listOf(
            trialDefinitionValidationErrors,
            cohortDefinitionValidationErrors,
            inclusionCriteriaValidationErrors,
            inclusionCriteriaReferenceValidationErrors,
            unusedRuleToKeepValidationErrors
        ).any { it.isNotEmpty() }
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

data class CohortDefinitionValidationError(
    override val config: CohortDefinitionConfig,
    override val message: String
) : TrialValidationError<CohortDefinitionConfig> {
    override fun configFormat(config: CohortDefinitionConfig): String {
        return "trial id=${config.trialId} cohort id=${config.cohortId}"
    }
}

data class InclusionCriteriaValidationError(
    override val config: InclusionCriteriaConfig,
    override val message: String
) : TrialValidationError<InclusionCriteriaConfig> {
    override fun configFormat(config: InclusionCriteriaConfig): String {
        return "trial id=${config.trialId} rule=${config.inclusionRule}"
    }
}

data class InclusionCriteriaReferenceValidationError(
    override val config: InclusionCriteriaReferenceConfig,
    override val message: String
) : TrialValidationError<InclusionCriteriaReferenceConfig> {
    override fun configFormat(config: InclusionCriteriaReferenceConfig): String {
        return "trial id=${config.trialId} reference id=${config.referenceId}"
    }
}

data class UnusedRuleToKeepValidationError(
    override val config: String
) : ValidationError<String> {
    override val message = config

    override fun configFormat(config: String): String {
        return "Unrecognized rule to keep"
    }
}