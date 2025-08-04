package com.hartwig.actin.datamodel.trial

data class CohortAvailability(val open: Boolean, val slotsAvailable: Boolean)

data class CohortMetadata(
    val cohortId: String,
    val evaluable: Boolean,
    val cohortAvailability: CohortAvailability,
    val availabilityByLocation: Map<String, CohortAvailability>? = null,
    val ignore: Boolean,
    val description: String
) {
    val open = cohortAvailability.open
    val slotsAvailable = cohortAvailability.slotsAvailable
}
