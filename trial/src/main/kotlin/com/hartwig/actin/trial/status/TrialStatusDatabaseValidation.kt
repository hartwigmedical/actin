package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.ValidationError

data class TrialStatusDatabaseValidation(
    val trialStatusConfigValidationErrors: List<TrialStatusConfigValidationError>,
    val trialStatusDatabaseValidationErrors: List<TrialStatusDatabaseValidationError>,
) {
    fun hasErrors(): Boolean {
        return (trialStatusConfigValidationErrors + trialStatusDatabaseValidationErrors).isNotEmpty()
    }
}

data class TrialStatusConfigValidationError(override val config: String, override val message: String) : ValidationError<String> {
    override fun configFormat(config: String): String {
        return "trial id=${config}"
    }
}

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
