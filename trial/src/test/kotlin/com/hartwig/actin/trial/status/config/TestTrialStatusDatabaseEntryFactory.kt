package com.hartwig.actin.trial.status.config

import com.hartwig.actin.trial.status.TrialStatus
import com.hartwig.actin.trial.status.TrialStatusEntry

object TestTrialStatusDatabaseEntryFactory {

    val MINIMAL = TrialStatusEntry(
        metcStudyID = "",
        studyStatus = TrialStatus.OPEN
    )

    fun createEntry(
        cohortId: String?,
        cohortParentId: String?,
        cohortStatus: TrialStatus?,
        cohortSlotsNumberAvailable: Int?
    ): TrialStatusEntry {
        return MINIMAL.copy(
            cohortId = cohortId,
            cohortParentId = cohortParentId,
            cohortStatus = cohortStatus,
            cohortSlotsNumberAvailable = cohortSlotsNumberAvailable,
        )
    }
}