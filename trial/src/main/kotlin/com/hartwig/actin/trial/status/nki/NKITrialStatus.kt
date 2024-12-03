package com.hartwig.actin.trial.status.nki

data class NKITrialStatus(
    val studyMetc: String?,
    val studyStatus: String?,
    val cohortId: String?,
    val cohortOpen: Boolean?
)