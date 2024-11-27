package com.hartwig.actin.trial.status

data class TrialStatusEntry(
    val metcStudyID: String,
    val studyStatus: TrialStatus,
    val cohortId: String? = null,
    val cohortParentId: String? = null,
    val cohortStatus: TrialStatus? = null,
    val cohortSlotsNumberAvailable: Int? = null,
    val cohortSlotsDateUpdate: String? = null
)