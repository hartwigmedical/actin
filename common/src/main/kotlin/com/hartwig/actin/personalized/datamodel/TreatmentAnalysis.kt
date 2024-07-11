package com.hartwig.actin.personalized.datamodel

data class TreatmentAnalysis(
    val treatment: TreatmentGroup,
    val treatmentMeasurements: Map<MeasurementType, Map<String, Measurement>>,
)
