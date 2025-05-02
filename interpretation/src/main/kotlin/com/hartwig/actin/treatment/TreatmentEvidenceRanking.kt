package com.hartwig.actin.treatment

data class RankedTreatment(
    val treatment: String,
    val events: Set<String>,
    val score: Double,
)

data class TreatmentEvidenceRanking(val ranking: List<RankedTreatment>)