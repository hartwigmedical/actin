package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.ValidationError
import com.hartwig.actin.trial.config.TrialDefinitionValidationError

data class TrialStatusDatabaseValidationError(
    override val config: TrialStatusEntry, override val message: String
) : ValidationError<TrialStatusEntry> {
    override fun configFormat(config: TrialStatusEntry): String {
        return "METC=${config.studyMETC} cohort=${config.cohortName}"
    }
}

data class IgnoreValidationError(override val config: String, override val message: String) : ValidationError<String> {
    override fun configFormat(config: String): String {
        return "METC=${config}"
    }
}

data class TrialStatusUnmappedValidationError(override val config: Int, override val message: String) : ValidationError<Int> {
    override fun configFormat(config: Int): String {
        return "cohort id=${config}"
    }
}

data class TrialStatusDatabaseValidation(
    val trialDefinitionValidationErrors: List<TrialDefinitionValidationError>,
    val ctcDatabaseValidationErrors: List<TrialStatusDatabaseValidationError>,
) {
    fun hasErrors(): Boolean {
        return (trialDefinitionValidationErrors + ctcDatabaseValidationErrors).isNotEmpty()
    }
}