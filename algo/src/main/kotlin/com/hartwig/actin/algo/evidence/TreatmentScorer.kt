package com.hartwig.actin.algo.evidence

import com.hartwig.actin.datamodel.molecular.evidence.EvidenceApprovalPhase
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence

data class Score(
    val variant: String,
    val scoringMatch: ScoringMatch,
    val evidenceLevelDetails: EvidenceApprovalPhase,
    val factor: Int,
    val score: Double,
    val evidenceDescription: String
) : Comparable<Score> {

    fun score() = factor * score

    override fun compareTo(other: Score): Int {
        return score().compareTo(other.score)
    }
}

class TreatmentScorer {

    fun score(treatment: TreatmentEvidence): Score {
        val onLabel = if (treatment.isOnLabel) TumorMatch.ON_LABEL else TumorMatch.OFF_LABEL
        val exactVariant = if (treatment.molecularMatch.isCategoryEvent) VariantMatch.CATEGORY else VariantMatch.EXACT
        val config = create()
        val scoringMatch = ScoringMatch(onLabel, exactVariant)
        val direction = if (treatment.evidenceDirection.hasBenefit) 1 else -1
        val factor = (config.categoryMatchLevels[scoringMatch] ?: 0) * direction
        val score = config.approvalPhaseLevel.scoring[treatment.evidenceLevelDetails] ?: 0
        return Score(
            scoringMatch = scoringMatch,
            evidenceLevelDetails = treatment.evidenceLevelDetails,
            factor = factor,
            score = score.toDouble(),
            variant = treatment.molecularMatch.sourceEvent,
            evidenceDescription = treatment.efficacyDescription
        )
    }
}