package com.hartwig.actin.personalization.datamodel

data class SubPopulation(
    val name: String,
    val patientCountByMeasurementType: Map<MeasurementType, Int>
)
