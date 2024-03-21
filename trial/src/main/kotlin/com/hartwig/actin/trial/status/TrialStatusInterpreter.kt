package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionValidationError

internal object TrialStatusInterpreter {

    fun isOpen(
        entries: List<TrialStatusEntry>,
        trialDefinitionConfig: TrialDefinitionConfig,
        trialIdConstructor: (TrialStatusEntry) -> String,
    ): Pair<Boolean?, List<TrialDefinitionValidationError>> {
        val trialId = trialDefinitionConfig.trialId
        val trialStates = entries.filter { trialId.equals(trialIdConstructor.invoke(it), ignoreCase = true) }
            .map { it.studyStatus }
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