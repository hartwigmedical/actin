package com.hartwig.actin.trial.status

data class TrialStatusEntry(
    val studyId: Int,
    val metcStudyID: String,
    val studyAcronym: String?,
    val studyTitle: String?,
    val studyStatus: TrialStatus,
    val cohortId: String? = null,
    val cohortParentId: String? = null,
    val cohortName: String? = null,
    val cohortStatus: TrialStatus? = null,
    val cohortSlotsNumberAvailable: Int? = null,
    val cohortSlotsDateUpdate: String? = null
)