package com.hartwig.actin.algo.evidence

import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TumorMatch

data class Score(
    val event: String,
    val scoringMatch: ScoringMatch,
    val evidenceLevelDetails: EvidenceLevelDetails,
    val tumorType: CancerType,
    val score: Double,
    val evidenceDescription: String
) : Comparable<Score> {

    override fun compareTo(other: Score): Int {
        return score.compareTo(other.score)
    }
}

class TreatmentScorer {

    fun score(treatment: TreatmentEvidence): Score {
        val tumorMatch = when (treatment.tumorMatch) {
            TumorMatch.SPECIFIC -> com.hartwig.actin.algo.evidence.TumorMatch.EXACT
            TumorMatch.AGNOSTIC -> com.hartwig.actin.algo.evidence.TumorMatch.INEXACT
            TumorMatch.OFF -> com.hartwig.actin.algo.evidence.TumorMatch.OTHER
        }
        val exactVariant = if (treatment.molecularMatch.sourceEvidenceType.isCategoryEvent()) VariantMatch.CATEGORY else VariantMatch.EXACT
        val config = create()
        val scoringMatch = ScoringMatch(tumorMatch, exactVariant)
        val direction = if (treatment.evidenceDirection.hasBenefit) 1 else -1
        val factor = (config.categoryMatchLevels[scoringMatch] ?: 0) * direction
        val score = config.approvalPhaseLevel.scoring[treatment.evidenceLevelDetails] ?: 0
        return Score(
            scoringMatch = scoringMatch,
            evidenceLevelDetails = treatment.evidenceLevelDetails,
            score = factor * score.toDouble(),
            event = treatment.molecularMatch.sourceEvent,
            evidenceDescription = treatment.efficacyDescription,
            tumorType = treatment.applicableCancerType
        )
    }
}