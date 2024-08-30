package com.hartwig.actin.datamodel.personalization

data class PersonalizedDataAnalysis(
    val treatmentAnalyses: List<TreatmentAnalysis>,
    val populations: List<Population>
)

