package com.hartwig.actin.trial.status.config

import com.hartwig.actin.trial.status.CohortStatusEntry
import com.hartwig.actin.trial.status.TrialStatus

object TestTrialStatusDatabaseEntryFactory {

    val MINIMAL = CohortStatusEntry(
        nctId = "",
        trialStatus = TrialStatus.OPEN,
        cohortId = "",
        cohortStatus = TrialStatus.OPEN
    )

    fun createEntry(
        cohortId: String,
        cohortParentId: String?,
        cohortStatus: TrialStatus,
        cohortSlotsNumberAvailable: Int?
    ): CohortStatusEntry {
        return MINIMAL.copy(
            cohortId = cohortId,
            cohortParentId = cohortParentId,
            cohortStatus = cohortStatus,
            cohortSlotsNumberAvailable = cohortSlotsNumberAvailable,
        )
    }
}