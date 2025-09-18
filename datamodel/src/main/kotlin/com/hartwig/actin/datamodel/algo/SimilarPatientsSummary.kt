package com.hartwig.actin.datamodel.algo

data class SimilarPatientsSummary(
    val overallTreatmentProportion: List<TreatmentProportion>,
    val similarPatientsTreatmentProportion: List<TreatmentProportion>
) {
}