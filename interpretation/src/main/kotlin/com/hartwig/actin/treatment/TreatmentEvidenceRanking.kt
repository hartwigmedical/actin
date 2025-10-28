package com.hartwig.actin.treatment

data class RankedTreatment(
    val treatment: String,
    val events: String?,
    val score: Double,
)

data class TreatmentEvidenceRanking(val ranking: List<RankedTreatment>)