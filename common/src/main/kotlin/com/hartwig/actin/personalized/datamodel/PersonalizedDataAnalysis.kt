package com.hartwig.actin.personalized.datamodel

data class PersonalizedDataAnalysis(
    val treatmentAnalyses: List<TreatmentAnalysis>,
    val populations: List<Population>
)

