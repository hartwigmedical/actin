package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.ctc.config.CTCDatabaseEntry
import org.apache.logging.log4j.LogManager

internal object TrialStatusInterpreter {

    private val LOGGER = LogManager.getLogger(TrialStatusInterpreter::class.java)

    fun isOpen(entries: List<CTCDatabaseEntry>, trialId: String): Boolean? {
        val trialStates = entries.filter { trialId.equals(CTCModel.constructTrialId(it), ignoreCase = true) }
            .map { CTCStatus.fromStatusString(it.studyStatus) }
            .distinct()
        if (trialStates.size > 1) {
            LOGGER.warn(" Inconsistent study status found for trial '{}' in CTC database. Assuming trial is closed", trialId)
            return false
        } else if (trialStates.size == 1) {
            return trialStates.first() == CTCStatus.OPEN
        }
        return null
    }
}