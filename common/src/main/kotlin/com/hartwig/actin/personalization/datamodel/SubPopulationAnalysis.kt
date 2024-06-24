package com.hartwig.actin.personalization.datamodel

import com.hartwig.actin.clinical.datamodel.treatment.Treatment

data class TreatmentMeasurementCollection(val measurementsByTreatment: Map<Treatment, Measurement>, val numPatients: Int)

data class SubPopulationAnalysis(
    val name: String,
    val treatmentMeasurements: Map<MeasurementType, TreatmentMeasurementCollection>,
    val treatments: List<Treatment>
)
