package com.hartwig.actin.datamodel.algo

data class RankedTreatment(val treatment: String, val score: Double) : Comparable<RankedTreatment> {
    override fun compareTo(other: RankedTreatment): Int {
        return Comparator.comparingDouble<RankedTreatment> { it.score }.compare(this, other)
    }
}

data class TreatmentEvidenceRanking(val ranking: List<RankedTreatment>)