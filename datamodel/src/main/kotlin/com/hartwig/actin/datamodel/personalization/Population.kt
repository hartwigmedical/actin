package com.hartwig.actin.datamodel.personalization

data class Population(
    val name: String,
    val patientCountByMeasurementType: Map<MeasurementType, Int>
)
