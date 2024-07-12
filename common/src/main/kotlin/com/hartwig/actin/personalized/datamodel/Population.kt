package com.hartwig.actin.personalized.datamodel

data class Population(
    val name: String,
    val patientCountByMeasurementType: Map<MeasurementType, Int>
)
