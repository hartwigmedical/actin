package com.hartwig.actin.personalized.datamodel

data class TreatmentAnalysis(
    val treatment: TreatmentGroup,
    val measurementsByTypeAndPopulationName: Map<MeasurementType, Map<String, Measurement>>,
)
