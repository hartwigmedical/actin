package com.hartwig.actin.treatment.ctc

import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig
import org.apache.logging.log4j.LogManager

internal object CohortStatusInterpreter {
    private val LOGGER = LogManager.getLogger(CohortStatusInterpreter::class.java)
    const val NOT_AVAILABLE = "NA"
    const val NOT_IN_CTC_OVERVIEW_UNKNOWN_WHY = "not_in_ctc_overview_unknown_why"
    const val WONT_BE_MAPPED_BECAUSE_CLOSED = "wont_be_mapped_because_closed"
    const val WONT_BE_MAPPED_BECAUSE_NOT_AVAILABLE = "wont_be_mapped_because_not_available"

    fun interpret(entries: List<CTCDatabaseEntry>, cohortConfig: CohortDefinitionConfig): InterpretedCohortStatus? {
        val ctcCohortIds: Set<String> = cohortConfig.ctcCohortIds
        if (isNotAvailable(ctcCohortIds)) {
            LOGGER.debug(
                "CTC entry for cohort '{}' of trial '{}' explicitly configured to be unavailable",
                cohortConfig.cohortId,
                cohortConfig.trialId
            )
            return null
        } else if (isMissingEntry(ctcCohortIds)) {
            LOGGER.info(
                "CTC entry missing for unknown reason for cohort '{}' of trial '{}'! Setting cohort to closed without slots",
                cohortConfig.cohortId,
                cohortConfig.trialId
            )
            return closedWithoutSlots()
        } else if (isMissingBecauseClosedOrUnavailable(ctcCohortIds)) {
            LOGGER.debug(
                "CTC entry missing for cohort '{}' of trial '{}' because it's assumed closed or not available. "
                        + "Setting cohort to closed without slots", cohortConfig.cohortId, cohortConfig.trialId
            )
            return closedWithoutSlots()
        }
        return CTCDatabaseEntryInterpreter.consolidatedCohortStatus(entries, cohortConfig)
    }

    private fun isNotAvailable(ctcCohortIds: Set<String>): Boolean {
        return isSingleEntryWithValue(ctcCohortIds, NOT_AVAILABLE)
    }

    private fun isMissingEntry(ctcCohortIds: Set<String>): Boolean {
        return isSingleEntryWithValue(ctcCohortIds, NOT_IN_CTC_OVERVIEW_UNKNOWN_WHY)
    }

    private fun isMissingBecauseClosedOrUnavailable(ctcCohortIds: Set<String>): Boolean {
        return isSingleEntryWithValue(ctcCohortIds, WONT_BE_MAPPED_BECAUSE_CLOSED, WONT_BE_MAPPED_BECAUSE_NOT_AVAILABLE)
    }

    private fun isSingleEntryWithValue(ctcCohortIds: Set<String>, vararg valuesToFind: String): Boolean {
        return ctcCohortIds.size == 1 && ctcCohortIds.first() in valuesToFind
    }

    private fun closedWithoutSlots(): InterpretedCohortStatus {
        return InterpretedCohortStatus(open = false, slotsAvailable = false)
    }
}