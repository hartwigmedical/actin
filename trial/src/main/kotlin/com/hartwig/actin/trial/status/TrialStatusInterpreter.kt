package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionValidationError

internal object TrialStatusInterpreter {

    fun isOpen(
        entries: List<CohortStatusEntry>,
        trialDefinitionConfig: TrialDefinitionConfig,
        trialIdConstructor: (CohortStatusEntry) -> String,
    ): Pair<Boolean?, List<TrialDefinitionValidationError>> {
        val trialId = trialDefinitionConfig.nctId
        val trialStates = entries.filter {
            val other = trialIdConstructor.invoke(it).trim()
            trialId.equals(other, ignoreCase = true) }
            .map { it.trialStatus }
            .distinct()
        if (trialStates.size > 1) {
            return false to listOf(
                TrialDefinitionValidationError(
                    trialDefinitionConfig,
                    "Inconsistent study status found in trial status database"
                )
            )
        } else if (trialStates.size == 1) {
            return (trialStates.first() == TrialStatus.OPEN) to emptyList()
        }
        return null to emptyList()
    }
}