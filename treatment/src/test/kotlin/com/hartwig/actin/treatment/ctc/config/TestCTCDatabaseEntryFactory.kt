package com.hartwig.actin.treatment.ctc.config

object TestCTCDatabaseEntryFactory {

    val MINIMAL = CTCDatabaseEntry(
        studyId = 0,
        studyMETC = "",
        studyAcronym = "",
        studyTitle = "",
        studyStatus = ""
    )

    fun createEntry(cohortId: Int?, cohortParentId: Int?, cohortStatus: String?, cohortSlotsNumberAvailable: Int?): CTCDatabaseEntry {
        return MINIMAL.copy(
            cohortId = cohortId,
            cohortParentId = cohortParentId,
            cohortStatus = cohortStatus,
            cohortSlotsNumberAvailable = cohortSlotsNumberAvailable,
        )
    }
}