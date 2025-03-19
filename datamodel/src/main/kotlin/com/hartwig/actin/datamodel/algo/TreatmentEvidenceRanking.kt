package com.hartwig.actin.datamodel.algo

data class RankedTreatment(
    val treatment: String,
    val event: String,
    val score: Double,
)

data class TreatmentEvidenceRanking(val ranking: List<RankedTreatment>)