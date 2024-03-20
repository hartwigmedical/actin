package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.config.CohortDefinitionConfig
import org.apache.logging.log4j.LogManager

internal object CohortStatusInterpreter {

    private val LOGGER = LogManager.getLogger(CohortStatusInterpreter::class.java)

    const val NOT_AVAILABLE = "NA"
    const val NOT_IN_TRIAL_STATUS_DATABASE_OVERVIEW_UNKNOWN_WHY = "not_in_trial_status_database_overview_unknown_why"
    const val OVERRULED_BECAUSE_INCORRECT_IN_TRIAL_STATUS_DATABASE = "overruled_because_incorrect_in_trial_status_database"
    const val WONT_BE_MAPPED_BECAUSE_CLOSED = "wont_be_mapped_because_closed"
    const val WONT_BE_MAPPED_BECAUSE_NOT_AVAILABLE = "wont_be_mapped_because_not_available"

    fun interpret(
        entries: List<TrialStatusEntry>,
        cohortConfig: CohortDefinitionConfig
    ): CohortStatusInterpretation {
        val ctcCohortIds = cohortConfig.externalCohortIds
        if (isNotAvailableOrIncorrect(ctcCohortIds)) {
            LOGGER.debug(
                " Trial status entry for cohort '{}' of trial '{}' explicitly configured to be unavailable or incorrect in trial status database. "
                        + "Ingesting cohort status as configured",
                cohortConfig.cohortId,
                cohortConfig.trialId
            )
            return CohortStatusInterpretation(null, emptyList(), emptyList())
        } else if (isMissingBecauseClosedOrUnavailable(ctcCohortIds)) {
            LOGGER.debug(
                " Trial status entry missing for cohort '{}' of trial '{}' because it's assumed closed or not available. "
                        + "Setting cohort to closed without slots", cohortConfig.cohortId, cohortConfig.trialId
            )
            return CohortStatusInterpretation(closedWithoutSlots(), emptyList(), emptyList())
        }

        return CohortStatusResolver.resolve(entries, cohortConfig)
    }

    private fun isNotAvailableOrIncorrect(ctcCohortIds: Set<String>): Boolean {
        return isSingleEntryWithValue(
            ctcCohortIds,
            NOT_AVAILABLE,
            NOT_IN_TRIAL_STATUS_DATABASE_OVERVIEW_UNKNOWN_WHY,
            OVERRULED_BECAUSE_INCORRECT_IN_TRIAL_STATUS_DATABASE
        )
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