package com.hartwig.actin.trial.status.nki

data class NKITrialStatus(
    val studyId: String,
    val studyMetc: String?,
    val studyAcronym: String?,
    val studyTitle: String?,
    val studyStatus: String?,
    val studySlotsNumberAvailable: Int?,
)