package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.TrialDefinitionValidationError
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.ctc.config.CTCDatabaseEntry
import org.apache.logging.log4j.LogManager

internal object TrialStatusInterpreter {

    fun isOpen(
        entries: List<CTCDatabaseEntry>,
        trialDefinitionConfig: TrialDefinitionConfig
    ): Pair<Boolean?, TrialDefinitionValidationError?> {
        val trialId = trialDefinitionConfig.trialId
        val trialStates = entries.filter { trialId.equals(CTCModel.constructTrialId(it), ignoreCase = true) }
            .map { CTCStatus.fromStatusString(it.studyStatus) }
            .distinct()
        if (trialStates.size > 1) {
            return false to TrialDefinitionValidationError(trialDefinitionConfig, "Inconsistent study status found in CTC database")
        } else if (trialStates.size == 1) {
            return (trialStates.first() == CTCStatus.OPEN) to null
        }
        return null to null
    }
}