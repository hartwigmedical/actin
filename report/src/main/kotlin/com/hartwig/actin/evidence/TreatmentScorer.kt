package com.hartwig.actin.evidence

import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence

data class Score(
    val variant: String,
    val scoringMatch: ScoringMatch,
    val evidenceLevelDetails: EvidenceLevelDetails,
    val factor: Int,
    val score: Int,
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
        val factor = (config.levels[scoringMatch] ?: 0) * direction
        val score = config.level.scoring[treatment.evidenceLevelDetails] ?: 0
        return Score(
            scoringMatch = scoringMatch,
            evidenceLevelDetails = treatment.evidenceLevelDetails,
            factor = factor,
            score = score,
            variant = treatment.molecularMatch.sourceEvent,
            evidenceDescription = treatment.efficacyDescription
        )
    }
}