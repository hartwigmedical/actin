package com.hartwig.actin.datamodel.personalization

data class TreatmentAnalysis(
    val treatment: TreatmentGroup,
    val measurementsByTypeAndPopulationName: Map<MeasurementType, Map<String, Measurement>>,
)
