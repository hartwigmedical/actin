package com.hartwig.actin.treatment.datamodel

data class CohortMetadata(
    val cohortId: String,
    val evaluable: Boolean,
    val open: Boolean,
    val slotsAvailable: Boolean,
    val blacklist: Boolean,
    val description: String
)
