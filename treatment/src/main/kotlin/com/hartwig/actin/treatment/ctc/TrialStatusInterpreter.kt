package com.hartwig.actin.treatment.ctc

import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig
import org.apache.logging.log4j.LogManager

internal object TrialStatusInterpreter {
    private val LOGGER = LogManager.getLogger(TrialStatusInterpreter::class.java)

    fun isOpen(entries: List<CTCDatabaseEntry>, trialConfig: TrialDefinitionConfig): Boolean? {
        val trialStates = entries.filter { trialConfig.trialId.equals(CTCModel.extractTrialId(it), ignoreCase = true) }
            .map { CTCStatus.fromStatusString(it.studyStatus) }
            .distinct()
        if (trialStates.size > 1) {
            LOGGER.warn("Inconsistent study status found for trial '{}' in CTC database. Assuming trial is closed", trialConfig.trialId)
            return false
        } else if (trialStates.size == 1) {
            return trialStates.first() == CTCStatus.OPEN
        }
        return null
    }
}