package com.hartwig.actin.trial.status

data class TrialStatusEntry(
    val studyId: Int,
    val studyMETC: String,
    val studyAcronym: String,
    val studyTitle: String,
    val studyStatus: TrialStatus,
    val cohortId: Int? = null,
    val cohortParentId: Int? = null,
    val cohortName: String? = null,
    val cohortStatus: TrialStatus? = null,
    val cohortSlotsNumberAvailable: Int? = null,
    val cohortSlotsDateUpdate: String? = null
)