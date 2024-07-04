package com.hartwig.actin.personalization.datamodel

data class PersonalizedDataAnalysis(
    val treatmentAnalyses: List<TreatmentAnalysis>,
    val subPopulations: List<SubPopulation>
)

