package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.ValidationError

data class TrialStatusDatabaseValidation(
    val trialStatusConfigValidationErrors: List<TrialStatusConfigValidationError> = emptyList(),
    val trialStatusDatabaseValidationErrors: List<TrialStatusDatabaseValidationError> = emptyList()
) {
    fun hasErrors(): Boolean {
        return (trialStatusConfigValidationErrors + trialStatusDatabaseValidationErrors).isNotEmpty()
    }
}

data class TrialStatusConfigValidationError(override val config: String, override val message: String) : ValidationError<String> {
    override fun configFormat(config: String): String {
        return "id=${config}"
    }
}

data class TrialStatusDatabaseValidationError(
    override val config: CohortStatusEntry, override val message: String
) : ValidationError<CohortStatusEntry> {
    override fun configFormat(config: CohortStatusEntry): String {
        return "METC=${config.nctId} cohort=${config.cohortId}"
    }
}
