package com.hartwig.actin.datamodel.trial

typealias Location = String

data class CohortAvailability(val open: Boolean, val slotsAvailable: Boolean)

data class CohortMetadata(
    val cohortId: String,
    val description: String,
    val cohortAvailability: CohortAvailability,
    val availabilityByLocation: Map<Location, CohortAvailability>? = null,
    val evaluable: Boolean,
    val ignore: Boolean,
) {
    val open = cohortAvailability.open
    val slotsAvailable = cohortAvailability.slotsAvailable
}
