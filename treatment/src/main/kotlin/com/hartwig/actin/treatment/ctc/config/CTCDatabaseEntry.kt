package com.hartwig.actin.treatment.ctc.config

data class CTCDatabaseEntry(
    val studyId: Int,
    val studyMETC: String,
    val studyAcronym: String,
    val studyTitle: String,
    val studyStatus: String,
    val cohortId: Int? = null,
    val cohortParentId: Int? = null,
    val cohortName: String? = null,
    val cohortStatus: String? = null,
    val cohortSlotsNumberAvailable: Int? = null,
    val cohortSlotsDateUpdate: String? = null
)