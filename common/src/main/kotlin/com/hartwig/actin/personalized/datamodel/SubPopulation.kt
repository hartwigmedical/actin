package com.hartwig.actin.personalized.datamodel

data class SubPopulation(
    val name: String,
    val patientCountByMeasurementType: Map<MeasurementType, Int>
)
