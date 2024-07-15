package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.ValidationError
import com.hartwig.actin.trial.config.TrialDefinitionValidationError

data class TrialStatusDatabaseValidationError(
    override val config: TrialStatusEntry, override val message: String
) : ValidationError<TrialStatusEntry> {
    override fun configFormat(config: TrialStatusEntry): String {
        return "METC=${config.metcStudyID} cohort=${config.cohortName}"
    }
}

data class IgnoreValidationError(override val config: String, override val message: String) : ValidationError<String> {
    override fun configFormat(config: String): String {
        return "METC=${config}"
    }
}

data class TrialStatusUnmappedValidationError(override val config: String, override val message: String) : ValidationError<String> {
    override fun configFormat(config: String): String {
        return "cohort id=${config}"
    }
}

data class TrialStatusDatabaseValidation(
    val trialDefinitionValidationErrors: List<TrialDefinitionValidationError>,
    val trialStatusDatabaseValidationErrors: List<TrialStatusDatabaseValidationError>,
) {
    fun hasErrors(): Boolean {
        return (trialDefinitionValidationErrors + trialStatusDatabaseValidationErrors).isNotEmpty()
    }
}

data class TrialStatusDatabaseConfigValidationError(override val config: String, override val message: String) : ValidationError<String> {
    override fun configFormat(config: String): String {
        return "trial id=${config}"
    }
}