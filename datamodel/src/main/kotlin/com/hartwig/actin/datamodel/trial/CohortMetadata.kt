package com.hartwig.actin.datamodel.trial

data class CohortMetadata(
    val cohortId: String,
    val evaluable: Boolean,
    val open: Boolean,
    val slotsAvailable: Boolean,
    val ignore: Boolean,
    val description: String
)
