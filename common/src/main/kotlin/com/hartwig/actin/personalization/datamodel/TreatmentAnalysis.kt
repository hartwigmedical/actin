package com.hartwig.actin.personalization.datamodel

data class TreatmentAnalysis(
    val treatment: TreatmentGroup,
    val treatmentMeasurements: Map<MeasurementType, Map<String, Measurement>>,
)
