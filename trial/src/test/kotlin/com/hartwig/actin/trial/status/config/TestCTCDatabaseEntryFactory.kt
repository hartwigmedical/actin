package com.hartwig.actin.trial.status.config

import com.hartwig.actin.trial.status.TrialStatusEntry

object TestCTCDatabaseEntryFactory {

    val MINIMAL = TrialStatusEntry(
        studyId = 0,
        studyMETC = "",
        studyAcronym = "",
        studyTitle = "",
        studyStatus = ""
    )

    fun createEntry(cohortId: Int?, cohortParentId: Int?, cohortStatus: String?, cohortSlotsNumberAvailable: Int?): TrialStatusEntry {
        return MINIMAL.copy(
            cohortId = cohortId,
            cohortParentId = cohortParentId,
            cohortStatus = cohortStatus,
            cohortSlotsNumberAvailable = cohortSlotsNumberAvailable,
        )
    }
}