package com.hartwig.actin.trial.status.config

import com.hartwig.actin.trial.status.TrialStatus
import com.hartwig.actin.trial.status.TrialStatusEntry

object TestTrialStatusDatabaseEntryFactory {

    val MINIMAL = TrialStatusEntry(
        studyId = 0,
        studyMETC = "",
        studyAcronym = "",
        studyTitle = "",
        studyStatus = TrialStatus.OPEN
    )

    fun createEntry(cohortId: Int?, cohortParentId: Int?, cohortStatus: TrialStatus?, cohortSlotsNumberAvailable: Int?): TrialStatusEntry {
        return MINIMAL.copy(
            cohortId = cohortId,
            cohortParentId = cohortParentId,
            cohortStatus = cohortStatus,
            cohortSlotsNumberAvailable = cohortSlotsNumberAvailable,
        )
    }
}